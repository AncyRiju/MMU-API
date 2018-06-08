package com.iemr.mmu.repo.login;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iemr.mmu.data.login.UserVanSpDetails_View;

@Repository
public interface UserVanSpDetails_View_Repo extends CrudRepository<UserVanSpDetails_View, Long> {
	@Query("SELECT t.userID, t.vanID, t.vanNoAndType, t.vanSession,t.servicePointID, t.servicePointName, "
			+ " t.parkingPlaceID, t.facilityID "
			+ " FROM UserVanSpDetails_View t WHERE t.userID =:userID AND "
			+ " t.providerServiceMapID =:providerServiceMapID ")
	public ArrayList<Object[]> getUserVanSpDetails_View(@Param("userID") Integer userID,
			@Param("providerServiceMapID") Integer providerServiceMapID);
}
