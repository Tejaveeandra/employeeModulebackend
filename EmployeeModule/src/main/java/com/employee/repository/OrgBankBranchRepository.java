package com.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.employee.entity.OrgBankBranch;

@Repository
public interface OrgBankBranchRepository extends JpaRepository<OrgBankBranch, Integer> {

}

