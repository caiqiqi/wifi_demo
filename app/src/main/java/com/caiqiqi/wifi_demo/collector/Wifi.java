package com.caiqiqi.wifi_demo.collector;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;


import java.util.Comparator;
import java.util.List;

public class Wifi {
	
	public static final ConfigurationSecurities ConfigSec = ConfigurationSecurities.newInstance();
    
	private static final String TAG = "Wifi Connecter";

	
	/**
	 * Change the password of an existing configured network and connect to it
	 */
	public static boolean changePasswordAndConnect(final Context context, final WifiManager wifiMgr, final WifiConfiguration config, final String newPassword, final int numOpenNetworksKept) {
		ConfigSec.setupSecurity(config, ConfigSec.getWifiConfigurationSecurity(config), newPassword);
		final int networkId = wifiMgr.updateNetwork(config);
		if(networkId == -1) {
			// Update failed.
			return false;
		}
		// Force the change to apply.
		wifiMgr.disconnect();
		return connectToConfiguredNetwork(context, wifiMgr, config, true);
	}
	
	/**
	 * Configure a network, and connect to it
	 */
	public static boolean connectToNewNetwork(final Context context, final WifiManager wifiMgr, final ScanResult scanResult, final String password, final int numOpenNetworksKept) {
		final String security = ConfigSec.getScanResultSecurity(scanResult);
		//
		if(ConfigSec.isOpenNetwork(security)) {
			checkForExcessOpenNetworkAndSave(wifiMgr, numOpenNetworksKept);
		}
		
		WifiConfiguration config = new WifiConfiguration();
		config.SSID = convertToQuotedString(scanResult.SSID);
		config.BSSID = scanResult.BSSID;
		ConfigSec.setupSecurity(config, security, password);
		
		int id = -1;
		try {
			id = wifiMgr.addNetwork(config);
		} catch(NullPointerException e) {
			Log.e(TAG, "Weird!! Really!! What's wrong??", e);
			// Weird!! Really!!
			// This exception is reported by user to Android Developer Console(https://market.android.com/publish/Home)
		}
		if(id == -1) {
			return false;
		}
		
		if(!wifiMgr.saveConfiguration()) {
			return false;
		}
		
		config = getWifiConfiguration(wifiMgr, config, security);
		if(config == null) {
			return false;
		}
		
		return connectToConfiguredNetwork(context, wifiMgr, config, true);
	}
	
	/**
	 * Connect to a configured network
	 */
	public static boolean connectToConfiguredNetwork(final Context context, final WifiManager wifiMgr, WifiConfiguration config, boolean reassociate) {
		final String security = ConfigSec.getWifiConfigurationSecurity(config);
		
		int oldPri = config.priority;
		// Make it the highest priority.
		int newPri = getMaxPriority(wifiMgr) + 1;
		if(newPri > MAX_PRIORITY) {
			newPri = shiftPriorityAndSave(wifiMgr);
			config = getWifiConfiguration(wifiMgr, config, security);
			if(config == null) {
				return false;
			}
		}
		
		// Set highest priority to this configured network
		config.priority = newPri;
		int networkId = wifiMgr.updateNetwork(config);
		if(networkId == -1) {
			return false;
		}
		
		// Do not disable others
		if(!wifiMgr.enableNetwork(networkId, false)) {
			config.priority = oldPri;
			return false;
		}
		
		if(!wifiMgr.saveConfiguration()) {
			config.priority = oldPri;
			return false;
		}
		
		// We have to retrieve the WifiConfiguration after save.
		config = getWifiConfiguration(wifiMgr, config, security);
		if(config == null) {
			return false;
		}
		
		ReenableAllApsWhenNetworkStateChanged.schedule(context);
		
		// Disable others, but do not save.
		// Just to force the WifiManager to connect to it.
		if(!wifiMgr.enableNetwork(config.networkId, true)) {
			return false;
		}
		
		final boolean connect = reassociate ? wifiMgr.reassociate() : wifiMgr.reconnect();
		if(!connect) {
			return false;
		}
		
		return true;
	}
	
	//根据优先级排序
	private static void sortByPriority(final List<WifiConfiguration> configurations) {
		java.util.Collections.sort(configurations, new Comparator<WifiConfiguration>() {

			@Override
			public int compare(WifiConfiguration object1,
					WifiConfiguration object2) {
				return object1.priority - object2.priority;
			}
		});
	}

