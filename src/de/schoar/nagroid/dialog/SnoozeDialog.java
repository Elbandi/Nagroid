package de.schoar.nagroid.dialog;

import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import de.schoar.nagroid.ConfigurationAccess;
import de.schoar.nagroid.DM;
import de.schoar.nagroid.nagios.NagiosHost;
import de.schoar.nagroid.nagios.NagiosService;
import de.schoar.nagroid.nagios.NagiosSite;
import de.schoar.nagroid.nagios.parser.NagiosParsingFailedException;
import de.schoar.nagroid.nagios.parser.NagiosV2Parser;

public class SnoozeDialog extends AlertDialog {
	
	private ConfigurationAccess ca = DM.I.getConfiguration();
	private String snoozeTime = ca.getSnoozeTime();
	
	public SnoozeDialog(Context context, Object o) {
		super(context);
		init(o);
	}

	private void init(final Object problemObj) {
		setTitle("Snooze Problem");
				
		String problemDesc = "";
		
		if (problemObj.getClass() == NagiosService.class) {
			NagiosService service = (NagiosService) problemObj;
			problemDesc = service.getHost().getName() + "/" + service.getName();

			if (service.getExtState() != null) {
				problemDesc += " ("+service.getExtState().getInfo()+")";
			}
		}
		else if (problemObj.getClass() == NagiosHost.class) {
			NagiosHost host = (NagiosHost) problemObj;
			problemDesc = host.getName();
		}
		
		Date now = new Date();
		Calendar nowcal = Calendar.getInstance();
		
		// Calculate if snooze time is today or tomorrow
		if (now.getHours() < Integer.valueOf(snoozeTime.split(":")[0])) {
			// Today
			snoozeTime = nowcal.get(Calendar.DATE)+"-"+(nowcal.get(Calendar.MONTH) + 1)+"-"+nowcal.get(Calendar.YEAR)+" "+snoozeTime+":00";
		} else {
			// Tomorrow
			nowcal.add(Calendar.DATE, 1);			
			snoozeTime = nowcal.get(Calendar.DATE)+"-"+(nowcal.get(Calendar.MONTH) + 1)+"-"+nowcal.get(Calendar.YEAR)+" "+snoozeTime+":00";
		}
		
		setMessage("Problem: "+problemDesc+"\n\nSnooze: "+snoozeTime+"\n\nComment:");
		
		final EditText input = new EditText(this.getContext());
		setView(input, 20, 0, 20, 20);
		
		setCancelable(true);
		
		setButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString(); 	
			  	SnoozeProblem(problemObj, value);	  	
			  	DM.I.getPollHandler().poll();
			  }
			});

		setButton2("Cancel", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
			    // Canceled.
			  }
		});
	}
	
	private void SnoozeProblem(Object o, String comment) {
	  	// Not working :(
		//ProgressDialog busyDialog = ProgressDialog.show(this.getContext(), "", "Sending Acknowledge...", true);
		
		NagiosSite site = ca.getNagiosSite();
		try {
			String cmdRes = new NagiosV2Parser(site).DowntimeProblem(o, snoozeTime, comment);
		  	
			//busyDialog.dismiss();
		  	
			AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
			builder.setTitle("Snooze problem");
			builder.setMessage(cmdRes);
			builder.setCancelable(true);
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
					//Nothing :)
		           }
			});
			builder.show();
					
		} catch (NagiosParsingFailedException e) {
			//Log.d("NagiosAcknowledge", "Nagios Acknowledge failed!", e);
			DM.I.getNagroidLog().addLogWithTime(
					"SnoozeProblem: Failed: " + e.getMessage());
			return;
		}

		DM.I.getNagroidLog().addLogWithTime("SnoozeProblem: Ok");
	}
}
