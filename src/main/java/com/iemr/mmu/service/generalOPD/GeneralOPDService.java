package com.iemr.mmu.service.generalOPD;

import java.util.Map;

import com.google.gson.JsonObject;
import com.iemr.mmu.data.nurse.CommonUtilityClass;

public interface GeneralOPDService {

	Long saveNurseData(JsonObject requestOBJ) throws Exception;

	Map<String, Long> saveBenVisitDetails(JsonObject visitDetailsOBJ, CommonUtilityClass nurseUtilityClass)
			throws Exception;

	Long saveDoctorData(JsonObject requestOBJ, String Authorization) throws Exception;

	Long saveBenGeneralOPDHistoryDetails(JsonObject generalOPDHistoryOBJ, Long benVisitID, Long benVisitCode)
			throws Exception;

	Long saveBenVitalDetails(JsonObject vitalDetailsOBJ, Long benVisitID, Long benVisitCode) throws Exception;

	Long saveBenExaminationDetails(JsonObject examinationDetailsOBJ, Long benVisitID, Long benVisitCode)
			throws Exception;

}
