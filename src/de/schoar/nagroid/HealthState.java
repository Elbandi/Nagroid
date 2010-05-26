package de.schoar.nagroid;

import android.net.Uri;
import android.provider.Settings;
import de.schoar.nagroid.nagios.NagiosState;

public class HealthState {
	private boolean mPollingSuccessfull;
	private NagiosState mStateHosts;
	private NagiosState mStateServices;

	private String mText;
	private long mVibrate[];
	private Uri mSoundUri;
	private String mResourceId;

	public HealthState(boolean pollingSuccessfull, NagiosState stateHosts,
			NagiosState stateServices) {

		mPollingSuccessfull = pollingSuccessfull;
		mStateHosts = stateHosts;
		mStateServices = stateServices;
		init();
	}

	private void init() {
		long[] vibrate = new long[0];
		String text = "Everything is doing fine, relax...";

		if (!NagiosState.SERVICE_OK.equals(mStateServices)
				&& !NagiosState.SERVICE_LOCAL_ERROR.equals(mStateServices)) {
			text = "Houston, we have a service problem";
			vibrate = new long[] { 0, 400, 200, 100 };
		}
		if (!NagiosState.HOST_UP.equals(mStateHosts)
				&& !NagiosState.HOST_LOCAL_ERROR.equals(mStateHosts)) {
			text = "Houston, we have a host problem";
			vibrate = new long[] { 0, 400, 200, 100, 200, 100 };
		}
		if (!NagiosState.HOST_UP.equals(mStateHosts)
				&& !NagiosState.SERVICE_OK.equals(mStateServices)
				&& !NagiosState.SERVICE_LOCAL_ERROR.equals(mStateServices)
				&& !NagiosState.HOST_LOCAL_ERROR.equals(mStateHosts)) {
			text = "Ohoh - we have service and host problems. Hurry!";
			vibrate = new long[] { 0, 400, 200, 100, 200, 100, 200, 100 };
		}
		mVibrate = vibrate;
		mText = text;

		Uri soundUri = null;
		if (NagiosState.SERVICE_WARNING.equals(mStateServices)) {
			String uriSelected = DM.I.getConfiguration()
					.getNotificationAlarmWarning();
			String uriDefault = "android.resource://de.schoar.nagroid/raw/warning";
			soundUri = toSoundUri(uriSelected, uriDefault);
		}
		if (NagiosState.SERVICE_CRITICAL.equals(mStateServices)) {
			String uriSelected = DM.I.getConfiguration()
					.getNotificationAlarmCritical();
			String uriDefault = "android.resource://de.schoar.nagroid/raw/critical";
			soundUri = toSoundUri(uriSelected, uriDefault);
		}
		if (NagiosState.HOST_DOWN.equals(mStateHosts)
				|| NagiosState.HOST_UNREACHABLE.equals(mStateHosts)) {
			String uriSelected = DM.I.getConfiguration()
					.getNotificationAlarmDownUnreachable();
			String uriDefault = "android.resource://de.schoar.nagroid/raw/hostdown";
			soundUri = toSoundUri(uriSelected, uriDefault);
		}
		mSoundUri = soundUri;

		StringBuffer sb = new StringBuffer();
		sb.append("de.schoar.nagroid:drawable/state");
		if (mPollingSuccessfull) {
			sb.append("_ok");
		} else {
			sb.append("_error");
		}
		sb.append("_" + mStateHosts.toColorStrNoHash().toLowerCase());
		sb.append("_" + mStateServices.toColorStrNoHash().toLowerCase());
		mResourceId = sb.toString();
	}

	public boolean isOk() {
		if (!mPollingSuccessfull) {
			return false;
		}
		if (!NagiosState.HOST_UP.equals(mStateHosts)) {
			return false;
		}
		if (!NagiosState.SERVICE_OK.equals(mStateServices)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof HealthState)) {
			return false;
		}

		HealthState hs = (HealthState) o;
		if (mPollingSuccessfull != hs.getPollingSuccessfull()) {
			return false;
		}
		if (!mStateHosts.equals(hs.getStateHosts())) {
			return false;
		}
		if (!mStateServices.equals(hs.getStateServices())) {
			return false;
		}

		return true;
	}

	public boolean getPollingSuccessfull() {
		return mPollingSuccessfull;
	}

	public NagiosState getStateHosts() {
		return mStateHosts;
	}

	public NagiosState getStateServices() {
		return mStateServices;
	}

	public String getText() {
		return mText;
	}

	public long[] getVibrate() {
		if (!DM.I.getConfiguration().getNotificationVibrate()) {
			return new long[0];
		}

		return mVibrate;
	}

	public String getResourceId() {
		return mResourceId;
	}

	public Uri getSoundUri() {
		return mSoundUri;
	}

	private Uri toSoundUri(String soundUri, String defaultUri) {
		if (soundUri == null) {
			return null;
		}
		if (soundUri.length() == 0) {
			return null;
		}
		if (Settings.System.DEFAULT_RINGTONE_URI.toString().equals(soundUri)) {
			return Uri.parse(defaultUri);
		}
		if (Settings.System.DEFAULT_NOTIFICATION_URI.toString()
				.equals(soundUri)) {
			return Uri.parse(defaultUri);
		}
		return Uri.parse(soundUri);
	}
}
