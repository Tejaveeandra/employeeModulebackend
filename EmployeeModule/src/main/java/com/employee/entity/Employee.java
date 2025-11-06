package com.employee.entity;

import java.sql.Date;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="sce_emp",schema="sce_employee")
public class Employee {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private int emp_id;
	private String first_name;
	private String last_name;
	private Date date_of_join;
	private  long primary_mobile_no;
	private String email;
	private String user_name; // Required NOT NULL - username for login
	private String password; // Required NOT NULL - password for login
	private int is_active;
	private String status;
	// Note: passout_year column does NOT exist in sce_emp table
	// Marked as @Transient to prevent Hibernate from trying to map it to database
	@Transient
	private int passout_year;
	
	private Integer total_experience; // Total years of experience
	
	private Date contract_start_date;
	private Date contract_end_date;
	
	 	@ManyToOne
	    @JoinColumn(name = "gender_id")
	    private Gender gender;
	 	
	 	 @ManyToOne
	     @JoinColumn(name = "reference_emp_id")
	     private Employee employee_reference;
	 	 
	 	@ManyToOne
	     @JoinColumn(name = "hired_by_emp_id")
	     private Employee employee_hired;
	 	
	 	@ManyToOne
	    @JoinColumn(name = "designation_id")
	    private Designation designation;
	 	
	 	@ManyToOne
	    @JoinColumn(name = "department_id")
	    private Department department;
	 	
	 	@ManyToOne
	     @JoinColumn(name = "manager_id")
	     private Employee employee_manager_id;
	 	
	 	@ManyToOne
	     @JoinColumn(name = "category_id")
	     private Category category;
	 	
	 	@ManyToOne
	     @JoinColumn(name = "reporting_manager_id")
	     private Employee employee_reporting_id;
	 	
	 	@ManyToOne
	     @JoinColumn(name = "emp_type_id")
	     private EmployeeType employee_type_id;
	 	
	 	@ManyToOne
	     @JoinColumn(name = "qualification_id")
	     private Qualification qualification_id;
	 	
	 	@ManyToOne
	     @JoinColumn(name = "emp_work_mode_id")
	     private WoringMode workingMode_id;
	 	
	 	@ManyToOne
	     @JoinColumn(name = "replaced_by_emp_id", nullable = true)
	     private Employee employee_replaceby_id; // Optional - can be null
	 	
	 	@ManyToOne
	     @JoinColumn(name = "join_type_id")
	     private JoiningAs join_type_id;
	 	
	 	@ManyToOne
	     @JoinColumn(name = "mode_of_hiring_id")
	     private ModeOfHiring modeOfHiring_id;
	 
	 	@ManyToOne
	    @JoinColumn(name = "emp_app_status_id")
	    private EmployeeCheckListStatus emp_check_list_status_id;
	 
	 	@ManyToOne
	    @JoinColumn(name = "cmps_id", nullable = true)
	    private Campus campus_id; // Optional - can be null (maps to cmps_id in database)
	 
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



