package com.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.employee.entity.EmpPfDetails;

@Repository
public interface EmpPfDetailsRepository extends JpaRepository<EmpPfDetails, Integer> {

}

