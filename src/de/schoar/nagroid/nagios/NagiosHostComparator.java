package de.schoar.nagroid.nagios;

import java.util.Comparator;

public class NagiosHostComparator implements Comparator<NagiosHost> {

	@Override
	public int compare(NagiosHost nh1, NagiosHost nh2) {
		// if (nh1.getServices().size() > 0 && nh2.getServices().size() == 0) {
		// return 1;
		// }
		// if (nh1.getServices().size() == 0 && nh2.getServices().size() > 0) {
		// return -1;
		// }
		return nh1.getName().toLowerCase().compareTo(
				nh2.getName().toLowerCase());
	}
}
