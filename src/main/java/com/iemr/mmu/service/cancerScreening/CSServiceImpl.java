package com.iemr.mmu.service.cancerScreening;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.iemr.mmu.data.benFlowStatus.BeneficiaryFlowStatus;
import com.iemr.mmu.data.doctor.CancerAbdominalExamination;
import com.iemr.mmu.data.doctor.CancerBreastExamination;
import com.iemr.mmu.data.doctor.CancerDiagnosis;
import com.iemr.mmu.data.doctor.CancerGynecologicalExamination;
import com.iemr.mmu.data.doctor.CancerOralExamination;
import com.iemr.mmu.data.doctor.WrapperCancerExamImgAnotasn;
import com.iemr.mmu.data.doctor.WrapperCancerSymptoms;
import com.iemr.mmu.data.nurse.BenCancerVitalDetail;
import com.iemr.mmu.data.nurse.BenFamilyCancerHistory;
import com.iemr.mmu.data.nurse.BenObstetricCancerHistory;
import com.iemr.mmu.data.nurse.BenPersonalCancerDietHistory;
import com.iemr.mmu.data.nurse.BenPersonalCancerHistory;
import com.iemr.mmu.data.nurse.BeneficiaryVisitDetail;
import com.iemr.mmu.repo.benFlowStatus.BeneficiaryFlowStatusRepo;
import com.iemr.mmu.repo.registrar.RegistrarRepoBenData;
import com.iemr.mmu.service.benFlowStatus.CommonBenStatusFlowServiceImpl;
import com.iemr.mmu.service.common.transaction.CommonNurseServiceImpl;
import com.iemr.mmu.service.doctor.DoctorServiceImpl;
import com.iemr.mmu.service.nurse.NurseServiceImpl;
import com.iemr.mmu.service.registrar.RegistrarServiceImpl;
import com.iemr.mmu.utils.mapper.InputMapper;

@Service
public class CSServiceImpl implements CSService {

	@Value("${registrarQuickSearchByIdUrl}")
	private String registrarQuickSearchByIdUrl;

	private NurseServiceImpl nurseServiceImpl;
	private CSNurseServiceImpl cSNurseServiceImpl;
	private CSDoctorServiceImpl cSDoctorServiceImpl;
	private RegistrarServiceImpl registrarServiceImpl;
	private CSOncologistServiceImpl csOncologistServiceImpl;
	private DoctorServiceImpl doctorServiceImpl;
	private CommonNurseServiceImpl commonNurseServiceImpl;
	private CSCarestreamServiceImpl cSCarestreamServiceImpl;
	private RegistrarRepoBenData registrarRepoBenData;
	private CommonBenStatusFlowServiceImpl commonBenStatusFlowServiceImpl;
	private BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo;

	@Autowired
	public void setBeneficiaryFlowStatusRepo(BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo) {
		this.beneficiaryFlowStatusRepo = beneficiaryFlowStatusRepo;
	}

	@Autowired
	public void setCommonBenStatusFlowServiceImpl(CommonBenStatusFlowServiceImpl commonBenStatusFlowServiceImpl) {
		this.commonBenStatusFlowServiceImpl = commonBenStatusFlowServiceImpl;
	}

	@Autowired
	public void setRegistrarRepoBenData(RegistrarRepoBenData registrarRepoBenData) {
		this.registrarRepoBenData = registrarRepoBenData;
	}

	@Autowired
	public void setcSCarestreamServiceImpl(CSCarestreamServiceImpl cSCarestreamServiceImpl) {
		this.cSCarestreamServiceImpl = cSCarestreamServiceImpl;
	}

	@Autowired
	public void setCommonNurseServiceImpl(CommonNurseServiceImpl commonNurseServiceImpl) {
		this.commonNurseServiceImpl = commonNurseServiceImpl;
	}

	@Autowired
	public void setDoctorServiceImpl(DoctorServiceImpl doctorServiceImpl) {
		this.doctorServiceImpl = doctorServiceImpl;
	}

	@Autowired
	public void setRegistrarServiceImpl(RegistrarServiceImpl registrarServiceImpl) {
		this.registrarServiceImpl = registrarServiceImpl;
	}

	@Autowired
	public void setcSDoctorServiceImpl(CSDoctorServiceImpl cSDoctorServiceImpl) {
		this.cSDoctorServiceImpl = cSDoctorServiceImpl;
	}

	@Autowired
	public void setcSNurseServiceImpl(CSNurseServiceImpl cSNurseServiceImpl) {
		this.cSNurseServiceImpl = cSNurseServiceImpl;
	}

	@Autowired
	public void setNurseServiceImpl(NurseServiceImpl nurseServiceImpl) {
		this.nurseServiceImpl = nurseServiceImpl;
	}

	@Autowired
	public void setCsOncologistServiceImpl(CSOncologistServiceImpl csOncologistServiceImpl) {
		this.csOncologistServiceImpl = csOncologistServiceImpl;
	}

	// ----------Save/Create (Nurse)--------------------------------------

