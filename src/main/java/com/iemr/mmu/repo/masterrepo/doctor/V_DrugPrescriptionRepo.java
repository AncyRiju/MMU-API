package com.iemr.mmu.repo.masterrepo.doctor;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iemr.mmu.data.masterdata.doctor.V_DrugPrescription;

@Repository
public interface V_DrugPrescriptionRepo extends CrudRepository<V_DrugPrescription, Long> {
	@Query("SELECT t FROM V_DrugPrescription t WHERE t.facilityID =:facilityID ")
	public ArrayList<V_DrugPrescription> getItemListForFacility(@Param("facilityID") Integer facilityID);
}
