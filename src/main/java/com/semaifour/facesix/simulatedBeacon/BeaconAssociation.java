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
	private String 	x;
	private String 	y;
	private int 	width;
	private int 	height;
	
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

	public String getX() {
		return x;
	}

	public void setX(String x) {
		this.x = x;
	}

	public String getY() {
		return y;
	}

	public void setY(String y) {
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public String toString() {
		return "BeaconAssociation [id=" + id + ", macaddr=" + macaddr + ", cid=" + cid + ", sid=" + sid + ", spid="
				+ spid + ", uid=" + uid + ", lat=" + lat + ", lon=" + lon + ", x=" + x + ", y=" + y + ", width=" + width
				+ ", height=" + height + "]";
	}
}
