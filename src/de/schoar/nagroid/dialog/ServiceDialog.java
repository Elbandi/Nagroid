package de.schoar.nagroid.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import de.schoar.nagroid.DM;

public class ServiceDialog extends AlertDialog {

	public ServiceDialog(Activity act) {
		super(act);
		init(act);
	}

	private void init(final Activity act) {
		setTitle("En/Disable Service");

		final boolean enabled = DM.I.getConfiguration().getPollingEnabled();

		String msg = enabled ? "enabled" : "disabled";
		setMessage("Service is currently " + msg);

		String text = enabled ? "Disable" : "Enable";

		setButton(text, new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				DM.I.getConfiguration().setPollingEnabled(!enabled,
						act.getApplicationContext());
			}
		});

		setButton2("Cancel", new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
			}
		});

		setCancelable(true);
	}
}
