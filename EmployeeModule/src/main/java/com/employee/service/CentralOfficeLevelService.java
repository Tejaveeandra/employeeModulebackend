package com.employee.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.employee.dto.CentralOfficeChecklistDTO;
import com.employee.dto.RejectBackToDODTO;
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
	 * Also updates emp_check_list_status_id from "Pending at CO" to "Confirm"
	 * 
	 * Flow:
	 * 1. Validate temp_payroll_id exists in Employee table
	 * 2. Find employee by temp_payroll_id
	 * 3. Check if current status is "Pending at CO", if yes update to "Confirm"
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
		
		// Step 3: Check if current status is "Pending at CO", update to "Confirm"
		if (employee.getEmp_check_list_status_id() != null && 
		    "Pending at CO".equals(employee.getEmp_check_list_status_id().getCheck_app_status_name())) {
			// Capture old status ID before updating
			Integer oldStatusId = employee.getEmp_check_list_status_id().getEmp_app_status_id();
			EmployeeCheckListStatus confirmStatus = employeeCheckListStatusRepository.findByCheck_app_status_name("Confirm")
					.orElseThrow(() -> new ResourceNotFoundException("EmployeeCheckListStatus with name 'Confirm' not found"));
			employee.setEmp_check_list_status_id(confirmStatus);
			logger.info("Updated employee (emp_id: {}) status from 'Pending at CO' (ID: {}) to 'Confirm' (ID: {})", 
					empId, oldStatusId, confirmStatus.getEmp_app_status_id());
			
			// Clear remarks when confirming (as per requirement)
			employee.setRemarks(null);
			logger.info("Cleared remarks for employee (emp_id: {}) when confirming", empId);
		} else {
			String currentStatusName = employee.getEmp_check_list_status_id() != null ? 
					employee.getEmp_check_list_status_id().getCheck_app_status_name() : null;
			logger.info("Employee (emp_id: {}) current status is '{}', not updating to 'Confirm'. Status 'Confirm' is only set when current status is 'Pending at CO'.", 
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
	
	/**
	 * Reject and send employee back to DO (Demand Officer)
	 * This method is called when Central Office rejects an employee application
	 * 
	 * Flow:
	 * 1. Validate temp_payroll_id exists in Employee table
	 * 2. Find employee by temp_payroll_id
	 * 3. Validate that current status is "Pending at CO" (required)
	 * 4. Update status to "Back to DO"
	 * 5. Update remarks (if remarks already exist, update them; if not, set new remarks)
	 * 
	 * @param rejectDTO DTO containing tempPayrollId and remarks
	 * @return Updated DTO with the saved data
	 * @throws ResourceNotFoundException if employee not found or status is not "Pending at CO"
	 */
	public RejectBackToDODTO rejectBackToDO(RejectBackToDODTO rejectDTO) {
		// Validation: Check if tempPayrollId is provided
		if (rejectDTO.getTempPayrollId() == null || rejectDTO.getTempPayrollId().trim().isEmpty()) {
			throw new ResourceNotFoundException("tempPayrollId is required. Please provide a valid temp_payroll_id.");
		}
		
		// Validation: Check if remarks is provided
		if (rejectDTO.getRemarks() == null || rejectDTO.getRemarks().trim().isEmpty()) {
			throw new ResourceNotFoundException("remarks is required. Please provide a reason for rejecting and sending back to DO.");
		}
		
		// Validation: Check remarks length (max 250 characters)
		if (rejectDTO.getRemarks().length() > 250) {
			throw new IllegalArgumentException("remarks cannot exceed 250 characters. Current length: " + rejectDTO.getRemarks().length());
		}
		
		logger.info("Rejecting employee and sending back to DO - temp_payroll_id: {}, remarks: {}", 
				rejectDTO.getTempPayrollId(), rejectDTO.getRemarks());
		
		// Step 1: Validate tempPayrollId exists in Employee table
		validateTempPayrollId(rejectDTO.getTempPayrollId());
		
		// Step 2: Find employee by temp_payroll_id
		Employee employee = employeeRepository.findByTemp_payroll_id(rejectDTO.getTempPayrollId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Employee not found with temp_payroll_id: " + rejectDTO.getTempPayrollId()));
		
		Integer empId = employee.getEmp_id();
		logger.info("Found employee with emp_id: {} for temp_payroll_id: {}", empId, rejectDTO.getTempPayrollId());
		
		// Step 3: Validate that current status is "Pending at CO" - this method only works for "Pending at CO" status
		if (employee.getEmp_check_list_status_id() == null) {
			throw new ResourceNotFoundException(
					"Cannot reject employee: Employee (emp_id: " + empId + 
					", temp_payroll_id: '" + rejectDTO.getTempPayrollId() + 
					"') does not have a status set. This method only works when employee status is 'Pending at CO'.");
		}
		
		String currentStatusName = employee.getEmp_check_list_status_id().getCheck_app_status_name();
		if (!"Pending at CO".equals(currentStatusName)) {
			throw new ResourceNotFoundException(
					"Cannot reject employee: Current employee status is '" + currentStatusName + 
					"' (emp_id: " + empId + ", temp_payroll_id: '" + rejectDTO.getTempPayrollId() + 
					"'). This method only works when employee status is 'Pending at CO'.");
		}
		
		logger.info("Employee (emp_id: {}) current status is 'Pending at CO', proceeding with reject back to DO", empId);
		
		// Step 4: Update status to "Back to DO"
		EmployeeCheckListStatus backToDOStatus = employeeCheckListStatusRepository.findByCheck_app_status_name("Back to DO")
				.orElseThrow(() -> new ResourceNotFoundException("EmployeeCheckListStatus with name 'Back to DO' not found"));
		employee.setEmp_check_list_status_id(backToDOStatus);
		logger.info("Updated employee (emp_id: {}) status from 'Pending at CO' to 'Back to DO' (ID: {})", 
				empId, backToDOStatus.getEmp_app_status_id());
		
		// Step 5: Update remarks (if remarks already exist, update them; if not, set new remarks)
		String existingRemarks = employee.getRemarks();
		if (existingRemarks != null && !existingRemarks.trim().isEmpty()) {
			// Update existing remarks (append or replace based on business logic - here we're replacing)
			employee.setRemarks(rejectDTO.getRemarks().trim());
			logger.info("Updated existing remarks for employee (emp_id: {}). Previous remarks: '{}', New remarks: '{}'", 
					empId, existingRemarks, rejectDTO.getRemarks());
		} else {
			// Set new remarks
			employee.setRemarks(rejectDTO.getRemarks().trim());
			logger.info("Set new remarks for employee (emp_id: {}): {}", empId, rejectDTO.getRemarks());
		}
		
		// Save employee updates (status and remarks)
		employeeRepository.save(employee);
		
		logger.info("Successfully rejected employee (emp_id: {}, temp_payroll_id: '{}') and sent back to DO with remarks", 
				empId, rejectDTO.getTempPayrollId());
		
		// Return the DTO with saved data
		return rejectDTO;
	}
}

