package de.schoar.nagroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import de.schoar.android.helper.http.SSLSelfSigned;
import de.schoar.extern.Base64;
import de.schoar.nagroid.nagios.NagiosSite;
import de.schoar.nagroid.service.PollReceiver;

public class ConfigurationAccess {
	private static final String CONFIG_PREFERENCE = "de.schoar.nagroid";
	private static final String CONFIG_INTERN = "de.schoar.nagroid.basic";

	public static final String NAGIOS_URL = "configuration_nagios_url";
	public static final String NAGIOS_SELF_SIGNED = "configuration_nagios_self_signed";
	public static final String NAGIOS_HTTP_BASIC_AUTH = "configuration_nagios_http_basic_auth";
	public static final String NAGIOS_HTTP_BASIC_AUTH_USERNAME = "configuration_nagios_http_basic_auth_username";
	public static final String NAGIOS_HTTP_BASIC_AUTH_PASSWORD = "configuration_nagios_http_basic_auth_password";

	public static final String NOTIFICATION_UNHANDLED_ONLY = "configuration_notification_unhandled_only";
	public static final String NOTIFICATION_VIBRATE = "configuration_notification_vibrate";
	public static final String NOTIFICATION_HIDE_IF_OK = "configuration_notification_hide_if_ok";

	public static final String NOTIFICATION_ALARM_ENABLED = "configuration_notification_alarm_enabled";
	public static final String NOTIFICATION_ALARM_WARNING = "configuration_notification_alarm_warning";
	public static final String NOTIFICATION_ALARM_CRITICAL = "configuration_notification_alarm_critical";
	public static final String NOTIFICATION_ALARM_DOWN_UNREACHABLE = "configuration_notification_alarm_down_unreachable";

	public static final String POLLING_INTERVAL = "configuration_polling_interval";
	public static final String POLLING_ENABLED = "configuration_polling_enabled";
	public static final String POLLING_EXTSTATE = "configuration_polling_extstate";

	public static final String MISC_AUTOSTART = "configuration_misc_autostart";
	public static final String MISC_SNOOZETIME = "configuration_misc_snoozetime";

	private static final String INTERN_LAST_POLL_TIME = "intern_last_poll_time";
	private static final String INTERN_LAST_POLL_SUCCESSFULL = "intern_last_poll_successfull";
	private static final String INTERN_LAST_POLL_TIME_SUCCESSFULL = "intern_last_poll_time_successfull";
	private static final String INTERN_LOGS = "intern_logs";

	private SharedPreferences mSharedPreferencesPreference = null;
	private SharedPreferences mSharedPreferencesIntern = null;

	public ConfigurationAccess(final Context ctx) {
		// PreferenceManager.setDefaultValues(ctx, R.xml.configuration, false);

		mSharedPreferencesPreference = PreferenceManager
				.getDefaultSharedPreferences(ctx.getApplicationContext());

		mSharedPreferencesPreference = ctx.getApplicationContext()
				.getSharedPreferences(CONFIG_PREFERENCE, Context.MODE_PRIVATE);

		mSharedPreferencesIntern = ctx.getApplicationContext()
				.getSharedPreferences(CONFIG_INTERN, Context.MODE_PRIVATE);
	}

	public synchronized String getNagiosUrl() {
		return mSharedPreferencesPreference.getString(NAGIOS_URL,
				"http://android.schoar.de/cgi-bin/nagios2");
	}

	public synchronized void setNagiosUrl(String url) {
		if (url != null && url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}

		mSharedPreferencesPreference.edit().putString(NAGIOS_URL, url).commit();
	}

	public synchronized boolean getNagiosSelfSigned() {
		return mSharedPreferencesPreference.getBoolean(NAGIOS_SELF_SIGNED,
				false);
	}

	public synchronized void setNagiosSelfSigned(boolean bool) {
		mSharedPreferencesPreference.edit()
				.putBoolean(NAGIOS_SELF_SIGNED, bool).commit();
		if (bool) {
			SSLSelfSigned.gi().enable();
		} else {
			SSLSelfSigned.gi().disable();
		}
	}

