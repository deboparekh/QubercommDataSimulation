package com.semaifour.facesix.data.elasticsearch.device;

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
	public String id;

	public String devname;

	public String ip;
	public String radio;
	public String ap;
	public String rssi;
	public String conn;
	public String mac;
	public String pid;
	public String acl;
	public String sid;
	public String spid;
	private String cid;
	public String peermac;
	public String tx;
	public String rx;
	public String ssid;

	public ClientDevice() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getAcl() {
		return acl;
	}

	public void setAcl(String acl) {
		this.acl = acl;
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

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getPeermac() {
		return peermac;
	}

	public void setPeermac(String peermac) {
		this.peermac = peermac;
	}

	public String getTx() {
		return tx;
	}

	public void setTx(String tx) {
		this.tx = tx;
	}

	public String getRx() {
		return rx;
	}

	public void setRx(String rx) {
		this.rx = rx;
	}

	public String getSsid() {
		return ssid;
	}

	public void setSsid(String ssid) {
		this.ssid = ssid;
	}

	public void update(ClientDevice newfso) {
		super.update(newfso);
		this.devname = newfso.getDevname();
		this.ip = newfso.getIp();
		this.ap = newfso.getAP();
		this.radio = newfso.getRadio();
		this.rssi = newfso.getRssi();
		this.conn = newfso.getConn();
		this.mac = newfso.getMac();
		this.pid = newfso.getPid();
		this.acl = newfso.getAcl();
		this.cid = newfso.getCid();
		this.sid = newfso.getSid();
		this.spid = newfso.getSpid();
		this.peermac = newfso.getPeermac();
	}

	@Override
	public String toString() {
		return "ClientDevice [id=" + id + ", devname=" + devname + ", ip=" + ip + ", radio=" + radio + ", ap=" + ap
				+ ", rssi=" + rssi + ", conn=" + conn + ", mac=" + mac + ", pid=" + pid + ", acl=" + acl + ", sid="
				+ sid + ", spid=" + spid + ", cid=" + cid + ", peermac=" + peermac + ", tx=" + tx + ", rx=" + rx
				+ ", ssid=" + ssid + "]";
	}

	@Override
	public int compareTo(ClientDevice arg0) {
		int str = pid.compareTo(arg0.pid);
		return str;
	}

}
