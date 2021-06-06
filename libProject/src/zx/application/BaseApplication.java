package zx.application;

import zx.androidUtil.CrashHandler;
import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

public class BaseApplication extends Application{
	
	@Override
	public void onCreate() {
		super.onCreate();
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(this);
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		System.gc();
		System.gc();
		System.gc();
		Log.e("zx", "ÄÚ´æ²»×ã£¡");
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
}
