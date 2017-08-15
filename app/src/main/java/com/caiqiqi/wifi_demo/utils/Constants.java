package com.caiqiqi.wifi_demo.utils;

public class Constants {

	public static String SERVER_IP = "192.168.23.4";
	public static int SERVER_PORT = 30000;
	//因为这里两个常量是为了给msg.what用的，所以只能是int类型，而不能是String类型
	public static int MESSAGE_TO_BE_SENT            = 1;
	public static int MESSAGE_RECEIVED_FROM_SERVER  = 2;
}
