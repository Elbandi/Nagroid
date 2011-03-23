package de.schoar.nagroid.nagios.parser;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.net.Uri;
import android.util.Log;
import de.schoar.android.helper.http.HTTPDownloader;
import de.schoar.android.helper.http.HTTPDownloaderException;
import de.schoar.android.helper.misc.DateFormat;
import de.schoar.nagroid.DM;
import de.schoar.nagroid.nagios.NagiosExtState;
import de.schoar.nagroid.nagios.NagiosHost;
import de.schoar.nagroid.nagios.NagiosService;
import de.schoar.nagroid.nagios.NagiosSite;
import de.schoar.nagroid.nagios.NagiosState;

public class NagiosV2Parser extends NagiosParser {
	private static final String LOGT = "NagiosV2Parser";
	
	public NagiosV2Parser(NagiosSite site) {
		super(site);
	}

	@Override
	public void updateProblems() throws NagiosParsingFailedException {
		mNagiosSite.getHosts().clear();

		String style = mNagiosSite.getUnhandledOnly() ? "uprobs" : "aprobs";
		String url = mNagiosSite.getUrlBase() + "/statuswml.cgi?style=" + style;
		String user = mNagiosSite.getUrlUser();
		String pass = mNagiosSite.getUrlPass();

		InputStream is;

		try {
			is = new HTTPDownloader(url, user, pass).getBodyAsInputStream();
		} catch (HTTPDownloaderException e) {
			throw new NagiosParsingFailedException(e.getMessage(), e);
		}

		try {

			Document doc = getDocument(is);

			boolean foundHost = false;
			boolean foundService = false;
			NodeList nl = doc.getElementsByTagName("b");
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				String value = getNodeValue(n);
				if ("Host Problems:".equals(value)) {
					foundHost = true;
				}
				if ("Svc Problems:".equals(value)) {
					foundService = true;
				}
			}

			if (!foundHost || !foundService) {
				throw new NagiosParsingFailedException(
						"Download is not valid, could not find magic words: "
								+ url);
			}

			nl = doc.getElementsByTagName("anchor");
			for (int i = 0; i < nl.getLength(); i++) {
				Node nAnchor = nl.item(i);
				parseAnchor(nAnchor);
			}
		} catch (Exception e) {
			throw new NagiosParsingFailedException(e.getMessage(), e);
		}
	}
	
	private NagiosExtState getExtState(NagiosHost nagiosHost, String service) throws NagiosParsingFailedException {
		
		String url = mNagiosSite.getUrlBase() + "/statuswml.cgi?host=" + Uri.encode(nagiosHost.getName());
		if (service != null ) url += "&service=" + Uri.encode(service);
		String user = mNagiosSite.getUrlUser();
		String pass = mNagiosSite.getUrlPass();
		
		InputStream is;

		try {
			is = new HTTPDownloader(url, user, pass).getBodyAsInputStream();
		} catch (HTTPDownloaderException e) {
			throw new NagiosParsingFailedException(e.getMessage(), e);
		}

		try {
			String mInfo = "";
			String mDuration = "";
			String mLastCheck = "";
			boolean mChecksDisabled = false;
			boolean mNotificationsDisabled = false;
			boolean mProblemAcknowledged = false;
			boolean mInScheduledDowntime = false;

			Document doc = getDocument(is);
			
			NodeList nl = doc.getElementsByTagName("td");
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				String value = getNodeValue(n);
				if("Info:".equals(value)) {
					mInfo = getNodeValue(nl.item(++i));
				}
				else if ("Duration:".equals(value)) {
					mDuration = getNodeValue(nl.item(++i));
				}
				else if ("Last Check:".equals(value)) {
					mLastCheck = getNodeValue(nl.item(++i));
				} else if ("Properties:".equals(value)) {
					String d = getNodeValue(nl.item(++i));
					if (d == null) continue;
					for (String s : d.split(", ")) {
						if ("Checks disabled".equals(s)) {
							mChecksDisabled = true;
						} else if ("Notifications disabled".equals(s)) {
							mNotificationsDisabled = true;
						} else if ("Problem acknowledged".equals(s)) {
							mProblemAcknowledged = true;
						} else if ("In scheduled downtime".equals(s)) {
							mInScheduledDowntime = true;
						}
					}
				}
			}
			
			NagiosExtState extState = new NagiosExtState(mInfo, mDuration, mLastCheck, mChecksDisabled, mNotificationsDisabled, mProblemAcknowledged, mInScheduledDowntime);
			return extState;
			
		} catch (Exception e) {
			//For now, just return something empty...
			//throw new NagiosParsingFailedException(e.getMessage(), e);
			
			return null;
		}
	}

	private void parseAnchor(Node nAnchor) throws NagiosParsingFailedException {
		String service = null;
		String host = null;

		String state = getNodeValue(nAnchor);
		if (state == null) {
			Log.w(LOGT, "Found an anchor, but it didnt have a value");
			return;
		}
		state = state.trim();

		NodeList nlC1 = nAnchor.getChildNodes();
		if (nlC1 == null) {
			Log.w(LOGT, "Found an anchor, but it didnt have a child");
			return;
		}
		for (int c1 = 0; c1 < nlC1.getLength(); c1++) {
			Node nC1 = nlC1.item(c1);
			NodeList nlC2 = nC1.getChildNodes();
			if (nlC2 == null) {
				Log.w(LOGT, "Found anchors child, but it didnt have childs");
				continue;
			}
			for (int c2 = 0; c2 < nlC2.getLength(); c2++) {
				Node nC2 = nlC2.item(c2);
				if ("postfield".equals(nC2.getNodeName())) {
					String name = NagiosParser.getNodeAttribute(nC2, "name");
					String value = NagiosParser.getNodeAttribute(nC2, "value");
					if (name == null || value == null) {
						continue;
					}
					if ("host".equals(name)) {
						host = value;
					}
					if ("service".equals(name)) {
						service = value;
					}
				}
			}
		}

		// // Create random events, usefull for debugging
		// Random rnd = new Random();
		// if (host != null) {
		// host = host + " xxxxxx " + rnd.nextInt();
		// state = rnd.nextBoolean() ? "DWN" : "UP";
		// }
		//
		// if (service != null) {
		// service = service + " xxxxxx " + rnd.nextInt();
		// state = rnd.nextBoolean() ? "CRI" : "OK";
		// }

		if (host == null) {
			return;
		}

		NagiosHost nh = mNagiosSite.findHost(host);
		if (nh == null) {
			nh = new NagiosHost(mNagiosSite, host, NagiosState.HOST_UP);
		}
		if (service == null) {
			nh.setState(decodeStateHost(state));
			NagiosExtState extState = null;
			if (DM.I.getConfiguration().getPollingExtState()) {
				extState = getExtState(nh, null);
			}
			nh.setExtState(extState);
		}

		if (service != null) {
			NagiosExtState extState = null;
			if (DM.I.getConfiguration().getPollingExtState()) {
				extState = getExtState(nh, service);
			}
			
			@SuppressWarnings("unused")
			NagiosService ns = new NagiosService(nh, service,
					decodeStateService(state), extState);
		}
	}
	
	public static final int EnableChecks = 5;
	public static final int DisableChecks = 6;
	public static final int ScheduleImmediateCheck = 7;
	public static final int ScheduleHostImmediateCheck = 96;
	public static final int ScheduleHostServiceImmediateCheck = 17;
	public static final int EnableAllServiceChecks = 15;
	public static final int DisableAllServiceChecks = 16;
	public static final int EnableNotifications = 22;
	public static final int DisableNotifications = 23;
	public static final int EnableHostNotifications = 24;
	public static final int DisableHostNotifications = 25;
	public static final int EnableAllServiceNotifications = 28;
	public static final int DisableAllServiceNotifications = 29;
	public static final int AcknowledgeHostProblem = 33;
	public static final int AcknowledgeServiceProblem = 34;
	public static final int EnableHostChecks = 47;
	public static final int DisableHostChecks = 48;
	public static final int RemoveAcknowledgeHostProblem = 51;
	public static final int RemoveAcknowledgeServiceProblem = 52;
	public static final int ScheduleHostDowntime = 55;
	public static final int ScheduleDowntime = 56;
	
	
	public String SendCmd(Object problemObj, int type, String CustomData) throws NagiosParsingFailedException {
		String url = mNagiosSite.getUrlBase() + "/cmd.cgi";
		String user = mNagiosSite.getUrlUser();
		String pass = mNagiosSite.getUrlPass();
		
		InputStream is;
		String postData = "";
		String cmdRes = "Could not execute command...";
		
		postData += "cmd_typ=" + type;
		postData += "&cmd_mod=2";
		postData += "&content=wml";
		if (CustomData != null && CustomData.length() > 0)
			postData += "&" + CustomData;
		
		if (problemObj.getClass() == NagiosService.class) {
			NagiosService service = (NagiosService) problemObj;
			postData += "&host=" + Uri.encode(service.getHost().getName());
			postData += "&service=" + Uri.encode(service.getName());
		} else if (problemObj.getClass() == NagiosHost.class) {
			NagiosHost host = (NagiosHost) problemObj;
			postData += "&host=" + Uri.encode(host.getName());
		}
		
		try {
			HTTPDownloader http = new HTTPDownloader(url, user, pass);
			http.setPostData(postData);
			is = http.getBodyAsInputStream();
			
			Document doc = getDocument(is);
			NodeList nl = doc.getElementsByTagName("p");
			
			Node n = nl.item(0);
			cmdRes = getNodeValue(n);
			
		} catch (HTTPDownloaderException e) {
			throw new NagiosParsingFailedException(e.getMessage(), e);
		}

		try {
			
		} catch (Exception e) {
			throw new NagiosParsingFailedException(e.getMessage(), e);
		}
		
		return cmdRes;
		
	}
	
	public String ScheduleImmediateCheck(Object problemObj, int type) throws NagiosParsingFailedException {
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(DateFormat.toDateFormat(DM.I.getConfiguration().getMiscDateFormat()));
		return SendCmd(problemObj, type, "start_time=" + Uri.encode(sdf.format(now).toString()));
	}
	
	public String AcknowledgeProblem(Object problemObj, String comment) throws NagiosParsingFailedException {
		
		String CustomData = "";
		
		if (comment.equals("")) {
			comment = "no comment";
		}
		
		CustomData += "com_author=" + Uri.encode(mNagiosSite.getUrlUser());
		CustomData += "&com_data=" + Uri.encode(comment);
		CustomData += "&send_notification=1";
		
		if (problemObj.getClass() == NagiosService.class) {
			return SendCmd(problemObj, AcknowledgeServiceProblem, CustomData);
		} else if (problemObj.getClass() == NagiosHost.class) {
			return SendCmd(problemObj, AcknowledgeHostProblem, CustomData);
		} else
			return "Could not execute command...";
	}
	
	public String DowntimeProblem(Object problemObj, String snoozeTime, String comment) throws NagiosParsingFailedException {
		
		String CustomData = "";
		
		if (comment.equals("")) {
			comment = "no comment";
		}
		
		CustomData += "com_author=" + Uri.encode(mNagiosSite.getUrlUser());
		CustomData += "&com_data=" + Uri.encode(comment);
		
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(DateFormat.toDateFormat(DM.I.getConfiguration().getMiscDateFormat()));
		
		CustomData += "&start_time=" + Uri.encode(sdf.format(now).toString());
		CustomData += "&end_time=" + Uri.encode(snoozeTime);
		
		CustomData += "&fixed=1";

		if (problemObj.getClass() == NagiosService.class) {
			return SendCmd(problemObj, ScheduleDowntime, CustomData);
		} else if (problemObj.getClass() == NagiosHost.class) {
			return SendCmd(problemObj, ScheduleHostDowntime, CustomData);
		} else
			return "Could not execute command...";
	}
	
	private NagiosState decodeStateHost(String state) {
		
		if (state == null) {
			return NagiosState.HOST_LOCAL_ERROR;
		}
		
		String s = state.trim().toUpperCase();
		
		if ("UP".equals(s)) {
			return NagiosState.HOST_UP;
		}
		if ("UNR".equals(s)) {
			return NagiosState.HOST_UNREACHABLE;
		}
		if ("DWN".equals(s)) {
			return NagiosState.HOST_DOWN;
		}
		if ("PND".equals(s)) {
			return NagiosState.HOST_PENDING;
		}

		return NagiosState.HOST_LOCAL_ERROR;
	}

	private NagiosState decodeStateService(String state) {

		if (state == null) {
			return NagiosState.SERVICE_LOCAL_ERROR;
		}

		String s = state.trim().toUpperCase();

		if ("OK".equals(s)) {
			return NagiosState.SERVICE_OK;
		}
		if ("WRN".equals(s)) {
			return NagiosState.SERVICE_WARNING;
		}
		if ("CRI".equals(s)) {
			return NagiosState.SERVICE_CRITICAL;
		}
		if ("UNK".equals(s)) {
			return NagiosState.SERVICE_UNKNOWN;
		}
		if ("PND".equals(s)) {
			return NagiosState.SERVICE_PENDING;
		}

		return NagiosState.SERVICE_LOCAL_ERROR;
	}
}
