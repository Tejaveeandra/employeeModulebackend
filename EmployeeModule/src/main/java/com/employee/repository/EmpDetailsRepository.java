package com.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.employee.entity.EmpDetails;

@Repository
public interface EmpDetailsRepository extends JpaRepository<EmpDetails, Integer> {

}

