package zx.xml;

import java.io.Serializable;
/*
 * 形如 btnEDate="2014-12-15"
 * */
public class Attribute implements Serializable{

	private static final long serialVersionUID = 4233216982243098384L;
	/**属性名*/
	private String attributeName;
	/**属性值*/
	private String attributeValue = "";
	
	public Attribute(){}
	
	public Attribute(String attributeName,String attributeValue){
		if(attributeName==null||attributeName.trim().equals("")){
			DocumentUtil.throwException(DocumentUtil.ARGUMENT_NULL);
		}
		this.attributeName = attributeName.trim();
		this.attributeValue = attributeValue==null?"":attributeValue;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(attributeName);
		sb.append("=\"");
		sb.append(attributeValue);
		sb.append("\"");
		return sb.toString();
	}
	
	public String getAttributeName() {
		return attributeName;
	}
	public void setAttributeName(String attributeName) {
		if(attributeName==null||attributeName.trim().equals("")){
			DocumentUtil.throwException(DocumentUtil.ARGUMENT_NULL);
		}
		this.attributeName = attributeName.trim();
	}
	public String getAttributeValue() {
		return attributeValue;
	}
	public void setAttributeValue(String attributeValue) {
		if(attributeValue==null){
			DocumentUtil.throwException("空指针！");
		}
		this.attributeValue = attributeValue;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this==obj){
			return true;
		}
		try{
			Attribute att = (Attribute)obj;
			return attributeName.equals(att.attributeName);
		}catch(Exception e){
			return false;
		}
	}
}
