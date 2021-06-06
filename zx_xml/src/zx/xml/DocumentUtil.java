package zx.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
/**
 * @author zx
 * @category 解析xml文件
 * @link <a href='http://blog.csdn.net/bidelicai/article/details/17675087'>我的博客</a>
 * */
public class DocumentUtil {
	public static final String ILLEGALARGUMENT = "非法参数异常！";
	public static final String ARGUMENT_NULL = "参数不能为空！";

	private DocumentUtil() {
	}

	/** 打印和扔出异常 */
	public static void throwException(String msg) {
		System.err.println(msg);
		throw new RuntimeException(msg);
	}

	/** 打印和扔出异常 */
	public static void throwException(Exception e) {
		System.err.println(e);
		throw new RuntimeException(e);
	}

	/**
	 * 解析xml文件生成文档对象
	 * 
	 * @throws IOException
	 */
	public static Document parse(File xmlFile) throws IOException {
		String xmlString = getXMLString(xmlFile);
//		throwExceptionIfNull(xmlString);
//		Document document = new Document();
//		// 得到头部说明
//		List<Attribute> headAttributeList = getHeadAttributeList(xmlString);
//		for (Attribute attribute : headAttributeList) {
//			document.addAttribute(attribute);
//		}
//		// 得到NodeList
//		List<Node> nodeList = getNodeList(xmlString);
//		for (Node node : nodeList) {
//			document.addNode(node);
//		}
//		return document;
		return parseXMLString(xmlString);
	}
	
	/**
	 * 解析xml字符串生成文档对象
	 * 
	 * @throws IOException
	 */
	public static Document parseXMLString(String xmlString){
		throwExceptionIfNull(xmlString);
		Document document = new Document();
		// 得到头部说明
		List<Attribute> headAttributeList = getHeadAttributeList(xmlString);
		for (Attribute attribute : headAttributeList) {
			document.addAttribute(attribute);
		}
		// 得到NodeList
		List<Node> nodeList = getNodeList(xmlString);
		for (Node node : nodeList) {
			document.addNode(node);
		}
		return document;
	}

	/**
	 * 解析xml文件生成文档对象
	 * 
	 * @throws IOException
	 */
	public static Document parse(String xmlFile) throws IOException {
		return parse(new File(xmlFile));
	}

	/** 获取头部属性 */
	private static List<Attribute> getHeadAttributeList(String xmlString) {
		String temp = xmlString;
		try {
			temp = temp.substring(temp.indexOf("<?xml") + "<?xml".length(),
					temp.indexOf("?>"));// 可能数组越界
		} catch (Exception e) {
//			e.printStackTrace();
			return new ArrayList<Attribute>();
		}
		// temp 形如 version="1.0" encoding="utf-8"
		return getAttributeList(temp);
	}

	/**
	 * str形如 version="1.0" encoding="utf-8"，从中获得属性
	 * */
	private static List<Attribute> getAttributeList(String str) {
		List<Attribute> attributeList = new ArrayList<Attribute>();
		String temp = str;
		temp = StringUtil.trim(temp);
		String attname = "";
		String attValue = "";
		for (int i = 0; i < temp.length(); i++) {
			char c = temp.charAt(i);
			if (StringUtil.BLANKS.indexOf(c + "") == -1 && c != '=') {
				attname += c;
			} else {
				attValue = StringUtil.getSubStringBeetween(temp, '"');
				attributeList.add(new Attribute(attname, attValue));
				attname = new String();
				attValue = new String();
				List<Integer> list = StringUtil.getIndexs(temp, '"');
				int idx = list.get(1);
				if (idx < temp.length() - 1) {
					temp = temp.substring(idx + 1);
					temp = StringUtil.trim(temp);
				} else {
					temp = "";
				}
				i = -1;
			}
		}
		return attributeList;
	}

	/**
	 * 获取到节点列表，正如document对象含有一个普通节点和其他注释节点
	 * */
	private static List<Node> getNodeList(String xmlString) {
		List<String> list = getNodeStringList(xmlString);
		List<Node> nodeList = new ArrayList<Node>();
		for (String str : list) {
			Node node;
			//文档对象里没有文本节点，所以只两种可能
			if (str.startsWith("<!--")) {
				node = parseAnnotationNodeString(str);
			} else {
				node = parseElementString2(str);
			}
			nodeList.add(node);
		}
		return nodeList;
	}

