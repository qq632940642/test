package zx.xml;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 思想上仿照json序列化 简单对象的序列化。如果对象里含有引用类型的变量，且变量间存在着关联则可能得到不是想要的结果！ 也可以加入注解，注释。
 * 非public修饰的属性须有get方法才能序列化.否则将被忽略 . 注解了ignore且注解值为true，也忽略 内部类不能反序列化! 必须有无参构造器!
 * 父类有同名属性的被舍弃. 这里把对象分为基本类型、Collection、Map、Enum、数组，以及普通对象类型.
 * @author zx
 * */
public class SerializeUtil {

	private SerializeUtil() {
	}

	/**
	 * 基类
	 * */
	public static final Set<Class<?>> BASE_CLASSES = new HashSet<Class<?>>();

	static {
		BASE_CLASSES.add(Character.class);
		BASE_CLASSES.add(char.class);
		BASE_CLASSES.add(Byte.class);
		BASE_CLASSES.add(byte.class);
		BASE_CLASSES.add(Short.class);
		BASE_CLASSES.add(short.class);
		BASE_CLASSES.add(Integer.class);
		BASE_CLASSES.add(int.class);
		BASE_CLASSES.add(Long.class);
		BASE_CLASSES.add(long.class);
		BASE_CLASSES.add(Double.class);
		BASE_CLASSES.add(double.class);
		BASE_CLASSES.add(Float.class);
		BASE_CLASSES.add(float.class);
		BASE_CLASSES.add(Boolean.class);
		BASE_CLASSES.add(boolean.class);

		BASE_CLASSES.add(String.class);
		BASE_CLASSES.add(Class.class);
		BASE_CLASSES.add(Enum.class);// 枚举

	}

	/**
	 * 获取某属性的get方法
	 * */
	static Method obtainGetMethod(Class<?> clazz, String fieldName) {
		String methodName;
		Field field = null;
		try {
			field = clazz.getDeclaredField(fieldName);
		} catch (Exception e) {
			return null;
		}
		if (field == null)
			return null;
		Class<?> fieldType = field.getType();
		if (fieldType == boolean.class || fieldType == Boolean.class) {
			if (fieldName.length() > 2
					&& fieldName.startsWith("is")
					&& (fieldName.charAt(2) >= 'A' && fieldName.charAt(2) <= 'Z')) {
				methodName = fieldName;
			} else {
				methodName = "is" + (fieldName.charAt(0) + "").toUpperCase();
				if (fieldName.length() > 1) {
					methodName += fieldName.substring(1);
				}
			}
		} else {
			methodName = "get" + (fieldName.charAt(0) + "").toUpperCase();
			if (fieldName.length() > 1) {
				methodName += fieldName.substring(1);
			}
		}
		try {
			Method method = clazz.getMethod(methodName);
			return method;
		} catch (Exception e1) {
			return null;
		}
	}

	/**
	 * 获取某属性的set方法
	 * */
	static Method obtainSetMethod(Class<?> clazz, String fieldName) {
		String methodName;
		Field field = null;
		try {
			field = clazz.getDeclaredField(fieldName);
		} catch (Exception e1) {
			return null;
		}
		if (field == null)
			return null;
		Class<?> fieldType = field.getType();
		if (fieldType == boolean.class || fieldType == Boolean.class) {
			if (fieldName.length() > 2 && fieldName.startsWith("is")
					&& fieldName.charAt(2) >= 'A' && fieldName.charAt(2) <= 'Z') {
				methodName = "set" + fieldName.substring(2);
			} else {
				methodName = "set" + (fieldName.charAt(0) + "").toUpperCase();
				if (fieldName.length() > 1) {
					methodName += fieldName.substring(1);
				}
			}
		} else {
			methodName = "set" + (fieldName.charAt(0) + "").toUpperCase();
			if (fieldName.length() > 1) {
				methodName += fieldName.substring(1);
			}
		}
		try {
			Method method = clazz.getMethod(methodName);
			return method;
		} catch (Exception e1) {
			return null;
		}
	}

	/** 检查注解，变量序列化和反序列化是否忽略 */
	static boolean ignore(Field field) {
		XMLSerializeIgnore annotation = field
				.getAnnotation(XMLSerializeIgnore.class);
		if (annotation == null) {
			return false;
		}
		return annotation.ignore();
	}

