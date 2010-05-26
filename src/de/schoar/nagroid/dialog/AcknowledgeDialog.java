package de.schoar.nagroid.dialog;

import de.schoar.nagroid.R;
import de.schoar.nagroid.nagios.NagiosHost;
import de.schoar.nagroid.nagios.NagiosService;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

public class AcknowledgeDialog extends Dialog {
	
	Object problemObj;

	public AcknowledgeDialog(Context context, Object o) {
		super(context);
		problemObj = o;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.acknowledge);
		init();
		setButtons(this);
	}

	private void init() {
		setTitle("Acknowlegde Problem");
		
		String problemDesc = "";
		
		if (problemObj.getClass() == NagiosService.class) {
			NagiosService service = (NagiosService) problemObj;
			problemDesc = service.getHost().getName() + "/" + service.getName();
		}
		else if (problemObj.getClass() == NagiosHost.class) {
			NagiosHost host = (NagiosHost) problemObj;
			problemDesc = host.getName();
		}
		
		((TextView) findViewById(R.id.ackProblemDesc)).setText(problemDesc);
		setCancelable(true);
	}
	
	private void setButtons(final Dialog d) {
		Button btnCancel = (Button) findViewById(R.id.ackBtnCancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				d.dismiss();
			}
		});
		
		Button btnAck = (Button) findViewById(R.id.ackBtnAck);
		btnAck.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				
			}
		});
	}
}
