package com.employee.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.employee.dto.CentralOfficeChecklistDTO;
import com.employee.entity.Employee;
import com.employee.entity.EmployeeCheckListStatus;
import com.employee.exception.ResourceNotFoundException;
import com.employee.repository.EmpAppCheckListDetlRepository;
import com.employee.repository.EmployeeRepository;
import com.employee.repository.EmployeeCheckListStatusRepository;

/**
 * Service for Central Office Level operations
 * Handles checklist updates at Central Office level
 */
@Service
@Transactional
public class CentralOfficeLevelService {

	private static final Logger logger = LoggerFactory.getLogger(CentralOfficeLevelService.class);

	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private EmpAppCheckListDetlRepository empAppCheckListDetlRepository;
	
	@Autowired
	private EmployeeCheckListStatusRepository employeeCheckListStatusRepository;

	/**
	 * Update checklist IDs and notice period for an employee based on temp_payroll_id
	 * Also updates emp_check_list_status_id from "Forward to CO" to "Confirm"
	 * 
	 * Flow:
	 * 1. Validate temp_payroll_id exists in Employee table
	 * 2. Find employee by temp_payroll_id
	 * 3. Check if current status is "Forward to CO", if yes update to "Confirm"
	 * 4. Validate all checklist IDs exist and are active in EmpAppCheckListDetl table
	 * 5. Update emp_app_check_list_detl_id in Employee table
	 * 6. Update notice_period in Employee table (if provided)
	 * 
	 * @param checklistDTO DTO containing tempPayrollId, checkListIds, and optional noticePeriod
	 * @return Updated DTO with the saved checklist IDs and notice period
	 * @throws ResourceNotFoundException if employee not found or checklist IDs are invalid
	 */
	public CentralOfficeChecklistDTO updateChecklist(CentralOfficeChecklistDTO checklistDTO) {
		// Validation: Check if tempPayrollId is provided
		if (checklistDTO.getTempPayrollId() == null || checklistDTO.getTempPayrollId().trim().isEmpty()) {
			throw new ResourceNotFoundException("tempPayrollId is required. Please provide a valid temp_payroll_id.");
		}
		
		// Validation: Check if checkListIds is provided
		if (checklistDTO.getCheckListIds() == null || checklistDTO.getCheckListIds().trim().isEmpty()) {
			throw new ResourceNotFoundException("checkListIds is required. Please provide checklist IDs (comma-separated string).");
		}
		
		logger.info("Updating checklist for temp_payroll_id: {}", checklistDTO.getTempPayrollId());
		
		// Step 1: Validate tempPayrollId exists in Employee table
		validateTempPayrollId(checklistDTO.getTempPayrollId());
		
		// Step 2: Find employee by temp_payroll_id (already validated, so this should not throw)
		Employee employee = employeeRepository.findByTemp_payroll_id(checklistDTO.getTempPayrollId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Employee not found with temp_payroll_id: " + checklistDTO.getTempPayrollId()));
		
		Integer empId = employee.getEmp_id();
		logger.info("Found employee with emp_id: {} for temp_payroll_id: {}", empId, checklistDTO.getTempPayrollId());
		
		// Step 3: Check if current status is "Forward to CO", update to "Confirm"
		if (employee.getEmp_check_list_status_id() != null && 
		    "Forward to CO".equals(employee.getEmp_check_list_status_id().getCheck_app_status_name())) {
			// Capture old status ID before updating
			Integer oldStatusId = employee.getEmp_check_list_status_id().getEmp_app_status_id();
			EmployeeCheckListStatus confirmStatus = employeeCheckListStatusRepository.findByCheck_app_status_name("Confirm")
					.orElseThrow(() -> new ResourceNotFoundException("EmployeeCheckListStatus with name 'Confirm' not found"));
			employee.setEmp_check_list_status_id(confirmStatus);
			logger.info("Updated employee (emp_id: {}) status from 'Forward to CO' (ID: {}) to 'Confirm' (ID: {})", 
					empId, oldStatusId, confirmStatus.getEmp_app_status_id());
		} else {
			String currentStatusName = employee.getEmp_check_list_status_id() != null ? 
					employee.getEmp_check_list_status_id().getCheck_app_status_name() : null;
			logger.info("Employee (emp_id: {}) current status is '{}', not updating to 'Confirm'. Status 'Confirm' is only set when current status is 'Forward to CO'.", 
					empId, currentStatusName);
		}
		
		// Step 4: Validate checklist IDs before saving
		validateCheckListIds(checklistDTO.getCheckListIds());
		
		// Step 5: Update emp_app_check_list_detl_id in Employee table
		employee.setEmp_app_check_list_detl_id(checklistDTO.getCheckListIds());
		
		// Step 6: Update notice_period in Employee table (if provided)
		if (checklistDTO.getNoticePeriod() != null) {
			employee.setNotice_period(checklistDTO.getNoticePeriod().trim());
			logger.info("Updated notice period for employee (emp_id: {}): {}", empId, checklistDTO.getNoticePeriod());
		}
		
		employeeRepository.save(employee);
		
		logger.info("Successfully updated checklist IDs for employee (emp_id: {}, temp_payroll_id: '{}'): {}", 
				empId, checklistDTO.getTempPayrollId(), checklistDTO.getCheckListIds());
		
		// Return the DTO with updated data
		return checklistDTO;
	}
	
	/**
	 * Validate checklist IDs - checks if all IDs in comma-separated string exist in EmpAppCheckListDetl table
	 * @param checkListIds Comma-separated string of checklist IDs (e.g., "1,2,3,4,5,6,7")
	 * @throws ResourceNotFoundException if any checklist ID is invalid or not found
	 */
	private void validateCheckListIds(String checkListIds) {
		if (checkListIds == null || checkListIds.trim().isEmpty()) {
			return; // No validation needed if empty
		}
		
		// Split comma-separated string into individual IDs
		String[] idArray = checkListIds.split(",");
		
		for (String idStr : idArray) {
			idStr = idStr.trim(); // Remove whitespace
			
			if (idStr.isEmpty()) {
				continue; // Skip empty strings
			}
			
			try {
				Integer checklistId = Integer.parseInt(idStr);
				
				// Check if checklist ID exists and is active in EmpAppCheckListDetl table
				empAppCheckListDetlRepository.findByIdAndIsActive(checklistId, 1)
					.orElseThrow(() -> new ResourceNotFoundException(
							"Checklist ID " + checklistId + " not found or inactive in checklist master table. " +
							"Please provide valid checklist IDs. Provided checklist IDs: " + checkListIds));
				
				logger.debug("Validated checklist ID: {} exists and is active", checklistId);
				
			} catch (NumberFormatException e) {
				throw new ResourceNotFoundException(
						"Invalid checklist ID format: '" + idStr + "'. Checklist IDs must be numeric. " +
						"Provided checklist IDs: " + checkListIds);
			}
		}
		
		logger.info("✅ All checklist IDs validated successfully: {}", checkListIds);
	}
	
	/**
	 * Validate tempPayrollId - checks if employee exists with the given temp_payroll_id
	 * @param tempPayrollId The temp_payroll_id to validate
	 * @throws ResourceNotFoundException if employee not found with the given temp_payroll_id
	 */
	private void validateTempPayrollId(String tempPayrollId) {
		if (tempPayrollId == null || tempPayrollId.trim().isEmpty()) {
			throw new ResourceNotFoundException("tempPayrollId is required. Please provide a valid temp_payroll_id.");
		}
		
		// Check if employee exists with the given temp_payroll_id
		employeeRepository.findByTemp_payroll_id(tempPayrollId.trim())
			.orElseThrow(() -> new ResourceNotFoundException(
					"Employee not found with temp_payroll_id: " + tempPayrollId + ". " +
					"Please provide a valid temp_payroll_id that exists in the Employee table."));
		
		logger.info("✅ Validated temp_payroll_id exists: {}", tempPayrollId);
	}
}

