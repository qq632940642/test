package zx.androidUtil;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.graphics.drawable.Drawable;
import android.net.Uri;

public class BitMapCache {

	private final static HashMap<String, SoftReference<Drawable>> mImageCacheByUri = new HashMap<String, SoftReference<Drawable>>();

	/**
	 * @category Í¼Æ¬×ÊÔ´»º³åÇø
	 * @param imgUri
	 * @return
	 */
	public static Drawable getImageFromUri(String imgUri) throws Exception {
		Drawable d = null;
		Uri mUri = null;
		if (imgUri == null)
			return null;
		if (imgUri.equalsIgnoreCase(""))
			return null;

		synchronized (mImageCacheByUri) {
			if (mImageCacheByUri.containsKey(imgUri)) {
				SoftReference<Drawable> ref = mImageCacheByUri.get(imgUri);
				if (ref != null) {
					d = ref.get();
				}
			}

			if (d == null && null != (mUri = Uri.parse(imgUri))) {
				File file = new File(imgUri);
				if (file.exists()) {
					if (file.isFile())
						d = Drawable.createFromPath(mUri.toString());
					if (d != null)
						mImageCacheByUri.put(imgUri,
								new SoftReference<Drawable>(d));
					else
						mImageCacheByUri.put(imgUri, null);
				}
				file = null;
			}
		}
		return d;
	}
}
