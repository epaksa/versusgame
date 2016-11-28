package com.example.holepunching;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.holepunching.data.User;

public class MainActivity extends Activity {

	public static final String NEW_LINE = System.getProperties().getProperty("line.separator");
	public static final int PORT = 5678;
	public static final int BUF_SIZE = 65507;
	
	public static final String PREFERENCE_NAME = "ip";
	public static final String PREFERENCE_KEY = "saved";

	User peer;
	DatagramSocket ds;
	TextView content;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		content = (TextView) findViewById(R.id.content);
		
		setIP();
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
					sendToPeer(msg);
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
					peer = receiveFromServer();
					
					if(!peer.isSameNAT()){
						holePunch();
					}
					
					while(true){
						receiveFromPeer();
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
	
	private void sendToServer(String ip) throws IOException {
		print("### sent my information to server("+ip+") ###");
		
		InetAddress server = InetAddress.getByName(ip);
		String localIP = getMyIp();
		byte[] msg = ("hello, my private IP is "+localIP).getBytes();
		
		DatagramPacket dp = new DatagramPacket(msg, 0, msg.length, server, PORT);
		ds.send(dp);
	}
	
	private String getMyIp() {
		String result = "";
		
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if(!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
						result = inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		}
		
		return result;
	}

	private User receiveFromServer() throws IOException {
		byte[] buffer = new byte[BUF_SIZE];
		DatagramPacket dp = new DatagramPacket(buffer, 0, BUF_SIZE);
		ds.receive(dp);
		
		String receivedIp = dp.getAddress().getHostAddress();
		int receivedPort = dp.getPort();
		String receivedMsg = new String(dp.getData());
		
		print("### received peer's information from server("+receivedIp+":"+receivedPort+") ###"
				+NEW_LINE+ "peer's information : "+receivedMsg);
		
		boolean sameNAT = false;
		if(receivedMsg.contains("same")){
			String[] splited = receivedMsg.split("same");
			receivedMsg = splited[0];
			sameNAT = true;
		}
		
		String[] peerInfo = receivedMsg.split(":");
		String ip = peerInfo[0];
		int port = Integer.parseInt(peerInfo[1].trim());
		
		return new User(ip, port, sameNAT);
	}

	private void holePunch() throws IOException {
		InetAddress ip = InetAddress.getByName(peer.ip);
		int port = peer.port;
		
		// send
		byte[] buffer = "$ message for holepunch $".getBytes();
		DatagramPacket dp = new DatagramPacket(buffer, 0, buffer.length, ip, port);
		ds.send(dp);
		
		print("### punched to peer("+ip.getHostAddress()+":"+port+"). wait for response.. ###");
	}

	private void sendToPeer(String msg) throws IOException {
		InetAddress ip = InetAddress.getByName(peer.ip);
		int port = peer.port;
		byte[] buffer = msg.getBytes();
		
		DatagramPacket dp = new DatagramPacket(buffer, 0, buffer.length, ip, port);
		ds.send(dp);
		
		print("[SENT] "+msg+" ("+ip.getHostAddress()+":"+port+")");
	}

	private String receiveFromPeer() throws IOException {
		byte[] buffer = new byte[BUF_SIZE];
		DatagramPacket dp = new DatagramPacket(buffer, 0, BUF_SIZE);
		ds.receive(dp);
		
		String receivedIp = dp.getAddress().getHostAddress();
		int receivedPort = dp.getPort();
		String receivedMsg = new String(dp.getData());
		
		/* 서버가 보내준 peer의 port와 실제 peer에게 응답받은 port가 다를 수 있음.
		 * peer에게 응답받은 port로 update해야함. */
		peer = new User(receivedIp, receivedPort, peer.isSameNAT() ? true : false);
		
		print("[RECEIVED] "+receivedMsg+" ("+receivedIp+":"+receivedPort+")");
		
		return receivedMsg;
	}

	private void print(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String printStr = content.getText()+NEW_LINE+msg;
				content.setText(printStr);
			}
		});
	}
	
	private String getIP() {
		EditText edit = (EditText) findViewById(R.id.ip);
		String ip = edit.getText().toString().trim(); 
		
		SharedPreferences.Editor editor = getSharedPreferences(PREFERENCE_NAME, 0).edit();
		editor.putString(PREFERENCE_KEY, ip);
		editor.commit();
		
		return ip;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(ds != null) ds.close();
	}
}
