package ru.korusconsulting.test;

import ru.korusconsulting.connector.funambol.CalendarUtils;
import ru.korusconsulting.connector.funambol.ContactUtils;

import com.funambol.common.pim.calendar.Calendar;
import com.funambol.common.pim.common.Property;
import com.funambol.common.pim.contact.Contact;
import com.funambol.common.pim.converter.ConverterException;
import com.funambol.syncclient.spds.engine.SyncItem;
import com.funambol.syncclient.spds.engine.SyncItemImpl;
import com.funambol.syncclient.spds.engine.SyncItemProperty;

public class CalendarItemManager extends SyncItemManager {
    public CalendarItemManager(String name) {
        super(name);
    }

    public String addCalendar(Calendar c) throws ConverterException {
        String key = KeyGenerator.getKey();
        SyncItemImpl item = new SyncItemImpl(syncSource, key);
        String type = syncSource.getType();
        String version = syncSource.getVersion();
        item.setProperty(new SyncItemProperty(SyncItemImpl.PROPERTY_TYPE, type));
        byte[] content = CalendarUtils.convertTo(type, version, c, null, "UTF-8");
        item.setProperty(new SyncItemProperty(SyncItemImpl.PROPERTY_BINARY_CONTENT,
                                              content));
        add(item);
        return key;
    }

    public Calendar update(String key, Calendar c) throws ConverterException {
        SyncItem item = get(key);
        String type = syncSource.getType();
        String version = syncSource.getVersion();
        byte[] content = CalendarUtils.convertTo(type, version, c, null, "UTF-8");
        item.setProperty(new SyncItemProperty(SyncItemImpl.PROPERTY_BINARY_CONTENT,
                                              content));
        item = super.update(item);
        return c;
    }

}
