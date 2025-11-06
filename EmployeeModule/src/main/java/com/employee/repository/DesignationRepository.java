package com.employee.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.employee.entity.Designation;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Integer> {

	// Find by ID and is_active = 1
	@Query("SELECT d FROM Designation d WHERE d.designation_id = :id AND d.isActive = :isActive")
	Optional<Designation> findByIdAndIsActive(@Param("id") Integer id, @Param("isActive") Integer isActive);

}

