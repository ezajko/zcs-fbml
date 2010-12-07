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
package ru.korusconsulting.connector.funambol;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.TimeZone;

import ru.korusconsulting.connector.exceptions.ConversionException;

import com.funambol.common.pim.calendar.Calendar;
import com.funambol.common.pim.contact.Contact;
import com.funambol.common.pim.converter.ContactToVcard;
import com.funambol.common.pim.converter.ConverterException;
import com.funambol.common.pim.note.Note;
import com.funambol.common.pim.vcard.VcardParser;
import com.funambol.framework.core.DataStore;
import com.funambol.framework.engine.SyncItem;
import com.funambol.framework.logging.FunambolLogger;
import com.funambol.framework.logging.FunambolLoggerFactory;

public class PhoneDependedConverter {

    public static final String SIFE_TYPE = "text/x-s4j-sife";
    public static final String SIFC_TYPE = "text/x-s4j-sifc";
    public static final String SIFT_TYPE = "text/x-s4j-sift";
    public static final String SIFN_TYPE = "text/x-s4j-sifn";

    public static final String VCARD_TYPE = "text/x-vcard";
    public static final String VCAL_TYPE = "text/x-vcalendar";
    public static final String ICAL_TYPE = "text/calendar";
    public static final String VNOTE_TYPE = "text/x-vnote";

    private static PhoneDependedConverter instance;

    private FunambolLogger logger;

    public PhoneDependedConverter() {
        logger = FunambolLoggerFactory.getLogger("funambol.zimbra.internal.PhoneDependedConverter");
    }

    public Contact getContact(SyncItem item, ZimbraSyncSource source) throws ConversionException {
        Contact contact;
        byte[] content = item.getContent();
        try {
            String charset = source.getPrincipal().getDevice().getCharset();
            if (charset == null)
                charset = "UTF-8";
            String deviceId = source.getPrincipal().getDeviceId();
            if (deviceId.contains("iphone")) {
                if (logger.isTraceEnabled())
                    logger.trace("sc-iphone fix vcard, before:\n" + new String(content));
                content = fixVcard(content);
                if (logger.isTraceEnabled())
                    logger.trace("sc-iphone fix vcard, fixed:\n" + new String(content));
            }
            contact = ContactUtils.convertFrom(item.getType(),
                                               null,
                                               content,
                                               source.getTimeZone(),
                                               charset);
        } catch (Throwable e) {
            throw new ConversionException("Error occur while convert syncItem Content into Contact\ncontent:\n<<"
                                                  + new String(item.getContent())
                                                  + ">>\nException trace:",
                                          e);
        }
        return contact;
    }

    /**
     * This function fix the iphone client bug iphone client sends wrong vcards
     * like card: BEGIN:VCARD VERSION:2.1 TEL;TYPE=CELL:78129999999
     * NOTE;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:=D0=9A=D0=B0=D1=82=D0=B0=D1=80=D0=B8=D0=BD=D0=BA=D0=B0
     * =0D=0A
     * N;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:;=D0=95=D0=BB=D0=B5=D0=BD=D0=B0;;;
     * END:VCARD
     * 
     * @param content
     * @return
     */
    public byte[] fixVcard(byte[] content) {
        String vcard = new String(content);
        StringTokenizer strTok = new StringTokenizer(vcard, "\r\n");
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        while (strTok.hasMoreTokens()) {
            String line = strTok.nextToken();
            if (line.startsWith("CATEGORIES") || line.startsWith("VERSION")
                    || line.startsWith("TITLE") || line.startsWith("NICKNAME")
                    || line.startsWith("EMAIL") || line.startsWith("FN") || line.startsWith("ORG")
                    || line.startsWith("BDAY") || line.startsWith("PHOTO")
                    || line.startsWith("ADR") || line.startsWith("UID") || line.startsWith("LABEL")
                    || line.startsWith("ROLE") || line.startsWith("TZ") || line.startsWith("LOGO")
                    || line.startsWith("NOTE") || line.startsWith("URL") || line.startsWith("N")
                    || line.startsWith("REV") || line.startsWith("TEL")
                    || line.startsWith("X-FUNAMBOL-FOLDER") || line.startsWith("<VCEND>")
                    || line.startsWith("<EXTENSION>") || line.startsWith("<IDENTIFIER>")
                    || line.startsWith("BEGIN:VCARD") || line.startsWith("END:VCARD")) {
                if (!first)
                    sb.append("\n");
            }
            sb.append(line);
            first = false;
        }
        return sb.toString().getBytes();
    }

