package com.employee.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.employee.entity.BankDetails;

@Repository
public interface BankDetailsRepository extends JpaRepository<BankDetails, Integer> {

	// Find by employee ID
	@Query("SELECT bd FROM BankDetails bd WHERE bd.empId.emp_id = :empId")
	List<BankDetails> findByEmpId_Emp_id(@Param("empId") Integer empId);
	
	// Find by employee ID and is_active = 1
	@Query("SELECT bd FROM BankDetails bd WHERE bd.empId.emp_id = :empId AND bd.isActive = :isActive")
	List<BankDetails> findByEmpIdAndIsActive(@Param("empId") Integer empId, @Param("isActive") Integer isActive);

}

