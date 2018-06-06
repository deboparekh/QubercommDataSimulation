package com.com.semaifour.facesix.data.device;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;

import com.semaifour.facesix.domain.FSObject;

public class ClientDevice extends FSObject implements Serializable, Comparable<ClientDevice> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger LOG = LoggerFactory.getLogger(ClientDeviceService.class.getName());

	@Id
	public String devname;

	public String ip;
	public String radio;
	public String ap;
	public String rssi;
	public String conn;

	public ClientDevice() {
		super();
	}

	public String getDevname() {
		return devname;
	}

	public void setDevname(String devname) {
		this.devname = devname;
	}

	public String getUid() {
		return super.getUid();
	}

	public void setUid(String uid) {
		super.setUid(uid);
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setStatus(String status) {
		super.setStatus(status);
	}

	public String getStatus() {
		return super.getStatus();
	}

	public void setTypefs(String type) {
		super.setTypefs(type);
	}

	public String getTypefs() {
		return super.getTypefs();
	}

	public String getAP() {
		return ap;
	}

	public void setAP(String ap) {
		this.ap = ap;
	}

	public String getRadio() {
		return radio;
	}

	public void setRadio(String radio) {
		this.radio = radio;
	}

	public String getRssi() {
		return rssi;
	}

	public void setRssi(String rssi) {
		this.rssi = rssi;
	}

	public String getConn() {
		return conn;
	}

	public void setConn(String conn) {
		this.conn = conn;
	}

	public void update(ClientDevice newfso) {
		super.update(newfso);
		this.devname = newfso.getDevname();
		this.ip = newfso.getIp();
		this.ap = newfso.getAP();
		this.radio = newfso.getRadio();
		this.rssi = newfso.getRssi();
		this.conn = newfso.getConn();
	}

	@Override
	public String toString() {
		return "ClientDevice [devname=" + devname + ", ip=" + ip + ", ap=" + ap + ", radio=" + radio + ", rssi=" + rssi
				+ ", conn=" + conn + "dev_uid=" + super.getUid() + ", cli_status=" + super.getStatus() + ", cli_type="
				+ super.getTypefs() + ", toString()=" + super.toString() + "]";
	}

	@Override
	public int compareTo(ClientDevice arg0) {
		String uid = super.getUid();
		int str = uid.compareTo(arg0.getUid());
		return str;
	}
}
