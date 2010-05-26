package de.schoar.nagroid.polling;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;
import de.schoar.android.helper.misc.Millis;
import de.schoar.android.helper.misc.Sleep;
import de.schoar.nagroid.DM;

public class PollHandler {

	private static final String LOGT = "Poller";

	private Context mContext;
	private ConnectivityManager mConnectivityManager;
	private WifiLock mWifiLock;

	private Boolean mPollRunning = false;

	private NagiosPollHandler mNagiosPollHandler;
	private UpdatePollHandler mUpdatePollHandler;

	public PollHandler(Context ctx) {
		mContext = ctx;
		mConnectivityManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		mNagiosPollHandler = new NagiosPollHandler(ctx);
		mUpdatePollHandler = new UpdatePollHandler(ctx);
	}

	public void poll() {
		synchronized (mPollRunning) {
			if (mPollRunning) {
				Log.d(LOGT, "Poll request ignored, already running");
				return;
			}
			mPollRunning = true;
		}
		new Thread() {
			@Override
			public void run() {
				try {
					prePoll();
					runPoll();
					postPoll();
				} catch (Throwable t) {
					Log.d(LOGT, "Catched throwable while polling", t);
				}
				mPollRunning = false;
			}
		}.start();

	}

	private void prePoll() {
		mWifiLock = null;

		if (!checkConnectivity()) {
			WifiManager wm = (WifiManager) mContext
					.getSystemService(Context.WIFI_SERVICE);
			mWifiLock = wm.createWifiLock(getClass().getSimpleName());

			mWifiLock.acquire();

			long start = System.currentTimeMillis();
			while (!checkConnectivity()
					&& System.currentTimeMillis() - start < Millis.SEC * 10) {
				Sleep.sleep(Millis.SEC);
			}
		}
	}

	private void runPoll() {
		long now = System.currentTimeMillis();
		long diff = (now - DM.I.getConfiguration().getInternLastPollTime()) / 1000;
		DM.I.getConfiguration().setInternLastPollTime(now);

		Log.d(LOGT, "Received poll request. Last poll was " + diff + "s ago");

		mNagiosPollHandler.poll();
		mUpdatePollHandler.poll();
	}

	private void postPoll() {
		if (mWifiLock != null) {
			mWifiLock.release();
		}
	}

	private boolean checkConnectivity() {
		boolean mobile = mConnectivityManager.getNetworkInfo(
				ConnectivityManager.TYPE_MOBILE).isConnected();
		boolean wifi = mConnectivityManager.getNetworkInfo(
				ConnectivityManager.TYPE_WIFI).isConnected();
		Log.d(LOGT, "Connectivity: Mobile: " + mobile + " WiFi: " + wifi);
		return mobile || wifi;
	}

	public boolean getPollRunning() {
		return mPollRunning;
	}

	public String getLastPollTimeSuccessfullText() {
		return "Last successfull update was "
				+ Millis.msToText(System.currentTimeMillis()
						- DM.I.getConfiguration()
								.getInternLastPollTimeSuccessfull()) + " ago";
	}

	public NagiosPollHandler getNagiosPollHandler() {
		return mNagiosPollHandler;
	}

}
