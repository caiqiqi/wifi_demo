package com.caiqiqi.wifi_demo.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.caiqiqi.wifi_demo.R;

public class Floating extends Activity {
	
	//那三个按钮！！！最多三个，也可为两个
	private static final int[] BUTTONS = {R.id.button1, R.id.button2, R.id.button3};
	
	private View mView;
	private ViewGroup mContentViewContainer;
	private Content mContent;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// It will not work if we setTheme here.
		// Please add android:theme="@android:style/Theme.Dialog" to any descendant class in AndroidManifest.xml!
		// See http://code.google.com/p/android/issues/detail?id=4394
		// setTheme(android.R.style.Theme_Dialog);
		
		//没有标题栏的
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);
		
		mView = View.inflate(this, R.layout.floating, null);
		final DisplayMetrics dm = new DisplayMetrics(); 
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		mView.setMinimumWidth(Math.min(dm.widthPixels, dm.heightPixels) - 20);
		setContentView(mView);
		
		//这就是显示内容的区域(ScrollView)
		//状态、速度、信号强度等。。。
		mContentViewContainer = (ViewGroup) mView.findViewById(R.id.content);
	}

/**
 * 设置对话框的View(先移除所有VIew，再重新添加)
 */
	@SuppressWarnings("deprecation")
	private void setDialogContentView(final View contentView) {
		mContentViewContainer.removeAllViews();
		mContentViewContainer.addView(contentView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
	}
	
/**
 * 设置内容
 */
	public void setContent(Content content) {
		mContent = content;
		refreshContent();
	}

/**
 * 刷新内容
 */
	public void refreshContent() {
		//先把View刷新一遍
		setDialogContentView(mContent.getView());
		((TextView)findViewById(R.id.title)).setText(mContent.getTitle());
		
		final int btnCount = mContent.getButtonCount();
		//按钮的总数不能超过 BUTTONS.length（即3）
		if(btnCount > BUTTONS.length) {
			throw new RuntimeException(String.format("%d exceeds maximum button count: %d!", btnCount, BUTTONS.length));
		}
		//按钮数 >0,则显示按钮，否则不显示
		findViewById(R.id.buttons_view).setVisibility(btnCount > 0 ? View.VISIBLE : View.GONE);
		
		for(int buttonId:BUTTONS) {
			final Button btn = (Button) findViewById(buttonId);
			btn.setOnClickListener(null);
			btn.setVisibility(View.GONE);
		}

		for(int btnIndex = 0; btnIndex < btnCount; btnIndex++){
			final Button btn = (Button)findViewById(BUTTONS[btnIndex]);
			btn.setText(mContent.getButtonText(btnIndex));
			btn.setVisibility(View.VISIBLE);
			btn.setOnClickListener(mContent.getButtonOnClickListener(btnIndex));
		}
	}
	
//接口定义	
	public interface Content {
		/**
		 * 得到标题
		 */
		CharSequence getTitle();
		/**
		 * 得到View
		 */
		View getView();
		/**
		 * 得到按钮数
		 */
		int getButtonCount();
		/**
		 * 得到按钮中的文本
		 */
		CharSequence getButtonText(int index);
		/**
		 * 得到那个按钮的监听器
		 */
		OnClickListener getButtonOnClickListener(int index);
	}
}
