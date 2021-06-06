package zx.androidUtil;

import android.os.AsyncTask;
import android.os.Handler;

public abstract class TaskThread extends AsyncTask<Void, Void, Void> {

	private Handler handler = new Handler();

	/**
	 * ��̨����UI
	 */
	public abstract void doBackUI();

	/**
	 * ��̨����,��UI
	 */
	public abstract void doBack();

	/**
	 * ����UI
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
