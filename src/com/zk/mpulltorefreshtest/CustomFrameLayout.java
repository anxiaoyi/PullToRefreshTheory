package com.zk.mpulltorefreshtest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

public class CustomFrameLayout extends FrameLayout {

	private float mLastMotionX, mLastMotionY;
	private float mDeltaX, mDeltaY;
	
	private ScrollToHomeRunnable mScrollToHomeRunnable;
	
	private enum State{
		REFRESHING,
		PULLING_HORIZONTAL,
		PULLING_VERTICAL,
		NORMAL,
	}
	
	private enum Orientation{
		HORIZONTAL,
		VERTICAL
	}
	
	private State mState;
	private Orientation mOrientation;
	
	@SuppressLint("NewApi")
	public CustomFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public CustomFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CustomFrameLayout(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context){
		mState = State.NORMAL;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(mState == State.REFRESHING){
			return true;
		}
		
		int action = event.getAction();
		switch(action){
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = event.getX();
			mLastMotionY = event.getY();
			mDeltaY = .0F;
			mDeltaX = .0F;
			break;
			
		case MotionEvent.ACTION_MOVE:
			float innerDeltaY = event.getY() - mLastMotionY;
			float innerDeltaX = event.getX() - mLastMotionX;
			float absInnerDeltaY = Math.abs(innerDeltaY);
			float absInnerDeltaX = Math.abs(innerDeltaX);
			
			if(absInnerDeltaY > absInnerDeltaX && mState != State.PULLING_HORIZONTAL){
				mOrientation = Orientation.VERTICAL;
				mState = State.PULLING_VERTICAL;
				if(innerDeltaY > 1.0F){
					mDeltaY -= absInnerDeltaY;
					pull(mDeltaY);
				}else if(innerDeltaY < -1.0F){
					mDeltaY += absInnerDeltaY;
					pull(mDeltaY);
				}
			}else if(absInnerDeltaY < absInnerDeltaX && mState != State.PULLING_VERTICAL){
				mOrientation = Orientation.HORIZONTAL;
				mState = State.PULLING_HORIZONTAL;
				if(innerDeltaX > 1.0F){
					mDeltaX -= absInnerDeltaX;
					pull(mDeltaX);
				}else if(innerDeltaX < -1.0F){
					mDeltaX += absInnerDeltaX;
					pull(mDeltaX);
				}
			}
			
			mLastMotionX = event.getX();
			mLastMotionY = event.getY();
			break;
			
		case MotionEvent.ACTION_UP:
			switch(mOrientation){
			case VERTICAL:
				smoothScrollTo(mDeltaY);
				break;
				
			case HORIZONTAL:
				smoothScrollTo(mDeltaX);
				break;
				
			default:
				break;
			}
			break;
		}
		return true;
	}
	
	private void pull(float diff){
		int value = Math.round(diff / 2.0F);
		if(mOrientation == Orientation.VERTICAL){
			scrollTo(0, value);
		}else if(mOrientation == Orientation.HORIZONTAL){
			scrollTo(value, 0);
		}
	}
	
	private void smoothScrollTo(float diff){
		int value = Math.round(diff / 2.0F);
		mScrollToHomeRunnable = new ScrollToHomeRunnable(value, 0);
		mState = State.REFRESHING;
		post(mScrollToHomeRunnable);
	}
	
	final class ScrollToHomeRunnable implements Runnable{
		
		private final Interpolator mInterpolator;
		private int target;
		private int current;
		private long mStartTime = -1;
		
		public ScrollToHomeRunnable(int current, int target){
			this.target = target;
			this.current = current;
			mInterpolator = new DecelerateInterpolator();
		}
		
		@Override
		public void run() {
			if(mStartTime == -1){
				mStartTime = System.currentTimeMillis();
			}else{
				long normalizedTime = (1000 * (System.currentTimeMillis() - mStartTime)) / 200;
				normalizedTime = Math.max(Math.min(normalizedTime, 1000), 0);

				final int delta = Math.round((current - target)
						* mInterpolator.getInterpolation(normalizedTime / 1000f));
				
				current = current - delta;
				
				if(mOrientation == Orientation.HORIZONTAL){
					scrollTo(current, 0);
				}else if(mOrientation == Orientation.VERTICAL){
					scrollTo(0, current);
				}
			}
			
			if(current != target){
				postDelayed(this, 16);
			}else{
				mState = State.NORMAL;
			}
		}
	}
}
