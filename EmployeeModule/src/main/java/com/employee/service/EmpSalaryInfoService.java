package com.employee.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.employee.dto.SalaryInfoDTO;
import com.employee.entity.BankDetails;
import com.employee.entity.CostCenter;
import com.employee.entity.EmpGrade;
import com.employee.entity.EmpPaymentType;
import com.employee.entity.EmpPfDetails;
import com.employee.entity.EmpSalaryInfo;
import com.employee.entity.EmpStructure;
import com.employee.entity.Employee;
import com.employee.entity.EmployeeCheckListStatus;
import com.employee.exception.ResourceNotFoundException;
import com.employee.repository.BankDetailsRepository;
import com.employee.repository.CostCenterRepository;
import com.employee.repository.EmpGradeRepository;
import com.employee.repository.EmpPaymentTypeRepository;
import com.employee.repository.EmpPfDetailsRepository;
import com.employee.repository.EmpSalaryInfoRepository;
import com.employee.repository.EmpStructureRepository;
import com.employee.repository.EmployeeRepository;
import com.employee.repository.EmployeeCheckListStatusRepository;

@Service
@Transactional
public class EmpSalaryInfoService {

	private static final Logger logger = LoggerFactory.getLogger(EmpSalaryInfoService.class);

	@Autowired
	private EmpSalaryInfoRepository empSalaryInfoRepository;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private BankDetailsRepository bankDetailsRepository;

	@Autowired
	private EmpPaymentTypeRepository empPaymentTypeRepository;
	
	@Autowired
	private EmpStructureRepository empStructureRepository;
	
	@Autowired
	private CostCenterRepository costCenterRepository;
	
	@Autowired
	private EmpGradeRepository empGradeRepository;
	
	@Autowired
	private EmpPfDetailsRepository empPfDetailsRepository;
	
	@Autowired
	private EmployeeCheckListStatusRepository employeeCheckListStatusRepository;

	/**
	 * Create salary info based on temp_payroll_id
	 * Flow:
	 * 1. Find employee by temp_payroll_id (emp_id will be fetched from Employee table)
	 * 2. Get emp_payment_type_id from BankDetails table for that emp_id
	 * 3. Post data to EmpSalaryInfo table with emp_id and emp_payment_type_id
	 * 4. Update checklist in Employee table (emp_app_check_list_detl_id)
	 * 5. Update app status to 3
	 * 
	 * Returns DTO with all the saved data (no entity relationships to avoid circular references)
	 */
	public SalaryInfoDTO createSalaryInfo(SalaryInfoDTO salaryInfoDTO) {
		// Validation: Check if tempPayrollId is provided
		if (salaryInfoDTO.getTempPayrollId() == null || salaryInfoDTO.getTempPayrollId().trim().isEmpty()) {
			throw new ResourceNotFoundException("tempPayrollId is required. Please provide a valid temp_payroll_id.");
		}
		
		// Validation: Check required salary fields
		if (salaryInfoDTO.getMonthlyCtc() == null) {
			throw new ResourceNotFoundException("monthlyCtc is required (NOT NULL column)");
		}
		if (salaryInfoDTO.getYearlyCtc() == null) {
			throw new ResourceNotFoundException("yearlyCtc is required (NOT NULL column)");
		}
		if (salaryInfoDTO.getEmpStructureId() == null) {
			throw new ResourceNotFoundException("empStructureId is required (NOT NULL column)");
		}
		
		logger.info("Creating salary info for temp_payroll_id: {}", salaryInfoDTO.getTempPayrollId());

		// Step 1: Find employee by temp_payroll_id
		Employee employee = employeeRepository.findByTemp_payroll_id(salaryInfoDTO.getTempPayrollId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Employee not found with temp_payroll_id: '" + salaryInfoDTO.getTempPayrollId() + 
						"'. Please verify the temp_payroll_id is correct and the employee exists in the system."));

		Integer empId = employee.getEmp_id();
		logger.info("Found employee - temp_payroll_id: {}, emp_id: {}", 
				employee.getTemp_payroll_id(), empId);
		