    public Calendar getCalendar(SyncItem item, ZimbraSyncSource source) throws ConversionException {
        Calendar calendar;
        byte[] content = item.getContent();
        try {
            String charset = source.getPrincipal().getDevice().getCharset();
            if (charset == null)
                charset = "UTF-8";
            calendar = CalendarUtils.convertFrom(item.getType(),
                                                 null,
                                                 content,
                                                 source.getTimeZone(),
                                                 charset);
        } catch (Throwable e) {
            throw new ConversionException("Error occur while convert Calendar into syncItem Content\ncontent:\n<<"
                                                  + new String(item.getContent())
                                                  + ">>\nException trace:",
                                          e);
        }
        return calendar;
    }

    public Note getNote(SyncItem item, ZimbraSyncSource source) throws ConversionException {
        Note note;
        byte[] content = item.getContent();
        try {
            note = NoteUtils.convertFrom(item.getType(), null, content, null, null);
        } catch (Throwable e) {
            throw new ConversionException("Error occur while convert syncItem Content into Note\ncontent:\n<<"
                                                  + new String(item.getContent())
                                                  + ">>\nException trace:",
                                          e);
        }
        return note;
    }

    public void setContent(SyncItem item, Contact contact, ZimbraSyncSource source)
            throws ConversionException {
        int type = getPreferredItemType(source, CONTACT, RX);
        try {
            if (type == X_S4J_SIFC) {
                item.setType(SIFC_TYPE);
                item.setContent(ContactUtils.convertTo(SIFC_TYPE,
                                                       null,
                                                       contact,
                                                       source.getTimeZone(),
                                                       source.getCharset()));
            } else if (type == VCARD_VERSION_30) {
                item.setType(VCARD_TYPE);
                item.setContent(ContactUtils.convertTo(VCARD_TYPE,
                                                       null,
                                                       contact,
                                                       source.getTimeZone(),
                                                       source.getCharset()));
            } else {
                item.setType(VCARD_TYPE);
                item.setContent(ContactUtils.convertTo(VCARD_TYPE,
                                                       "2.1",
                                                       contact,
                                                       source.getTimeZone(),
                                                       source.getCharset()));
            }
        } catch (ConverterException e) {
            throw new ConversionException(e);
        }

    }

    public void setContent(SyncItem item, Note note, ZimbraSyncSource source)
            throws ConversionException {
        int type = getPreferredItemType(source, CONTACT, RX);
        try {
            if (type == X_S4J_SIFN) {
                item.setType(SIFC_TYPE);
                item.setContent(NoteUtils.convertTo(SIFC_TYPE,
                                                    null,
                                                    note,
                                                    source.getTimeZone(),
                                                    source.getCharset()));
            } else {
                item.setType(VNOTE_TYPE);
                item.setContent(NoteUtils.convertTo(VNOTE_TYPE,
                                                    null,
                                                    note,
                                                    source.getTimeZone(),
                                                    source.getCharset()));
            }
        } catch (ConverterException e) {
            throw new ConversionException(e);
        }

    }

