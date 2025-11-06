package com.employee.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.employee.entity.Campus;



@Repository
public interface CampusRepository extends JpaRepository<Campus, Integer>{
	
//	List<Campus> findByZoneZoneId(int zoneId);
//	List<Campus> findByCityCityId(int cityId);
//	
//	@Query(value = """
//	        SELECT c.* 
//	        FROM sce_campus.sce_cmps c 
//	        JOIN sce_locations.sce_campaign cam 
//	          ON c.cmps_id = cam.cmps_id 
//	        WHERE cam.campaign_id = :campaignId
//	        """, nativeQuery = true)
//	    List<Campus> findCampusByCampaignId(@Param("campaignId") int campaignId);
//	
//	 @Query("SELECT c.state.stateId FROM Campus c WHERE c.campusId = :campusId")
//	    Integer findStateIdByCampusId(@Param("campusId") int campusId);
//	 
//	 @Query("SELECT new com.application.dto.CampusDto(c.campusId, c.campusName) FROM Campus c WHERE c.zone = :zone")
//	    List<CampusDto> findByZoneAsDto(@Param("zone") Zone zone);
//	 
//	 @Query("SELECT new com.application.dto.CampusDetailsDTO(c.cmps_type, city.cityId, city.cityName) " +
//	           "FROM Campus c " +
//	           "JOIN c.city city " +
//	           "WHERE c.campusId = :campusId")
//	    Optional<CampusDetailsDTO> findCampusDetailsById(@Param("campusId") int campusId);
//	 
//	 @Query("SELECT new com.application.dto.CampusDropdownDTO(c.campusId, c.campusName) " +
//	           "FROM Campus c JOIN c.businessType bt " +
//	           // --- FIX: Compare uppercase versions ---
//	           "WHERE UPPER(bt.businessTypeName) = UPPER(:businessTypeName) AND c.isActive = 1 " +
//	           "ORDER BY c.campusName ASC")
//	    List<CampusDropdownDTO> findActiveCampusesByBusinessTypeName(@Param("businessTypeName") String businessTypeName);
//	 
//	 @Override
//	 @EntityGraph(attributePaths = {"zone", "state", "businessType"}, 
//	              type = EntityGraph.EntityGraphType.LOAD)
//	 Optional<Campus> findById(Integer id);
}