		// Additional validation: Check if employee is active
		if (employee.getIs_active() != 1) {
			throw new ResourceNotFoundException(
					"Employee with temp_payroll_id: '" + salaryInfoDTO.getTempPayrollId() + 
					"' is not active. emp_id: " + empId);
		}

		// Step 2: Get emp_payment_type_id from BankDetails table (where emp_id matches)
		EmpPaymentType empPaymentType = null;
		List<BankDetails> bankDetailsList = bankDetailsRepository.findByEmpId_Emp_id(empId);
		
		if (bankDetailsList != null && !bankDetailsList.isEmpty()) {
			logger.info("Found {} BankDetails record(s) for emp_id: {}", bankDetailsList.size(), empId);
			// Get payment type from bank details - prefer salary account if available
			for (BankDetails bankDetail : bankDetailsList) {
				logger.info("Checking BankDetails - acc_type: {}, emp_payment_type_id: {}", 
						bankDetail.getAccType(), 
						bankDetail.getEmpPaymentType() != null ? bankDetail.getEmpPaymentType().getEmp_payment_type_id() : "null");
				
				if (bankDetail.getEmpPaymentType() != null) {
					// Reload the entity from repository to ensure it's managed
					Integer paymentTypeId = bankDetail.getEmpPaymentType().getEmp_payment_type_id();
					empPaymentType = empPaymentTypeRepository.findById(paymentTypeId)
							.orElse(null);
					
					if (empPaymentType != null) {
						logger.info("Loaded emp_payment_type_id: {} from repository (acc_type: {}) for emp_id: {}", 
								empPaymentType.getEmp_payment_type_id(), bankDetail.getAccType(), empId);
						// Prefer salary account if available
						if ("SALARY".equalsIgnoreCase(bankDetail.getAccType())) {
							logger.info("Using emp_payment_type_id from SALARY account type");
							break;
						}
					} else {
						logger.warn("EmpPaymentType with ID {} not found in repository", paymentTypeId);
					}
				}
			}
			
			if (empPaymentType == null) {
				logger.warn("No emp_payment_type_id found in any BankDetails records for emp_id: {}. Will be set to null.", empId);
			}
		} else {
			logger.warn("No BankDetails found for emp_id: {}. emp_payment_type_id will be null.", empId);
		}

		// Step 3: Create and save EmpSalaryInfo entity
		EmpSalaryInfo empSalaryInfo = new EmpSalaryInfo();
		empSalaryInfo.setEmpId(employee); // Set emp_id (FK to Employee table) - fetched from Employee using temp_payroll_id
		
		// Set temp_payroll_id from Employee table (store in temp_payroll_id column)
		if (employee.getTemp_payroll_id() != null && !employee.getTemp_payroll_id().trim().isEmpty()) {
			empSalaryInfo.setTempPayrollId(employee.getTemp_payroll_id());
			logger.info("Storing temp_payroll_id '{}' in temp_payroll_id column", employee.getTemp_payroll_id());
		} else {
			throw new ResourceNotFoundException("Employee with temp_payroll_id: '" + salaryInfoDTO.getTempPayrollId() + 
					"' does not have temp_payroll_id set in Employee table.");
		}
		
		// Set payroll_id to null (as per team's approach - will be updated later)
		empSalaryInfo.setPayrollId(null);
		logger.info("Setting payroll_id to null (will be updated later when available)");
		
		// Set emp_payment_type_id from BankDetails (for that emp_id)
		empSalaryInfo.setEmpPaymentType(empPaymentType);
		if (empPaymentType != null) {
			logger.info("Storing emp_payment_type_id: {} in EmpSalaryInfo (retrieved from BankDetails)", 
					empPaymentType.getEmp_payment_type_id());
		} else {
			logger.warn("emp_payment_type_id is null - not found in BankDetails for emp_id: {}", empId);
		}
		empSalaryInfo.setMonthlyCtc(salaryInfoDTO.getMonthlyCtc()); // From swagger
		empSalaryInfo.setCtcWords(salaryInfoDTO.getCtcWords()); // From swagger (optional)
		empSalaryInfo.setYearlyCtc(salaryInfoDTO.getYearlyCtc()); // From swagger
		
