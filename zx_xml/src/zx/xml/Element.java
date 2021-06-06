package zx.xml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 普通节点
 * */
public class Element extends Node implements INodeAction {

	private static final long serialVersionUID = 8400116833587665357L;
	/**
	 * 属性列表
	 * */
	private List<Attribute> attributeList = new ArrayList<Attribute>();
	/**
	 * 文本内容
	 * */
	// private String textComment;
	/**
	 * 子节点列表
	 * */
	private final List<Node> nodeList = new ArrayList<Node>();
	/** 节点名 */
	private String name;

	/** 节点属性换行 */
	public static final int NEW_LINE = 0x0;
	/** 节点属性不换行 */
	public static final int SINGLE_LINE = 0x1;
	/**
	 * 节点属性是否换行(格式化用)
	 * */
	public int attributeLine = SINGLE_LINE;

	/** 设置自己以及所有的后代节点是否换行,true则不换行，false换行 */
	public void setAll_attributeLine(boolean singleLine) {
		attributeLine = singleLine ? SINGLE_LINE : NEW_LINE;
		Set<Node> nodes = getChildNodes();
		for (Iterator<Node> it = nodes.iterator(); it.hasNext();) {
			Node node = it.next();
			if (node instanceof Element) {
				((Element) node).attributeLine = attributeLine;
			}
		}
	}

	/**获取属性列表*/
	public List<Attribute> getAttributeList() {
		return attributeList;
	}
	
	/**
	 * 由属性名获取属性值
	 * */
	public String getAttributeValue(String attributeName){
		for(Attribute att:attributeList){
			if(att.getAttributeName().equals(attributeName)){
				return att.getAttributeValue();
			}
		}
		return null;
	}

	/**
	 * 清空属性
	 * */
	public void clearAllAttribute() {
		attributeList.clear();
	}

	/**
	 * 添加属性
	 * */
	public void addAttribute(Attribute attribute) {
		if (attribute == null) {
			return;
		}
		int flag = 0;
		for (int i = 0; i < attributeList.size(); i++) {
			Attribute att = attributeList.get(i);
			if (att.equals(attribute)) {
				attributeList.remove(i);
				attributeList.add(i, attribute);
				flag++;
				break;
			}
		}
		if (flag == 0) {
			attributeList.add(attribute);
		}
	}

	/**
	 * 删除属性
	 * @param attributeName 属性名
	 * */
	public void deleteAttributeByAttributeName(String attributeName) {
		for (Attribute att : attributeList) {
			if (att.getAttributeName().equals(attributeName)) {
				attributeList.remove(att);
				return;
			}
		}
	}

	/**
	 * 添加属性
	 * */
	public void addAttributes(List<Attribute> list) {
		if (list != null) {
			for (Attribute attribute : list) {
				addAttribute(attribute);
			}
		}
	}

	/**
	 * 设置属性
	 * */
	public void setAttributes(List<Attribute> list) {
		attributeList = list;
	}

	/**
	 *是否包含属性
	 * */
	public boolean containsAttribute(Attribute attribute) {
		for (Attribute att : attributeList) {
			if (att.equals(attribute)
					&& att.getAttributeValue().equals(
							attribute.getAttributeValue())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 取得子节点列表(直接子节点)
	 * */
	public List<Node> getNodeList() {
		return nodeList;
	}

	// public String getTextComment() {
	// return textComment;
	// }

	// public void setTextComment(String textComment) {
	// if(nodeList.size()==0){
	// this.textComment = textComment;
	// }else{
	// DocumentUtil.throwException("不是文本节点不能添加文本内容！");
	// }
	// }

	/**
	 * 添加子节点
	 * */
	public void addSonNode(Node node) {
		DocumentUtil.throwExceptionIfNull(node);
		// if(textComment!=null && node instanceof Element){
		// DocumentUtil.throwException("文本节点不能添加节点！");
		// }
		if (nodeList.add(node)) {
			node.father = this;
		}
	}

	/**
	 * 删除子节点
	 * */
	public void deleteSonNode(Node node) {
		if (nodeList.remove(node)) {
			node.father = null;
		}
	}

	/** 查询本节点以及其后代节点中名称为name的所有节点 */
	public Set<Node> getNodesByName(String name) {
		Set<Node> set = getChildNodes();
		set.add(this);
		Set<Node> s = new HashSet<Node>();
		Iterator<Node> it = set.iterator();
		for (; it.hasNext();) {
			Node node = it.next();
			if (!(node instanceof Element)) {
				continue;
			}
			Element element = (Element) node;
			if (element.name.equals(name)) {
				s.add(node);
			}
		}
		return s;
	}

	/**
	 * 得到本节点的所有后代节点
	 * */
	public Set<Node> getChildNodes() {
		Set<Node> set = new HashSet<Node>();
		Element node = this;
		if (node.nodeList.size() > 0) {
			for (Node n : node.nodeList) {
				set.add(n);
				if (n instanceof Element) {
					Element element = (Element) n;
					set.addAll(element.getChildNodes());
				}
			}
		}
		return set;
	}

	/**
	 * 获取节点名
	 * */
	public String getName() {
		return name;
	}

	/**
	 * 设置节点名
	 * */
	public void setName(String name) {
		DocumentUtil.throwExceptionIfNull(name);
		this.name = name;
	}

	/*
	 * 考虑两种形式: <Adv subDirectory="" Img="02.png"/>
	 * 
	 * <adv subDirectory="" Img="02.png"> ... </adv>
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		String space = printSpace();
		// 用递归实现
		sb.append(space);
		sb.append("<");
		sb.append(name);
		for (int i = 0; i < attributeList.size(); i++) {
			Attribute attribute = attributeList.get(i);
			if (i == 0) {
				sb.append(" " + attribute.toString());
			} else {
				sb.append(attributeLine == SINGLE_LINE ? " "
						+ attribute.toString() : "\n" + space + "    "
						+ attribute.toString());
			}
		}

		// if(textComment==null&&nodeList.size()==0){
		if (nodeList.size() == 0&&simpleFormat) {
			sb.append("/>");
			return sb.toString();
		}
		sb.append(">");
		for (Node node : nodeList) {
			sb.append("\n");
			sb.append(node.toString());
		}
		// if(textComment!=null&&textComment.length()>0){
		// sb.append("\n");
		// sb.append(space+"  ");
		// char c = textComment.charAt(0);
		// if((c<'0'&&c<'a'&&c<'A')||(c>'9'&&c>'z'&&c>'Z')){
		// sb.append("          ");//修复中文空格bug
		// }
		// sb.append(textComment);
		// }
		sb.append("\n");
		sb.append(space);
		sb.append("</" + name + ">");
		return sb.toString();
	}
	
	/**
	 * 当没有子节点时，是否采用简单格式
	 * */
	public boolean simpleFormat = true;

	/** 获取第一个子节点,如果没有则返回null */
	public Node getFirstChildNode() {
		return nodeList.isEmpty() ? null : nodeList.get(0);
	}

	/** 获取最后一个子节点，如果没有则返回null */
	public Node getLastChildNode() {
		return nodeList.isEmpty() ? null : nodeList.get(nodeList.size() - 1);
	}

	/** 获取根节点 */
	public Element getRootNode() {
		if (father == null) {
			if (domFather == null) {
				DocumentUtil.throwException("节点还没有添加到文档对象！");
			}else{
				return this;
			}
		}
		Element node = this;
		while(node.father!=null){
			node = node.father;
			if(node.father==null){
				if (node.domFather == null) {
					DocumentUtil.throwException("节点还没有添加到文档对象！");
				}else{
					return node;
				}
			}
		}
		return null;
	}
}
