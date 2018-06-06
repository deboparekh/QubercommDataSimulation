package com.semaifour.facesix.data.elasticsearch.device;

import java.util.Collection;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.json.simple.JSONObject;

@Document(indexName = "fsi-device-#{systemProperties['fs.app'] ?: 'default'}", type = "device")
public class Device {

	public enum STATUS {
		REGISTERED, CONFIGURED, AUTOCONFIGURED
	}

	public enum STATE {
		inactive, active, idle
	}

	@Id
	private String id;
	private String pkid;
	private String uid;
	private String name;
	private String conf;
	private String fstype;
	private String createdBy;
	private String modifiedBy;
	private Date createdOn;
	private Date modifiedOn;
	private int score;
	private long ver;
	private String status;
	private String template;
	private String state;
	private String vap2gcount;
	private String vap5gcount;
	private int peercount;
	private int peer5gcount;
	private int peer2gcount;
	private int android;
	private int ios;
	private int windows;
	private int speaker;
	private int printer;
	private int others;
	private int blkcount;
	public String sid;
	public String spid;
	private String cid;
	private String alias;
	private String ip;
	private String role;
	public String steering;
	public String keepAliveInterval;
	public String root;
	public String lastseen;
	private String customizeInactivityMailSent;
	private String customizekeepAliveflag;
	private String devCrashTimestamp;
	private String devCrashdumpFileName;
	private String devCrashDumpUploadStatus;

	/*
	 * band balance
	 * 
	 */

	public String bssStationRejectThresh;
	public String bssChanPbusyPercent;
	public String bssRejectTimeout;
	public String band2GStaRatio;
	public String band5GStaRatio;
	public String bandRcpiDiff;

	/*
	 * Load Balance
	 * 
	 */

	public String minStaCount;
	public String rssiThreshold;
	public String rcpiRange;
	public String avgStaCntLb;
	public String loadBalance;
	public String workingMode;

	@Field(type = FieldType.Nested)
	private Collection<String> tags;

	private JSONObject steerClientList;
	public String statconnectedClientsList;
	public String peerList;

	public Device() {
		super();
	}

