package de.schoar.nagroid.nagios.parser;

import java.io.InputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.net.Uri;
import android.util.Log;
import de.schoar.android.helper.http.HTTPDownloader;
import de.schoar.android.helper.http.HTTPDownloaderException;
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
		
		String url = mNagiosSite.getUrlBase() + "/statuswml.cgi?host=" + Uri.encode(nagiosHost.getName()) + "&service=" + Uri.encode(service);
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

			Document doc = getDocument(is);
			
			NodeList nl = doc.getElementsByTagName("td");
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				String value = getNodeValue(n);
				if("Info:".equals(value)) {
					mInfo = getNodeValue(nl.item(i+1));
				}
				else if ("Duration:".equals(value)) {
					mDuration = getNodeValue(nl.item(i+1));
				}
				else if ("Last Check:".equals(value)) {
					mLastCheck = getNodeValue(nl.item(i+1));
				}
			}
			
			NagiosExtState extState = new NagiosExtState(mInfo, mDuration, mLastCheck);
			return extState;
			
		} catch (Exception e) {
			throw new NagiosParsingFailedException(e.getMessage(), e);
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
	
	public String AcknowledgeProblem(Object problemObj, String comment) throws NagiosParsingFailedException {
		
		String url = mNagiosSite.getUrlBase() + "/cmd.cgi";
		String user = mNagiosSite.getUrlUser();
		String pass = mNagiosSite.getUrlPass();
		
		InputStream is;
		String postData = "";
		String cmdRes = "Could not execute command...";
		
		if (comment.equals("")) {
			comment = "no comment";
		}

		postData += "com_author="+Uri.encode(user);
		postData += "&com_data="+Uri.encode(comment);
		postData += "&send_notification=1";
		postData += "&content=wml";
		postData += "&cmd_typ=34";
		postData += "&cmd_mod=2";
		
		if (problemObj.getClass() == NagiosService.class) {
			NagiosService service = (NagiosService) problemObj;
			
			postData += "&host="+Uri.encode(service.getHost().getName());
			postData += "&service="+Uri.encode(service.getName());
		}
		else if (problemObj.getClass() == NagiosHost.class) {
			NagiosHost host = (NagiosHost) problemObj;
			
			postData += "&host="+Uri.encode(host.getName());
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
