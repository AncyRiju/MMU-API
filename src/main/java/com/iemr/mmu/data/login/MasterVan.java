package com.iemr.mmu.data.login;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.google.gson.annotations.Expose;

@Entity
@Table(name = "m_van")
public class MasterVan {
	@Id
	@GeneratedValue
	@Expose
	@Column(name = "VanID")
	private Integer vanID;
	@Expose
	@Column(name = "VehicalNo")
	private String vehicalNo;
	@Expose
	@Column(name = "ProviderServiceMapID")
	private Integer providerServiceMapID;
	@Expose
	@Column(name = "ParkingPlaceID")
	private Integer parkingPlaceID;
	@Expose
	@Column(name = "Deleted")
	private Boolean deleted;
	@Expose
	@Column(name = "IsFacility")
	private Boolean isFacility;
	@Expose
	@Column(name = "FacilityID")
	private Integer facilityID;

	@ManyToOne
	@JoinColumn(name = "ParkingPlaceID", insertable = false, updatable = false)
	private UserParkingplaceMapping userParkingplaceMapping;

	public MasterVan() {
	}

	public MasterVan(Integer vanID, String vehicalNo, Integer providerServiceMapID, Integer parkingPlaceID,
			Boolean deleted, UserParkingplaceMapping userParkingplaceMapping) {
		super();
		this.vanID = vanID;
		this.vehicalNo = vehicalNo;
		this.providerServiceMapID = providerServiceMapID;
		this.parkingPlaceID = parkingPlaceID;
		this.deleted = deleted;
		this.userParkingplaceMapping = userParkingplaceMapping;
	}

	public Boolean getIsFacility() {
		return isFacility;
	}

	public void setIsFacility(Boolean isFacility) {
		this.isFacility = isFacility;
	}

	public Integer getFacilityID() {
		return facilityID;
	}

	public void setFacilityID(Integer facilityID) {
		this.facilityID = facilityID;
	}

	public Integer getVanID() {
		return vanID;
	}

	public void setVanID(Integer vanID) {
		this.vanID = vanID;
	}

	public String getVehicalNo() {
		return vehicalNo;
	}

	public void setVehicalNo(String vehicalNo) {
		this.vehicalNo = vehicalNo;
	}

	public Integer getProviderServiceMapID() {
		return providerServiceMapID;
	}

	public void setProviderServiceMapID(Integer providerServiceMapID) {
		this.providerServiceMapID = providerServiceMapID;
	}

	public Integer getParkingPlaceID() {
		return parkingPlaceID;
	}

	public void setParkingPlaceID(Integer parkingPlaceID) {
		this.parkingPlaceID = parkingPlaceID;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public UserParkingplaceMapping getUserParkingplaceMapping() {
		return userParkingplaceMapping;
	}

	public void setUserParkingplaceMapping(UserParkingplaceMapping userParkingplaceMapping) {
		this.userParkingplaceMapping = userParkingplaceMapping;
	}

}
