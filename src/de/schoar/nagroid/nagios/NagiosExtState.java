package de.schoar.nagroid.nagios;

public class NagiosExtState {
	
	private String mInfo;
	private String mLastCheck;
	private String mDuration;
	private boolean mChecksDisabled;
	private boolean mNotificationsDisabled;
	private boolean mProblemAcknowledged;
	private boolean mInScheduledDowntime;

	public NagiosExtState(String info, String duration, String lastcheck, boolean ChecksDisabled, boolean NotificationsDisabled, boolean ProblemAcknowledged, boolean InScheduledDowntime) {
		mInfo = info;
		mDuration = duration;
		mLastCheck = lastcheck;
		mChecksDisabled = ChecksDisabled;
		mNotificationsDisabled = NotificationsDisabled;
		mProblemAcknowledged = ProblemAcknowledged;
		mInScheduledDowntime = InScheduledDowntime;
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

	public boolean isNotificationsDisabled() {
		return mNotificationsDisabled;
	}

	public boolean isProblemAcknowledged() {
		return mProblemAcknowledged;
	}

	public boolean isChecksDisabled() {
		return mChecksDisabled;
	}

	public boolean isInScheduledDowntime() {
		return mInScheduledDowntime;
	}

}
