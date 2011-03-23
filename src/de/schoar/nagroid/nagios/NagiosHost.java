package de.schoar.nagroid.nagios;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class NagiosHost {
	private NagiosSite mSite;
	private String mName;
	private NagiosState mState;
	private NagiosExtState mExtState;
	private List<NagiosService> mServices = new LinkedList<NagiosService>();

	public NagiosHost(NagiosSite site, String hostname, NagiosState state) {
		mSite = site;
		mName = hostname;
		mState = state;
		mSite.addHost(this);
	}

	public void addChild(NagiosService ns) {
		mServices.add(ns);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Host: " + mName + " State: " + mState + "\n");
		for (NagiosService ns : mServices) {
			sb.append(ns.toString() + "\n");
		}
		return sb.toString();
	}

	public String getName() {
		return mName;
	}

	public NagiosState getState() {
		return mState;
	}

	public void setState(NagiosState ns) {
		mState = ns;
	}

	public NagiosExtState getExtState() {
		return mExtState;
	}

	public void setExtState(NagiosExtState ns) {
		mExtState = ns;
	}

	public List<NagiosService> getServices() {
		Collections.sort(mServices, new NagiosServiceComparator());
		return mServices;
	}
}
