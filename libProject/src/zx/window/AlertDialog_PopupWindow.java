package zx.window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.example.libproject.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * @功能:简化版的自定义弹出框
 * @描述:这是一个基于popupWindow设计而成的,支持屏蔽其他控件响应（当然你也可以选择不屏蔽）；最大的特点在于该对话框的灵活性很强，比如说： 
 * 支持弹出的位置自定义 ，支持屏蔽其他控件 （ 只要你不点击我弹出对话框上面的确定或者取消按钮 ，则当前对话框一直存在，同时屏蔽其他
     控件 ），支持获得按钮对象（满足用户修改按键背景或者标题的操作 ）
     
 * @see setTopPadding
 * @see setBackGroundDrawable
 * @see setBtnsHeigth
 * @author 宋雅军
 * 
 */
@SuppressLint("NewApi")
public class AlertDialog_PopupWindow {

	private View view;
	private MyPopupWindow myPopUpWindow = null;
	private AlertDialog_PopupWindow dialog = null;
	private Context context;
	private boolean isOutTouchable = false;

	public interface OnFinishListener {
		void onFinish(Object obj);
	}

	// HashMap<String, Object> map = new HashMap<String, Object>();
	/**
	 * @解释: 该构造函数只有一个参数，默认弹出来的对话框后屏蔽外面点击事件（也就是说，只有当用户点击我的确定或者取消按钮对话框才会消失），即
	 *      isOutTouchable默认值为false
	 * @param context
	 *            上下文
	 * @return 对话框对象
	 */
	public AlertDialog_PopupWindow(Context context) {
		this.context = context;
		dialog = this;
		view = LayoutInflater.from(context).inflate(
				R.layout.alert_dialog_popup_window, null);
//		init();
	}

	/**
	 * @param context
	 *            上下文
	 * @param isOutTouchable
	 *            为true则在对话框外点击是对话框会自动消失，否则只有点击确定或者取消按钮时才会消失
	 * @return 对话框对象
	 */
	public AlertDialog_PopupWindow(Context context, boolean isOutTouchable) {
		this.isOutTouchable = isOutTouchable;
		this.context = context;
		dialog = this;
		view = LayoutInflater.from(context).inflate(
				R.layout.alert_dialog_popup_window, null);
//		init();
	}

//	private void init() {
//		int[] hideViewId = { R.id.alertDialogPopupWindow_LLoutTitleLayoutId,
//				R.id.alertDialogPopupWindow_IconContentId,
//				R.id.alertDialogPopupWindow_tvTitleId,
//				R.id.alertDialogPopupWindow_tvMessageId,
//				R.id.alertDialogPopupWindow_gvMenuId,
//				R.id.alertDialogPopupWindow_tvPositiveId,
//				R.id.alertDialogPopupWindow_tvNegativeId };
//		for (int i = 0; i < hideViewId.length; i++) {
//			view.findViewById(hideViewId[i]).setVisibility(View.GONE);
//		}
//	}

	// public AlertDialog_PopupWindow SaveData(String Key, Object obj){
	//
	// map.put(Key, obj);
	// return dialog;
	// }
	//
	// public Object GetData(String Key){
	// return map.get(Key);
	// }

	//代码报错？？？ 
	public AlertDialog_PopupWindow setTopPadding(int topPadding) {
		view.setTop(topPadding);
		return dialog;
	}

	public AlertDialog_PopupWindow setBackGroundDrawable(int resid) {
		view.setBackgroundResource(resid);
		return dialog;
	}

	public AlertDialog_PopupWindow setBackGroundColor(int color) {
		view.setBackgroundColor(color);
		return dialog;
	}

