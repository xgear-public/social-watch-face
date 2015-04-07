package com.yobalabs.socialwf.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class BitmapUtils {
	
	public static final int EDGE = 400;

	
	public static Bitmap getSquareBitmap(Bitmap bitmap, int edge) {
		
		int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        if(height >= width) {
        	if(width > edge) {
        		bitmap = Bitmap.createScaledBitmap(bitmap, edge, (int) (((double)edge / width) * height), true);
        	}
        } else {
        	if(height > edge) {
        		bitmap = Bitmap.createScaledBitmap(bitmap, (int) (((double)(width * edge)) / height), edge, true);
        	}
        }
        
        if (bitmap.getWidth() >= bitmap.getHeight()){

        	bitmap = Bitmap.createBitmap(
        			  bitmap, 
        			  bitmap.getWidth()/2 - bitmap.getHeight()/2,
        	     0,
        	     bitmap.getHeight(), 
        	     bitmap.getHeight()
        	     );

        	}else{

        		bitmap = Bitmap.createBitmap(
        			  bitmap,
        	     0, 
        	     bitmap.getHeight()/2 - bitmap.getWidth()/2,
        	     bitmap.getWidth(),
        	     bitmap.getWidth() 
        	     );
        	}
        
		return bitmap;
	}
	
	public static Bitmap getCircleBitmap(Bitmap bitmapimg) {
        Bitmap output = Bitmap.createBitmap(bitmapimg.getWidth(),
                bitmapimg.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmapimg.getWidth(),
                bitmapimg.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmapimg.getWidth() / 2,
                bitmapimg.getHeight() / 2, bitmapimg.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmapimg, rect, rect, paint);
        return output;
	}
	
	public static ImageView machSquareBitmapToImage(ImageView imageView, Bitmap bitmap) {
		if(bitmap != null) {
			int imgWidth = imageView.getWidth();
			int bitmpWidth = bitmap.getWidth();
			
			if(imgWidth == 0) {
				LayoutParams params = imageView.getLayoutParams();
				imgWidth = params.width;
			}
			
			if(imgWidth != 0) {
				if(bitmpWidth > imgWidth) {
					imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, imgWidth, imgWidth, true));
				} else {
					imageView.setImageBitmap(bitmap);
				}
			}
		}
		return imageView;
	}
	
	public static boolean saveImageInternal(Context context, Bitmap bitmap, String fileName) {
		FileOutputStream fos = null;
		try {
			fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
			fos.write(stream.toByteArray());
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	
	public static Bitmap loadImageFromInternal(Context context, String fileName) {
		FileInputStream in = null;
		BufferedInputStream buf = null;
			try {
				in = context.openFileInput(fileName);
				buf = new BufferedInputStream(in);
	            byte[] bitMapA;
				bitMapA = new byte[buf.available()];
	            buf.read(bitMapA);
	            return BitmapFactory.decodeByteArray(bitMapA, 0, bitMapA.length);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (buf != null) {
					try {
						buf.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		return null;
		
	}
	
	public static boolean isImageExist(Context context, String fileName) {
		return context.getFileStreamPath(fileName).exists();
	}
	
	public static int calculateInSampleSize(Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}
	
	public static Bitmap loadInSampleBitmpa(String path) {
    	Options ops = new Options();
    	ops.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, ops);
        int imageHeight = ops.outHeight;
        int imageWidth = ops.outWidth;
        int minDimen = Math.min(imageHeight, imageWidth);
        float ratio = 1;
        if(minDimen > 500) {
        	ratio = (float)minDimen/500;
        	int reqWidth = (int) (((float)imageWidth)/ratio);
        	int reqHeight = (int) (((float)imageHeight)/ratio);
        	
        	ratio = BitmapUtils.calculateInSampleSize(ops,reqWidth, reqHeight);
        }
        Options loadOps = new Options();
        loadOps.inSampleSize = (int) ratio;
        loadOps.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, loadOps);
	}
}
