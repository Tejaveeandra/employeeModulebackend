package com.employee.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.employee.entity.EmpDocType;

@Repository
public interface EmpDocTypeRepository extends JpaRepository<EmpDocType, Integer> {

	// Find by document name
	@Query("SELECT edt FROM EmpDocType edt WHERE edt.doc_name = :docName")
	Optional<EmpDocType> findByDocName(@Param("docName") String docName);
	
	// Find by document name and is_active = 1
	@Query("SELECT edt FROM EmpDocType edt WHERE edt.doc_name = :docName AND edt.is_active = :isActive")
	Optional<EmpDocType> findByDocNameAndIsActive(@Param("docName") String docName, @Param("isActive") Integer isActive);
}

