package de.schoar.nagroid.nagios;

public class NagiosService {
	private NagiosHost mHost;
	private String mServiceName;
	private NagiosState mState;
	private NagiosExtState mExtState;

	public NagiosService(NagiosHost host, String servicename, NagiosState state, NagiosExtState extstate) {
		mHost = host;
		mServiceName = servicename;
		mState = state;
		mExtState = extstate;
		host.addChild(this);
	}

	// state, info, attempt, lastcheck, age, last state change, current state
	// duration, is downtime,
	// notifications

	@Override
	public String toString() {
		return ("Service: " + mServiceName + " State: " + mState);
	}

	public NagiosHost getHost() {
		return mHost;
	}

	public String getName() {
		return mServiceName;
	}

	public NagiosState getState() {
		return mState;
	}
	
	public NagiosExtState getExtState() {
		return mExtState;
	}
}
