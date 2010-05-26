package de.schoar.nagroid.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import de.schoar.nagroid.DM;
import de.schoar.nagroid.DefaultMenu;
import de.schoar.nagroid.R;
import de.schoar.nagroid.dialog.ProblemDialog;
import de.schoar.nagroid.nagios.NagiosSite;
import de.schoar.nagroid.nagios.NagiosSiteAdapater;
import de.schoar.nagroid.nagios.NagiosUpdatedListener;

public class ProblemsActivity extends Activity implements NagiosUpdatedListener {

	private Handler mHandler = new Handler();

	private NagiosSiteAdapater mAdapter = new NagiosSiteAdapater(this);

	private Timer mTimerLastPollSuccessfull = new Timer();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		DM.register(this);

		setContentView(R.layout.problems);

		ListView lstSite = (ListView) findViewById(R.id.problemsLstProblems);
		lstSite.setAdapter(mAdapter);
		lstSite.setTextFilterEnabled(true);

		lstSite.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    	Object o = ((ListView) parent).getItemAtPosition(position);
		    	if(o.getClass() != String.class) {
		    		new ProblemDialog(view.getContext(), parent, position).show();
		    	}
		    }
		});

		DM.I.getPollHandler().getNagiosPollHandler().addNagiosUpdatedListener(
				this);

		final TextView tvRunning = (TextView) findViewById(R.id.problemsTvRunning);
		final TextView tvLast = (TextView) findViewById(R.id.problemsTvLast);

		mTimerLastPollSuccessfull.schedule(new TimerTask() {
			@Override
			public void run() {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if (DM.I.getPollHandler().getPollRunning()) {
							tvLast.setText("Polling - please wait...");
						} else {
							tvLast.setText(DM.I.getPollHandler()
									.getLastPollTimeSuccessfullText());
						}
						tvRunning.setText(DM.I.getConfiguration()
								.getPollingEnabled() ? "Service is enabled"
								: "Service is DISABLED");
					}
				});
			}
		}, 0, 1000);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DM.I.getPollHandler().getNagiosPollHandler()
				.removeNagiosUpdatedListener(this);
		DM.unregister(this);
		mTimerLastPollSuccessfull.cancel();
	}

	@Override
	protected void onPause() {
		super.onPause();
		DM.unregister(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		DM.register(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		DefaultMenu.addRefresh(menu);
		DefaultMenu.addLog(menu);
		DefaultMenu.addNagios(menu);
		DefaultMenu.addAbout(menu);
		DefaultMenu.addConfiguration(menu);
		DefaultMenu.addHelp(menu);
		DefaultMenu.addEnDisableService(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return DefaultMenu.onOptionsItemSelected(item, this);
	}

	@Override
	public void nagiosUpdated(NagiosSite site) {
		mAdapter.updateHosts(site.getHosts());

		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mAdapter.notifyDataSetChanged();
			}
		});

	}
}
