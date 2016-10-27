package com.example.commandtest;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.example.commandtest.controller.Controller;

public class MainActivity extends Activity {

	public boolean set;
	
	private Controller[] controller;
	private FrameLayout field;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		field = (FrameLayout) findViewById(R.id.field);

		controller = new Controller[2];
		controller[0] = new Controller(this);
		controller[1] = new Controller(this);
		controller[0].setPlayer(R.id.controller_1p, R.id.man_1p, true, Color.GREEN);
		controller[1].setPlayer(R.id.controller_2p, R.id.man_2p, false, Color.BLUE);
	}

	public void setPlayerPosition() {
		if(set) return;
		
		int width = field.getWidth();

		View player = findViewById(R.id.man_1p);
		player.setX((float) (width * 0.3));
		player = findViewById(R.id.man_2p);
		player.setX((float) (width * 0.7));
		
		set = true;
	}

}
