package de.schoar.nagroid.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
//import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import de.schoar.nagroid.ConfigurationAccess;
import de.schoar.nagroid.DM;
import de.schoar.nagroid.nagios.NagiosExtState;
import de.schoar.nagroid.nagios.NagiosHost;
import de.schoar.nagroid.nagios.NagiosService;
import de.schoar.nagroid.nagios.NagiosSite;
import de.schoar.nagroid.nagios.parser.NagiosParsingFailedException;
import de.schoar.nagroid.nagios.parser.NagiosV2Parser;

public class AcknowledgeDialog extends AlertDialog {
	
	public AcknowledgeDialog(Context context, Object o, DialogInterface parent) {
		super(context);
		init(o, parent);
	}

	private void init(final Object problemObj, final DialogInterface parent) {
		final NagiosService service;
		final NagiosHost host;
		setTitle("Acknowledge Problem");
				
		String problemDesc = "";
		
		if (problemObj.getClass() == NagiosService.class) {
			service = (NagiosService) problemObj;
			host = null;
			problemDesc = service.getHost().getName() + "/" + service.getName();

			if (service.getExtState() != null) {
				problemDesc += " ("+service.getExtState().getInfo()+")";
			}
		}
		else if (problemObj.getClass() == NagiosHost.class) {
			host = (NagiosHost) problemObj;
			service = null;
			problemDesc = host.getName();
		} else {
			host = null;
			service = null;
		}
		
		setMessage("Problem: "+problemDesc+"\n\nComment:");
		
		final EditText input = new EditText(this.getContext());
		setView(input, 20, 0, 20, 20);
		
		setCancelable(true);
		
		setButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				acknowledgeProblem(problemObj, value);
				/* nagios has delay, so we have to change this */
				//DM.I.getPollHandler().poll();
				if (service != null) {
					NagiosExtState serviceExtState = service.getExtState();
					if (serviceExtState != null)
						serviceExtState.setProblemAcknowledged(true);
				}
				parent.cancel();
			}
		});
		setButton2("Cancel", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
			    // Canceled.
			  }
		});
	}
	
	private void acknowledgeProblem(Object o, String comment) {
	  	// Not working :(
		//ProgressDialog busyDialog = ProgressDialog.show(this.getContext(), "", "Sending Acknowledge...", true);
	  	
		ConfigurationAccess ca = DM.I.getConfiguration();
		NagiosSite site = ca.getNagiosSite();
		try {
			String cmdRes = new NagiosV2Parser(site).AcknowledgeProblem(o, comment);
		  	
			//busyDialog.dismiss();
		  	
			AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
			builder.setTitle("Acknowledge problem");
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
					"AcknowledgeProblem: Failed: " + e.getMessage());
			return;
		}

		DM.I.getNagroidLog().addLogWithTime("AcknowledgeProblem: Ok");
	}
}
