package com.example.holepunching;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends Activity {

	public static final int PORT = 5678;
	public static final int BUF_SIZE = 65507;
	public static final String IP = "52.78.185.203";

	DatagramSocket ds;
	String msg;
	ViewGroup root;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		root = (ViewGroup) findViewById(R.id.root);

		AsyncTask.execute(new Runnable() {

			@Override
			public void run() {
				try {
					ds = new DatagramSocket(PORT);

					sendToServer(ds);
					msg = receive(ds);
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (ds != null)
						ds.close();
				}
			}
		});

	}

	public void click(View v) {
		AsyncTask.execute(new Runnable() {

			@Override
			public void run() {
				try {
					receive(ds);
					sendToClient(ds, msg);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		// receive(ds);
	}

	private void sendToClient(DatagramSocket ds, String msg) throws IOException {
		String[] split = msg.split(":");
		String otherClientIP = split[0];
		String otherClientPort = split[1];
		InetAddress ip = InetAddress.getByName(otherClientIP);
		int port = Integer.parseInt(otherClientPort.trim());

		// send
		byte[] buffer = "hello".getBytes();
		DatagramPacket dp = new DatagramPacket(buffer, 0, buffer.length, ip, port);
		ds.send(dp);
	}

	private String receive(DatagramSocket ds) throws IOException {
		byte[] buffer = new byte[BUF_SIZE];
		DatagramPacket dp = new DatagramPacket(buffer, 0, BUF_SIZE);
		ds.receive(dp);

		String receivedIp = dp.getAddress().getHostAddress();
		int receivedPort = dp.getPort();
		String receivedMsg = new String(dp.getData());

		print("*** received! ***\nip:port - " + receivedIp + ":" + receivedPort + "\nmsg : " + receivedMsg);

		return receivedMsg;
	}

	private void sendToServer(DatagramSocket ds) throws IOException {
		InetAddress server = InetAddress.getByName(IP);
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
				root.addView(t);
			}
		});
	}
}
