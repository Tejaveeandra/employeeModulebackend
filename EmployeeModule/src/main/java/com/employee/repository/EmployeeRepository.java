package com.employee.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.employee.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

	// Find by ID and is_active = 1
	@Query("SELECT e FROM Employee e WHERE e.emp_id = :id AND e.is_active = :is_active")
	Optional<Employee> findByIdAndIs_active(@Param("id") Integer id, @Param("is_active") int is_active);
	
	// Find by temp_payroll_id
	@Query("SELECT e FROM Employee e WHERE e.temp_payroll_id = :tempPayrollId")
	Optional<Employee> findByTemp_payroll_id(@Param("tempPayrollId") String tempPayrollId);

}