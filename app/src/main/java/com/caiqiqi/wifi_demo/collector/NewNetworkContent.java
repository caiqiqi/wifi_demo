package com.caiqiqi.wifi_demo.collector;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.caiqiqi.wifi_demo.ui.Floating;
import com.caiqiqi.wifi_demo.R;

/**
 * 新的网络
 */
public class NewNetworkContent extends BaseContent {
	
	private boolean mIsOpenNetwork = false;
	
	public NewNetworkContent(final Floating floating, final WifiManager wifiManager, ScanResult scanResult) {
		super(floating, wifiManager, scanResult);
		
		findViewAndSetGone();
		
		//如果是开放的网络，则将“密码”一栏设置为GONE
		if(Wifi.ConfigSec.isOpenNetwork(mScanResultSecurity)) {
			mIsOpenNetwork = true;
			mView.findViewById(R.id.Password).setVisibility(View.GONE);
		} else {
			((TextView)mView.findViewById(R.id.Password_TextView)).setText(R.string.please_type_passphrase);
		}
	}

	private void findViewAndSetGone() {
		mView.findViewById(R.id.Status).setVisibility(View.GONE);
		mView.findViewById(R.id.Speed).setVisibility(View.GONE);
		mView.findViewById(R.id.IPAddress).setVisibility(View.GONE);
		
		mView.findViewById(R.id.MacAddress).setVisibility(View.GONE);
		mView.findViewById(R.id.Netmask).setVisibility(View.GONE);
		mView.findViewById(R.id.Gateway).setVisibility(View.GONE);
		mView.findViewById(R.id.DNS1).setVisibility(View.GONE);
		mView.findViewById(R.id.DNS2).setVisibility(View.GONE);
		//当前连接数
		mView.findViewById(R.id.CrrntConnecsCnt).setVisibility(View.GONE);
	}

/**
 * 在父类（BaseContent）基础上增加的 “连接”按钮监听器
 * 于是，现在就有三个按钮“连接”、“修改密码”、“取消”的监听器了
 */
	private OnClickListener mConnectOnClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			boolean connResult;
			if(mIsOpenNetwork) {
				connResult = Wifi.connectToNewNetwork(mFloating, mWifiManager, mScanResult, null, mNumOpenNetworksKept);
			} else {
				connResult = Wifi.connectToNewNetwork(mFloating, mWifiManager, mScanResult 
						, ((EditText)mView.findViewById(R.id.Password_EditText)).getText().toString()
						, mNumOpenNetworksKept);
			}
			
			if(!connResult) {
				Toast.makeText(mFloating, R.string.toastFailed, Toast.LENGTH_LONG).show();
			}
			
			mFloating.finish();
		}
	};
	
/**
 * 两个按钮--“连接”和“取消”
 */
	private OnClickListener mOnClickListeners[] = {mConnectOnClick, mCancelOnClick};

	@Override
	public int getButtonCount() {
		return 2;
	}

	@Override
	public OnClickListener getButtonOnClickListener(int index) {
		return mOnClickListeners[index];
	}
	

/**
 * 获取按钮上的文本
 */
	@Override
	public CharSequence getButtonText(int index) {
		switch(index) {
		case 0:
			return mFloating.getText(R.string.connect);
		case 1:
			return getCancelString();
		default:
			return null;
		}
	}

	@Override
	public CharSequence getTitle() {
		return mFloating.getString(R.string.wifi_connect_to, mScanResult.SSID);
	}

}
