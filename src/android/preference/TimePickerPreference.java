// Please note this must be the package if you want to use XML-based preferences
package android.preference;
 
import de.schoar.nagroid.ConfigurationAccess;
import de.schoar.nagroid.DM;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;
 
/**
 * A preference type that allows a user to choose a time
 */
public class TimePickerPreference extends DialogPreference implements
		TimePicker.OnTimeChangedListener {
 
	/**
	 * The validation expression for this preference
	 */
	private static final String VALIDATION_EXPRESSION = "[0-2]*[0-9]:[0-5]*[0-9]";
 
	/**
	 * The default value for this preference
	 */
	private String defaultValue;
	
	/**
	 * Timepicker object
	 */
	private TimePicker tp;
	
	/**
	 * Configruation access
	 */
	private ConfigurationAccess ca = DM.I.getConfiguration();
	
	/**
	 * @param context
	 * @param attrs
	 */
	public TimePickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}
 
	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public TimePickerPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}
 
	/**
	 * Initialize this preference
	 */
	private void initialize() {
		setPersistent(true);
	}
 
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.DialogPreference#onCreateDialogView()
	 */
	@Override
	protected View onCreateDialogView() {
 
		tp = new TimePicker(getContext());
		tp.setOnTimeChangedListener(this);
		tp.setIs24HourView(true);
 
		int h = getHour();
		int m = getMinute();
		if (h >= 0 && m >= 0) {
			tp.setCurrentHour(h);
			tp.setCurrentMinute(m);
		}
 
		return tp;
	}
 
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.DialogPreference#onDialogClosed(boolean)
	 */
	@Override
	protected void onDialogClosed (boolean positiveResult){
		if (positiveResult) {
			tp.clearFocus();
			int hour = tp.getCurrentHour();
			int minute = tp.getCurrentMinute();
			
			String fhour = ""+hour;
			String fminute = ""+minute;
			
			// Ieeuuwww really ugly. Someone has a better method?
			if (fhour.length() == 1) {
				fhour = "0"+fhour;
			}
			if (fminute.length() == 1) {
				fminute = "0"+fminute;
			}
			
			String result = fhour + ":" + fminute;
			
			ca.setSnoozeTime(result);
			callChangeListener(result);
		}
	}
 
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.Preference#setDefaultValue(java.lang.Object)
	 */
	@Override
	public void setDefaultValue(Object defaultValue) {
		// BUG this method is never called if you use the 'android:defaultValue' attribute in your XML preference file, not sure why it isn't		
 
		super.setDefaultValue(defaultValue);
 
		if (!(defaultValue instanceof String)) {
			return;
		}
 
		if (!((String) defaultValue).matches(VALIDATION_EXPRESSION)) {
			return;
		}
 
		this.defaultValue = (String) defaultValue;
	}
 
	/**
	 * Get the hour value (in 24 hour time)
	 * 
	 * @return The hour value, will be 0 to 23 (inclusive)
	 */
	private int getHour() {
		String time = ca.getSnoozeTime();
		if (time == null || !time.matches(VALIDATION_EXPRESSION)) {
			return -1;
		}
 
		return Integer.valueOf(time.split(":")[0]);
	}
 
	/**
	 * Get the minute value
	 * 
	 * @return the minute value, will be 0 to 59 (inclusive)
	 */
	private int getMinute() {
		String time = ca.getSnoozeTime();
		if (time == null || !time.matches(VALIDATION_EXPRESSION)) {
			return -1;
		}
 
		return Integer.valueOf(time.split(":")[1]);
	}

	@Override
	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
		// TODO Auto-generated method stub
		
	}
}