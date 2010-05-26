package de.schoar.android.helper.misc;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.util.Log;
import de.schoar.android.helper.http.HTTPDownloader;

public abstract class Updater {
	private static final String LOGT = "Updater";
	private static final String CONFIG_UPDATER = "de.schoar.android.updater";

	private static final long INTERVAL_SUCCESSFULL = Millis.DAY;
	private static final long INTERVAL_FAILED = Millis.HOUR;
	private static final String URLBASE = "http://android.schoar.de/update";

	private Context mCtx;
	private SharedPreferences mSharedPreferences;

	private int mVersionCodeCurrent = Integer.MAX_VALUE;

	public Updater(Context ctx) {
		mCtx = ctx;
		mVersionCodeCurrent = getVersionCode(ctx);
		mSharedPreferences = ctx.getApplicationContext().getSharedPreferences(
				CONFIG_UPDATER, Context.MODE_PRIVATE);
	}

	public abstract void updateNotAvailable(Context ctx);

	public abstract void updateAvailable(Context ctx);

	public void checkUpdate() throws UpdaterException {
		long now = System.currentTimeMillis();
		if (now - getUpdateLastSuccessfull() < INTERVAL_SUCCESSFULL) {
			return;
		}
		if (now - getUpdateLastFail() < INTERVAL_FAILED) {
			return;
		}

		Log.d(LOGT, "Checking for updates...");

		int versionCodeNew = 0;
		try {
			versionCodeNew = getVersionCodeNew();
		} catch (UpdaterException e) {
			setUpdateLastFail(now);
			throw e;
		}
		setUpdateLastSuccessfull(now);

		if (versionCodeNew <= mVersionCodeCurrent) {
			Log.d(LOGT, "No update available - Current: " + mVersionCodeCurrent
					+ " New: " + versionCodeNew);
			updateNotAvailable(mCtx);
			return;
		}

		Log.d(LOGT, "Update available - Current: " + mVersionCodeCurrent
				+ " New: " + versionCodeNew);

		updateAvailable(mCtx);
	}

	private int getVersionCodeNew() throws UpdaterException {
		String versionCodeNewBody = null;
		try {
			String url = URLBASE + "?pn=" + mCtx.getPackageName() + "&vc="
					+ mVersionCodeCurrent;
			versionCodeNewBody = new HTTPDownloader(url).getBodyAsString();
			if (versionCodeNewBody == null) {
				Log.w(LOGT, "Failed to retrieve: Body is null");
				throw new UpdaterException("Failed to retrieve: Body is null");
			}
		} catch (Exception e) {
			Log.w(LOGT, "Failed to retrieve: " + e.getMessage());
			throw new UpdaterException("Failed to retrieve: " + e.getMessage(),
					e);
		}

		try {
			return Integer.valueOf(versionCodeNewBody.trim());
		} catch (Exception e) {
			Log.w(LOGT, "Failed to retrieve: Unparseable: "
					+ versionCodeNewBody);
			throw new UpdaterException("Failed to retrieve: Unparseable: "
					+ versionCodeNewBody);
		}
	}

	private static int getVersionCode(Context ctx) {
		PackageManager pm = ctx.getPackageManager();
		try {
			return pm.getPackageInfo(ctx.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			Log.e(LOGT, "Could not find current version code: "
					+ e.getMessage());
			return Integer.MAX_VALUE;
		}
	}

	public static String getVersionName(Context ctx, String def) {
		PackageManager pm = ctx.getPackageManager();
		try {
			return pm.getPackageInfo(ctx.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Log.e(LOGT, "Could not find current version name: "
					+ e.getMessage());
			return def;
		}
	}

	public static PendingIntent getUpdateMarketIntent(Context ctx) {
		Uri uri = Uri.parse("market://search?q=pname:" + ctx.getPackageName());
		return PendingIntent.getActivity(ctx,
				PendingIntent.FLAG_CANCEL_CURRENT, new Intent(
						Intent.ACTION_VIEW, uri), 0);
	}

	public synchronized long getUpdateLastSuccessfull() {
		return mSharedPreferences.getLong("UpdateLastSuccessfull", 0);
	}

	public synchronized void setUpdateLastSuccessfull(long value) {
		mSharedPreferences.edit().putLong("UpdateLastSuccessfull", value)
				.commit();
	}

	public synchronized long getUpdateLastFail() {
		return mSharedPreferences.getLong("UpdateLastFail", 0);
	}

	public synchronized void setUpdateLastFail(long value) {
		mSharedPreferences.edit().putLong("UpdateLastFail", value).commit();
	}
}
