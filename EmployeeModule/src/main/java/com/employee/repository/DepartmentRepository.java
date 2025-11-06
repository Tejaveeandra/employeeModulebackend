package com.employee.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.employee.entity.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {

	// Find by ID and is_active = 1
	@Query("SELECT d FROM Department d WHERE d.department_id = :id AND d.isActive = :isActive")
	Optional<Department> findByIdAndIsActive(@Param("id") Integer id, @Param("isActive") Integer isActive);

}

