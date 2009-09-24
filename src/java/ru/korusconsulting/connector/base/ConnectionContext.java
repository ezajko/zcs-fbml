/*
 * Copyright (C) 2008 KorusConsulting
 * 
 * Author: Roman Bliznets <RBliznets@korusconsulting.ru>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package ru.korusconsulting.connector.base;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;


public class ConnectionContext {
	private String sessionId;
	private String changeId;
	private String authToken;
	private String lifetime;
	
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getChangeId() {
		return changeId;
	}
	public void setChangeId(String changeId) {
		this.changeId = changeId;
	}
	public String getAuthToken() {
		return authToken;
	}
	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}
	public String getLifetime() {
		return lifetime;
	}
	public void setLifetime(String lifetime) {
		this.lifetime = lifetime;
	}
	
	
	
    /*
     * 
       <context xmlns="urn:zimbra">
          <authToken>...</authToken>
          [<nosession/>]
          [<sessionId id="{returned-from-server-in-last-response}"/>]
          [<account by="name|id">{account-name-or-id}</account>]
          [<change token="{change-id}" [type="mod|new"]/>]
          [<notify seq="{highest_notification_received}">]
          [<nonotify/>]
          [<targetServer>{proxy-target-server-id}</targetServer>]
          [<userAgent name="{client-name}" [version="{client-version}"]/>]
       </context>
     */
    /**
     * Fill context with ConnectionContext information   
     * @param context - context element in the request Document
     */
    public Element fillContext(DocumentFactory documentFactory) {
        Element context = documentFactory.createElement("context", "urn:zimbra");
        if (this.getAuthToken() != null) {
            Element authToken = documentFactory.createElement("authToken", context.getNamespaceURI());
            {
                authToken.setText(this.getAuthToken());
            }
            context.add(authToken);
        }
        //If we enable change id then we should modify item and move item to folder in the diffeerent 
        //request other way we catch MODIFY_CONFLICT fault
        if (this.getChangeId() != null) {
            Element change = documentFactory.createElement("change", context.getNamespaceURI());
            {
                change.addAttribute("token", this.getChangeId());
            }
            context.add(change);
        }
        if (this.getSessionId() != null) {
            Element sessionId = documentFactory.createElement("session", context.getNamespaceURI());
            {
                sessionId.addAttribute("id", this.getSessionId());
            }
            context.add(sessionId);
        } else {
	    Element session = documentFactory.createElement("session", context.getNamespaceURI());
	    context.add(session);
	}
        if (FunambolConnector.getInstance() != null) {// always true
            Element userAgent = documentFactory.createElement("userAgent", context.getNamespaceURI());
            {
                userAgent.addAttribute("name", FunambolConnector.NAME);
                userAgent.addAttribute("version", FunambolConnector.getInstance().getVersion());
            }
            context.add(userAgent);
        }
        return context;
    }
    
    
    
    public void processContext(Element context){
        Element change = context.element("change");
        if (change != null) {
            this.setChangeId(change.attribute("token").getValue());
        }
        Element session = context.element("sessionId");
        if (session != null) {
            this.setSessionId(session.attribute("id").getValue());
        }
    }
    
    public void clear(){
        setAuthToken(null);
        setSessionId(null);
        setChangeId(null);
        setLifetime(null);
    }
}
