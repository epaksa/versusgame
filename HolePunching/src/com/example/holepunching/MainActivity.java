package com.example.holepunching;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

	public static final int PORT = 5678;
	public static final int BUF_SIZE = 65507;
	
	public static final String PREFERENCE_NAME = "ip";
	public static final String PREFERENCE_KEY = "saved";

	DatagramSocket ds;
	String peerInfo;
	LinearLayout content;
	Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		content = (LinearLayout) findViewById(R.id.content);
		
		setIP();
		
		handler = new Handler();
	}

	private void setIP() {
		SharedPreferences preference = getSharedPreferences(PREFERENCE_NAME, 0);
		String savedIP = preference.getString(PREFERENCE_KEY, "");
		
		EditText edit = (EditText) findViewById(R.id.ip);
		edit.setText(savedIP);
	}

	public void click(View v) {
		switch (v.getId()) {
		case R.id.set_ip:
			String ip = getIP();
			connectToServer(ip);
			v.setEnabled(false);
			findViewById(R.id.ip).setEnabled(false);
			break;
			
		case R.id.send:
			send();
			break;

		default:
			break;
		}
	}

	private void send() {
		EditText edit = (EditText) findViewById(R.id.message);
		final String msg = edit.getText().toString().trim();
		
		Thread sendThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					sendToClient(msg);
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
		});
		
		sendThread.start();
	}

	private void connectToServer(final String ip) {
		Thread serverThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ds = new DatagramSocket(PORT);

					sendToServer(ip);
					peerInfo = receive();
					sendToClient("initial msg");
					
					while(true){
						receive();
					}
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
		});
		
		serverThread.start();
	}

	private String getIP() {
		EditText edit = (EditText) findViewById(R.id.ip);
		String ip = edit.getText().toString().trim(); 
		
		SharedPreferences.Editor editor = getSharedPreferences(PREFERENCE_NAME, 0).edit();
		editor.putString(PREFERENCE_KEY, ip);
		editor.commit();
		
		return ip;
	}

	private void sendToClient(String msg) throws IOException {
		String[] split = peerInfo.split(":");
		String otherClientIP = split[0]; 
		String otherClientPort = split[1];
		InetAddress ip = InetAddress.getByName(otherClientIP);
		int port = Integer.parseInt(otherClientPort.trim());
		
		// send
		byte[] buffer = msg.getBytes();
		DatagramPacket dp = new DatagramPacket(buffer, 0, buffer.length, ip, port);
		ds.send(dp);
		
		print("send ("+msg+") to "+ip.getHostAddress()+":"+port);
	}

	private String receive() throws IOException {
		byte[] buffer = new byte[BUF_SIZE];
		DatagramPacket dp = new DatagramPacket(buffer, 0, BUF_SIZE);
		ds.receive(dp);
		
		String receivedIp = dp.getAddress().getHostAddress();
		int receivedPort = dp.getPort();
		String receivedMsg = new String(dp.getData());
		
		peerInfo = receivedIp+":"+receivedPort;
		
		print("msg : "+receivedMsg+" ("+receivedIp+":"+receivedPort+")");

		return receivedMsg;
	}

	private void sendToServer(String ip) throws IOException {
		InetAddress server = InetAddress.getByName(ip);
		byte[] msg = "This is message..".getBytes();

		DatagramPacket dp = new DatagramPacket(msg, 0, msg.length, server, PORT);
		ds.send(dp);
	}

	private void print(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				TextView t = new TextView(MainActivity.this);
				t.setText(msg);
				content.addView(t);
			}
		});
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(ds != null) ds.close();
	}
}
