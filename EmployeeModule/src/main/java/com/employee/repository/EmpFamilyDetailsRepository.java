package com.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.employee.entity.EmpFamilyDetails;

@Repository
public interface EmpFamilyDetailsRepository extends JpaRepository<EmpFamilyDetails, Integer> {

}

