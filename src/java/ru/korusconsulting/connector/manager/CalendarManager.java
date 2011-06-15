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
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.dom4j.Element;

import ru.korusconsulting.connector.base.ZConst;
import ru.korusconsulting.connector.exceptions.SoapRequestException;
import ru.korusconsulting.connector.funambol.CalendarUtils;

import com.funambol.common.pim.calendar.Calendar;
import com.funambol.common.pim.calendar.CalendarContent;
import com.funambol.common.pim.calendar.Task;
import com.funambol.common.pim.common.ConversionException;
import com.funambol.framework.engine.SyncItemKey;
import com.funambol.framework.engine.source.SyncSourceException;
import com.funambol.framework.tools.merge.MergeResult;

public class CalendarManager extends Manager<Calendar> {
    public static final String A_CALENDAR_ID = "id";
    private boolean task = false;
    // fullCalendarRequest use with Twins search, I can't find another way
    // Request by id each calendar and check if is't twins
    private boolean fullCalendarRequest = false;
    private ArrayList<Calendar> calendars = null;
    private HashMap<String, Calendar> calendarscache = new HashMap<String, Calendar>();
    private CalendarHash[] calendarHash = null;
    private long lastToken;
    private Element syncResponse = null;
    private ArrayList<String> servedItems;
    private TimeZone phoneTimeZone;

    public void removeAllItems() throws IOException, SoapRequestException {
        zimbraPort.requestRemoveAllItems(itemsResponse, A_CALENDAR_ID);
        itemsResponse.elements().retainAll(new ArrayList<Element>());// remove
                                                                        // all
    }

    public void requestItemsForSync() throws IOException, SoapRequestException {
        itemsResponse = zimbraPort.requestAllCalendarsIds(task);
        syncResponse = zimbraPort.requestSyncronization(String.valueOf(lastToken));
    }

    public ArrayList<Calendar> getAllCalendars() throws IOException, SoapRequestException, ConversionException{
        ArrayList<String> ids=new ArrayList<String>();
        for (Iterator iterator = itemsResponse.elementIterator(); iterator.hasNext();) {
            Element cn = (Element) iterator.next();
            String id = extractKeyFromElement(cn);
            ids.add(id);
        }
        calendars = zimbraPort.requestAllCalendars(ids, task, phoneTimeZone);

        fullCalendarRequest = true;
        return calendars;

    }

    public void determineItemsState(Timestamp since) {
        if (prepared)
            return;
        prepared = true;

        if (this.logger.isDebugEnabled()) {
        	this.logger.debug("CalendarManager::determineItemsState");
        	this.logger.debug("syncResponse:" + syncResponse.asXML());
        	this.logger.debug("servedItems:" + servedItems);
        }
        Element deletedEl = syncResponse.element(ZConst.E_DELETED);
        String[] saved = servedItems.toArray(new String[0]);


        if (deletedEl != null) {
            String ids = deletedEl.attributeValue(ZConst.A_IDS);
            delItems.addAll(Arrays.asList(ids.split(",")));
            //filterItems(saved, ids.split(","));
        }
        for (Iterator iterator = itemsResponse.elementIterator(); iterator.hasNext();) {
            Element cn = (Element) iterator.next();
            String id = extractKeyFromElement(cn);
            long modTime = Long.parseLong(cn.attributeValue("md")) * 1000;
            if (this.logger.isDebugEnabled()) {
            	this.logger.debug("allItems ->:" + id);
            }
            allItems.add(id);

            if (since != null && modTime > since.getTime()) {
                boolean finded = false;
                for (int i = 0; i < saved.length; i++) {
                    if (saved[i] == null)
                        continue;
                    if (id.equals(saved[i])) {
                        // bingo, it was modification, else we wouldn't find
                        // calendar id
                        if (this.logger.isDebugEnabled()) {
                        	this.logger.debug("updItems ->:" + id);
                        }
                        updItems.add(id);
                        // erase elem, alter all all non erased element will be
                        // deleted
                        saved[i] = null;
                        finded = true;
                        break;
                    }
                }
                if (!finded) {
                    if (this.logger.isDebugEnabled()) {
                    	this.logger.debug("newItems ->:" + id);
                    }
                    newItems.add(id);
                }
            }
            else{
                for (int i = 0; i < saved.length; i++) {
                    if (saved[i] == null)
                        continue;
                    if (id.equals(saved[i])) {
                        if (this.logger.isDebugEnabled()) {
                        	this.logger.debug("no change ->:" + id);
                        }
                        // erase elem, alter all all non erased element will be
                        // deleted
                        saved[i] = null;
                        break;
                    }
                }
            }
        }
        for (int i = 0; i < saved.length; i++) {
            if (saved[i] == null)
                continue;
            int index=delItems.indexOf(saved[i]);
            if(index==-1){
                if (this.logger.isDebugEnabled()) {
                	this.logger.debug("delItems ->:" + saved[i]);
                }
                delItems.add(saved[i]);
            }
        }


    }

