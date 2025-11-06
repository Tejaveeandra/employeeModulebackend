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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="sce_emp",schema="sce_employee")
	public class Employee {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "emp_id")
	private int emp_id;
	
	@Column(name = "first_name", nullable = false, length = 50)
	private String first_name;
	
	@Column(name = "last_name", nullable = false, length = 50)
	private String last_name;
	
	@Column(name = "date_of_join", nullable = false)
	private Date date_of_join;
	
	@Column(name = "primary_mobile_no", nullable = false)
	private long primary_mobile_no;
	
	@Column(name = "secondary_mobile_no")
	private Long secondary_mobile_no; // Optional - nullable
	
	@Column(name = "user_name", length = 50)
	private String user_name; // Optional - nullable
	
	@Column(name = "password", length = 50)
	private String password; // Optional - nullable
	
	@Column(name = "email", length = 50, unique = true)
	private String email; // Optional - nullable, but unique if provided
	
	@Column(name = "is_active", nullable = false)
	private int is_active = 1; // Default 1
	
	@Column(name = "status", nullable = false, length = 40)
	private String status;
	
	// Note: passout_year column does NOT exist in sce_emp table
	// Marked as @Transient to prevent Hibernate from trying to map it to database
	@Transient
	private int passout_year;
	
	@Column(name = "total_experience")
	private Double total_experience; // Total years of experience (float8 - double)
	
	@Column(name = "contract_start_date")
	private Date contract_start_date;
	
	@Column(name = "contract_end_date")
	private Date contract_end_date;
	
	@Column(name = "payroll_id", length = 30)
	private String payroll_id; // Optional - nullable
	
	@Column(name = "temp_payroll_id", length = 50)
	private String temp_payroll_id; // Optional - nullable
	
	@Column(name = "emp_app_check_list_detl_id", length = 100)
	private String emp_app_check_list_detl_id; // Optional - nullable
	
	@Column(name = "org_id")
	private Integer org_id; // Optional - nullable (FK to sce_campus.sce_organization)
	
	// @ToString.Exclude
	// @EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "gender_id", nullable = false)
	private Gender gender; // Required NOT NULL
	
	// @ToString.Exclude
	// @EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reference_emp_id", nullable = true)
	private Employee employee_reference; // Optional - nullable
	
	// @ToString.Exclude
	// @EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "hired_by_emp_id", nullable = true)
	private Employee employee_hired; // Optional - nullable
	
	// @ToString.Exclude
	// @EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "designation_id", nullable = false)
	private Designation designation; // Required NOT NULL
	
	// @ToString.Exclude
	// @EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "department_id", nullable = false)
	private Department department; // Required NOT NULL
	
	// @ToString.Exclude
	// @EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "manager_id", nullable = true)
	private Employee employee_manager_id; // Optional - nullable
	
	// @ToString.Exclude
	// @EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category; // Required NOT NULL
	
	// @ToString.Exclude
	// @EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reporting_manager_id", nullable = true)
	private Employee employee_reporting_id; // Optional - nullable
	
	// @ToString.Exclude
	// @EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "emp_type_id", nullable = true)
	private EmployeeType employee_type_id; // Optional - nullable
	
	// @ToString.Exclude
	// @EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "qualification_id", nullable = true)
	private Qualification qualification_id; // Optional - nullable
	
	// @ToString.Exclude
	// @EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "emp_work_mode_id", nullable = true)
	private WoringMode workingMode_id; // Optional - nullable
	
	// @ToString.Exclude
	// @EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "replaced_by_emp_id", nullable = true)
	private Employee employee_replaceby_id; // Optional - nullable
	
	// @ToString.Exclude
	// @EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "join_type_id", nullable = true)
	private JoiningAs join_type_id; // Optional - nullable
	
	// @ToString.Exclude
	// @EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mode_of_hiring_id", nullable = true)
	private ModeOfHiring modeOfHiring_id; // Optional - nullable
	
	// @ToString.Exclude
	// @EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "emp_app_status_id", nullable = false)
	private EmployeeCheckListStatus emp_check_list_status_id; // Required NOT NULL
	
	// @ToString.Exclude
	// @EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cmps_id", nullable = true)
	private Campus campus_id; // Optional - nullable (maps to cmps_id in database)
	 
	 	// Audit fields - required NOT NULL columns
	 	@Column(name = "created_by", nullable = false)
	 	private Integer created_by = 1; // Default to 1 if not provided
	 	
	 	@Column(name = "created_date", nullable = false)
	 	private Timestamp created_date = new Timestamp(System.currentTimeMillis());
	 	
	 	@Column(name = "updated_by")
	 	private Integer updated_by;
	 	
	 	@Column(name = "updated_date")
	 	private Timestamp updated_date;

}



