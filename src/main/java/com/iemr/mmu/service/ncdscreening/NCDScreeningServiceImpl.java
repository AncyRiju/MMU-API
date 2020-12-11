package com.iemr.mmu.service.ncdscreening;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.iemr.mmu.data.anc.BenFamilyHistory;

import com.iemr.mmu.data.anc.BenAllergyHistory;
import com.iemr.mmu.data.anc.BenChildDevelopmentHistory;
import com.iemr.mmu.data.anc.BenFamilyHistory;
import com.iemr.mmu.data.anc.BenMedHistory;
import com.iemr.mmu.data.anc.BenMenstrualDetails;
import com.iemr.mmu.data.anc.BenPersonalHabit;
import com.iemr.mmu.data.anc.ChildFeedingDetails;
import com.iemr.mmu.data.anc.PerinatalHistory;
import com.iemr.mmu.data.anc.WrapperChildOptionalVaccineDetail;
import com.iemr.mmu.data.anc.WrapperComorbidCondDetails;
import com.iemr.mmu.data.anc.WrapperFemaleObstetricHistory;
import com.iemr.mmu.data.anc.WrapperImmunizationHistory;
import com.iemr.mmu.data.anc.WrapperMedicationHistory;
import com.iemr.mmu.data.ncdScreening.IDRSData;
import com.iemr.mmu.data.ncdScreening.NCDScreening;
import com.iemr.mmu.data.ncdScreening.PhysicalActivityType;
import com.iemr.mmu.data.nurse.BenAnthropometryDetail;
import com.iemr.mmu.data.nurse.BenPhysicalVitalDetail;
import com.iemr.mmu.data.nurse.BeneficiaryVisitDetail;
import com.iemr.mmu.data.nurse.CommonUtilityClass;
import com.iemr.mmu.data.quickConsultation.BenChiefComplaint;
import com.iemr.mmu.data.tele_consultation.TeleconsultationRequestOBJ;
import com.iemr.mmu.repo.benFlowStatus.BeneficiaryFlowStatusRepo;
import com.iemr.mmu.repo.nurse.BenVisitDetailRepo;
import com.iemr.mmu.service.benFlowStatus.CommonBenStatusFlowServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonNurseServiceImpl;
import com.iemr.mmu.utils.mapper.InputMapper;

@Service
public class NCDScreeningServiceImpl implements NCDScreeningService {

	private NCDScreeningNurseServiceImpl ncdScreeningNurseServiceImpl;
	private CommonNurseServiceImpl commonNurseServiceImpl;
	private CommonBenStatusFlowServiceImpl commonBenStatusFlowServiceImpl;
	private BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo;

	@Autowired
	private BenVisitDetailRepo benVisitDetailRepo;

	@Autowired
	public void setBeneficiaryFlowStatusRepo(BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo) {
		this.beneficiaryFlowStatusRepo = beneficiaryFlowStatusRepo;
	}

	@Autowired
	public void setCommonBenStatusFlowServiceImpl(CommonBenStatusFlowServiceImpl commonBenStatusFlowServiceImpl) {
		this.commonBenStatusFlowServiceImpl = commonBenStatusFlowServiceImpl;
	}

	@Autowired
	public void setCommonNurseServiceImpl(CommonNurseServiceImpl commonNurseServiceImpl) {
		this.commonNurseServiceImpl = commonNurseServiceImpl;
	}

