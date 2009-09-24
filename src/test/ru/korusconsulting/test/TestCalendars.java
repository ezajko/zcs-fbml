package ru.korusconsulting.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.TimeZone;

import org.dom4j.Document;
import org.dom4j.DocumentException;

import junit.framework.Assert;
import ru.korusconsulting.connector.funambol.CalendarUtils;
import ru.korusconsulting.connector.funambol.ContactUtils;
import ru.korusconsulting.connector.funambol.PhoneDependedConverter;

import com.funambol.common.pim.calendar.Calendar;
import com.funambol.common.pim.common.Property;
import com.funambol.common.pim.contact.Contact;
import com.funambol.common.pim.contact.Photo;
import com.funambol.common.pim.converter.ConverterException;
import com.funambol.framework.tools.merge.MergeResult;

public class TestCalendars {
    public static HashMap<String, Calendar> calendars = new HashMap<String, Calendar>();

    static {
        org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
        File contactsDir = new File("test/appt");
        File[] listFiles = contactsDir.listFiles();
        for (File file : listFiles) {
            String name = file.getName();
            int lastIndexOf = name.lastIndexOf('.');
            if (lastIndexOf == -1) {
                continue;
            }
            try {
                String extention = name.substring(lastIndexOf + 1);
                if ("xml".equalsIgnoreCase(extention)) {
                    Document appt = reader.read(file);
                    Calendar c = CalendarUtils.getInstance()
                                              .asCalendar(appt.getRootElement(),
                                                          true,
                                                          TimeZone.getTimeZone("Europe/Moscow"));
                    calendars.put(name.substring(0, lastIndexOf), c);
                } else if ("scal".equalsIgnoreCase(extention)) {
                    byte[] content = getContent(file);
                    CalendarUtils.convertFrom(PhoneDependedConverter.SIFE_TYPE,
                                              null,
                                              content,
                                              TimeZone.getTimeZone("Europe/Moscow"),
                                              "UTF-8");
                } else if ("cal".equalsIgnoreCase(extention)) {
                    byte[] content = getContent(file);
                    CalendarUtils.convertFrom(PhoneDependedConverter.VCAL_TYPE,
                                              "2.0",
                                              content,
                                              TimeZone.getTimeZone("Europe/Moscow"),
                                              "UTF-8");
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

        }
    }

    private static byte[] getContent(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream((int) Math.min(file.length(),
                                                                              Integer.MAX_VALUE));
        byte[] buffer = new byte[1024];
        int index = -1;
        while ((index = fis.read(buffer, 0, buffer.length)) != -1) {
            baos.write(buffer, 0, index);
        }
        fis.close();
        return baos.toByteArray();
    }

    public static Calendar get() {
        if (calendars.size() == 0)
            return null;

        int max = calendars.size();
        double random = Math.abs(Math.sinh(Math.exp(System.currentTimeMillis())));
        int cur = (int) (random * max);
        cur = Math.min(cur, max - 1);
        Iterator<Calendar> iter = calendars.values().iterator();
        for (int i = 0; i < cur; i++) {
            iter.next();
        }
        return iter.next();
    }

    public static Calendar get(String name) {
        return calendars.get(name);
    }

    public static boolean equals(Calendar c1, Calendar c2) throws Throwable {
        try {
            boolean compare = equals(c1.getVersion(), c2.getVersion());
            if (!compare) {
                System.out.println("Calendars Version not equals: ");
                System.out.println("c1.getVersion()=" + c1.getVersion().getPropertyValueAsString());
                System.out.println("c2.getVersion()=" + c2.getVersion().getPropertyValueAsString());
                return false;
            }

            // compare = equals(c1.merge(otherCalendar), c2.getVersion());
            // if (!compare) {
            // System.out.println("Calendars Version not equals: ");
            // System.out.println("c1.getVersion()=" +
            // c1.getVersion().getPropertyValueAsString());
            // System.out.println("c2.getVersion()=" +
            // c2.getVersion().getPropertyValueAsString());
            // return false;
            // }

            MergeResult mr = c1.merge(c2);
            compare = !(mr.isSetARequired() || mr.isSetBRequired());

            if (!compare) {
                System.out.println("Calendars not equals:");
                byte[] b1 = CalendarUtils.convertTo(PhoneDependedConverter.SIFE_TYPE,
                                                    null,
                                                    c1,
                                                    TimeZone.getTimeZone("Europe/Moscow"),
                                                    "UTF-8");
                byte[] b2 = CalendarUtils.convertTo(PhoneDependedConverter.SIFE_TYPE,
                                                    null,
                                                    c2,
                                                    TimeZone.getTimeZone("Europe/Moscow"),
                                                    "UTF-8");
                System.out.println("c1:\n" + new String(b1));
                System.out.println("c2:\n" + new String(b2));
            }
            return compare;
        } catch (ConverterException e) {
            throw new Throwable(e);
        }
    }

    public static boolean equals(Property p1, Property p2) {
        if (p1.getPropertyValue() != null) {
            return p1.getPropertyValue().equals(p2.getPropertyValue());
        } else {
            return p2 == null;
        }

    }

    public static void assertEquals(ArrayList<Calendar> expected, Calendar... contacts)
            throws Throwable {
        Assert.assertTrue("Cann't find any contacts in the zimbra", expected.size() > 0);
        Assert.assertTrue("Too many contacts in the zimbra", expected.size() < contacts.length + 1);
        Assert.assertTrue("Too small contacts in the zimbra", expected.size() == contacts.length);
        boolean rows[] = new boolean[expected.size()];
        boolean columns[] = new boolean[expected.size()];
        boolean eqGlobal = true;
        boolean eq;
        for (int i = 0; i < expected.size(); i++) {
            eq = false;
            for (int j = 0; i < expected.size(); i++) {
                if (rows[i] || columns[j]) {
                    eq = true;
                    continue;
                }
                eq = equals(contacts[i], expected.get(j));
                if (eq) {
                    // contacts[i].setUid(expected.get(j).getUid());
                    rows[i] = true;
                    columns[j] = true;
                    break;
                }
            }
            eqGlobal &= eq;

        }
        Assert.assertTrue("Contact in the zimbra not equals contact that was loaded", eqGlobal);
    }
}
