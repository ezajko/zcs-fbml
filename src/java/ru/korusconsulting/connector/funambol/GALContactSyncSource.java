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
package ru.korusconsulting.connector.funambol;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;

import ru.korusconsulting.connector.exceptions.SoapRequestException;

import com.funambol.common.pim.contact.Contact;
import com.funambol.framework.engine.SyncItem;
import com.funambol.framework.engine.SyncItemImpl;
import com.funambol.framework.engine.SyncItemKey;
import com.funambol.framework.engine.SyncItemState;
import com.funambol.framework.engine.source.SyncContext;
import com.funambol.framework.engine.source.SyncSourceException;

public class GALContactSyncSource extends ZimbraSyncSource {
    transient private Element contactsResponse;
    
    public GALContactSyncSource() {
    }

    public void beginSync(SyncContext context) throws SyncSourceException {
        super.beginSync(context);
    }

    @SuppressWarnings("unchecked")
    public SyncItemKey[] getAllSyncItemKeys() throws SyncSourceException {
        logger.info("In getAllSyncItemKeys()...");
        try {
            List<Element> contacts = contactsResponse.elements();
            return listAsKeyArray(contacts);
        } catch (Throwable e) {
            logger.error("", e);
            throw new SyncSourceException(e);
        }
    }
    
