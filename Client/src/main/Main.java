package main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

public class Main {
	
	public static final int PORT = 5678;
	public static final int BUF_SIZE = 65507;
	
	static DatagramSocket ds = null;
	static String peerInfo = null;
	
	public static void main(String[] args) {
		
		try {
			ds = new DatagramSocket(PORT);
			
			sendToServer(args[0]);
			peerInfo = receive();
			
			Thread sendThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						@SuppressWarnings("resource")
						Scanner sc = new Scanner(System.in);
						while(true){
							sendToClient(sc.nextLine());
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});

			Thread receiveThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						while(true){
							receive();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			
			sendThread.start();
			receiveThread.start();
			sendThread.join();
			receiveThread.join();
			
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

	private static void sendToClient(String msg) throws IOException {
		String[] split = peerInfo.split(":");
		String otherClientIP = split[0]; 
		String otherClientPort = split[1];
		InetAddress ip = InetAddress.getByName(otherClientIP);
		int port = Integer.parseInt(otherClientPort.trim());
		
		// send
		byte[] buffer = msg.getBytes();
		DatagramPacket dp = new DatagramPacket(buffer, 0, buffer.length, ip, port);
		ds.send(dp);
		
		System.out.println("send ("+msg+") to "+ip.getHostAddress()+":"+port);
	}

	private static String receive() throws IOException {
		byte[] buffer = new byte[BUF_SIZE];
		DatagramPacket dp = new DatagramPacket(buffer, 0, BUF_SIZE);
		ds.receive(dp);
		
		String receivedIp = dp.getAddress().getHostAddress();
		int receivedPort = dp.getPort();
		String receivedMsg = new String(dp.getData());
		
		peerInfo = receivedIp+":"+receivedPort;
		
		System.out.println("msg : "+receivedMsg+" ("+receivedIp+":"+receivedPort+")");
		
		return receivedMsg;
	}

	private static void sendToServer(String args) throws IOException {
		InetAddress server = InetAddress.getByName(args);
		byte[] msg = "message from client!".getBytes();
		
		DatagramPacket dp = new DatagramPacket(msg, 0, msg.length, server, PORT);
		ds.send(dp);
	}

}
