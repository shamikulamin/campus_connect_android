package com.campusconnect;

import android.os.Parcel;
import android.os.Parcelable;


public class CommunityMsg implements Parcelable {

	private int commMsgId;
	private String msgTitle;
	private String msgDescription;
	private String reportingTime;
	private String expiryTime;
	private String latLong;
	private String msgType;
	
	public CommunityMsg(){};
	
	public CommunityMsg(String msgType, String title, String desc, String loc){
         this.msgType = msgType;
         this.msgTitle = title;
         this.msgDescription = desc;
         this.latLong = loc;
    }
	
	public String getExpiryTime() {
		return expiryTime;
	}

	public void setExpiryTime(String expiryTime) {
		this.expiryTime = expiryTime;
	}
	
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
	
	public CommunityMsg(Parcel in){
        String[] data = new String[4];
        
        in.readStringArray(data);
        this.msgType = data[0];
        this.msgTitle = data[1];
        this.msgDescription = data[2];
        this.latLong = data[3];
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {
							        		this.msgType,
							        	    this.msgTitle,
							        	    this.msgDescription,
							        	    this.latLong
                                            });
    }
    
    public static final Parcelable.Creator<CommunityMsg> CREATOR = new Parcelable.Creator<CommunityMsg>() {
	        public CommunityMsg createFromParcel(Parcel in) {
	            return new CommunityMsg(in); 
	        }
	
	        public CommunityMsg[] newArray(int size) {
	            return new CommunityMsg[size];
	        }
	    };
	
}

/*
//@SuppressWarnings("serial")
public class CommunityMsg implements Serializable {
	
	/**
	 * Auto generated serial
	 
	private static final long serialVersionUID = -2428394639356736629L;
	
	private int commMsgId;
	private String msgTitle;
	private String msgDescription;
	private String reportingTime;
	private String expiryTime;
	private String latLong;
	private String msgType;
	
	public String getExpiryTime() {
		return expiryTime;
	}

	public void setExpiryTime(String expiryTime) {
		this.expiryTime = expiryTime;
	}
	
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
*/
