package de.schoar.nagroid.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import de.schoar.nagroid.ConfigurationAccess;

public class BootCompleteReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent t) {
		if (new ConfigurationAccess(ctx).getMiscAutostart()) {
			Intent svc = new Intent(ctx, PollService.class);
			ctx.startService(svc);
		}
	}

}
