package com.caiqiqi.wifi_demo.collector;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.caiqiqi.wifi_demo.collector.BaseContent;
import com.caiqiqi.wifi_demo.ui.Floating;

import com.caiqiqi.wifi_demo.R;

/**
 * 已保存的网络
 */
public class ConfiguredNetworkContent extends BaseContent {

	public ConfiguredNetworkContent(Floating floating, WifiManager wifiManager, ScanResult scanResult) {
		super(floating, wifiManager, scanResult);
		
		findViewAndSetGone();
	}

	private void findViewAndSetGone() {
		mView.findViewById(R.id.Status).setVisibility(View.GONE);
		mView.findViewById(R.id.Speed).setVisibility(View.GONE);
		mView.findViewById(R.id.IPAddress).setVisibility(View.GONE);
		mView.findViewById(R.id.Password).setVisibility(View.GONE);
		
		mView.findViewById(R.id.MacAddress).setVisibility(View.GONE);
		mView.findViewById(R.id.Netmask).setVisibility(View.GONE);
		mView.findViewById(R.id.Gateway).setVisibility(View.GONE);
		mView.findViewById(R.id.DNS1).setVisibility(View.GONE);
		mView.findViewById(R.id.DNS2).setVisibility(View.GONE);
		//当前连接数
		mView.findViewById(R.id.CrrntConnecsCnt).setVisibility(View.GONE);
	}

	@Override
	public int getButtonCount() {
		return 3;
	}

	@Override
	public OnClickListener getButtonOnClickListener(int index) {
		switch(index) {
		case 0:
			return mConnectOnClick;
		case 1:
			if(mIsOpenNetwork) {
				return mForgetOnClick;
			} else {
				return mOpOnClick;
			}
		case 2:
			return mCancelOnClick;
		default:
			return null;
		}
	}

	@Override
	public CharSequence getButtonText(int index) {
		switch(index) {
		case 0:
			return mFloating.getString(R.string.connect);
		case 1:
			if(mIsOpenNetwork) {
				return mFloating.getString(R.string.forget_network);
			} else {
				return mFloating.getString(R.string.buttonOp);
			}
		case 2:
			return getCancelString();
		default:
			return null;
		}
	}

	@Override
	public CharSequence getTitle() {
		//哦，原来在字符串里面也可以加一个花括号来引用对象的属性啊！
		return mFloating.getString(R.string.wifi_connect_to, mScanResult.SSID);
	}
	
/**
 * “连接”按钮监听器
 */
	private OnClickListener mConnectOnClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			final WifiConfiguration config = Wifi.getWifiConfiguration(mWifiManager, mScanResult, mScanResultSecurity);
			boolean connResult = false;
			if(config != null) {
				connResult = Wifi.connectToConfiguredNetwork(mFloating, mWifiManager, config, false);
			}
			if(!connResult) {
				Toast.makeText(mFloating, R.string.toastFailed, Toast.LENGTH_LONG).show();
			}
			
			mFloating.finish();
		}
	};
	
	private OnClickListener mOpOnClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mFloating.registerForContextMenu(v);
			mFloating.openContextMenu(v);
			mFloating.unregisterForContextMenu(v);
		}
	};
	
/**
 * “忘记”按钮监听器
 */
	private OnClickListener mForgetOnClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			forget();
		}
	};

/**
 * 忘记网络
 */
	private void forget() {
		final WifiConfiguration config = Wifi.getWifiConfiguration(mWifiManager, mScanResult, mScanResultSecurity);
		boolean b_result = false;
		
		if(config != null) {
			//只有移除该网络，并且保存了配置信息之后才返回true
			b_result = mWifiManager.removeNetwork(config.networkId)
				&& mWifiManager.saveConfiguration();
		}
		if(!b_result) {
			Toast.makeText(mFloating, R.string.toastFailed, Toast.LENGTH_LONG).show();
		}
		
		mFloating.finish();
	}

}
