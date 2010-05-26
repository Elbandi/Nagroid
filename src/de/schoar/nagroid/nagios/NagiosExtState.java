package de.schoar.nagroid.nagios;

public class NagiosExtState {
	
	private String mInfo;
	private String mLastCheck;
	private String mDuration;

	public NagiosExtState(String info, String duration, String lastcheck) {
		mInfo = info;
		mDuration = duration;
		mLastCheck = lastcheck;
	}
	
	public String getInfo() {
		return mInfo;
	}
	public String getDuration() {
		return mDuration;
	}
	public String getLastCheck() {
		return mLastCheck;
	}

}
