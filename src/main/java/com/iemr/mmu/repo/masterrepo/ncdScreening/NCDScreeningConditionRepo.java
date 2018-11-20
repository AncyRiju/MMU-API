package com.iemr.mmu.repo.masterrepo.ncdScreening;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.iemr.mmu.data.masterdata.ncdscreening.NCDScreeningCondition;

@Repository
public interface NCDScreeningConditionRepo extends CrudRepository<NCDScreeningCondition, Long>{

	@Query("select ncdScreeningConditionID, screeningCondition from NCDScreeningCondition where deleted = false order by ncdScreeningCondition")
	public ArrayList<Object[]> getNCDScreeningConditions();
}
