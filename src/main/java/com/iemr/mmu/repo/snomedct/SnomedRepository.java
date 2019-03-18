package com.iemr.mmu.repo.snomedct;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iemr.mmu.data.snomedct.SCTDescription;

@Repository
public interface SnomedRepository extends CrudRepository<SCTDescription, Long> {

	/* @Query("select u from SCTDescription u where u.term like %:term%") */
	@Query("SELECT s.conceptID,s.term, s.caseSignificanceID "
			+ " FROM SCTDescription s WHERE s.term =:term and s.active = '1'")
	public List<Object[]> findSnomedCTRecordFromTerm(@Param("term") String term);

	@Query("SELECT DISTINCT s FROM SCTDescription s WHERE s.term like :term% and s.active = '1' ")
	public Page<SCTDescription> findSnomedCTRecordList(@Param("term") String term, Pageable pageable);

}
