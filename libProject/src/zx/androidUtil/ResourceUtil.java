package zx.androidUtil;

import android.content.Context;

public class ResourceUtil {
	public static int getDrawableResId(Context context, String resName) {
		int resId = context.getResources().getIdentifier(resName, "drawable",
				context.getPackageName());
		if (resId == 0)
			System.err.println("Drawable[" + resName + "] not exists.");
		return resId;
	}
	
	public static int getLayoutResId(Context context, String resName){
		int resId = context.getResources().getIdentifier(resName, "layout", context.getPackageName());
		if (resId == 0)
			System.err.println("Layout[" + resName + "] not exists.");
		return resId;
	}
	public static int getIdResId(Context context, String resName){
		int resId = context.getResources().getIdentifier(resName, "id", context.getPackageName());
		if (resId == 0)
			System.err.println("Id[" + resName + "] not exists.");
		return resId;
	}
	public static int getAnimationResId(Context context, String resName){
		int resId = context.getResources().getIdentifier(resName, "anim", context.getPackageName());
		if (resId == 0)
			System.err.println("Anim[" + resName + "] not exists.");
		return resId;
	}
	
}