	@Autowired
	public void setNcdScreeningNurseServiceImpl(NCDScreeningNurseServiceImpl ncdScreeningNurseServiceImpl) {
		this.ncdScreeningNurseServiceImpl = ncdScreeningNurseServiceImpl;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Long saveNCDScreeningNurseData(JsonObject requestOBJ, String Authorization) throws Exception {

//		Integer result = null;
//
//		if (jsonObject != null && jsonObject.has("visitDetails") && jsonObject.has("ncdScreeningDetails")) {
//			// JsonElement visitDetails = jsonObject.get("visitDetails");
//			// JsonElement ncdScreeningDetails =
//			// jsonObject.get("ncdScreeningDetails");
//			CommonUtilityClass nurseUtilityClass = InputMapper.gson().fromJson(jsonObject, CommonUtilityClass.class);
//			Long benFlowID = nurseUtilityClass.getBenFlowID();
//
//			BeneficiaryVisitDetail beneficiaryVisitDetail = InputMapper.gson().fromJson(jsonObject.get("visitDetails"),
//					BeneficiaryVisitDetail.class);
//
//			NCDScreening ncdScreening = InputMapper.gson().fromJson(jsonObject.get("ncdScreeningDetails"),
//					NCDScreening.class);
//
//			if (ncdScreening.getNextScreeningDate() != null)
//				ncdScreening.setNextScreeningDateDB(Timestamp
//						.valueOf(ncdScreening.getNextScreeningDate().replaceAll("T", " ").replaceAll("Z", " ")));
//
//			Long visitID = commonNurseServiceImpl.saveBeneficiaryVisitDetails(beneficiaryVisitDetail);
//
//			// 11-06-2018 visit code
//			Long benVisitCode = commonNurseServiceImpl.generateVisitCode(visitID, nurseUtilityClass.getVanID(),
//					nurseUtilityClass.getSessionID());
//
//			if (null != visitID) {
//
//				Long vitalSuccessFlag = saveNCDScreeningVitalDetails(jsonObject, visitID, benVisitCode);
//				Long saveNCDScreeningDetails = null;
//				ncdScreening.setBenVisitID(visitID);
//				ncdScreening.setVisitCode(benVisitCode);
//				saveNCDScreeningDetails = ncdScreeningNurseServiceImpl.saveNCDScreeningDetails(ncdScreening);
//
//				if (null != vitalSuccessFlag && null != saveNCDScreeningDetails) {
//
//					int i = updateBenFlowNurseAfterNurseActivityANC(beneficiaryVisitDetail.getBeneficiaryRegID(),
//							visitID, benFlowID, beneficiaryVisitDetail.getVisitReason(),
//							beneficiaryVisitDetail.getVisitCategory(), ncdScreening.getIsScreeningComplete(),
//							benVisitCode, nurseUtilityClass.getVanID());
//
//					result = 1;
//				} else {
//					throw new RuntimeException("Error occurred while saving data");
//				}
//			} else
//				throw new RuntimeException("Error occurred while creating beneficiary visit");
//		} else {
//			throw new Exception("Invalid input");
//		}
//		return result;
		//Shubham Shekhar,9-12-2020,WDF
		Long saveSuccessFlag = null;
		TeleconsultationRequestOBJ tcRequestOBJ = null;
		// check if visit details data is not null
		if (requestOBJ != null && requestOBJ.has("visitDetails") && !requestOBJ.get("visitDetails").isJsonNull()) {
			CommonUtilityClass nurseUtilityClass = InputMapper.gson().fromJson(requestOBJ, CommonUtilityClass.class);
			// Call method to save visit details data
			Map<String, Long> visitIdAndCodeMap = saveBenVisitDetails(requestOBJ.getAsJsonObject("visitDetails"),
					nurseUtilityClass);

			// 07-06-2018 visit code
			Long benVisitID = null;
			Long benVisitCode = null;

			if (visitIdAndCodeMap != null && visitIdAndCodeMap.size() > 0 && visitIdAndCodeMap.containsKey("visitID")
					&& visitIdAndCodeMap.containsKey("visitCode")) {
				benVisitID = visitIdAndCodeMap.get("visitID");
				benVisitCode = visitIdAndCodeMap.get("visitCode");

				nurseUtilityClass.setVisitCode(benVisitCode);
				nurseUtilityClass.setBenVisitID(benVisitID);
			}

			// check if visit details data saved successfully
			Long historySaveSuccessFlag = null;
			Long vitalSaveSuccessFlag = null;
			Long idrsFlag = null;
			Long physicalActivityFlag = null;
			Integer i = null;

			JsonObject tmpOBJ = requestOBJ.getAsJsonObject("visitDetails").getAsJsonObject("visitDetails");
			// Getting benflowID for ben status update
			Long benFlowID = null;

			// Above if block code replaced by below line
			benFlowID = nurseUtilityClass.getBenFlowID();
      
			if (benVisitID != null && benVisitID > 0) {
				//tcRequestOBJ = commonServiceImpl.createTcRequest(requestOBJ, nurseUtilityClass, Authorization);
				// call method to save History data
				if (requestOBJ.has("historyDetails") && !requestOBJ.get("historyDetails").isJsonNull())
					historySaveSuccessFlag = saveBenNCDCareHistoryDetails(
							requestOBJ.getAsJsonObject("historyDetails"), benVisitID, benVisitCode);
				// call method to save Vital data
				vitalSaveSuccessFlag = saveBenNCDCareVitalDetails(requestOBJ.getAsJsonObject("vitalDetails"),
						benVisitID, benVisitCode);
                //call method to save IDRS data
				idrsFlag=saveidrsDetails(requestOBJ.getAsJsonObject("idrsDetails"),
						benVisitID, benVisitCode);
				//call method to save physical activity
				physicalActivityFlag=savePhysicalActivityDetails(requestOBJ.getAsJsonObject("historyDetails").getAsJsonObject("physicalActivityHistory"),
						benVisitID, benVisitCode);
				// i = commonNurseServiceImpl.updateBeneficiaryStatus('N',
				// tmpOBJ.get("beneficiaryRegID").getAsLong());
			} else {
				throw new RuntimeException("Error occurred while creating beneficiary visit");
			}
			if ((null != historySaveSuccessFlag && historySaveSuccessFlag > 0)
					&& (null != vitalSaveSuccessFlag && vitalSaveSuccessFlag > 0)) {

				/**
				 * We have to write new code to update ben status flow new logic
				 */
				int J = updateBenStatusFlagAfterNurseSaveSuccess(tmpOBJ, benVisitID, benFlowID, benVisitCode,
						nurseUtilityClass.getVanID());

				if (J > 0)
					saveSuccessFlag = historySaveSuccessFlag;
				else
					throw new RuntimeException("Error occurred while saving data. Beneficiary status update failed");


			} else {
				throw new RuntimeException("Error occurred while saving data");
			}
		} else {
			throw new Exception("Invalid input");
		}
		return saveSuccessFlag;

	}
	/**
	 * 
	 * @param requestOBJ
	 * @return success or failure flag for visitDetails data saving
	 */
	public Long saveBenNCDCareHistoryDetails(JsonObject ncdCareHistoryOBJ, Long benVisitID, Long benVisitCode)
			throws Exception {
		Long pastHistorySuccessFlag = null;
		Long comrbidSuccessFlag = null;
		Long medicationSuccessFlag = null;
		Long obstetricSuccessFlag = null;
		Integer menstrualHistorySuccessFlag = null;
		Long familyHistorySuccessFlag = null;
		Integer personalHistorySuccessFlag = null;
		Long allergyHistorySuccessFlag = null;
		Long childVaccineSuccessFlag = null;
		Long immunizationSuccessFlag = null;
		Long developmentHistorySuccessFlag = null;
		Long childFeedingSuccessFlag = null;
		Long perinatalHistorySuccessFlag = null;

		// Save past History
		if (ncdCareHistoryOBJ != null && ncdCareHistoryOBJ.has("pastHistory")
				&& !ncdCareHistoryOBJ.get("pastHistory").isJsonNull()) {
			BenMedHistory benMedHistory = InputMapper.gson().fromJson(ncdCareHistoryOBJ.get("pastHistory"),
					BenMedHistory.class);
			if (null != benMedHistory) {
				benMedHistory.setBenVisitID(benVisitID);
				benMedHistory.setVisitCode(benVisitCode);
				pastHistorySuccessFlag = commonNurseServiceImpl.saveBenPastHistory(benMedHistory);
			}

		} else {
			pastHistorySuccessFlag = new Long(1);
		}

		// Save Comorbidity/concurrent Conditions
		if (ncdCareHistoryOBJ != null && ncdCareHistoryOBJ.has("comorbidConditions")
				&& !ncdCareHistoryOBJ.get("comorbidConditions").isJsonNull()) {
			WrapperComorbidCondDetails wrapperComorbidCondDetails = InputMapper.gson()
					.fromJson(ncdCareHistoryOBJ.get("comorbidConditions"), WrapperComorbidCondDetails.class);
			if (null != wrapperComorbidCondDetails) {
				wrapperComorbidCondDetails.setBenVisitID(benVisitID);
				wrapperComorbidCondDetails.setVisitCode(benVisitCode);
				comrbidSuccessFlag = commonNurseServiceImpl.saveBenComorbidConditions(wrapperComorbidCondDetails);
			}
		} else {
			comrbidSuccessFlag = new Long(1);
		}

		// Save Medication History
		if (ncdCareHistoryOBJ != null && ncdCareHistoryOBJ.has("medicationHistory")
				&& !ncdCareHistoryOBJ.get("medicationHistory").isJsonNull()) {
			WrapperMedicationHistory wrapperMedicationHistory = InputMapper.gson()
					.fromJson(ncdCareHistoryOBJ.get("medicationHistory"), WrapperMedicationHistory.class);
			if (null != wrapperMedicationHistory
					&& wrapperMedicationHistory.getBenMedicationHistoryDetails().size() > 0) {
				wrapperMedicationHistory.setBenVisitID(benVisitID);
				wrapperMedicationHistory.setVisitCode(benVisitCode);
				medicationSuccessFlag = commonNurseServiceImpl.saveBenMedicationHistory(wrapperMedicationHistory);
			} else {
				medicationSuccessFlag = new Long(1);
			}

		} else {
			medicationSuccessFlag = new Long(1);
		}

		// Save Past Obstetric History
		if (ncdCareHistoryOBJ != null && ncdCareHistoryOBJ.has("femaleObstetricHistory")
				&& !ncdCareHistoryOBJ.get("femaleObstetricHistory").isJsonNull()) {
			WrapperFemaleObstetricHistory wrapperFemaleObstetricHistory = InputMapper.gson()
					.fromJson(ncdCareHistoryOBJ.get("femaleObstetricHistory"), WrapperFemaleObstetricHistory.class);

			if (wrapperFemaleObstetricHistory != null) {
				wrapperFemaleObstetricHistory.setBenVisitID(benVisitID);
				wrapperFemaleObstetricHistory.setVisitCode(benVisitCode);
				obstetricSuccessFlag = commonNurseServiceImpl.saveFemaleObstetricHistory(wrapperFemaleObstetricHistory);
			} else {
				// Female Obstetric Details not provided.
			}

		} else {
			obstetricSuccessFlag = new Long(1);
		}

		// Save Menstrual History
		if (ncdCareHistoryOBJ != null && ncdCareHistoryOBJ.has("menstrualHistory")
				&& !ncdCareHistoryOBJ.get("menstrualHistory").isJsonNull()) {
			BenMenstrualDetails menstrualDetails = InputMapper.gson()
					.fromJson(ncdCareHistoryOBJ.get("menstrualHistory"), BenMenstrualDetails.class);
			if (null != menstrualDetails) {
				menstrualDetails.setBenVisitID(benVisitID);
				menstrualDetails.setVisitCode(benVisitCode);
				menstrualHistorySuccessFlag = commonNurseServiceImpl.saveBenMenstrualHistory(menstrualDetails);
			}

		} else {
			menstrualHistorySuccessFlag = 1;
		}

		// Save Family History
		if (ncdCareHistoryOBJ != null && ncdCareHistoryOBJ.has("familyHistory")
				&& !ncdCareHistoryOBJ.get("familyHistory").isJsonNull()) {
			BenFamilyHistory benFamilyHistory = InputMapper.gson().fromJson(ncdCareHistoryOBJ.get("familyHistory"),
					BenFamilyHistory.class);
			if (null != benFamilyHistory) {
				benFamilyHistory.setBenVisitID(benVisitID);
				benFamilyHistory.setVisitCode(benVisitCode);
				familyHistorySuccessFlag = commonNurseServiceImpl.saveBenFamilyHistory(benFamilyHistory);
			}
		} else {
			familyHistorySuccessFlag = new Long(1);
		}

		// Save Personal History
		if (ncdCareHistoryOBJ != null && ncdCareHistoryOBJ.has("personalHistory")
				&& !ncdCareHistoryOBJ.get("personalHistory").isJsonNull()) {
			// Save Ben Personal Habits..
			BenPersonalHabit personalHabit = InputMapper.gson().fromJson(ncdCareHistoryOBJ.get("personalHistory"),
					BenPersonalHabit.class);
			if (null != personalHabit) {
				personalHabit.setBenVisitID(benVisitID);
				personalHabit.setVisitCode(benVisitCode);
				personalHistorySuccessFlag = commonNurseServiceImpl.savePersonalHistory(personalHabit);
			}

			BenAllergyHistory benAllergyHistory = InputMapper.gson().fromJson(ncdCareHistoryOBJ.get("personalHistory"),
					BenAllergyHistory.class);
			if (null != benAllergyHistory) {
				benAllergyHistory.setBenVisitID(benVisitID);
				benAllergyHistory.setVisitCode(benVisitCode);
				allergyHistorySuccessFlag = commonNurseServiceImpl.saveAllergyHistory(benAllergyHistory);
			}

		} else {
			personalHistorySuccessFlag = 1;
			allergyHistorySuccessFlag = new Long(1);
		}

		// Save Other/Optional Vaccines History
		if (ncdCareHistoryOBJ != null && ncdCareHistoryOBJ.has("childVaccineDetails")
				&& !ncdCareHistoryOBJ.get("childVaccineDetails").isJsonNull()) {
			WrapperChildOptionalVaccineDetail wrapperChildVaccineDetail = InputMapper.gson()
					.fromJson(ncdCareHistoryOBJ.get("childVaccineDetails"), WrapperChildOptionalVaccineDetail.class);
			if (null != wrapperChildVaccineDetail) {
				wrapperChildVaccineDetail.setBenVisitID(benVisitID);
				wrapperChildVaccineDetail.setVisitCode(benVisitCode);
				childVaccineSuccessFlag = commonNurseServiceImpl
						.saveChildOptionalVaccineDetail(wrapperChildVaccineDetail);
			} else {
				// Child Optional Vaccine Detail not provided.
			}

		} else {
			childVaccineSuccessFlag = new Long(1);
		}

		// Save Immunization History
		if (ncdCareHistoryOBJ != null && ncdCareHistoryOBJ.has("immunizationHistory")
				&& !ncdCareHistoryOBJ.get("immunizationHistory").isJsonNull()) {
			WrapperImmunizationHistory wrapperImmunizationHistory = InputMapper.gson()
					.fromJson(ncdCareHistoryOBJ.get("immunizationHistory"), WrapperImmunizationHistory.class);
			if (null != wrapperImmunizationHistory) {
				wrapperImmunizationHistory.setBenVisitID(benVisitID);
				wrapperImmunizationHistory.setVisitCode(benVisitCode);
				immunizationSuccessFlag = commonNurseServiceImpl.saveImmunizationHistory(wrapperImmunizationHistory);
			} else {

				// ImmunizationList Data not Available
			}

		} else {
			immunizationSuccessFlag = new Long(1);
		}

		// Save Development History
		if (ncdCareHistoryOBJ != null && ncdCareHistoryOBJ.has("developmentHistory")
				&& !ncdCareHistoryOBJ.get("developmentHistory").isJsonNull()) {
			BenChildDevelopmentHistory benChildDevelopmentHistory = InputMapper.gson()
					.fromJson(ncdCareHistoryOBJ.get("developmentHistory"), BenChildDevelopmentHistory.class);

			if (null != benChildDevelopmentHistory) {
				benChildDevelopmentHistory.setBenVisitID(benVisitID);
				benChildDevelopmentHistory.setVisitCode(benVisitCode);
				developmentHistorySuccessFlag = commonNurseServiceImpl
						.saveChildDevelopmentHistory(benChildDevelopmentHistory);
			}

		} else {
			developmentHistorySuccessFlag = new Long(1);
		}

		// Save Feeding History
		if (ncdCareHistoryOBJ != null && ncdCareHistoryOBJ.has("feedingHistory")
				&& !ncdCareHistoryOBJ.get("feedingHistory").isJsonNull()) {
			ChildFeedingDetails childFeedingDetails = InputMapper.gson()
					.fromJson(ncdCareHistoryOBJ.get("feedingHistory"), ChildFeedingDetails.class);

			if (null != childFeedingDetails) {
				childFeedingDetails.setBenVisitID(benVisitID);
				childFeedingDetails.setVisitCode(benVisitCode);
				childFeedingSuccessFlag = commonNurseServiceImpl.saveChildFeedingHistory(childFeedingDetails);
			}

		}
		{
			childFeedingSuccessFlag = new Long(1);
		}

		// Save Perinatal Histroy
		if (ncdCareHistoryOBJ != null && ncdCareHistoryOBJ.has("perinatalHistroy")
				&& !ncdCareHistoryOBJ.get("perinatalHistroy").isJsonNull()) {
			PerinatalHistory perinatalHistory = InputMapper.gson().fromJson(ncdCareHistoryOBJ.get("perinatalHistroy"),
					PerinatalHistory.class);

			if (null != perinatalHistory) {
				perinatalHistory.setBenVisitID(benVisitID);
				perinatalHistory.setVisitCode(benVisitCode);
				perinatalHistorySuccessFlag = commonNurseServiceImpl.savePerinatalHistory(perinatalHistory);
			}

		}
		{
			perinatalHistorySuccessFlag = new Long(1);
		}

		Long historySaveSucccessFlag = null;

		if ((null != pastHistorySuccessFlag && pastHistorySuccessFlag > 0)
				&& (null != comrbidSuccessFlag && comrbidSuccessFlag > 0)
				&& (null != medicationSuccessFlag && medicationSuccessFlag > 0)
				&& (null != obstetricSuccessFlag && obstetricSuccessFlag > 0)
				&& (null != menstrualHistorySuccessFlag && menstrualHistorySuccessFlag > 0)
				&& (null != familyHistorySuccessFlag && familyHistorySuccessFlag > 0)
				&& (null != personalHistorySuccessFlag && personalHistorySuccessFlag > 0)
				&& (null != allergyHistorySuccessFlag && allergyHistorySuccessFlag > 0)
				&& (null != childVaccineSuccessFlag && childVaccineSuccessFlag > 0)
				&& (null != immunizationSuccessFlag && immunizationSuccessFlag > 0)
				&& (null != developmentHistorySuccessFlag && developmentHistorySuccessFlag > 0)
				&& (null != childFeedingSuccessFlag && childFeedingSuccessFlag > 0)
				&& (null != perinatalHistorySuccessFlag && perinatalHistorySuccessFlag > 0)) {

			historySaveSucccessFlag = pastHistorySuccessFlag;
		}
		return historySaveSucccessFlag;
	}
	public Long saveidrsDetails(JsonObject idrsDetailsOBJ, Long benVisitID, Long benVisitCode)
			throws Exception {
		Long idrsFlag = null;
		// Save Physical Anthropometry && Physical Vital Details
		if (idrsDetailsOBJ != null) {
			IDRSData idrsDetail = InputMapper.gson().fromJson(idrsDetailsOBJ,
					IDRSData.class);
            String temp=""; 
			if (null != idrsDetail) {
				if(idrsDetail.getQuestionArray()!=null && idrsDetail.getQuestionArray().length>0)
				{
					IDRSData[] ar=idrsDetail.getQuestionArray();
					for(int i=0;i<ar.length;i++)
					{
						 idrsDetail = InputMapper.gson().fromJson(idrsDetailsOBJ,
								IDRSData.class);temp="";
						idrsDetail.setIdrsQuestionID(ar[i].getIdrsQuestionID());
						idrsDetail.setAnswer(ar[i].getAnswer());
						idrsDetail.setQuestion(ar[i].getQuestion());
						idrsDetail.setDiseaseQuestionType(ar[i].getDiseaseQuestionType());
						idrsDetail.setBenVisitID(benVisitID);
						idrsDetail.setVisitCode(benVisitCode);
						if(idrsDetail.getSuspectArray()!=null)
						{
							for(int a=0;a<idrsDetail.getSuspectArray().length;a++)
					    	{
					    		if(a==idrsDetail.getSuspectArray().length-1)
					    		temp+=idrsDetail.getSuspectArray()[a];
					    		else
					    		temp=temp+idrsDetail.getSuspectArray()[a]+",";
					    	}
							if(temp.equalsIgnoreCase(""))
								temp=null;
							idrsDetail.setSuspectedDisease(temp);
						}
						idrsFlag = commonNurseServiceImpl
								.saveIDRS(idrsDetail);
					}
				}
				else
				{
					idrsDetail.setBenVisitID(benVisitID);
					idrsDetail.setVisitCode(benVisitCode);
					idrsFlag = commonNurseServiceImpl
							.saveIDRS(idrsDetail);
				}
				
			}

//			if (idrsFlag != null && idrsFlag > 0 ) {
//				vitalSuccessFlag = anthropometrySuccessFlag;
//			}
		}

		return idrsFlag;
	}
	
	public Long savePhysicalActivityDetails(JsonObject physicalActivityDetailsOBJ, Long benVisitID, Long benVisitCode)
			throws Exception {
		Long physicalActivityFlag = null;
		// Save Physical Anthropometry && Physical Vital Details
		if (physicalActivityDetailsOBJ != null) {
			PhysicalActivityType physicalActivityDetail = InputMapper.gson().fromJson(physicalActivityDetailsOBJ,
					PhysicalActivityType.class);

			if (null != physicalActivityDetail && (physicalActivityDetail.getActivityType() !=null || physicalActivityDetail.getPhysicalActivityType() != null)) {
				physicalActivityDetail.setBenVisitID(benVisitID);
				physicalActivityDetail.setVisitCode(benVisitCode);
				physicalActivityFlag = commonNurseServiceImpl
						.savePhysicalActivity(physicalActivityDetail);
			}

//			if (idrsFlag != null && idrsFlag > 0 ) {
//				vitalSuccessFlag = anthropometrySuccessFlag;
//			}
		}

		return physicalActivityFlag;
	}
	/**
	 * 
	 * @param requestOBJ
	 * @return success or failure flag for visitDetails data saving
	 */
	public Long saveBenNCDCareVitalDetails(JsonObject vitalDetailsOBJ, Long benVisitID, Long benVisitCode)
			throws Exception {
		Long vitalSuccessFlag = null;
		Long anthropometrySuccessFlag = null;
		Long phyVitalSuccessFlag = null;
		// Save Physical Anthropometry && Physical Vital Details
		if (vitalDetailsOBJ != null) {
			BenAnthropometryDetail benAnthropometryDetail = InputMapper.gson().fromJson(vitalDetailsOBJ,
					BenAnthropometryDetail.class);
			BenPhysicalVitalDetail benPhysicalVitalDetail = InputMapper.gson().fromJson(vitalDetailsOBJ,
					BenPhysicalVitalDetail.class);

			if (null != benAnthropometryDetail) {
				benAnthropometryDetail.setBenVisitID(benVisitID);
				benAnthropometryDetail.setVisitCode(benVisitCode);
				anthropometrySuccessFlag = commonNurseServiceImpl
						.saveBeneficiaryPhysicalAnthropometryDetails(benAnthropometryDetail);
			}
			if (null != benPhysicalVitalDetail) {
				benPhysicalVitalDetail.setBenVisitID(benVisitID);
				benPhysicalVitalDetail.setVisitCode(benVisitCode);
				phyVitalSuccessFlag = commonNurseServiceImpl
						.saveBeneficiaryPhysicalVitalDetails(benPhysicalVitalDetail);
			}

			if (anthropometrySuccessFlag != null && anthropometrySuccessFlag > 0 && phyVitalSuccessFlag != null
					&& phyVitalSuccessFlag > 0) {
				vitalSuccessFlag = anthropometrySuccessFlag;
			}
		}

		return vitalSuccessFlag;
	}

	/**
	 * 
	 * @param requestOBJ
	 * @return success or failure flag for visitDetails data saving
	 */
	public Map<String, Long> saveBenVisitDetails(JsonObject visitDetailsOBJ, CommonUtilityClass nurseUtilityClass)
			throws Exception {
		Map<String, Long> visitIdAndCodeMap = new HashMap<>();
		Long benVisitID = null;
		int adherenceSuccessFlag = 0;
		int investigationSuccessFlag = 0;
		if (visitDetailsOBJ != null && visitDetailsOBJ.has("visitDetails")
				&& !visitDetailsOBJ.get("visitDetails").isJsonNull()) {

			BeneficiaryVisitDetail benVisitDetailsOBJ = InputMapper.gson().fromJson(visitDetailsOBJ.get("visitDetails"),
					BeneficiaryVisitDetail.class);
			benVisitID = commonNurseServiceImpl.saveBeneficiaryVisitDetails(benVisitDetailsOBJ);

			// 11-06-2018 visit code
			Long benVisitCode = commonNurseServiceImpl.generateVisitCode(benVisitID, nurseUtilityClass.getVanID(),
					nurseUtilityClass.getSessionID());

			if (benVisitID != null && benVisitID > 0 && benVisitCode != null && benVisitCode > 0) {
				if (visitDetailsOBJ.has("chiefComplaints") && !visitDetailsOBJ.get("chiefComplaints").isJsonNull()) {
					BenChiefComplaint[] benChiefComplaintArray = InputMapper.gson()
							.fromJson(visitDetailsOBJ.get("chiefComplaints"), BenChiefComplaint[].class);

					List<BenChiefComplaint> benChiefComplaintList = Arrays.asList(benChiefComplaintArray);
					if (null != benChiefComplaintList && benChiefComplaintList.size() > 0) {
						for (BenChiefComplaint benChiefComplaint : benChiefComplaintList) {
							benChiefComplaint.setBenVisitID(benVisitID);
							benChiefComplaint.setVisitCode(benVisitCode);
						}
					}
					// Save Beneficiary Chief Complaints
					commonNurseServiceImpl.saveBenChiefComplaints(benChiefComplaintList);
				}
			}
			visitIdAndCodeMap.put("visitID", benVisitID);
			visitIdAndCodeMap.put("visitCode", benVisitCode);
		}
		return visitIdAndCodeMap;
	}
	// method for updating ben flow status flag for nurse
		private int updateBenStatusFlagAfterNurseSaveSuccess(JsonObject tmpOBJ, Long benVisitID, Long benFlowID,
				Long benVisitCode, Integer vanID) {
			short nurseFlag = (short) 9;
			short docFlag = (short) 1;
			short labIteration = (short) 0;

			int i = commonBenStatusFlowServiceImpl.updateBenFlowNurseAfterNurseActivity(benFlowID,
					tmpOBJ.get("beneficiaryRegID").getAsLong(), benVisitID, tmpOBJ.get("visitReason").getAsString(),
					tmpOBJ.get("visitCategory").getAsString(), nurseFlag, docFlag, labIteration, (short) 0, (short) 0,
					benVisitCode, vanID);

			return i;
		}
	private int updateBenFlowNurseAfterNurseActivityANC(Long benRegID, Long benVisitID, Long benFlowID,
			String visitReason, String visitCategory, Boolean isScreeningDone, Long benVisitCode, Integer vanID) {
		short nurseFlag;
		short docFlag = (short) 0;
		short labIteration = (short) 0;

		if (isScreeningDone != null && isScreeningDone == true)
			nurseFlag = (short) 9;
		else
			nurseFlag = (short) 100;

		int rs = commonBenStatusFlowServiceImpl.updateBenFlowNurseAfterNurseActivity(benFlowID, benRegID, benVisitID,
				visitReason, visitCategory, nurseFlag, docFlag, labIteration, (short) 0, (short) 0, benVisitCode,
				vanID);

		return rs;
	}

	public Long saveNCDScreeningVitalDetails(JsonObject jsonObject, Long benVisitID, Long benVisitCode)
			throws Exception {

		Long vitalSuccessFlag = null;
		JsonElement ncdScreeningDetails = jsonObject.get("ncdScreeningDetails");

		BenAnthropometryDetail anthropometryDetail = null;
		BenPhysicalVitalDetail physicalVitalDetail = null;

		anthropometryDetail = InputMapper.gson().fromJson(ncdScreeningDetails, BenAnthropometryDetail.class);

		physicalVitalDetail = InputMapper.gson().fromJson(ncdScreeningDetails, BenPhysicalVitalDetail.class);

		Long saveAnthropometryDetail = null;
		if (null != anthropometryDetail) {
			anthropometryDetail.setBenVisitID(benVisitID);
			anthropometryDetail.setVisitCode(benVisitCode);
			saveAnthropometryDetail = commonNurseServiceImpl
					.saveBeneficiaryPhysicalAnthropometryDetails(anthropometryDetail);
		}
		Long savePhysicalVitalDetails = null;
		if (null != physicalVitalDetail) {
			physicalVitalDetail.setBenVisitID(benVisitID);
			physicalVitalDetail.setVisitCode(benVisitCode);
			savePhysicalVitalDetails = commonNurseServiceImpl.saveBeneficiaryPhysicalVitalDetails(physicalVitalDetail);
		}

		if ((null != saveAnthropometryDetail && saveAnthropometryDetail > 0)
				&& (null != savePhysicalVitalDetails && savePhysicalVitalDetails > 0)) {
			vitalSuccessFlag = saveAnthropometryDetail;
		}

		return vitalSuccessFlag;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer updateNurseNCDScreeningDetails(JsonObject jsonObject) throws Exception {

		NCDScreening ncdScreening = InputMapper.gson().fromJson(jsonObject, NCDScreening.class);

		if (ncdScreening.getNextScreeningDate() != null)
			ncdScreening.setNextScreeningDateDB(
					Timestamp.valueOf(ncdScreening.getNextScreeningDate().replaceAll("T", " ").replaceAll("Z", " ")));

		BenAnthropometryDetail anthropometryDetail = InputMapper.gson().fromJson(jsonObject,
				BenAnthropometryDetail.class);
		BenPhysicalVitalDetail physicalVitalDetail = InputMapper.gson().fromJson(jsonObject,
				BenPhysicalVitalDetail.class);

		Integer result = null;

		Integer updateNCDScreeningDetails = ncdScreeningNurseServiceImpl.updateNCDScreeningDetails(ncdScreening);
		Integer updateANCAnthropometryDetails = commonNurseServiceImpl
				.updateANCAnthropometryDetails(anthropometryDetail);
		Integer updateANCPhysicalVitalDetails = commonNurseServiceImpl
				.updateANCPhysicalVitalDetails(physicalVitalDetail);

		// add file/doc id
		String[] docIdArr = ncdScreening.getFileIDs();
		StringBuilder sb = new StringBuilder();
		if (docIdArr != null && docIdArr.length > 0) {
			for (String i : docIdArr) {
				sb.append(i + ",");
			}
		}
		// update file path in db
		if (sb.length() > 0)
			benVisitDetailRepo.updateFileID(sb.toString(), ncdScreening.getBeneficiaryRegID(),
					ncdScreening.getVisitCode());

		if (null != updateANCAnthropometryDetails && null != updateANCPhysicalVitalDetails
				&& null != updateNCDScreeningDetails) {

			short nurseFlag = (short) 0;

			if (ncdScreening.getIsScreeningComplete() != null && ncdScreening.getIsScreeningComplete() == true)
				nurseFlag = (short) 9;
			else
				nurseFlag = (short) 100;

			int i = commonBenStatusFlowServiceImpl.updateBenFlowNurseAfterNurseUpdateNCD_Screening(
					ncdScreening.getBenFlowID(), ncdScreening.getBeneficiaryRegID(), nurseFlag);

			result = 1;
		}

		return result;

	}
	
	@Transactional(rollbackFor = Exception.class)
	public int updateBenVitalDetails(JsonObject vitalDetailsOBJ) throws Exception {
		int vitalSuccessFlag = 0;
		int anthropometrySuccessFlag = 0;
		int phyVitalSuccessFlag = 0;
		// Save Physical Anthropometry && Physical Vital Details
		if (vitalDetailsOBJ != null) {
			BenAnthropometryDetail benAnthropometryDetail = InputMapper.gson().fromJson(vitalDetailsOBJ,
					BenAnthropometryDetail.class);
			BenPhysicalVitalDetail benPhysicalVitalDetail = InputMapper.gson().fromJson(vitalDetailsOBJ,
					BenPhysicalVitalDetail.class);

			anthropometrySuccessFlag = commonNurseServiceImpl.updateANCAnthropometryDetails(benAnthropometryDetail);
			phyVitalSuccessFlag = commonNurseServiceImpl.updateANCPhysicalVitalDetails(benPhysicalVitalDetail);

			if (anthropometrySuccessFlag > 0 && phyVitalSuccessFlag > 0) {
				vitalSuccessFlag = anthropometrySuccessFlag;
			}
		} else {
			vitalSuccessFlag = 1;
		}

		return vitalSuccessFlag;
	}

	@Override
	public String getNCDScreeningDetails(Long beneficiaryRegID, Long visitCode) {
		String ncdScreeningDetails = ncdScreeningNurseServiceImpl.getNCDScreeningDetails(beneficiaryRegID, visitCode);
		String anthropometryDetails = commonNurseServiceImpl
				.getBeneficiaryPhysicalAnthropometryDetails(beneficiaryRegID, visitCode);
		String vitalDetails = commonNurseServiceImpl.getBeneficiaryPhysicalVitalDetails(beneficiaryRegID, visitCode);

		Map<String, Object> res = new HashMap<String, Object>();

		if (ncdScreeningDetails != null && anthropometryDetails != null && vitalDetails != null) {
			res.put("ncdScreeningDetails", ncdScreeningDetails);
			res.put("anthropometryDetails", anthropometryDetails);
			res.put("vitalDetails", vitalDetails);
		} else {
			// Failed to Fetch Beneficiary NCD Screening Details
		}
		return res.toString();
	}

	public String getNcdScreeningVisitCnt(Long beneficiaryRegID) {
		Map<String, Long> returnMap = new HashMap<>();
		Long visitCount = beneficiaryFlowStatusRepo.getNcdScreeningVisitCount(beneficiaryRegID);
		returnMap.put("ncdScreeningVisitCount", visitCount + 1);
		return new Gson().toJson(returnMap);
	}
	
	/// --------------- Start of Fetching NCD Screening Nurse Data ----------------
	public String getBenVisitDetailsFrmNurseNCDScreening(Long benRegID, Long visitCode) {
		Map<String, Object> resMap = new HashMap<>();

		BeneficiaryVisitDetail visitDetail = commonNurseServiceImpl.getCSVisitDetails(benRegID, visitCode);

		resMap.put("NCDScreeningNurseVisitDetail", new Gson().toJson(visitDetail));

		resMap.put("BenChiefComplaints", commonNurseServiceImpl.getBenChiefComplaints(benRegID, visitCode));

		return resMap.toString();
	}
	
	public String getBenHistoryDetails(Long benRegID, Long visitCode) {
		
		Map<String, Object> HistoryDetailsMap = new HashMap<String, Object>();

		
		HistoryDetailsMap.put("FamilyHistory", commonNurseServiceImpl.getFamilyHistoryDetail(benRegID, visitCode));
		HistoryDetailsMap.put("PhysicalActivityHistory", commonNurseServiceImpl.getPhysicalActivityType(benRegID, visitCode));
		

		return new Gson().toJson(HistoryDetailsMap);
	}
	
	public String getBenIdrsDetailsFrmNurse(Long beneficiaryRegID, Long benVisitID) {
		Map<String, Object> resMap = new HashMap<>();

		resMap.put("IDRSDetail",
				commonNurseServiceImpl.getBeneficiaryIdrsDetails(beneficiaryRegID, benVisitID));
		

		return resMap.toString();
	}


	
	public String getBeneficiaryVitalDetails(Long beneficiaryRegID, Long benVisitID) {
		Map<String, Object> resMap = new HashMap<>();

		resMap.put("benAnthropometryDetail",
				commonNurseServiceImpl.getBeneficiaryPhysicalAnthropometryDetails(beneficiaryRegID, benVisitID));
		resMap.put("benPhysicalVitalDetail",
				commonNurseServiceImpl.getBeneficiaryPhysicalVitalDetails(beneficiaryRegID, benVisitID));

		return resMap.toString();
	}


	@Override
	public Integer UpdateNCDScreeningHistory(JsonObject historyOBJ) throws Exception {

		int familyHistorySuccessFlag = 0;
		int historyUpdatedSuccessfully = 0;
		int physicalActivitySuccessFlag = 0;

		// Update Family History
		if (historyOBJ != null && historyOBJ.has("familyHistory") && !historyOBJ.get("familyHistory").isJsonNull()) {
			BenFamilyHistory benFamilyHistory = InputMapper.gson().fromJson(historyOBJ.get("familyHistory"),
					BenFamilyHistory.class);

			familyHistorySuccessFlag = commonNurseServiceImpl.updateBenFamilyHistoryNCDScreening(benFamilyHistory);
		} else {
			familyHistorySuccessFlag = 0;
		}
		
		//Update Physical Activity
		if (historyOBJ != null && historyOBJ.has("physicalActivityHistory") && !historyOBJ.get("physicalActivityHistory").isJsonNull()) {
			PhysicalActivityType physicalActivityType = InputMapper.gson().fromJson(historyOBJ.get("physicalActivityHistory"),
					PhysicalActivityType.class);
			
			physicalActivitySuccessFlag = commonNurseServiceImpl.updateBenPhysicalActivityHistoryNCDScreening(physicalActivityType);
		}else {
			physicalActivitySuccessFlag = 0;
		}
		

		if(familyHistorySuccessFlag > 0 && physicalActivitySuccessFlag > 0) {
			historyUpdatedSuccessfully = 1;
		}
		return historyUpdatedSuccessfully;
	}

	

}
