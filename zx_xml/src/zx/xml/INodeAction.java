package zx.xml;

public interface INodeAction {
	/**
	 * 获取上一个节点
	 * */
	public Node getPreviousSibling();
	/**
	 * 获取下一个节点
	 * */
	public Node getNextSibling();
	/**
	 * root节点为第0辈分，她的直接子节点为第一辈分，以此类推.如果没有根节点则返回null
	 * @return 节点辈分
	 * */
	public Integer getBeifen();
	/**
	 * 首行缩进,文档美化
	 * */
	public String printSpace();
}
