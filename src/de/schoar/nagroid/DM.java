package de.schoar.nagroid;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import de.schoar.android.helper.http.SSLSelfSigned;
import de.schoar.nagroid.log.NagroidLog;
import de.schoar.nagroid.nagios.NagiosState;
import de.schoar.nagroid.notification.HealthNotificationHelper;
import de.schoar.nagroid.notification.UpdateNotificationHelper;
import de.schoar.nagroid.polling.PollHandler;
import de.schoar.nagroid.service.PollReceiver;

public class DM {

	private static final String LOGT = "DM";

	// ----

	public static DM I = null;

	private static List<Context> CONTEXTS = new LinkedList<Context>();

	public synchronized static void register(Context ctx) {
		CONTEXTS.add(ctx);
		if (I == null) {
			I = new DM(ctx.getApplicationContext());
			I.init(ctx);
		}
		Log.d(LOGT, "Register: " + ctx.toString());
	}

	public synchronized static void unregister(Context ctx) {
		CONTEXTS.remove(ctx);
		Log.d(LOGT, "Unregister: " + ctx.toString());
	}

	// ----

	private Context mContext;

	private ConfigurationAccess mConfigurationAccess;

	private HealthNotificationHelper mHealthNotificationHelper;
	private UpdateNotificationHelper mUpdateNotificationHelper;

	private PollHandler mPollHandler;

	private NagroidLog mNagroidLog;

	private DM(Context ctx) {
		mContext = ctx;

		mConfigurationAccess = new ConfigurationAccess(ctx);
		mNagroidLog = new NagroidLog(mConfigurationAccess);

		mHealthNotificationHelper = new HealthNotificationHelper(ctx,
				mConfigurationAccess);
		mUpdateNotificationHelper = new UpdateNotificationHelper(ctx);

		mPollHandler = new PollHandler(ctx);
	}

	private void init(Context ctx) {
		if (getConfiguration().getNagiosSelfSigned()) {
			SSLSelfSigned.gi().enable();
		}

		mHealthNotificationHelper.updateNagiosState(mContext,
				NagiosState.HOST_LOCAL_ERROR, NagiosState.SERVICE_LOCAL_ERROR,
				false);
		mNagroidLog
				.addLogWithTime("Restored nagios state (not implemented yet). On next poll everything will be fine again.");

		PollReceiver.reschedule(mContext);
		if (ctx instanceof Activity) {
			getPollHandler().poll();
		}

	}

	// ----

	public ConfigurationAccess getConfiguration() {
		return mConfigurationAccess;
	}

	public PollHandler getPollHandler() {
		return mPollHandler;
	}

	public HealthNotificationHelper getHealthNotificationHelper() {
		return mHealthNotificationHelper;
	}

	public UpdateNotificationHelper getUpdateNotificationHelper() {
		return mUpdateNotificationHelper;
	}

	public NagroidLog getNagroidLog() {
		return mNagroidLog;
	}

}
