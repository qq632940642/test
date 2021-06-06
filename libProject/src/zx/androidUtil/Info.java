package zx.androidUtil;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;

import java.io.File;
import java.util.List;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class Info {
	public static final String DEFAULT_LANGUAGE = "zh";
	private static String MachMac = "00:00:00:00:00:00";
	private static Info configInfo;

	private Info() {
	}

	public static Info getInstance() {
		if (configInfo == null)
			configInfo = new Info();
		return configInfo;
	}

	public boolean SDExist() {
		return Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}

	public String getSDRootDic() {
		File dic = null;
		if (SDExist()) {
			dic = Environment.getExternalStorageDirectory();
			return dic.getName();
		} else
			return null;
	}

	public final String getSystemLang(Context context) {
		if (context != null)
			return context.getResources().getConfiguration().locale
					.getLanguage();
		else
			return DEFAULT_LANGUAGE;
	}

	public boolean CheckApkExist(Context context, String packName) {
		try {
			@SuppressWarnings("unused")
			PackageInfo info = context.getPackageManager().getPackageInfo(
					packName, 0);
			info = null;
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	/**
	 * 获取Mac地址
	 * 
	 * @return
	 */
	public String getMacAddress(Context context) {
		if ("00:00:00:00:00:00".equalsIgnoreCase(MachMac)) {
			final WifiManager wifi = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			final WifiInfo info = wifi.getConnectionInfo();
			if (info == null) {
				MachMac = info.getMacAddress();
				if (TextUtils.isEmpty(MachMac)) {
					MachMac = "00:00:00:00:00:00";
				}
			}
		}
		return MachMac;
	}

	/**
	 * 检查摄像头是否存在
	 * */
	public boolean cameraHardwareExist(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// 摄像头存在
			return true;
		} else {
			// 摄像头不存在
			return false;
		}
	}

	/**
	 * 检测服务是否运行
	 * 
	 * @param mContext
	 * @param className
	 * @return
	 */
	public boolean isServiceRunning(Context mContext, String className) {
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
				.getRunningServices(50);
		if (!(serviceList.size() > 0)) {
			return false;
		}
		for (int i = 0; i < serviceList.size(); i++) {
			if (serviceList.get(i).service.getClassName().equals(className)) {
				isRunning = true;
				break;
			}
		}
		return isRunning;
	}

	/**
	 * 检测App是否运行
	 * 
	 * @param context
	 * @param packName
	 * @return
	 */
	public boolean isRunApp(Context context, String packName) {
		boolean isRun = false;
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		KeyguardManager keyguardManager = (KeyguardManager) context
				.getSystemService(Context.KEYGUARD_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> processList = activityManager
				.getRunningAppProcesses();
		for (ActivityManager.RunningAppProcessInfo process : processList) {
			if (process.processName.startsWith(packName)) {
				boolean isBackground = process.importance != IMPORTANCE_FOREGROUND
						&& process.importance != IMPORTANCE_VISIBLE;
				boolean isLockedState = keyguardManager
						.inKeyguardRestrictedInputMode();
				if (isBackground || isLockedState)
					isRun = true;
				else
					isRun = false;
			}
		}
		return isRun;
	}

	/**
	 * @category 关闭软键盘
	 */
	public void closeSoftKeyboard(Context m, View v) {
		InputMethodManager imm = (InputMethodManager) m
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	public boolean isEnabledNetWork(Context context) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager.isWifiEnabled()) {
			return true;
		}
		return false;
	}

	public boolean isWifiConnect(Context context) {
		ConnectivityManager conMan = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.getState();
		return (wifi == State.CONNECTED || wifi == State.CONNECTING || wifi == State.UNKNOWN);
	}

	// 检查网络是否可用
	public boolean checkNetworkIsAvailable(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkinfo = manager.getActiveNetworkInfo();
		if (networkinfo == null || !networkinfo.isAvailable()) {// 当前网络不可用
			return false;
		} else {
			return true;
		}
	}

	//获取当前版本号 
	public String getVersionName(Context context) throws Exception {
		// 获取packagemanager的实例
		String versionName = "";
		//TODO
		return versionName;

	}

}
