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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.dom4j.Attribute;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import com.funambol.common.pim.calendar.Attendee;
import com.funambol.common.pim.calendar.Calendar;
import com.funambol.common.pim.calendar.CalendarContent;
import com.funambol.common.pim.calendar.Event;
import com.funambol.common.pim.calendar.ExceptionToRecurrenceRule;
import com.funambol.common.pim.calendar.RecurrencePattern;
import com.funambol.common.pim.calendar.Reminder;
import com.funambol.common.pim.calendar.Task;
import com.funambol.common.pim.common.Property;
import com.funambol.common.pim.converter.CalendarToSIFE;
import com.funambol.common.pim.converter.ConverterException;
import com.funambol.common.pim.converter.TaskToSIFT;
import com.funambol.common.pim.converter.VCalendarConverter;
import com.funambol.common.pim.icalendar.ICalendarParser;
import com.funambol.common.pim.icalendar.ParseException;
import com.funambol.common.pim.model.VCalendar;
import com.funambol.common.pim.sif.SIFCalendarParser;
import com.funambol.common.pim.utility.TimeUtils;
import com.funambol.common.pim.xvcalendar.XVCalendarParser;
import com.funambol.framework.logging.FunambolLogger;
import com.funambol.framework.logging.FunambolLoggerFactory;

public class CalendarUtils {
    private static final String E_WKDAY = "wkday";
    private static final String E_BYDAY = "byday";
    private static final String E_EXCEPTID = "exceptId";
    private static final String A_ATTENDEE_CUTYPE = "cutype";
    private static final String E_CONTENT = "content";
    private static final String TEXT_PLAIN = "text/plain";
    private static final String MULTIPART_ALTERNATIVE = "multipart/alternative";
    private static final String E_MP = "mp";
    private static final String A_CT = "ct";
    private static final String TYPE_UID = "UID";
    private static final String E_DUEDATE = "dueDate";
    private static final String BUSYSTATUS_UNAVAILABLE = "appointment.value.X-MICROSOFT-CDO-BUSYSTATUS.UNAVAILABLE";
    private static final String BUSYSTATUS_BUSY = "appointment.value.X-MICROSOFT-CDO-BUSYSTATUS.BUSY";
    private static final String BUSYSTATUS_TENTATIVE = "appointment.value.X-MICROSOFT-CDO-BUSYSTATUS.TENTATIVE";
    private static final String BUSYSTATUS_FREE = "appointment.value.X-MICROSOFT-CDO-BUSYSTATUS.FREE";
    private static final String TRANSP_OPAQUE = "OPAQUE";
    private static final char MOD_TIMESTAMP = 'T';
    private static final String A_ID = "id";
    private static final String A_TIMEZONE = "tz";
    private static final char MOD_SECONDS = 'S';
    private static final char MOD_MINUTE = 'M';
    private static final char MOD_HOUR = 'H';
    private static final String A_DAY = "d";
    private static final String A_SECOND = "s";
    private static final String A_MINUTE = "m";
    private static final char MOD_DAY = 'D';
    private static final char MOD_WEEK = 'W';
    private static final String A_HOUR = "h";
    private static final String A_WEEK = "w";
    private static final String A_NEGATIVE = "neg";
    private static final String TYPE_REPEAT_COUNT = "repeatCount";
    private static final String TYPE_INTERVAL = "interval";
    private static final String TYPE_OPTIONS = "options";
    private static final String TYPE_SOUND_FILE = "soundFile";
    private static final String TYPE_TIME = "time";
    private static final String TYPE_MINUTES = "minutes";
    private static final String TYPE_ACTIVE = "active";
    private static final String A_VALUE = "value";
    private static final String TYPE_REMINDER = "reminder";
    private static final String E_XPROP = "xprop";
    private static final String E_EXCLUDE = "exclude";
    private static final String A_DAYOFWEEK = "day";
    private static final String E_WKST = "wkst";
    private static final String E_UNTIL = "until";
    private static final String A_NUM = "num";
    private static final String E_COUNT = "count";
    private static final String E_DTVAL = "dtval";
    private static final String E_DATES = "dates";
    private static final String E_INTERVAL = "interval";
    private static final String A_IVAL = "ival";
    private static final String A_POSLIST = "poslist";
    private static final String E_BYSETPOS = "bysetpos";
    private static final String A_MODAYLIST = "modaylist";
    private static final String E_BYMONTHDAY = "bymonthday";
    private static final String A_MOLIST = "molist";
    private static final String E_BYMONTH = "bymonth";
    private static final String FREQ = "freq";
    private static final String E_RULE = "rule";
    private static final String E_ADD = "add";
    private static final String E_RECURENCE = "recur";
    private static final String A_ATTENDEE_PTST = "ptst";
    private static final String A_ATTENDEE_ROLE = "role";
    private static final String A_ATTENDEE_EMAIL = "a";
    private static final String E_ATTENDEE = "at";
    private static final String E_DESCRIPTION = "desc";
    private static final String E_DURATION = "dur";
    private static final String E_ENDDATE = "e";
    private static final String A_DATETIME = "d";
    private static final String E_STARTDATE = "s";
    private static String STATUS_COMPLETED = "COMPLETED";
    private static final String A_COMPLETED = "completed";
    private static final String A_PERCENT_COMPLETE = "percentComplete";
    private static final String A_STATUS = "status";
    private static final String A_TRANSPARENT = "transp";
    private static final String TRANSP_TRANSPARENT = "TRANSPARENT";
    private static final String A_PRIORITY = "priority";
    private static final String A_SEQUENCE = "seq";
    private static final String A_CLASS = "class";
    private static final String A_LOCATION = "loc";
    private static final String A_NAME = "name";
    private static final String A_ALLDAY = "allDay";
    private static final String A_FREEBUSYA = "fba";
    private static final String A_FREEBUSY = "fb";
    private static final String E_COMP = "comp";
    private static final String E_INVITE = "inv";
    private static final String E_SUMMARY = "su";
    private static final String E_MESSAGE = "m";
    private static HashMap<Short, String> busyStatus = new HashMap<Short, String>(7);
    private static HashMap<String, Short> busyStatus_ = new HashMap<String, Short>(7);
    private static HashMap<Short, String> kindOfAttendee = new HashMap<Short, String>(7);
    private static HashMap<String, Short> kindOfAttendee_ = new HashMap<String, Short>(7);
    private static HashMap<Short, String> roles = new HashMap<Short, String>(7);
    private static HashMap<String, Short> roles_ = new HashMap<String, Short>(7);
    private static HashMap<Short, String> participStatus = new HashMap<Short, String>(11);
    private static HashMap<String, Short> participStatus_ = new HashMap<String, Short>(11);
    private static HashMap<Short, String> accessClasses = new HashMap<Short, String>(7);
    private static HashMap<String, Short> accessClasses_ = new HashMap<String, Short>(7);
    private static HashMap<String, String> apptStatus = new HashMap<String, String>(11);
    private static HashMap<String, String> apptStatus_ = new HashMap<String, String>(11);
    private static HashMap<String, String> taskStatus = new HashMap<String, String>(11);
    private static HashMap<String, String> taskStatus_ = new HashMap<String, String>(11);

    private static final short SENSITIVITY_NORMAL = 0;
    private static final short SENSITIVITY_PERSONAL = 1;
    private static final short SENSITIVITY_PRIVATE = 2;
    private static final short SENSITIVITY_CONFIDENTIAL = 3;

    private static final String DAILY = "DAI";
    private static final String WEEKLY = "WEE";
    private static final String MONTHLY = "MON";
    private static final String YEARLY = "YEA";
    private static final String A_ATTENDEE_NAME = "d";

    private final HashMap<String, TimeZone> timeZonesHash= new HashMap<String, TimeZone>();

    private static CalendarUtils instance;
    private FunambolLogger logger = FunambolLoggerFactory.getLogger("funambol.zimbra.internal.CalendarUtils");

    private CalendarUtils() {

    }

    public static CalendarUtils getInstance() {
        if (instance == null) {
            instance = new CalendarUtils();
        }
        return instance;
    }

