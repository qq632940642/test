package zx.androidUtil;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Hashtable;


public abstract class BaseCache2<T> {
	/** ����Chche���ݵĴ洢 */
	private Hashtable<String, MySoftRef<T>> hashRefs;
	/** ����Reference�Ķ��У������õĶ����Ѿ������գ��򽫸����ô�������У� */
	private ReferenceQueue<T> q;

	/**
	 * �̳�SoftReference��ʹ��ÿһ��ʵ�������п�ʶ��ı�ʶ��
	 */
	private class MySoftRef<T> extends SoftReference <T>{
		private String _key;

		private MySoftRef(T obj, ReferenceQueue<? super T> q,
				String key) {
			super(obj, q);
			_key = key;
		}
	}

	public BaseCache2() {
		hashRefs = new Hashtable<String, MySoftRef<T>>();
		q = new ReferenceQueue<T>();
	}

	/**
	 * �������õķ�ʽ��һ�������ʵ���������ò����������
	 */
	private void addCache(T obj, String key) {
		cleanCache();// �����������
		MySoftRef<T> ref = new MySoftRef<T>(obj, q, key);
		hashRefs.put(key, ref);
	}

	public T getObj(String key) {
		T obj = null;
		// �������Ƿ��и�Bitmapʵ���������ã�����У�����������ȡ�á�
		if (hashRefs.containsKey(key)) {
			MySoftRef<T> ref = (MySoftRef<T>) hashRefs.get(key);
			obj = ref.get();
		}
		// ���û�������ã����ߴ��������еõ���ʵ����null�����¹���һ��ʵ����
		// �����������½�ʵ����������
		if (obj == null) {
			obj = newObj(key);
			this.addCache(obj, key);
		}
		return obj;
	}
	
	//���ɶ���
	protected abstract T newObj(String key);
	
	private void cleanCache() {
		MySoftRef<T> ref = null;
		while ((ref = (MySoftRef<T>) q.poll()) != null) {
			hashRefs.remove(ref._key);
		}
	}

	/**
	 * ���Cache�ڵ�ȫ������
	 */
	public void clearCache() {
		for(String key:hashRefs.keySet()){
			T obj = getObj(key);
			if(obj!=null){
				recycle(obj);
			}
		}
		cleanCache();
		hashRefs.clear();
		System.gc();
		System.runFinalization();
	}
	
	protected abstract void recycle(T obj);
	
}
