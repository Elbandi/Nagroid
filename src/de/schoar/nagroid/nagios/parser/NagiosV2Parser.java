package de.schoar.nagroid.nagios.parser;

import java.io.InputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;
import de.schoar.android.helper.http.HTTPDownloader;
import de.schoar.android.helper.http.HTTPDownloaderException;
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

	private void parseAnchor(Node nAnchor) {
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
			@SuppressWarnings("unused")
			NagiosService ns = new NagiosService(nh, service,
					decodeStateService(state));
		}
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
