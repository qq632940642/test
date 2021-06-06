package zx.xml;

/**
 * 注释节点
 * 
 * @形如<!-- xxxx -->
 * */
public class AnnotationNode extends Node {

	private static final long serialVersionUID = -8705251712083166809L;
	/** 注释内容 */
	private String annotation;

	public AnnotationNode() {
		annotation = "";
	}

	public AnnotationNode(String annotation) {
		if (annotation == null) {
			this.annotation = "";
		} else {
			this.annotation = annotation;
		}
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation == null ? "" : annotation;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(printSpace());
		sb.append("<!--");
		sb.append(annotation);
		sb.append("-->");
		return sb.toString();
	}
}