    static {
        InputStream is = CalendarUtils.class.getResourceAsStream("ICal2S4jMapping.properties");
        Properties icalMapping = new Properties();
        try {
            icalMapping.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        busyStatus.put(Short.valueOf(icalMapping.getProperty(BUSYSTATUS_FREE)), "F");
        busyStatus.put(Short.valueOf(icalMapping.getProperty(BUSYSTATUS_TENTATIVE)), "T");
        busyStatus.put(Short.valueOf(icalMapping.getProperty(BUSYSTATUS_BUSY)), "B");
        busyStatus.put(Short.valueOf(icalMapping.getProperty(BUSYSTATUS_UNAVAILABLE)), "O");

        busyStatus_.put("F", Short.valueOf(icalMapping.getProperty(BUSYSTATUS_FREE)));
        busyStatus_.put("T", Short.valueOf(icalMapping.getProperty(BUSYSTATUS_TENTATIVE)));
        busyStatus_.put("B", Short.valueOf(icalMapping.getProperty(BUSYSTATUS_BUSY)));
        busyStatus_.put("O", Short.valueOf(icalMapping.getProperty(BUSYSTATUS_UNAVAILABLE)));

        roles.put(Attendee.CHAIRMAN, "CHA");
        roles.put(Attendee.REQUIRED, "REQ");
        roles.put(Attendee.OPTIONAL, "OPT");
        roles.put(Attendee.NON_PARTICIPANT, "NON");

        roles_.put("CHA", Attendee.CHAIRMAN);
        roles_.put("REQ", Attendee.REQUIRED);
        roles_.put("OPT", Attendee.OPTIONAL);
        roles_.put("NON", Attendee.NON_PARTICIPANT);

        // ptst (ParTSTat - participation status) = "NE"eds-action, "TE"ntative,
        // "AC"cept, "DE"clined,
        // "DG" (delegated), "CO"mpleted (todo), "IN"-process (todo),
        // "WA"iting (custom value only for todo), "DF" (deferred; custom value
        // only for todo)
        participStatus.put(Attendee.NEEDS_ACTION, "NE");
        participStatus.put(Attendee.TENTATIVE, "TE");
        participStatus.put(Attendee.ACCEPTED, "AC");
        participStatus.put(Attendee.DECLINED, "DE");
        participStatus.put(Attendee.SENT, "IN");
        participStatus.put(Attendee.DELEGATED, "DG");
        participStatus.put(Attendee.COMPLETED, "CO");
        participStatus.put(Attendee.IN_PROCESS, "IN");
        // participationStatus.put(Attendee.Waiting, "DE");
        // participationStatus.put(Attendee.Deffered, "DE");
        participStatus_.put("NE", Attendee.NEEDS_ACTION);
        participStatus_.put("TE", Attendee.TENTATIVE);
        participStatus_.put("AC", Attendee.ACCEPTED);
        participStatus_.put("DE", Attendee.DECLINED);
        participStatus_.put("DG", Attendee.DELEGATED);
        participStatus_.put("CO", Attendee.COMPLETED);
        participStatus_.put("IN", Attendee.IN_PROCESS);

        accessClasses.put(SENSITIVITY_NORMAL, "PUB");
        // BUGFIX Zimbra hasn't PERSONAL value
        accessClasses.put(SENSITIVITY_PERSONAL, "PRI");
        accessClasses.put(SENSITIVITY_PRIVATE, "PRI");
        accessClasses.put(SENSITIVITY_CONFIDENTIAL, "CON");

        accessClasses_.put("PUB", SENSITIVITY_NORMAL);
        // BUGFIX Zimbra hasn't PERSONAL value
        // accessClasses_.put("PER", SENSITIVITY_PERSONAL);
        accessClasses_.put("PRI", SENSITIVITY_PRIVATE);
        accessClasses_.put("CON", SENSITIVITY_CONFIDENTIAL);

        apptStatus.put(icalMapping.getProperty("appointment.value.STATUS.TENTATIVE"), "TENT");
        apptStatus.put(icalMapping.getProperty("appointment.value.STATUS.CONFIRMED"), "CONF");
        apptStatus.put(icalMapping.getProperty("appointment.value.STATUS.CANCELLED"), "CANC");

        STATUS_COMPLETED = icalMapping.getProperty("task.value.STATUS.COMPLETED");
        taskStatus.put(icalMapping.getProperty("task.value.STATUS.COMPLETED"), "COMP");
        taskStatus.put(icalMapping.getProperty("task.value.STATUS.IN-PROCESS"), "INPR");
        taskStatus.put(icalMapping.getProperty("task.value.STATUS.WAINTING"), "WAITING");
        taskStatus.put(icalMapping.getProperty("task.value.STATUS.NEEDS-ACTION"), "NEED");
        taskStatus.put(icalMapping.getProperty("task.value.STATUS.CANCELLED"), "DEFERRED");
        // see #1934277
        taskStatus.put("NEEDS ACTION", "NEED");
        taskStatus.put("NEEDS-ACTION", "NEED");

        apptStatus_.put("TENT", icalMapping.getProperty("appointment.value.STATUS.TENTATIVE"));
        apptStatus_.put("CONF", icalMapping.getProperty("appointment.value.STATUS.CONFIRMED"));
        apptStatus_.put("CANC", icalMapping.getProperty("appointment.value.STATUS.CANCELLED"));

        taskStatus_.put("COMP", icalMapping.getProperty("task.value.STATUS.COMPLETED"));
        taskStatus_.put("INPR", icalMapping.getProperty("task.value.STATUS.IN-PROCESS"));
        taskStatus_.put("WAITING", icalMapping.getProperty("task.value.STATUS.NEEDS-ACTION"));
        taskStatus_.put("DEFERRED", icalMapping.getProperty("task.value.STATUS.CANCELLED"));

        kindOfAttendee.put(Attendee.INDIVIDUAL, "IND");
        kindOfAttendee.put(Attendee.GROUP, "GRO");
        kindOfAttendee.put(Attendee.RESOURCE, "RES");
        kindOfAttendee.put(Attendee.ROOM, "ROO");

        kindOfAttendee_.put("IND", Attendee.INDIVIDUAL);
        kindOfAttendee_.put("IND", Attendee.GROUP);
        kindOfAttendee_.put("RES", Attendee.RESOURCE);
        kindOfAttendee_.put("ROO", Attendee.ROOM);
    }

    /**
     * Convert Calendar object into Element for zimbra soap request
     *
     * @param -
     *            the calendar object
     * @param df -
     *            document factory
     * @param namespace -
     *            namespace of element
     * @param u
     *            update flag - if present then add empty <a n="..."/> elements
     * @return element of document
     */
    public Element asElement(Calendar c, DocumentFactory df, String namespace, boolean u) {
        Element calendar = df.createElement(E_MESSAGE, namespace);
        CalendarContent cc = c.getCalendarContent();

        if (cc.getSummary() != null) {
            Element summary = df.createElement(E_SUMMARY, namespace);
            summary.setText(cc.getSummary().getPropertyValueAsString());
            calendar.add(summary);
        }

        Element inv = df.createElement(E_INVITE, namespace);
        {
            // TODO if we have timezone we should set it;
            Element comp = df.createElement(E_COMP, namespace);
            {
                addAttr(A_FREEBUSY, map(busyStatus, cc.getBusyStatus()), comp);
                addAttr(A_FREEBUSYA, map(busyStatus, cc.getBusyStatus()), comp);
                addAttr(A_ALLDAY, String.valueOf(cc.isAllDay()), comp);
                addAttr(A_NAME, cc.getSummary(), comp);
                addAttr(A_LOCATION, cc.getLocation(), comp);
                Property accessClass = cc.getAccessClass();
                if (accessClass != null && accessClass.getPropertyValue() != null) {
                    short acValue = Short.valueOf(accessClass.getPropertyValueAsString());
                    addAttr(A_CLASS, map(accessClasses, acValue), comp);
                }
                addAttr(A_SEQUENCE, cc.getSequence(), comp);
                addAttr(A_PRIORITY, cc.getPriority(), comp);

                Property status = cc.getStatus();
                if (cc instanceof Event) {
                    Property transp = ((Event) cc).getTransp();
                    boolean trnsprnt = false;
                    if (transp != null) {
                        trnsprnt = TRANSP_TRANSPARENT.equals(transp.getPropertyValue());
                    }
                    addAttr(A_TRANSPARENT, trnsprnt ? "T" : "O", comp);
                    if (status != null)
                        addAttr(A_STATUS, map(apptStatus, status.getPropertyValue()), comp);
                } else if (cc instanceof Task) {
                    addAttr(A_PERCENT_COMPLETE, ((Task) cc).getPercentComplete(), comp);
                    String dateCompleted = getString(((Task) cc).getDateCompleted());
                    if (dateCompleted != null)
                        dateCompleted = dateCompleted.replace("-", "");
                    addAttr(A_COMPLETED, dateCompleted, comp);

                    String percentComplete = getString(((Task) cc).getPercentComplete());
                    String completed = getString(((Task) cc).getComplete());
                    if ("100".equals(percentComplete) || "1".equals(completed)) {
                        addAttr(A_STATUS, map(taskStatus, STATUS_COMPLETED), comp);
                    } else if (status != null) {
                        addAttr(A_STATUS, map(taskStatus, status.getPropertyValue()), comp);
                    }
                }

                Element startDate = df.createElement(E_STARTDATE, namespace);
                if (cc.isAllDay()) {
                    String date = cc.getDtStart().getPropertyValueAsString();
                    // workaround null date time
                    date = date==null? null: date.replace("-", "");
                    addAttr(A_DATETIME, date, startDate);
                } else {
                    addAttr(A_DATETIME, cc.getDtStart(), startDate);
                }
                if (startDate.attributeCount() > 0)
                    comp.add(startDate);

                if (cc instanceof Event && cc.getDtEnd() != null) {
                    Element endDate = df.createElement(E_ENDDATE, namespace);
                    if (cc.isAllDay()) {
                        String date = cc.getDtEnd().getPropertyValueAsString();
                        date = date.replace("-", "");
                        addAttr(A_DATETIME, date, endDate);
                    } else {
                        addAttr(A_DATETIME, cc.getDtEnd(), endDate);
                    }
                    comp.add(endDate);
                } else if (cc instanceof Task) {
                    Element durationDate = df.createElement(E_DURATION, namespace);
                    getDurationElement(cc.getDuration(), durationDate);
                    if (durationDate.attributeCount() > 0)
                        comp.add(durationDate);

                    String date = ((Task) cc).getDueDate().getPropertyValueAsString();
                    if (date != null && !"".equals(date)) {
                        Element dueDate = df.createElement(E_DUEDATE, namespace);
                        date = date.replace("-", "");
                        addAttr(A_DATETIME, date, dueDate);
                        comp.add(dueDate);
                    }
                }

                Reminder reminder = cc.getReminder();
                if (reminder != null) {
                    Element remElement = getReminderAsElement(cc, df, namespace);
                    comp.add(remElement);
                }

                RecurrencePattern recurrencePattern = cc.getRecurrencePattern();
                if (recurrencePattern != null) {
                    Element recElement = getRecurrenceAsElement(cc, df, namespace);
                    comp.add(recElement);
                }

                // 4.8.4.3 Organizer
                // Property Name: ORGANIZER
                // Purpose: The property defines the organizer for a calendar
                // component.
                // [isOrg="0|1"] // Am I the organizer? (default = 0)
                // addAttrByProp("isOrg", cc.getOrganizer()., comp, df);
                // How to correlate CN form Property ORGANIZER and isOrg???

                // TODO replies???
                String organizerEmail = getString(cc.getOrganizer());
                if ("".equals(organizerEmail))
                    organizerEmail = null;

                // String organizerName=null;

                List<Attendee> attendees = cc.getAttendees();
                if (attendees != null) {
                    Iterator<Attendee> itter = attendees.iterator();
                    while (itter.hasNext()) {
                        Attendee attendee = itter.next();
                        Element at = df.createElement(E_ATTENDEE);
                        addAttr(A_ATTENDEE_EMAIL, attendee.getEmail(), at);
                        addAttr(A_ATTENDEE_NAME, attendee.getName(), at);
                        if (attendee.getRole() == Attendee.ORGANIZER && organizerEmail == null) {
                            organizerEmail = attendee.getEmail();
                        }
                        addAttr(A_ATTENDEE_ROLE, map(roles, attendee.getExpected()), at);
                        addAttr(A_ATTENDEE_PTST, map(participStatus, attendee.getStatus()), at);
                        addAttr(A_ATTENDEE_CUTYPE, map(kindOfAttendee, attendee.getKind()), at);
                        comp.add(at);
                    }
                }
                // BUGFIX - We can't set organizer different from user who
                // request sync
                // By default it set to user who request sync
                // if(organizerEmail!=null){
                // Element organizerEl = comp.addElement("or");
                // organizerEl.addAttribute("a",
                // organizerEmail).addAttribute("d", organizerName);
                // }

                Property uid = cc.getUid();
                if (uid != null && uid.getPropertyValueAsString() != null) {
                    Element uidprop = df.createElement(E_XPROP, namespace);
                    uidprop.addAttribute(A_NAME, TYPE_UID);
                    uidprop.addAttribute(A_VALUE, uid.getPropertyValueAsString());
                    comp.add(uidprop);
                }

                inv.add(comp);
            }

            calendar.add(inv);

            boolean haveToAddMP = false;
            Element rootMP = df.createElement(E_MP, namespace);
            rootMP.addAttribute(A_CT, MULTIPART_ALTERNATIVE);
            {
                if (cc.getDescription() != null && cc.getDescription().getPropertyValue() != null) {
                    Element mp = rootMP.addElement(E_MP).addAttribute(A_CT, TEXT_PLAIN);
                    mp.addElement(E_CONTENT)
                      .setText(cc.getDescription().getPropertyValueAsString());
                    haveToAddMP = true;
                }
            }
            if (haveToAddMP)
                calendar.add(rootMP);
        }

        return calendar;
    }

    public static String getTags(Calendar c) {
        return getString(c.getCalendarContent().getCategories());
    }

    public static void setTags(Calendar c, String tags) {
        Property prop = new Property();
        if (tags != null)
            prop.setPropertyValue(tags);
        c.getCalendarContent().setCategories(prop);
    }



    private Element getRecurrenceAsElement(CalendarContent cc, DocumentFactory df, String namespace) {
        RecurrencePattern rp = cc.getRecurrencePattern();
        Element recur = df.createElement(E_RECURENCE, namespace);
        Element actionElement = recur.addElement(E_ADD);
        Element ruleElement = actionElement.addElement(E_RULE);
        short monthOfYear = rp.getMonthOfYear();
        short dayOfMonth = rp.getDayOfMonth();
        List<String> dayOfWeek = rp.getDayOfWeek();
        // short type=recPattern.getTypeId();

        String type = null;
        switch (rp.getTypeId()) {
        case RecurrencePattern.TYPE_DAYLY: // sic
            type = DAILY;
            break;
        case RecurrencePattern.TYPE_WEEKLY:
            type = WEEKLY;
            break;
        case RecurrencePattern.TYPE_MONTHLY:
        case RecurrencePattern.TYPE_MONTH_NTH:
            type = MONTHLY;
            break;
        case RecurrencePattern.TYPE_YEARLY:
        case RecurrencePattern.TYPE_YEAR_NTH:
            type = YEARLY;
            break;
        default:
            return null;
        }
        ruleElement.addAttribute(FREQ, type);

        if (monthOfYear != -1 && monthOfYear != 0) {
            Element bymonth = ruleElement.addElement(E_BYMONTH);
            bymonth.addAttribute(A_MOLIST, String.valueOf(monthOfYear));
        }

        if (dayOfMonth != -1 && dayOfMonth != 0) {
            Element bymonthday = ruleElement.addElement(E_BYMONTHDAY);
            bymonthday.addAttribute(A_MODAYLIST, String.valueOf(dayOfMonth));
        }

        if (rp.getInstance() != 0) {
            Element bysetpos = ruleElement.addElement(E_BYSETPOS);
            bysetpos.addAttribute(A_POSLIST, String.valueOf(rp.getInstance()));
        }

        if (rp.getInterval() != 0) {
            Element interval = ruleElement.addElement(E_INTERVAL);
            interval.addAttribute(A_IVAL, String.valueOf(rp.getInterval()));
        }

        Element datesElement = actionElement.addElement(E_DATES);
        datesElement.addAttribute(A_TIMEZONE, "");
        Element dtvalElement = datesElement.addElement(E_DTVAL);
        dtvalElement.addElement(E_STARTDATE)
                    .addAttribute(A_DATETIME, rp.getStartDatePattern().replace("-", ""));

        int occurrences = rp.getOccurrences();
        if (occurrences != -1) { // means unspecified: see RecurrencePattern
            Element count = ruleElement.addElement(E_COUNT);
            count.addAttribute(A_NUM, String.valueOf(occurrences));
        } else {
            if (!rp.isNoEndDate()) {
                Element until = ruleElement.addElement(E_UNTIL);
                until.addAttribute(A_DATETIME, rp.getEndDatePattern().replace("-", ""));
            }
        }

        if (dayOfWeek.size() > 0) {
            Element byday = ruleElement.addElement(E_BYDAY);
            for (int i = 0; i < dayOfWeek.size(); i++) {
                byday.addElement(E_WKDAY).addAttribute(A_DAYOFWEEK, dayOfWeek.get(i));
            }
        }

        List<ExceptionToRecurrenceRule> list = rp.getExceptions();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                ExceptionToRecurrenceRule exrule = list.get(i);
                actionElement = recur.addElement(exrule.isAddition() ? E_ADD : E_EXCLUDE);
                datesElement = actionElement.addElement(E_DATES);
                datesElement.addAttribute(A_TIMEZONE, "");
                dtvalElement = datesElement.addElement(E_DTVAL);
                String date = exrule.getDate().replace("-", "");
                dtvalElement.addElement(E_STARTDATE).addAttribute(A_DATETIME, date);
            }
        }
        return recur;
    }

    private Element getReminderAsElement(CalendarContent cc, DocumentFactory df, String namespace) {
        // Zimbra doesn't allow set reminder for each calendar, only global
        // variable.
        // But for each sync we should store reminder in the zimbra or local
        // storage
        // <xprop name="x-name" [value="text"]>
        // [<xparam name="x-name" [value="text"]/>]*
        // </xprop>
        Reminder reminder = cc.getReminder();
        Element xprop = df.createElement(E_XPROP, namespace);

        xprop.addAttribute(A_NAME, TYPE_REMINDER);
        if (reminder.getPropertyValueAsString() != null)
            xprop.addAttribute(A_VALUE, reminder.getPropertyValueAsString());

        addXParam(xprop, TYPE_ACTIVE, String.valueOf(reminder.isActive()));
        addXParam(xprop, TYPE_MINUTES, String.valueOf(reminder.getMinutes()));
        if (reminder.getTime() != null)
            addXParam(xprop, TYPE_TIME, String.valueOf(reminder.getTime()));
        if (reminder.getSoundFile() != null)
            addXParam(xprop, TYPE_SOUND_FILE, String.valueOf(reminder.getSoundFile()));
        addXParam(xprop, TYPE_OPTIONS, String.valueOf(reminder.getOptions()));
        addXParam(xprop, E_INTERVAL, String.valueOf(reminder.getInterval()));
        addXParam(xprop, TYPE_REPEAT_COUNT, String.valueOf(reminder.getRepeatCount()));
        return xprop;
    }

    private void addXParam(Element xprop, String name, String value) {
        Element xparam = xprop.addElement("xparam");
        xparam.addAttribute(A_NAME, name).addAttribute(A_VALUE, value);
    }

    // private static Element getVAlarmElement(CalendarContent cc,
    // DocumentFactory df,
    // String namespace) {
    // Reminder reminder = cc.getReminder();
    // Element valarm = df.createElement("alarm", namespace);
    // //reminder.getInterval();
    // //reminder.getMinutes()
    // System.out.println(reminder.getOptions());
    // valarm.addAttribute("action", reminder.getPropertyValueAsString());
    //
    // Element trigger = valarm.addElement("trigger");
    // {
    // // cc.getDAlarm().getPropertyValue()
    // reminder.getTime();
    // }
    //
    // return null;
    // }

    private void addAttr(String attrName, Property prop, Element comp) {
        if (prop != null)
            addAttr(attrName, prop.getPropertyValueAsString(), comp);
    }

    public static String getString(Property prop) {
        if (prop == null)
            return null;
        else
            return prop.getPropertyValueAsString();
    }

    private void addAttr(String attrName, String val, Element comp) {
        if (val != null && !val.trim().equals(""))
            comp.addAttribute(attrName, val);
    }

    private void addAttr(String attrName, Object val, Element comp) {
        addAttr(attrName, val == null ? null : val.toString(), comp);
    }

    public Calendar asCalendar(Element appt, boolean vevent, TimeZone phoneTimeZone) {
        Calendar c = new Calendar();
        if (vevent) {
            c.setEvent(new Event());
        } else {
            c.setTask(new Task());
        }
        CalendarContent cc = c.getCalendarContent();
        Element su = appt.element(E_SUMMARY);
        if (su != null)
            cc.setSummary(getProperty(su.getText()));
        cc.setCreated(getDateTime(Long.valueOf(appt.attributeValue(A_DATETIME))));
        cc.setLastModified(getDateTime(Long.valueOf(appt.attributeValue("md")) * 1000L));

        List<Element> invites = appt.elements(E_INVITE);
        int defaultInvite = 0;
        for (int i = 0; i < invites.size(); i++) {
            Element inv = invites.get(i);
            if (inv.element(E_COMP).element(E_EXCEPTID) == null) {
                defaultInvite = i;
            }
        }

        {
            // if it's default invite
            Element inv = invites.get(defaultInvite);
            {

                HashMap<String, Element> timezones = new HashMap<String, Element>(7);
                List<Element> tzLists = inv.elements(A_TIMEZONE);
                for (Iterator iterator = tzLists.iterator(); iterator.hasNext();) {
                    Element tz = (Element) iterator.next();
                    timezones.put(tz.attributeValue(A_ID), tz);
                }

                Element comp = inv.element(E_COMP);
                {
                    cc.setBusyStatus(getShort(busyStatus_, comp.attributeValue(A_FREEBUSY)));
                    cc.setBusyStatus(getShort(busyStatus_, comp.attributeValue(A_FREEBUSYA)));
                    String allDay = comp.attributeValue(A_ALLDAY);
                    cc.setAllDay(Boolean.valueOf("1".equals(allDay) || "true".equalsIgnoreCase(allDay)));
                    cc.setSummary(getProperty(comp.attributeValue(A_NAME)));
                    cc.setLocation(getProperty(comp.attributeValue(A_LOCATION)));
                    Short accessClass = getShort(accessClasses_, comp.attributeValue(A_CLASS));
                    cc.setAccessClass(getProperty(accessClass.toString()));

                    cc.setSequence(getProperty(comp.attributeValue(A_SEQUENCE)));
                    cc.setPriority(getProperty(comp.attributeValue(A_PRIORITY)));
                    if (cc instanceof Event) {
                        Object status = map(apptStatus_, comp.attributeValue(A_STATUS));
                        boolean trnsprnt = "T".equals(comp.attributeValue(A_TRANSPARENT));
                        ((Event) cc).setTransp(new Property(trnsprnt ? TRANSP_TRANSPARENT
                                : TRANSP_OPAQUE));
                        cc.setStatus(getProperty(status));
                    } else if (cc instanceof Task) {
                        Object status = map(taskStatus_, comp.attributeValue(A_STATUS));
                        String percentComplete = comp.attributeValue(A_PERCENT_COMPLETE);
                        String dateComplete = comp.attributeValue(A_COMPLETED);
                        ((Task) cc).setPercentComplete(getProperty(percentComplete));
                        ((Task) cc).setDateCompleted(getProperty(dateComplete));
                        if ("100".equals(percentComplete) || "1".equals(dateComplete)) {
                            cc.setStatus(getProperty(map(taskStatus_, "COMP")));
                        } else {
                            cc.setStatus(getProperty(status));
                        }
                    }

                    Element startDate = comp.element(E_STARTDATE);
                    if (startDate != null)
                        cc.setDtStart(getDateProperty(timezones, startDate, phoneTimeZone));

                    Element endDate = comp.element(E_ENDDATE);
                    if (endDate != null) {
                        cc.setDtEnd(getDateProperty(timezones, endDate, phoneTimeZone));
                    } else {
                        Element durationDate = comp.element(E_DURATION);
                        if (durationDate != null)
                            cc.setDuration(getDurationProperty(durationDate));
                    }

                    Element dueDate = comp.element(E_DUEDATE);
                    if (dueDate != null && cc instanceof Task) {
                        ((Task) cc).setDueDate(getDateProperty(timezones, dueDate, phoneTimeZone));
                    }
                    Element desc = comp.element(E_DESCRIPTION);

                    if (desc != null) {
                        cc.setDescription(getProperty(desc.getText()));
                    }

                    List<Element> xprops = comp.elements(E_XPROP);
                    for (int i = 0; i < xprops.size(); i++) {
                        Element xprop = xprops.get(i);
                        if (TYPE_REMINDER.equals(xprop.attributeValue(A_NAME))) {
                            cc.setReminder(getReminderAsProperty(xprop));
                        }
                        if (TYPE_UID.equals(xprop.attributeValue(A_NAME))) {
                            cc.setUid(getProperty(xprop.attributeValue(A_VALUE)));
                        }
                    }

                    Element recurElem = comp.element(E_RECURENCE);
                    
                    //FIXME: check if this works (patched)
                    if (recurElem != null) {
                        String d = startDate.attributeValue(A_DATETIME);
                        String tz = startDate.attributeValue(A_TIMEZONE);
                    	try {
							d = correctTimeZone(d,timezones.get(tz),TimeZone.getTimeZone("UTC"));
							if(!d.endsWith("Z")) d+="Z";
						} catch (ConverterException e) {
							//Handled in correctTimeZone
						}
                        cc.setRecurrencePattern(getRecurrenceAsProperty(recurElem, d));
                    }
                    //if (recurElem != null)
                       // cc.setRecurrencePattern(getRecurrenceAsProperty(recurElem));

                    Element organizer = comp.element("or");
                    String organizerEmail = null;
                    if (organizer != null) {
                        organizerEmail = organizer.attributeValue(A_ATTENDEE_EMAIL);
                        cc.setOrganizer(new Property(organizerEmail));
                    }

                    List<Element> atts = comp.elements(E_ATTENDEE);
                    for (Iterator iterator = atts.iterator(); iterator.hasNext();) {
                        Element at = (Element) iterator.next();
                        Attendee attendee = new Attendee();
                        attendee.setEmail(at.attributeValue(A_ATTENDEE_EMAIL));
                        String withoutmailto = attendee.getEmail();
                        if (withoutmailto.startsWith("MAILTO:"))
                            withoutmailto = withoutmailto.substring(6);
                        if (organizerEmail != null && organizerEmail.equals(withoutmailto)) {
                            attendee.setRole(Attendee.ORGANIZER);
                        } else {
                            attendee.setRole(Attendee.ATTENDEE);
                        }
                        attendee.setName(at.attributeValue(A_ATTENDEE_NAME));
                        attendee.setExpected(getShort(roles_, at.attributeValue(A_ATTENDEE_ROLE)));
                        attendee.setStatus(getShort(participStatus_,
                                                    at.attributeValue(A_ATTENDEE_PTST)));
                        attendee.setKind(getShort(kindOfAttendee_,
                                                  at.attributeValue(A_ATTENDEE_CUTYPE)));
                        cc.addAttendee(attendee);
                    }

                }
            }
        }
        for (int i = 0; i < invites.size(); i++) {
            // if it is not default invite
            if (i != defaultInvite) {
                // if we has non default invite then we has recurence
                RecurrencePattern rp = cc.getRecurrencePattern();
                if (rp == null) {
                    logger.error("Recurence pattern can't be null!!!!");
                }

                Element inv = invites.get(i);
                HashMap<String, Element> timezones = new HashMap<String, Element>(7);
                List<Element> tzLists = inv.elements(A_TIMEZONE);
                for (Iterator iterator = tzLists.iterator(); iterator.hasNext();) {
                    Element tz = (Element) iterator.next();
                    timezones.put(tz.attributeValue(A_ID), tz);
                }
                Element comp = inv.element(E_COMP);
                {
                    // Element startDateEl = comp.element(E_STARTDATE);
                    // if (startDateEl != null)
                    // cc.setDtStart(getDateProperty(timezones, startDateEl));
                    //
                    // Element endDate = comp.element(E_ENDDATE);
                    // if (endDate != null) {
                    // cc.setDtEnd(getDateProperty(timezones, endDate));
                    // } else {
                    // Element durationDate = comp.element(E_DURATION);
                    // if (durationDate != null)
                    // cc.setDuration(getDurationProperty(durationDate));
                    // }
                    if ("CANC".equalsIgnoreCase(comp.attributeValue(A_STATUS))) {
                        // exclude rule to recurence pattern
                        Element except = comp.element(E_EXCEPTID);
                        Property exdate = getDateProperty(timezones, except, phoneTimeZone);
                        try {
                            ExceptionToRecurrenceRule etrr = new ExceptionToRecurrenceRule(false,
                                                                                           exdate.getPropertyValueAsString());
                            rp.getExceptions().add(etrr);
                        } catch (java.text.ParseException e) {
                            logger.error("Error set recurence rule exception", e);
                        }
                    } else {
                        logger.warn("Can't moving calendar event (status of this instance set to CANC)");
                    }
                }

            }
        }
        return c;
    }

	//FIXME: check if this works (patched)
    private RecurrencePattern getRecurrenceAsProperty(Element recur, String defaultStartDatePat) {
        List<Element> listEl = recur.elements();
        byte typeId = RecurrencePattern.UNSPECIFIED;
        boolean first = true;
        boolean startDatePatternFind = false;
        short monthOfYear = RecurrencePattern.UNSPECIFIED;
        short dayOfMonth = RecurrencePattern.UNSPECIFIED;
        short instance = RecurrencePattern.UNSPECIFIED;
        short interval = RecurrencePattern.UNSPECIFIED;
        short dayOfWeekMask = RecurrencePattern.UNSPECIFIED;
        int occurrences = -1;
        String endDatePattern = null;
        String startDatePattern = null;
        List<ExceptionToRecurrenceRule> list = new ArrayList<ExceptionToRecurrenceRule>();
        for (Iterator iterator = listEl.iterator(); iterator.hasNext();) {
            Element actionElement = (Element) iterator.next();
            boolean isAddition = actionElement.getName().equals(E_ADD);
            if (first && isAddition) {
                first = false;
                Element ruleElement = actionElement.element(E_RULE);
                String type = ruleElement.attributeValue(FREQ);
                if (DAILY.equals(type)) {
                    typeId = RecurrencePattern.TYPE_DAYLY;
                } else if (WEEKLY.equals(type)) {
                    typeId = RecurrencePattern.TYPE_WEEKLY;
                } else if (MONTHLY.equals(type)) {
                    typeId = RecurrencePattern.TYPE_MONTHLY;
                } else if (YEARLY.equals(type)) {
                    typeId = RecurrencePattern.TYPE_YEARLY;
                } else {
                    return null;
                }

                Element bymonth = ruleElement.element(E_BYMONTH);
                if (bymonth != null) {
                    String listId = bymonth.attributeValue(A_MOLIST);
                    String[] listIds = listId.split(",", 2);
                    monthOfYear = Short.valueOf(listIds[0]);
                }

                Element bymonthday = ruleElement.element(E_BYMONTHDAY);
                if (bymonthday != null) {
                    String listId = bymonthday.attributeValue(A_MODAYLIST);
                    String[] listIds = listId.split(",", 2);
                    dayOfMonth = Short.valueOf(listIds[0]);
                }

                Element bysetpos = ruleElement.element(E_BYSETPOS);
                if (bysetpos != null) {
                    String listId = bysetpos.attributeValue(A_POSLIST);
                    String[] listIds = listId.split(",", 2);
                    instance = Short.valueOf(listIds[0]);
                }

                Element interv = ruleElement.element(E_INTERVAL);
                if (interv != null) {
                    String listId = interv.attributeValue(A_IVAL);
                    String[] listIds = listId.split(",", 2);
                    interval = Short.valueOf(listIds[0]);
                }

                Element count = ruleElement.element(E_COUNT);
                if (count != null) {
                    occurrences = Integer.valueOf(count.attributeValue(A_NUM));
                }

                Element until = ruleElement.element(E_UNTIL);
                if (until != null) {
                    endDatePattern = until.attributeValue(A_DATETIME);
                }

                // I don't remove this statment because I think in some cases
                // zimbra can use this method determine week
                Element wkst = ruleElement.element(E_WKST);
                if (wkst != null) {
                    String listId = wkst.attributeValue(A_DAYOFWEEK);
                    String[] listIds = listId.split(",");
                    for (String day : listIds) {
                        dayOfWeekMask = setDayMask(dayOfWeekMask, day);
                    }
                }

                Element byday = ruleElement.element(E_BYDAY);
                if (byday != null) {
                    List<Element> weekdays = byday.elements(E_WKDAY);
                    for (int i = 0; i < weekdays.size(); i++) {
                        String day = weekdays.get(i).attributeValue(A_DAYOFWEEK);
                        dayOfWeekMask = setDayMask(dayOfWeekMask, day);
                    }
                }
            }

            if (!startDatePatternFind && isAddition) {
                Element datesElement = actionElement.element(E_DATES);
                if (datesElement != null) {
                    Element dtvalElement = datesElement.element(E_DTVAL);
                    startDatePattern = dtvalElement.element(E_STARTDATE).attributeValue(A_DATETIME);
                    startDatePatternFind = true;
                }
            } else {
                Element datesElement = actionElement.element(E_DATES);
                if (datesElement != null) {
                    Element dtvalElement = datesElement.element(E_DTVAL);
                    String date = dtvalElement.element(E_STARTDATE).attributeValue(A_DATETIME);
                    try {
                        ExceptionToRecurrenceRule exrule = new ExceptionToRecurrenceRule(isAddition,
                                                                                         date);
                        list.add(exrule);
                    } catch (java.text.ParseException e) {
                        FunambolLogger log = FunambolLoggerFactory.getLogger("funambol.zimbra.calendar");
                        log.error("Invalid date:" + date, e);
                    }
                }

            }
        }
		//FIXME: check if this works or not (patched)
        if(!startDatePatternFind) startDatePattern = defaultStartDatePat;
        RecurrencePattern rp = new RecurrencePattern(typeId,
                                                     interval,
                                                     monthOfYear,
                                                     dayOfMonth,
                                                     dayOfWeekMask,
                                                     instance,
                                                     startDatePattern,
                                                     endDatePattern,
                                                     endDatePattern == null,
                                                     occurrences);
        return rp;
    }

    private short setDayMask(short dayOfWeekMask, String day) {
        if ("SU".equals(day)) {
            dayOfWeekMask |= RecurrencePattern.DAY_OF_WEEK_SUNDAY;
        } else if ("MO".equals(day)) {
            dayOfWeekMask |= RecurrencePattern.DAY_OF_WEEK_MONDAY;
        } else if ("TU".equals(day)) {
            dayOfWeekMask |= RecurrencePattern.DAY_OF_WEEK_TUESDAY;
        } else if ("WE".equals(day)) {
            dayOfWeekMask |= RecurrencePattern.DAY_OF_WEEK_WEDNESDAY;
        } else if ("TH".equals(day)) {
            dayOfWeekMask |= RecurrencePattern.DAY_OF_WEEK_THURSDAY;
        } else if ("FR".equals(day)) {
            dayOfWeekMask |= RecurrencePattern.DAY_OF_WEEK_FRIDAY;
        } else if ("SA".equals(day)) {
            dayOfWeekMask |= RecurrencePattern.DAY_OF_WEEK_SATURDAY;
        }
        return dayOfWeekMask;
    }

    private Reminder getReminderAsProperty(Element xprop) {
        Reminder rem = new Reminder();
        rem.setPropertyValue(xprop.attributeValue(A_VALUE));
        List<Element> xparams = xprop.elements();
        for (int i = 0; i < xparams.size(); i++) {
            Element xparam = xparams.get(i);
            String name = xparam.attributeValue(A_NAME);
            String value = xparam.attributeValue(A_VALUE);
            if (TYPE_ACTIVE.equalsIgnoreCase(name)) {
                rem.setActive(Boolean.valueOf(value));
            } else if (TYPE_MINUTES.equalsIgnoreCase(name)) {
                rem.setMinutes(Integer.valueOf(value));
            } else if (TYPE_TIME.equalsIgnoreCase(name)) {
                rem.setTime(value);
            } else if (TYPE_SOUND_FILE.equalsIgnoreCase(name)) {
                rem.setSoundFile(value);
            } else if (TYPE_OPTIONS.equalsIgnoreCase(name)) {
                rem.setOptions(Integer.valueOf(value));
            } else if (TYPE_INTERVAL.equalsIgnoreCase(name)) {
                rem.setInterval(Integer.valueOf(value));
            } else if (TYPE_REPEAT_COUNT.equalsIgnoreCase(name)) {
                rem.setRepeatCount(Integer.valueOf(value));
            }
        }
        return rem;

    }

    private Property getDateTime(Long valueOf) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(valueOf);
        return getProperty(formatCallendar(cal));
    }

    /**
     * Convert zimbra duration tag into iCal spec duration for more information
     * see RFC 2445 iCalendar specification
     *
     * @param durationDate
     * @return
     */
    private Property getDurationProperty(Element durationDate) {
        StringBuilder sb = new StringBuilder();
        Attribute negAttr = durationDate.attribute(A_NEGATIVE);
        if (negAttr != null) {
            sb.append(negAttr.getValue().equals("1") ? '-' : '+');
        }
        sb.append('P');
        Attribute wAttr = durationDate.attribute(A_WEEK);// week
        if (wAttr != null) {
            sb.append(wAttr.getValue()).append(MOD_WEEK);
            return getProperty(sb.toString());
        }
        
        Attribute dAttr = durationDate.attribute(A_DAY);// day
        if (dAttr != null) {
            sb.append(dAttr.getValue()).append(MOD_DAY);
        }
        
        Attribute hAttr = durationDate.attribute(A_HOUR);// hour
        Attribute mAttr = durationDate.attribute(A_MINUTE);// minute
        Attribute sAttr = durationDate.attribute(A_SECOND);// second
        if (hAttr != null && mAttr != null && sAttr != null) {// then specified Hours, Minutes, Seconds
            sb.append(MOD_TIMESTAMP);
            sb.append(hAttr.getValue()).append(MOD_HOUR);
            sb.append(mAttr.getValue()).append(MOD_MINUTE);
            sb.append(sAttr.getValue()).append(MOD_SECONDS);
        }

        return getProperty(sb.toString());
    }

    /**
     * Convert iCal duration tag into zimbra duration for more information see
     * RFC 2445 iCalendar specification
     *
     * @param durationDate
     * @return
     */
    private Element getDurationElement(Property duration, Element durationDate) {
        String dur = duration.getPropertyValueAsString();
        if (dur == null)
            return null;
        int index = 0;
        if (dur.charAt(index) == '-') {
            index += 2;
            durationDate.addAttribute(A_NEGATIVE, "1");
        } else {
            durationDate.addAttribute(A_NEGATIVE, "0");
            index += dur.charAt(index) == '+' ? 2 : 1;
        }
        int weekFind = dur.indexOf(MOD_WEEK, index);
        if (weekFind != -1) {
            durationDate.addAttribute(A_WEEK, dur.substring(index, weekFind));
            return durationDate;
        }
        int dayFind = dur.indexOf(MOD_DAY, index);
        if (dayFind != -1) {
            durationDate.addAttribute(A_DATETIME, dur.substring(index, dayFind));
            index = dayFind + 1;
        }
        assert dur.charAt(index) == MOD_TIMESTAMP : "Illegal Duration format";
        index++;
        int hourFind = dur.indexOf(MOD_HOUR, index);
        durationDate.addAttribute(A_HOUR, dur.substring(index, hourFind));
        index = hourFind + 1;
        int minFind = dur.indexOf(MOD_MINUTE, index);
        durationDate.addAttribute(E_MESSAGE, dur.substring(index, minFind));
        index = minFind + 1;
        int secFind = dur.indexOf(MOD_SECONDS, index);
        durationDate.addAttribute(E_STARTDATE, dur.substring(index, secFind));
        return durationDate;
    }

    private Property getDateProperty(HashMap<String, Element> timezones, Element endDate,
            TimeZone phoneTimeZone) {
        String tzAttr;
        String endD = endDate.attributeValue(A_DATETIME);
        if (endD.trim().equals(""))
            return new Property();
        tzAttr = endDate.attributeValue(A_TIMEZONE);
        try {
            endD = correctTimeZone(endD,
                                   (tzAttr == null ? null : timezones.get(tzAttr)),
                                   phoneTimeZone);
        } catch (ConverterException e) {
            // do nothing, use end date from attribute directly, without
            // timezone changes
        }
        Property property = getProperty(endD);
        return property;
    }

    private Property getProperty(Object propValue) {
        Property prop = new Property();
        if (propValue == null || propValue.toString().trim().equals(""))
            return prop;// BUGFIX - If one of properties doesn't exist then
        // exception occur
        prop.setPropertyValue(propValue);
        return prop;
    }

    /**
     * Recommendation - don't use version field if you don't known exactly know
     * that you do
     *
     * @param type
     * @param version
     * @param c
     * @param timezone
     * @param charset
     * @return
     * @throws ConverterException
     */
    public static byte[] convertTo(String type, String version, Calendar c, TimeZone timezone,
            String charset) throws ConverterException {
        byte[] content = null;
        
        FunambolLogger logger = FunambolLoggerFactory.getLogger("funambol.zimbra.internal.CalendarUtils");
        
        
        if (PhoneDependedConverter.SIFE_TYPE.equals(type)) {
    		CalendarToSIFE conv = new CalendarToSIFE(timezone, charset);
            content = conv.convert(c).getBytes();
        } else if (PhoneDependedConverter.SIFT_TYPE.equals(type)) {
            TaskToSIFT conv = new TaskToSIFT(timezone, charset);
            content = conv.convert(c.getTask()).getBytes();
        } else if (PhoneDependedConverter.VCAL_TYPE.equals(type)
                || PhoneDependedConverter.ICAL_TYPE.equals(type)) {
            VCalendarConverter vconv = new VCalendarConverter(timezone, charset, false);
            CalendarContent cc = c.getCalendarContent();
            Object val = cc.getAccessClass().getPropertyValue();
            if (val instanceof String) {
                cc.getAccessClass().setPropertyValue(Short.valueOf((String) val));
            }
            if (version == null) {
                version = PhoneDependedConverter.VCAL_TYPE.equals(type) ? "1.0" : "2.0";
            }
            VCalendar vcal = vconv.calendar2vcalendar(c, "1.0".equals(version));
            content = vcal.toString().getBytes();

        } else {
        	//TODO: add default type to the configuration of the syncsource
        	logger.error("Unsupported type: " + type + " there is no default calendar sync protocol defined");
            throw new ConverterException("Unsupported type:" + type + " there is no default calendar sync protocol defined");
        }
        return content;
    }

    /**
     * Recommendation - don't use version field if you don't known exactly know
     * that you do
     *
     * @param type
     * @param version
     *            version of calendar
     * @param content
     * @param timezone
     * @param charset
     * @return
     * @throws ConverterException
     */

	public static Calendar convertFrom(String type, String version, byte[] content,
            TimeZone timezone, String charset) throws ConverterException {
        Calendar calendar = null;
        
        FunambolLogger logger = FunambolLoggerFactory.getLogger("funambol.zimbra.internal.CalendarUtils");
        
        
        if(type == null) type = guessType(new String(content));
        if (PhoneDependedConverter.SIFE_TYPE.equals(type)
                || PhoneDependedConverter.SIFT_TYPE.equals(type)) {
            SIFCalendarParser sifParser;
            try {
                sifParser = new SIFCalendarParser(new ByteArrayInputStream(content));
                calendar = sifParser.parse();
            } catch (Throwable e) {
                throw new ConverterException(e);
            }
        } else if (PhoneDependedConverter.VCAL_TYPE.equals(type)) {
            XVCalendarParser parser = new XVCalendarParser(new ByteArrayInputStream(content));
            VCalendarConverter vconv = new VCalendarConverter(timezone, charset, false);
            try {
                VCalendar vcalendar = (VCalendar) parser.XVCalendar();
                vcalendar.addProperty("VERSION", version == null ? "1.0" : version);
                calendar = vconv.vcalendar2calendar(vcalendar);
            } catch (com.funambol.common.pim.xvcalendar.ParseException e) {
                throw new ConverterException(e);
            }
        } else if (PhoneDependedConverter.ICAL_TYPE.equals(type)) {
            ICalendarParser parser;
            parser = new ICalendarParser(new ByteArrayInputStream(content));
            VCalendarConverter vconv = new VCalendarConverter(timezone, charset, false);
            try {
                VCalendar vcalendar = parser.ICalendar();
                vcalendar.addProperty("VERSION", version == null ? "2.0" : version);
                calendar = vconv.vcalendar2calendar(vcalendar);
            } catch (ParseException e) {
            	logger.error("Calendar parse error:", e);
                throw new ConverterException(e);
            }
        } else {
        	//TODO: add default type to the configuration of the syncsource
        	logger.error("Unsupported type: " + type + " there is no default calendar sync protocol defined");
            throw new ConverterException("Unsupported type:" + type + " there is no default calendar sync protocol defined");
        }
        return calendar;
    }

	/**
	 * Essaye de deviner le type d'un element en fonction de son contenu
	 *
	 * @param content Le contenu de l'élément
	 * @return Le type déterminé
	 */
	protected static String guessType(String content) {
		FunambolLogger log = FunambolLoggerFactory.getLogger("funambol.zimbra.internal.CalendarUtils");
		if (log.isTraceEnabled()) log.trace("Guessing type based on content");
		String type=null;
		content = content.replaceAll("(?s)(\\A(\\n|\\r)*)|((\\n|\\r)*\\Z)", "");//Trims \r and \n at the beginning and the end of the string
		if (log.isTraceEnabled()) log.debug("Content : <<"+content+">>");
		if(content.matches("(?s)\\ABEGIN:VCALENDAR(.*)END:VCALENDAR\\Z")) {//TODO reconnaitre ICal
			type = PhoneDependedConverter.VCAL_TYPE;
		}
		else if(content.matches("(?s)\\A<\\?xml(.*)<appointment>(.*)<SIFVersion>(.*)</SIFVersion>(.*)</appointment>\\Z")) {
			type = PhoneDependedConverter.SIFE_TYPE;
		}
		if (log.isTraceEnabled()) log.trace("Guessed type : "+type);
		return type;
	}

    private Short getShort(HashMap<String, Short> map, String key) {
        Short val = (Short) map(map, key);
        if (val != null)
            return val;
        return -1;

    }

    public String correctTimeZone(String datetime, Element tz, TimeZone phoneTimeZone)
            throws ConverterException {

        // if (datetime.length() == 8) {
        // datetime = datetime + "T000000" + (tz == null ? "Z" : "");
        // }
        // if (datetime.length() < 9) {
        // // return as it is;
        // return datetime;
        // }
        // if (datetime.charAt(8) == 'T') {
        String sDate = datetime;
        if (sDate == null || sDate.equals("") || TimeUtils.isInAllDayFormat(sDate)) {
            return sDate;
        }

        TimeZone timeZone = getTimeZone(tz);
        try {
            if (sDate.indexOf('Z') != -1) {
                // No conversion is required
                sDate=TimeUtils.convertUTCDateToLocal(sDate, phoneTimeZone);
                return sDate;
            }

//            int stdoff = 0;
            DateFormat noZFormatter = new SimpleDateFormat(TimeUtils.PATTERN_UTC_WOZ);

            Date date = null;
            noZFormatter.setTimeZone(timeZone);
            date = noZFormatter.parse(datetime);
            noZFormatter.setTimeZone(phoneTimeZone);
            sDate = noZFormatter.format(date);
        } catch (java.text.ParseException e) {
            logger.error("Error occur while converting local date to UTC:" + datetime
                    + ", timezone=" + timeZone + ", phoneTZ=" + phoneTimeZone);
            throw new ConverterException(e);
        } catch (Throwable e) {
            logger.error("Error occur while converting local date to UTC:" + datetime
                    + ", timezone=" + timeZone + ", phoneTZ=" + phoneTimeZone);
            throw new ConverterException(e);
        }
        return sDate;
        // try {
        // TimeUtils.convertLocalDateToUTC(datetime, timeZone)
        // return TimeUtils.convertUTCDateToLocal(datetime, timeZone);
        // } catch (Exception e) {
        // logger.error("Error occur while converting local date to
        // UTC:"+datetime+", timezone="+timeZone);
        // throw new ConverterException(e);
        // }

        // int year = Integer.parseInt(datetime.substring(0, 4));
        // int month = Integer.parseInt(datetime.substring(4, 6));
        // int date = Integer.parseInt(datetime.substring(6, 8));
        // int hourOfDay = Integer.parseInt(datetime.substring(9, 11));
        // int minute = Integer.parseInt(datetime.substring(11, 13));
        // int second = Integer.parseInt(datetime.substring(13, 15));
        //
        // java.util.Calendar calendar = java.util.Calendar.getInstance();
        // calendar.set(year, month - 1, date, hourOfDay, minute, second);
        // calendar.setTimeZone(timeZone);
        // try {
        // datetime=TimeUtils.convertUTCDateToLocal(datetime, timeZone);
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // boolean convertTimezone = !(datetime.length() == 16 &&
        // datetime.charAt(15) == 'Z');
        // // boolean convertDaylight = true;
        // // if (tz != null) {
        // // convertDaylight = dayLightTime(tz, calendar);
        // // } else {
        // // convertDaylight = timeZone.inDaylightTime(calendar.getTime());
        // // }
        // if (/*convertDaylight ||*/ convertTimezone) {
        // long time = calendar.getTimeInMillis();
        // if (convertTimezone) {
        // // tz can't be null because convertTimezone in this case
        // // must be null
        // time -= stdoff * 60 * 1000L;
        // }
        // // if (convertDaylight) {
        // // time -= 60 * 60 * 1000L;
        // // }
        // // if tz == null then we do double convertion
        // if (tz != null) {
        // TimeZone UTC = new SimpleTimeZone(0, "UTC");
        // calendar.setTimeZone(UTC);
        // }
        // calendar.setTimeInMillis(time);
        // } else {
        // return datetime;
        // }
        // return formatCallendar(calendar);
        //
        // } else
        // throw new RuntimeException("Unexpected value: " + datetime);
    }

    /**
     * Create new timezone by timezoneElement
     *
     * @param tz
     * @return
     */
    public TimeZone getTimeZone(Element tz) {
        TimeZone timeZone;
        if (tz != null) {
            String timeZoneId = tz.attributeValue(A_ID);
            timeZone=timeZonesHash.get(timeZoneId);
            if(timeZone==null){
                int stdoff = Integer.parseInt(tz.attributeValue("stdoff"));
                int rawoffset = stdoff * 60 * 1000;

                SimpleTimeZone simpleTimeZone = new SimpleTimeZone(rawoffset, timeZoneId);

                if(tz.attributeValue("dayoff")!=null){ //parse daylight saving  if possible
                    int dstoff = Integer.parseInt(tz.attributeValue("dayoff")) * 60 * 1000 - rawoffset;
                    Element dlElement = tz.element("daylight");
                    Element stElement = tz.element("standard");

                    simpleTimeZone.setDSTSavings(dstoff);

                    int mMonth;
                    int mDayOfMonth;
                    int mDayOfWeek;
                    int mDtStartMillis;
                    int hour = Integer.parseInt(dlElement.attributeValue("hour"));
                    int min = Integer.parseInt(dlElement.attributeValue("min"));
                    int sec = Integer.parseInt(dlElement.attributeValue("sec"));

                    mMonth = Integer.parseInt(dlElement.attributeValue("mon")) - 1;
                    String weekAtr = dlElement.attributeValue("week");
                    int week = weekAtr == null ? 0 : Integer.parseInt(weekAtr);
                    if (week != 0) {
                        if (week < 0) {
                            mDayOfMonth = week;
                            mDayOfWeek = Integer.parseInt(dlElement.attributeValue("wkday"));
                        } else {
                            mDayOfMonth = (week - 1) * 7 + 1;
                            mDayOfWeek = -1 * Integer.parseInt(dlElement.attributeValue("wkday"));
                        }
                    } else {
                        mDayOfMonth = Integer.parseInt(dlElement.attributeValue("mday"));
                        mDayOfWeek = 0;
                    }

                    mDtStartMillis = hour * 3600000 + min * 60000 + sec * 1000;
                    simpleTimeZone.setStartRule(mMonth, mDayOfMonth, mDayOfWeek, mDtStartMillis);

                    hour = Integer.parseInt(stElement.attributeValue("hour"));
                    min = Integer.parseInt(stElement.attributeValue("min"));
                    sec = Integer.parseInt(stElement.attributeValue("sec"));

                    mMonth = Integer.parseInt(stElement.attributeValue("mon")) - 1;
                    weekAtr = stElement.attributeValue("week");
                    week = weekAtr == null ? 0 : Integer.parseInt(weekAtr);
                    if (week != 0) {
                        if (week < 0) {
                            mDayOfMonth = week;
                            mDayOfWeek = Integer.parseInt(stElement.attributeValue("wkday"));
                        } else {
                            mDayOfMonth = (week - 1) * 7 + 1;
                            mDayOfWeek = -1 * Integer.parseInt(stElement.attributeValue("wkday"));
                        }
                    } else {
                        mDayOfMonth = Integer.parseInt(stElement.attributeValue("mday"));
                        mDayOfWeek = 0;
                    }
                    mDtStartMillis = hour * 3600000 + min * 60000 + sec * 1000;
                    simpleTimeZone.setEndRule(mMonth, mDayOfMonth, mDayOfWeek, mDtStartMillis);
                }

                timeZone = simpleTimeZone;
                timeZonesHash.put(timeZoneId, timeZone);
            }
            // }
        } else {
            timeZone = TimeZone.getDefault();
        }
        if (logger.isTraceEnabled()) {
            logger.trace("getTimeZone:: tz=" + (tz==null? "null":tz.asXML()));
            logger.trace("getTimeZone:: timezone=" + timeZone);
        }
        return timeZone;
    }

    public boolean dayLightTime(Element tz, java.util.Calendar calendar) {
        Element dlElement = tz.element("daylight");
        Element stElement = tz.element("standard");
        Integer dlMon, stMon;
        dlMon = Integer.parseInt(dlElement.attributeValue("mon"));
        stMon = Integer.parseInt(stElement.attributeValue("mon"));

        // I think it more perfomance then always create calendars for dayligh
        // and standart time
        if (dlMon < calendar.get(java.util.Calendar.MONTH)
                && calendar.get(java.util.Calendar.MONTH) < stMon) {
            return true;
        } else if (dlMon == calendar.get(java.util.Calendar.MONTH)) {
            java.util.Calendar dlTime = getDayLightTransmition(dlElement);
            dlTime.set(java.util.Calendar.YEAR, calendar.get(java.util.Calendar.YEAR));
            return calendar.after(dlTime);
        } else if (calendar.get(java.util.Calendar.MONTH) == stMon) {
            java.util.Calendar stTime = getDayLightTransmition(stElement);
            stTime.set(java.util.Calendar.YEAR, calendar.get(java.util.Calendar.YEAR));
            return calendar.before(stTime);
        } else {
            return false;
        }
    }

    private java.util.Calendar getDayLightTransmition(Element dlElement) {
        String mday;
        java.util.Calendar dlTime = java.util.Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
        dlTime.set(java.util.Calendar.MONTH, Integer.parseInt(dlElement.attributeValue("mon")) - 1);
        mday = dlElement.attributeValue("mday");
        if (mday == null) {
            dlTime.set(java.util.Calendar.WEEK_OF_MONTH,
                       Integer.parseInt(dlElement.attributeValue("week")));
            dlTime.set(java.util.Calendar.DAY_OF_WEEK,
                       Integer.parseInt(dlElement.attributeValue("wkday")));
        } else {
            dlTime.set(java.util.Calendar.DAY_OF_MONTH, Integer.parseInt(mday));
        }
        dlTime.set(java.util.Calendar.HOUR_OF_DAY,
                   Integer.parseInt(dlElement.attributeValue("hour")));
        dlTime.set(java.util.Calendar.MINUTE, Integer.parseInt(dlElement.attributeValue("min")));
        dlTime.set(java.util.Calendar.SECOND, Integer.parseInt(dlElement.attributeValue("sec")));
        return dlTime;
    }

    public java.util.Calendar getLocalDate(String datetime, int tzo) {
        datetime = datetime.replace("-", "");
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        try {
            if (datetime.length() == 8) {
                datetime = datetime + "T000000" + (tzo == 0 ? "Z" : "");
            }
            if (datetime.length() < 9) {
                int year = Integer.parseInt(datetime.substring(0, 4));
                int month = Integer.parseInt(datetime.substring(4, 6));
                int date = Integer.parseInt(datetime.substring(6, 8));
                calendar.set(year, month - 1, date, 0, 0, 0);
                return calendar;
            }
            if (datetime.charAt(8) == 'T') {
                int year = Integer.parseInt(datetime.substring(0, 4));
                int month = Integer.parseInt(datetime.substring(4, 6));
                int date = Integer.parseInt(datetime.substring(6, 8));
                int hourOfDay = Integer.parseInt(datetime.substring(9, 11));
                int minute = Integer.parseInt(datetime.substring(11, 13));
                int second = Integer.parseInt(datetime.substring(13, 15));
                calendar.set(year, month - 1, date, hourOfDay, minute, second);
                if (datetime.length() == 16 && datetime.charAt(15) == 'Z') {
                    return calendar;
                }
                long time = calendar.getTimeInMillis();
                time -= 1000L * 60 * tzo;
                TimeZone UTC = new SimpleTimeZone(0, "UTC");
                calendar.setTimeZone(UTC);
                calendar.setTimeInMillis(time);
            } else
                throw new RuntimeException("Unexpected value: " + datetime);
        } catch (Throwable ex) {
            throw new RuntimeException("Unexpected value: " + datetime, ex);
        }
        return calendar;
    }

    public java.util.Calendar getLocalDate(String datetime, long tzo) {
        datetime = datetime.replace("-", "");
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        try {
            if (datetime.length() == 8) {
                datetime = datetime + "T000000" + (tzo == 0 ? "Z" : "");
            }
            if (datetime.length() < 9) {
                int year = Integer.parseInt(datetime.substring(0, 4));
                int month = Integer.parseInt(datetime.substring(4, 6));
                int date = Integer.parseInt(datetime.substring(6, 8));
                calendar.set(year, month - 1, date, 0, 0, 0);
                return calendar;
            }
            if (datetime.charAt(8) == 'T') {
                int year = Integer.parseInt(datetime.substring(0, 4));
                int month = Integer.parseInt(datetime.substring(4, 6));
                int date = Integer.parseInt(datetime.substring(6, 8));
                int hourOfDay = Integer.parseInt(datetime.substring(9, 11));
                int minute = Integer.parseInt(datetime.substring(11, 13));
                int second = Integer.parseInt(datetime.substring(13, 15));
                calendar.set(year, month - 1, date, hourOfDay, minute, second);
                if (datetime.length() == 16 && datetime.charAt(15) == 'Z') {
                    return calendar;
                }
                long time = calendar.getTimeInMillis();
                time -= tzo;
                TimeZone UTC = new SimpleTimeZone(0, "UTC");
                calendar.setTimeZone(UTC);
                calendar.setTimeInMillis(time);
            } else
                throw new RuntimeException("Unexpected value: " + datetime);
        } catch (Throwable ex) {
            throw new RuntimeException("Unexpected value: " + datetime, ex);
        }
        return calendar;
    }

    private String formatCallendar(java.util.Calendar calendar) {
        int year;
        int month;
        int date;
        int hourOfDay;
        int minute;
        int second;
        year = calendar.get(java.util.Calendar.YEAR);
        month = calendar.get(java.util.Calendar.MONTH) + 1;
        date = calendar.get(java.util.Calendar.DAY_OF_MONTH);
        hourOfDay = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        minute = calendar.get(java.util.Calendar.MINUTE);
        second = calendar.get(java.util.Calendar.SECOND);
        Formatter res = new Formatter().format("%04d%02d%02dT%02d%02d%02dZ",
                                               year,
                                               month,
                                               date,
                                               hourOfDay,
                                               minute,
                                               second);
        return res.toString();
    }

    private Object map(Map map, Object key) {
        if (key == null)
            return null;
        Object value = map.get(key);
        if (value == null) {
            FunambolLogger log = FunambolLoggerFactory.getLogger("funambol.zimbra");
            String mapDesc;
            if (map == busyStatus) {
                mapDesc = "busyStatus";
            } else if (map == busyStatus_) {
                mapDesc = "busyStatus_";
            } else if (map == roles) {
                mapDesc = "roles";
            } else if (map == roles_) {
                mapDesc = "roles_";
            } else if (map == participStatus) {
                mapDesc = "participStatus";
            } else if (map == participStatus_) {
                mapDesc = "participStatus_";
            } else if (map == accessClasses) {
                mapDesc = "accessClasses";
            } else if (map == accessClasses_) {
                mapDesc = "accessClasses_";
            } else if (map == apptStatus) {
                mapDesc = "statusStates";
            } else if (map == apptStatus_) {
                mapDesc = "statusStates_";
            } else if (map == taskStatus) {
                mapDesc = "taskStatus";
            } else if (map == taskStatus_) {
                mapDesc = "taskStatus_";
            } else {
                mapDesc = map.toString();
            }
            log.warn("Try to map the " + mapDesc + " , but it doesn't contain key=" + key);
        }
        return value;
    }

    public static String getSubject(Calendar calendar) {
        return calendar.getCalendarContent().getSummary().getPropertyValueAsString();
    }
}
