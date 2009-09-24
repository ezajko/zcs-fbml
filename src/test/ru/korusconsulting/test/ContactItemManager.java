package ru.korusconsulting.test;

import ru.korusconsulting.connector.funambol.ContactUtils;

import com.funambol.common.pim.contact.Contact;
import com.funambol.common.pim.converter.ConverterException;
import com.funambol.syncclient.spds.engine.SyncItem;
import com.funambol.syncclient.spds.engine.SyncItemImpl;
import com.funambol.syncclient.spds.engine.SyncItemProperty;

public class ContactItemManager extends SyncItemManager {
    public ContactItemManager(String name) {
        super(name);
    }

    public void addContact(Contact c) throws ConverterException {
        String key = KeyGenerator.getKey();
        SyncItemImpl item = new SyncItemImpl(syncSource, key);
        c.setUid(key);
        String type = syncSource.getType();
        String version = syncSource.getVersion();
        item.setProperty(new SyncItemProperty(SyncItemImpl.PROPERTY_TYPE, type));
        byte[] content = ContactUtils.convertTo(type, version, c, null, "UTF-8");
        item.setProperty(new SyncItemProperty(SyncItemImpl.PROPERTY_BINARY_CONTENT,
                                              content));
        add(item);
    }

    public SyncItem remove(Contact c) {
        return remove(c.getUid());
    }

    public Contact update(Contact c) throws ConverterException {
        SyncItem item = get(c.getUid());
        String type = syncSource.getType();
        String version = syncSource.getVersion();
        byte[] content = ContactUtils.convertTo(type, version, c, null, "UTF-8");
        item.setProperty(new SyncItemProperty(SyncItemImpl.PROPERTY_BINARY_CONTENT,
                                              content));
        item = super.update(item);
        return c;
    }

}
