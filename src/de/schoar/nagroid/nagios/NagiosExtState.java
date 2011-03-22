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

	public void setNotificationsDisabled(boolean NotificationsDisabled) {
		this.mNotificationsDisabled = NotificationsDisabled;
	}

	public boolean isNotificationsDisabled() {
		return mNotificationsDisabled;
	}

	public void setProblemAcknowledged(boolean ProblemAcknowledged) {
		this.mProblemAcknowledged = ProblemAcknowledged;
	}

	public boolean isProblemAcknowledged() {
		return mProblemAcknowledged;
	}

	public void setChecksDisabled(boolean ChecksDisabled) {
		this.mChecksDisabled = ChecksDisabled;
	}

	public boolean isChecksDisabled() {
		return mChecksDisabled;
	}

	public boolean isInScheduledDowntime() {
		return mInScheduledDowntime;
	}

}
