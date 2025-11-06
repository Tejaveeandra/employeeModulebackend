package com.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.employee.entity.EmpExperienceDetails;

@Repository
public interface EmpExperienceDetailsRepository extends JpaRepository<EmpExperienceDetails, Integer> {

}

