package main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import data.User;

public class Main {
	
	public static final String NEW_LINE = System.getProperties().getProperty("line.separator");
	public static final int PORT = 5678;
	public static final int BUF_SIZE = 65507;
	
	static User peer;
	static DatagramSocket ds;
	
	public static void main(String[] args) {
		String serverIP = args[0];
		
		try {
			sendToServer(serverIP);
			peer = receiveFromServer();
			
			if(!peer.isSameNAT()){
				holePunch();
			}
			
			Thread sendThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						@SuppressWarnings("resource")
						Scanner sc = new Scanner(System.in);
						while(true){
							sendToPeer(sc.nextLine());
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
							receiveFromPeer();
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

	private static void sendToServer(String args) throws IOException {
		System.out.println("### sent my information to server("+args+") ###");
		
		InetAddress server = InetAddress.getByName(args);
		String localIP = InetAddress.getLocalHost().getHostAddress();
		byte[] msg = ("hello, my private IP is "+localIP).getBytes();
		
		DatagramPacket dp = new DatagramPacket(msg, 0, msg.length, server, PORT);
		ds = new DatagramSocket(PORT);
		ds.send(dp);
	}

	private static User receiveFromServer() throws IOException {
		byte[] buffer = new byte[BUF_SIZE];
		DatagramPacket dp = new DatagramPacket(buffer, 0, BUF_SIZE);
		ds.receive(dp);
		
		String receivedIp = dp.getAddress().getHostAddress();
		int receivedPort = dp.getPort();
		String receivedMsg = new String(dp.getData());
		
		System.out.println("### received peer's information from server("+receivedIp+":"+receivedPort+") ###");
		System.out.println("peer's information : "+receivedMsg.trim()+NEW_LINE);
		
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

	private static void holePunch() throws IOException {
		InetAddress ip = InetAddress.getByName(peer.ip);
		int port = peer.port;
		
		// send
		byte[] buffer = "$ message for holepunch $".getBytes();
		DatagramPacket dp = new DatagramPacket(buffer, 0, buffer.length, ip, port);
		ds.send(dp);
		
		System.out.println("### punched to peer("+ip.getHostAddress()+":"+port+"). wait for response.. ###");
	}
	
	private static void sendToPeer(String msg) throws IOException {
		InetAddress ip = InetAddress.getByName(peer.ip);
		int port = peer.port;
		byte[] buffer = msg.getBytes();
		
		DatagramPacket dp = new DatagramPacket(buffer, 0, buffer.length, ip, port);
		ds.send(dp);
		
		System.out.println("[SENT] "+msg+" ("+ip.getHostAddress()+":"+port+")");
	}

	private static String receiveFromPeer() throws IOException {
		byte[] buffer = new byte[BUF_SIZE];
		DatagramPacket dp = new DatagramPacket(buffer, 0, BUF_SIZE);
		ds.receive(dp);
		
		String receivedIp = dp.getAddress().getHostAddress();
		int receivedPort = dp.getPort();
		String receivedMsg = new String(dp.getData(), StandardCharsets.UTF_8);
		
		/* 서버가 보내준 peer의 port와 실제 peer에게 응답받은 port가 다를 수 있음.
		 * peer에게 응답받은 port로 update해야함. */
		peer = new User(receivedIp, receivedPort, peer.isSameNAT() ? true : false);
		
		System.out.println("[RECEIVED] "+receivedMsg.trim()+" ("+receivedIp+":"+receivedPort+")");
		
		return receivedMsg;
	}
}
