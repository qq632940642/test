package zx.view;

import java.util.Calendar;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.libproject.R;

public class ClockView extends View {

	private int Hour = 1, Minute = 1, Second = 1;
	private int Day = 1;
	private int hHour, wHour;
	private int hMinute, wMinute;
	private int hSecond, wSecond;
	private int centerX, centerY;
	private Paint mPaint = new Paint();

	private BitmapDrawable mHourBmp, mMinuteBmp, mSecondBmp;
	Calendar c = Calendar.getInstance();

	private final static int CLOCK_UPDATE = 1;

	boolean bUpdating = false;
	boolean bVisibility = false;

	void updateClock() {
		boolean handle = handler.hasMessages(CLOCK_UPDATE);
		if (bUpdating && bVisibility) {
			if (!handle)
				handler.sendEmptyMessage(CLOCK_UPDATE);
		} else {
			if (!handle)
				handler.removeMessages(CLOCK_UPDATE);
		}
	}

	@Override
	protected void onAttachedToWindow() {
		bUpdating = true;
		updateClock();
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		bUpdating = false;
		updateClock();
		super.onDetachedFromWindow();
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);

		if (visibility == View.VISIBLE) {
			bVisibility = true;
			updateClock();
		} else {
			bVisibility = false;
			updateClock();
		}
	}

	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CLOCK_UPDATE: {
				c.setTimeInMillis(System.currentTimeMillis());
				Day = c.get(Calendar.DAY_OF_MONTH);
				Hour = c.get(Calendar.HOUR);
				Minute = c.get(Calendar.MINUTE);
				Second = c.get(Calendar.SECOND);

				invalidate();
				if (bVisibility && bUpdating)
					sendEmptyMessageDelayed(CLOCK_UPDATE, 1000);
			}
				break;
			}
		}
	};

	public ClockView(Context context) {
		super(context);
		onFinishInflate();
	}

	public ClockView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mHourBmp = new BitmapDrawable(BitmapFactory.decodeResource(
				getResources(), R.drawable.hour));
		mMinuteBmp = new BitmapDrawable(BitmapFactory.decodeResource(
				getResources(), R.drawable.minute));
		mSecondBmp = new BitmapDrawable(BitmapFactory.decodeResource(
				getResources(), R.drawable.second));

		wHour = mHourBmp.getIntrinsicWidth() / 2;
		hHour = mHourBmp.getIntrinsicHeight() / 2;

		wMinute = mMinuteBmp.getIntrinsicWidth() / 2;
		hMinute = mMinuteBmp.getIntrinsicHeight() / 2;

		wSecond = mSecondBmp.getIntrinsicWidth() / 2;
		hSecond = mSecondBmp.getIntrinsicHeight() / 2;

		mPaint.setTextSize(15);
		mPaint.setColor(0xff000000);
		mPaint.setAntiAlias(true);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		if (w > 0 && h > 0) {
			centerX = (w >> 1) - 5;
			centerY = (h >> 1);

			mHourBmp.setBounds(centerX - wHour, centerY - hHour, centerX
					+ wHour, centerY + hHour);
			mMinuteBmp.setBounds(centerX - wMinute, centerY - hMinute, centerX
					+ wMinute, centerY + hMinute);
			mSecondBmp.setBounds(centerX - wSecond, centerY - hSecond, centerX
					+ wSecond, centerY + hSecond);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawText(String.format("%d", Day), 232, 162, mPaint);

		// canvas.drawARGB(0xff, 0x22, 0x33, 0x65);
		float hourRotate = Hour * 30.0f + Minute / 2.0f;// (60.0f * 30.0f)
//		float minuteRotate = Minute * 6.0f;
		float minuteRotate = Minute * 6.0f+(Second*6.0f)/60.0f;
		Log.e("zx", "minuteRotate:"+minuteRotate);
		float secondRotate = Second * 6.0f;

		canvas.drawARGB(0x00, 0, 0, 0);

		// Draw hour on view

		canvas.save();// Save non rotated canvas
		canvas.rotate(hourRotate, centerX, centerY);
		mHourBmp.draw(canvas);
		canvas.restore(); // restore canvas to last saved state

		// Draw Minute on view
		canvas.save();
		canvas.rotate(minuteRotate, centerX, centerY);
		mMinuteBmp.draw(canvas);
		canvas.restore();

		// Draw Second on view
		canvas.save();
		canvas.rotate(secondRotate, centerX, centerY);
		mSecondBmp.draw(canvas);
		canvas.restore();

	}
}
