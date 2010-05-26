package de.schoar.nagroid.polling;

import android.content.Context;
import de.schoar.android.helper.misc.Updater;
import de.schoar.android.helper.misc.UpdaterException;
import de.schoar.nagroid.DM;

public class UpdatePollHandler {
	private Context mContext;
	private Updater mUpdater;

	public UpdatePollHandler(Context ctx) {
		mContext = ctx;

		mUpdater = new Updater(ctx) {
			@Override
			public void updateAvailable(Context ctx) {
				DM.I.getNagroidLog()
						.addLogWithTime("Updater: Update available");
				DM.I.getUpdateNotificationHelper().showUpdateAvailable(ctx);
			}

			@Override
			public void updateNotAvailable(Context ctx) {
				DM.I.getNagroidLog().addLogWithTime(
						"Updater: No update available");
			}
		};
	}

	public void poll() {
		if (!DM.I.getConfiguration().getMiscUpdate()) {
			return;
		}
		try {
			mUpdater.checkUpdate();
		} catch (UpdaterException e) {
			DM.I.getNagroidLog().addLogWithTime(
					"Updater: Failed: " + e.getMessage());
		}
	}

}
