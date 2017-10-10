package com.iemr.mmu.repo.masterrepo;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.iemr.mmu.data.masterdata.registrar.CommunityMaster;

@Repository
public interface CommunityMasterRepo extends CrudRepository<CommunityMaster, Short> {
	@Query("select communityID, communityType from CommunityMaster where deleted = false  order by communityType ")
	public ArrayList<Object[]> getCommunityMaster();
}
