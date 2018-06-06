package com.com.semaifour.facesix.data.device;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;

import com.semaifour.facesix.domain.FSObject;

public class NetworkDevice extends FSObject implements Serializable, Comparable<NetworkDevice> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger LOG = LoggerFactory.getLogger(NetworkDeviceService.class.getName());

	@Id
	public String id;

	public String parent;
	public String sid;
	public String spid;
	public String svid;
	public String swid;
	public String xposition;
	public String yposition;
	public String gparent;
	public String uuid;

	public NetworkDevice() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUid() {
		return super.getUid();
	}

	public void setUid(String uid) {
		super.setUid(uid);
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
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

	public String getGParent() {
		return gparent;
	}

	public void setGParent(String gparent) {
		this.gparent = gparent;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
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

	public String getSvid() {
		return svid;
	}

	public void setSvid(String svid) {
		this.svid = svid;
	}

	public String getSwid() {
		return swid;
	}

	public void setSwid(String swid) {
		this.swid = swid;
	}

	public String getXposition() {
		return xposition;
	}

	public void setXposition(String xposition) {
		this.xposition = xposition;
	}

	public String getYposition() {
		return yposition;
	}

	public void setYposition(String yposition) {
		this.yposition = yposition;
	}

	public void update(NetworkDevice newfso) {
		super.update(newfso);
		this.parent = newfso.getParent();
		this.gparent = newfso.getGParent();
		this.sid = newfso.getSid();
		this.spid = newfso.getSpid();
		this.svid = newfso.getSvid();
		this.swid = newfso.getSwid();
		this.uuid = newfso.getUuid();
		this.xposition = newfso.getXposition();
		this.yposition = newfso.getYposition();
	}

	@Override
	public String toString() {
		return "NetworkDevice [id=" + id + ", parent=" + parent + ", gparent=" + gparent + ", sid=" + sid + ", spid="
				+ spid + ", svid=" + svid + ", swid=" + swid + ", uuid=" + uuid + ", xposition=" + xposition
				+ ", yposition=" + yposition + ", dev_uid=" + super.getUid() + ", dev_status=" + super.getStatus()
				+ ", dev_type=" + super.getTypefs() + ", toString()=" + super.toString() + "]";
	}

	@Override
	public int compareTo(NetworkDevice arg0) {
		int str = spid.compareTo(arg0.spid);
		return str;
	}
}
