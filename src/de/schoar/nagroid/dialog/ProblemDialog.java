package de.schoar.nagroid.dialog;

import android.app.Dialog;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import de.schoar.nagroid.ConfigurationAccess;
import de.schoar.nagroid.DM;
import de.schoar.nagroid.R;
import de.schoar.nagroid.nagios.NagiosExtState;
import de.schoar.nagroid.nagios.NagiosHost;
import de.schoar.nagroid.nagios.NagiosService;
import de.schoar.nagroid.nagios.NagiosSite;
import de.schoar.nagroid.nagios.NagiosState;
import de.schoar.nagroid.nagios.parser.NagiosParsingFailedException;
import de.schoar.nagroid.nagios.parser.NagiosV2Parser;

public class ProblemDialog extends Dialog {

	public ProblemDialog(Context context, AdapterView<?> view, int position) {
		super(context);
		init(view, position);
	}

	private void init(AdapterView<?> view, int position) {
		setTitle("Status details");

		final ListView lstSite = (ListView) view;
		Object problem = lstSite.getItemAtPosition(position);
		
		if (problem.getClass() == NagiosService.class) {
			final NagiosService serviceProblem = (NagiosService) problem;
			NagiosExtState serviceExtState = serviceProblem.getExtState();
			
			setContentView(R.layout.service);
			
			TextView tv = (TextView) findViewById(R.id.serviceTvHost);
			tv.setText("Host: " + serviceProblem.getHost().getName());
			
			tv = (TextView) findViewById(R.id.serviceTvService);
			tv.setText("Service: " + serviceProblem.getName());
			
			tv = (TextView) findViewById(R.id.serviceTvInfo);
			if (serviceExtState != null) {
				tv.setText("Info: " + serviceExtState.getInfo());
				tv.setVisibility(View.VISIBLE);
			} else
				tv.setVisibility(View.INVISIBLE);
			
			Button btn = (Button) findViewById(R.id.serviceBtnAckProblem);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					new AcknowledgeDialog(lstSite.getContext(), serviceProblem).show();
				}
			});
			btn = (Button) findViewById(R.id.serviceBtnDisableCheck);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ShowAlert(lstSite.getContext(), "Disable Check", "Really?", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							DisableCheck(serviceProblem, NagiosV2Parser.DisableChecks);
							DM.I.getPollHandler().poll();
						}
					});
				}
			});
			btn = (Button) findViewById(R.id.serviceBtnDisableNotify);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ShowAlert(lstSite.getContext(), "Disable Notify", "Really?", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							DisableNotify(serviceProblem, NagiosV2Parser.DisableNotifications);
							DM.I.getPollHandler().poll();
						}
					});
				}
			});
			btn = (Button) findViewById(R.id.serviceBtnSchedCheck);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ScheduleCheck(serviceProblem);
					DM.I.getPollHandler().poll();
				}
			});
		} else if (problem.getClass() == NagiosHost.class) {
			final NagiosHost hostProblem = (NagiosHost) problem;
			setContentView(R.layout.host);
			
			TextView tv = (TextView) findViewById(R.id.hostTvHost);
			tv.setText("Host: " + hostProblem.getName());
			
			tv = (TextView) findViewById(R.id.hostTvInfo);
			tv.setText("Service Problems: " + hostProblem.getServices().size());

			Button btn = (Button) findViewById(R.id.hostBtnDisableServiceCheck);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					ChangeServiceCheck(hostProblem, NagiosV2Parser.DisableAllServiceChecks, "Disable All Check");
				}
			});
			btn = (Button) findViewById(R.id.hostBtnEnableServiceCheck);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					ChangeServiceCheck(hostProblem, NagiosV2Parser.EnableAllServiceChecks, "Enable All Check");
				}
			});
			btn = (Button) findViewById(R.id.hostBtnDisableServiceNotify);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					ChangeServiceNotify(hostProblem, NagiosV2Parser.DisableAllServiceChecks, "Disable All Notify");
				}
			});
			btn = (Button) findViewById(R.id.hostBtnEnableServiceNotify);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					ChangeServiceNotify(hostProblem, NagiosV2Parser.EnableAllServiceChecks, "Enable All Notify");
				}
			});
			btn = (Button) findViewById(R.id.hostBtnDisableCheck);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ShowAlert(lstSite.getContext(), "Disable Check", "Really?", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							DisableCheck(hostProblem, NagiosV2Parser.DisableHostChecks);
							DM.I.getPollHandler().poll();
						}
					});
				}
			});
			btn = (Button) findViewById(R.id.hostBtnDisableNotify);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ShowAlert(lstSite.getContext(), "Disable Notify", "Really?", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							DisableNotify(hostProblem, NagiosV2Parser.DisableHostNotifications);
							DM.I.getPollHandler().poll();
						}
					});
				}
			});
			
		}
		setCancelable(true);
	}
	
	private void ShowAlert(Context context, String title, String message, DialogInterface.OnClickListener cl) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setCancelable(true);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setPositiveButton("Yes" /* android.R.string.yes */, cl);
		builder.setNegativeButton("no" /* android.R.string.no */, null);
		builder.show();
	}
	
	private void ShowResDialog(String title, String cmdRes) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
		builder.setTitle(title);
		builder.setMessage(cmdRes);
		builder.setCancelable(true);
		builder.setPositiveButton("OK", null);
		builder.show();
	}
	
	private void DisableCheck(Object o, int type) {
		// Not working :(
		// ProgressDialog busyDialog = ProgressDialog.show(this.getContext(),
		// "", "Sending Acknowledge...", true);
		
		ConfigurationAccess ca = DM.I.getConfiguration();
		NagiosSite site = ca.getNagiosSite();
		try {
			String cmdRes = new NagiosV2Parser(site).SendCmd(o, type, null);
			
			// busyDialog.dismiss();
			ShowResDialog("Disable Check", cmdRes);
		} catch (NagiosParsingFailedException e) {
			// Log.d("NagiosAcknowledge", "Nagios Acknowledge failed!", e);
			DM.I.getNagroidLog().addLogWithTime("DisableCheck: Failed: " + e.getMessage());
			return;
		}
		
		DM.I.getNagroidLog().addLogWithTime("DisableCheck: Ok");
	}
	
	private void DisableNotify(Object o, int type) {
		// Not working :(
		// ProgressDialog busyDialog = ProgressDialog.show(this.getContext(),
		// "", "Sending Acknowledge...", true);
		
		ConfigurationAccess ca = DM.I.getConfiguration();
		NagiosSite site = ca.getNagiosSite();
		try {
			String cmdRes = new NagiosV2Parser(site).SendCmd(o, type, null);
			// busyDialog.dismiss();
			ShowResDialog("Disable Notify", cmdRes);
		} catch (NagiosParsingFailedException e) {
			// Log.d("NagiosAcknowledge", "Nagios Acknowledge failed!", e);
			DM.I.getNagroidLog().addLogWithTime("DisableNotify: Failed: " + e.getMessage());
			return;
		}
		
		DM.I.getNagroidLog().addLogWithTime("DisableNotify: Ok");
	}
	
	private void ScheduleCheck(Object o) {
		// Not working :(
		// ProgressDialog busyDialog = ProgressDialog.show(this.getContext(),
		// "", "Sending Acknowledge...", true);
		
		ConfigurationAccess ca = DM.I.getConfiguration();
		NagiosSite site = ca.getNagiosSite();
		try {
			String cmdRes = new NagiosV2Parser(site).ScheduleImmediateCheck(o);
			
			// busyDialog.dismiss();
			
			ShowResDialog("Schedule Check", cmdRes);
		} catch (NagiosParsingFailedException e) {
			// Log.d("NagiosAcknowledge", "Nagios Acknowledge failed!", e);
			DM.I.getNagroidLog().addLogWithTime("ScheduleCheck: Failed: " + e.getMessage());
			return;
		}
		
		DM.I.getNagroidLog().addLogWithTime("ScheduleCheck: Ok");
	}
	
	private void ChangeServiceCheck(Object o, int type, String title) {
		// Not working :(
		// ProgressDialog busyDialog = ProgressDialog.show(this.getContext(),
		// "", "Sending Acknowledge...", true);
		
		ConfigurationAccess ca = DM.I.getConfiguration();
		NagiosSite site = ca.getNagiosSite();
		try {
			String cmdRes = new NagiosV2Parser(site).SendCmd(o, type, null);
			
			// busyDialog.dismiss();
			ShowResDialog(title, cmdRes);
		} catch (NagiosParsingFailedException e) {
			// Log.d("NagiosAcknowledge", "Nagios Acknowledge failed!", e);
			DM.I.getNagroidLog().addLogWithTime("ChangeServiceCheck: Failed: " + e.getMessage());
			return;
		}
		
		DM.I.getNagroidLog().addLogWithTime(title + ": Ok");
	}
	
	private void ChangeServiceNotify(Object o, int type, String title) {
		// Not working :(
		// ProgressDialog busyDialog = ProgressDialog.show(this.getContext(),
		// "", "Sending Acknowledge...", true);
		
		ConfigurationAccess ca = DM.I.getConfiguration();
		NagiosSite site = ca.getNagiosSite();
		try {
			String cmdRes = new NagiosV2Parser(site).SendCmd(o, type, null);
			// busyDialog.dismiss();
			ShowResDialog(title, cmdRes);
		} catch (NagiosParsingFailedException e) {
			// Log.d("NagiosAcknowledge", "Nagios Acknowledge failed!", e);
			DM.I.getNagroidLog().addLogWithTime("ChangeServiceNotify: Failed: " + e.getMessage());
			return;
		}
		
		DM.I.getNagroidLog().addLogWithTime(title + ": Ok");
	}
}
