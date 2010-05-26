package de.schoar.nagroid.nagios;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class NagiosSite {

	private String mUrlBase;
	private String mUrlUser = null;
	private String mUrlPass = null;
	private boolean mUnhandledOnly = false;

	private List<NagiosHost> mHosts = new LinkedList<NagiosHost>();

	public NagiosSite(String urlbase, boolean unhandledOnly) {
		this(urlbase, null, null, unhandledOnly);
	}

	public NagiosSite(String urlbase, String urluser, String urlpass,
			boolean unhandledOnly) {
		mUrlUser = urluser;
		mUrlPass = urlpass;
		mUrlBase = urlbase;
		mUnhandledOnly = unhandledOnly;
	}

	public NagiosHost findHost(String name) {
		for (NagiosHost nh : mHosts) {
			if (nh.getName().equals(name)) {
				return nh;
			}
		}
		return null;
	}

	public void addHost(NagiosHost nh) {
		mHosts.add(nh);
	}

	public List<NagiosHost> getHosts() {
		Collections.sort(mHosts, new NagiosHostComparator());
		return mHosts;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("URL: " + mUrlBase + "\n");
		for (NagiosHost nh : mHosts) {
			sb.append(nh.toString() + "\n");
		}
		return sb.toString();
	}

	public String getUrlBase() {
		return mUrlBase;
	}

	public String getUrlUser() {
		return mUrlUser;
	}

	public String getUrlPass() {
		return mUrlPass;
	}

	public boolean getUnhandledOnly() {
		return mUnhandledOnly;
	}
}
