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
package ru.korusconsulting.connector.manager;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Element;

import ru.korusconsulting.connector.base.ZimbraPort;
import ru.korusconsulting.connector.exceptions.SoapRequestException;

import com.funambol.common.pim.common.Property;
import com.funambol.framework.engine.SyncItemKey;
import com.funambol.framework.engine.source.SyncSourceException;
import com.funambol.framework.logging.FunambolLogger;
import com.funambol.framework.logging.FunambolLoggerFactory;

public abstract class Manager<T> {

    protected ZimbraPort zimbraPort;
    protected FunambolLogger logger;
    
    protected Element itemsResponse;
    protected ArrayList<String> allItems = new ArrayList<String>();
    protected ArrayList<String> newItems = new ArrayList<String>();
    protected ArrayList<String> updItems = new ArrayList<String>();
    protected ArrayList<String> delItems = new ArrayList<String>();
    protected boolean prepared = false;

    public Manager() {
        logger = FunambolLoggerFactory.getLogger("funambol.zimbra");
    }

    public abstract T getItem(String key, Object... args) throws IOException, SoapRequestException;
    

    public abstract void requestItemsForSync() throws IOException, SoapRequestException;
    
    public void removeAllItems() throws IOException, SoapRequestException{
        zimbraPort.requestRemoveAllItems(itemsResponse, "id");
        itemsResponse.elements().retainAll(new ArrayList<Element>());//remove all
    } 

    public void determineItemsState(Timestamp since) {
        if (prepared)
            return;
        prepared = true;
        for (Iterator iterator = itemsResponse.elementIterator(); iterator.hasNext();) {
            Element cal = (Element) iterator.next();
            String id = extractKeyFromElement(cal);
            if (cal.attributeValue("l").equals(zimbraPort.getTrashFolderId())) {
                delItems.add(id);
            } else {
                long crtTime = Long.parseLong(cal.attributeValue("d"));
                long modTime = Long.parseLong(cal.attributeValue("md")) * 1000;

                allItems.add(id);
                if(since!=null){
                    if (crtTime > since.getTime()) {
                        newItems.add(id);
                    } else if (modTime > since.getTime()) {
                        updItems.add(id);
                    }
                }
            }
        }
    }

    public abstract String extractKeyFromElement(Element cn) ;

    public abstract SyncItemKey[] getTwins(T item) ;
    
    public abstract String updItem(String key, T item) throws IOException, SoapRequestException, SyncSourceException;
    
    public abstract String addItem(T calendar) throws IOException, SoapRequestException, SyncSourceException;
    
    public void delItem(String key, boolean soft) throws IOException, SoapRequestException{
        zimbraPort.requestDeleteItem(key, soft);
    }
    
    public abstract boolean mergeItems(String key, T clientItem) throws IOException, SoapRequestException, SyncSourceException ;

    public SyncItemKey[] getDeletedItems() {
        return listAsKeyArray(delItems);
    }

    public SyncItemKey[] getUpdatedItems() {
        return listAsKeyArray(updItems);
    }

    public SyncItemKey[] getCreatedItems() {
        return listAsKeyArray(newItems);
    }

    public SyncItemKey[] getAllItems() {
        return listAsKeyArray(allItems);
    }

    public boolean isExist(String key) {
        return allItems.indexOf(key) != -1;
    }

    

    protected static SyncItemKey[] listAsKeyArray(ArrayList<String> list) {
        SyncItemKey[] keys = new SyncItemKey[list.size()];
        Iterator<String> iterator = list.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            String id = iterator.next();
            keys[i++] = new SyncItemKey(id);
        }
        return keys;
    }
    
    protected static String listAsString(ArrayList<String> list) {
        StringBuilder sb=new StringBuilder();
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String id = iterator.next();
            sb.append(id);
            if(iterator.hasNext())
                sb.append(',');
        }
        return sb.toString();
    }

    public ZimbraPort getZimbraPort() {
        return zimbraPort;
    }

    public void setZimbraPort(ZimbraPort zimbraPort) {
        this.zimbraPort = zimbraPort;
    }

    public static String getValue(Property prop) {
        if (prop == null)
            return null;
        else{
            String v=prop.getPropertyValueAsString();
            return v==null?null:v.equals("")?null:v;
        }
    }
}