	public synchronized boolean getNagiosHttpBasicAuth() {
		return mSharedPreferencesPreference.getBoolean(NAGIOS_HTTP_BASIC_AUTH,
				false);
	}

	public synchronized void setNagiosHttpBasicAuth(boolean bool) {
		mSharedPreferencesPreference.edit().putBoolean(NAGIOS_HTTP_BASIC_AUTH,
				bool).commit();
	}

	public synchronized String getNagiosHttpBasicAuthUsername() {
		return mSharedPreferencesPreference.getString(
				NAGIOS_HTTP_BASIC_AUTH_USERNAME, "");
	}

	public synchronized void setNagiosHttpBasicAuthUsername(String user) {
		mSharedPreferencesPreference.edit().putString(
				NAGIOS_HTTP_BASIC_AUTH_USERNAME, user).commit();
	}

	public synchronized String getNagiosHttpBasicAuthPassword() {
		return mSharedPreferencesPreference.getString(
				NAGIOS_HTTP_BASIC_AUTH_PASSWORD, "");
	}

	public synchronized void setNagiosHttpBasicAuthPassword(String pass) {
		mSharedPreferencesPreference.edit().putString(
				NAGIOS_HTTP_BASIC_AUTH_PASSWORD, pass).commit();
	}

	public synchronized long getPollingInterval() {
		return mSharedPreferencesPreference.getLong(POLLING_INTERVAL, 600000);
	}

	public synchronized void setPollingInterval(long ms, Context ctx) {
		mSharedPreferencesPreference.edit().putLong(POLLING_INTERVAL, ms)
				.commit();
		PollReceiver.reschedule(ctx);
	}

	public synchronized boolean getPollingEnabled() {
		return mSharedPreferencesIntern.getBoolean(POLLING_ENABLED, true);
	}
	
	public synchronized boolean getPollingExtState() {
		return mSharedPreferencesPreference.getBoolean(POLLING_EXTSTATE, false);
	}
	
	public synchronized void setPollingExtState(boolean enable) {
		mSharedPreferencesPreference.edit()
				.putBoolean(POLLING_EXTSTATE, enable).commit();
	}

	public synchronized void setPollingEnabled(boolean enabled, Context ctx) {
		mSharedPreferencesIntern.edit()
				.putBoolean(POLLING_ENABLED, enabled).commit();
		if (enabled) {			
			PollReceiver.reschedule(ctx);
			DM.I.getHealthNotificationHelper().showLast(ctx);
		} else {			
			DM.I.getHealthNotificationHelper().clear();
			PollReceiver.stop(ctx);			
		}
	}

	public synchronized boolean getNotificationUnhandledOnly() {
		return mSharedPreferencesPreference.getBoolean(
				NOTIFICATION_UNHANDLED_ONLY, true);
	}

	public synchronized void setNotificationUnhandledOnly(boolean bool) {
		mSharedPreferencesPreference.edit().putBoolean(
				NOTIFICATION_UNHANDLED_ONLY, bool).commit();
	}

	public synchronized boolean getNotificationVibrate() {
		return mSharedPreferencesPreference.getBoolean(NOTIFICATION_VIBRATE,
				true);
	}

	public synchronized void setNotificationVibrate(boolean bool) {
		mSharedPreferencesPreference.edit().putBoolean(NOTIFICATION_VIBRATE,
				bool).commit();
	}

	public synchronized boolean getNotificationHideIfOk() {
		return mSharedPreferencesPreference.getBoolean(NOTIFICATION_HIDE_IF_OK,
				false);
	}

	public synchronized void setNotificationHideIfOk(boolean bool, Context ctx) {
		mSharedPreferencesPreference.edit().putBoolean(NOTIFICATION_HIDE_IF_OK,
				bool).commit();
		DM.I.getHealthNotificationHelper().showLast(ctx);
	}

	public synchronized boolean getNotificationAlarmEnabled() {
		return mSharedPreferencesPreference.getBoolean(
				NOTIFICATION_ALARM_ENABLED, false);
	}

	public synchronized void setNotificationAlarmEnabled(boolean bool) {
		mSharedPreferencesPreference.edit().putBoolean(
				NOTIFICATION_ALARM_ENABLED, bool).commit();
	}

