package com.example.commandtest.controller;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

import com.example.commandtest.MainActivity;
import com.example.commandtest.R;

public class Controller implements OnTouchListener, OnClickListener {

	/*
	 * 1. 1000/30 이런거 변수로 빼야됨. 2. test 코드만들 방법
	 */

	private static final int MOVE_LEFT = 0;
	private static final int MOVE_RIGHT = 1;

	private MainActivity activity;
	private View[] btn;
	private View man;

	private boolean pressed;
	private MoveThread thread;
	private SlashHandler handler;

	public Controller(MainActivity activity) {
		this.activity = activity;

		btn = new View[2];
		this.btn[0] = activity.findViewById(R.id.move);
		this.btn[1] = activity.findViewById(R.id.slash);

		this.btn[0].setOnTouchListener(this);
		this.btn[1].setOnClickListener(this);

		this.man = activity.findViewById(R.id.man);

		this.thread = new MoveThread();
		this.handler = new SlashHandler();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int eventType = event.getAction();

		int x = (int) event.getX();
		int y = (int) event.getY();

		int width = v.getWidth();
		int centerOfX = v.getLeft() + width / 2;
		int left = v.getLeft();
		int right = v.getRight();
		int top = v.getTop();
		int bottom = v.getBottom();

		switch (eventType) {
		case MotionEvent.ACTION_DOWN:
			if (isInLeftside(x, centerOfX, left)) {
				thread.direction = MOVE_LEFT;
			} else if (isInRightside(x, centerOfX, right)) {
				thread.direction = MOVE_RIGHT;
			}
			pressed = true;
			Log.i("ttt", "down : " + x);
			AsyncTask.execute(thread);
			break;

		case MotionEvent.ACTION_MOVE:
			if (!pressed) {
				return true;
			} else if (!isValidYPosition(y, top, bottom)) {
				Log.i("ttt", "cant move");
				pressed = false;
			} else if (isInLeftside(x, centerOfX, left)) {
				if (thread.direction == MOVE_RIGHT)
					thread.direction = MOVE_LEFT;
			} else if (isInRightside(x, centerOfX, right)) {
				if (thread.direction == MOVE_LEFT)
					thread.direction = MOVE_RIGHT;
			} else {
				Log.i("ttt", "cant move");
				pressed = false;
			}
			break;

		case MotionEvent.ACTION_UP:
			Log.i("ttt", "up b");
			pressed = false;
			break;

		default:
			break;
		}

		return true;
	}

	@Override
	public void onClick(View v) {
		man.setBackgroundColor(Color.RED);
		handler.sendEmptyMessageDelayed(0, 1000 / 30 * 10);
	}

	private boolean canMove(float x) {
		if (thread.direction == MOVE_LEFT)
			return x >= 10;
		else if (thread.direction == MOVE_RIGHT)
			return x <= 1210;
		return false;
	}

	private boolean isValidYPosition(int y, int top, int bottom) {
		return y >= top && y <= bottom;
	}

	private boolean isInLeftside(int x, int centerOfX, int left) {
		return x <= centerOfX && x >= left;
	}

	private boolean isInRightside(int x, int centerOfX, int right) {
		return x > centerOfX && x <= right;
	}

	private void startMove(int direction) {
		float x = man.getX();

		Log.i("ttt", "startMove : " + x);

		switch (direction) {
		case MOVE_LEFT:
			x -= 10;
			break;
		case MOVE_RIGHT:
			x += 10;
			break;
		default:
			break;
		}

		man.setX(x);
	}

	private class MoveThread implements Runnable {

		private int direction;
		private Runnable runnable;

		public MoveThread() {
			runnable = new Runnable() {
				@Override
				public void run() {
					if (canMove(man.getX())) {
						startMove(direction);
					}
				}
			};
		}

		@Override
		public void run() {
			try {
				while (pressed) {
					Thread.sleep(1000 / 30);
					activity.runOnUiThread(runnable);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private class SlashHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			man.setBackgroundColor(Color.GREEN);
		}
	}
}
