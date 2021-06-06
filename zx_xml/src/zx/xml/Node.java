package zx.xml;

import java.io.Serializable;
import java.util.List;

/**
 * xml文档节点，只可以是普通节点，注释节点,文本节点
 * */
public abstract class Node implements Serializable,INodeAction{

	/**
	 * 序列化版本号
	 */
	private static final long serialVersionUID = 3858789701720999153L;
	/**
	 * 父节点.规定 根节点的父节点为空
	 * */
    protected Element father;
    /**
     * 由文档对象直接添加的节点 domFather赋值为document对象
     * */
    protected Document domFather;
    
    @Override
	public Integer getBeifen(){
		Integer n;
		if(father==null){
			n = 0;
			return n;
		}
		Node node = this;
		n = 0;
		while(node.father!=null){
			n++;
			node = node.father;
			if(node.father==null){
				return n;
			}
		}
		if(!(node.father==null)){
			return null;
		}
		return n;
	}
	
    /**
     * 获取父节点
     * */
	public Element getFather() {
		return father;
	}
	@Override
	public String printSpace() {
		StringBuffer sb = new StringBuffer();
		Integer n = getBeifen();
		if (n == null) {
			return sb.toString();
		}
		for (int i = 0; i < n; i++) {
			sb.append("  ");
		}
		return sb.toString();
	}
	
	public Document getDomFather() {
		return domFather;
	}
	
	@Override
	public Node getPreviousSibling(){
		if(father==null){
			if(domFather==null){
				DocumentUtil.throwException("节点还没添加到文档！");
			}else{
				List<Node> nodeList = domFather.getNodeList();
				int idx = nodeList.indexOf(this);
				return idx==0?null:nodeList.get(idx-1);
			}
		}
		List<Node> nodeList = father.getNodeList();
		int idx = nodeList.indexOf(this);
		return idx==0?null:nodeList.get(idx-1);
	}
	
	@Override
	public Node getNextSibling() {
		if(father==null){
			if(domFather==null){
				DocumentUtil.throwException("节点还没添加到文档！");
			}else{
				List<Node> nodeList = domFather.getNodeList();
				int idx = nodeList.indexOf(this);
				return idx==nodeList.size()-1?null:nodeList.get(idx+1);
			}
		}
		List<Node> nodeList = father.getNodeList();
		int idx = nodeList.indexOf(this);
		return idx==nodeList.size()-1?null:nodeList.get(idx+1);
	}
}
