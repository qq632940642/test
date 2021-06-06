package zx.view;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParserException;

import com.example.libproject.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.MeasureSpec;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;


/**
 * @category 软键盘
 * @author wzh
 * <p>
 * Keyboard = {keyWidth, keyHeight}
 * </p>
 * <p>
 * Row = {marginLeft, marginRight}
 * </p>
 * <p>
 * Key = {weight }
 * </p>
 * SurfaceView
 */
public class SoftKeyboardView extends View {//需要绑定一个输入框
	
	private static final boolean DEBUG = false;
	private static final String  TAG = "SoftKeyboardView";
	
	public interface KeyFilter{
		boolean isFilter();
		boolean push(char codes);
		boolean pop();
		boolean popAll();
		char[] getFilters2();
	}
	

    
    public SoftKeyboardView(Context context) {
        super(context);
        InitView(context);
        loadKeyboard(context, R.xml.mysymbols);
    }
	
    public SoftKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        InitView(context);
        loadKeyboard(context, R.xml.mysymbols);
    }
	
	public SoftKeyboardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		InitView(context);
        loadKeyboard(context, R.xml.mysymbols);
	}
	
	private void InitView(Context context){
		Resources	resources = context.getResources();
		mTextColor = ColorStateList.valueOf(0xff000000);
	
		setFocusable(true);
		setClickable(true);
		
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.density = resources.getDisplayMetrics().density;
        mTextPaint.setFakeBoldText(false);
        mTextPaint.setTextSkewX(0);
        mTextPaint.setTextSize(25);
        
        HashKey = new HashMap<Integer, Key>(36);
	}
	
	
	public static final int KEY_ENTRY = 10;
	public static final int KEY_SPACE = 32;
	public static final int KEY_DELETE = 27;
	public static final int KEY_SHIFT = 1;
	
    boolean		mAllDraw = true;
    boolean		mBackgroundSizeChanged = true;
    

	Key		mDownKey;
	Key		mInvalidatedKey;
	
	InputConnection		mInputConnection;

    private Bitmap mBuffer;
    private boolean mKeyboardChanged;
    final Rect	mDirtyRect	= new Rect();
	final Rect	mKeyRect 	= new Rect();
	
	
	HashMap<Integer, Key>	HashKey;
	
	KeyFilter		mFilter;
	
    ColorStateList		mTextColor;
    TextPaint			mTextPaint;
    Rect				mTextRect = new Rect();
    
    private	int totalWidth, totalHeight;
    

	
    // Keyboard XML Tags
    private static final String TAG_KEYBOARD = "Keyboard";
    private static final String TAG_ROW = "Row";
    private static final String TAG_KEY = "Key";
	

    /**
     * @category 默认按键的宽
     */
   int	mKeyWidth, mKeyHeight;
    /**
     * @category 默认按键的水平垂直间
     */
   int mHorizontalGap, mVerticalGap;
	/**
	 * @category 默认背景
	 */
   String	mBackground;
	/**
	 * @category 重复按键
	 */
   boolean	isRepeatable = false;

	private Row[] 	mRows;
	private Key[] 	mKeys;
	private Canvas 	mCanvas;
    
   

    
    final static int FLAG_PRESS			= 0X00000001;		//按下
    final static int FLAG_REPEAT		= 0X00000002;		//重复
    final static int FLAG_INVALID		= 0X01000000;		//无效
    final static int FLAG_REDRAW		= 0X00000004;		//重绘
    
    
    public void setFilter(KeyFilter filter){
    	mFilter = filter;
    	if(filter != null && filter.isFilter()){
    		isRepeatable = false;
    		setKeys(mFilter.getFilters2(), true);
    	}else{
    		isRepeatable = true;
    		enableAll(true, true);
    	}
    }
    
	/**
	 * @category 大写锁定
	 */
	boolean		capsLock = false;
    
	private void loadKeyboard(Context context, int res_id){//XmlResourceParser parser) {
		Key key = null;
		Row currentRow = null;
		int event = 0;
		String tag = null;
		int	start = 0, end = 0;
		ArrayList<Row> mRowList = new ArrayList<Row>();
		ArrayList<Key> mKeyList = new ArrayList<Key>();
		final XmlResourceParser parser = context.getResources().getXml(res_id);
		try {
			while ((event = parser.next()) != XmlResourceParser.END_DOCUMENT) {
				if (event == XmlResourceParser.START_TAG) {
					tag = parser.getName();
					if (TAG_KEY.equals(tag)) {// key
						key = new Key(context, parser);
						mKeyList.add(key);

						int codes = key.codes;
						HashKey.put(codes, key);
						if ('A' <= codes && codes <= 'Z') {
							HashKey.put(codes - ('A' - 'a'), key);
						} else if ('a' <= codes && codes <= 'z') {
							HashKey.put(codes + ('A' - 'a'), key);
						}
						
						
					} else if (TAG_ROW.equals(tag)) {// line
						currentRow = new Row(context, parser);
						mRowList.add(currentRow);
						
						
					} else if (TAG_KEYBOARD.equals(tag)) {// all
						parseKeyboardAttributes(context, parser);
						
						
					}
				}else if(event == XmlResourceParser.END_TAG){
					if(TAG_ROW.equals(parser.getName()) && currentRow != null){
						end = mKeyList.size();
						final Key[] keys = new Key[end - start];
						mKeyList.subList(start, end).toArray(keys);
						currentRow.keys = keys;
						currentRow = null;
						start = end;
					}
				}
			}
			
			if(mRowList.size() > 0){
				mRows = new Row[mRowList.size()];
				mRowList.toArray(mRows);
			}
			
			if(mKeyList.size() > 0){
				mKeys = new Key[mKeyList.size()];
				mKeyList.toArray(mKeys);
			}
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			mRowList.clear();
			mKeyList.clear();
			
			mRowList = null;
			mKeyList = null;
		}
	}

    private void parseKeyboardAttributes(Context context, XmlResourceParser parser) {
   	
    	mKeyWidth = parser.getAttributeIntValue(null, "keyWidth", 0);
    	mKeyHeight = parser.getAttributeIntValue(null, "keyHeight", 0);
    	mHorizontalGap = parser.getAttributeIntValue(null, "horizontalGap", 10);
    	mVerticalGap = parser.getAttributeIntValue(null, "verticalGap", 10);
    	mBackground = parser.getAttributeValue(null, "background");
    	if(mBackground == null){
    		throw(new RuntimeException("指定默认背景"));
    	}
    	isRepeatable = parser.getAttributeBooleanValue(null, "repeatable", false);
    	
    }

    


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int height = MeasureSpec.getSize(heightMeasureSpec);
		boolean		bMeasure = false;
		if(width != totalWidth || totalHeight != height){
	    	totalWidth = width; 
	        totalHeight = height;
	        bMeasure = true;
		}
		if(bMeasure && mRows != null){
			int left = getPaddingLeft();
			int top = getPaddingTop();
        	int right = width - getPaddingRight();
  		
        	for(Row row : mRows){
        		row.layout(left, top, right, top+mKeyHeight);
        		top += mKeyHeight;
        		top += mVerticalGap;
        	}
    	}    	
		
	}
	


	@Override
	protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBuffer == null || mKeyboardChanged) {
            onBufferDraw();
        }
        canvas.drawBitmap(mBuffer, 0, 0, null);
	}

	private void onBufferDraw(){
        if (mBuffer == null || mKeyboardChanged) {
            if (mBuffer == null || mKeyboardChanged && (mBuffer.getWidth() != getWidth() || mBuffer.getHeight() != getHeight())) {
                // Make sure our bitmap is at least 1x1
                final int width = Math.max(1, getWidth());
                final int height = Math.max(1, getHeight());
                mBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                mCanvas = new Canvas(mBuffer);
            }
            invalidateAllKeys();
            mKeyboardChanged = false;
        }

        final Canvas canvas = mCanvas;
        final Rect	clipRect = mKeyRect;
        
         canvas.clipRect(mDirtyRect, Op.REPLACE);
       //是否有按键
         if(mKeys == null) return ;
         
        final Key invalidKey = mInvalidatedKey;
        boolean drawSingleKey = false;
        
        if (invalidKey != null && canvas.getClipBounds(clipRect)) {
        	//查看是否绘制的单一按键
          if (invalidKey.mLeft <= clipRect.left &&
              invalidKey.mTop <= clipRect.top &&
              invalidKey.mRight >= clipRect.right &&
              invalidKey.mBottom >= clipRect.bottom) {
              drawSingleKey = true;
          }
        }
        canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
        final int keyCount = mKeys.length;
        for (int i = 0; i < keyCount; i++) {
        	final Key key = mKeys[i];
        	if(drawSingleKey && invalidKey != key){
        		continue;
        	}
        	key.getInvalid(clipRect);
        	if(Rect.intersects(mDirtyRect, clipRect)){
        		key.draw(canvas);
        	}
        }
        
//        saevBitmap(mBuffer);
        mKeyRect.set(mDirtyRect);
        mDirtyRect.setEmpty();
        mInvalidatedKey = null;
	}
    
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int x = (int) event.getX();
		final int y = (int) event.getY();
		final int action = event.getAction();
		final Rect	rect = mKeyRect;
		final Key	key = mDownKey;
		
		switch(action){
		case MotionEvent.ACTION_DOWN:
			mDownKey = null;
			for(Key k: mKeys){
				k.getHitRect(rect);
				if(rect.contains(x, y)){
					if(0 != (k.mFlags & FLAG_INVALID))break;
					mHandler.sendEmptyMessageDelayed(REPEATABLE, LONGPRESS_TIMEOUT);
					k.setFlags(k.mFlags | FLAG_PRESS);
					invalidateKey(k);
					mDownKey = k;
					break;
				}
			}break;
		case MotionEvent.ACTION_MOVE:
			if(key != null){
				key.getHitRect(rect);
				if(!rect.contains(x, y)){
					mHandler.removeMessages(REPEATABLE);
					key.setFlags(key.mFlags & ~FLAG_PRESS);
					invalidateKey(key);
					mDownKey = null;
				}
			}break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if(key != null){
				mHandler.removeMessages(REPEATABLE);
				key.getHitRect(rect);
				key.setFlags(key.mFlags & ~FLAG_PRESS);
				invalidateKey(key);

				if(action == MotionEvent.ACTION_UP && rect.contains(x, y)){
					sendKeyChar((char)key.codes);
				}
				mDownKey = null;
			}break;
		}
		return true;
	}
	
	public void invalidateAllKeys(){
		mDirtyRect.union(0, 0, getWidth(), getHeight());
		mKeyboardChanged = true;
		invalidate();
	}
	
	public void invalidateKey(Key key){
        final Rect rect = mKeyRect;
		mInvalidatedKey = key;
        key.getInvalid(rect);
        mDirtyRect.union(rect);
        onBufferDraw();
        if(DEBUG)Log.e(TAG, "invalidate >> " + mKeyRect);
        invalidate(mKeyRect);
	}
	
	public void capsLockKey(boolean lock){
		final Rect rect = mKeyRect;
		if(lock == capsLock) return ;
		
		capsLock = lock;
		if(capsLock){//大写锁定
			for(Key key : mKeys){
				final int code = key.codes;
				if('a' <= code && code <= 'z'){
					key.codes = code + ('A' - 'a');
					key.label = String.valueOf((char)key.codes);
			        key.getInvalid(rect);
			        mDirtyRect.union(rect);
				}
			}
		}else{//小写锁定
			for(Key key : mKeys){
				final int code = key.codes;
	        	if('A' <= code && code <= 'Z'){
	        		key.codes = code - ('A' - 'a');
					key.label = String.valueOf((char)key.codes);
			        key.getInvalid(rect);
			        mDirtyRect.union(rect);
	        	}
			}
		}

        onBufferDraw();
        if(DEBUG)Log.e(TAG, "invalidate >> " + mKeyRect);
        invalidate(mKeyRect);
	}
	
	public final void enableAll(boolean enable, boolean update){
		if(mKeys == null) return ;
		int count = 0;
		for(Key key : mKeys){
			final int code = key.codes;
			if(code == KEY_ENTRY || code == KEY_DELETE || code == KEY_SHIFT){
				continue;
			}
			count ++;
			if(enable){
				key.setFlags(key.mFlags & ~FLAG_INVALID);
			}else{
				key.setFlags(key.mFlags | FLAG_INVALID);
			}
		}
		
		if(update && count > 0){
			invalidateAllKeys();
		}
	}
	
   
	public final void enableKeys(char[] codes, boolean enable, boolean update){
		if(codes == null) return;
		for(int code : codes){
			Key key = HashKey.get(code);
			if(enable){
				key.setFlags(key.mFlags & ~FLAG_INVALID);
			}else{
				key.setFlags(key.mFlags | FLAG_INVALID);
			}
		}
		
		if(update){
			invalidateAllKeys();
		}
	}
	
	protected final void setKeys(char[] codes, boolean enable){
		enableAll(!enable, false);
		enableKeys(codes, enable, false);
		
		invalidateAllKeys();
	}
	
	

	protected void sendDownUpKeyEvents(int keyEventCode) {
        final InputConnection ic = mInputConnection;
        if (ic == null) return;
//        long eventTime = SystemClock.uptimeMillis();
//        ic.sendKeyEvent(new KeyEvent(eventTime, eventTime,
//                KeyEvent.ACTION_DOWN, keyEventCode, 0, 0, 0, 0,
//                KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE));
//        ic.sendKeyEvent(new KeyEvent(SystemClock.uptimeMillis(), eventTime,
//                KeyEvent.ACTION_UP, keyEventCode, 0, 0, 0, 0,
//                KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE));
//        
        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }
	
	private View	targetView;
	 EditorInfo tba = new EditorInfo();
	 
	public void bindView(View v){
		targetView = v;
		if(targetView == null) return ;
		
        tba.packageName = v.getContext().getPackageName();
        tba.fieldId = v.getId();
        mInputConnection = v.onCreateInputConnection(tba);
	}
	
	public void unbindView(){
		targetView = null;
	}
	
    protected void sendKeyChar(char charCode) {
    	final InputConnection ic = mInputConnection;
		switch (charCode) {
		case '\n':// 回车   
//			sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
			if(ic != null) ic.performEditorAction(tba.imeOptions&EditorInfo.IME_MASK_ACTION);
			break;
        case 1:{//上档
        	mHandler.sendEmptyMessage(CAPS_LOCK);
        }break;
        case 27:{//删除
        	if(ic != null){
        		ic.deleteSurroundingText(1, 0);
        	}
//        	sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
			if(mFilter != null && mFilter.pop()){
				setKeys(mFilter.getFilters2(), true);
			}
        }break;
        case ' ':{//空格(32)
            if (ic != null) {
                ic.commitText(String.valueOf((char) charCode), 1);
            }
        }break;
        default:
			if (mFilter != null && mFilter.push((char) charCode)) {
				setKeys(mFilter.getFilters2(), true);
			}
			if (ic != null) {
				ic.commitText(String.valueOf((char) charCode), 1);
			}
            break;
        }
    }
    
	public void saevBitmap(Bitmap bitmap){
		File f = new File("/mnt/sdcard/", String.valueOf(System.currentTimeMillis()) + ".bmp");
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(f);
			bitmap.compress(CompressFormat.PNG, 0, fout);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(fout != null)
				try {
					fout.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
	}
	   
	
	private static final int LONGPRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();
	public	int		REPEAT_KEY_TIMEOUT = 100;
	
	final static int REPEATABLE = 1;	//Repeatable
	final static int CAPS_LOCK = 2;		//Caps Lock  

	Handler mHandler = new Handler(Looper.getMainLooper()){

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case REPEATABLE:{
				final Key	key = mDownKey;
				if(key != null && 0 != (key.mFlags & FLAG_REPEAT)){
					sendKeyChar((char)key.codes);
					sendEmptyMessageDelayed(REPEATABLE, REPEAT_KEY_TIMEOUT);
				}
			}break;
			case CAPS_LOCK:
				capsLockKey(!capsLock);
				break;
			}
		}
	};
	
	
    
    
	class Row {

		public Key[]	keys;

		public int marginLeft, marginRight;

		public Row(Context context, XmlResourceParser parser) {
			marginLeft = parser.getAttributeIntValue(null, "marginLeft", 0);
			marginRight = parser.getAttributeIntValue(null, "marginRight", 0);
		}

		public void layout(int l, int t, int r, int b) {
			l += marginLeft;
			r -= marginRight;

			for (Key k : keys) {
				k.layout(l, t, l + k.mWidth, b);
				l += k.mWidth;
				l += mHorizontalGap;
			}
		}
	}
    
    
    
    class Key{
    	public int codes;
    	
    	public int mWidth, mHeight;
    	
        public String label;
        
        public Drawable mDrawable;

        public Drawable mIcon;

    	public int mLeft, mTop, mRight, mBottom;
        
        public int	offLeft, offRight, offTop, offBottom;

        private		Key		associateKey;
        
        private		int	mFlags;
        
        public void setFlags(int flag){
        	mFlags =flag; 
        	if(associateKey != null){
        		associateKey.mFlags = mFlags;
        	}
        }
        
        @Override
		public String toString() {
			return "<" + codes + "," + label + "  <" + mLeft + "," + mTop + " | " + mRight + "," + mBottom + ">  " + 
				"  <" + offLeft + "," + offTop + " | " + offRight + "," + offBottom + ">  " + mFlags + "  >";
		}

        public void layout(int l, int t, int r, int b){
        	mLeft = l; mRight = r; mTop = t; mBottom = b;
        };
        
        public int[] getCurrentDrawableState(){
    		if(0 != (mFlags & FLAG_INVALID)){
    			return EMPTY_STATE_SET;	
    		}else if(0 != (mFlags & FLAG_PRESS)){
    			return PRESSED_ENABLED_FOCUSED_STATE_SET;
    		}else{
    			return ENABLED_FOCUSED_STATE_SET;
    		}
        }

        public void draw(Canvas canvas){
        	if(DEBUG)Log.e(TAG, toString());
           	final int[] drawableState = getCurrentDrawableState();
        	canvas.save();
        	
        	if(mDrawable != null){
        		mDrawable.setState(drawableState);	
            	mDrawable.setBounds(mLeft+offLeft, mTop+offTop, mRight+offRight, mBottom+offBottom);
            	mDrawable.draw(canvas);
        	}
        	
        	if(mIcon != null){
        		int w = mIcon.getIntrinsicWidth();
        		int h = mIcon.getIntrinsicHeight();
        		int x = (mLeft + mRight - w) >> 1;
        		int y = (mTop + mBottom - h) >> 1;
        		
            	mIcon.setBounds(x, y, x+w, y+h);
            	mIcon.draw(canvas);
        	}else if(label != null){
        		final Rect rect = mTextRect;
            	mTextPaint.setColor(mTextColor.getColorForState(drawableState, 0xff808080));
            	mTextPaint.getTextBounds(label, 0, label.length(), rect);
            	int x = (mLeft + mRight - rect.width()) / 2 - rect.left;
            	int y = (mTop + mBottom - rect.height()) / 2 - rect.top;
            	
            	canvas.drawText(label, x, y, mTextPaint);
        	}
        	canvas.restore();
        }
        
        public void getHitRect(Rect outRect) {
            outRect.set(mLeft, mTop, mRight, mBottom);
        }
        
		public void getInvalid(Rect outRect){
			final Key key = associateKey;
			if(key != null){
				outRect.set(key.mLeft+key.offLeft, key.mTop+key.offTop, key.mRight+key.offRight, key.mBottom+key.offBottom);
				outRect.union(mLeft+offLeft, mTop+offTop, mRight+offRight, mBottom+offBottom);
			}else{
				outRect.set(mLeft+offLeft, mTop+offTop, mRight+offRight, mBottom+offBottom);
			}
		}
		
       public Key(Context context, XmlResourceParser parser) {
    	   final Resources res = context.getResources();
           final String	packName = context.getPackageName();
            String	drawable = parser.getAttributeValue(null, "background");
            if(drawable == null) drawable = mBackground;
            if(mDrawable!=null){
            	mDrawable.setCallback(null);
            	System.gc();
            }
            if(mIcon!=null){
            	mIcon.setCallback(null);
            	System.gc();
            }
            if(drawable.equals("null")){
            	mDrawable = null;
            }else{
            	mDrawable = res.getDrawable(res.getIdentifier(drawable, "drawable", packName));
            }

            drawable = parser.getAttributeValue(null, "keyIcon");
            if(drawable != null){
        		if(null != (mIcon = res.getDrawable(res.getIdentifier(drawable, "drawable", packName))))
        		mIcon.setBounds(0, 0, mIcon.getIntrinsicWidth(), mIcon.getIntrinsicHeight());
        	}
            
            codes = parser.getAttributeIntValue(null, "codes", 0);
            label = parser.getAttributeValue(null, "keyLabel");
            associateKey = HashKey.get(codes);
            if(associateKey != null){
            	associateKey.associateKey = this;
            }
            
            if(parser.getAttributeBooleanValue(null, "repeatable", isRepeatable)){
            	setFlags( mFlags | FLAG_REPEAT);
            }else{
            	setFlags( mFlags & ~FLAG_REPEAT);
            }
            
            float weight			=parser.getAttributeFloatValue(null, "weight", 1.0f);
            mWidth			= (int) (mKeyWidth * weight);
            mHeight			= mKeyHeight;
            
            float offset = parser.getAttributeFloatValue(null, "offLeft", 0.0f);
            offLeft = (int) (offset * mKeyWidth);
            
            offset = parser.getAttributeFloatValue(null, "offRight", 0.0f);
            offRight = (int) (offset * mKeyWidth);
            
            offset = parser.getAttributeFloatValue(null, "offTop", 0.0f);
            offTop = (int) (offset * mKeyHeight);
            
            offset=parser.getAttributeFloatValue(null, "offBottom", 0.0f);
            offBottom = (int) (offset * mKeyHeight);
        }
    }
}

