package de.schoar.nagroid.dialog;

import de.schoar.nagroid.ConfigurationAccess;
import de.schoar.nagroid.DM;
import de.schoar.nagroid.R;
import de.schoar.nagroid.nagios.NagiosHost;
import de.schoar.nagroid.nagios.NagiosService;
import de.schoar.nagroid.nagios.NagiosSite;
import de.schoar.nagroid.nagios.parser.NagiosParsingFailedException;
import de.schoar.nagroid.nagios.parser.NagiosV2Parser;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AcknowledgeDialog extends AlertDialog {
	
	public AcknowledgeDialog(Context context, Object o) {
		super(context);
		init(context, o);
	}

	private void init(final Context context, final Object problemObj) {
		setTitle("Acknowlegde Problem");
				
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
		
		setMessage("Problem: "+problemDesc+"\n\nComment:");
		
		final EditText input = new EditText(context);
		setView(input, 20, 0, 20, 20);
		
		setCancelable(true);
		
		setButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
			  	ProgressDialog busyDialog = ProgressDialog.show(context, "", "Sending Acknowledge...", true);
			  	acknowledgeProblem(problemObj, value);
			  	busyDialog.dismiss();
			  }
			});

		setButton2("Cancel", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
			    // Canceled.
			  }
		});
	}
	
	private void acknowledgeProblem(Object o, String comment) {
		ConfigurationAccess ca = DM.I.getConfiguration();
		NagiosSite site = ca.getNagiosSite();
		try {
			new NagiosV2Parser(site).AcknowledgeProblem(o, comment);
			
		} catch (NagiosParsingFailedException e) {
			//Log.d("NagiosAcknowledge", "Nagios Acknowledge failed!", e);
			DM.I.getNagroidLog().addLogWithTime(
					"AcknowledgeProblem: Failed: " + e.getMessage());
			return;
		}

		DM.I.getNagroidLog().addLogWithTime("AcknowledgeProblem: Ok");
	}
}
