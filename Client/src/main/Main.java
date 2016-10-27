package main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Main {
	
	public static final int PORT = 5678;
	public static final int BUF_SIZE = 65507;
	public static final String IP = "52.78.185.203";
	
	static DatagramSocket ds = null;
	
	public static void main(String[] args) {
		
		try {
			ds = new DatagramSocket(PORT);
			
			sendToServer(ds);
			String msg = receive(ds);
			
			Thread t1 = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						sendToClient(ds, msg);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			
			Thread t2 = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						receive(ds);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			
			t1.start();
			t2.start();
			t1.join();
			t2.join();
			
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally{
			if(ds != null) ds.close();
		}
	}

	private static void sendToClient(DatagramSocket ds, String msg) throws IOException {
		String[] split = msg.split(":");
		String otherClientIP = split[0]; 
		String otherClientPort = split[1];
		InetAddress ip = InetAddress.getByName(otherClientIP);
		int port = Integer.parseInt(otherClientPort.trim());
		
		// send
		byte[] buffer = "hello".getBytes();
		DatagramPacket dp = new DatagramPacket(buffer, 0, buffer.length, ip, port);
		ds.send(dp);
		System.out.println("send to "+ip.getHostAddress()+":"+port);
	}

	private static String receive(DatagramSocket ds) throws IOException {
		byte[] buffer = new byte[BUF_SIZE];
		DatagramPacket dp = new DatagramPacket(buffer, 0, BUF_SIZE);
		ds.receive(dp);
		
		System.out.println("*** received! ***");
		
		String receivedIp = dp.getAddress().getHostAddress();
		int receivedPort = dp.getPort();
		String receivedMsg = new String(dp.getData());
		
		System.out.println("ip:port - "+receivedIp+":"+receivedPort);
		System.out.println("msg : "+receivedMsg);
		
		return receivedMsg;
	}

	private static void sendToServer(DatagramSocket ds) throws IOException {
		InetAddress server = InetAddress.getByName(IP);
		byte[] msg = "This is message..".getBytes();
		
		DatagramPacket dp = new DatagramPacket(msg, 0, msg.length, server, PORT);
		ds.send(dp);
	}

}
