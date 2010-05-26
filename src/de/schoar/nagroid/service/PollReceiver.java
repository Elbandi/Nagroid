package de.schoar.nagroid.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import de.schoar.nagroid.DM;

public class PollReceiver extends BroadcastReceiver {

	private static final String LOGT = "PollReceiver";

	private static final String INTENT_POLL = "de.schoar.nagroid.POLL";

	@Override
	public void onReceive(final Context ctx, Intent t) {
		Intent svc = new Intent(ctx, PollService.class);
		ctx.startService(svc);
	}

	public static void stop(Context ctx) {
		Log.d(LOGT, "AlarmService: stopped");
		AlarmManager am = (AlarmManager) ctx
				.getSystemService(Context.ALARM_SERVICE);
		am.cancel(getPendingIntentPoll(ctx));
	}

	public static void reschedule(Context ctx) {
		long interval = DM.I.getConfiguration().getPollingInterval();
		long next = DM.I.getConfiguration().getInternLastPollTime() + interval;
		int intervalAsSeconds = (int) (interval / 1000);
		int nextAsSeconds = (int) ((next - System.currentTimeMillis()) / 1000);

		Log.d(LOGT, "Reschedule request: " + intervalAsSeconds + "s");

		if (interval == 0) {
			stop(ctx);
			return;
		}

		if (!DM.I.getConfiguration().getPollingEnabled()) {
			return;
		}

		AlarmManager am = (AlarmManager) ctx
				.getSystemService(Context.ALARM_SERVICE);

		Log.d(LOGT, "AlarmService: scheduled: " + intervalAsSeconds
				+ "s, next in " + nextAsSeconds + "s");
		am.setRepeating(AlarmManager.RTC_WAKEUP, next, interval,
				getPendingIntentPoll(ctx));
	}

	private static PendingIntent getPendingIntentPoll(Context ctx) {
		return PendingIntent.getBroadcast(ctx,
				PendingIntent.FLAG_CANCEL_CURRENT, new Intent(
						PollReceiver.INTENT_POLL), 0);
	}

}