    public void setContent(SyncItem item, Calendar calendar, ZimbraSyncSource source)
            throws ConversionException {
        boolean event = calendar.getEvent() != null;
        int type = getPreferredItemType(source, event ? EVENT : TODO, RX);
        try {
            if (type == X_S4J_SIFE) {
                item.setType(SIFE_TYPE);
                item.setContent(CalendarUtils.convertTo(SIFE_TYPE,
                                                        null,
                                                        calendar,
                                                        source.getTimeZone(),
                                                        source.getCharset()));
            } else if (type == X_S4J_SIFT) {
                item.setType(SIFT_TYPE);
                item.setContent(CalendarUtils.convertTo(SIFT_TYPE,
                                                        null,
                                                        calendar,
                                                        source.getTimeZone(),
                                                        source.getCharset()));
            } else if (type == VCALENDAR_VERSION_20) {
                item.setType(event ? VCAL_TYPE : ICAL_TYPE);
                item.setContent(CalendarUtils.convertTo(event ? VCAL_TYPE : ICAL_TYPE,
                                                        "2.0",
                                                        calendar,
                                                        source.getTimeZone(),
                                                        source.getCharset()));
            } else if (type == VCALENDAR_VERSION_10) {
                item.setType(event ? VCAL_TYPE : ICAL_TYPE);
                item.setContent(CalendarUtils.convertTo(event ? VCAL_TYPE : ICAL_TYPE,
                                                        "1.0",
                                                        calendar,
                                                        source.getTimeZone(),
                                                        source.getCharset()));
            } else {
            	if (event) { // EVENT_item = X_S4J_SIFE as default
                    item.setType(SIFE_TYPE);
                    item.setContent(CalendarUtils.convertTo(SIFE_TYPE,
                                                            null,
                                                            calendar,
                                                            source.getTimeZone(),
                                                            source.getCharset()));
                } else { // TODO_item = X_S4J_SIFT as default
                    item.setType(SIFT_TYPE);
                    item.setContent(CalendarUtils.convertTo(SIFT_TYPE,
                                                            null,
                                                            calendar,
                                                            source.getTimeZone(),
                                                            source.getCharset()));
                }
            }
        } catch (ConverterException e) {
            throw new ConversionException(e);
        }
    }

    /*
     * Code below was copied from Sogo Connector, with small modification
     */
    /*
     * Copyright (C) 2007 Inverse groupe conseil and Ludovic Marcotte
     * 
     * Author: Ludovic Marcotte <ludovic@inverse.ca>
     * 
     * This program is free software; you can redistribute it and/or modify it
     * under the terms of the GNU General Public License as published by the
     * Free Software Foundation; either version 2 of the License, or (at your
     * option) any later version.
     * 
     * This program is distributed in the hope that it will be useful, but
     * WITHOUT ANY WARRANTY; without even the implied warranty of
     * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
     * Public License for more details.
     * 
     * You should have received a copy of the GNU General Public License along
     * with this program; if not, write to the Free Software Foundation, Inc.,
     * 675 Mass Ave, Cambridge, MA 02139, USA.
     */
    public static final int CONTACT = 1;
    public static final int EVENT = 2;
    public static final int TODO = 3;
    private static final int NOTE = 4;

    public static final int VCALENDAR_VERSION_10 = 1;
    public static final int VCALENDAR_VERSION_20 = 2;
    public static final int VCARD_VERSION_21 = 3;
    public static final int VCARD_VERSION_30 = 4;
    public static final int X_S4J_SIFC = 5;
    public static final int X_S4J_SIFE = 6;
    public static final int X_S4J_SIFT = 7;
    private static final int X_S4J_SIFN = 8;
    private static final int VNOTE = 9;
    private static final int RX = 1;

    /**
     * This method is used to convert a vCard object from v3 to v2.1. We use
     * Funambol API to do this at its producer only supports v2.1 :)
     * 
     * @param bytes
     * @return
     * @throws ConverterException
     */
    public byte[] vCardV3toV21(byte[] bytes, String charset) throws ConverterException {
        FunambolLogger log = FunambolLoggerFactory.getLogger("funambol.zimbra");
        try {
            ContactToVcard conv;
            VcardParser p;
            Contact c;
            log.trace("About to convert vCard (from v3 to v2.1): " + new String(bytes));

            p = new VcardParser(new ByteArrayInputStream(sanitizevCardInput(bytes).getBytes()),
                                null,
                                null);
            c = p.vCard();

            conv = new ContactToVcard(TimeZone.getTimeZone("GMT"), charset);

            return conv.convert(c).getBytes();
        } catch (Exception e) {
            log.debug("Exception occured in vCardV3toV21(): " + e.toString());
            throw new ConverterException(e);
        }
    }

