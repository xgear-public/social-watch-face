package com.yobalabs.socialwf.view;

import com.yobalabs.socialwf.util.BitmapUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;


public class PreviewView extends ImageView {

	public PreviewView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PreviewView(Context context) {
		super(context);
	}

	public PreviewView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private int topX = 0, topY = 0;
	@Override
	protected void onDraw(Canvas canvas) {
		Drawable drawable = getDrawable();
		if (drawable == null) {
			return;
		}
		if (getWidth() == 0 || getHeight() == 0) {
			return;
		}

		if (drawable instanceof BitmapDrawable) {

			Bitmap b = ((BitmapDrawable) drawable).getBitmap();
			Bitmap roundBitmap = BitmapUtils.getCircleBitmap(b);
			if(roundBitmap.getWidth() < getWidth()) {
				int curW = getWidth();
				int bitmapW = roundBitmap.getWidth();
				topX = (int) ((double) curW / 2 - (double)bitmapW / 2);
				int curH = getHeight();
				int bitmapH = roundBitmap.getHeight();
				topY = (int) ((double) curH / 2 - (double)bitmapH / 2);
			} else {
				topX = 0;
				topY = 0;
			}
			canvas.drawBitmap(roundBitmap, topX, topY, null);
		} else
			super.onDraw(canvas);
	}

}
