package com.employee.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.employee.entity.EmpDetails;

@Repository
public interface EmpDetailsRepository extends JpaRepository<EmpDetails, Integer> {

	/**
	 * Find EmpDetails by personal_email (unique constraint)
	 * Used to check if email already exists when updating employee
	 */
	@Query("SELECT ed FROM EmpDetails ed WHERE ed.personal_email = :personalEmail")
	Optional<EmpDetails> findByPersonal_email(@Param("personalEmail") String personalEmail);
}

