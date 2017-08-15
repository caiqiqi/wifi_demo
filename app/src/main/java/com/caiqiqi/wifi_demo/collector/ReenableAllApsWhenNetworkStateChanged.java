package com.caiqiqi.wifi_demo.collector;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.IBinder;

import java.util.List;

/**
 * 当网络发生变化时重新使能所有APP
 * @author caiqiqi
 *
 */
public class ReenableAllApsWhenNetworkStateChanged {
	
	/**
	 * 开启后台Service
	 * @param context
	 */
	public static void schedule(final Context context) {
		context.startService(new Intent(context, BackgroundService.class));
	}
	
	private static void reenableAllAps(final Context context) {
		final WifiManager wifiMgr = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		final List<WifiConfiguration> list_configs = wifiMgr.getConfiguredNetworks();
		if(list_configs != null) {
			for(final WifiConfiguration config:list_configs) {
				// "false" means "do not disable all other configured networks"
				wifiMgr.enableNetwork(config.networkId, false);
			}
		}
	}
	
	public static class BackgroundService extends Service {

		/**
		 * 是否已经重新使能了
		 */
		private boolean mReenabled;
		
		private BroadcastReceiver mReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				final String str_action = intent.getAction();
				if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(str_action)) {
					//若传入的Action匹配到了
					final NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
					final NetworkInfo.DetailedState detailed = networkInfo.getDetailedState();
					if(detailed != NetworkInfo.DetailedState.DISCONNECTED
							&& detailed != NetworkInfo.DetailedState.DISCONNECTING
							&& detailed != NetworkInfo.DetailedState.SCANNING) {
						if(!mReenabled) {
							mReenabled = true;
							reenableAllAps(context);
							//stop the service if it was previously started
							BackgroundService.this.stopSelf();
						}
					}
				}
			}
		};
		
		private IntentFilter mIntentFilter;
		
		@Override
		public IBinder onBind(Intent intent) {
			return null; // We need not bind to it at all.
		}
		
		@Override
		public void onCreate() {
			super.onCreate();
			mReenabled = false;
			mIntentFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
			//开始时注册广播
			registerReceiver(mReceiver, mIntentFilter);
		}
		
		@Override
		public void onDestroy() {
			super.onDestroy();
			// 销毁时注销掉广播
			unregisterReceiver(mReceiver);
		}

	}
}
