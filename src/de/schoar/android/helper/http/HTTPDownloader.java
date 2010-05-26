package de.schoar.android.helper.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import de.schoar.android.helper.misc.StreamUtils;
import de.schoar.extern.Base64;

public class HTTPDownloader {
	private String mUrl;
	private String mUser = null;
	private String mPass = null;

	public HTTPDownloader(String url) {
		mUrl = url;
	}

	public HTTPDownloader(String url, String user, String pass) {
		this(url);
		mUser = user;
		mPass = pass;
	}

	public InputStream getBodyAsInputStream() throws HTTPDownloaderException {
		return getBodyAsInputStream(true);
	}

	private InputStream getBodyAsInputStream(boolean retryOnceSSLFix)
			throws HTTPDownloaderException {
		URL url;
		try {
			url = URI.create(mUrl).toURL();
		} catch (Exception e) {
			throw new HTTPDownloaderException("Invalid URL (" + e.getMessage()
					+ "): " + mUrl);
		}

		HttpURLConnection.setFollowRedirects(true);
		HttpURLConnection huc;
		try {
			huc = (HttpURLConnection) url.openConnection();
		} catch (Exception e) {
			throw new HTTPDownloaderException("Could not open URL (" + mUrl
					+ "): " + e.getMessage());
		}

		if (mUser != null && mPass != null) {
			String credential = Base64.encodeString(mUser + ":" + mPass);
			huc.setRequestProperty("Authorization", "Basic " + credential);
		}

		try {
			huc.connect();

			int status = huc.getResponseCode();
			if (retryOnceSSLFix && status == -1) {
				return getBodyAsInputStream(false);
			}
			if (status != 200) {
				throw new HTTPDownloaderException("Response code is not 200: "
						+ status);
			}

			byte[] ba = StreamUtils.readstreamFully(huc.getInputStream());

			huc.disconnect();

			return new ByteArrayInputStream(ba);

		} catch (Exception e) {
			throw new HTTPDownloaderException("Could not read from URL ("
					+ mUrl + "): " + e.getMessage());
		}

	}

	public byte[] getBodyAsBytes() throws HTTPDownloaderException {
		return StreamUtils.readstreamFully(getBodyAsInputStream());
	}

	public String getBodyAsString() throws HTTPDownloaderException {
		return new String(getBodyAsBytes());
	}

	public String getBodyAsString(String encoding)
			throws HTTPDownloaderException, UnsupportedEncodingException {
		return new String(getBodyAsBytes(), encoding);
	}
}
