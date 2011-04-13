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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import ru.korusconsulting.connector.base.ZConst;
import ru.korusconsulting.connector.exceptions.SoapRequestException;
import ru.korusconsulting.connector.funambol.ContactUtils;

import com.funambol.common.pim.contact.Contact;
import com.funambol.framework.engine.SyncItemKey;
import com.funambol.framework.tools.merge.MergeResult;

public class ContactManager extends Manager<Contact> {

    private static final String A_CONTACT_ID = "id";

    public void requestItemsForSync() throws IOException, SoapRequestException {
        itemsResponse = zimbraPort.requestAllContactIds();
    }

    public SyncItemKey[] getTwins(Contact contact) {
        ArrayList<String> twins = new ArrayList<String>();
        String firstName = ContactUtils.getFirstName(contact);
        String lastName = ContactUtils.getLastName(contact);
        String email = ContactUtils.getEmail(contact);
        String company = ContactUtils.getCompany(contact);

        //I think for more perfomance We can use a hashtable with hashfunction as Last Name
        for (Iterator iterator = itemsResponse.elementIterator(); iterator.hasNext();) {
            Element cn = (Element) iterator.next();
            String id = cn.attributeValue(A_CONTACT_ID);
            Properties prop = ContactUtils.toProperties(cn);
            //            if(prop.getProperty("firstName", "").equals(firstName))
            boolean twin = true;
            if (!(StringUtils.isBlank(firstName) && StringUtils.isBlank(lastName) && StringUtils.isBlank(email))) {
                if (!StringUtils.isBlank(firstName)) {
                    twin = twin
                            & firstName.equalsIgnoreCase(prop.getProperty(ContactUtils.FIRST_NAME));
                }
                if (!StringUtils.isBlank(lastName)) {
                    twin = twin
                            & lastName.equalsIgnoreCase(prop.getProperty(ContactUtils.LAST_NAME));
                }
                if (!StringUtils.isBlank(email)) {
                    twin = twin | email.equalsIgnoreCase(prop.getProperty(ContactUtils.EMAIL));
                }
            } else {
                if (!StringUtils.isBlank(company)) {
                    twin = twin & company.equalsIgnoreCase(prop.getProperty(ContactUtils.COMPANY));
                } else {
                    twin = false;
                    logger.warn("Contact hasn't any data for twins search");
                }
            }
            if (twin) {
                twins.add(id);
            }
        }
        return listAsKeyArray(twins);
    }

    @Override
    public String addItem(Contact contact) throws IOException, SoapRequestException {
        Element createdContacts = zimbraPort.requestCreateContact(contact);
        Element conEl = createdContacts.element(ZConst.E_CN);
        if (conEl == null)
            return null;
        String key = conEl.attribute(A_CONTACT_ID).getValue();
        return key;
    }

    @Override
    public Contact getItem(String key, Object... args) throws IOException,
            SoapRequestException {
        if (!isExist(key))
            return null;
        boolean syncPhoto = (Boolean) args[0];
        Contact contact = zimbraPort.requestContactById(key, syncPhoto);
        return contact;
    }

    @Override
    public boolean mergeItems(String key, Contact clientContact) throws IOException,
            SoapRequestException {
        Contact serverContact = zimbraPort.requestContactById(key, true);
        MergeResult mergeResult = clientContact.merge(serverContact);
        if (mergeResult.isSetBRequired()) {
            updItem(key, serverContact);
        }
        return mergeResult.isSetARequired();
    }

    @Override
    public String updItem(String key, Contact contact) throws IOException,
            SoapRequestException {
        String oldFolderId=null;
        String oldTags = null;
        List<Element> items=itemsResponse.elements();
        for (int i=0;i<items.size(); i++) {
            Element cn = (Element) items.get(i);
            if(cn.attributeValue(A_CONTACT_ID).equals(key)){
                oldFolderId=cn.attributeValue(ZConst.A_FOLDER);
                oldTags = cn.attributeValue(ZConst.A_TAG);
            }
        }
        if (oldFolderId==null) {
            // can't find contact with key 
            // we have to create Contact
            key = addItem(contact);
            contact.setUid(key);
        } else {
            
            zimbraPort.requestModifyContact(contact, oldFolderId, oldTags);
        }
        return key;
    }

    @Override
    public void delItem(String key, boolean soft) throws IOException,
            SoapRequestException {
        zimbraPort.requestDeleteContact(key, soft);
    }

    public String extractKeyFromElement(Element element) {
        String id = element.attributeValue(A_CONTACT_ID);
        return id;
    }

}
