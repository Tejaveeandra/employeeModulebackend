package com.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.employee.entity.EmpQualification;

@Repository
public interface EmpQualificationRepository extends JpaRepository<EmpQualification, Integer> {

}

