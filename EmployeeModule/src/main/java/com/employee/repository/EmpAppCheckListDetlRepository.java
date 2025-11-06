package com.employee.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.employee.entity.EmpAppCheckListDetl;

@Repository
public interface EmpAppCheckListDetlRepository extends JpaRepository<EmpAppCheckListDetl, Integer> {

	// Find by ID and is_active = 1
	@Query("SELECT eacld FROM EmpAppCheckListDetl eacld WHERE eacld.empAppCheckListDetlId = :id AND eacld.isActive = :isActive")
	Optional<EmpAppCheckListDetl> findByIdAndIsActive(@Param("id") Integer id, @Param("isActive") Integer isActive);

}

