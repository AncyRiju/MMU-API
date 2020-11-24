package com.iemr.mmu.repo.masterrepo.nurse;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.iemr.mmu.data.masterdata.nurse.CancerDiseaseType;
@Repository
public interface CancerDiseaseMasterRepo extends CrudRepository<CancerDiseaseType, Long>{
	
	@Query("SELECT cancerDiseaseTypeID, cancerDiseaseType, gender, snomedCode, snomedTerm from CancerDiseaseType "
			+ "WHERE deleted = false ORDER BY cancerDiseaseType")
	public ArrayList<Object[]> getCancerDiseaseMaster();
	
}
