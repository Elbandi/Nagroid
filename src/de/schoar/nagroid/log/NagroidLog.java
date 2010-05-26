package de.schoar.nagroid.log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;
import de.schoar.nagroid.ConfigurationAccess;

public class NagroidLog {
	private static final String LOGT = "NagroidLog";
	private static final int LOG_SIZE = 15;

	private final SimpleDateFormat mSdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private List<NagroidLogUpdatedListener> mNagroidLogUpdatedListeners = new LinkedList<NagroidLogUpdatedListener>();

	private List<String> mLogs = new LinkedList<String>();

	private ConfigurationAccess mConfigurationAccess;

	public NagroidLog(ConfigurationAccess ca) {
		mConfigurationAccess = ca;
		load();
	}

	public synchronized void addLogWithTime(String msg) {
		addLog(mSdf.format(new Date()) + "\n" + msg);
	}

	public synchronized void addLog(String msg) {
		while (mLogs.size() >= LOG_SIZE) {
			mLogs.remove(mLogs.size() - 1);
		}
		mLogs.add(0, msg);

		Log.d(LOGT, "Logged: " + msg);

		fireLogUpdated();
	}

	public synchronized List<String> getLogs() {
		return mLogs;
	}

	public synchronized void clearLogs() {
		mLogs.clear();
		fireLogUpdated();
	}

	private synchronized void fireLogUpdated() {
		store();
		for (NagroidLogUpdatedListener lul : mNagroidLogUpdatedListeners) {
			lul.logUpdated();
		}
	}

	public synchronized void addNagroidLogUpdatedListener(
			NagroidLogUpdatedListener lul) {
		mNagroidLogUpdatedListeners.add(lul);
	}

	public synchronized void removeNagroidLogUpdatedListener(
			NagroidLogUpdatedListener lul) {
		mNagroidLogUpdatedListeners.remove(lul);
	}

	private void load() {
		byte[] ba = mConfigurationAccess.getInternNagroidLogs();

		List<String> msgs = new LinkedList<String>();
		try {
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(
					ba));
			while (dis.available() != 0) {
				msgs.add(dis.readUTF());
			}
			mLogs = msgs;
		} catch (IOException e) {
		}
	}

	private void store() {
		byte[] ba = new byte[0];

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			for (String msg : mLogs) {
				dos.writeUTF(msg);
			}
			dos.flush();
			ba = baos.toByteArray();
		} catch (IOException e) {
		}
		mConfigurationAccess.setInternNagroidLogs(ba);
	}

}
