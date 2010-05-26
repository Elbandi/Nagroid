package de.schoar.nagroid.polling;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import de.schoar.nagroid.ConfigurationAccess;
import de.schoar.nagroid.DM;
import de.schoar.nagroid.nagios.NagiosHost;
import de.schoar.nagroid.nagios.NagiosService;
import de.schoar.nagroid.nagios.NagiosSite;
import de.schoar.nagroid.nagios.NagiosState;
import de.schoar.nagroid.nagios.NagiosUpdatedListener;
import de.schoar.nagroid.nagios.parser.NagiosParsingFailedException;
import de.schoar.nagroid.nagios.parser.NagiosV2Parser;

public class NagiosPollHandler {
	private static final String LOGT = "NagiosPollHandler";

	private Context mContext;

	private NagiosSite mNagiosSite = null;
	private NagiosState mHighestHostState = NagiosState.HOST_LOCAL_ERROR;
	private NagiosState mHighestServiceState = NagiosState.SERVICE_LOCAL_ERROR;

	private List<NagiosUpdatedListener> mNagiosUpdatedListeners = new LinkedList<NagiosUpdatedListener>();

	public NagiosPollHandler(Context ctx) {
		mContext = ctx;
	}

	public void poll() {
		updateProblems();
		updateHighestState();

		doNotification();
	}

	private void updateProblems() {
		ConfigurationAccess ca = DM.I.getConfiguration();
		NagiosSite site = ca.getNagiosSite();
		try {
			new NagiosV2Parser(site).updateProblems();
		} catch (NagiosParsingFailedException e) {
			Log.d(LOGT, "Nagios Poll failed!", e);
			ca.setInternLastPollSuccessfull(false);
			DM.I.getNagroidLog().addLogWithTime(
					"Poller: Failed: " + e.getMessage());
			return;
		}

		mNagiosSite = site;

		DM.I.getNagroidLog().addLogWithTime("Poller: Ok");
		ca.setInternLastPollSuccessfull(true);

		ca.setInternLastPollTimeSuccessfull(System.currentTimeMillis());

		fireNagiosUpdated(site);
	}

	private void doNotification() {
		DM.I.getHealthNotificationHelper().updateNagiosState(mContext,
				mHighestHostState, mHighestServiceState,
				DM.I.getConfiguration().getInternLastPollSuccessfull());
	}

	private void updateHighestState() {
		NagiosState nstateHost = NagiosState.HOST_UP;
		NagiosState nstateService = NagiosState.SERVICE_OK;
		for (NagiosHost nh : mNagiosSite.getHosts()) {
			if (nstateHost.ordinal() > nh.getState().ordinal()) {
				nstateHost = nh.getState();
			}
			for (NagiosService ns : nh.getServices()) {
				if (nstateService.ordinal() > ns.getState().ordinal()) {
					nstateService = ns.getState();

				}
			}
		}
		mHighestServiceState = nstateService;
		mHighestHostState = nstateHost;
	}

	private synchronized void fireNagiosUpdated(NagiosSite site) {
		for (NagiosUpdatedListener nul : mNagiosUpdatedListeners) {
			nul.nagiosUpdated(site);
		}
	}

	public synchronized void addNagiosUpdatedListener(NagiosUpdatedListener nul) {
		mNagiosUpdatedListeners.add(nul);
		if (mNagiosSite != null) {
			nul.nagiosUpdated(mNagiosSite);
		}
	}

	public synchronized void removeNagiosUpdatedListener(
			NagiosUpdatedListener nul) {
		mNagiosUpdatedListeners.remove(nul);
	}

}
