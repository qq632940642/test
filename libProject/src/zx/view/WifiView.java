package zx.view;

import zx.androidUtil.ResourceUtil;
import zx.androidUtil.TaskThread;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;


public class WifiView extends ImageView {
	private final static int WIFI_LEVEL_COUNT = 5;
	private int[] stats = new int[WIFI_LEVEL_COUNT];
	WifiManager mWifiManager;
	boolean bVisibility = false;
	boolean bAttached = false;
	boolean bRegister = false;
	private final static long wifiDelayTime = 3000;
	Context wifiContext = null;
	private String wifiPicName = "home_adv_wifi_level_img_";
	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				setWifiLevel();
				if (bVisibility && bAttached) {
					sendEmptyMessageDelayed(1, wifiDelayTime);
				}
			}
		};
	};
	
	public void setWifiPicName(String wifiPicName) {
		this.wifiPicName = wifiPicName;
		initWifiPic(getContext());
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		bAttached = true;
		registerWifi();
	}

	@Override
	protected void onDetachedFromWindow() {
		bAttached = false;
		unregisterWifi();
		super.onDetachedFromWindow();
	}

	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		if (changedView != this)
			return;
		bVisibility = visibility == View.VISIBLE;
		if (bVisibility) {
			registerWifi();
		} else {
			unregisterWifi();
		}
	}

	public WifiView(Context context) {
		super(context);
		Init(context);
	}

	public WifiView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Init(context);
	}

	public WifiView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Init(context);
	}

	private void Init(Context context) {
		wifiContext = context;
		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		bVisibility = getVisibility() == View.VISIBLE;
//		for (int i = 0; i < WIFI_LEVEL_COUNT; i++) {
//			stats[i] = ResourceUtil.getDrawableResId(context, "home_adv_wifi_level_img_" + i);//
//		}
		initWifiPic(context);
		setImageResource(stats[level]);
	}
	
	private void initWifiPic(Context context){
		for (int i = 0; i < WIFI_LEVEL_COUNT; i++) {
			stats[i] = ResourceUtil.getDrawableResId(context, wifiPicName + i);//
		}
	}

	public static boolean isWifiConnected(Context context) {
		if (context != null) {
			ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo wifiNetworkInfo = connectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			return wifiNetworkInfo.isConnected();
		} else
			return false;
	}

	int WifiLevel;
	int level = 2;
	boolean isWifiConnect = false;

	private void setWifiLevel() {
		if (wifiContext != null) {
			new TaskThread() {
				@Override
				public void doPost() {
					setImageResource(stats[level]);
				}

				@Override
				public void doBackUI() {

				}

				@Override
				public void doBack() {
					isWifiConnect = isWifiConnected(wifiContext);
					WifiLevel = mWifiManager.getConnectionInfo().getRssi();
					level = WifiManager.calculateSignalLevel(WifiLevel,
							WIFI_LEVEL_COUNT);
				}
			}.execute();

		}
	}

	protected void registerWifi() {
		if (!bRegister && bAttached && bVisibility) {
			bRegister = true;
			// IntentFilter wifiIntentFilter = new IntentFilter();
			// wifiIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
			// wifiIntentFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
			// wifiIntentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
			// wifiIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
			// wifiIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
			// wifiIntentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
			// getContext().registerReceiver(wifiIntentReceiver,
			// wifiIntentFilter);
			mHandler.sendEmptyMessage(1);
		}
	}

	protected void unregisterWifi() {
		if (bRegister && (!bAttached || !bVisibility)) {
			// getContext().unregisterReceiver(wifiIntentReceiver);
			mHandler.removeMessages(1);
			bRegister = false;
		}
	}

}
