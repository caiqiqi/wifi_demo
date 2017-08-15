package com.caiqiqi.wifi_demo.utils;

import android.net.wifi.ScanResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.caiqiqi.wifi_demo.utils.Constants;

public class ClientThread implements Runnable {

	public static final String TAG = "ClientThread";

	private String server_ip;
			//= Constants.SERVER_IP;
	private int server_port;
			//= Constants.SERVER_PORT;

	private Socket s;

	// 接收UI线程消息的Handler对象
	public Handler rcvHandler;

	private Handler mHandler;

	// 该线程所处理的Socket所对应的输入流
	private BufferedReader br;

	private OutputStream os;
	private ObjectOutputStream oos;


	public ClientThread(Handler handler, String ip, int port) {
		this.mHandler = handler;
		this.server_ip = ip;
		this.server_port = port;

		rcvHandler =  new MyHander();
		//不能放这里(主线程里不能进行网络操作)，不然主线程会直接崩溃，android.os.NetworkOnMainThreadException
	}

	public void run() {


		Log.d(TAG, "ClientThread started");
		try {

			initSocket();
			// 为当前线程初始化Looper
			Looper.prepare();
			//rcvHandler =  new MyHander();
			//Log.d(TAG, "rcvHandler对象建立");


			//必须要这这里启动一个新线程，而不能直接写run方法里面的代码，因为如果不新开一个线程，那么loop不会运行
			// 启动一条子线程来读取服务器响应的数据
			new Thread() {
				@Override
				public void run() {
					Log.d(TAG, "子线程开启");
					String content;
					// 不断读取Socket输入流中的内容

					try {
						//先得看br是否为空，因为可能这个时候Socket还没有建立好，导致BufferredReader为空
						if (br != null) {
							while ((content = br.readLine()) != null) {
                                Log.d(TAG, "子线程读取到消息");

                                Message msg = new Message();
                                msg.what = Constants.MESSAGE_RECEIVED_FROM_SERVER;
                                msg.obj = content;

                                // 因为这个mHandler是主线程的，所以主线程中的mHandler会处理的
                                mHandler.sendMessage(msg);
                                Log.d(TAG, "子线程的mHandler已发送消息：" + Constants.MESSAGE_RECEIVED_FROM_SERVER);
                            }
						}

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();

			// 启动Looper
			// 注意:写在Looper.loop()之后的代码不会被执行,这个函数内部应该是一个循环
			Looper.loop();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initSocket(){

		try {

			s = new Socket(server_ip, server_port);
			Log.d(TAG,"Socket " + "创建成功" + s);

			br = new BufferedReader(new InputStreamReader(this.s.getInputStream()));

			//is = this.s.getInputStream();
			//ois = new ObjectInputStream(is);

			os = this.s.getOutputStream();
			oos = new ObjectOutputStream(os);

		} catch (IOException e) {
			Log.e(TAG, "initSocket failed !");
			e.printStackTrace();
		}
	}
	
/** 由于ScanResult不能Serizilible，而且这个类只定义在Android的SDK中，这里将其转换为String */
	private List<String> convertScanResult(List<ScanResult> list){
		List<String> strList= new ArrayList<String>();
		String strScanResult;
		for (ScanResult scanResult: list){
			strScanResult = scanResult.toString();
			strList.add(strScanResult);
		}
		Log.d(TAG, "ScanResult对象转换成功");
		return strList;
		
	}

	public class MyHander extends Handler{
		@Override
		public void handleMessage(Message msg) {

			if (msg.what == Constants.MESSAGE_TO_BE_SENT){
				Log.d(TAG, "rcvHandler收到消息");
				List<ScanResult> list_result = (List<ScanResult>) msg.obj;
				List<String> list_string = convertScanResult(list_result);

				try {

					if (oos != null) {
						oos.writeObject(list_string);
						oos.flush();
					}

				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}
}