	/***
	 * 
	 * @param requestOBJ
	 * @throws Exception
	 */
	@Transactional(rollbackFor = Exception.class)
	public Long saveCancerScreeningNurseData(JsonObject requestOBJ, String Authorization) throws Exception {
		Long nurseDataSuccessFlag = null;
		// check if visit details data is not null
		if (requestOBJ != null && requestOBJ.has("visitDetails") && !requestOBJ.get("visitDetails").isJsonNull()) {
			// Call method to save visit details data
			BeneficiaryVisitDetail benVisitDetailsOBJ = InputMapper.gson().fromJson(requestOBJ.get("visitDetails"),
					BeneficiaryVisitDetail.class);

			Long benVisitID = saveBenVisitDetails(benVisitDetailsOBJ);

			// Getting benflowID for ben status update
			Long benFlowID = null;
			if (requestOBJ.has("benFlowID")) {
				benFlowID = requestOBJ.get("benFlowID").getAsLong();
			}

			// check if visit details data saved successfully
			if (benVisitID != null && benVisitID > 0) {

				Boolean isReferedToMammogram = null;
				// check if referred to mammogram is true or false
				if (requestOBJ.has("examinationDetails") && requestOBJ.get("examinationDetails") != null
						&& !requestOBJ.get("examinationDetails").isJsonNull()) {
					JsonObject examination = requestOBJ.getAsJsonObject("examinationDetails");
					if (examination.has("breastDetails") && examination.get("breastDetails") != null
							&& !examination.get("breastDetails").isJsonNull()) {
						JsonObject breastExamination = examination.getAsJsonObject("breastDetails");
						if (breastExamination.has("referredToMammogram")
								&& breastExamination.get("referredToMammogram") != null
								&& !breastExamination.get("referredToMammogram").isJsonNull()) {
							isReferedToMammogram = breastExamination.get("referredToMammogram").getAsBoolean();
						}

					}

				}

				// check if doctor visit required ??
				Boolean docVisitReq = false;
				if (requestOBJ.has("sendToDoctorWorklist") && requestOBJ.get("sendToDoctorWorklist") != null
						&& !requestOBJ.get("sendToDoctorWorklist").isJsonNull()) {
					docVisitReq = requestOBJ.get("sendToDoctorWorklist").getAsBoolean();
				}

				// call method to save history data
				Long historySaveSuccessFlag = saveBenHistoryDetails(requestOBJ, benVisitID);
				// call method to save Examination data
				Long examinationSuccessFlag = saveBenExaminationDetails(requestOBJ, benVisitID, Authorization);
				// call method to save vitals data
				Long vitalSaveSuccessFlag = saveBenVitalsDetails(requestOBJ, benVisitID);

				if ((historySaveSuccessFlag != null && historySaveSuccessFlag > 0)
						&& (examinationSuccessFlag != null && examinationSuccessFlag > 0)
						&& (vitalSaveSuccessFlag != null && vitalSaveSuccessFlag > 0)) {

					Integer i = commonNurseServiceImpl.updateBeneficiaryStatus('N', getBenRegID(requestOBJ));

					// if (requestOBJ != null &&
					// requestOBJ.has("sendToDoctorWorklist")
					// && !requestOBJ.get("sendToDoctorWorklist").isJsonNull())
					// {
					// if (requestOBJ.get("sendToDoctorWorklist").getAsBoolean()
					// == false) {
					// String s =
					// commonNurseServiceImpl.updateBenStatus(benVisitID, "Z");
					// }
					// }

					nurseDataSuccessFlag = examinationSuccessFlag;

					/**
					 * We have to write new code to update ben status flow new
					 * logic
					 */
					int j = updateBenStatusFlagAfterNurseSaveSuccess(benVisitDetailsOBJ, benVisitID, benFlowID,
							isReferedToMammogram, docVisitReq);

				}

			} else {
				// Error in visit details saving or it is null
			}

		}
		return nurseDataSuccessFlag;
	}

	// method for updating ben flow status flag for nurse
	private int updateBenStatusFlagAfterNurseSaveSuccess(BeneficiaryVisitDetail benVisitDetailsOBJ, Long benVisitID,
			Long benFlowID, Boolean isReferedToMammogram, Boolean docVisitReq) {
		short nurseFlag = (short) 9;
		short docFlag = (short) 0;
		short labIteration = (short) 0;
		short radiologistFlag = (short) 0;
		short oncologistFlag = (short) 0;

		if (isReferedToMammogram != null) {
			if (isReferedToMammogram == true)
				radiologistFlag = (short) 1;

		}

		if (docVisitReq == true)
			docFlag = (short) 1;
		else
			oncologistFlag = (short) 1;

		int i = commonBenStatusFlowServiceImpl.updateBenFlowNurseAfterNurseActivity(benFlowID,
				benVisitDetailsOBJ.getBeneficiaryRegID(), benVisitID, benVisitDetailsOBJ.getVisitReason(),
				benVisitDetailsOBJ.getVisitCategory(), nurseFlag, docFlag, labIteration, radiologistFlag,
				oncologistFlag);

		return i;
	}

	private Long getBenRegID(JsonObject requestOBJ) throws Exception {
		BeneficiaryVisitDetail benVisitDetailsOBJ = InputMapper.gson().fromJson(requestOBJ.get("visitDetails"),
				BeneficiaryVisitDetail.class);
		return benVisitDetailsOBJ.getBeneficiaryRegID();
	}

	/**
	 * 
	 * @param requestOBJ
	 * @return success or failure flag for visitDetails data saving
	 */
	public Long saveBenVisitDetails(BeneficiaryVisitDetail benVisitDetailsOBJ) throws Exception {
		// BeneficiaryVisitDetail benVisitDetailsOBJ =
		// InputMapper.gson().fromJson(requestOBJ.get("visitDetails"),
		// BeneficiaryVisitDetail.class);

		Long benVisitID = commonNurseServiceImpl.saveBeneficiaryVisitDetails(benVisitDetailsOBJ);
		// Long benVisitID =
		// nurseServiceImpl.saveBeneficiaryVisitDetails(benVisitDetailsOBJ);
		return benVisitID;
	}

