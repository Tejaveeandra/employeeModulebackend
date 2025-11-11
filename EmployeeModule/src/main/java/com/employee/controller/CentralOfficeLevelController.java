package com.employee.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.employee.dto.CentralOfficeChecklistDTO;
import com.employee.service.CentralOfficeLevelService;

/**
 * Controller for Central Office Level operations
 * Handles checklist updates at Central Office level
 */
@RestController
@RequestMapping("/api/employee/central-office")
public class CentralOfficeLevelController {

	@Autowired
	private CentralOfficeLevelService centralOfficeLevelService;

	/**
	 * POST/PUT endpoint to update checklist IDs and notice period for an employee
	 * This endpoint is called at Central Office level to update checklist IDs and notice period
	 * Also updates emp_check_list_status_id from "Forward to CO" to "Confirm"
	 * 
	 * Flow:
	 * 1. Validates temp_payroll_id exists
	 * 2. Checks if current status is "Forward to CO", if yes updates to "Confirm"
	 * 3. Validates all checklist IDs exist and are active in EmpAppCheckListDetl table
	 * 4. Updates emp_app_check_list_detl_id in Employee table
	 * 5. Updates notice_period in Employee table (if provided)
	 * 
	 * @param checklistDTO Contains tempPayrollId (required), checkListIds (required, comma-separated), and noticePeriod (optional)
	 * @return ResponseEntity with the updated CentralOfficeChecklistDTO
	 */
	@PostMapping("/update-checklist")
	@PutMapping("/update-checklist")
	public ResponseEntity<CentralOfficeChecklistDTO> updateChecklist(@RequestBody CentralOfficeChecklistDTO checklistDTO) {
		CentralOfficeChecklistDTO result = centralOfficeLevelService.updateChecklist(checklistDTO);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
}

