package com.campusconnect;

import android.util.Log;

public class CommMsgHelper 
{
	// remove the comment.
	public static boolean isParkingMsg(CommunityMsg v)
	{
		if(v == null)
		{
			Log.println(1, "error", "Community Msg is not initialized.");
			return false;
		}
		String sMsgType = v.getMsgType();
		if(sMsgType.equalsIgnoreCase("parking"))
			return true;
		
		return false;
	}
	
	public static boolean isWeatherMsg(CommunityMsg v)
	{
		if(v == null)
		{
			Log.println(1, "error", "Community Msg is not initialized.");
			return false;
		}
		String sMsgType = v.getMsgType();
		if(sMsgType.equalsIgnoreCase("weather"))
			return true;
		
		return false;
	}
	
	public static boolean isGeneralMsg(CommunityMsg v)
	{
		if(v == null)
		{
			Log.println(1, "error", "Community Msg is not initialized.");
			return false;
		}
		String sMsgType = v.getMsgType();
		if(sMsgType.equalsIgnoreCase("general"))
			return true;
		
		return false;
	}
	
	public static boolean isCommMsg(CommunityMsg v)
	{
		if(v == null)
		{
			Log.println(1, "error", "Community Msg is not initialized.");
			return false;
		}
		String sMsgType = v.getMsgType();
		if(sMsgType.equalsIgnoreCase("crimealert"))
			return true;
		
		return false;
	}
}
