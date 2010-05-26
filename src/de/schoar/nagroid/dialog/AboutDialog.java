package de.schoar.nagroid.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import de.schoar.android.helper.misc.Updater;
import de.schoar.nagroid.R;

public class AboutDialog extends Dialog {

	public AboutDialog(Context context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.about);

		setTitle("About");
		setVersion();
		setButton(this);
	}

	private void setVersion() {
		String versionCurrent = Updater.getVersionName(getContext(), "?.?.?");
		((TextView) findViewById(R.id.aboutAppVersion)).setText("v"
				+ versionCurrent);
	}

	private void setButton(final Dialog d) {
		Button btn = (Button) findViewById(R.id.aboutBtnOk);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				d.dismiss();
			}
		});
	}
}
