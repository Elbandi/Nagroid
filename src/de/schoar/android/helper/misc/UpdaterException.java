package de.schoar.android.helper.misc;

public class UpdaterException extends Exception {

	private static final long serialVersionUID = -2203272139390693384L;

	public UpdaterException(String msg) {
		super(msg);
	}

	public UpdaterException(String msg, Exception e) {
		super(msg, e);
	}

}
