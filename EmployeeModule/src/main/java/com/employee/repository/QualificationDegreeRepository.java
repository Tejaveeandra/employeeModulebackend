package com.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.employee.entity.QualificationDegree;

@Repository
public interface QualificationDegreeRepository extends JpaRepository<QualificationDegree, Integer> {

}

