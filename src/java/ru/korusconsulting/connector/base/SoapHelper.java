/*
 * Copyright (C) 2008 KorusConsulting Author: Roman Bliznets
 * <RBliznets@korusconsulting.ru> This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA
 * 02139, USA.
 */
package ru.korusconsulting.connector.base;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.XPath;


public class SoapHelper {
    public static final String SOAP12_ENV_NS = "http://www.w3.org/2003/05/soap-envelope";//soap1.2
    private XPath findHeader;
    private XPath findBody;
    private XPath findTrashFolder;
    private static SoapHelper instance;
    
    private SoapHelper(DocumentFactory df) {
        Document document = df.createDocument();
        findHeader = document.createXPath("/soap:Envelope/soap:Header");
        findBody = document.createXPath("/soap:Envelope/soap:Body");
        findTrashFolder = document.createXPath("//zimbra:folder[@name='Trash']");
        
    }
    
    public static SoapHelper getInstance(DocumentFactory df){
        if(instance==null){
            instance=new SoapHelper(df);
        }
        return instance;
        
    } 

    public Element getHeader(Document doc) {
        return (Element) findHeader.selectSingleNode(doc);
    }

    public Element getBody(Document doc) {
        return (Element) findBody.selectSingleNode(doc);
    }

    public String getTrashFolderId(Document doc) {
        Element trashFolder = (Element) findTrashFolder.selectSingleNode(getContext(doc));
        return trashFolder.attributeValue("id");
    }
    
    public Element getRootFolder(Document doc) {
        return getContext(doc).element("refresh").element("folder");
    }
    
    public Element getRootTags(Document doc) {
        return getContext(doc).element("refresh").element("tags");
    }

    public Element getContext(Document doc) {
        return ((Element) getHeader(doc).element("context"));
    }
    
    

    @SuppressWarnings("unchecked")
    public List<Element> getContextContent(Document doc) {
        return getContext(doc).elements();
    }

    public Document createZimbraCall(String function, String namespace,
            DocumentFactory documentFactory, ConnectionContext ccontext) {
        Document request = createZimbraDoc(documentFactory, ccontext);
        Element func = documentFactory.createElement(function, namespace);
        getBody(request).add(func);
        return request;

    }
    
    public Document createZimbraDoc(DocumentFactory documentFactory, ConnectionContext ccontext) {
        Document request = documentFactory.createDocument("UTF-8");
        Element envelope = documentFactory.createElement("soap:Envelope", SOAP12_ENV_NS);
        {
            Element header = documentFactory.createElement("soap:Header", SOAP12_ENV_NS);
            {
                Element context = ccontext.fillContext(documentFactory);
                header.add(context);
            }
            envelope.add(header);
            Element body = documentFactory.createElement("soap:Body", SOAP12_ENV_NS);
            envelope.add(body);
        }
        request.add(envelope);
        return request;

    }

}