	/**
	 * 根据信号强度排序
	 * @param scanResults
	 * @author caiqiqi
     */
	private static void sortByLevel(final List<ScanResult> scanResults) {

		java.util.Collections.sort(scanResults, new Comparator<ScanResult>() {

			@Override
			public int compare(ScanResult object1,
							   ScanResult object2) {
				return object1.level - object2.level;
			}
		});
	}

	/**
	 * Ensure no more than numOpenNetworksKept open networks in configuration list.
	 */
	private static boolean checkForExcessOpenNetworkAndSave(final WifiManager wifiMgr, final int numOpenNetworksKept) {
		final List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();
		sortByPriority(configurations);
		
		boolean modified = false;
		int tempCount = 0;
		for(int i = configurations.size() - 1; i >= 0; i--) {
			final WifiConfiguration config = configurations.get(i);
			if(ConfigSec.isOpenNetwork(ConfigSec.getWifiConfigurationSecurity(config))) {
				tempCount++;
				if(tempCount >= numOpenNetworksKept) {
					modified = true;
					wifiMgr.removeNetwork(config.networkId);
				}
			}
		}
		if(modified) {
			return wifiMgr.saveConfiguration();
		}
		
		return true;
	}
	
	private static final int MAX_PRIORITY = 99999;
	
	private static int shiftPriorityAndSave(final WifiManager wifiMgr) {
		final List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();
		sortByPriority(configurations);
		final int size = configurations.size();
		for(int i = 0; i < size; i++) {
			final WifiConfiguration config = configurations.get(i);
			config.priority = i;
			wifiMgr.updateNetwork(config);
		}
		wifiMgr.saveConfiguration();
		return size;
	}

	private static int getMaxPriority(final WifiManager wifiManager) {
		final List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
		int pri = 0;
		for(final WifiConfiguration config : configurations) {
			if(config.priority > pri) {
				pri = config.priority;
			}
		}
		return pri;
	}
	
	private static final String BSSID_ANY = "any";

	public static WifiConfiguration getWifiConfiguration(final WifiManager wifiMgr, final ScanResult hotsopt, String hotspotSecurity) {
		
		final String ssid = convertToQuotedString(hotsopt.SSID);
		final String bssid = hotsopt.BSSID;
		final List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();
		if(ssid.length() == 0 || bssid == null || configurations == null) {
			return null;
		}
		
		if(hotspotSecurity == null) {
			hotspotSecurity = ConfigSec.getScanResultSecurity(hotsopt);
		}

		for(final WifiConfiguration config : configurations) {
			if(config.SSID == null || !ssid.equals(config.SSID)) {
				continue;
			}
			if(config.BSSID == null || BSSID_ANY.equals(config.BSSID) ||  bssid.equals(config.BSSID)) {
				final String configSecurity = ConfigSec.getWifiConfigurationSecurity(config);
				if(hotspotSecurity.equals(configSecurity)) {
					return config;
				}
			}
		}
		return null;
	}
	
	public static WifiConfiguration getWifiConfiguration(final WifiManager wifiMgr, final WifiConfiguration wifiConfigToFind, String security) {
		final String ssid = wifiConfigToFind.SSID;
		if(ssid.length() == 0) {
			return null;
		}
		
		final String bssid = wifiConfigToFind.BSSID;

		
		if(security == null) {
			security = ConfigSec.getWifiConfigurationSecurity(wifiConfigToFind);
		}
		//先得到已经配置好的所有热点的信息
		final List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();

		for(final WifiConfiguration config : configurations) {
			if(config.SSID == null || !ssid.equals(config.SSID)) {
				continue;
			}
			if(config.BSSID == null || BSSID_ANY.equals(config.BSSID) || bssid == null || bssid.equals(config.BSSID)) {
				final String configSecurity = ConfigSec.getWifiConfigurationSecurity(config);
				if(security.equals(configSecurity)) {
					return config;
				}
			}
		}
		return null;
	}
	
	public static String convertToQuotedString(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        
        final int lastPos = string.length() - 1;
        if(lastPos > 0 && (string.charAt(0) == '"' && string.charAt(lastPos) == '"')) {
            return string;
        }
        
        return "\"" + string + "\"";
    }
   
}
