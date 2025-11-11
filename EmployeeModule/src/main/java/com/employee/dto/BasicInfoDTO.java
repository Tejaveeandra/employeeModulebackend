package com.employee.dto;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Basic Info Tab (Step 1)
 * Maps to: Employee, EmpDetails, EmpPfDetails entities
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasicInfoDTO {
	
	// Employee Entity fields
	private Integer empId; // Generated after save - will be included in response
	private String firstName;
	private String lastName;
	private Date dateOfJoin;
	private Long primaryMobileNo;
	private String email;
	
	
	// Employee foreign key references (IDs) - All Integer types
	private Integer genderId;
	private Integer referenceEmpId; // Referred By
	private Integer hiredByEmpId; // Hired By
	private Integer designationId;
	private Integer departmentId;
	private Integer managerId; // Manager
	private Integer categoryId;
	private Integer reportingManagerId;
	private Integer empTypeId;
	// Note: qualificationId is NOT in DTO - it will be set automatically from qualification tab's highest qualification
	private Integer empWorkModeId; // Working Mode
	private Integer replacedByEmpId; // Replacement Employee
	private Integer joinTypeId; // Joining As
	private Integer modeOfHiringId; // Mode of Hiring
	
	// Contract dates - required when joinTypeId = 4 (Contract)
	private Date contractStartDate; // Contract start date
	private Date contractEndDate; // Contract end date
	
	
	// EmpDetails Entity fields
	private String adhaarName;
	private Date dateOfBirth;
	
	private String aadharNum;
	private String aadharEnrolmentNum; // Aadhaar Enrollment No
	private String pancardNum;
	
	private Integer bloodGroupId;
	private Integer casteId;
	private Integer religionId;
	private Integer maritalStatusId;
	private String emergencyPhNo; // Emergency contact phone number (REQUIRED NOT NULL)
	private Integer emergencyRelationId; // Emergency contact relation ID (FK to sce_stud_relation, optional)
	private Long sscNo; // SSC number - matches EmpDocuments.ssc_no (Long type)
	private Boolean sscNotAvailable;
	
	// EmpPfDetails Entity fields - Only previous UAN and previous ESI numbers are stored at HR level
	@JsonAlias({"preUanNo"})
	private Long preUanNum; // Previous UAN No (int8 - bigint) - accepts both "preUanNum" and "preUanNo"
	
	@JsonAlias({"preEsiNo"})
	private Long preEsiNum; // Previous ESI No (int8 - bigint) - accepts both "preEsiNum" and "preEsiNo"
	
	// Note: PF Number, PF Join Date, UAN Number, and ESI Number are NOT stored at HR level
	// private String pfNo;
	// private Date pfJoinDate;
	// private Long uanNo;
	// private Long esiNo;
	
	// Working Information
	private Integer campusId;
	
	private Integer totalExperience; // Total Years of Experience
	
	// For profile picture upload (handle separately or as base64)
	private String profilePicture; // Can be base64 string or file path
	
	// Audit field - passed from frontend
	private Integer createdBy; // User ID who created the employee record
	
	// Temp Payroll ID - for validation against SkillTestDetl table
	private String tempPayrollId; // Optional - validates against sce_skill_test_detl.temp_payroll_id
	
}

