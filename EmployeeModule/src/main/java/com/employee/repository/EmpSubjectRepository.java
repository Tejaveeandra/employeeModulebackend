package com.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.employee.entity.EmpSubject;

@Repository
public interface EmpSubjectRepository extends JpaRepository<EmpSubject, Integer> {

}