	/**
	 * 
	 * @param requestOBJ
	 * @param benVisitID
	 * @return success or failure flag for history data saving
	 * @throws Exception
	 */
	public Long saveBenHistoryDetails(JsonObject requestOBJ, Long benVisitID) throws Exception {
		Integer benFamilyHistoryDataSavingSuccessFlag = null;
		Long benPersonalHistorySaveSuccessFlag = null;
		Long benPersonalDietHistorySaveSuccessFlag = null;
		Long benObstetricSaveSuccessFlag = null;
		Long csHistorySaveRes = null;

		if (requestOBJ != null && requestOBJ.has("historyDetails") && !requestOBJ.get("historyDetails").isJsonNull()) {
			JsonObject historyOBJ = requestOBJ.getAsJsonObject("historyDetails");

			if (historyOBJ != null && historyOBJ.has("familyHistory")
					&& !historyOBJ.get("familyHistory").isJsonNull()) {

				JsonObject familyHistoryOBJ = historyOBJ.getAsJsonObject("familyHistory");
				if (familyHistoryOBJ != null && familyHistoryOBJ.has("diseases")
						&& !familyHistoryOBJ.get("diseases").isJsonNull()) {
					BenFamilyCancerHistory[] benFamilyCancerHistoryArray = InputMapper.gson().fromJson(
							familyHistoryOBJ.get("diseases").getAsJsonArray(), BenFamilyCancerHistory[].class);
					List<BenFamilyCancerHistory> benFamilyCancerHistoryList = Arrays
							.asList(benFamilyCancerHistoryArray);
					if (benFamilyCancerHistoryArray != null && benFamilyCancerHistoryList.size() > 0) {
						for (BenFamilyCancerHistory obj : benFamilyCancerHistoryArray) {
							obj.setBenVisitID(benVisitID);
						}
						benFamilyHistoryDataSavingSuccessFlag = cSNurseServiceImpl
								.saveBenFamilyCancerHistory(benFamilyCancerHistoryList);

					} else {
						// Failed to store family History..
					}
				} else {
					// familyHistoryOBJ is null or diseases are not
					// there in familyHistoryOBJ or null
					benFamilyHistoryDataSavingSuccessFlag = 1;
				}
			} else {
				// history Obj is null or family history history is null
				// or not there in history obj !!!
				benFamilyHistoryDataSavingSuccessFlag = 1;
			}
			if (historyOBJ != null && historyOBJ.has("personalHistory")
					&& !historyOBJ.get("personalHistory").isJsonNull()) {
				BenPersonalCancerHistory benPersonalCancerHistory = InputMapper.gson()
						.fromJson(historyOBJ.get("personalHistory"), BenPersonalCancerHistory.class);

				BenPersonalCancerDietHistory benPersonalCancerDietHistory = InputMapper.gson()
						.fromJson(historyOBJ.get("personalHistory"), BenPersonalCancerDietHistory.class);

				if (benPersonalCancerHistory != null && benPersonalCancerDietHistory != null) {

					benPersonalCancerHistory.setBenVisitID(benVisitID);
					benPersonalCancerDietHistory.setBenVisitID(benVisitID);

					benPersonalHistorySaveSuccessFlag = cSNurseServiceImpl
							.saveBenPersonalCancerHistory(benPersonalCancerHistory);
					benPersonalDietHistorySaveSuccessFlag = cSNurseServiceImpl
							.saveBenPersonalCancerDietHistory(benPersonalCancerDietHistory);
				} else {
					// Failed to store Personal History..
				}
			} else {
				// ben personal history data not there !!!
				benPersonalHistorySaveSuccessFlag = new Long(1);
				benPersonalDietHistorySaveSuccessFlag = new Long(1);
			}
			if (historyOBJ != null && historyOBJ.has("pastObstetricHistory")
					&& !historyOBJ.get("pastObstetricHistory").isJsonNull()) {
				BenObstetricCancerHistory benObstetricCancerHistory = InputMapper.gson()
						.fromJson(historyOBJ.get("pastObstetricHistory"), BenObstetricCancerHistory.class);

				benObstetricCancerHistory.setBenVisitID(benVisitID);
				benObstetricSaveSuccessFlag = cSNurseServiceImpl
						.saveBenObstetricCancerHistory(benObstetricCancerHistory);
			} else {
				// ben obstetric history data not there !!!
				benObstetricSaveSuccessFlag = new Long(1);
			}

			if (benFamilyHistoryDataSavingSuccessFlag > 0
					&& (null != benPersonalHistorySaveSuccessFlag && benPersonalHistorySaveSuccessFlag > 0)
					&& (null != benPersonalDietHistorySaveSuccessFlag && benPersonalDietHistorySaveSuccessFlag > 0)
					&& (null != benObstetricSaveSuccessFlag && benObstetricSaveSuccessFlag > 0)) {
				csHistorySaveRes = benPersonalHistorySaveSuccessFlag;
			}

		} else {
			// History Object is not there in request object !!!
			csHistorySaveRes = new Long(1);
		}

		return csHistorySaveRes;
	}

	public Long saveBenFamilyHistoryDetails() {
		return null;
	}

	/**
	 * 
	 * @param requestOBJ
	 * @param benVisitID
	 * @return success or failure flag for vitals data saving
	 * @throws Exception
	 */
	public Long saveBenVitalsDetails(JsonObject requestOBJ, Long benVisitID) throws Exception {
		Long benVitalSaveSuccessFlag = null;
		if (requestOBJ != null && requestOBJ.has("vitalsDetails") && !requestOBJ.get("vitalsDetails").isJsonNull()) {
			BenCancerVitalDetail benCancerVitalDetail = InputMapper.gson().fromJson(requestOBJ.get("vitalsDetails"),
					BenCancerVitalDetail.class);
			if (benCancerVitalDetail != null) {
				benCancerVitalDetail.setBenVisitID(benVisitID);
				benVitalSaveSuccessFlag = cSNurseServiceImpl.saveBenVitalDetail(benCancerVitalDetail);
			}
		} else {
			// ben vitals data is not there !!!
			benVitalSaveSuccessFlag = new Long(1);
		}
		return benVitalSaveSuccessFlag;
	}

	/// -------End of Save/Create (Nurse)-----------------------------------