	public synchronized String getNotificationAlarmWarning() {
		return mSharedPreferencesPreference.getString(
				NOTIFICATION_ALARM_WARNING, "");
	}

	public synchronized void setNotificationAlarmWarning(String uri) {
		mSharedPreferencesPreference.edit().putString(
				NOTIFICATION_ALARM_WARNING, uri).commit();
	}

	public synchronized String getNotificationAlarmCritical() {
		return mSharedPreferencesPreference.getString(
				NOTIFICATION_ALARM_CRITICAL,
				Settings.System.DEFAULT_RINGTONE_URI.toString());
	}

	public synchronized void setNotificationAlarmCritical(String uri) {
		mSharedPreferencesPreference.edit().putString(
				NOTIFICATION_ALARM_CRITICAL, uri).commit();
	}

	public synchronized String getNotificationAlarmDownUnreachable() {
		return mSharedPreferencesPreference.getString(
				NOTIFICATION_ALARM_DOWN_UNREACHABLE,
				Settings.System.DEFAULT_RINGTONE_URI.toString());
	}

	public synchronized void setNotificationAlarmDownUnreachable(String uri) {
		mSharedPreferencesPreference.edit().putString(
				NOTIFICATION_ALARM_DOWN_UNREACHABLE, uri).commit();
	}

	public synchronized boolean getMiscAutostart() {
		return mSharedPreferencesPreference.getBoolean(MISC_AUTOSTART, true);
	}

	public synchronized void setMiscAutostart(boolean bool) {
		mSharedPreferencesPreference.edit().putBoolean(MISC_AUTOSTART, bool)
				.commit();
	}

	public synchronized long getInternLastPollTime() {
		return mSharedPreferencesIntern.getLong(INTERN_LAST_POLL_TIME, 0);
	}

	public synchronized void setInternLastPollTime(long last) {
		mSharedPreferencesIntern.edit().putLong(INTERN_LAST_POLL_TIME, last)
				.commit();
	}

	public synchronized boolean getInternLastPollSuccessfull() {
		return mSharedPreferencesIntern.getBoolean(
				INTERN_LAST_POLL_SUCCESSFULL, true);
	}

	public synchronized void setInternLastPollSuccessfull(boolean last) {
		mSharedPreferencesIntern.edit().putBoolean(
				INTERN_LAST_POLL_SUCCESSFULL, last).commit();
	}

	public synchronized long getInternLastPollTimeSuccessfull() {
		return mSharedPreferencesIntern.getLong(
				INTERN_LAST_POLL_TIME_SUCCESSFULL, 0);
	}

	public synchronized void setInternLastPollTimeSuccessfull(long last) {
		mSharedPreferencesIntern.edit().putLong(
				INTERN_LAST_POLL_TIME_SUCCESSFULL, last).commit();
	}

	public synchronized byte[] getInternNagroidLogs() {
		String str = mSharedPreferencesIntern.getString(INTERN_LOGS, "");
		try {
			return Base64.decode(str);
		} catch (Exception e) {
			return new byte[0];
		}
	}

	public synchronized void setInternNagroidLogs(byte[] ba) {
		String str = "";
		try {
			str = String.valueOf(Base64.encode(ba));
		} catch (Exception e) {
		}
		mSharedPreferencesIntern.edit().putString(INTERN_LOGS, str).commit();
	}

	public NagiosSite getNagiosSite() {
		String urlbase = getNagiosUrl();
		boolean httpbasicauth = getNagiosHttpBasicAuth();
		String user = getNagiosHttpBasicAuthUsername();
		String pass = getNagiosHttpBasicAuthPassword();
		boolean unhandledOnly = getNotificationUnhandledOnly();

		if (!httpbasicauth) {
			return new NagiosSite(urlbase, unhandledOnly);
		}
		return new NagiosSite(urlbase, user, pass, unhandledOnly);
	}
	
	public synchronized void setSnoozeTime(String snoozeTime) {
		mSharedPreferencesPreference.edit().putString(
				MISC_SNOOZETIME, snoozeTime).commit();
	}
	public synchronized String getSnoozeTime() {
		return mSharedPreferencesPreference.getString(
				MISC_SNOOZETIME, "9:00");
	}

}