	/** 解析注释节点字符串 */
	private static AnnotationNode parseAnnotationNodeString(String ElementString) {
		AnnotationNode node = null;
		String str = ElementString.substring(4, ElementString.length() - 3);
		node = new AnnotationNode(str);
		return node;
	}

	private static Element parseElementString2(String elementString){
		Element element = new Element();
		String name = "";
		element.setName(name = getName(elementString));
		element.setAttributes(getAttributes(elementString));
		List<String> childsStr = splitElement(elementString);
		for(String eleStr:childsStr){
			Node node;
			if(getNodeClass(eleStr).getName().indexOf("TextNode")>=0){//文本节点
				node = new TextNode(eleStr);
				element.addSonNode(node);
			}else if(getNodeClass(eleStr).getName().indexOf("AnnotationNode")>=0){//注释节点
				node = parseAnnotationNodeString(eleStr);
				element.addSonNode(node);
			}else{//普通节点
				element.addSonNode(node = parseElementString2(eleStr));
			}
		}
		return element;
	}
	
	/**
	 * 获取节点类型
	 * */
	static Class getNodeClass(String nodeStr){
		char c = nodeStr.charAt(0);
		if(c!='<'){//文本节点
			return TextNode.class;
		}
		if(nodeStr.indexOf("<!--")==0){//注释节点
			return AnnotationNode.class;
		}
		//普通节点
		return Element.class;
	}
	
	/**
	 * 分割节点字符串，获取子节点list
	 * */
    private static List<String> splitElement(String elementString){
    	String temp = elementString;
    	List<String> nodeStringList = new ArrayList<String>();
    	int rightAngleBracket = temp.indexOf(">");//开始
    	int slash_rightAngleBracket = temp.indexOf("/>");//结束
    	if(slash_rightAngleBracket!=-1&&slash_rightAngleBracket<rightAngleBracket){//无子节点
    		return nodeStringList;
    	}
    	//或者看第二个“<”的位置
    	//扔掉最后的结束标签字符串
    	temp = temp.substring(rightAngleBracket+1, temp.lastIndexOf("</"));
    	temp = StringUtil.trim(temp);
    	StringBuffer nodeString = new StringBuffer();
    	while(temp.length()>0){
    		if(temp.startsWith("<!--")){
    			for (int i = 0; i < temp.length(); i++) {
					if (i <= 3) {
						nodeString.append(temp.charAt(i));
					} else {
						char c = temp.charAt(i);
						char c1 = temp.charAt(i - 1);
						char c2 = temp.charAt(i - 2);
						if (c == '>' && c1 == c2 && c2 == '-') {
							nodeString.append(c);
							nodeStringList.add(nodeString.toString());
							nodeString = new StringBuffer();
							// 扔掉先前的
							if (i != temp.length() - 1) {
								temp = temp.substring(i + 1);
								temp = StringUtil.trim(temp);
							} else {
								temp = "";
							}
							break;
						} else {
							nodeString.append(c);
						}
					}
				}
    		}else if(temp.charAt(0)!='<'){//文本节点
    			for (int i = 0; i < temp.length(); i++) {
    				char c = temp.charAt(i);
    				if(c!='<'){
    					nodeString.append(c);
    					if(i==temp.length()-1){
    						nodeStringList.add(nodeString.toString());
    						temp = "";
    					}
    				}else{//文本节点结束
    					if(StringUtil.trim(nodeString.toString())!=""){
    						nodeStringList.add(StringUtil.trim(nodeString.toString()));
    					}
    					nodeString = new StringBuffer();
						// 扔掉先前的
						temp = temp.substring(i);
						temp = StringUtil.trim(temp);
						break;
    				}
    			}
    		}else{//普通节点
				// 下面目的是找出结束标签
				Stack<Node> stack = new Stack<Node>();// 写一个栈stack<Node>，节点开始时就压入栈中，结束时就弹出。作为标记
				Element element = new Element();// 只是临时变量，标记用
				stack.push(element);
				for (int i = 1; i < temp.length(); i++) {
					char c = temp.charAt(i);
					if (stack.empty()) {
						// 这里已经找到根节点结束地点
						int j;
						for(j=i;j<temp.length();j++){
							char ch = temp.charAt(j);
							if(ch=='>'){
								nodeString.append(temp.substring(0, j+1));
								break;
							}
						}
						nodeStringList.add(nodeString.toString());
						nodeString = new StringBuffer();
						// 去掉先前的
						if(j-1==temp.length()-1){
							temp = "";
						}else{
							temp = temp.substring(j+1);
							temp = StringUtil.trim(temp);
						}
						break;
					}
					Node node = stack.peek();
					if (c == '<' && temp.charAt(i + 1) == '!' && temp.charAt(i + 2) == temp.charAt(i + 3) && temp.charAt(i + 2) == '-') {// 注释开始
						AnnotationNode annotationNode = new AnnotationNode();
						stack.push(annotationNode);
					} else if (c == '-' && c == temp.charAt(i + 1) && temp.charAt(i + 2) == '>') {// 注释结束
						stack.pop();
						i += 2;
					} else if (node instanceof AnnotationNode) {// 注释中
						continue; 
					} else if (c == '<') {
						// 判断是开始标签或结束标签
						if (temp.charAt(i + 1) != '/') {// 开始标签
							Element ele = new Element();
							stack.push(ele);
						} else if (temp.charAt(i + 1) == '/') {// </Root>为结束标签 
							stack.pop();
						}
					} else if (c == '/' && temp.charAt(i + 1) == '>') {// 结束标签
						stack.pop();
					} else if (node instanceof Element) {
						continue;
					} else {
						continue;
					}
				}
    		}
    	}
    	return nodeStringList;
    }
	