	// -------Update (Nurse data from Doctor screen)----------------------
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int UpdateCSHistoryNurseData(JsonObject jsnOBJ) throws Exception {

		int familyCURes = 0;
		int pastObstetricCURes = 0;
		int personalCURes = 0;
		int personalDietURes = 0;
		int historyUpdateRes = 0;

		if (null != jsnOBJ && jsnOBJ.has("familyHistory") && !jsnOBJ.get("familyHistory").isJsonNull()) {
			BenFamilyCancerHistory[] benFamilyCancerHistoryArray = InputMapper.gson()
					.fromJson(jsnOBJ.get("familyHistory"), BenFamilyCancerHistory[].class);
			List<BenFamilyCancerHistory> benFamilyCancerHistoryList = Arrays.asList(benFamilyCancerHistoryArray);

			if (benFamilyCancerHistoryList != null && benFamilyCancerHistoryList.size() > 0) {
				familyCURes = cSNurseServiceImpl.updateBeneficiaryFamilyCancerHistory(benFamilyCancerHistoryList);
			} else {
				familyCURes = 1;
			}
		} else {
			familyCURes = 1;
		}

		if (null != jsnOBJ && jsnOBJ.has("pastObstetricHistory") && !jsnOBJ.get("pastObstetricHistory").isJsonNull()) {

			BenObstetricCancerHistory benObstetricCancerHistory = InputMapper.gson()
					.fromJson(jsnOBJ.get("pastObstetricHistory"), BenObstetricCancerHistory.class);
			pastObstetricCURes = cSNurseServiceImpl.updateBenObstetricCancerHistory(benObstetricCancerHistory);
		} else {
			pastObstetricCURes = 1;
		}

		if (null != jsnOBJ && jsnOBJ.has("personalHistory") && !jsnOBJ.get("personalHistory").isJsonNull()) {

			BenPersonalCancerHistory benPersonalCancerHistory = InputMapper.gson()
					.fromJson(jsnOBJ.get("personalHistory"), BenPersonalCancerHistory.class);

			BenPersonalCancerDietHistory benPersonalCancerDietHistory = InputMapper.gson()
					.fromJson(jsnOBJ.get("personalHistory"), BenPersonalCancerDietHistory.class);

			personalCURes = cSNurseServiceImpl.updateBenPersonalCancerHistory(benPersonalCancerHistory);

			personalDietURes = cSNurseServiceImpl.updateBenPersonalCancerDietHistory(benPersonalCancerDietHistory);

		} else {
			personalCURes = 1;
			personalDietURes = 1;
		}

		if (familyCURes > 0 && pastObstetricCURes > 0 && personalCURes > 0 && personalDietURes > 0) {
			historyUpdateRes = 1;
		} else {
			// TODO Rollback the succeeded transactions
		}

		return historyUpdateRes;
	}

