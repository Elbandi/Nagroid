package de.schoar.nagroid.nagios;

import android.graphics.Color;

public enum NagiosState {
	SERVICE_CRITICAL, SERVICE_WARNING, SERVICE_UNKNOWN, SERVICE_PENDING, SERVICE_LOCAL_ERROR, SERVICE_OK, HOST_UNREACHABLE, HOST_DOWN, HOST_PENDING, HOST_LOCAL_ERROR, HOST_UP;
	// NOT IN WML: SERVICE_RECOVERY

	public int toColor() {
		return Color.parseColor(toColorStr());
	}

	public String toColorStrNoHash() {
		return toColorStr().replace("#", "");
	}

	public String toColorStr() {
		if (SERVICE_CRITICAL.equals(this)) {
			return "#F83838";
		}
		if (SERVICE_WARNING.equals(this)) {
			return "#FFFF00";
		}
		if (SERVICE_UNKNOWN.equals(this)) {
			return "#FF9900";
		}
		// if (SERVICE_RECOVERY.equals(this)) {
		// return "#33FF00";
		// }
		if (SERVICE_PENDING.equals(this)) {
			return "#ACACAC";
		}
		if (SERVICE_OK.equals(this)) {
			return "#33FF00";
		}

		if (HOST_UNREACHABLE.equals(this)) {
			return "#F83838";
		}
		if (HOST_DOWN.equals(this)) {
			return "#F83838";
		}
		if (HOST_PENDING.equals(this)) {
			return "#ACACAC";
		}
		if (HOST_UP.equals(this)) {
			return "#33FF00";
		}

		return "#1212cd";
	}

	public String toShort() {
		if (SERVICE_CRITICAL.equals(this)) {
			return "CRI";
		}
		if (SERVICE_WARNING.equals(this)) {
			return "WRN";
		}
		if (SERVICE_UNKNOWN.equals(this)) {
			return "UNK";
		}
		// if (SERVICE_RECOVERY.equals(this)) {
		// return "REC";
		// }
		if (SERVICE_PENDING.equals(this)) {
			return "PND";
		}
		if (SERVICE_OK.equals(this)) {
			return "OK";
		}

		if (HOST_UNREACHABLE.equals(this)) {
			return "UNR";
		}
		if (HOST_DOWN.equals(this)) {
			return "DWN";
		}
		if (HOST_PENDING.equals(this)) {
			return "PND";
		}
		if (HOST_UP.equals(this)) {
			return "UP";
		}

		return "LOC";
	}

	// .statusPENDING { #ACACAC; }
	// .statusOK { #33FF00; }
	// .statusRECOVERY { #33FF00; }
	// .statusUNKNOWN { #FF9900; }
	// .statusWARNING { #FFFF00; }
	// .statusCRITICAL { #F83838; }
	//
	// .statusHOSTPENDING { #ACACAC; }
	// .statusHOSTUP { #33FF00; }
	// .statusHOSTDOWN { #F83838; }
	// .statusHOSTDOWNACK { #F83838; }
	// .statusHOSTDOWNSCHED { #F83838; }
	// .statusHOSTUNREACHABLE { #F83838; }
	// .statusHOSTUNREACHABLEACK { #F83838; }
	// .statusHOSTUNREACHABLESCHED { #F83838; }

}
