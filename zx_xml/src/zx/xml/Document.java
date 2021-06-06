package zx.xml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** xml文档对象 */
public class Document implements Serializable {

	/**
	 * 序列化版本号
	 */
	private static final long serialVersionUID = 1353311427641889804L;
	/*
	 * <?xml version="1.0" encoding="UTF-8"?>
	 */
	/**
	 * 默认版本
	 * */
	public final static String DEFAULT_VERSION = "1.0";
	/**
	 * UTF-8编码
	 * */
	public final static String UTF8 = "UTF-8";
	/**
	 * 默认编码 
	 * */
	public final static String DEFAULT_ENCODING = UTF8;
	/** 版本号 */
	private String version = DEFAULT_VERSION;
	/** 编码 */
	private String encoding = DEFAULT_ENCODING;
	/**GBK编码*/
	public final static String GBK = "gbk";
	/**GB2312编码*/
	public final static String GB2312 = "gb2312";
	/**ISO8859-1编码*/
	public final static String ISO8859_1 = "ISO8859-1";
	
	/**文档属性换行*/
	public static final int NEW_LINE = 0x0;
	/**文档属性不换行*/
	public static final int SINGLE_LINE = 0x1;
	/**
	 * 文档属性是否换行(格式化用)
	 * */
	public int attributeLine = SINGLE_LINE;
	
	/** 子节点列表,其中有且只有一个根节点，可能还有注释节点 */
	private List<Node> nodeList = new ArrayList<Node>();
	/**
	 * 类似<?xml version="1.0" encoding="utf-8" standalone="no"?>，可能还有其他内容
	 * */
	/**
	 * 属性列表
	 * */
	private final List<Attribute> headAttributeList = new ArrayList<Attribute>();

	public Document() {
		init();
	}
	
	private void init() {
		headAttributeList.add(new Attribute("version", DEFAULT_VERSION));
		headAttributeList.add(new Attribute("encoding", DEFAULT_ENCODING));
	}

	/**添加文档属性*/
	public void addAttribute(Attribute attribute){
		if(attribute==null){
			return;
		}
		int flag = 0;
		for(int i=0;i<headAttributeList.size();i++){
			Attribute att = headAttributeList.get(i);
			if(att.equals(attribute)){
				headAttributeList.remove(i);
				headAttributeList.add(i, attribute);
				flag++;
				break;
			}
		}
		if(flag == 0){
			headAttributeList.add(attribute);
		}
		if(attribute.getAttributeName().equalsIgnoreCase("encoding")){
			encoding = attribute.getAttributeValue();
		}
	}

	/**设置版本 */
	public void setVersion(String version) {
		if (version == null || ((version = version.trim()).equals(""))) {
			version = DEFAULT_VERSION;
		}
		this.version = version;
		addAttribute(new Attribute("version", this.version));
	}

	/**
	 * 获取编码
	 * */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * 设置编码
	 * */
	public void setEncoding(String encoding) {
		this.encoding = encoding == null
				|| (encoding = encoding.trim()).length() == 0 ? DEFAULT_ENCODING
				: encoding.trim();
		addAttribute(new Attribute("encoding", this.encoding));
	}

	/**
	 * 获取子节点列表
	 * */
	public List<Node> getNodeList() {
		return nodeList;
	}