	@Transactional(rollbackFor = Exception.class)
	public int updateBenExaminationDetail(JsonObject jsnOBJ) throws Exception {
		int examinationUpdateRes = 0;
		int signSympSuccessFlag = 0;
		int lymphNodeSuccessFlag = 0;
		int oralDetailsSuccessFlag = 0;
		int breastExmnSuccessFlag = 0;
		int abdominalExmnSuccessFlag = 0;
		int gynecologicalExmnSuccessFlag = 0;
		int imgCoordinatesSuccessFlag = 0;

		if (null != jsnOBJ && jsnOBJ.has("signsDetails") && !jsnOBJ.get("signsDetails").isJsonNull()) {

			JsonObject signsDetailsOBJ = jsnOBJ.getAsJsonObject("signsDetails");

			WrapperCancerSymptoms wrapperCancerSymptoms = InputMapper.gson().fromJson(signsDetailsOBJ,
					WrapperCancerSymptoms.class);

			if (null != wrapperCancerSymptoms.getCancerSignAndSymptoms()) {
				int ID = cSNurseServiceImpl
						.updateSignAndSymptomsExaminationDetails(wrapperCancerSymptoms.getCancerSignAndSymptoms());
				if (ID > 0) {
					// SignAndSymptoms Details stored successfully..
					signSympSuccessFlag = ID;
				} else {
					//
				}
			} else {
				// signsDetails not available..
				signSympSuccessFlag = 1;
			}

			if (null != wrapperCancerSymptoms.getCancerLymphNodeDetails()) {
				int ID = cSNurseServiceImpl.updateLymphNodeExaminationDetails(wrapperCancerSymptoms);
				if (ID > 0) {
					// LymphNode Details stored successfully..
					lymphNodeSuccessFlag = ID;
				} else {

				} // Failed to store LymphNode Details..
			} else {
				// lymphNode not available..
				lymphNodeSuccessFlag = 1;
			}

		} else {
			signSympSuccessFlag = 1;
			lymphNodeSuccessFlag = 1;
		}

		if (jsnOBJ != null && jsnOBJ.has("oralDetails") && !jsnOBJ.get("oralDetails").isJsonNull()) {

			CancerOralExamination cancerOralExamination = InputMapper.gson().fromJson(jsnOBJ.get("oralDetails"),
					CancerOralExamination.class);
			int ID = cSNurseServiceImpl.updateCancerOralDetails(cancerOralExamination);
			if (ID > 0) {
				// oralDetails stored successfully...
				oralDetailsSuccessFlag = ID;
			} else {
				// Failed to store oralDetails..
			}

		} else {
			// oralDetails not available..
			oralDetailsSuccessFlag = 1;
		}

		if (jsnOBJ != null && jsnOBJ.has("breastDetails") && !jsnOBJ.get("breastDetails").isJsonNull()) {

			CancerBreastExamination cancerBreastExamination = InputMapper.gson().fromJson(jsnOBJ.get("breastDetails"),
					CancerBreastExamination.class);
			int ID = cSNurseServiceImpl.updateCancerBreastDetails(cancerBreastExamination);
			if (ID > 0) {
				// breastDetails stored successfully...
				breastExmnSuccessFlag = ID;
			} else {
				// Failed to store breastDetails..
			}

		} else {
			// breastDetails not available..
			breastExmnSuccessFlag = 1;
		}

		if (jsnOBJ != null && jsnOBJ.has("abdominalDetails") && !jsnOBJ.get("abdominalDetails").isJsonNull()) {

			CancerAbdominalExamination cancerAbdominalExamination = InputMapper.gson()
					.fromJson(jsnOBJ.get("abdominalDetails"), CancerAbdominalExamination.class);
			int ID = cSNurseServiceImpl.updateCancerAbdominalExaminationDetails(cancerAbdominalExamination);

			if (ID > 0) {
				// abdominalDetails stored successfully...
				abdominalExmnSuccessFlag = ID;
			} else {
				// Failed to store abdominalDetails..
			}

		} else {
			// abdominalDetails not available..
			abdominalExmnSuccessFlag = 1;
		}

		if (jsnOBJ != null && jsnOBJ.has("gynecologicalDetails") && !jsnOBJ.get("gynecologicalDetails").isJsonNull()) {

			CancerGynecologicalExamination cancerGynecologicalExamination = InputMapper.gson()
					.fromJson(jsnOBJ.get("gynecologicalDetails"), CancerGynecologicalExamination.class);

			int ID = cSNurseServiceImpl.updateCancerGynecologicalExaminationDetails(cancerGynecologicalExamination);
			if (ID > 0) {
				// gynecologicalDetails stored successfully...
				gynecologicalExmnSuccessFlag = ID;
			} else {
				// Failed to store gynecologicalDetails..
			}

		} else {
			// gynecologicalDetails not available..
			gynecologicalExmnSuccessFlag = 1;
		}

		if (jsnOBJ != null && jsnOBJ.has("imageCoordinates") && !jsnOBJ.get("imageCoordinates").isJsonNull()) {

			WrapperCancerExamImgAnotasn[] wrapperCancerExamImgAnotasn = InputMapper.gson()
					.fromJson(jsnOBJ.get("imageCoordinates"), WrapperCancerExamImgAnotasn[].class);

			List<WrapperCancerExamImgAnotasn> wrapperCancerExamImgAnotasnList = Arrays
					.asList(wrapperCancerExamImgAnotasn);
			if (null != wrapperCancerExamImgAnotasnList) {
				int r = cSNurseServiceImpl.updateCancerExamImgAnotasnDetails(
						cSNurseServiceImpl.getCancerExaminationImageAnnotationList(wrapperCancerExamImgAnotasnList));
				if (r > 0) {
					// imageCoordinates stored successfully...
					imgCoordinatesSuccessFlag = r;
				}
			} else {
				imgCoordinatesSuccessFlag = 1;
			}
		} else {
			// imageCoordinates not available..
			imgCoordinatesSuccessFlag = 1;
		}

		if (signSympSuccessFlag > 0 && lymphNodeSuccessFlag > 0 && oralDetailsSuccessFlag > 0
				&& breastExmnSuccessFlag > 0 && abdominalExmnSuccessFlag > 0 && gynecologicalExmnSuccessFlag > 0
				&& imgCoordinatesSuccessFlag > 0) {
			examinationUpdateRes = 1;
		} else {
			// TODO Rollback the succeeded transactions
		}

		return examinationUpdateRes;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int updateBenVitalDetail(BenCancerVitalDetail benCancerVitalDetail) {
		return cSNurseServiceImpl.updateBenVitalDetail(benCancerVitalDetail);
	}

	/// ------- End of Update (Nurse data from Doctor screen)-----------

	// ------- Fetch (Nurse data to Doctor screen) ----------------
	public String getBenDataFrmNurseToDocVisitDetailsScreen(Long benRegID, Long benVisitID) {
		Map<String, Object> resMap = new HashMap<>();
		BeneficiaryVisitDetail benVisitDetailsOBJ = commonNurseServiceImpl.getCSVisitDetails(benRegID, benVisitID);

		if (null != benVisitDetailsOBJ) {

			resMap.put("benVisitDetails", benVisitDetailsOBJ);
		}

		return new Gson().toJson(resMap);
	}

	public String getBenDataFrmNurseToDocHistoryScreen(Long benRegID, Long benVisitID) {
		Map<String, Object> resMap = new HashMap<>();
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.serializeNulls();
		Gson gson = gsonBuilder.create();

		resMap.put("benFamilyHistory", cSNurseServiceImpl.getBenFamilyHisData(benRegID, benVisitID));

		resMap.put("benObstetricHistory", cSNurseServiceImpl.getBenObstetricDetailsData(benRegID, benVisitID));

		resMap.put("benPersonalHistory", cSNurseServiceImpl.getBenPersonalCancerHistoryData(benRegID, benVisitID));

		resMap.put("benPersonalDietHistory",
				cSNurseServiceImpl.getBenPersonalCancerDietHistoryData(benRegID, benVisitID));

		return gson.toJson(resMap);
	}

	public String getBenDataFrmNurseToDocVitalScreen(Long benRegID, Long benVisitID) {
		Map<String, Object> resMap = new HashMap<>();
		resMap.put("benVitalDetails", cSNurseServiceImpl.getBenCancerVitalDetailData(benRegID, benVisitID));
		return new Gson().toJson(resMap);
	}

	public String getBenDataFrmNurseToDocExaminationScreen(Long benRegID, Long benVisitID) {
		Map<String, Object> resMap = new HashMap<>();
		// TODO

		resMap.put("abdominalExamination",
				cSNurseServiceImpl.getBenCancerAbdominalExaminationData(benRegID, benVisitID));

		resMap.put("breastExamination", cSNurseServiceImpl.getBenCancerBreastExaminationData(benRegID, benVisitID));

		resMap.put("gynecologicalExamination",
				cSNurseServiceImpl.getBenCancerGynecologicalExaminationData(benRegID, benVisitID));

		resMap.put("signsAndSymptoms", cSNurseServiceImpl.getBenCancerSignAndSymptomsData(benRegID, benVisitID));

		resMap.put("BenCancerLymphNodeDetails",
				cSNurseServiceImpl.getBenCancerLymphNodeDetailsData(benRegID, benVisitID));

		resMap.put("oralExamination", cSNurseServiceImpl.getBenCancerOralExaminationData(benRegID, benVisitID));

		resMap.put("imageCoordinates",
				cSNurseServiceImpl.getCancerExaminationImageAnnotationCasesheet(benRegID, benVisitID));

		return new Gson().toJson(resMap);
	}

	/// -------End of Fetch (Nurse data to Doctor screen) -------

	// -------Create/save (Doctor)---------------------------
	@Transactional(rollbackFor = Exception.class)
	public Long saveCancerScreeningDoctorData(JsonObject requestOBJ) throws Exception {
		Long docDataSuccessFlag = null;

		if (requestOBJ != null) {
			// Examination page moved to Nurse screen, So commenting : On
			// 26-02-18 by Navya
			// Long examinationSuccessFlag =
			// saveBenExaminationDetails(requestOBJ);

			Long diagnosisSuccessFlag = saveBenDiagnosisDetails(requestOBJ);

			/*
			 * 
			 * if ((examinationSuccessFlag != null && examinationSuccessFlag >
			 * 0) && (diagnosisSuccessFlag != null && diagnosisSuccessFlag > 0))
			 * {
			 * 
			 * String s =
			 * commonNurseServiceImpl.updateBenStatus(getBenVisitID(requestOBJ),
			 * "D"); if (s != null && s.length() > 0) docDataSuccessFlag =
			 * examinationSuccessFlag; }
			 */
			if (diagnosisSuccessFlag != null && diagnosisSuccessFlag > 0) {

				Long tmpBenFlowID = null;
				Long tmpbeneficiaryRegID = null;
				Long tmpBeneficiaryID = null;
				Long tmpBenVisitID = null;

				if (requestOBJ.getAsJsonObject("diagnosis") != null
						&& !requestOBJ.getAsJsonObject("diagnosis").isJsonNull()) {
					tmpBenFlowID = requestOBJ.getAsJsonObject("diagnosis").get("benFlowID").getAsLong();
					tmpbeneficiaryRegID = requestOBJ.getAsJsonObject("diagnosis").get("beneficiaryRegID").getAsLong();
					tmpBeneficiaryID = requestOBJ.getAsJsonObject("diagnosis").get("beneficiaryID").getAsLong();
					tmpBenVisitID = requestOBJ.getAsJsonObject("diagnosis").get("benVisitID").getAsLong();
				}

				short docFlag = (short) 9;
				short pharmaFalg = (short) 0;
				short oncologistFlag = (short) 1;

				int l = commonBenStatusFlowServiceImpl.updateBenFlowAfterDocData(tmpBenFlowID, tmpbeneficiaryRegID,
						tmpBeneficiaryID, tmpBenVisitID, docFlag, pharmaFalg, oncologistFlag);

				// String s =
				// commonNurseServiceImpl.updateBenStatus(getBenVisitID(requestOBJ),
				// "D");
				// if (s != null && s.length() > 0)
				docDataSuccessFlag = diagnosisSuccessFlag;
			}
		} else {
			// NO input available..
		}
		return docDataSuccessFlag;

	}

	private Long getBenVisitID(JsonObject requestOBJ) throws Exception {
		Long benVisitID = null;
		if (requestOBJ != null && requestOBJ.has("diagnosis") && !requestOBJ.get("diagnosis").isJsonNull()) {

			// JsonObject signsDetailsOBJ =
			// requestOBJ.getAsJsonObject("signsDetails");
			//
			// WrapperCancerSymptoms wrapperCancerSymptoms =
			// InputMapper.gson().fromJson(signsDetailsOBJ,
			// WrapperCancerSymptoms.class);

			JsonObject tmpOBJ = requestOBJ.get("diagnosis").getAsJsonObject();
			if (tmpOBJ != null && tmpOBJ.has("benVisitID") && !tmpOBJ.get("benVisitID").isJsonNull()) {
				benVisitID = tmpOBJ.get("benVisitID").getAsLong();
			}
		}

		return benVisitID;
	}

	/**
	 * 
	 * @param requestOBJ
	 * @return success or failure flag for Examination data saving
	 */

	// Examination page moved to Nurse screen, So passing visitId when calling
	// from saveNurseData: On 26-02-18 by Navya
	public Long saveBenExaminationDetails(JsonObject requestOBJ, Long benVisitID, String Authorization)
			throws Exception {
		Long signSympSuccessFlag = null;
		Long lymphNodeSuccessFlag = null;
		Long oralDetailsSuccessFlag = null;
		Long breastExmnSuccessFlag = null;
		Long abdominalExmnSuccessFlag = null;
		Long gynecologicalExmnSuccessFlag = null;
		Long imgCoordinatesSuccessFlag = null;

		Long exmnSuccessFlag = null;

		if (requestOBJ != null && requestOBJ.has("examinationDetails")
				&& !requestOBJ.get("examinationDetails").isJsonNull()) {
			JsonObject examinationOBJ = requestOBJ.getAsJsonObject("examinationDetails");
			if (examinationOBJ != null && examinationOBJ.has("signsDetails")
					&& !examinationOBJ.get("signsDetails").isJsonNull()) {

				JsonObject signsDetailsOBJ = examinationOBJ.getAsJsonObject("signsDetails");

				WrapperCancerSymptoms wrapperCancerSymptoms = InputMapper.gson().fromJson(signsDetailsOBJ,
						WrapperCancerSymptoms.class);

				if (null != wrapperCancerSymptoms.getCancerSignAndSymptoms()) {
					Long ID = cSNurseServiceImpl.saveCancerSignAndSymptomsData(
							wrapperCancerSymptoms.getCancerSignAndSymptoms(), benVisitID);
					if (ID > 0 && ID != null) {
						// SignAndSymptoms Details stored successfully..
						signSympSuccessFlag = ID;
					} else {
						// Failed to store signsDetails..
					}
				} else {
					// signsDetails not available..
					signSympSuccessFlag = new Long(1);
				}

				if (null != wrapperCancerSymptoms.getCancerLymphNodeDetails()
						&& wrapperCancerSymptoms.getCancerLymphNodeDetails().size() > 0) {
					Long ID = cSNurseServiceImpl.saveLymphNodeDetails(wrapperCancerSymptoms.getCancerLymphNodeDetails(),
							benVisitID);
					if (null != ID && ID > 0) {
						// LymphNode Details stored successfully..
						lymphNodeSuccessFlag = ID;
					} else {

					} // Failed to store LymphNode Details..
				} else {
					// lymphNode not available..
					lymphNodeSuccessFlag = new Long(1);
				}

			} else {
				signSympSuccessFlag = new Long(1);
				lymphNodeSuccessFlag = new Long(1);
			}

			if (examinationOBJ != null && examinationOBJ.has("oralDetails")
					&& !examinationOBJ.get("oralDetails").isJsonNull()) {

				CancerOralExamination cancerOralExamination = InputMapper.gson()
						.fromJson(examinationOBJ.get("oralDetails"), CancerOralExamination.class);
				cancerOralExamination.setBenVisitID(benVisitID);
				Long ID = cSNurseServiceImpl.saveCancerOralExaminationData(cancerOralExamination);
				if (ID != null && ID > 0) {
					// oralDetails stored successfully...
					oralDetailsSuccessFlag = ID;
				} else {
					// Failed to store oralDetails..
				}

			} else {
				// oralDetails not available..
				oralDetailsSuccessFlag = new Long(1);
			}

			if (examinationOBJ != null && examinationOBJ.has("breastDetails")
					&& !examinationOBJ.get("breastDetails").isJsonNull()) {

				CancerBreastExamination cancerBreastExamination = InputMapper.gson()
						.fromJson(examinationOBJ.get("breastDetails"), CancerBreastExamination.class);
				cancerBreastExamination.setBenVisitID(benVisitID);
				Long ID = cSNurseServiceImpl.saveCancerBreastExaminationData(cancerBreastExamination);
				if (ID != null && ID > 0) {
					// breastDetails stored successfully...

					// New code for care stream... 16-03-2018
					if (cancerBreastExamination.getReferredToMammogram() != null
							&& cancerBreastExamination.getReferredToMammogram() == true) {

						int r = createCareStreamOrder(cancerBreastExamination.getBeneficiaryRegID(),
								cancerBreastExamination.getBenVisitID(), Authorization);
						if (r > 0) {
							breastExmnSuccessFlag = Long.valueOf(2);
						} else {
							breastExmnSuccessFlag = Long.valueOf(3);
						}
					} else {
						breastExmnSuccessFlag = Long.valueOf(1);
					}
					// End of New code for care stream... 16-03-2018
				} else {
					// Failed to store breastDetails..
				}

			} else {
				// breastDetails not available..
				breastExmnSuccessFlag = new Long(1);
			}

			if (examinationOBJ != null && examinationOBJ.has("abdominalDetails")
					&& !examinationOBJ.get("abdominalDetails").isJsonNull()) {

				CancerAbdominalExamination cancerAbdominalExamination = InputMapper.gson()
						.fromJson(examinationOBJ.get("abdominalDetails"), CancerAbdominalExamination.class);
				cancerAbdominalExamination.setBenVisitID(benVisitID);
				Long ID = cSNurseServiceImpl.saveCancerAbdominalExaminationData(cancerAbdominalExamination);
				if (ID != null && ID > 0) {
					// abdominalDetails stored successfully...
					abdominalExmnSuccessFlag = ID;
				} else {
					// Failed to store abdominalDetails..
				}

			} else {
				// abdominalDetails not available..
				abdominalExmnSuccessFlag = new Long(1);
			}

			if (examinationOBJ != null && examinationOBJ.has("gynecologicalDetails")
					&& !examinationOBJ.get("gynecologicalDetails").isJsonNull()) {

				CancerGynecologicalExamination cancerGynecologicalExamination = InputMapper.gson()
						.fromJson(examinationOBJ.get("gynecologicalDetails"), CancerGynecologicalExamination.class);
				cancerGynecologicalExamination.setBenVisitID(benVisitID);
				Long ID = cSNurseServiceImpl.saveCancerGynecologicalExaminationData(cancerGynecologicalExamination);
				if (ID != null && ID > 0) {
					// gynecologicalDetails stored successfully...
					gynecologicalExmnSuccessFlag = ID;
				} else {
					// Failed to store gynecologicalDetails..
				}

			} else {
				// gynecologicalDetails not available..
				gynecologicalExmnSuccessFlag = new Long(1);
			}

			if (examinationOBJ != null && examinationOBJ.has("imageCoordinates")
					&& !examinationOBJ.get("imageCoordinates").isJsonNull()) {

				WrapperCancerExamImgAnotasn[] wrapperCancerExamImgAnotasn = InputMapper.gson()
						.fromJson(examinationOBJ.get("imageCoordinates"), WrapperCancerExamImgAnotasn[].class);

				List<WrapperCancerExamImgAnotasn> wrapperCancerExamImgAnotasnList = Arrays
						.asList(wrapperCancerExamImgAnotasn);
				if (null != wrapperCancerExamImgAnotasnList && wrapperCancerExamImgAnotasnList.size() > 0) {
					Long r = cSNurseServiceImpl.saveDocExaminationImageAnnotation(wrapperCancerExamImgAnotasnList,
							benVisitID);
					if (r != null && r > 0) {
						// imageCoordinates stored successfully...
						imgCoordinatesSuccessFlag = r;
					} else {
						// Failed to store image coordinates..
					}
				} else {
					imgCoordinatesSuccessFlag = new Long(1);
				}
			} else {
				// imageCoordinates not available..
				imgCoordinatesSuccessFlag = new Long(1);
			}

			if (null != signSympSuccessFlag && signSympSuccessFlag > 0 && null != lymphNodeSuccessFlag
					&& lymphNodeSuccessFlag > 0 && null != oralDetailsSuccessFlag && oralDetailsSuccessFlag > 0
					&& null != breastExmnSuccessFlag && breastExmnSuccessFlag > 0 && null != abdominalExmnSuccessFlag
					&& abdominalExmnSuccessFlag > 0 && null != gynecologicalExmnSuccessFlag
					&& gynecologicalExmnSuccessFlag > 0 && null != imgCoordinatesSuccessFlag
					&& imgCoordinatesSuccessFlag > 0) {

				exmnSuccessFlag = breastExmnSuccessFlag;

			} else {
				// TODO Rollback
			}

		} else {
			exmnSuccessFlag = new Long(1);
		}

		return exmnSuccessFlag;
	}

	/**
	 * 
	 * @param requestOBJ
	 * @return success or failure flag for Diagnosis data saving
	 */
	public Long saveBenDiagnosisDetails(JsonObject requestOBJ) throws Exception {

		Long diagnosisSuccessFlag = null;
		if (requestOBJ != null && requestOBJ.has("diagnosis") && !requestOBJ.get("diagnosis").isJsonNull()) {
			CancerDiagnosis cancerDiagnosis = InputMapper.gson().fromJson(requestOBJ.get("diagnosis"),
					CancerDiagnosis.class);
			Long ID = cSDoctorServiceImpl.saveCancerDiagnosisData(cancerDiagnosis);
			if (ID != null && ID > 0) {
				// diagnosis details stored successfully...
				diagnosisSuccessFlag = ID;
			} else {
				// Failed to store diagnosis details..
			}
		} else {
			// diagnosis details not available..
			diagnosisSuccessFlag = new Long(1);
		}

		return diagnosisSuccessFlag;
	}

	/// ------- End of Create/save (Doctor)---------------------------

	// -------Fetch Case-sheet data ----------------------------------

	public String getCancerCasesheetData(JSONObject obj, String Authorization) throws Exception {
		String caseSheetData = null;
		if (obj.length() > 1) {
			Long benRegID = null;
			Long benVisitID = null;
			Long benFlowID = null;
			try {
				benRegID = obj.getLong("benRegID");
				benVisitID = obj.getLong("benVisitID");
				benFlowID = obj.getLong("benFlowID");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			caseSheetData = getBenDataForCaseSheet(benFlowID, benRegID, benVisitID, Authorization);

		} else {

		}

		return caseSheetData;
	}

	public String getBenDataForCaseSheet(Long benFlowID, Long benRegID, Long benVisitID, String Authorization)
			throws Exception {

		Map<String, Object> caseSheetData = cSNurseServiceImpl.getBenNurseDataForCaseSheet(benRegID, benVisitID);
		// getBenDetailsByBenRegID(benRegID, Authorization);
		caseSheetData.putAll(cSDoctorServiceImpl.getBenDoctorEnteredDataForCaseSheet(benRegID, benVisitID));

		caseSheetData.put("BeneficiaryData", getBenDetails(benFlowID, benRegID));
		caseSheetData.put("ImageAnnotatedData",
				cSNurseServiceImpl.getCancerExaminationImageAnnotationCasesheet(benRegID, benVisitID));

		return new Gson().toJson(caseSheetData);
	}

	//Same method we have copied to commonServices, in future we can remove this from here..
	private BeneficiaryFlowStatus getBenDetails(Long benFlowID, Long benRegID) {
		ArrayList<Object[]> tmpOBJ = beneficiaryFlowStatusRepo.getBenDetailsForLeftSidePanel(benRegID, benFlowID);
		BeneficiaryFlowStatus obj = BeneficiaryFlowStatus.getBeneficiaryFlowStatusForLeftPanel(tmpOBJ);
		return obj;
	}

	// method not required
	@Deprecated
	private void getBenDetailsByBenRegID(Long benRegID, String Authorization) throws Exception {
		String s = null;
		Map<String, Object> requestMap = new HashMap<>();
		requestMap.put("beneficiaryRegID", benRegID);
		String requestObj = new Gson().toJson(requestMap);

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		headers.add("Content-Type", "application/json");
		headers.add("AUTHORIZATION", Authorization);

		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Object> request = new HttpEntity<Object>(requestObj, headers);
		ResponseEntity<String> response = restTemplate.exchange(registrarQuickSearchByIdUrl, HttpMethod.POST, request,
				String.class);
		if (response.hasBody()) {
			s = response.getBody();
		}

		BeneficiaryFlowStatus obj = InputMapper.gson().fromJson(s, BeneficiaryFlowStatus.class);
		System.out.println("hello");
	}

	/// -------End of Fetch Case-sheet data ----------------------------------

	// -------Fetch beneficiary all past family history data------------------
	public String getBenFamilyHistoryData(Long beneficiaryRegID) {
		return cSNurseServiceImpl.getBenCancerFamilyHistory(beneficiaryRegID);
	}
	/// ------- End of Fetch beneficiary all past family history data----------

	// -------Fetch beneficiary all past personal history data------------------
	public String getBenPersonalHistoryData(Long beneficiaryRegID) {
		return cSNurseServiceImpl.getBenCancerPersonalHistory(beneficiaryRegID);
	}
	/// ------- End of Fetch beneficiary all past personal history data--------

	// -------Fetch beneficiary all past personal diet history data-----------
	public String getBenPersonalDietHistoryData(Long beneficiaryRegID) {
		return cSNurseServiceImpl.getBenCancerPersonalDietHistory(beneficiaryRegID);
	}
	/// ------- End of Fetch beneficiary all past personal diet history data--

	// -------Fetch beneficiary all past obstetric history data---------------
	public String getBenObstetricHistoryData(Long beneficiaryRegID) {
		return cSNurseServiceImpl.getBenCancerObstetricHistory(beneficiaryRegID);
	}
	/// ------- End of Fetch beneficiary all past obstetric history data------

	@Override
	public int updateCancerDiagnosisDetailsByOncologist(CancerDiagnosis cancerDiagnosis) {
		return csOncologistServiceImpl.updateCancerDiagnosisDetailsByOncologist(cancerDiagnosis);

	}

	private int createCareStreamOrder(long benRegID, long benVisitID, String Authorization) {
		ArrayList<Object[]> benDataForCareStream = registrarRepoBenData.getBenDataForCareStream(benRegID);

		int r = cSCarestreamServiceImpl.createMamographyRequest(benDataForCareStream, benRegID, benVisitID,
				Authorization);
		return r;
	}

	// Fetch CS Doctor Details START....
	public String getBenDoctorDiagnosisData(Long benRegID, Long benVisitID) {
		Map<String, Object> resMap = new HashMap<>();
		resMap.put("benDiagnosisDetails", cSDoctorServiceImpl.getBenCancerDiagnosisData(benRegID, benVisitID));
		return new Gson().toJson(resMap);
	}
	// Fetch CS Doctor Details END....

	public String getBenCaseRecordFromDoctorCS(Long benRegID, Long benVisitID) {
		Map<String, Object> resMap = new HashMap<>();

		resMap.put("diagnosis", cSDoctorServiceImpl.getBenCancerDiagnosisData(benRegID, benVisitID));

		return new Gson().toJson(resMap);
	}

	@Transactional(rollbackFor = Exception.class)
	public Long updateCancerScreeningDoctorData(JsonObject requestOBJ) throws Exception {
		Long updateSuccessFlag = null;
		int diagnosisSuccessFlag = 0;
		if (requestOBJ != null) {

			if (requestOBJ.has("diagnosis") && !requestOBJ.get("diagnosis").isJsonNull()) {
				CancerDiagnosis cancerDiagnosis = InputMapper.gson().fromJson(requestOBJ.get("diagnosis"),
						CancerDiagnosis.class);
				diagnosisSuccessFlag = cSDoctorServiceImpl.updateCancerDiagnosisDetailsByDoctor(cancerDiagnosis);
			} else {
				diagnosisSuccessFlag = 1;
			}
		} else {
			// request OBJ is null.
		}
		return updateSuccessFlag;
	}

}
