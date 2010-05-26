package de.schoar.nagroid.nagios;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class NagiosSiteAdapater extends BaseAdapter {

	private Context mCtx;
	private List<NagiosHost> mHosts = new LinkedList<NagiosHost>();

	public NagiosSiteAdapater(Context ctx) {
		mCtx = ctx;
	}

	public void updateHosts(List<NagiosHost> hosts) {
		mHosts = hosts;
	}

	@Override
	public int getCount() {
		int i = 0;
		for (NagiosHost nh : mHosts) {
			i++;
			for (@SuppressWarnings("unused")
			NagiosService ns : nh.getServices()) {
				i++;
			}
			i++;
		}
		return i;
	}

	@Override
	public Object getItem(int position) {
		int i = -1;

		for (NagiosHost nh : mHosts) {
			i++;
			if (i == position) {
				return nh;
			}
			for (NagiosService ns : nh.getServices()) {
				i++;
				if (i == position) {
					return ns;
				}
			}
			i++;
			if (i == position) {
				return "";
			}
		}
		return "";
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Object o = getItem(position);

		if (o instanceof NagiosHost) {
			NagiosHost nh = (NagiosHost) o;
			return createRowState(convertView, nh.getName(), nh.getState()
					.toShort(), nh.getState().toColor(), 5);
		}

		if (o instanceof NagiosService) {
			NagiosService ns = (NagiosService) o;
			return createRowState(convertView, ns.getName(), ns.getState()
					.toShort(), ns.getState().toColor(), 15);
		}

		return createRowSpacer(convertView);

	}

	private View createRowState(View view, String textName, String textState,
			int bgColor, int padLeft) {
		boolean sameType = view instanceof LinearLayout;

		LinearLayout ll = null;
		TextView tvName = null;
		TextView tvState = null;

		if (sameType) {
			ll = (LinearLayout) view;
			tvName = (TextView) ll.getChildAt(0);
			tvState = (TextView) ll.getChildAt(1);
		} else {
			ll = new LinearLayout(mCtx);
			ll.setOrientation(LinearLayout.HORIZONTAL);

			tvName = new TextView(mCtx);
			tvName.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT, 1));

			tvName.setSingleLine();
			tvName.setTextColor(Color.BLACK);
			tvName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
			ll.addView(tvName);

			tvState = new TextView(mCtx);
			tvState.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT, 0));
			tvState.setPadding(5, 0, 10, 0);
			tvState.setSingleLine();

			tvState.setTextColor(Color.BLACK);
			tvState.setGravity(Gravity.CENTER_HORIZONTAL);
			ll.addView(tvState);
		}

		tvName.setPadding(padLeft, 0, 5, 0);

		ll.setBackgroundColor(bgColor);
		tvName.setText(textName);
		tvState.setText(textState);

		return ll;
	}

	private View createRowSpacer(View view) {
		boolean sameType = view instanceof TextView;

		TextView tv = null;

		if (sameType) {
			tv = (TextView) view;
		} else {
			tv = new TextView(mCtx);
			tv.setText("");
			tv.setBackgroundColor(Color.BLACK);
			tv.setTextColor(Color.BLACK);
		}

		return tv;
	}
}
