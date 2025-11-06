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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="sce_emp_family_detl",schema="sce_employee")
public class EmpFamilyDetails {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private int emp_family_detl_id;
	
	@ManyToOne
    @JoinColumn(name = "emp_id", nullable = false)
    private Employee emp_id; // Required NOT NULL
	
	private Integer is_dependent; // Nullable
	
	private Date date_of_birth; // Nullable
	
	@Column(name = "first_name", nullable = false)
	private String first_name; // Required NOT NULL
	
	@Column(name = "last_name", nullable = false)
	private String last_name; // Required NOT NULL
	
	@Column(name = "occupation", nullable = false)
	private String occupation; // Required NOT NULL
	
	@ManyToOne
    @JoinColumn(name = "gender_id", nullable = false)
    private Gender gender_id; // Required NOT NULL
	
	@ManyToOne
    @JoinColumn(name = "blood_group_id", nullable = false)
    private BloodGroup blood_group_id; // Required NOT NULL
	
	@Column(name = "nationality", nullable = false)
	private String nationality; // Required NOT NULL
	
	private String is_late; // Nullable
	
	@ManyToOne
    @JoinColumn(name = "relation_id", nullable = false)
    private Relation relation_id; // Required NOT NULL
	
	private int is_active; // Default 1
	
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