	/**
	 * 获取属性列表
	 * */
	private static List<Attribute> getAttributes(String elementString){
		List<Attribute> list;
		String temp = elementString;
		String attr = "";
		for(int i=0;i<temp.length();i++){
			char c = temp.charAt(i);
			if(StringUtil.BLANKS.indexOf(c+"")!=-1){
				temp = temp.substring(i);
				break;
			}else if(c=='>'){
				return new ArrayList<Attribute>();
			}
		}
		temp = StringUtil.trim(temp);
		
		for(int i=0;i<temp.length();i++){
			char c = temp.charAt(i);
			if(c=='>'||(i+1<temp.length()&&c=='/'&&temp.charAt(i+1)=='>')){
				break;
			}else{
				attr+=c;
			}
		}
		list = getAttributeList(attr);
		return list;
	}
	
	/**
	 * 获取节点名
	 * */
	private static String getName(String elementString){
		String name = "";
		for(int i=1;i<elementString.length();i++){
			char c = elementString.charAt(i);
			if(StringUtil.BLANKS.indexOf(c+"")==-1&&c!='/'&&c!='>'){
				name+=c;
			}else{
				break;
			}
		}
		return name;
	}
	
	/**
	 * 获取文档节点字符串list
	 * */
	private static List<String> getNodeStringList(String xmlString) {
		List<String> nodeStringList = new ArrayList<String>();
		// 先去掉文件头部,获取节点字符串
		int idx = xmlString.indexOf("?>");
		String temp = xmlString;
		if (idx != -1) {
			temp = xmlString.substring(idx);
		}
		for (int i = 0; i < temp.length(); i++) {
			char c = temp.charAt(i);
			if (c == '<') {
				temp = temp.substring(i, temp.lastIndexOf('>') + 1);
				break;
			}
		}
		// 分割temp为节点字符串
		StringBuffer nodeString = new StringBuffer();
		while (temp.length() > 0) {
			if (temp.startsWith("<!--")) {// 注释节点
				for (int i = 0; i < temp.length(); i++) {
					if (i <= 3) {
						nodeString.append(temp.charAt(i));
					} else {
						char c = temp.charAt(i);
						char c1 = temp.charAt(i - 1);
						char c2 = temp.charAt(i - 2);
						if (c == '>' && c1 == c2 && c2 == '-') {
							nodeString.append(c);
							nodeStringList.add(nodeString.toString());
							nodeString = new StringBuffer();
							// 扔掉先前的
							if (i != temp.length() - 1) {
								temp = temp.substring(i + 1);
								for (int j = 0; j < temp.length(); j++) {
									char ch = temp.charAt(j);
									if (ch == '<') {
										temp = temp.substring(j);
										break;
									}
								}
							} else {
								temp = "";
							}
							break;
						} else {
							nodeString.append(c);
						}
					}
				}
			} else {// 普通节点
					// 先获得节点名
				String name = "";
				for (int i = 1; i < temp.length(); i++) {
					char c = temp.charAt(i);
					if (c != ' ' && c != '\t' && c != '/' && c != '>') {
						name += c;
					} else {
						break;
					}
				}
				// 下面目的是找出结束标签
				Stack<Node> stack = new Stack<Node>();// 写一个栈stack<Node>，节点开始时就压入栈中，结束时就弹出。作为标记
				Element element = new Element();// 只是临时变量，标记用
				stack.push(element);
				for (int i = 1; i < temp.length(); i++) {
					char c = temp.charAt(i);
					char c1 = temp.charAt(i + 1);
					char c2 = temp.charAt(i + 2);
					if (stack.empty()) {
						// 这里已经找到根节点结束地点
						int j;
						for(j=i;j<temp.length();j++){
							char ch = temp.charAt(j);
							if(ch=='>'){
								nodeString.append(temp.substring(0, j+1));
								break;
							}
						}
						nodeStringList.add(nodeString.toString());
						nodeString = new StringBuffer();
						// 去掉先前的
						if(j-1==temp.length()-1){
							temp = "";
						}else{
							temp = temp.substring(j+1);
							//去掉空格
							temp = StringUtil.trim(temp);
						}
						break;
					}
					Node node = stack.peek();
					if (c == '<' && c1 == '!' && c2 == temp.charAt(i + 3) && c2 == '-') {// 注释开始
						AnnotationNode annotationNode = new AnnotationNode();
						stack.push(annotationNode);
					} else if (c == '-' && c == c1 && c2 == '>') {// 注释结束
						stack.pop();
						i += 2;
					} else if (node instanceof AnnotationNode) {// 注释中
						continue; 
					} else if (c == '<') {
						// 判断是开始标签或结束标签
						if (c1 != '/') {// 开始标签
							Element ele = new Element();
							stack.push(ele);
						} else if (c1 == '/') {// </Root>为结束标签 ,只有这里可能是根节点结束
							stack.pop();
						}
					} else if (c == '/' && c1 == '>') {// 结束标签
						stack.pop();
					} else if (node instanceof Element) {
						continue;
					} else {
						continue;
					}
				}
			}
		}
		return nodeStringList;
	}

