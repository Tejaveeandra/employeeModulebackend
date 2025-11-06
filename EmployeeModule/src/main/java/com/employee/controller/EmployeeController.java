package com.employee.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.employee.dto.EmployeeOnboardingDTO;
import com.employee.service.EmployeeService;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

	@Autowired
	private EmployeeService employeeService;

	/**
	 * POST endpoint for Employee Onboarding - All 8 Tabs in One Request
	 * 
	 * This endpoint handles complete employee onboarding data from all 8 tabs:
	 * 1. Basic Info
	 * 2. Address Info
	 * 3. Family Info
	 * 4. Previous Employer Info
	 * 5. Qualification
	 * 6. Upload Documents
	 * 7. Category Info
	 * 8. Bank Info
	 * 
	 * @param onboardingDTO Complete employee onboarding data from all tabs
	 * @return ResponseEntity with the same DTO structure that was posted (what you post is what you get back)
	 */
	@PostMapping("/onboard")
	public ResponseEntity<EmployeeOnboardingDTO> onboardEmployee(@RequestBody EmployeeOnboardingDTO onboardingDTO) {
		// Remove try-catch - let GlobalExceptionHandler handle exceptions and log them
		EmployeeOnboardingDTO response = employeeService.onboardEmployee(onboardingDTO);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}

