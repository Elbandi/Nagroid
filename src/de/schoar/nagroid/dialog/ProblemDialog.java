package de.schoar.nagroid.dialog;

import android.R.drawable;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
		final Dialog dlg = this;
		final boolean isProblemAcknowledged, isChecksDisabled, isNotificationsDisabled, isInScheduledDowntime;
		Drawable drawable;
		setTitle("Status details");

		final ListView lstSite = (ListView) view;
		Object problem = lstSite.getItemAtPosition(position);
		
		if (problem.getClass() == NagiosService.class) {
			final NagiosService serviceProblem = (NagiosService) problem;
			final NagiosExtState serviceExtState = serviceProblem.getExtState();
			
			setContentView(R.layout.service);
			getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			
			TextView tv = (TextView) findViewById(R.id.serviceTvHost);
			tv.setText("Host: " + serviceProblem.getHost().getName());
			
			tv = (TextView) findViewById(R.id.serviceTvService);
			tv.setText("Service: " + serviceProblem.getName());
			
			tv = (TextView) findViewById(R.id.serviceTvInfo);
			if (serviceExtState != null) {
				tv.setText("Info: " + serviceExtState.getInfo());
				tv.setVisibility(View.VISIBLE);
				
				isProblemAcknowledged = serviceExtState.isProblemAcknowledged();
				isChecksDisabled = serviceExtState.isChecksDisabled();
				isNotificationsDisabled = serviceExtState.isNotificationsDisabled();
				isInScheduledDowntime = serviceExtState.isInScheduledDowntime();
			} else {
				tv.setVisibility(View.GONE);
				isProblemAcknowledged = isChecksDisabled = isNotificationsDisabled = isInScheduledDowntime = false;
			}
			
			Button btn = (Button) findViewById(R.id.serviceBtnAckProblem);
			btn.setWidth(100);
			drawable = lstSite.getContext().getResources().getDrawable(isProblemAcknowledged ? R.drawable.noack2 : R.drawable.ack2);
			btn.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (isProblemAcknowledged) {
						RemoveAcknowledgeProblem(serviceProblem, NagiosV2Parser.RemoveAcknowledgeServiceProblem);
						/* nagios has delay, so we have to change this */
						// DM.I.getPollHandler().poll();
						serviceExtState.setProblemAcknowledged(false);
						dlg.cancel();
					} else
						new AcknowledgeDialog(lstSite.getContext(), serviceProblem, dlg).show();
				}
			});
			btn = (Button) findViewById(R.id.serviceBtnChecks);
			btn.setWidth(100);
			drawable = lstSite.getContext().getResources().getDrawable(isChecksDisabled ? R.drawable.enabled : R.drawable.disabled);
			btn.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (isChecksDisabled) {
						ChangeServiceCheck(serviceProblem, NagiosV2Parser.EnableChecks, "Enable Check");
						/* nagios has delay, so we have to change this */
						// DM.I.getPollHandler().poll();
						serviceExtState.setChecksDisabled(false);
						dlg.cancel();
					} else
						ShowAlert(lstSite.getContext(), "Disable Check", "Really?", new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								ChangeServiceCheck(serviceProblem, NagiosV2Parser.DisableChecks, "Disable Check");
								/* nagios has delay, so we have to change this */
								// DM.I.getPollHandler().poll();
								if (serviceExtState != null)
									serviceExtState.setChecksDisabled(true);
								dlg.cancel();
							}
						});
				}
			});
			btn = (Button) findViewById(R.id.serviceBtnNotifications);
			btn.setWidth(100);
			drawable = lstSite.getContext().getResources().getDrawable(isNotificationsDisabled ? R.drawable.enabled : R.drawable.disabled);
			btn.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (isNotificationsDisabled) {
						ChangeServiceNotify(serviceProblem, NagiosV2Parser.EnableNotifications, "Enable Notify");
						/* nagios has delay, so we have to change this */
						// DM.I.getPollHandler().poll();
						serviceExtState.setNotificationsDisabled(false);
						dlg.cancel();
					} else
						ShowAlert(lstSite.getContext(), "Disable Notify", "Really?", new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								ChangeServiceNotify(serviceProblem, NagiosV2Parser.DisableNotifications, "Disable Notify");
								/* nagios has delay, so we have to change this */
								// DM.I.getPollHandler().poll();
								if (serviceExtState != null)
									serviceExtState.setNotificationsDisabled(true);
								dlg.cancel();
							}
						});
				}
			});
			btn = (Button) findViewById(R.id.serviceBtnSchedCheck);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ScheduleCheck(serviceProblem, NagiosV2Parser.ScheduleImmediateCheck);
					DM.I.getPollHandler().poll();
					dlg.cancel();
				}
			});
			btn = (Button) findViewById(R.id.serviceBtnScheduleDowntime);
			btn.setWidth(100);
			drawable = lstSite.getContext().getResources().getDrawable(isInScheduledDowntime ? R.drawable.downtime_blue : R.drawable.downtime_red);
			btn.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					new SnoozeDialog(lstSite.getContext(), serviceProblem).show();
					dlg.cancel();
				}
			});
		} else if (problem.getClass() == NagiosHost.class) {
			final NagiosHost hostProblem = (NagiosHost) problem;
			final NagiosExtState hostExtState = hostProblem.getExtState();

			setContentView(R.layout.host);
			getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			
			TextView tv = (TextView) findViewById(R.id.hostTvHost);
			tv.setText("Host: " + hostProblem.getName());
			
			tv = (TextView) findViewById(R.id.hostTvInfo);

			if (hostExtState != null) {
				tv.setText("Info: " + hostExtState.getInfo());
				
				isProblemAcknowledged = hostExtState.isProblemAcknowledged();
				isChecksDisabled = hostExtState.isChecksDisabled();
				isNotificationsDisabled = hostExtState.isNotificationsDisabled();
				isInScheduledDowntime = hostExtState.isInScheduledDowntime();
			} else {
				tv.setText("Service Problems: " + hostProblem.getServices().size());
				isProblemAcknowledged = isChecksDisabled = isNotificationsDisabled = isInScheduledDowntime = false;
			}
			
			
			Button btn = (Button) findViewById(R.id.hostBtnAckProblem);
			btn.setWidth(100);
			drawable = lstSite.getContext().getResources().getDrawable(isProblemAcknowledged ? R.drawable.noack2 : R.drawable.ack2);
			btn.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (isProblemAcknowledged) {
						RemoveAcknowledgeProblem(hostProblem, NagiosV2Parser.RemoveAcknowledgeHostProblem);
						/* nagios has delay, so we have to change this */
						// DM.I.getPollHandler().poll();
						hostExtState.setProblemAcknowledged(false);
						dlg.cancel();
					} else
						new AcknowledgeDialog(lstSite.getContext(), hostProblem, dlg).show();
				}
			});
			btn = (Button) findViewById(R.id.hostBtnDisableServiceCheck);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					ChangeServiceCheck(hostProblem, NagiosV2Parser.DisableAllServiceChecks, "Disable All Check");
					dlg.cancel();
				}
			});
			btn = (Button) findViewById(R.id.hostBtnEnableServiceCheck);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					ChangeServiceCheck(hostProblem, NagiosV2Parser.EnableAllServiceChecks, "Enable All Check");
					dlg.cancel();
				}
			});
			btn = (Button) findViewById(R.id.hostBtnDisableServiceNotify);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					ChangeServiceNotify(hostProblem, NagiosV2Parser.DisableAllServiceNotifications, "Disable All Notify");
					dlg.cancel();
				}
			});
			btn = (Button) findViewById(R.id.hostBtnEnableServiceNotify);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					ChangeServiceNotify(hostProblem, NagiosV2Parser.EnableAllServiceNotifications, "Enable All Notify");
					dlg.cancel();
				}
			});
			btn = (Button) findViewById(R.id.hostBtnChecks);
			btn.setWidth(100);
			drawable = lstSite.getContext().getResources().getDrawable(isChecksDisabled ? R.drawable.enabled : R.drawable.disabled);
			btn.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (isChecksDisabled) {
						ChangeServiceCheck(hostProblem, NagiosV2Parser.EnableHostChecks, "Enable Check");
						/* nagios has delay, so we have to change this */
						// DM.I.getPollHandler().poll();
						hostExtState.setChecksDisabled(false);
						dlg.cancel();
					} else
						ShowAlert(lstSite.getContext(), "Disable Check", "Really?", new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								ChangeServiceCheck(hostProblem, NagiosV2Parser.DisableHostChecks, "Disable Check");
								/* nagios has delay, so we have to change this */
								// DM.I.getPollHandler().poll();
								if (hostExtState != null)
									hostExtState.setChecksDisabled(true);
								dlg.cancel();
							}
						});
				}
			});
			btn = (Button) findViewById(R.id.hostBtnNotifications);
			btn.setWidth(100);
			drawable = lstSite.getContext().getResources().getDrawable(isNotificationsDisabled ? R.drawable.enabled : R.drawable.disabled);
			btn.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (isNotificationsDisabled) {
						ChangeServiceNotify(hostProblem, NagiosV2Parser.EnableHostNotifications, "Enable Notify");
						/* nagios has delay, so we have to change this */
						// DM.I.getPollHandler().poll();
						hostExtState.setNotificationsDisabled(false);
						dlg.cancel();
					} else
						ShowAlert(lstSite.getContext(), "Disable Notify", "Really?", new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								ChangeServiceNotify(hostProblem, NagiosV2Parser.DisableHostNotifications, "Disable Notify");
								/* nagios has delay, so we have to change this */
								// DM.I.getPollHandler().poll();
								if (hostExtState != null)
									hostExtState.setNotificationsDisabled(true);
								dlg.cancel();
							}
						});
				}
			});
			
			btn = (Button) findViewById(R.id.hostBtnSchedCheck);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ScheduleCheck(hostProblem, NagiosV2Parser.ScheduleHostImmediateCheck);
					DM.I.getPollHandler().poll();
					dlg.cancel();
				}
			});

			btn = (Button) findViewById(R.id.hostBtnSchedServiceCheck);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ScheduleCheck(hostProblem, NagiosV2Parser.ScheduleHostServiceImmediateCheck);
					DM.I.getPollHandler().poll();
					dlg.cancel();
				}
			});

			btn = (Button) findViewById(R.id.hostBtnScheduleDowntime);
			btn.setWidth(100);
			drawable = lstSite.getContext().getResources().getDrawable(isInScheduledDowntime ? R.drawable.downtime_blue : R.drawable.downtime_red);
			btn.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					new SnoozeDialog(lstSite.getContext(), hostProblem).show();
					dlg.cancel();
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
	
	private void RemoveAcknowledgeProblem(Object o, int type) {
		// Not working :(
		// ProgressDialog busyDialog = ProgressDialog.show(this.getContext(),
		// "", "Sending Acknowledge...", true);
		
		ConfigurationAccess ca = DM.I.getConfiguration();
		NagiosSite site = ca.getNagiosSite();
		try {
			String cmdRes = new NagiosV2Parser(site).SendCmd(o, type, null);
			
			// busyDialog.dismiss();
			ShowResDialog("Remove Acknowledge", cmdRes);
		} catch (NagiosParsingFailedException e) {
			// Log.d("NagiosAcknowledge", "Nagios Acknowledge failed!", e);
			DM.I.getNagroidLog().addLogWithTime("RemoveAcknowledge: Failed: " + e.getMessage());
			return;
		}
		
		DM.I.getNagroidLog().addLogWithTime("RemoveAcknowledge: Ok");
	}
	
	private void ScheduleCheck(Object o, int type) {
		// Not working :(
		// ProgressDialog busyDialog = ProgressDialog.show(this.getContext(),
		// "", "Sending Acknowledge...", true);
		
		ConfigurationAccess ca = DM.I.getConfiguration();
		NagiosSite site = ca.getNagiosSite();
		try {
			String cmdRes = new NagiosV2Parser(site).ScheduleImmediateCheck(o, type);
			
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