    private void filterItems(String[] filter, String[] ids) {
        for (int j = 0; j < ids.length; j++) {
            String key = null;
            // long searchKey = -1;
            for (int i = 0; i < filter.length; i++) {
                if (filter[i] == null)
                    continue;
                int idx = filter[i].indexOf('-');
                String part1 = filter[i];
                if (idx != -1) {
                    part1 = filter[i].substring(0, idx);
                }
                if (ids[j].equals(part1)) {
                    // Long part2Long =
                    // Long.valueOf(filter[i].substring(idx+1));
                    // if we have 4520-4519 and 4520-4521 we should take more
                    // later id its 4520-4521
                    // if(searchKey<part2Long){
                    key = filter[i];
                    // searchKey=part2Long;
                    // filter[i] = null;
                    // }
                    delItems.add(key);
                }
            }
            // if(key!=null){
            // delItems.add(key);
            // }
        }
    }

    public SyncItemKey[] getTwins(Calendar calendar) {
        ArrayList<String> twins = new ArrayList<String>();

        try {
            CalendarContent cc = calendar.getCalendarContent();
            String subject = getValue(cc.getSummary());

            java.util.Calendar dtStart = null;
            java.util.Calendar dtEnd = null;
            Date startDate = null;
            Date endDate = null;
            Date startTomorrowNoon = null;
            Date startYesterdayNoon = null;
            Date dueTomorrowNoon = null;
            Date dueYesterdayNoon = null;
            boolean isAllDay = cc.isAllDay();

            String dtStartStr = getValue(cc.getDtStart());
            if (dtStartStr != null)
                dtStart = CalendarUtils.getInstance().getLocalDate(dtStartStr, (long) phoneTimeZone.getRawOffset());

            String dtEndStr = getValue(cc.getDtEnd());
            if (dtEndStr != null) {
                dtEnd = CalendarUtils.getInstance().getLocalDate(dtEndStr, (long) phoneTimeZone.getRawOffset());
                if (cc.isAllDay()) {
                    dtEnd.set(java.util.Calendar.HOUR_OF_DAY, 23);
                    dtEnd.set(java.util.Calendar.MINUTE, 59);
                    dtEnd.set(java.util.Calendar.SECOND, 00);
                }
            }

            if ((dtStart != null) && (cc instanceof Task)) {
                java.util.Calendar noon = (java.util.Calendar) dtStart.clone();
                noon.add(java.util.Calendar.DATE, +1);
                startTomorrowNoon = noon.getTime();
                noon.add(java.util.Calendar.DATE, -2); // go back and another
                                                        // -1
                startYesterdayNoon = noon.getTime();
            }

            if ((dtEnd != null) && (cc instanceof Task)) {
                java.util.Calendar noon = (java.util.Calendar) dtEnd.clone();
                noon.add(java.util.Calendar.DATE, +1);
                dueTomorrowNoon = noon.getTime();
                noon.add(java.util.Calendar.DATE, -2); // go back and another
                                                        // -1
                dueYesterdayNoon = noon.getTime();
            }

            startDate = dtStart == null ? null : dtStart.getTime();
            endDate = dtEnd == null ? null : dtEnd.getTime();

            if (!fullCalendarRequest) {
                // Can be performance issue with request all calendars
                // but in this case we can quick return calendar instance
                // if their not exist on the phone
                calendars = zimbraPort.requestAllCalendars(allItems, task, phoneTimeZone);
                // I'm create this hash because this function will be call many
                // times
                // Exactly N times, where N - count of client calendars
                calendarHash = new CalendarHash[calendars.size()];
                fullCalendarRequest = true;
            }

            if (this.logger.isDebugEnabled()) {
            	this.logger.debug("Start twin matching on: \nSOURCE:"
                		+ " allDay: " + Boolean.toString(cc.isAllDay())
                		+ " isTask: " + Boolean.toString(cc instanceof Task)
                		+ " subject: " + subject
                		+ " startDate: " + startDate.toString()
                		+ " endDate: " + endDate.toString()
                		+ " timezone phone: " + phoneTimeZone.getDisplayName()
                		+ " (" + Integer.toString(phoneTimeZone.getRawOffset()) + ")"
                		);
            }
            for (int i = 0; i < calendars.size(); i++) {
                boolean twin = true;
                CalendarHash hash;
                if (calendarHash[i] == null) {
                    Calendar servCal = calendars.get(i);
                    hash = getHash(servCal);
                    calendarHash[i] = hash;
                } else {
                    hash = calendarHash[i];
                }

                twin &= subject == null ? hash.subject == null : subject.equalsIgnoreCase(hash.subject);
                twin &= isAllDay == hash.allDay;

                if (startDate == null) {
                    twin &= hash.startDate == null;
                } else {
                    twin &= hash.startDate != null;
                    if (hash.startDate != null) {
                        if (cc instanceof Task) {
                            twin &= hash.startDate.getTime() > startYesterdayNoon.getTime()
                                    & hash.startDate.getTime() < startTomorrowNoon.getTime();
                        } else {
                            twin &= startDate.getTime() == hash.startDate.getTime();
                        }
                    }
                }

                if (endDate == null) {
                    twin &= hash.endDate == null;
                } else {
                    twin &= hash.endDate != null;
                    if (hash.endDate != null) {
                        if (cc instanceof Task) {
                            twin &= hash.endDate.getTime() > dueYesterdayNoon.getTime()
                                    & hash.endDate.getTime() < dueTomorrowNoon.getTime();
                        } else {
                            twin &= endDate.getTime() == hash.endDate.getTime();
                        }
                    }
                }

                if (this.logger.isDebugEnabled()) {
                	this.logger.debug("\tis twin matching on: \nTARGET:"
                    		+ " allDay: " + Boolean.toString(hash.allDay)
                    		+ " isTask: " + Boolean.toString(cc instanceof Task)
                    		+ " subject: " + hash.subject
                    		+ " startDate: " + hash.startDate.toString()
                    		+ " endDate: " + hash.endDate.toString()
                    		+ " timezone item: " + ((hash.timezone == null) ? null : hash.timezone.getDisplayName())
                    		+ " (" + ((hash.timezone == null) ? null : Integer.toString(hash.timezone.getRawOffset())) + ")"
                    		+ " = twin: " + Boolean.toString(twin)
                    		);
                }

                if (twin) {
                    twins.add(allItems.get(i));
                }
            }
        } catch (Throwable e) {
        	this.logger.error("While try to find twins, error occur ", e);
        }
        return listAsKeyArray(twins);
    }