    public SyncItemKey[] getNewSyncItemKeys(Timestamp since, Timestamp until) throws SyncSourceException {
        logger.info("In getNewSyncItemKeys()...");
        try {
            long start, end;
            start = since.getTime();
            end = until.getTime();
            SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddhhmmss");
            ArrayList<SyncItemKey> keys=new ArrayList<SyncItemKey>();
            int index;
            for (Iterator iterator = contactsResponse.elementIterator(); iterator.hasNext();) {
                Element cn = (Element) iterator.next();
                Element crEl = (Element)cn.selectSingleNode("zimbraAccount:a[@n='createTimeStamp']");
                if(crEl==null)continue;
                String created = crEl.getText();
                index=created.lastIndexOf(".0Z");
                if(index!=-1)
                    created=created.substring(0, index);
                Date createdDate=sdf.parse(created);
                if(createdDate.getTime()>start&&createdDate.getTime()<end){
                    keys.add(new SyncItemKey(cn.attributeValue("id")));
                }
            }
            return keys.toArray(new SyncItemKey[keys.size()]);
        } catch (Throwable e) {
            logger.error("", e);
            throw new SyncSourceException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public SyncItemKey[] getDeletedSyncItemKeys(Timestamp since, Timestamp until) throws SyncSourceException {
        logger.debug("In getDeletedSyncItemKeys()...");
        return new SyncItemKey[0];
    }
    
    public SyncItemKey[] getUpdatedSyncItemKeys(Timestamp since, Timestamp until) throws SyncSourceException {
        logger.debug("In getUpdatedSyncItemKeys()...");
        try {
            long start, end;
            start = (long) (since.getTime() / 1000);
            end = (long) (until.getTime() / 1000);
            SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddhhmmss");
            ArrayList<SyncItemKey> keys=new ArrayList<SyncItemKey>();
            int index;
            for (Iterator iterator = contactsResponse.elementIterator(); iterator.hasNext();) {
                Element cn = (Element) iterator.next();
                Element crEl = (Element)cn.selectSingleNode("zimbraAccount:a[@n='createTimeStamp']");
                Element mdEl = (Element)cn.selectSingleNode("zimbraAccount:a[@n='modifyTimeStamp']");
                if(crEl==null||mdEl==null)continue;
                
                String created = crEl.getText();
                String modified = mdEl.getText();
                index=created.lastIndexOf(".0Z");
                if(index!=-1)
                    created=created.substring(0, index);
                index=modified.lastIndexOf(".0Z");
                if(index!=-1)
                    modified=modified.substring(0, index);
                Date createdDate=sdf.parse(created);
                Date modifiedDate=sdf.parse(created);
                if(!(createdDate.getTime()>start&&createdDate.getTime()<end)&&
                        (modifiedDate.getTime()>start&&modifiedDate.getTime()<end)){
                    keys.add(new SyncItemKey(cn.attributeValue("id")));
                }
            }
            return keys.toArray(new SyncItemKey[keys.size()]);
        } catch (Throwable e) {
            logger.error("", e);
            throw new SyncSourceException(e);
        }
    }

    public SyncItem getSyncItemFromId(SyncItemKey itemKey) throws SyncSourceException {
        logger.debug("In getSyncItemFromId()...");
        try {
            Element cn=(Element) contactsResponse.selectSingleNode("zimbraAccount:cn[@id='"+itemKey.getKeyAsString()+"']");
            Contact contact=ContactUtils.asContact(cn);
            SyncItem item=new SyncItemImpl(this, itemKey, SyncItemState.UNKNOWN);
            PhoneDependedConverter.getInstance().setContent(item, contact, this);
            return item;
        } catch (Throwable e) {
            logger.error("", e);
            throw new SyncSourceException(e);
        }
    }

    /**
     * Called by the engine to get the SyncItemKeys of the twins of the given
     * item. Each source implementation can interpret this as desired (i.e.,
     * comparing all fields).
     */
    public SyncItemKey[] getSyncItemKeysFromTwin(SyncItem item) throws SyncSourceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Try to find the twin for '" + item.getKey().getKeyAsString() + "':"
                    + item);
        }
        Contact contact;
        try {
            contact = PhoneDependedConverter.getInstance().getContact(item, this);
            String firstName = ContactUtils.getFirstName(contact);
            String lastName = ContactUtils.getLastName(contact);
            String email = ContactUtils.getEmail(contact);
            String company = ContactUtils.getCompany(contact);
            
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("zimbraMail:cn[");
            if(!(firstName==null&&lastName==null&&email==null)){
                if (firstName != null) {
                    stringBuilder.append("zimbraMail:a[@n='firstName']='" + firstName + "'");
                    if (lastName != null || email != null) {
                        stringBuilder.append(" and ");
                    }
                }
                if (lastName != null) {
                    stringBuilder.append("zimbraMail:a[@n='lastName']='" + lastName + "'");
                    if (email != null) {
                        stringBuilder.append(" or ");
                    }
                }
                if (email != null) {
                    stringBuilder.append("zimbraMail:a[@n='email']='" + email + "'");
                }
            }else{
                if(company!=null){
                    stringBuilder.append("zimbraMail:a[@n='company']='" + company + "'");
                }
                else{
                    stringBuilder=null;//cann't find any twins
                    if (logger.isDebugEnabled()) {
                        logger.debug("Contact hasn't any data for twins search");
                    }
                }
            }
            //company
            SyncItemKey[] twins=null;
            if(stringBuilder!=null){
                stringBuilder.append("]");
                if (logger.isDebugEnabled()) {
                    logger.debug("The xpath filter is '" + stringBuilder.toString() + "':");
                }
                List<Element> contacts = contactsResponse.selectNodes(stringBuilder.toString());
                twins = listAsKeyArray(contacts);
            }
            if (logger.isDebugEnabled()) {
                if (twins!=null&&twins.length > 0)
                    logger.debug("The twin's is zimbra contact with id='"
                            + item.getKey().getKeyAsString() + "'=" + toString(twins));
                else
                    logger.debug("Cann't find any twins for '" + item.getKey().getKeyAsString()
                            + "'");
            }
            return twins;
        } catch (Throwable e) {
            logger.error("", e);
            throw new SyncSourceException(e);
        }
    }


    public SyncItem addSyncItem(SyncItem item) throws SyncSourceException {
        logger.debug("In addSyncItem()...");
//        Contact contact;
//        try {
//            contact=PhoneDependedConvertor.getContact(item);
//            Element createdContacts=zimbraPort.requestCreateContact(contact);
            item.setState(SyncItemState.SYNCHRONIZED);
//            item.getKey().setKeyValue(createdContacts.element("cn").attribute("id").getValue());
//        } catch (Throwable e) {
//            logger.error("", e);
//            throw new SyncSourceException(e);
//        }
        return item;
    }
    
    public void removeSyncItem(SyncItemKey itemKey, Timestamp arg1, boolean soft) throws SyncSourceException {
        logger.debug("In removeSyncItem()...");
//        try {
//            zimbraPort.requestDeleteContact(itemKey.getKeyAsString(), soft);
//        } catch (Throwable e) {
//            logger.error("", e);
//            throw new SyncSourceException(e);
//        }
    }

    public SyncItem updateSyncItem(SyncItem item) throws SyncSourceException {
        //_context.getConflictResolution() == SyncContext.CONFLICT_RESOLUTION_CLIENT_WINS
        logger.debug("In updateSyncItem("+item+")");
        item=getSyncItemFromId(item.getKey());
        return item;
    }
    
    public SyncItemKey[] listAsKeyArray(List<Element> list) {
        SyncItemKey[] keys = new SyncItemKey[list.size()];
        Iterator<Element> iterator = list.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            String id = iterator.next().attribute("id").getValue();
            keys[i++] = new SyncItemKey(id);
        }
        return keys;
    }
    
    public void endSync() throws SyncSourceException {
        contactsResponse = null;
        super.endSync();
    }

    @Override
    protected void removeAllSyncItems() throws SyncSourceException {
        //do nothing
    }

    @Override
    protected void requestAllItems() throws IOException, SoapRequestException {
        contactsResponse = zimbraPort.requestGALContactIds();
        
    }
}
