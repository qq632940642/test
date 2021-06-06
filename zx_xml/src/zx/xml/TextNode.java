package zx.xml;

/**
 * 文本节点，纯字符串
 * */
public class TextNode extends Node {

	private static final long serialVersionUID = 4992529597680776751L;
	/**
	 * 节点内容
	 * */
	private String text;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		if (text != null) {
			if (text.indexOf("<") >= 0) {
				DocumentUtil.throwException("非法格式！");
			}
		}
		this.text = text;
	}

	public TextNode() {
	}

	public TextNode(String text) {
		setText(text);
	}
	
	@Override
	public String printSpace(){
		return super.printSpace();
	}

	@Override
	public String toString() {
		if (text == null) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		String space = printSpace();
		if (text != null && text.length() > 0) {
			sb.append(space);
//			char c = text.charAt(0);
//			if ((c < '0' && c < 'a' && c < 'A')
//					|| (c > '9' && c > 'z' && c > 'Z')) {
//				sb.append("");// 修复中文空格bug?
//			}
			sb.append(text);
		}
		return sb.toString();
	}
	
}
