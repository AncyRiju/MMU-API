package com.iemr.mmu.repo.registrar;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.iemr.mmu.data.registrar.BenGovIdMapping;

@Repository
public interface RegistrarRepoBenGovIdMapping extends CrudRepository<BenGovIdMapping, Long> {
	// @Transactional
	// @Modifying
	// @Query("Delete from BenGovIdMapping where ID = :ID and "
	// + "beneficiaryRegID = :beneficiaryRegID ")
	// public Integer deleteBenGovIdMapping(@Param("ID") Long ID,
	// @Param("beneficiaryRegID") Long beneficiaryRegID);
	@Modifying
    @Transactional
	@Query("Delete from BenGovIdMapping where beneficiaryRegID =:benRegID")
	public int deletePreviousGovMapID(@Param("benRegID") Long benRegID);
}
