package de.schoar.android.helper.http;

public class HTTPDownloaderException extends Exception {

	private static final long serialVersionUID = 1075140254227912426L;

	public HTTPDownloaderException(String msg) {
		super(msg);
	}

	public HTTPDownloaderException(String msg, Exception e) {
		super(msg, e);
	}

}
