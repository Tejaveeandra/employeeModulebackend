package com.employee.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.employee.entity.SkillTestDetl;

@Repository
public interface SkillTestDetlRepository extends JpaRepository<SkillTestDetl, Integer> {

	// Find by temp payroll ID
	@Query("SELECT std FROM SkillTestDetl std WHERE std.tempPayrollId = :tempPayrollId")
	Optional<SkillTestDetl> findByTempPayrollId(@Param("tempPayrollId") String tempPayrollId);
	
	// Find by temp payroll ID and is_active = 1
	@Query("SELECT std FROM SkillTestDetl std WHERE std.tempPayrollId = :tempPayrollId AND std.isActive = :isActive")
	Optional<SkillTestDetl> findByTempPayrollIdAndIsActive(@Param("tempPayrollId") String tempPayrollId, @Param("isActive") Integer isActive);
}

