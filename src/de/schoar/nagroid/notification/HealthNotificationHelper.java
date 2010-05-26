package de.schoar.nagroid.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Vibrator;
import de.schoar.nagroid.ConfigurationAccess;
import de.schoar.nagroid.DM;
import de.schoar.nagroid.HealthState;
import de.schoar.nagroid.PIFactory;
import de.schoar.nagroid.R;
import de.schoar.nagroid.activity.ProblemsActivity;
import de.schoar.nagroid.nagios.NagiosState;

public class HealthNotificationHelper extends NotificationHelper {

	private ConfigurationAccess mConfigurationAccess;

	private HealthState mLastHealthState = null;

	private boolean mIsDisplayed = false;

	public HealthNotificationHelper(Context ctx, ConfigurationAccess ca) {
		super(ctx, R.layout.problems);
		mConfigurationAccess = ca;
	}

	public void updateNagiosState(Context ctx, NagiosState stateHost,
			NagiosState stateService, boolean noErrorOccured) {
		updateNagiosState(ctx, new HealthState(noErrorOccured, stateHost,
				stateService), false);
	}

	public void showLast(Context ctx) {
		if (mLastHealthState == null) {
			return;
		}
		updateNagiosState(ctx, mLastHealthState, true);
	}

	@Override
	public void clear() {
		super.clear();
		mIsDisplayed = false;
	}

	private void updateNagiosState(Context ctx, HealthState hs, boolean quiet) {

		if (hs.getVibrate().length != 0 && !quiet) {
			Vibrator v = (Vibrator) ctx
					.getSystemService(Context.VIBRATOR_SERVICE);
			v.vibrate(hs.getVibrate(), -1);
		}

		if (DM.I.getConfiguration().getNotificationAlarmEnabled()
				&& hs.getSoundUri() != null && !quiet) {
			MediaPlayer mp = MediaPlayer.create(ctx, hs.getSoundUri());
			mp.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					mp.release();
				}
			});
			mp.start();
		}

		if (mLastHealthState != null && mLastHealthState.equals(hs)
				&& mIsDisplayed) {
			return;
		}

		if (mConfigurationAccess.getNotificationHideIfOk() && hs.isOk()) {
			clear();
			return;
		}

		if (!DM.I.getConfiguration().getPollingEnabled()) {
			return;
		}

		show(ctx, hs);

	}

	private void show(Context ctx, HealthState hs) {
		mIsDisplayed = true;
		mLastHealthState = hs;

		PendingIntent pi = PIFactory
				.getForActivity(ctx, ProblemsActivity.class);

		notify(ctx, hs.getText(), hs.getResourceId(),
				Notification.FLAG_NO_CLEAR, pi);
	}
}
