package com.example.commandtest;

import android.app.Activity;
import android.os.Bundle;

import com.example.commandtest.controller.Controller;

public class MainActivity extends Activity {

	private Controller controller;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		controller = new Controller(this);
	}
}
