package de.schoar.nagroid.activity;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.RingtonePreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import de.schoar.android.helper.misc.DateFormat;
import de.schoar.nagroid.ConfigurationAccess;
import de.schoar.nagroid.DM;
import de.schoar.nagroid.DefaultMenu;
import de.schoar.nagroid.R;

public class ConfigurationActivity extends PreferenceActivity {
	private static final String LOGT = "ConfigurationActivity";
	private static final String UNSET = "(unset)";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		DM.register(this);

		addPreferencesFromResource(R.xml.configuration);

		init(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DM.unregister(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		DM.unregister(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		init(this);
		DM.register(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		DefaultMenu.addRefresh(menu);
		DefaultMenu.addNagios(menu);
		DefaultMenu.addAbout(menu);
		DefaultMenu.addLog(menu);
		DefaultMenu.addHelp(menu);
		DefaultMenu.addEnDisableService(menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return DefaultMenu.onOptionsItemSelected(item, this);
	}

	private void init(final Context ctx) {
		final EditTextPreference etpNagiosUrl = (EditTextPreference) findPreference(ConfigurationAccess.NAGIOS_URL);
		etpNagiosUrl.setSummary(textOrUnset(DM.I.getConfiguration()
				.getNagiosUrl()));
		etpNagiosUrl.setText(DM.I.getConfiguration().getNagiosUrl());

		etpNagiosUrl
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference pref,
							Object obj) {
						DM.I.getConfiguration().setNagiosUrl(obj.toString());
						etpNagiosUrl.setSummary(textOrUnset(obj.toString()));
						etpNagiosUrl.setText(obj.toString());
						return false;
					}
				});

		// -----

		final CheckBoxPreference cbpNagiosSelfSigned = (CheckBoxPreference) findPreference(ConfigurationAccess.NAGIOS_SELF_SIGNED);
		boolean cbpNagiosSelfSignedValue = DM.I.getConfiguration()
				.getNagiosSelfSigned();
		cbpNagiosSelfSigned.setChecked(cbpNagiosSelfSignedValue);

		cbpNagiosSelfSigned
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference pref,
							Object obj) {
						Boolean bool = (Boolean) obj;
						DM.I.getConfiguration().setNagiosSelfSigned(bool);
						cbpNagiosSelfSigned.setChecked(bool);
						if (bool) {
							Toast
									.makeText(
											getApplicationContext(),
											"WARNING: Man-In-The-Middle attacks possible!",
											Toast.LENGTH_LONG).show();
						}
						return false;
					}
				});

		// -----

		final EditTextPreference etpNagiosHttpBasicAuthUsername = (EditTextPreference) findPreference(ConfigurationAccess.NAGIOS_HTTP_BASIC_AUTH_USERNAME);
		String etpNagiosHttpBasicAuthUsernameValue = DM.I.getConfiguration()
				.getNagiosHttpBasicAuthUsername();
		etpNagiosHttpBasicAuthUsername
				.setSummary(textOrUnset(etpNagiosHttpBasicAuthUsernameValue));
		etpNagiosHttpBasicAuthUsername
				.setText(etpNagiosHttpBasicAuthUsernameValue);

		etpNagiosHttpBasicAuthUsername
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference pref,
							Object obj) {
						DM.I.getConfiguration().setNagiosHttpBasicAuthUsername(
								obj.toString());
						etpNagiosHttpBasicAuthUsername
								.setSummary(textOrUnset(obj.toString()));
						etpNagiosHttpBasicAuthUsername.setText(obj.toString());

						return false;
					}
				});

		// -----

		final EditTextPreference etpNagiosHttpBasicAuthPassword = (EditTextPreference) findPreference(ConfigurationAccess.NAGIOS_HTTP_BASIC_AUTH_PASSWORD);
		String etpNagiosHttpBasicAuthPasswordValue = DM.I.getConfiguration()
				.getNagiosHttpBasicAuthPassword();
		etpNagiosHttpBasicAuthPassword
				.setSummary(passOrUnset(etpNagiosHttpBasicAuthPasswordValue));
		etpNagiosHttpBasicAuthPassword
				.setText(etpNagiosHttpBasicAuthPasswordValue);

		etpNagiosHttpBasicAuthPassword
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference pref,
							Object obj) {
						DM.I.getConfiguration().setNagiosHttpBasicAuthPassword(
								obj.toString());
						etpNagiosHttpBasicAuthPassword
								.setSummary(passOrUnset(obj.toString()));
						etpNagiosHttpBasicAuthPassword.setText(obj.toString());
						return false;
					}
				});

		// -----

		final CheckBoxPreference cbpNagiosHttpBasicAuth = (CheckBoxPreference) findPreference(ConfigurationAccess.NAGIOS_HTTP_BASIC_AUTH);
		boolean cpbNagiosHttpBasicAuthChecked = DM.I.getConfiguration()
				.getNagiosHttpBasicAuth();
		cbpNagiosHttpBasicAuth.setChecked(cpbNagiosHttpBasicAuthChecked);

		etpNagiosHttpBasicAuthUsername
				.setEnabled(cpbNagiosHttpBasicAuthChecked);
		etpNagiosHttpBasicAuthPassword
				.setEnabled(cpbNagiosHttpBasicAuthChecked);

		cbpNagiosHttpBasicAuth
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference pref,
							Object obj) {
						Boolean bool = (Boolean) obj;
						DM.I.getConfiguration().setNagiosHttpBasicAuth(bool);
						cbpNagiosHttpBasicAuth.setChecked(bool);
						etpNagiosHttpBasicAuthUsername.setEnabled(bool);
						etpNagiosHttpBasicAuthPassword.setEnabled(bool);
						return false;
					}
				});

		// -----

		final ListPreference lpPollingInterval = (ListPreference) findPreference(ConfigurationAccess.POLLING_INTERVAL);

		setListSummary(lpPollingInterval, String.valueOf(DM.I
				.getConfiguration().getPollingInterval()));

		lpPollingInterval
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference pref,
							Object obj) {

						long ms = Long.valueOf(obj.toString()).longValue();

						setListSummary(lpPollingInterval, String.valueOf(ms));

						DM.I.getConfiguration().setPollingInterval(ms,
								getApplicationContext());
						return false;
					}
				});
		
		final CheckBoxPreference cbpPollingExtState = (CheckBoxPreference) findPreference(ConfigurationAccess.POLLING_EXTSTATE);
		boolean cbpPollingExtStateValue = DM.I.getConfiguration()
				.getPollingExtState();
		cbpPollingExtState
				.setChecked(cbpPollingExtStateValue);

		cbpPollingExtState
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference pref,
							Object obj) {
						Boolean bool = (Boolean) obj;
						DM.I.getConfiguration().setPollingExtState(
								bool);
						cbpPollingExtState.setChecked(bool);
						return false;
					}
				});

		// -----

		final CheckBoxPreference cbpNotificationUnhandledOnly = (CheckBoxPreference) findPreference(ConfigurationAccess.NOTIFICATION_UNHANDLED_ONLY);
		boolean cbpNotificationUnhandledOnlyValue = DM.I.getConfiguration()
				.getNotificationUnhandledOnly();
		cbpNotificationUnhandledOnly
				.setChecked(cbpNotificationUnhandledOnlyValue);

		cbpNotificationUnhandledOnly
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference pref,
							Object obj) {
						Boolean bool = (Boolean) obj;
						DM.I.getConfiguration().setNotificationUnhandledOnly(
								bool);
						cbpNotificationUnhandledOnly.setChecked(bool);
						return false;
					}
				});

		final CheckBoxPreference cbpNotificationVibrate = (CheckBoxPreference) findPreference(ConfigurationAccess.NOTIFICATION_VIBRATE);
		boolean cbpNotificationVibrateValue = DM.I.getConfiguration()
				.getNotificationVibrate();
		cbpNotificationVibrate.setChecked(cbpNotificationVibrateValue);

		cbpNotificationVibrate
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference pref,
							Object obj) {
						Boolean bool = (Boolean) obj;
						DM.I.getConfiguration().setNotificationVibrate(bool);
						cbpNotificationVibrate.setChecked(bool);
						return false;
					}
				});

		// -----

		final CheckBoxPreference cbpNotificationHideIfOk = (CheckBoxPreference) findPreference(ConfigurationAccess.NOTIFICATION_HIDE_IF_OK);
		boolean cbpNotificationHideIfOkValue = DM.I.getConfiguration()
				.getNotificationHideIfOk();
		cbpNotificationHideIfOk.setChecked(cbpNotificationHideIfOkValue);

		cbpNotificationHideIfOk
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference pref,
							Object obj) {
						Boolean bool = (Boolean) obj;
						DM.I.getConfiguration().setNotificationHideIfOk(bool,
								ctx);
						cbpNotificationHideIfOk.setChecked(bool);
						return false;
					}
				});

		// -----

		final RingtonePreference rpWarning = (RingtonePreference) findPreference(ConfigurationAccess.NOTIFICATION_ALARM_WARNING);
		String rpWarningUri = DM.I.getConfiguration()
				.getNotificationAlarmWarning();
		rpWarning.setSummary(alarmText(rpWarningUri, "warning"));
		rpWarning
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference pref,
							Object obj) {
						String uristr = obj.toString();
						DM.I.getConfiguration().setNotificationAlarmWarning(
								uristr);
						rpWarning.setSummary(alarmText(uristr, "warning"));
						return false;
					}
				});

		// -----

		final RingtonePreference rpCritical = (RingtonePreference) findPreference(ConfigurationAccess.NOTIFICATION_ALARM_CRITICAL);
		String rpCriticalUri = DM.I.getConfiguration()
				.getNotificationAlarmCritical();
		rpCritical.setSummary(alarmText(rpCriticalUri, "critical"));
		rpCritical
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference pref,
							Object obj) {
						String uristr = obj.toString();
						DM.I.getConfiguration().setNotificationAlarmCritical(
								uristr);
						rpCritical.setSummary(alarmText(uristr, "critical"));
						return false;
					}
				});

		// -----

		final RingtonePreference rpDownUnreachable = (RingtonePreference) findPreference(ConfigurationAccess.NOTIFICATION_ALARM_DOWN_UNREACHABLE);
		String rpDownUnreachableUri = DM.I.getConfiguration()
				.getNotificationAlarmDownUnreachable();
		rpDownUnreachable.setSummary(alarmText(rpDownUnreachableUri,
				"down/unreachable"));
		rpDownUnreachable
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference pref,
							Object obj) {
						String uristr = obj.toString();
						DM.I.getConfiguration()
								.setNotificationAlarmDownUnreachable(uristr);
						rpDownUnreachable.setSummary(alarmText(uristr,
								"down/unreachable"));
						return false;
					}
				});

		// -----

		final CheckBoxPreference cbpNotificationAlarmEnabled = (CheckBoxPreference) findPreference(ConfigurationAccess.NOTIFICATION_ALARM_ENABLED);
		boolean cbpNotificationAlarmEnabledChecked = DM.I.getConfiguration()
				.getNotificationAlarmEnabled();
		cbpNotificationAlarmEnabled
				.setChecked(cbpNotificationAlarmEnabledChecked);

		rpWarning.setEnabled(cbpNotificationAlarmEnabledChecked);
		rpCritical.setEnabled(cbpNotificationAlarmEnabledChecked);
		rpDownUnreachable.setEnabled(cbpNotificationAlarmEnabledChecked);

		cbpNotificationAlarmEnabled
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference pref,
							Object obj) {
						Boolean bool = (Boolean) obj;
						DM.I.getConfiguration().setNotificationAlarmEnabled(
								bool);
						rpWarning.setEnabled(bool);
						rpCritical.setEnabled(bool);
						rpDownUnreachable.setEnabled(bool);
						cbpNotificationAlarmEnabled.setChecked(bool);
						return false;
					}
				});

		// -----

		final CheckBoxPreference cbpMiscAutostart = (CheckBoxPreference) findPreference(ConfigurationAccess.MISC_AUTOSTART);
		boolean cbpMiscAutostartValue = DM.I.getConfiguration()
				.getMiscAutostart();
		cbpMiscAutostart.setChecked(cbpMiscAutostartValue);

		cbpMiscAutostart
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference pref,
							Object obj) {
						Boolean bool = (Boolean) obj;
						DM.I.getConfiguration().setMiscAutostart(bool);
						cbpMiscAutostart.setChecked(bool);
						return false;
					}
				});
		
		final ListPreference lpMiscDateFormat = (ListPreference) findPreference(ConfigurationAccess.MISC_DATEFORMAT);

		final DateFormat df = DateFormat.toEnum(DM.I.getConfiguration().getMiscDateFormat());
		setListSummary(lpMiscDateFormat, df.toString());

		lpMiscDateFormat
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference pref,
							Object obj) {

						DateFormat type = DateFormat.valueOf(obj.toString()); 
						setListSummary(lpMiscDateFormat, type.toString());

						DM.I.getConfiguration().setMiscDateFormat(type.getValue(),
								getApplicationContext());
						return false;
					}
				});
	}

	private String alarmText(String uristr, String def) {
		if (Settings.System.DEFAULT_RINGTONE_URI.toString().equals(uristr)) {
			return "Nagroid: " + def;
		}
		if (Settings.System.DEFAULT_NOTIFICATION_URI.toString().equals(uristr)) {
			return "Nagroid: " + def;
		}
		if (uristr.length() == 0) {
			return "(disabled)";
		}

		return resolvContent(uristr);
	}

	private String resolvContent(String uristr) {
		Cursor c = getContentResolver().query(Uri.parse(uristr), null, null,
				null, null);
		if (c.getCount() == 0) {
			return "(Not Found)";
		}
		c.moveToFirst();

		String name = "";

		name = c.getString(c.getColumnIndex("title"));
		if (name != null && name.length() != 0) {
			return name;
		}

		name = c.getString(c.getColumnIndex("_display_name"));
		if (name != null && name.length() != 0) {
			return name;
		}

		return "(Not Found)";
	}

	private String textOrUnset(String text) {
		if (text == null || text.length() == 0) {
			return UNSET;
		}
		return text;
	}

	private String passOrUnset(String pass) {
		if (pass == null || pass.length() == 0) {
			return UNSET;
		}
		return pass.replaceAll(".", "*");
	}

	private void setListSummary(ListPreference lp, String selected) {
		int idx = lp.findIndexOfValue(selected);
		CharSequence summary = null;
		if (idx != -1 && idx < lp.getEntries().length) {
			summary = lp.getEntries()[idx];
		}

		if (summary != null) {
			lp.setSummary(summary);
		} else {
			lp.setSummary(UNSET);
		}
	}

}