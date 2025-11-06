package com.employee.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.employee.dto.SalaryInfoDTO;
import com.employee.service.EmpSalaryInfoService;

@RestController
@RequestMapping("/api/employee/salary-info")
public class EmpSalaryInfoController {

	@Autowired
	private EmpSalaryInfoService empSalaryInfoService;

	/**
	 * POST endpoint to create salary info
	 * Finds employee by temp_payroll_id, gets emp_id and emp_payment_type_id from BankDetails
	 * Returns only DTO, not the full entity
	 */
	@PostMapping
	public ResponseEntity<SalaryInfoDTO> createSalaryInfo(@RequestBody SalaryInfoDTO salaryInfoDTO) {
		SalaryInfoDTO createdSalaryInfo = empSalaryInfoService.createSalaryInfo(salaryInfoDTO);
		// Return the DTO returned from service (with all saved data, no entity relationships)
		return new ResponseEntity<>(createdSalaryInfo, HttpStatus.CREATED);
	}

	/**
	 * GET endpoint to retrieve salary info by temp_payroll_id
	 * Returns only DTO, not the full entity
	 */
	@GetMapping("/by-temp-payroll-id")
	public ResponseEntity<SalaryInfoDTO> getSalaryInfoByTempPayrollId(@RequestParam String tempPayrollId) {
		SalaryInfoDTO salaryInfoDTO = empSalaryInfoService.getSalaryInfoByTempPayrollIdAsDTO(tempPayrollId);
		return new ResponseEntity<>(salaryInfoDTO, HttpStatus.OK);
	}
}

