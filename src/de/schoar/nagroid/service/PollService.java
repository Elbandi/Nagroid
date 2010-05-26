package de.schoar.nagroid.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import de.schoar.nagroid.DM;

public class PollService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		DM.register(this);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		DM.I.getPollHandler().poll();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		DM.unregister(this);
	}

}
