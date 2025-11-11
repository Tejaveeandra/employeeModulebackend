package com.employee.entity;

import java.sql.Date;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sce_skill_test_detl", schema = "sce_employee")
public class SkillTestDetl {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "skill_test_detl_id")
	private Integer skillTestDetlId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "emp_id", nullable = true)
	private Employee empId; // Optional - nullable (FK to sce_employee.sce_emp)
	
	@Column(name = "aadhaar_no")
	private String aadhaarNo; // Optional - nullable
	
	@Column(name = "pan_number")
	private String panNumber; // Optional - nullable
	
	@Column(name = "ssc_no")
	private String sscNo; // Optional - nullable
	
	@Column(name = "previous_chaitanya_id")
	private String previousChaitanyaId; // Optional - nullable
	
	@Column(name = "first_name")
	private String firstName; // Optional - nullable
	
	@Column(name = "last_name")
	private String lastName; // Optional - nullable
	
	@Column(name = "dob")
	private Date dob; // Optional - nullable
	
	@Column(name = "age")
	private Integer age; // Optional - nullable
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "gender_id", nullable = true)
	private Gender genderId; // Optional - nullable (FK to sce_student.sce_gender)
	
	@Column(name = "contact_number")
	private Long contactNumber; // Optional - nullable (int8 - bigint)
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "marital_status_id", nullable = true)
	private MaritalStatus maritalStatusId; // Optional - nullable (FK to sce_employee.sce_marital_status)
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "qualification_id", nullable = true)
	private Qualification qualificationId; // Optional - nullable (FK to sce_employee.sce_qualification)
	
	@Column(name = "highest_qualification")
	private String highestQualification; // Optional - nullable
	
	@Column(name = "total_experience")
	private Double totalExperience; // Optional - nullable (float8 - double)
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "join_type_id", nullable = true)
	private JoiningAs joinTypeId; // Optional - nullable (FK to sce_employee.sce_join_type)
	
	@Column(name = "stream_id")
	private Integer streamId; // Optional - nullable (FK to sce_course.sce_stream - different schema, using Integer)
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subject_id", nullable = true)
	private subject subjectId; // Optional - nullable (FK to sce_employee.sce_subject)
	
	@Column(name = "sections_to_be_handled")
	private String sectionsToBeHandled; // Optional - nullable
	
	@Column(name = "proposed_ctc_per_month")
	private Double proposedCtcPerMonth; // Optional - nullable (float8 - double)
	
	@Column(name = "emp_level_id")
	private Integer empLevelId; // Optional - nullable (FK to sce_employee.sce_emp_level - entity doesn't exist, using Integer)
	
	@Column(name = "agreed_periods_per_week")
	private Integer agreedPeriodsPerWeek; // Optional - nullable
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "refered_by_id", nullable = true)
	private Employee referedById; // Optional - nullable (FK to sce_employee.sce_emp)
	
	@Column(name = "is_active", nullable = false)
	private Integer isActive = 1; // Default 1
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "emp_structure_id", nullable = true)
	private EmpStructure empStructureId; // Optional - nullable (FK to sce_employee.sce_emp_structure)
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "emp_grade_id", nullable = true)
	private EmpGrade empGradeId; // Optional - nullable (FK to sce_employee.sce_emp_grade)
	
	@Column(name = "email")
	private String email; // Optional - nullable
	
	@Column(name = "temp_payroll_id")
	private String tempPayrollId; // Optional - nullable
	
	@Column(name = "password")
	private String password; // Optional - nullable
	
	// Audit fields - required NOT NULL columns
	@Column(name = "created_by", nullable = false)
	private Integer createdBy = 1; // Default to 1 if not provided
	
	@Column(name = "created_date", nullable = false)
	private Timestamp createdDate = new Timestamp(System.currentTimeMillis());
	
	@Column(name = "updated_by")
	private Integer updatedBy; // Optional - nullable
	
	@Column(name = "updated_date")
	private Timestamp updatedDate; // Optional - nullable
}

