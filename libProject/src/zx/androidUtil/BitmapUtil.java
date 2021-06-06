package zx.androidUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class BitmapUtil {
	public static BitmapDrawable drawableFromPath(String path) {
		if (path == null)
			return null;
		BitmapDrawable draw = null;
		File file = new File(path);
		FileInputStream is = null;
		try {
			if (file.exists() && file.isFile()) {
				BitmapFactory.Options opt = new BitmapFactory.Options();
				opt.inPreferredConfig = Bitmap.Config.RGB_565;// 表示16位位图
																// 565代表对应三原色占的位数
				opt.inInputShareable = true;
				opt.inPurgeable = true;// 设置图片可以被回收
				is = new FileInputStream(file);
				draw = new BitmapDrawable(BitmapFactory.decodeStream(is, null,
						opt));
				// return BitmapDrawable.createFromPath(path);
			} else {
				draw = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			draw = null;
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return draw;
	}

	public static Bitmap readBitmap(Context context, int id) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;// 表示16位位图 565代表对应三原色占的位数
		opt.inInputShareable = true;
		opt.inPurgeable = true;// 设置图片可以被回收
		InputStream is = context.getResources().openRawResource(id);
		return BitmapFactory.decodeStream(is, null, opt);
	}

	private static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	public static Bitmap getSimpleBitmap(String path) {
		Bitmap bmp = null;
		File imageFile = new File(path);
		if (imageFile.exists() && imageFile.isFile()) {

			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, opts);

			opts.inSampleSize = computeSampleSize(opts, -1, 128 * 128);
			opts.inJustDecodeBounds = false;
			try {
				bmp = BitmapFactory.decodeFile(path, opts);
			} catch (OutOfMemoryError err) {
				err.printStackTrace();
				bmp = null;
			}
		}
		imageFile = null;
		return bmp;
	}

	public static void recycleBitmap(Drawable d) {
		if (d == null)
			return;
		if (d instanceof BitmapDrawable) {
			BitmapDrawable bd = (BitmapDrawable) d;
			if (bd != null && bd.getBitmap() != null
					&& !bd.getBitmap().isRecycled()) {
				bd.setCallback(null);
				d.setCallback(null);
				bd.getBitmap().recycle();
				System.gc();
			}
		}
	}
}