	/**
	 * 获取当前对象的所有可见属性和通过get方法可以得到的属性（包括继承自父类的属性）
	 * 
	 * @param object
	 *            : 对象
	 * @return 属性对象
	 */
	static Map<Field, Object> getAllAcceseableFields_ContainsSuper(Object object) {
		Class<?> clazz = object.getClass();
		Map<Field, Object> fieldMap = new HashMap<Field, Object>();
		class FieldMsg {
			String fieldName;
			Class<?> type;

			@Override
			public boolean equals(Object obj) {
				if (obj == this)
					return true;
				if (obj instanceof FieldMsg) {
					FieldMsg other = (FieldMsg) obj;
					return fieldName.equals(other.fieldName);
					// && type.toString().equals(other.type.toString());
				}
				return false;
			}
		}
		List<FieldMsg> fieldMsgs = new ArrayList<FieldMsg>();
		for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
			try {
				Field[] fields = clazz.getDeclaredFields();
				for (Field field : fields) {
					try {
						if (Modifier.isStatic(field.getModifiers())) {
							continue;
						}
						if (ignore(field)) {
							continue;
						}
						Object value = null;
						if (Modifier.isPublic(field.getModifiers())) {
							value = field.get(object);
						} else {// 用get方法获取值
							Method getMethod = obtainGetMethod(clazz,
									field.getName());
							if (getMethod == null) {
								continue;
							} else {
								value = getMethod.invoke(object);
							}
						}
						FieldMsg msg = new FieldMsg();
						msg.fieldName = field.getName();
						msg.type = field.getType();
						if (!fieldMsgs.contains(msg)) {
							fieldMsgs.add(msg);
							fieldMap.put(field, value);
						}
					} catch (Exception e) {
					}
				}
			} catch (Exception e) {
				// 这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
				// 如果这里的异常打印或者往外抛，则就不会执行clazz =
				// clazz.getSuperclass(),最后就不会进入到父类中了
			}
		}
		return fieldMap;
	}

	/**
	 * 将map中的属性赋值到对象中(getAllAcceseableFields_ContainsSuper方法的逆方法)
	 * 
	 * @param fieldValueMap
	 *            数据来源
	 * @param clazz
	 *            对象类型
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InstantiationException
	 * */
	static Object toObject(Map<Field, Object> fieldValueMap, Class<?> clazz)
			throws IllegalArgumentException, IllegalAccessException,
			InstantiationException {
		Object obj = clazz.newInstance();
		Set<Field> set = fieldValueMap.keySet();
		for (Field field : set) {
			field.setAccessible(true);
			Object value = fieldValueMap.get(field);
			field.set(obj, value);
		}
		return obj;
	}

	/**
	 * 判断是子类或者相等
	 * */
	private static boolean isRelation(Class<?> subClass, Class<?> superClass) {
		try {
			subClass.asSubclass(superClass);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 属性是一个带有泛型的变量，获取其中泛型
	 * */
	private static Class<?> getGenericType(Field field) {
		ParameterizedType pt = (ParameterizedType) field.getGenericType();
		try {
			return Class.forName(pt.getActualTypeArguments()[0].toString()
					.substring("class".length() + 1));
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	private static Element convertBase_ClassObjectToRootElement(Object object) {
		Element rootElement = new Element();
		rootElement.setName(object.getClass().getName().toString());
		rootElement.addSonNode(new TextNode(object.toString()));
		return rootElement;
	}

	/**map里面的entry在xml文档中的标记*/
	public static String entryInMap = "Entry";

	private static Element convertMapObjectToRootElement(Map<?, ?> object) {
		Element rootElement = new Element();
		rootElement.setName(object.getClass().getName().toString());
		Set<?> set = object.keySet();
		for (Object key : set) {
			Object value = object.get(key);
			Element entry = new Element();
			rootElement.addSonNode(entry);
			entry.setName(entryInMap);
			Element keyElement = convertObjectToNomalElement(key);
			Element valueElement = convertObjectToNomalElement(value);
			entry.addSonNode(keyElement);
			entry.addSonNode(valueElement);
		}
		return rootElement;
	}

	/**
	 * 得到根节点
	 * */
	private static Element convertObjectToRootElement(Object object) {
		if (object == null) {
			return getNullRootElement();
		}
		Class<?> clazz = object.getClass();
		Element rootElement = null;
		// 先判断自身类型
		if (BASE_CLASSES.contains(clazz)) {
			return (rootElement = convertBase_ClassObjectToRootElement(object));
		} else if (isRelation(clazz, Collection.class)) {
			return (rootElement = convertCollectionToRootElement((Collection<?>) object));
		} else if (isRelation(clazz, Map.class)) {
			rootElement = convertMapObjectToRootElement((Map<?, ?>) object);
			return rootElement;
		} else if (clazz.isArray()) {
			return convertArrayObjectToRootElement(object);
		} else {
			try {
				clazz.asSubclass(Enum.class);// 判断是枚举类型
				return (rootElement = convertBase_ClassObjectToRootElement(object));
			} catch (ClassCastException e) {// 其他对象类型
				return convertNomalObjectToRootElement(object);
			}
		}
	}

	/**
	 * 普通对象类型转化为根节点
	 * */
	private static Element convertNomalObjectToRootElement(Object object) {
		if (object == null) {
			return getNullRootElement();
		}
		Element rootElement = new Element();
		rootElement.setName(object.getClass().getName().toString());
		Map<Field, Object> map = getAllAcceseableFields_ContainsSuper(object);
		Set<Field> fields = map.keySet();
		for (Iterator<Field> it = fields.iterator(); it.hasNext();) {
			Field field = it.next();
			Object value = map.get(field);//这里的类型可能有变化，如int转化为Integer
			Element element = convertObjectToNomalElement(field, value);
			rootElement.addSonNode(element);
		}
		return rootElement;
	}

	/**数组标记*/
	public static String arrayMark = "-" + "array";
	/**
	 * 空值标记
	 * */
	public static String nullMark = "SerializeUtil-" + "NULL";

	/**
	 * 数组对象转化为根节点.这里自定义节点名的格式为 类名-array
	 * */
	private static Element convertArrayObjectToRootElement(Object object) {
		Element rootElement = new Element();
		rootElement.setName(object.getClass().getComponentType().getName()
				+ arrayMark);
		List<?> list = Arrays.asList(object);
		for (Object obj : list) {
			Element element = convertObjectToNomalElement(obj);
			rootElement.addSonNode(element);
		}
		return rootElement;
	}

	/**
	 * 对象转换为文档. 头结点的name属性为对象的类名.属性节点的name属性为属性名.
	 * */
	public static Document form(Object obj) {
		Document document = new Document();
		if (obj == null) {
			document.setRootNode(getNullRootElement());
			return document;
		}
		Element root = convertObjectToRootElement(obj);
		document.setRootNode(root);
		return document;
	}

	/**
	 * 对象转为序列化字符串
	 * */
	public static String getSerializeString(Object obj) {
		return form(obj).toString();
	}

	private static Element getNullRootElement() {
		Element element = new Element();
		element.setName(nullMark);
		element.addAttribute(new Attribute(valueMark, nullMark));
		return element;
	}

	/**
	 * 转化为普通节点,(处理内部的对象，比如在list内部的对象) 这里要记录下对象的类型以便反序列化. 注意：对象可能为空
	 * */
	private static Element convertObjectToNomalElement(Object obj) {
		if (obj == null) {
			return getNullRootElement();
		}
		Class<?> clazz = obj.getClass();
		// 判断obj类型
		// 这里的转化是和根节点一样
		if (BASE_CLASSES.contains(clazz)) {
			return convertBase_ClassObjectToRootElement(obj);
		} else if (isRelation(clazz, Collection.class)) {
			return convertCollectionToRootElement((Collection<?>) obj);
		} else if (isRelation(clazz, Map.class)) {
			return convertMapObjectToRootElement((Map<?, ?>) obj);
		} else if (clazz.isArray()) {
			return convertArrayObjectToRootElement(obj);
		} else {
			try {
				clazz.asSubclass(Enum.class);// 判断是枚举类型
				return convertBase_ClassObjectToRootElement(obj);
			} catch (ClassCastException e) {// 其他对象类型
				return convertNomalObjectToRootElement(obj);
			}
		}
	}

	private static Element getNomalNullElement(Field field) {
		Element element = new Element();
		element.setName(field.getName());
		element.addAttribute(new Attribute(valueMark, nullMark));
		return element;
	}

	/**
	 * 转化为普通节点.节点名为属性名.
	 * */
	private static Element convertObjectToNomalElement(Field field, Object obj) {
		if (obj == null) {
			return getNomalNullElement(field);
		}
		// 判断obj类型
		Class<?> clazz = field.getType();
		if (BASE_CLASSES.contains(clazz)) {
			return convertBase_ClassObjectToNomalElement(field, obj);
		} else if (isRelation(clazz, Collection.class)) {
			return convertCollectionToNomalElement(field, obj);
		} else if (isRelation(clazz, Map.class)) {
			return convertMapToNomalElement(field, obj);
		} else if (clazz.isArray()) {
			return convertArrayToNomalElement(field, obj);
		} else if (isRelation(clazz, Enum.class)) {
			return convertBase_ClassObjectToNomalElement(field, obj);
		} else {// 有属性子节点
			Element element = new Element();
			element.setName(field.getName());
			element.addAttribute(new Attribute(classMark, clazz.getName()));
			Map<Field, Object> map = getAllAcceseableFields_ContainsSuper(obj);
			Set<Field> set = map.keySet();
			for (Field f : set) {
				Object value = map.get(f);
				Element ele = convertObjectToNomalElement(f, value);
				element.addSonNode(ele);
			}
			return element;
		}
	}

	private static Element convertBase_ClassObjectToNomalElement(Field field,
			Object obj) {
		if (obj == null) {
			return getNomalNullElement(field);
		}
		Element element = new Element();
		element.setName(field.getName());
		element.addAttribute(new Attribute(classMark, obj.getClass().getName()));
		element.addSonNode(new TextNode(obj.toString()));
		return element;
	}

	/**
	 * 集合转化为根节点
	 * */
	private static Element convertCollectionToRootElement(Collection<?> object) {
		Element rootElement = new Element();
		rootElement.setName(object.getClass().getName().toString());
		for (Iterator<?> it = object.iterator(); it.hasNext();) {
			Object ele = it.next();
			Element element = convertObjectToNomalElement(ele);
			rootElement.addSonNode(element);
		}
		return rootElement;
	}

	private static Element convertCollectionToNomalElement(Field field,
			Object value) {
		if (value == null) {
			return getNomalNullElement(field);
		}
		Element element = new Element();
		element.setName(field.getName());
		element.addAttribute(new Attribute(classMark, value.getClass()
				.getName()));
		Collection<?> collection = (Collection<?>) value;
		for (Iterator<?> it = collection.iterator(); it.hasNext();) {
			Object obj = it.next();// 得到元素
			element.addSonNode(convertObjectToNomalElement(obj));
		}
		return element;
	}

	/**
	 * 类标记
	 * */
	public static String classMark = "Class";

	private static Element convertMapToNomalElement(Field field, Object value) {
		if (value == null) {
			return getNomalNullElement(field);
		}
		Element element = new Element();
		element.setName(field.getName());
		element.addAttribute(new Attribute(classMark, value.getClass()
				.getName()));
		Map<?, ?> map = (Map<?, ?>) value;
		for (Object key : map.keySet()) {
			Object valueObj = map.get(key);
			Element entry = new Element();
			element.addSonNode(entry);
			entry.setName(entryInMap);
			Element keyEle = convertObjectToNomalElement(key);
			Element valueEle = convertObjectToNomalElement(valueObj);
			entry.addSonNode(keyEle);
			entry.addSonNode(valueEle);
		}
		return element;
	}

	private static Element convertArrayToNomalElement(Field field, Object value) {
		if (value == null) {
			return getNomalNullElement(field);
		}
		Element element = new Element();
		element.setName(field.getName());
		element.addAttribute(new Attribute(classMark, value.getClass()
				.getComponentType().getName()
				+ arrayMark));// 记录数组元素类型
		Object[] valueArray = (Object[]) value;
		// List<?> list = Arrays.asList(value);//这里出错！没转化为list
		List<?> list = Arrays.asList(valueArray);
		for (Object obj : list) {
			element.addSonNode(convertObjectToNomalElement(obj));
		}
		return element;
	}

	private static Element convertEnumToNomalElement(Field field, Object value) {
		if (value == null) {
			return getNomalNullElement(field);
		}
		Element element = new Element();
		element.setName(field.getName());
		element.addAttribute(new Attribute(classMark, value.getClass()
				.getName()));
		element.addSonNode(new TextNode(value.toString()));
		return element;
	}

	/**
	 * 自定义序列化异常类
	 * */
	public static class SerializeException extends RuntimeException {
		private static final long serialVersionUID = 6173038655011512997L;

		public SerializeException(String string) {
			super(string);
		}

		public SerializeException() {
			super();
		}

		public SerializeException(Throwable e) {
			super(e);
		}

		@Override
		public String toString() {
			return super.toString();
		}
	}

	/**
	 * 值标记
	 * */
	public static String valueMark = "value";

	// 下面开始反序列化!
	/**
	 * 反序列化：文档转为对象
	 * */
	public static Object parse(Document document)
			throws IllegalArgumentException, IllegalAccessException,
			ClassNotFoundException, SecurityException, NoSuchFieldException,
			InstantiationException, InvocationTargetException,
			NoSuchMethodException {
		return parseRootElement(document.getRootNode());
	}

	/**
	 * 反序列化：字符串转为对象
	 * */
	public static Object parse(String serializeString)
			throws IllegalArgumentException, SecurityException,
			IllegalAccessException, ClassNotFoundException,
			NoSuchFieldException, InstantiationException,
			InvocationTargetException, NoSuchMethodException {
		return parse(DocumentUtil.parseXMLString(serializeString));
	}

	private static void setNullField(Field field, Object obj)
			throws IllegalArgumentException, IllegalAccessException {
		field.set(obj, null);
	}

	private static void setArrayField(Field field, Object obj,
			Element fieldElement, String type)
			throws ArrayIndexOutOfBoundsException, IllegalArgumentException,
			IllegalAccessException, ClassNotFoundException, SecurityException,
			NoSuchFieldException, InstantiationException,
			InvocationTargetException, NoSuchMethodException {
		Class<?> arrayElementClass = null;
		arrayElementClass = Class.forName(type);
		// 创建数组
		int length = fieldElement.getNodeList().size();
		Object array = Array.newInstance(arrayElementClass, length);
		// 给数组赋值,需要遍历数组节点
		int idx = 0;
		for (Node ele : fieldElement.getNodeList()) {
			Element element = (Element) ele;// 数组的元素
			Array.set(array, idx, parseRootElement(element));// 数组元素的解析方法和解析对象的方法相同
			idx++;
		}
		field.set(obj, array);
	}

	/**
	 * 转化节点为对象.
	 * 
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws ClassNotFoundException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * */
	private static Object parseRootElement(Element objElement)
			throws IllegalArgumentException, IllegalAccessException,
			ClassNotFoundException, SecurityException, NoSuchFieldException,
			InstantiationException, InvocationTargetException,
			NoSuchMethodException {
		Object obj = null;
		if (objElement == null) {
			throw new SerializeException();
		}
		String className = objElement.getName();
		String val = objElement.getAttributeValue(valueMark);
		if (val == nullMark) {
			return null;
		}
		if (objElement.getNodeList().size() == 0) {
			return null;
		}
		if (className.indexOf(arrayMark) >= 0) {// 数组
			className = className.substring(0,
					className.length() - arrayMark.length());
			obj = getArrayObj(objElement, className);
			return obj;
		}
		Class<?> clazz = Class.forName(className);
		if (BASE_CLASSES.contains(clazz)) {// 基类
			TextNode tNode = (TextNode) objElement.getNodeList().get(0);
			String textVal = tNode.getText();
			if (clazz == String.class) {
				obj = textVal;
			} else if (clazz == Class.class) {
				obj = Class.forName(textVal.substring("class".length() + 1));
			} else if (clazz == int.class || clazz == Integer.class) {
				obj = Integer.parseInt(textVal);
			} else if (clazz == long.class || clazz == Long.class) {
				obj = Long.parseLong(textVal);
			} else if (clazz == float.class || clazz == Float.class) {
				obj = Float.parseFloat(textVal);
			} else if (clazz == double.class || clazz == Double.class) {
				obj = Double.parseDouble(textVal);
			} else if (clazz == short.class || clazz == Short.class) {
				obj = Short.parseShort(textVal);
			} else if (clazz == byte.class || clazz == Byte.class) {
				obj = Byte.parseByte(textVal);
			} else if (clazz == char.class || clazz == Character.class) {
				if (textVal == null || textVal.length() != 1) {
					throw new SerializeException();
				}
				obj = textVal.charAt(0);
			} else if (clazz == boolean.class || clazz == Boolean.class) {
				obj = Boolean.parseBoolean(textVal);
			}
			return obj;
		}
		if (isRelation(clazz, Enum.class)) {
			TextNode tNode = (TextNode) objElement.getNodeList().get(0);
			String textVal = tNode.getText();
			Field field = clazz.getDeclaredField(textVal);
			obj = field.get(null);
			return obj;
		}
		if (isRelation(clazz, Collection.class)) {
			obj = clazz.newInstance();
			for (Node n : objElement.getNodeList()) {
				Element ele = (Element) n;
				Object object = parseRootElement(ele);
				Method add = null;
				try {
					add = clazz.getMethod("add");
				} catch (NoSuchMethodException e) {
					throw new SerializeException(e);
				}
				try {
					add.invoke(obj, object);
				} catch (InvocationTargetException e) {
					throw new SerializeException(e);
				}
			}
			return obj;
		}
		if (isRelation(clazz, Map.class)) {
			obj = clazz.newInstance();
			for (Node n : objElement.getNodeList()) {
				Element entry = (Element) n;
				Element key = (Element) entry.getNodeList().get(0);
				Element value = (Element) entry.getNodeList().get(1);
				Object keyObj = parseRootElement(key);
				Object valueObj = parseRootElement(value);
				Method put = null;
				try {
					put = clazz.getMethod("put", Object.class, Object.class);
				} catch (NoSuchMethodException e) {
					throw new SerializeException(e);
				}
				try {
					put.invoke(obj, keyObj, valueObj);
				} catch (InvocationTargetException e) {
					throw new SerializeException(e);
				}
			}
			return obj;
		}
		// 普通对象类型：
		return getNomalObj(objElement);
	}

	private static Object getNomalObj(Element objElement)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, SecurityException, NoSuchFieldException,
			ArrayIndexOutOfBoundsException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException {
		Object obj = null;
		String className = objElement.getName();
		Class<?> clazz = Class.forName(className);
		obj = clazz.newInstance();
		List<Node> list = objElement.getNodeList();
		for (Node node : list) {// 遍历子节点，也就是对象的属性
			Element fieldElement = (Element) node;
			String fieldName = fieldElement.getName();
			Field field = null;
			if ((field = getDeclaredField_ContainsSuper(obj, fieldName)) == null) {
				throw new SerializeException("反序列化异常：没有属性 " + fieldName + "!");
			} else if (ignore(field)) {
				throw new SerializeException("反序列化异常：属性 " + fieldName + "被忽略！");
			} else {
				field.setAccessible(true);
			}
			String value = fieldElement.getAttributeValue(valueMark);
			String cls = fieldElement.getAttributeValue(classMark);

			// 判断属性
			if (value != null && value.equals(nullMark)) {// 空值
				setNullField(field, obj);
				continue;
			}
			if (cls.indexOf(arrayMark) > -1) {// 数组
				String type = cls.substring(0,
						cls.length() - arrayMark.length());
				setArrayField(field, obj, fieldElement, type);
				continue;
			}
			Class<?> fieldClass = Class.forName(cls);
			if (BASE_CLASSES.contains(fieldClass)) {// 基本类型
				setBaseClassField(field, obj, fieldElement, fieldClass);
				continue;
			}
			if (isRelation(fieldClass, Enum.class)) {// 枚举
				setEnumField(field, obj, fieldElement, fieldClass);
				continue;
			}
			if (isRelation(fieldClass, Collection.class)) {
				setCollectionField(field, obj, fieldElement, fieldClass);
				continue;
			}
			if (isRelation(fieldClass, Map.class)) {
				setMapField(field, obj, fieldElement, fieldClass);
				continue;
			}
			// 普通对象类型：
			setNomalObjField(field, obj, fieldElement, fieldClass);
		}
		return obj;
	}

	private static void setNomalObjField(Field field, Object obj,
			Element fieldElement, Class<?> fieldClass)
			throws InstantiationException, IllegalAccessException,
			SecurityException, NoSuchFieldException,
			ArrayIndexOutOfBoundsException, IllegalArgumentException,
			ClassNotFoundException, InvocationTargetException,
			NoSuchMethodException {
		Object fieldObj = fieldClass.newInstance();
		List<Node> list = fieldElement.getNodeList();
		Class<?> clazz = fieldClass;
		// 设置属性：
		for (Node node : list) {
			Element fieldEle = (Element) node;
			String fieldName = fieldEle.getName();
			Field mfield = null;
			if ((mfield = clazz.getDeclaredField(fieldName)) == null) {
				throw new SerializeException("反序列化异常：没有属性 " + fieldName + "!");
			} else if (ignore(mfield)) {
				throw new SerializeException("反序列化异常：属性 " + fieldName + "被忽略！");
			} else {
				mfield.setAccessible(true);
			}
			String value = fieldEle.getAttributeValue(valueMark);
			String cls = fieldEle.getAttributeValue(classMark);
			// 判断属性
			if (value != null && value.equals(nullMark)) {// 空值
				setNullField(mfield, fieldObj);
				continue;
			}
			if (cls.indexOf(arrayMark) > -1) {// 数组
				String type = cls.substring(0,
						cls.length() - arrayMark.length());
				setArrayField(mfield, fieldObj, fieldEle, type);
				continue;
			}
			Class<?> fieldCla = Class.forName(cls);
			if (BASE_CLASSES.contains(fieldCla)) {// 基本类型
				setBaseClassField(mfield, fieldObj, fieldEle, fieldCla);
				continue;
			}
			if (isRelation(fieldCla, Enum.class)) {// 枚举
				setEnumField(mfield, fieldObj, fieldEle, fieldCla);
				continue;
			}
			if (isRelation(fieldCla, Collection.class)) {
				setCollectionField(mfield, fieldObj, fieldEle, fieldCla);
				continue;
			}
			if (isRelation(fieldCla, Map.class)) {
				setMapField(mfield, fieldObj, fieldEle, fieldCla);
				continue;
			}
			// 普通对象类型：
			setNomalObjField(mfield, fieldObj, fieldEle, fieldCla);
		}
		field.set(obj, fieldObj);
	}

	private static void setMapField(Field field, Object obj,
			Element fieldElement, Class<?> fieldClass)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, SecurityException,
			ClassNotFoundException, NoSuchFieldException,
			InvocationTargetException, NoSuchMethodException {
		Class<? extends Map> cla = fieldClass.asSubclass(Map.class);
		Map<?, ?> map = cla.newInstance();
		for (Node n : fieldElement.getNodeList()) {
			Element entry = (Element) n;
			Element keyEle = (Element) entry.getNodeList().get(0);
			Element valEle = (Element) entry.getNodeList().get(1);
			Object key = parseRootElement(keyEle);
			Object val = parseRootElement(valEle);
			map.getClass().getMethod("put", Object.class, Object.class)
					.invoke(map, key, val);
		}
		field.set(obj, map);
	}

	private static void setCollectionField(Field field, Object obj,
			Element fieldElement, Class<?> fieldClass)
			throws IllegalArgumentException, IllegalAccessException,
			InstantiationException, SecurityException,
			InvocationTargetException, NoSuchMethodException,
			ClassNotFoundException, NoSuchFieldException {
		Class<? extends Collection> cla = fieldClass
				.asSubclass(Collection.class);
		Collection<?> collection = cla.newInstance();
		for (Node n : fieldElement.getNodeList()) {
			Element ele = (Element) n;
			// collection.add(parseRootElement(ele));//报错，所以采用泛型
			collection.getClass().getMethod("add", Object.class)
					.invoke(collection, parseRootElement(ele));
		}
		field.set(obj, collection);
	}

	/**
	 * 设置为数组对象.
	 * 
	 * @param objElement
	 *            数组对象节点
	 * @param className
	 *            数组中元素类型字符串
	 * @return
	 * @throws ClassNotFoundException
	 * @throws NegativeArraySizeException
	 * @throws InstantiationException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * */
	private static Object getArrayObj(Element objElement, String className)
			throws NegativeArraySizeException, ClassNotFoundException,
			IllegalArgumentException, SecurityException,
			IllegalAccessException, NoSuchFieldException,
			InstantiationException, InvocationTargetException,
			NoSuchMethodException {
		Object obj = null;
		int length = objElement.getNodeList().size();
		obj = Array.newInstance(Class.forName(className), length);
		for (int i = 0; i < length; i++) {
			Node valNode = objElement.getNodeList().get(i);
			Object object = parseRootElement((Element) valNode);
			Array.set(obj, i, object);
		}
		return obj;
	}

	private static void setBaseClassField(Field field, Object obj,
			Element fieldElement, Class<?> fieldClass)
			throws IllegalArgumentException, IllegalAccessException,
			ClassNotFoundException {
		if (fieldClass == String.class
				&& fieldElement.getNodeList().size() == 0) {
			field.set(obj, "");
			return;
		}
		String val = ((TextNode) fieldElement.getNodeList().get(0)).getText();
		if (fieldClass == String.class) {
			field.set(obj, val);
		} else if (fieldClass == int.class || fieldClass == Integer.class) {
			field.set(obj, Integer.parseInt(val));
		} else if (fieldClass == short.class || fieldClass == Short.class) {
			field.set(obj, Short.parseShort(val));
		} else if (fieldClass == long.class || fieldClass == Long.class) {
			field.set(obj, Long.parseLong(val));
		} else if (fieldClass == double.class || fieldClass == Double.class) {
			field.set(obj, Double.parseDouble(val));
		} else if (fieldClass == Float.class || fieldClass == float.class) {
			field.set(obj, Float.parseFloat(val));
		} else if (fieldClass == boolean.class || fieldClass == Boolean.class) {
			field.set(obj, Boolean.parseBoolean(val));
		} else if (fieldClass == char.class || fieldClass == Character.class) {
			if (val == null || val.length() != 1) {
				throw new SerializeException();
			}
			field.set(obj, val.charAt(0));
		} else if (fieldClass == byte.class || fieldClass == Byte.class) {
			field.set(obj, Byte.parseByte(val));
		} else if (fieldClass == Class.class) {
			field.set(obj, Class.forName(val.substring("class".length() + 1)));
		}
	}

	private static void setEnumField(Field field, Object obj,
			Element fieldElement, Class<?> fieldClass)
			throws SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {
		String val = ((TextNode) fieldElement.getNodeList().get(0)).getText();
		Field f = fieldClass.getDeclaredField(val);
		if (ignore(f)) {
			throw new SerializeException("反序列化异常：属性 " + f.getName() + "被忽略！");
		}
		Object enumObj = f.get(null);
		field.set(obj, enumObj);
	}

	// // demo
	// private static enum Color {
	// GREEN, RED,
	// }

	/**
	 * 循环向上转型, 获取对象的 DeclaredField.可以获得由父类继承来的属性!
	 * 
	 * @param object
	 *            : 子类对象
	 * @param fieldName
	 *            : 属性名
	 * @return 属性对象
	 */
	private static Field getDeclaredField_ContainsSuper(Object object,
			String fieldName) {
		Field field = null;
		Class<?> clazz = object.getClass();
		for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
			try {
				field = clazz.getDeclaredField(fieldName);
				return field;
			} catch (Exception e) {
				// 这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
				// 如果这里的异常打印或者往外抛，则就不会执行clazz =
				// clazz.getSuperclass(),最后就不会进入到父类中了
			}
		}
		if (field == null) {
			try {
				field = Object.class.getDeclaredField(fieldName);
				return field;
			} catch (Exception e) {
			}
		}
		return null;
	}

	// public static void main(String[] args) throws SecurityException,
	// NoSuchFieldException, IllegalArgumentException,
	// IllegalAccessException, InvocationTargetException {
	// List<Object> list = new ArrayList<Object>();
	// System.out.println(list instanceof ArrayList<?>);
	//
	// Object[] objs = { 1 };
	// System.out.println(objs.getClass().getComponentType().getName());
	// Color color = Color.GREEN;
	// System.out.println(color);
	// Field f = Color.class.getDeclaredField("GREEN");
	// Object object = f.get(null);
	// System.out.println("object=" + object);
	// System.out.println(object instanceof Color);
	// System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
	// System.out.println(String.class instanceof Class);
	// System.out.println(String.class.getName());
	// System.out.println("".getClass().getName());
	// Class<String> c = String.class;
	// System.out.println(c.getClass().getName());// java.lang.Class
	// System.out.println(c.toString());
	//
	// System.out.println(c.getClass().getClass().getClass().getClass());
	// System.out.println(c.getClass() == Class.class);
	// Map<Integer, String> m = new HashMap<Integer, String>();
	// Method put = null;
	// try {
	// put = m.getClass().getMethod("put", Object.class, Object.class);
	// } catch (NoSuchMethodException e) {
	// throw new SerializeException(e);
	// }
	// put.invoke(m, "i", "咦~");// 这里没做类型检查！
	// System.out.println("得到" + m.get("i"));
	// System.out.println("-------------------------------------------------");
	// Class cla = ArrayList.class.asSubclass(Collection.class);
	// System.out.println(cla.getName());
	// System.out.println(List.class.asSubclass(Collection.class));
	// System.out.println(int.class.getName());
	// }

}
