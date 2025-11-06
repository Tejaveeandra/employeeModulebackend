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
	private Integer qualificationId; // Highest Qualification
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
	private Long sscNo; // SSC number - matches EmpDocuments.ssc_no (Long type)
	private Boolean sscNotAvailable;
	
	// EmpPfDetails Entity fields
	private String pfNo;
	private Date pfJoinDate;
	
	@JsonAlias({"preUanNo"})
	private Long preUanNum; // Previous UAN No (int8 - bigint) - accepts both "preUanNum" and "preUanNo"
	
	private Long uanNo; // UAN No (int8 - bigint)
	
	@JsonAlias({"preEsiNo"})
	private Long preEsiNum; // Previous ESI No (int8 - bigint) - accepts both "preEsiNum" and "preEsiNo"
	
	private Long esiNo; // ESI No (int8 - bigint)
	
	// Working Information
	private Integer campusId;
	
	private Integer totalExperience; // Total Years of Experience
	
	// For profile picture upload (handle separately or as base64)
	private String profilePicture; // Can be base64 string or file path
	
	// Audit field - passed from frontend
	private Integer createdBy; // User ID who created the employee record
	
}

