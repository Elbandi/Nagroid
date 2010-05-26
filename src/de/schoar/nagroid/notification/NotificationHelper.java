package de.schoar.nagroid.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import de.schoar.nagroid.R;

public class NotificationHelper {

	protected Context mContext;
	protected NotificationManager mNotificationManager;
	private int mNotificationId;

	protected NotificationHelper(Context ctx, int notificationId) {
		mContext = ctx;
		mNotificationManager = (NotificationManager) ctx
				.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public void clear() {
		mNotificationManager.cancel(mNotificationId);
	}

	protected void notify(Context ctx, String text, String iconId, int flags,
			PendingIntent intent) {
		int rid = mContext.getResources().getIdentifier(iconId, null, null);
		notify(ctx, text, rid, flags, intent);
	}

	protected void notify(Context ctx, String text, int iconId, int flags,
			PendingIntent intent) {

		if (iconId == 0) {
			iconId = R.drawable.nagroid_48x48;
		}
		Notification n = new Notification(iconId, null, System
				.currentTimeMillis());
		n.setLatestEventInfo(ctx, ctx.getText(R.string.app_name), text, intent);
		n.flags = flags;

		mNotificationManager.notify(mNotificationId, n);
	}
}
