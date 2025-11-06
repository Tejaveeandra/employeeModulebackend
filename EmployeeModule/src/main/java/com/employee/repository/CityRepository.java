package com.employee.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.employee.entity.City;



@Repository
public interface CityRepository extends JpaRepository<City, Integer>{
	  // Add this line
	// In your CityRepository interface
//	List<City> findByDistrictStateStateIdAndStatus(int stateId, int status);
//    List<City> findByDistrictDistrictIdAndStatus(int districtId,int status);
//    List<City> findByStatus(int status);
}