	public Device(String UID, String name, String conf, String version, String createdBy, String modifiedBy,
			Date createdOn, Date modifiedOn) {
		super();
		this.uid = UID;
		this.name = name;
		this.conf = conf;
		this.createdBy = createdBy;
		this.modifiedBy = modifiedBy;
		this.createdOn = createdOn;
		this.modifiedOn = modifiedOn;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPkid() {
		return pkid;
	}

	public void setPkid(String pkid) {
		this.pkid = pkid;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getConf() {
		return conf;
	}

	public void setConf(String conf) {
		this.conf = conf;
	}

	public String getFstype() {
		return fstype;
	}

	public void setFstype(String fstype) {
		this.fstype = fstype;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Date getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(Date modifiedOn) {
		this.modifiedOn = modifiedOn;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public long getVer() {
		return ver;
	}

	public void setVer(long ver) {
		this.ver = ver;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getVap2gcount() {
		return vap2gcount;
	}

	public void setVap2gcount(String vap2gcount) {
		this.vap2gcount = vap2gcount;
	}

	public String getVap5gcount() {
		return vap5gcount;
	}

	public void setVap5gcount(String vap5gcount) {
		this.vap5gcount = vap5gcount;
	}

	public int getPeercount() {
		return peercount;
	}

	public void setPeercount(int peercount) {
		this.peercount = peercount;
	}

	public int getPeer5gcount() {
		return peer5gcount;
	}

	public void setPeer5gcount(int peer5gcount) {
		this.peer5gcount = peer5gcount;
	}

	public int getPeer2gcount() {
		return peer2gcount;
	}

	public void setPeer2gcount(int peer2gcount) {
		this.peer2gcount = peer2gcount;
	}

	public int getAndroid() {
		return android;
	}

	public void setAndroid(int android) {
		this.android = android;
	}

	public int getIos() {
		return ios;
	}

	public void setIos(int ios) {
		this.ios = ios;
	}

	public int getWindows() {
		return windows;
	}

	public void setWindows(int windows) {
		this.windows = windows;
	}

	public int getSpeaker() {
		return speaker;
	}

	public void setSpeaker(int speaker) {
		this.speaker = speaker;
	}

	public int getPrinter() {
		return printer;
	}

	public void setPrinter(int printer) {
		this.printer = printer;
	}

	public int getOthers() {
		return others;
	}

	public void setOthers(int others) {
		this.others = others;
	}

	public int getBlkcount() {
		return blkcount;
	}

	public void setBlkcount(int blkcount) {
		this.blkcount = blkcount;
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

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getSteering() {
		return steering;
	}

	public void setSteering(String steering) {
		this.steering = steering;
	}

	public String getKeepAliveInterval() {
		return keepAliveInterval;
	}

	public void setKeepAliveInterval(String keepAliveInterval) {
		this.keepAliveInterval = keepAliveInterval;
	}

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public String getLastseen() {
		return lastseen;
	}

	public void setLastseen(String lastseen) {
		this.lastseen = lastseen;
	}

	public String getCustomizeInactivityMailSent() {
		return customizeInactivityMailSent;
	}

	public void setCustomizeInactivityMailSent(String customizeInactivityMailSent) {
		this.customizeInactivityMailSent = customizeInactivityMailSent;
	}

	public String getCustomizekeepAliveflag() {
		return customizekeepAliveflag;
	}

	public void setCustomizekeepAliveflag(String customizekeepAliveflag) {
		this.customizekeepAliveflag = customizekeepAliveflag;
	}

	public String getDevCrashTimestamp() {
		return devCrashTimestamp;
	}

	public void setDevCrashTimestamp(String devCrashTimestamp) {
		this.devCrashTimestamp = devCrashTimestamp;
	}

	public String getDevCrashdumpFileName() {
		return devCrashdumpFileName;
	}

	public void setDevCrashdumpFileName(String devCrashdumpFileName) {
		this.devCrashdumpFileName = devCrashdumpFileName;
	}

	public String getDevCrashDumpUploadStatus() {
		return devCrashDumpUploadStatus;
	}

	public void setDevCrashDumpUploadStatus(String devCrashDumpUploadStatus) {
		this.devCrashDumpUploadStatus = devCrashDumpUploadStatus;
	}

	public String getBssStationRejectThresh() {
		return bssStationRejectThresh;
	}

	public void setBssStationRejectThresh(String bssStationRejectThresh) {
		this.bssStationRejectThresh = bssStationRejectThresh;
	}

	public String getBssChanPbusyPercent() {
		return bssChanPbusyPercent;
	}

	public void setBssChanPbusyPercent(String bssChanPbusyPercent) {
		this.bssChanPbusyPercent = bssChanPbusyPercent;
	}

	public String getBssRejectTimeout() {
		return bssRejectTimeout;
	}

	public void setBssRejectTimeout(String bssRejectTimeout) {
		this.bssRejectTimeout = bssRejectTimeout;
	}

	public String getBand2GStaRatio() {
		return band2GStaRatio;
	}

	public void setBand2GStaRatio(String band2gStaRatio) {
		band2GStaRatio = band2gStaRatio;
	}

	public String getBand5GStaRatio() {
		return band5GStaRatio;
	}

	public void setBand5GStaRatio(String band5gStaRatio) {
		band5GStaRatio = band5gStaRatio;
	}

	public String getBandRcpiDiff() {
		return bandRcpiDiff;
	}

	public void setBandRcpiDiff(String bandRcpiDiff) {
		this.bandRcpiDiff = bandRcpiDiff;
	}

	public String getMinStaCount() {
		return minStaCount;
	}

	public void setMinStaCount(String minStaCount) {
		this.minStaCount = minStaCount;
	}

	public String getRssiThreshold() {
		return rssiThreshold;
	}

	public void setRssiThreshold(String rssiThreshold) {
		this.rssiThreshold = rssiThreshold;
	}

	public String getRcpiRange() {
		return rcpiRange;
	}

	public void setRcpiRange(String rcpiRange) {
		this.rcpiRange = rcpiRange;
	}

	public String getAvgStaCntLb() {
		return avgStaCntLb;
	}

	public void setAvgStaCntLb(String avgStaCntLb) {
		this.avgStaCntLb = avgStaCntLb;
	}

	public String getLoadBalance() {
		return loadBalance;
	}

	public void setLoadBalance(String loadBalance) {
		this.loadBalance = loadBalance;
	}

	public String getWorkingMode() {
		return workingMode;
	}

	public void setWorkingMode(String workingMode) {
		this.workingMode = workingMode;
	}

	public Collection<String> getTags() {
		return tags;
	}

	public void setTags(Collection<String> tags) {
		this.tags = tags;
	}

	public JSONObject getSteerClientList() {
		return steerClientList;
	}

	public void setSteerClientList(JSONObject steerClientList) {
		this.steerClientList = steerClientList;
	}

	public String getStatconnectedClientsList() {
		return statconnectedClientsList;
	}

	public void setStatconnectedClientsList(String statconnectedClientsList) {
		this.statconnectedClientsList = statconnectedClientsList;
	}

	public String getPeerList() {
		return peerList;
	}

	public void setPeerList(String peerList) {
		this.peerList = peerList;
	}

	@Override
	public String toString() {
		return "Device [id=" + id + ", pkid=" + pkid + ", uid=" + uid + ", name=" + name + ", conf=" + conf
				+ ", fstype=" + fstype + ", createdBy=" + createdBy + ", modifiedBy=" + modifiedBy + ", createdOn="
				+ createdOn + ", modifiedOn=" + modifiedOn + ", score=" + score + ", ver=" + ver + ", status=" + status
				+ ", template=" + template + ", state=" + state + ", vap2gcount=" + vap2gcount + ", vap5gcount="
				+ vap5gcount + ", peercount=" + peercount + ", peer5gcount=" + peer5gcount + ", peer2gcount="
				+ peer2gcount + ", android=" + android + ", ios=" + ios + ", windows=" + windows + ", speaker="
				+ speaker + ", printer=" + printer + ", others=" + others + ", blkcount=" + blkcount + ", sid=" + sid
				+ ", spid=" + spid + ", cid=" + cid + ", alias=" + alias + ", ip=" + ip + ", role=" + role
				+ ", steering=" + steering + ", keepAliveInterval=" + keepAliveInterval + ", root=" + root
				+ ", lastseen=" + lastseen + ", customizeInactivityMailSent=" + customizeInactivityMailSent
				+ ", customizekeepAliveflag=" + customizekeepAliveflag + ", devCrashTimestamp=" + devCrashTimestamp
				+ ", devCrashdumpFileName=" + devCrashdumpFileName + ", devCrashDumpUploadStatus="
				+ devCrashDumpUploadStatus + ", bssStationRejectThresh=" + bssStationRejectThresh
				+ ", bssChanPbusyPercent=" + bssChanPbusyPercent + ", bssRejectTimeout=" + bssRejectTimeout
				+ ", band2GStaRatio=" + band2GStaRatio + ", band5GStaRatio=" + band5GStaRatio + ", bandRcpiDiff="
				+ bandRcpiDiff + ", minStaCount=" + minStaCount + ", rssiThreshold=" + rssiThreshold + ", rcpiRange="
				+ rcpiRange + ", avgStaCntLb=" + avgStaCntLb + ", loadBalance=" + loadBalance + ", workingMode="
				+ workingMode + ", tags=" + tags + ", steerClientList=" + steerClientList
				+ ", statconnectedClientsList=" + statconnectedClientsList + ", peerList=" + peerList + "]";
	}

}
