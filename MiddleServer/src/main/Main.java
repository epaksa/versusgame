package main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import data.User;

public class Main {

	public static final int PORT = 5678;
	public static final int BUF_SIZE = 65507;
	
	public static void main(String[] args) {
		User[] queue = new User[2];
		
		byte[] buffer = new byte[BUF_SIZE];
		
		DatagramSocket ds = null;
		DatagramPacket dp = null;
		
		try {
			dp = new DatagramPacket(buffer, 0, BUF_SIZE);
			ds = new DatagramSocket(PORT);
			
			String dsIP = ds.getLocalAddress().getHostAddress();
			int dsPort = ds.getLocalPort();

			System.out.println("dsIP:dsPort - "+dsIP+":"+dsPort);
			
			String receivedIp = null;
			int receivedPort = 0;
			
			int i=0;
			while(i!=2){
				ds.receive(dp);
				System.out.println("*** received! ***");
				
				receivedIp = dp.getAddress().getHostAddress();
				receivedPort = dp.getPort();
				String msg = new String(dp.getData());
				
				System.out.println("ip:port - "+receivedIp+":"+receivedPort);
				System.out.println("msg : "+msg);
				
				queue[i++] = new User(receivedIp, receivedPort);
			}
			
			InetAddress addr = InetAddress.getByName(queue[1].ip);
			String msg = queue[0].ip+":"+queue[0].port;
			
			buffer = msg.getBytes();
			dp = new DatagramPacket(buffer, 0, buffer.length, addr, queue[1].port);
			ds.send(dp);
			System.out.println("send to "+queue[1].ip+":"+queue[1].port);
			
			addr = InetAddress.getByName(queue[0].ip);
			msg = queue[1].ip+":"+queue[1].port;
			
			buffer = msg.getBytes();
			dp = new DatagramPacket(buffer, 0, buffer.length, addr, queue[0].port);
			ds.send(dp);
			System.out.println("send to "+queue[0].ip+":"+queue[0].port);
			
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if(ds != null) ds.close();
		}
	}
}