    private CalendarHash getHash(Calendar servCal) {
        CalendarHash hash = new CalendarHash();
        CalendarContent servcc = servCal.getCalendarContent();
        hash.subject = getValue(servcc.getSummary()).trim();
        hash.allDay = servcc.isAllDay();

        java.util.Calendar dtStart = null;
        java.util.Calendar dtEnd = null;

        hash.timezone = null;
        String dtStartStr = null;
        String dtEndStr = null;

        try {
        	hash.timezone = TimeZone.getTimeZone(servcc.getDtStart().getTimeZone());
        } catch (NullPointerException e) {
        	hash.timezone = null;
        }

        if (hash.timezone != null) {
        	long tzoffset = (long) hash.timezone.getRawOffset();

        	dtStartStr = getValue(servcc.getDtStart());
            if (dtStartStr != null)
                dtStart = CalendarUtils.getInstance().getLocalDate(dtStartStr, tzoffset);
            dtEndStr = getValue(servcc.getDtEnd());
            if (dtEndStr != null) {
                dtEnd = CalendarUtils.getInstance().getLocalDate(dtEndStr, tzoffset);
                if (servcc.isAllDay()) {
                    dtEnd.set(java.util.Calendar.HOUR_OF_DAY, 23);
                    dtEnd.set(java.util.Calendar.MINUTE, 59);
                    dtEnd.set(java.util.Calendar.SECOND, 00);
                }
            }
        } else {
        	dtStartStr = getValue(servcc.getDtStart());
            if (dtStartStr != null)
                dtStart = CalendarUtils.getInstance().getLocalDate(dtStartStr, 0);
            dtEndStr = getValue(servcc.getDtEnd());
            if (dtEndStr != null) {
                dtEnd = CalendarUtils.getInstance().getLocalDate(dtEndStr, 0);
                if (servcc.isAllDay()) {
                    dtEnd.set(java.util.Calendar.HOUR_OF_DAY, 23);
                    dtEnd.set(java.util.Calendar.MINUTE, 59);
                    dtEnd.set(java.util.Calendar.SECOND, 00);

                }
            }
        }

        hash.startDate = dtStart == null ? null : dtStart.getTime();
        hash.endDate = dtEnd == null ? null : dtEnd.getTime();

        return hash;
    }

