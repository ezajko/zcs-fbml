/*
 *   Copyright (C) 2006 CINCOM SYSTEMS, INC.
 *   All Rights Reserved
 *   Copyright (C) 2006 Igor Mekterovic
 *   All Rights Reserved
 *   Copyright (C) 2008 KorusConsulting
 *   All Rights Reserved
 */
package ru.korusconsulting.connector.base;

import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.CDATA;
import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Text;

import com.funambol.framework.logging.FunambolLogger;
import com.funambol.framework.logging.FunambolLoggerFactory;

public class XMLDocumentWriter {
	protected FunambolLogger logger;

	/** Initialize logger */
	public XMLDocumentWriter() {
		logger = FunambolLoggerFactory.getLogger("funambol.zimbra.soap");
	}

	public void write(Node node) {
		write(node, "", "");
	}
	
	public void write(String xml, String msg) {
	    logger.debug(msg+"\n"+xml);
    }
	
	public void write(Node node, String align, String msg) {
	    if(logger.isDebugEnabled()){
    		StringBuffer stringBuffer = new StringBuffer();
    		write(node, align, stringBuffer);
    		write(stringBuffer.toString(), msg);
	    }
	}

	@SuppressWarnings("unchecked")
	private void write(Node node, String align, StringBuffer sb) {
		// log output depends on the type of the node
		short nodeType = node.getNodeType();
		if (nodeType == Node.DOCUMENT_NODE) {
			sb.append(align + "<?xml version='1.0'?>\n");
			List childs = ((Document) node).selectNodes("child::node()");
			Iterator<Element> iterator = childs.iterator();
			while (iterator.hasNext())// till not null
			{
				write(iterator.next(), align, sb); // log Output node
			}
		} else if (nodeType == Node.DOCUMENT_TYPE_NODE)// <!DOCTYPE> tag
		{
			sb.append("<!DOCTYPE " + ((DocumentType) node).getName() + ">\n");
		} else if (nodeType == Node.ELEMENT_NODE)// Most nodes are Elements
		{
			Element element = (Element) node;
			String prefix=element.getNamespacePrefix().equals("")?"":element.getNamespacePrefix()+ ":";
			String openElem = align + "<" + prefix + element.getName();
			sb.append(openElem); // Begin start tag
			boolean first=true;
			if(element.getParent()==null 
			        || !element.getNamespace().equals(element.getParent().getNamespace())){
			    sb.append(" "+element.getNamespace().asXML());
			    first=false;
			}
			if(element.attributeCount()>0){
				StringBuffer attributeAlign=new StringBuffer(align);
				int attrAligntCount= openElem.length() - align.length() +1;
				for(int i=0;i<attrAligntCount;i++){
					attributeAlign.append(' ');
				}
				Iterator<Attribute> iterator = element.attributeIterator(); // Get attributes
				while (iterator.hasNext()) {
					Attribute attr = (Attribute) iterator.next();
					if(first){
						sb.append(" " + attr.getName() + "='" + transform(attr.getValue()) + "'");
						first=false;
					}
					else{
						sb.append("\n"+attributeAlign.toString() + attr.getName() + "='" + transform(attr.getValue()) + "'");
					}
				}
			}
			List childs = element.elements();
			boolean hasChilds=childs.size()>0;
			if(hasChilds){
				sb.append(">\n");
				String newalign = align + "    ";
				Iterator<Element> childIterator = childs.iterator();
				while (childIterator.hasNext())// till not null
				{
					write(childIterator.next(), newalign, sb); // log Output node
				}
				sb.append(align + "</" + prefix + element.getName() + ">\n");// Log
			}else{
				String text = element.getTextTrim();
				if ((text != null) && text.length() > 0) {
				    if(element.getName().equals("password")){
				        text="********************";
				    }
					sb.append(">" + transform(text));
					sb.append("</" + element.getName() + ">\n");// Log
				}
				else{
					sb.append("/>\n");
				}
			}

			
																		// tag
		} else if (nodeType == Node.TEXT_NODE) {
			String text = ((Text) node).getText().trim();
			if ((text != null) && text.length() > 0) {
				sb.append(align + transform(text));
			}
		} else if (nodeType == Node.PROCESSING_INSTRUCTION_NODE) {
			// logger.info(align + "<?" + ((ProcessingInstruction)
			// node).getTarget() + " "
			// + ((ProcessingInstruction) node).getText() + "?>");
			sb.append(align + node.asXML());
		} else if (nodeType == Node.ENTITY_REFERENCE_NODE) {
			sb.append(align + "&" + node.getNodeTypeName() + ";");
		} else if (nodeType == Node.CDATA_SECTION_NODE) {
			sb.append(align + "<" + "![CDATA[" + ((CDATA) node).getText()
					+ "]]" + ">");
		} else if (nodeType == Node.COMMENT_NODE) {
			sb.append(align + "<!--" + ((Comment) node).getText() + "-->");
		} else {
			logger.error("Ignoring node: " + node.getClass().getName());
		}
	}

	String transform(String s) {
		StringBuffer sb = new StringBuffer();
		char[] c = s.toCharArray();
		for (int i = 0; i < c.length; i++) {
			switch (c[i]) {
			default:
				sb.append(c[i]);
				break;
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			case '&':
				sb.append("&amp;");
				break;
			case '\"':
				sb.append("&quot;");
				break;
			case '\'':
				sb.append("&apos;");
				break;
			}
		}
		return sb.toString();
	}

    public boolean isVerboseEnable() {
        return logger.isDebugEnabled();
    }
}
