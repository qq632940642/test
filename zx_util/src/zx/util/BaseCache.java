package zx.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Hashtable;


public abstract class BaseCache {
	/** 用于Chche内容的存储 */
	private Hashtable<String, MySoftRef> hashRefs;
	/** 垃圾Reference的队列（所引用的对象已经被回收，则将该引用存入队列中） */
	private ReferenceQueue<?> q;

	/**
	 * 继承SoftReference，使得每一个实例都具有可识别的标识。
	 */
	private class MySoftRef extends SoftReference {
		private String _key;

		private MySoftRef(Object obj, ReferenceQueue<?> q,
				String key) {
			super(obj, q);
			_key = key;
		}
	}

	public BaseCache() {
		hashRefs = new Hashtable<String, MySoftRef>();
		q = new ReferenceQueue();
	}

	/**
	 * 以软引用的方式对一个对象的实例进行引用并保存该引用
	 */
	private void addCache(Object obj, String key) {
		cleanCache();// 清除垃圾引用
		MySoftRef ref = new MySoftRef(obj, q, key);
		hashRefs.put(key, ref);
	}

	public Object getObj(String key) {
		Object obj = null;
		// 缓存中是否有该Bitmap实例的软引用，如果有，从软引用中取得。
		if (hashRefs.containsKey(key)) {
			MySoftRef ref = (MySoftRef) hashRefs.get(key);
			obj = ref.get();
		}
		// 如果没有软引用，或者从软引用中得到的实例是null，重新构建一个实例，
		// 并保存对这个新建实例的软引用
		if (obj == null) {
			obj = newObj(key);
			this.addCache(obj, key);
		}
		return obj;
	}

	//生成对象
	protected abstract Object newObj(String key);
	
	private void cleanCache() {
		MySoftRef ref = null;
		while ((ref = (MySoftRef) q.poll()) != null) {
			hashRefs.remove(ref._key);
		}
	}

	/**
	 * 清除Cache内的全部内容
	 */
	public void clearCache() {
		cleanCache();
		hashRefs.clear();
		System.gc();
		System.runFinalization();
	}

}