	/** 设置节点list*/
	public void setNodeList(List<Node> nodeList) {
		if (nodeList == null || nodeList.size() == 0) {
			DocumentUtil.throwException("参数不能为空!");
		}
		int flag = 0;
		for (Node node : nodeList) {
			if(node instanceof TextNode){
				DocumentUtil.throwException("文档对象不能包含文本节点！");
			}
			if (node instanceof Element) {
				flag++;
			}
		}
		if (flag != 1) {
			DocumentUtil.throwException("只能有且只有一个根节点!");
		}
		this.nodeList = nodeList;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		try {
			Document document = (Document) obj;
			return version.equalsIgnoreCase(document.version)
					&& encoding.equalsIgnoreCase(document.encoding)
					&& nodeList.equals(document.nodeList);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 获取根节点
	 * */
	public Element getRootNode() {
		if (nodeList == null || nodeList.size() == 0) {
			return null;
		}
		for (Node node : nodeList) {
			if (node != null && node instanceof Element) {
				return (Element) node;
			}
		}
		return null;
	}

	/**
	 * 设置根节点
	 * */
	public boolean setRootNode(Element root) {
		if (root == null) {
			return false;
		}
		root.domFather = this;
		if (hasSetRootNode()) {
			for (int i = 0; i < nodeList.size(); i++) {
				Node node = nodeList.get(i);
				if (node != null && node instanceof Element) {
					nodeList.remove(i);
					nodeList.add(i, root);
					node.domFather = null;
					return true;
				}
			}
		}
		return nodeList.add(root);
	}

	/**
	 * 设置根节点
	 * */
	public boolean setRootNode(Element root, int index) {
		if (root == null) {
			return false;
		}
		root.domFather = this;
		if (hasSetRootNode()) {
			for (int i = 0; i < nodeList.size(); i++) {
				Node node = nodeList.get(i);
				if (node != null && node instanceof Element) {
					nodeList.remove(i);
					nodeList.add(index, root);
					node.domFather = null;
					return true;
				}
			}
		}
		nodeList.add(index, root);
		return true;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if(headAttributeList!=null&&headAttributeList.size()>0){
			sb.append("<?xml");
			if(attributeLine == SINGLE_LINE){
				for(int i=0;i<headAttributeList.size();i++){
					Attribute attribute = headAttributeList.get(i);
					sb.append(" "+attribute.toString());
				}
			}else if(attributeLine == NEW_LINE){
				for(int i=0;i<headAttributeList.size();i++){
					Attribute attribute = headAttributeList.get(i);
					if(i==0){
						sb.append(" "+attribute.toString());
					}else{
						sb.append("\n"+"    "+attribute.toString());
					}
				}
			}
			sb.append("?>");
		}
		for (Node node : nodeList) {
			sb.append("\n");
			if(node instanceof Element){
				((Element) node).simpleFormat = false;
			}
			sb.append(node.toString());
		}
		return sb.toString();
	}

	/**添加节点*/
	public boolean addNode(Node node) {
		if (node == null) {
			return false;
		}
		if(node instanceof TextNode){
			DocumentUtil.throwException("文档不能添加文本节点!");
		}
		if (node instanceof AnnotationNode) {
			node.domFather = this;
			return nodeList.add(node);
		} else {
			Element element = (Element) node;
			return setRootNode(element);
		}
	}

	/**
	 * 是否已经设置根节点 
	 * */
	public boolean hasSetRootNode() {
		return getRootNode() != null;
	}

	/**
	 * 保存到文件
	 * */
	public void saveTo(File file) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
		writer.write(toString());
		if(writer!=null){
			writer.flush();
			writer.close();
			writer = null;
		}
	}
	
	/**
	 * 保存到文件
	 * */
	public void saveTo(String filename) throws IOException{
		saveTo(new File(filename));
	}

	/**
	 * 获取属性列表
	 * */
	public List<Attribute> getHeadAttributeList() {
		return headAttributeList;
	}

	/**
	 * 文档格式化
	 * */
	public void setRootNodeAttributeLine(boolean singleLine){
		Element root = getRootNode();
		if(root!=null){
			root.setAll_attributeLine(singleLine);
		}
	}
	
	/**
	 * 移除节点
	 * */
	public boolean remove(Node node){
		DocumentUtil.throwExceptionIfNull(node);
		if(nodeList==null||nodeList.size()==0){
			DocumentUtil.throwException(new RuntimeException("空指针！"));
		}
		if(nodeList.remove(node)){
			node.domFather = null;
			return true;
		}
		return false;
	}
	
	/**
	 * 得到第一个子节点
	 * */
	public Node getFirstChild(){
		if(nodeList==null||nodeList.isEmpty()){
			return null;
		}
		return nodeList.get(0);
	}
	
	/**
	 * 得到最后一个子节点
	 * */
	public Node getLastChild(){
		if(nodeList==null||nodeList.isEmpty()){
			return null;
		}
		return nodeList.get(nodeList.size()-1);
	}
}