    /**
     * @param bytes
     * @return
     */
    public String sanitizevCardInput(byte[] bytes) {
        String prefs[] = { ";TYPE=work:", ";TYPE=home:", ";TYPE=cell:" };
        StringBuffer buf;
        int a, i;

        buf = new StringBuffer(new String(bytes));

        // We replace:
        // TEL;TYPE=PREF,WORK:+1234567890
        // with:
        // TEL;TYPE=PREF;TYPE=WORK:+1234567890
        a = buf.indexOf("TYPE=PREF,WORK");

        while (a >= 0) {
            buf.replace(a, a + 14, "TYPE=PREF;TYPE=WORK");
            a = buf.indexOf("TYPE=PREF,WORK");
        }

        // We replace:
        // ;TYPE=work:
        // with:
        // ;TYPE=WORK:
        // and same for "home" and "cell".
        for (i = 0; i < prefs.length; i++) {
            a = buf.indexOf(prefs[i]);

            while (a > 0) {
                buf.replace(a, a + prefs[i].length(), prefs[i].toUpperCase());
                a = buf.indexOf(prefs[i], a + 1);
            }
        }

        return buf.toString();

    }

    /**
     * @param context
     * @param syncSourceType
     * @param way
     * @return
     */
    public int getPreferredItemType(ZimbraSyncSource source, int syncSourceType, int way) {
        ArrayList<String> l_types, l_versions;
        ArrayList dataStores;
        DataStore ds;
        int i, type;

        // First, we get the supported types from our data store
        // and also their version numbers.
        try {
            dataStores = source.getPrincipal()
                               .getDevice()
                               .getCapabilities()
                               .getDevInf()
                               .getDataStores();
        } catch (Exception e) {
            // We haven't been able to get any device, capabilities or data
            // stores. We simply
            // assume our connecting client only supports the SIF standard.
            switch (syncSourceType) {
	            case CONTACT:
	                return X_S4J_SIFC;
	            case EVENT:
	                return X_S4J_SIFE;
	            case TODO:
	                return X_S4J_SIFT;
	            default:
	                return X_S4J_SIFN;
            }
        }

        l_versions = new ArrayList<String>();
        l_types = new ArrayList<String>();

        for (i = 0; i < dataStores.size(); i++) {
            ds = (DataStore) dataStores.get(i);

            if (way == RX) {
                l_types.add(ds.getRxPref().getCTType().trim());
                l_versions.add(ds.getRxPref().getVerCT().trim());
            } else {
                l_types.add(ds.getTxPref().getCTType().trim());
                l_versions.add(ds.getTxPref().getVerCT().trim());
            }
        }

        // Based on the current sync source type, we return the most appropriate
        // item's content type. This still is a guess as we have no way on
        // linking
        // the Device's capabilities with a current SyncSource.
        switch (syncSourceType) {
        case NOTE:
            if (l_types.contains(SIFN_TYPE)) {
                type = X_S4J_SIFN;
            }
            // Let's assume we've got text/x-vnote
            else
                type = VNOTE;
            break;
        case EVENT:
            if (l_types.contains(SIFE_TYPE)) {
                type = X_S4J_SIFE;
            }
            // Let's assume we've got text/x-vcalendar
            else if (l_versions.contains("2.0")) {
                type = VCALENDAR_VERSION_20;
            } else {
                type = VCALENDAR_VERSION_10;
            }
            break;
        case TODO:
            if (l_types.contains(SIFT_TYPE)) {
                type = X_S4J_SIFT;
            }
            // Let's assume we've got text/x-vcalendar
            else if (l_versions.contains("1.0")) {
                type = VCALENDAR_VERSION_10;
            } else {
                type = VCALENDAR_VERSION_20;
            }
            break;
        case CONTACT:
        default:
            if (l_types.contains(SIFC_TYPE)) {
                type = X_S4J_SIFC;
            }
            // Let's assume we've got text/x-vcard
            else if (l_versions.contains("3.0")) {
                type = VCARD_VERSION_30;
            } else {
                type = VCARD_VERSION_21;
            }
            break;
        }

        return type;
    }

    public static PhoneDependedConverter getInstance() {
        if (instance == null) {
            instance = new PhoneDependedConverter();
        }
        return instance;
    }
}
