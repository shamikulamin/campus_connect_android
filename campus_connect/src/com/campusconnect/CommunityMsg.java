package com.campusconnect;

import java.io.Serializable;

@SuppressWarnings("serial")
public class CommunityMsg implements Serializable{
	
	private int commMsgId;
	private String msgTitle;
	private String msgDescription;
	private String reportingTime;
	private String expiryTime;
	public String getExpiryTime() {
		return expiryTime;
	}

	public void setExpiryTime(String expiryTime) {
		this.expiryTime = expiryTime;
	}
	private String latLong;
	private String msgType;
	
	
	public String getMsgType() {
		return msgType;
	}
	
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	public int getCommMsgId() {
		return commMsgId;
	}
	public void setCommMsgId(int commMsgId) {
		this.commMsgId = commMsgId;
	}
	public String getMsgTitle() {
		return msgTitle;
	}
	public void setMsgTitle(String msgTitle) {
		this.msgTitle = msgTitle;
	}
	public String getMsgDescription() {
		return msgDescription;
	}
	public void setMsgDescription(String msgDescription) {
		this.msgDescription = msgDescription;
	}
	public String getReportingTime() {
		return reportingTime;
	}
	
	public void setReportingTime(String reportingTime) {
		this.reportingTime = reportingTime;
	}
	public String getLatLong() {
		return latLong;
	}
	public void setLatLong(String latLong) {
		this.latLong = latLong;
	}
	
}
