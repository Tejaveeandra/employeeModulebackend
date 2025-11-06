package com.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.employee.entity.Occupation;

@Repository
public interface OccupationRepository extends JpaRepository<Occupation, Integer> {

}