    @Override
    public String addItem(Calendar calendar) throws IOException, SoapRequestException,
            SyncSourceException {
        Element createdCalendar = zimbraPort.requestCreateCalendar(calendar);
        String key = createdCalendar.attributeValue("invId");
        if (key == null)
            throw new SyncSourceException("Key can't be null");
        int idx = key.indexOf('-');
        if (idx != -1) {
            key = key.substring(0, idx);
        }
        return key;
    }

    @Override
    public Calendar getItem(String key, Object... args) throws IOException, SoapRequestException {
        int index = allItems.indexOf(key);
        if (index == -1)
            return null;
        Calendar calendar = null;
        boolean getfromzimbra = false;

        try {
        	calendar = calendarscache.get(key);

        	if (calendar == null) {
        		getfromzimbra = true;
        	}
        } catch (Exception e) {
        	getfromzimbra = true;
        }

        if (getfromzimbra) {
            calendar = zimbraPort.requestCalendarById(key, task, phoneTimeZone);

            calendarscache.put(key, calendar);
        }

        return calendar;
    }

    @Override
    public boolean mergeItems(String key, Calendar clientCalendar) throws IOException,
            SoapRequestException, SyncSourceException {
        Calendar serverContact = zimbraPort.requestCalendarById(key, task, phoneTimeZone);
        MergeResult mergeResult = clientCalendar.merge(serverContact);
        if (mergeResult.isSetBRequired()) {
            updItem(key, serverContact);
        }
        return mergeResult.isSetARequired();
    }

    @Override
    public String updItem(String key, Calendar calendar) throws IOException, SoapRequestException,
            SyncSourceException {
        String oldFolderId = null;
        String oldTags = null;
        String invId = null;
        List<Element> items = itemsResponse.elements();
        for (int i = 0; i < items.size(); i++) {
            Element calEl = (Element) items.get(i);
            if (calEl.attributeValue(A_CALENDAR_ID).equals(key)) {
                oldFolderId = calEl.attributeValue(ZConst.A_FOLDER);
                oldTags = calEl.attributeValue(ZConst.A_TAG);
                invId = calEl.attributeValue("invId");
            }
        }
        if (invId == null) {
            // can't find contact with key
            // we have to create Contact
            key = addItem(calendar);
        } else {
            //zimbraPort.requestDeleteItem(invId, false);
            //key = addItem(calendar);
            zimbraPort.requestModifyCalendar(invId, calendar, oldFolderId,
             oldTags);
        }
        return key;
    }

    public String extractKeyFromElement(Element element) {
        return (String) element.attributeValue(A_CALENDAR_ID);
    }

    public boolean isTask() {
        return task;
    }

    public void setTask(boolean task) {
        this.task = task;
    }

    private static class CalendarHash {
        public String subject;
        public Date startDate;
        public Date endDate;
        public boolean allDay;
        public TimeZone timezone;
    }

    public long getLastToken() {
        return lastToken;
    }

    public void setLastToken(long lastToken) {
        this.lastToken = lastToken;
    }

    public ArrayList<String> getServedItems() {
        return servedItems;
    }

    public void setServedItems(ArrayList<String> servedItems) {
        this.servedItems = servedItems;
    }

    public TimeZone getPhoneTimeZone() {
        return phoneTimeZone;
    }

    public void setPhoneTimeZone(TimeZone phoneTimeZone) {
        this.phoneTimeZone = phoneTimeZone;
    }
}