	/** 获取版本 */
	static String getVersion(String str) {
		int versionidx = str.indexOf("version");
		String version = "";
		int idx = 0;
		for (int i = versionidx; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == '"') {
				idx = i;
				break;
			}
		}
		if (idx == 0) {
			throwException("非法文件！");
		}
		for (int i = idx + 1; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c != '"') {
				version += c;
			} else {
				break;
			}
		}
		return version;
	}

	/**
	 * 读取文件字符串
	 * */
	public static String getXMLString(File file) throws IOException {
		String encoding = getEncoding(file);
		if (encoding == null) {
			encoding = System.getProperty("file.encoding");
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), encoding));
		String temp;
		StringBuffer sb = new StringBuffer();
		while ((temp = reader.readLine()) != null) {
			sb.append(temp);
			sb.append("\n");
		}
		if (reader != null) {
			reader.close();
			reader = null;
		}
		return sb.toString().substring(0, sb.length()-1);
	}

	/**读取文件字符串*/
	public static String getXMLString(String filename) throws IOException {
		return getXMLString(new File(filename));
	}

	/** 获得xml文件编码 */
	private static String getEncoding(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(file)));
		String temp;
		StringBuffer sb = new StringBuffer();
		String encoding = "";
		String mEncoding = "";
		while ((temp = reader.readLine()) != null) {
			sb.append(temp);
			int encodingidx = sb.indexOf("encoding");
			if (encodingidx > 0 && sb.indexOf("?>") > 0) {
				encoding = sb.substring(encodingidx);
				encoding = encoding.substring(encoding.indexOf("\"") + 1,
						encoding.lastIndexOf("\""));
				for (int j = 0; j < encoding.length(); j++) {
					char c = encoding.charAt(j);
					if (c != '"') {
						mEncoding += c;
					} else {
						if (reader != null) {
							reader.close();
							reader = null;
						}
						return mEncoding;
					}
				}
				if (reader != null) {
					reader.close();
					reader = null;
				}
				return mEncoding;
			}
		}
		if (reader != null) {
			reader.close();
			reader = null;
		}
		return null;
	}

	/** 判断参数空指针 */
	public static void throwExceptionIfNull(Object obj) {
		if (obj == null) {
			throwException(ARGUMENT_NULL);
		}
		if (obj instanceof java.lang.String) {
			String str = (String) obj;
			if (str.trim().equals("")) {
				throwException(ARGUMENT_NULL);
			}
		}
	}
	 
	 static class StringUtil {
		
		public static final String BLANKS = " \t\n";
		/**去掉首尾的空格、制表符、回车符*/
		public static String trim(String str){
			if(str==null){
				return str;
			}
			String ss = str.trim();
			for(int i=0;i<ss.length();i++){
				char c = ss.charAt(i);
				if(BLANKS.indexOf(c+"")==-1){
					ss = ss.substring(i);
					break;
				}
			}
			for(int i=ss.length()-1;i>=0;i--){
				char c = ss.charAt(i);
				if(BLANKS.indexOf(c+"")==-1){
					ss = ss.substring(0,i+1);
					break;
				}
			}
			return ss;
		}
		
		/**
		 * 获取字符串str中位于两个相同字符ch之间的字符串，ch为在字符串str中出现的所有位置的前两个，例如
		 * getSubStringBeetween("mimi...mi",'m') 返回字符串i
		 * */
		public static String getSubStringBeetween(String str,char ch){
			String temp = str;
			int idx = temp.indexOf(ch+"");
			temp = temp.substring(idx+1);
			idx = temp.indexOf(ch+"");
			temp = temp.substring(0,idx);
			return temp;
		}
		
		/**
		 * 得到字符ch在字符串src中的所有索引
		 * */
		public static List<Integer> getIndexs(String src,char ch){
			List<Integer> list = new ArrayList<Integer>();
			if(src==null||src.length()==0){
				return list;
			}
			for(int i=0;i<src.length();i++){
				if(src.charAt(i)==ch){
					list.add(i);
				}
			}
			return list;
		}
		
	}

	//test
