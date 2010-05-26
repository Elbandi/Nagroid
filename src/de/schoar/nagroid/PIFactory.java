package de.schoar.nagroid;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class PIFactory {

	@SuppressWarnings("unchecked")
	public static PendingIntent getForActivity(Context ctx, Class c) {
		return PendingIntent.getActivity(ctx,
				PendingIntent.FLAG_CANCEL_CURRENT, new Intent(ctx, c), 0);
	}

}
