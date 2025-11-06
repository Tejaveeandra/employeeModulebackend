package com.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.employee.entity.EmpDocuments;

@Repository
public interface EmpDocumentsRepository extends JpaRepository<EmpDocuments, Integer> {

}

