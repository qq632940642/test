package zx.androidUtil;

import android.os.AsyncTask;
import android.os.Handler;

public abstract class TaskThread extends AsyncTask<Void, Void, Void> {

	private Handler handler = new Handler();

	/**
	 * 后台操作UI
	 */
	public abstract void doBackUI();

	/**
	 * 后台运行,非UI
	 */
	public abstract void doBack();

	/**
	 * 更新UI
	 */
	public abstract void doPost();

	@Override
	protected Void doInBackground(Void... params) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				doBackUI();
			}
		});
		doBack();
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		handler.post(new Runnable() {
			@Override
			public void run() {
				doPost();
			}
		});
	}
	
}
