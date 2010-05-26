package de.schoar.nagroid.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import de.schoar.nagroid.DM;
import de.schoar.nagroid.DefaultMenu;
import de.schoar.nagroid.R;
import de.schoar.nagroid.log.NagroidLogUpdatedListener;

public class LogActivity extends Activity implements NagroidLogUpdatedListener {
	private Handler mHandler = new Handler();

	private ArrayAdapter<String> mAdapter = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		DM.register(this);

		setContentView(R.layout.log);

		ListView lstSite = (ListView) findViewById(R.id.logLstLogs);

		mAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, DM.I.getNagroidLog()
						.getLogs());

		lstSite.setAdapter(mAdapter);

		DM.I.getNagroidLog().addNagroidLogUpdatedListener(this);

		final Button btn = (Button) findViewById(R.id.logBtnClear);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				DM.I.getNagroidLog().clearLogs();
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DM.I.getNagroidLog().removeNagroidLogUpdatedListener(this);
		DM.unregister(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		DM.I.getNagroidLog().removeNagroidLogUpdatedListener(this);
		DM.unregister(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		DM.register(this);
		DM.I.getNagroidLog().addNagroidLogUpdatedListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		DefaultMenu.addRefresh(menu);
		DefaultMenu.addNagios(menu);
		DefaultMenu.addAbout(menu);
		DefaultMenu.addHelp(menu);
		DefaultMenu.addEnDisableService(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return DefaultMenu.onOptionsItemSelected(item, this);
	}

	@Override
	public void logUpdated() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mAdapter.notifyDataSetChanged();
			}
		});
	}

}