		// Set emp_structure_id as relationship (fetch from master table)
		if (salaryInfoDTO.getEmpStructureId() != null) {
			EmpStructure empStructure = empStructureRepository.findByIdAndIsActive(salaryInfoDTO.getEmpStructureId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active EmpStructure not found with ID: " + salaryInfoDTO.getEmpStructureId()));
			empSalaryInfo.setEmpStructure(empStructure);
			logger.info("Set emp_structure_id: {} (structure name: {})", empStructure.getEmpStructureId(), empStructure.getStructreName());
		} else {
			throw new ResourceNotFoundException("empStructureId is required (NOT NULL column)");
		}
		
		// Set grade_id as relationship (optional)
		if (salaryInfoDTO.getGradeId() != null) {
			EmpGrade empGrade = empGradeRepository.findByIdAndIsActive(salaryInfoDTO.getGradeId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active EmpGrade not found with ID: " + salaryInfoDTO.getGradeId()));
			empSalaryInfo.setGrade(empGrade);
			logger.info("Set grade_id: {} (grade name: {})", empGrade.getEmpGradeId(), empGrade.getGradeName());
		} else {
			empSalaryInfo.setGrade(null);
			logger.info("grade_id is null (optional field)");
		}
		
		// Set cost_center_id as relationship (optional)
		if (salaryInfoDTO.getCostCenterId() != null) {
			CostCenter costCenter = costCenterRepository.findById(salaryInfoDTO.getCostCenterId())
					.orElseThrow(() -> new ResourceNotFoundException("CostCenter not found with ID: " + salaryInfoDTO.getCostCenterId()));
			empSalaryInfo.setCostCenter(costCenter);
			logger.info("Set cost_center_id: {} (cost center name: {})", costCenter.getCostCenterId(), costCenter.getCostCenterName());
		} else {
			empSalaryInfo.setCostCenter(null);
			logger.info("cost_center_id is null (optional field)");
		}
		
		// Set is_pf_eligible (required NOT NULL, default 0)
		if (salaryInfoDTO.getIsPfEligible() != null) {
			empSalaryInfo.setIsPfEligible(salaryInfoDTO.getIsPfEligible());
		} else {
			empSalaryInfo.setIsPfEligible(0); // Default to 0 if not provided
		}
		logger.info("Set is_pf_eligible: {}", empSalaryInfo.getIsPfEligible());
		
		// Set is_esi_eligible (required NOT NULL, default 0)
		if (salaryInfoDTO.getIsEsiEligible() != null) {
			empSalaryInfo.setIsEsiEligible(salaryInfoDTO.getIsEsiEligible());
		} else {
			empSalaryInfo.setIsEsiEligible(0); // Default to 0 if not provided
		}
		logger.info("Set is_esi_eligible: {}", empSalaryInfo.getIsEsiEligible());
		
		empSalaryInfo.setIsActive(1);

		// Step 4: Save to salary table
		// Log before save to verify relationships are set
		logger.info("Before save - emp_payment_type_id: {}, grade_id: {}", 
				empSalaryInfo.getEmpPaymentType() != null ? empSalaryInfo.getEmpPaymentType().getEmp_payment_type_id() : "null",
				empSalaryInfo.getGrade() != null ? empSalaryInfo.getGrade().getEmpGradeId() : "null");
		
		EmpSalaryInfo savedSalaryInfo = empSalaryInfoRepository.save(empSalaryInfo);
		
		// Flush to ensure data is persisted immediately
		empSalaryInfoRepository.flush();
		
		// Log after save to verify relationships are saved
		logger.info("After save - emp_sal_info_id: {}, emp_id: {}, emp_payment_type_id: {}, grade_id: {}", 
				savedSalaryInfo.getEmpSalInfoId(), 
				empId,
				savedSalaryInfo.getEmpPaymentType() != null ? savedSalaryInfo.getEmpPaymentType().getEmp_payment_type_id() : "null",
				savedSalaryInfo.getGrade() != null ? savedSalaryInfo.getGrade().getEmpGradeId() : "null");

		// Step 4.5: Update PF/ESI/UAN details in EmpPfDetails table
		// Find existing EmpPfDetails or create new one
		EmpPfDetails empPfDetails = empPfDetailsRepository.findByEmployeeId(empId)
				.orElse(new EmpPfDetails());
		
		boolean pfDetailsNeedsSave = false;
		
		// If PF eligible (isPfEligible = 1), set PF number and PF join date
		if (empSalaryInfo.getIsPfEligible() != null && empSalaryInfo.getIsPfEligible() == 1) {
			if (salaryInfoDTO.getPfNo() != null && !salaryInfoDTO.getPfNo().trim().isEmpty()) {
				empPfDetails.setPf_no(salaryInfoDTO.getPfNo());
				pfDetailsNeedsSave = true;
				logger.info("Set PF number: {} for emp_id: {} (PF eligible)", salaryInfoDTO.getPfNo(), empId);
			}
			if (salaryInfoDTO.getPfJoinDate() != null) {
				empPfDetails.setPf_join_date(salaryInfoDTO.getPfJoinDate());
				pfDetailsNeedsSave = true;
				logger.info("Set PF join date: {} for emp_id: {} (PF eligible)", salaryInfoDTO.getPfJoinDate(), empId);
			}
		} else {
			logger.info("PF not eligible (isPfEligible = {}), skipping PF number and join date", empSalaryInfo.getIsPfEligible());
		}
		
		// If ESI eligible (isEsiEligible = 1), set ESI number
		if (empSalaryInfo.getIsEsiEligible() != null && empSalaryInfo.getIsEsiEligible() == 1) {
			if (salaryInfoDTO.getEsiNo() != null) {
				empPfDetails.setEsi_no(salaryInfoDTO.getEsiNo());
				pfDetailsNeedsSave = true;
				logger.info("Set ESI number: {} for emp_id: {} (ESI eligible)", salaryInfoDTO.getEsiNo(), empId);
			}
		} else {
			logger.info("ESI not eligible (isEsiEligible = {}), skipping ESI number", empSalaryInfo.getIsEsiEligible());
		}
		
		// Always set UAN number if provided (no validation)
		if (salaryInfoDTO.getUanNo() != null) {
			empPfDetails.setUan_no(salaryInfoDTO.getUanNo());
			pfDetailsNeedsSave = true;
			logger.info("Set UAN number: {} for emp_id: {}", salaryInfoDTO.getUanNo(), empId);
		}
		
		// Save EmpPfDetails if any changes were made
		if (pfDetailsNeedsSave) {
			empPfDetails.setEmployee_id(employee);
			empPfDetails.setIs_active(1);
			if (empPfDetails.getEmp_pf_esi_uan_info_id() == 0) {
				// New record - set created_by
				empPfDetails.setCreated_by(employee.getCreated_by());
			}
			empPfDetailsRepository.save(empPfDetails);
			logger.info("Saved/Updated PF/ESI/UAN details for emp_id: {}", empId);
		}

		// Step 5: Update checklist in Employee table (emp_app_check_list_detl_id)
		boolean needsUpdate = false;
		if (salaryInfoDTO.getCheckListIds() != null && !salaryInfoDTO.getCheckListIds().trim().isEmpty()) {
			employee.setEmp_app_check_list_detl_id(salaryInfoDTO.getCheckListIds());
			needsUpdate = true;
			logger.info("Setting checklist IDs for employee (emp_id: {}): {}", empId, salaryInfoDTO.getCheckListIds());
		}
		
		// Step 6: Update app status to 3 (always update, not just if current is 2)
		EmployeeCheckListStatus status3 = employeeCheckListStatusRepository.findById(3)
				.orElseThrow(() -> new ResourceNotFoundException("EmployeeCheckListStatus with ID 3 not found"));
		employee.setEmp_check_list_status_id(status3);
		needsUpdate = true;
		logger.info("Updated employee (emp_id: {}) app status to 3", empId);
		
		// Save employee updates (checklist and status)
		if (needsUpdate) {
			employeeRepository.save(employee);
			if (salaryInfoDTO.getCheckListIds() != null && !salaryInfoDTO.getCheckListIds().trim().isEmpty()) {
				logger.info("Updated employee (emp_id: {}) with checklist IDs: {}", 
						empId, salaryInfoDTO.getCheckListIds());
			}
		}
		
		// Return the DTO with all the data (no entity relationships to avoid circular references)
		// The DTO already contains all the input data, and we've updated the employee with checklist IDs
		// No need to refresh - we already have the checklist IDs in the DTO and employee object
		// The checklist IDs are already set in salaryInfoDTO from the input, so just return it
		return salaryInfoDTO;
	}

	/**
	 * Get salary info by temp_payroll_id (returns entity - for internal use)
	 */
	public EmpSalaryInfo getSalaryInfoByTempPayrollId(String tempPayrollId) {
		Employee employee = employeeRepository.findByTemp_payroll_id(tempPayrollId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Employee not found with temp_payroll_id: " + tempPayrollId));

		return empSalaryInfoRepository.findByEmpIdAndIsActive(employee.getEmp_id(), 1)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Salary info not found for employee with temp_payroll_id: " + tempPayrollId));
	}
	
	/**
	 * Get salary info by temp_payroll_id and return as DTO (for API response)
	 */
	public SalaryInfoDTO getSalaryInfoByTempPayrollIdAsDTO(String tempPayrollId) {
		Employee employee = employeeRepository.findByTemp_payroll_id(tempPayrollId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Employee not found with temp_payroll_id: " + tempPayrollId));

		EmpSalaryInfo empSalaryInfo = empSalaryInfoRepository.findByEmpIdAndIsActive(employee.getEmp_id(), 1)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Salary info not found for employee with temp_payroll_id: " + tempPayrollId));
		
		// Convert entity to DTO
		SalaryInfoDTO salaryInfoDTO = new SalaryInfoDTO();
		salaryInfoDTO.setTempPayrollId(tempPayrollId);
		salaryInfoDTO.setMonthlyCtc(empSalaryInfo.getMonthlyCtc());
		salaryInfoDTO.setCtcWords(empSalaryInfo.getCtcWords());
		salaryInfoDTO.setYearlyCtc(empSalaryInfo.getYearlyCtc());
		salaryInfoDTO.setEmpStructureId(empSalaryInfo.getEmpStructure() != null ? empSalaryInfo.getEmpStructure().getEmpStructureId() : null);
		salaryInfoDTO.setGradeId(empSalaryInfo.getGrade() != null ? empSalaryInfo.getGrade().getEmpGradeId() : null);
		salaryInfoDTO.setCostCenterId(empSalaryInfo.getCostCenter() != null ? empSalaryInfo.getCostCenter().getCostCenterId() : null);
		salaryInfoDTO.setIsPfEligible(empSalaryInfo.getIsPfEligible());
		salaryInfoDTO.setIsEsiEligible(empSalaryInfo.getIsEsiEligible());
		
		// Get PF/ESI/UAN details from EmpPfDetails
		Optional<EmpPfDetails> empPfDetailsOpt = empPfDetailsRepository.findByEmployeeId(employee.getEmp_id());
		if (empPfDetailsOpt.isPresent()) {
			EmpPfDetails empPfDetails = empPfDetailsOpt.get();
			salaryInfoDTO.setPfNo(empPfDetails.getPf_no());
			salaryInfoDTO.setPfJoinDate(empPfDetails.getPf_join_date());
			salaryInfoDTO.setEsiNo(empPfDetails.getEsi_no());
			salaryInfoDTO.setUanNo(empPfDetails.getUan_no());
		}
		
		salaryInfoDTO.setCheckListIds(employee.getEmp_app_check_list_detl_id());
		
		return salaryInfoDTO;
	}
}

