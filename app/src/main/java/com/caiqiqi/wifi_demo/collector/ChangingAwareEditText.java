package com.caiqiqi.wifi_demo.collector;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class ChangingAwareEditText extends EditText {

	public ChangingAwareEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	private boolean mChanged = false;
	
	public boolean getChanged() {
		return mChanged;
	}
	
	protected void onTextChanged (CharSequence text, int start, int before, int after) {
		mChanged = true;
	}
}
