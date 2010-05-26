package de.schoar.nagroid.nagios;

import java.util.Comparator;

public class NagiosServiceComparator implements Comparator<NagiosService> {

	@Override
	public int compare(NagiosService ns1, NagiosService ns2) {
		return ns1.getName().toLowerCase().compareTo(
				ns2.getName().toLowerCase());
	}

}
