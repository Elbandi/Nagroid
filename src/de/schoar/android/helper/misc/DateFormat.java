package de.schoar.android.helper.misc;

public enum DateFormat {
	DATE_FORMAT_US(1), DATE_FORMAT_EURO(2), DATE_FORMAT_ISO8601(3), DATE_FORMAT_STRICT_ISO8601(4);
	private final int df;
	
	DateFormat(int df) {
		this.df = df;
	}
	
	public int getValue() {
		return df;
	}
	
	static public DateFormat toEnum(int i) {
		return values()[i];
	}
	
	static public String toDateFormat(int i) {
		DateFormat df = toEnum(i);
		switch (df) {
		case DATE_FORMAT_EURO:
			return "dd-MM-yyyy HH:mm:ss";
		case DATE_FORMAT_ISO8601:
			return "yyyy-MM-dd HH:mm:ss";
		case DATE_FORMAT_STRICT_ISO8601:
			return "yyyy-MM-ddTHH:mm:ss";
		case DATE_FORMAT_US:
		default:
			return "MM-dd-yyyy HH:mm:ss";
		}
	}
}
