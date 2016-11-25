package main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import thread.MatchThread;
import data.User;

public class Main {

	public static final String NEW_LINE = System.getProperties().getProperty("line.separator");
	public static final int PORT = 5678;
	public static final int BUF_SIZE = 65507;
	public static final int QUEUE_CAPACITY = 1000;
	
	public static final Queue<User> userQueue = new ArrayBlockingQueue<User>(QUEUE_CAPACITY);
	
	public static DatagramSocket ds;
	
	static int clientCount = 1;
	static int threadCount = 1;
	
	public static void main(String[] args) {
		try {
			ds = new DatagramSocket(PORT);
			System.out.println("= Server start =");
			
			while(true){
				DatagramPacket dp = new DatagramPacket(new byte[BUF_SIZE], 0, BUF_SIZE);
				ds.receive(dp);
				
				System.out.println("#### received from client ("+(clientCount++)+") ####");
				User user = getUserInfo(dp);
				
				if(userQueue.size() != QUEUE_CAPACITY){
					userQueue.add(user);
				}
				
				if(userQueue.size() >= 2){
					new MatchThread(threadCount++).run();
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if(ds != null) ds.close();
		}
	}

	private static User getUserInfo(DatagramPacket dp) {
		String receivedIp = dp.getAddress().getHostAddress();
		int receivedPort = dp.getPort();
		String msg = new String(dp.getData());
		
		System.out.println("public Ip:port -> "+receivedIp+":"+receivedPort);
		System.out.println("msg -> "+msg+NEW_LINE);
		
		String clientPrivateIP = getPrivateIP(msg);
		
		return new User(receivedIp, receivedPort, clientPrivateIP);
	}

	private static String getPrivateIP(String msg) {
		String[] splited = msg.split("is ");
		
		if(splited.length != 2){
			System.out.println("invalid message From client");
			return null;
		}
		
		return splited[1];
	}
}
