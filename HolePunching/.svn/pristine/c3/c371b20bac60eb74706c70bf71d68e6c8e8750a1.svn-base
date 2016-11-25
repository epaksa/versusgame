package com.example.holepunching.data;

public class User {
	public String ip;
	public int port = 5678;
	public String privateIp;
	
	public User(String privateIp) {
		this.privateIp = privateIp;
	}
	
	public User(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	
	public boolean isSameNAT(){
		return privateIp != null;
	}
}
