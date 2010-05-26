package de.schoar.nagroid.nagios.parser;

public class NagiosParsingFailedException extends Exception {

	private static final long serialVersionUID = 2723395103522358227L;

	public NagiosParsingFailedException(String msg) {
		super(msg);
	}

	public NagiosParsingFailedException(String msg, Exception e) {
		super(msg, e);
	}

}
