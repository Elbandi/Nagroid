package de.schoar.nagroid.notification;

import android.app.Notification;
import android.content.Context;
import de.schoar.android.helper.misc.Updater;
import de.schoar.nagroid.R;

public class UpdateNotificationHelper extends NotificationHelper {

	public UpdateNotificationHelper(Context ctx) {
		super(ctx, R.drawable.nagroid_update_25x25);
	}

	public void showUpdateAvailable(final Context ctx) {
		notify(ctx, "Update available...", R.drawable.nagroid_update_25x25,
				Notification.FLAG_ONLY_ALERT_ONCE, Updater
						.getUpdateMarketIntent(ctx));
	}
}
