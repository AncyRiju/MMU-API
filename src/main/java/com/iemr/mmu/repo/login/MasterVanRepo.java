package com.iemr.mmu.repo.login;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iemr.mmu.data.login.MasterVan;

@Repository
public interface MasterVanRepo extends CrudRepository<MasterVan, Integer> {
	@Query("Select mv.vanID, mv.vehicalNo from MasterVan mv WHERE mv.deleted != 1 and mv.parkingPlaceID in :parkingPlaceList ")
	public List<Object[]> getUserVanDatails(@Param("parkingPlaceList") Set<Integer> parkingPlaceList);

	@Query("Select mv.facilityID from MasterVan mv WHERE mv.vanID = :vanID ")
	public Integer getFacilityID(@Param("vanID") Integer vanID);
}
