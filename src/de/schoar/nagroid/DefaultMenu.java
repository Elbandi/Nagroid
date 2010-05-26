package de.schoar.nagroid;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;
import de.schoar.nagroid.dialog.AboutDialog;
import de.schoar.nagroid.dialog.ServiceDialog;

public class DefaultMenu {
	private static final int MENU_ABOUT = 1;
	private static final int MENU_CONFIGURATION = 2;
	private static final int MENU_NAGIOS = 3;
	private static final int MENU_REFRESH = 4;
	private static final int MENU_HELP = 5;
	private static final int MENU_LOG = 6;
	private static final int MENU_ENDISABLE_SERVICE = 7;

	public static void addAbout(Menu menu) {
		menu.add(0, MENU_ABOUT, 0, "About").setIcon(
				android.R.drawable.ic_menu_info_details);
	}

	public static void addConfiguration(Menu menu) {
		menu.add(0, MENU_CONFIGURATION, 0, "Configuration").setIcon(
				android.R.drawable.ic_menu_preferences);
	}

	public static void addNagios(Menu menu) {
		menu.add(0, MENU_NAGIOS, 0, "My Nagios").setIcon(
				android.R.drawable.ic_menu_view);
	}

	public static void addRefresh(Menu menu) {
		menu.add(0, MENU_REFRESH, 0, "Refresh").setIcon(
				android.R.drawable.ic_menu_rotate);
	}

	public static void addHelp(Menu menu) {
		menu.add(0, MENU_HELP, 0, "Help").setIcon(
				android.R.drawable.ic_menu_help);
	}

	public static void addLog(Menu menu) {
		menu.add(0, MENU_LOG, 0, "Log").setIcon(
				android.R.drawable.ic_menu_agenda);
	}

	public static void addEnDisableService(Menu menu) {
		menu.add(0, MENU_ENDISABLE_SERVICE, 0, "En/Disable Service").setIcon(
				android.R.drawable.ic_menu_recent_history);
	}

	public static boolean onOptionsItemSelected(MenuItem item,
			final Activity act) {
		int itemid = item.getItemId();

		if (MENU_ABOUT == itemid) {
			new AboutDialog(act).show();
			return true;
		}

		if (MENU_NAGIOS == itemid) {
			Uri uri = Uri.parse(DM.I.getConfiguration().getNagiosUrl()
					+ "/status.cgi?host=all&servicestatustypes=28");
			act.startActivity(new Intent(Intent.ACTION_VIEW, uri));
			return true;
		}

		if (MENU_CONFIGURATION == itemid) {
			Intent intConfiguration = new Intent();
			intConfiguration.setClassName("de.schoar.nagroid",
					"de.schoar.nagroid.activity.ConfigurationActivity");
			act.startActivity(intConfiguration);

			return true;
		}

		if (MENU_REFRESH == itemid) {
			DM.I.getPollHandler().poll();
		}

		if (MENU_HELP == itemid) {
			Uri uri = Uri.parse("http://android.schoar.de/nagroid/help/#"
					+ act.getClass().getSimpleName());
			act.startActivity(new Intent(Intent.ACTION_VIEW, uri));
			return true;
		}

		if (MENU_LOG == itemid) {
			Intent intConfiguration = new Intent();
			intConfiguration.setClassName("de.schoar.nagroid",
					"de.schoar.nagroid.activity.LogActivity");
			act.startActivity(intConfiguration);
		}

		if (MENU_ENDISABLE_SERVICE == itemid) {
			new ServiceDialog(act).show();
		}

		return false;
	}
}
