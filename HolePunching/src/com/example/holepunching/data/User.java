package com.example.holepunching.data;

public class User {
	
	public String ip;
	public int port;
	public boolean sameNAT;
	
	public User(String ip, int port, boolean sameNAT) {
		this.ip = ip;
		this.port = port;
		this.sameNAT = sameNAT;
	}
	
	public boolean isSameNAT() {
		return sameNAT;
	}
}
