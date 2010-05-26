package de.schoar.android.helper.misc;

public class Millis {
	public static final long SEC = 1000;
	public static final long MIN = SEC * 60;
	public static final long HOUR = MIN * 60;
	public static final long DAY = HOUR * 24;

	public static String msToText(long ms) {
		if (ms > DAY) {
			long u = (ms / DAY);
			if (u == 1) {
				return "1 day";
			}
			return u + " days";
		}
		if (ms > HOUR) {
			long u = (ms / HOUR);
			if (u == 1) {
				return "1 hour";
			}
			return u + " hours";
		}
		if (ms > MIN) {
			long u = (ms / MIN);
			if (u == 1) {
				return "1 minute";
			}
			return u + " minutes";
		}
		if (ms > SEC) {
			long u = (ms / SEC);
			if (u == 1) {
				return "1 second";
			}
			return u + " seconds";
		}
		return "now";
	}
}
