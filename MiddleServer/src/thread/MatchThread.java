package thread;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

import main.Main;
import data.User;

public class MatchThread implements Runnable {
	
	public static final String MSG_SAME = "same";
	
	public String logFormat = "[TID ";
	public int tid;
	
	public MatchThread(int tid) {
		this.tid = tid;
		this.logFormat += tid+"] ";
	}

	@Override
	public void run() {
		try {
			matchClients();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void matchClients() throws IOException {
		User client1 = Main.userQueue.poll();
		User client2 = Main.userQueue.poll();
		
		if(client1 == null || client2 == null){
			printLog("fail to poll user!");
			return;
		}
		
		if(isSameNAT(client1, client2)){
			matchClientInSameNAT(client1, client2);
		}else{
			matchClient(client1, client2);
		}
	}

	private void matchClient(User client1, User client2) throws IOException {
		/* send client1's info to client2 */
		InetAddress addr = InetAddress.getByName(client2.ip);
		String msg = client1.ip+":"+client1.port;
		
		byte[] buffer = msg.getBytes();
		
		DatagramPacket dp = new DatagramPacket(buffer, 0, buffer.length, addr, client2.port);
		Main.ds.send(dp);
		printLog("### match start (different NAT) ###");
		printLog("send to "+client2.ip+":"+client2.port);
		
		/* send client2's info to client1 */
		addr = InetAddress.getByName(client1.ip);
		msg = client2.ip+":"+client2.port;
		
		buffer = msg.getBytes();
		dp = new DatagramPacket(buffer, 0, buffer.length, addr, client1.port);
		Main.ds.send(dp);
		printLog("send to "+client1.ip+":"+client1.port);
		printLog("### match finish (different NAT) ###"+Main.NEW_LINE);
	}

	private void matchClientInSameNAT(User client1, User client2) throws IOException {
		/* send client1's info to client2 */
		InetAddress addr = InetAddress.getByName(client2.ip);
		String msg = client1.privateIp+":"+client1.port+MSG_SAME;
		
		byte[] buffer = msg.getBytes();
		
		DatagramPacket dp = new DatagramPacket(buffer, 0, buffer.length, addr, client2.port);
		Main.ds.send(dp);
		printLog("### match start (same NAT) ###");
		printLog("send to "+client2.ip+":"+client2.port);
		
		/* send client2's info to client1 */
		addr = InetAddress.getByName(client1.ip);
		msg = client2.privateIp+":"+client2.port+MSG_SAME;
		
		buffer = msg.getBytes();
		dp = new DatagramPacket(buffer, 0, buffer.length, addr, client1.port);
		Main.ds.send(dp);
		printLog("send to "+client1.ip+":"+client1.port);
		printLog("### match finish (same NAT) ###"+Main.NEW_LINE);
	}
	
	private boolean isSameNAT(User client1, User client2) {
		return client1.ip.equals(client2.ip);
	}
	
	private void printLog(String msg){
		System.out.println(logFormat+msg);
	}
}