	/**
	 * @expression:设置两个按钮的高度
	 * @param positiveBtnHeight
	 * @param negativeBtnHeight
	 * @return
	 */
	public AlertDialog_PopupWindow setBtnsParams(int positiveTextSize,
			int positiveBtnHeight, int negativeTextSize, int negativeBtnHeight) {

		final Button btnPositive = (Button) view
				.findViewById(R.id.alertDialogPopupWindow_tvPositiveId);
		final Button btnNegative = (Button) view
				.findViewById(R.id.alertDialogPopupWindow_tvNegativeId);

		btnPositive.setTextSize(positiveTextSize);
		btnNegative.setTextSize(negativeTextSize);
		LinearLayout.LayoutParams params = (LayoutParams) btnPositive
				.getLayoutParams();
		params.height = positiveBtnHeight;
		btnPositive.setLayoutParams(params);

		params = (LayoutParams) btnNegative.getLayoutParams();
		params.height = negativeBtnHeight;
		btnNegative.setLayoutParams(params);
		return dialog;
	}

	private boolean isClickMissing_Positive = true;
	private OnFinishListener positiveFinishListener = null;

	public AlertDialog_PopupWindow setPositiveButton(boolean isClickMissing,
			String title, OnFinishListener onFinishListener) {
		isClickMissing_Positive = isClickMissing;
		positiveFinishListener = onFinishListener;
		final Button btnPositive = (Button) view
				.findViewById(R.id.alertDialogPopupWindow_tvPositiveId);
		if (title != null) {
			btnPositive.setText(title);
		}
		btnPositive.setVisibility(View.VISIBLE);
		btnPositive.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isClickMissing_Positive) {
					dialog.dissMiss();
				}
				if (positiveFinishListener != null) {
					positiveFinishListener.onFinish(dialog);
				}
			}
		});

		return dialog;
	}

	private boolean isClickMissing_negative = true;
	private OnFinishListener negativeListener = null;

	public AlertDialog_PopupWindow setNegativeButton(boolean isClickMissing,
			String title, OnFinishListener onFinishListener) {
		isClickMissing_negative = isClickMissing;
		negativeListener = onFinishListener;
		final Button btnNegative = (Button) view
				.findViewById(R.id.alertDialogPopupWindow_tvNegativeId);
		if (title != null) {
			btnNegative.setText(title);
		}
		btnNegative.setVisibility(View.VISIBLE);
		btnNegative.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isClickMissing_negative) {
					dialog.dissMiss();
				}

				if (negativeListener != null) {
					negativeListener.onFinish(dialog);
				}
			}
		});
		return dialog;
	}

	public AlertDialog_PopupWindow setIcon(int resId) {
		View iconLayout = view
				.findViewById(R.id.alertDialogPopupWindow_IconContentId);

		ImageView iv = (ImageView) view
				.findViewById(R.id.alertDialogPopupWindow_IconId);
		iv.setBackgroundResource(resId);
		iconLayout.setVisibility(View.VISIBLE);

		View layout = view
				.findViewById(R.id.alertDialogPopupWindow_LLoutTitleLayoutId);
		layout.setVisibility(View.VISIBLE);
		return dialog;
	}

	public AlertDialog_PopupWindow setIcon(int resId, int width, int height,
			int leftPading, int toppadding) {
		View iconLayout = view
				.findViewById(R.id.alertDialogPopupWindow_IconContentId);
		iconLayout.setVisibility(View.VISIBLE);
		ImageView iv = (ImageView) iconLayout
				.findViewById(R.id.alertDialogPopupWindow_IconId);
		iv.setBackgroundResource(resId);

		LayoutParams lp = (LayoutParams) iv.getLayoutParams();
		lp.width = width;
		lp.height = height;
		iv.setLayoutParams(lp);

		iconLayout.setPadding(leftPading, toppadding, 0, 0);
		View layout = view
				.findViewById(R.id.alertDialogPopupWindow_LLoutTitleLayoutId);
		layout.setVisibility(View.VISIBLE);

		return dialog;
	}

	public AlertDialog_PopupWindow setTitle(String title, int titleheight,
			int titleLeftPadding, int titleTextSize, int titleTextColor) {
		TextView tvTitle = (TextView) view
				.findViewById(R.id.alertDialogPopupWindow_tvTitleId);
		if (titleTextSize > 0) {
			tvTitle.setTextSize(titleTextSize);
		}

		if (titleTextColor != -1) {
			tvTitle.setTextColor(titleTextColor);
		}

		View layout = view
				.findViewById(R.id.alertDialogPopupWindow_LLoutTitleLayoutId);
		if (titleheight > 0) {
			LayoutParams lp = (LayoutParams) layout.getLayoutParams();
			lp.height = titleheight;
			layout.setLayoutParams(lp);
		}

		tvTitle.setText(title);
		tvTitle.setVisibility(View.VISIBLE);
		layout.setVisibility(View.VISIBLE);
		return dialog;
	}

	public AlertDialog_PopupWindow setTitle(String title) {
		View layout = view
				.findViewById(R.id.alertDialogPopupWindow_LLoutTitleLayoutId);

		TextView tvTitle = (TextView) view
				.findViewById(R.id.alertDialogPopupWindow_tvTitleId);
		tvTitle.setText(title);
		tvTitle.setVisibility(View.VISIBLE);
		layout.setVisibility(View.VISIBLE);
		return dialog;
	}

	public AlertDialog_PopupWindow setMessage(String Message) {
		TextView tvMessage = (TextView) view
				.findViewById(R.id.alertDialogPopupWindow_tvMessageId);
		tvMessage.setText(Message);
		tvMessage.setVisibility(View.VISIBLE);
		return dialog;
	}

	/**
	 * @注意： 
	 *      参数view如果是从xml文件通过inflate创建出来的，需要注意：xml文件的最外层将会被inflate修改为wrap_content
	 *      也就是说，如果最外层是绝对高度或者绝对宽度时候需要注意在此层外面再套一层布局，不然会出现高度和宽度（宽度到没发现不受控）不受控
	 * @解释：至于为什么会被修改为wrap_content,是因为inflate的带二个参数（root）为null导致的
	 * @param view
	 * @return
	 */
	public AlertDialog_PopupWindow setView(View view) {
		LinearLayout linearLayout = (LinearLayout) this.view
				.findViewById(R.id.alertDialogPopupWindow_viewContentId);
		linearLayout.setVisibility(View.VISIBLE);
		linearLayout.removeAllViews();
		linearLayout.addView(view);
		return dialog;
	}

	/**
	 * @param view
	 *            需要添加到dialog当中的view
	 * @param width
	 *            指定view的宽度
	 * @param height
	 *            指定view的高度
	 * @return
	 */
	public AlertDialog_PopupWindow setView(View view, int width, int height) {
		LinearLayout linearLayout = (LinearLayout) this.view
				.findViewById(R.id.alertDialogPopupWindow_viewContentId);
		linearLayout.setVisibility(View.VISIBLE);
		linearLayout.removeAllViews();
		linearLayout.addView(view, width, height);
		return dialog;
	}

	private boolean isClickMissing_items = false;
	OnFinishListener gvMenu_onFinishListener = null;

	/**
	 * @说明: onFinishListener所带的返回参数是所点击的item位置（int position）
	 * @param items
	 *            需要显示的数据项
	 * @param colums
	 *            以几列的形式显示
	 * @param onFinishListener
	 *            监听器
	 * @return AlertDialog_PopupWindow
	 */
	public AlertDialog_PopupWindow setItems(boolean isClickMissing,
			String[] items, int colums, OnFinishListener onFinishListener) {
		isClickMissing_items = isClickMissing;
		gvMenu_onFinishListener = onFinishListener;
		GridView gvMenu = (GridView) view
				.findViewById(R.id.alertDialogPopupWindow_gvMenuId);
		gvMenu.setVisibility(View.VISIBLE);
		gvMenu.setNumColumns(colums);
		SimpleAdapter adapter = new SimpleAdapter(context,
				changeStrsToListMaps(items), R.layout.one_text_view_item_15sp,
				new String[] { "item" },
				new int[] { R.id.oneTextViewItem_12sp_tvId });
		gvMenu.setAdapter(adapter);
		gvMenu.setOnItemClickListener(onItemClickListener);
		return dialog;
	}

	/**
	 * @说明: onFinishListener所带的返回参数是所点击的item位置（int position）
	 * @param items
	 *            需要显示的数据项
	 * @param colums
	 *            以几列的形式显示
	 * @textSize textSize item字体大小
	 * @param itemsBackGroudDrawableId
	 *            item的背景图片的缓冲区
	 * @param onFinishListener
	 *            监听器
	 * @return AlertDialog_PopupWindow
	 */
	public AlertDialog_PopupWindow setItems(boolean isClickMissing,
			String[] items, int colums, int textSize,
			int[] itemsBackGroudDrawableId, OnFinishListener onFinishListener) {
		isClickMissing_items = isClickMissing;
		gvMenu_onFinishListener = onFinishListener;
		GridView gvMenu = (GridView) view
				.findViewById(R.id.alertDialogPopupWindow_gvMenuId);
		gvMenu.setVisibility(View.VISIBLE);
		gvMenu.setNumColumns(colums);
		LocalAdapter localAdapter = new LocalAdapter(items, textSize,
				itemsBackGroudDrawableId);
		gvMenu.setAdapter(localAdapter);
		gvMenu.setOnItemClickListener(onItemClickListener);
		return dialog;
	}

	private int IconLeftPadding = -1;
	private int IconWidth = -1;

	public AlertDialog_PopupWindow SetItemIconParam(int leftPading, int width) {
		this.IconLeftPadding = leftPading;
		this.IconWidth = width;
		return dialog;
	}

	private int itemHeight = -1;

	public AlertDialog_PopupWindow SetItemHeight(int itemHeight) {
		this.itemHeight = itemHeight;
		return dialog;
	}

	OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			if (isClickMissing_items) {
				dialog.dissMiss();
			}

			if (gvMenu_onFinishListener != null) {
				gvMenu_onFinishListener.onFinish(position);
			}
		}
	};

	private ArrayList<Map<String, Object>> changeStrsToListMaps(String[] items) {
		ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < items.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("item", items[i]);
			list.add(map);
		}

		return list;
	}

	/**
	 * @param view
	 *            dialog相对于谁的位置偏移
	 * @param x
	 *            位置偏移
	 * @param y
	 *            位置偏移
	 * @param width
	 *            对话框的宽度
	 */
	public AlertDialog_PopupWindow show(View view, int x, int y, int width) {
		myPopUpWindow = null;
		myPopUpWindow = new MyPopupWindow(this.view, width, isOutTouchable);
		myPopUpWindow.showWindow(view, Gravity.NO_GRAVITY, x, y);
		return dialog;
	}

	/**
	 * @param view
	 *            dialog相对于谁的位置偏移
	 * @param x
	 *            x位置偏移
	 * @param y
	 *            y位置偏移
	 */
	public AlertDialog_PopupWindow show(View view, int x, int y) {
		Log.i("zx", "line 458.....................");
		myPopUpWindow = new MyPopupWindow(this.view, isOutTouchable);
		Log.i("zx", "line 460.....................");
		myPopUpWindow.showWindow(view, Gravity.NO_GRAVITY, x, y);
		return dialog;
	}

	public void dissMiss() {
		myPopUpWindow.disMiss();
	}

	class MyPopupWindow extends PopupWindow {
		View _localView = null;
		public PopupWindow _popUpWindow = null;

		public MyPopupWindow(View view, boolean isOutsideTouchable) {
			Log.i("zx", "line 474.....................");
			_localView = view;
			_popUpWindow = new PopupWindow(view,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			if (isOutsideTouchable) {
				_popUpWindow.setBackgroundDrawable(new BitmapDrawable());// 设置背景图片,不能为空
			}

			_popUpWindow.setFocusable(true);
			_popUpWindow.setOutsideTouchable(true);
		}

		@SuppressWarnings("deprecation")
		public MyPopupWindow(View view, int width, boolean isOutsideTouchable) {
			_localView = view;
			_popUpWindow = new PopupWindow(_localView, width,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			if (isOutsideTouchable) {
				_popUpWindow.setBackgroundDrawable(new BitmapDrawable());// 设置背景图片,不能为空
			}

			_popUpWindow.setFocusable(true);
			_popUpWindow.setOutsideTouchable(true);
		}

		public void showWindow(View layoutView, int gravity, int xOff, int yOff) {
			_popUpWindow.showAtLocation(layoutView, gravity, xOff, yOff);
		}

		public void disMiss() {
			_popUpWindow.dismiss();
		}
	}

	class LocalAdapter extends BaseAdapter {
		private String[] _items = null;
		private int[] _itemsBackGroudDrawableId = null;
		private int _textSize = -1;
		Holder holder;

		public LocalAdapter(String[] items, int textSize,
				int[] itemsBackGroudGrawableId) {
			_items = items;
			_textSize = textSize;
			_itemsBackGroudDrawableId = itemsBackGroudGrawableId;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub

			int itemLength = 0;
			int backGroundDrawableLegth = 0;
			if (_items != null) {
				itemLength = _items.length;
			}

			if (_itemsBackGroudDrawableId != null) {
				backGroundDrawableLegth = _itemsBackGroudDrawableId.length;
			}
			return Math.max(itemLength, backGroundDrawableLegth);
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return _items[position];
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (null == convertView) {
				holder = new Holder();
				convertView = View.inflate(context,
						R.layout.alert_dialog_popup_window_items_layout, null);
				holder.view = convertView
						.findViewById(R.id.alertDialogPopupWindowItemsLayout_itemLeftViewId);
				holder.imageView = (ImageView) convertView
						.findViewById(R.id.alertDialogPopupWindowItemsLayout_IViewId);
				holder.textView = (TextView) convertView
						.findViewById(R.id.alertDialogPopupWindowItemsLayout_TViewId);
				convertView.setTag(holder);

			} else {
				holder = (Holder) convertView.getTag();
			}

			if (_itemsBackGroudDrawableId != null) {
				if (_itemsBackGroudDrawableId.length > position) {

					if (itemHeight > 0) {
						LayoutParams lp = (LayoutParams) holder.view
								.getLayoutParams();
						lp.height = itemHeight;
						holder.view.setLayoutParams(lp);
					}

					if (IconLeftPadding > 0) {
						LayoutParams lp = (LayoutParams) holder.view
								.getLayoutParams();
						lp.width = IconLeftPadding;
						holder.view.setLayoutParams(lp);
					}

					if (IconWidth > 0) {
						LayoutParams lp = (LayoutParams) holder.imageView
								.getLayoutParams();
						lp.width = IconWidth;
						holder.imageView.setLayoutParams(lp);
					}

					holder.imageView.setVisibility(View.VISIBLE);
					holder.imageView
							.setBackgroundResource(_itemsBackGroudDrawableId[position]);
				} else {
					holder.imageView.setVisibility(View.GONE);
				}
			} else {
				holder.imageView.setVisibility(View.GONE);
			}

			if (_items != null) {
				if (_items.length > position) {
					holder.textView.setVisibility(View.VISIBLE);
					holder.textView.setTextSize(_textSize);
					holder.textView.setText(_items[position]);
				} else {
					holder.textView.setVisibility(View.GONE);
				}
			} else {
				holder.textView.setVisibility(View.GONE);
			}

			return convertView;
		}

		class Holder {
			View view = null;
			ImageView imageView = null;
			TextView textView = null;
		}

	}
}
