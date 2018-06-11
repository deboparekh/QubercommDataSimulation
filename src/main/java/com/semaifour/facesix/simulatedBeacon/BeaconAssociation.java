package com.semaifour.facesix.simulatedBeacon;

import org.springframework.data.annotation.Id;

public class BeaconAssociation {
	@Id
	private String id;
	private String macaddr;
	private String cid;
	private String sid;
	private String spid;
	private String uid;
	private String lat;
	private String lon;
	
	public String getId() {
		return id;
	}

	public String getMacaddr() {
		return macaddr;
	}

	public void setMacaddr(String macaddr) {
		this.macaddr = macaddr;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getSpid() {
		return spid;
	}

	public void setSpid(String spid) {
		this.spid = spid;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLon() {
		return lon;
	}

	public void setLon(String lon) {
		this.lon = lon;
	}

	@Override
	public String toString() {
		return "BeaconAssociation [id=" + id + ", macaddr=" + macaddr + ", cid=" + cid + ", sid=" + sid + ", spid="
				+ spid + ", uid=" + uid + ", lat=" + lat + ", lon=" + lon + "]";
	}
}