//	public static void main(String[] args) throws IOException {
////		 String xmlString = getXMLString("c:test/ScreenSaver.XML");
////		 System.out.println(xmlString+"\n"+"--------------------------------------------");
////		 List<String> list = getNodeStringList(xmlString);//ok啦
////		 for(String str:list){
////		 System.out.println(str+"\n===============================================");
////		 }
//		 Document document = parse("c:test2/ScreenSaver.XML");
//		 System.out.println(document);
////		System.out
////				.println(getHeadAttributeList(getXMLString("c:test/qwerty.XML")));
//		 
//		 Element root = document.getRootNode();
//		 Set<Node> advs = root.getNodesByName("Adv");
//		 Iterator<Node> it = advs.iterator();
//		 for(;it.hasNext();){
//			 Element node = (Element) it.next();
//			 List<Attribute> list = node.getAttributeList();
//			 for(Attribute att:list){
//				 if(node.containsAttribute(new Attribute("Img", "05.png"))){
//					 node.addAttribute(new Attribute("SDate", "2013-12-16"));
//					 node.addAttribute(new Attribute("EDate", "2020-12-16"));
//					 node.addAttribute(new Attribute("btnSDate", ""));
//					 node.addAttribute(new Attribute("btnEDate", ""));
//					 break;
//				 }
//			 }
//		 }
////		 Set<Node> nodes = root.getNodesByName("mimi");
////		 Node[] mimi = new Node[nodes.size()];
////		 nodes.toArray(mimi);
////		 Element aimimi = (Element) mimi[0];
////		 TextNode aimimiNode = (TextNode) aimimi.getNodeList().get(0);
////		 aimimiNode.setText("爱咪咪。。。");
//		 document.saveTo("c:test2/ScreenSaver.XML");
//		 System.exit(0);
//	}
}
