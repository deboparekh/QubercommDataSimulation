package com.com.semaifour.facesix.data.device;

import java.util.Date;

import org.springframework.data.annotation.Id;

import com.semaifour.facesix.domain.FSObject;

public class Device extends FSObject {

	public enum STATUS {
		REGISTERED, CONFIGURED, AUTOCONFIGURED
	}

	public enum STATE {
		inactive, active, idle
	}

	@Id
	private String id;
	private String conf;
	private String template;
	private String state;
	private String vap2gcount;
	private String vap5gcount;
	private long ver;

	public Device() {
		super();
	}

	public Device(String UID, String name, String conf, String version, String createdBy, String modifiedBy,
			Date createdOn, Date modifiedOn) {
		super();
		setUid(UID);
		setName(name);
		setConf(conf);
		setVersion(version);
		setCreatedBy(createdBy);
		setModifiedBy(modifiedBy);
		setCreatedOn(createdOn);
		setModifiedOn(modifiedOn);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getVer() {
		return ver;
	}

	public void setVer(long ver) {
		this.ver = ver;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getVap2GCount() {
		return vap2gcount;
	}

	public void setVap2GCount(String vap2gcount) {
		this.vap2gcount = vap2gcount;
	}

	public String getVap5GCount() {
		return vap5gcount;
	}

	public void setVap5GCount(String vap5gcount) {
		this.vap5gcount = vap5gcount;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getConf() {
		return conf;
	}

	public void setConf(String conf) {
		this.conf = conf;
	}

	@Override
	public String toString() {
		return "Device [id=" + id + ", conf=" + conf + ", template=" + template + ", state=" + state + ", vap2gcount="
				+ vap2gcount + ", vap5gcount=" + vap5gcount + ", ver=" + ver + ", toString()=" + super.toString() + "]";
	}

}
