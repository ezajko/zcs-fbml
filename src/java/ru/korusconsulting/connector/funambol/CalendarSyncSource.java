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

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

import ru.korusconsulting.connector.db.UserTokenDao;
import ru.korusconsulting.connector.exceptions.SoapRequestException;
import ru.korusconsulting.connector.manager.CalendarManager;

import com.funambol.common.pim.calendar.Calendar;
import com.funambol.common.pim.common.Property;
import com.funambol.framework.engine.SyncItem;
import com.funambol.framework.engine.SyncItemImpl;
import com.funambol.framework.engine.SyncItemKey;
import com.funambol.framework.engine.SyncItemState;
import com.funambol.framework.engine.source.SyncContext;
import com.funambol.framework.engine.source.SyncSourceException;

public class CalendarSyncSource extends ZimbraSyncSource {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4317848139346995376L;
	transient private CalendarManager manager = new CalendarManager();
    transient boolean task = false;
    transient private UserTokenDao userTokenDao;
    transient private ArrayList<String> servedItems;
    transient private long lastToken;

    public CalendarSyncSource() {
    }

    public void beginSync(SyncContext context) throws SyncSourceException {
        manager.setTask(task);
        super.beginSync(context);
    }
    
    @Override
    protected void commonSync() throws SyncSourceException {
        try {
            userTokenDao = new UserTokenDao(config.getDataSource());

            servedItems = userTokenDao.getClientMapping(principal.getId(), getSourceURI());
            lastToken = userTokenDao.getToken(principal.getId());
        } catch (Throwable e) {
            logger.error("Error occur while using userTokenDao, using datasource:"
                    + config.getDataSource(), e);
            throw new SyncSourceException(e);
        }
        manager.setLastToken(lastToken);
        manager.setServedItems(servedItems);
        manager.setPhoneTimeZone(getTimeZone());
        super.commonSync();
    }

    @SuppressWarnings("unchecked")
    public SyncItemKey[] getAllSyncItemKeys() throws SyncSourceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Get All Sync Item exclude deleted");
        }
        try {
            manager.determineItemsState(null);
            SyncItemKey[] allKeys = manager.getAllItems();
            if (logger.isDebugEnabled()) {
                logger.debug("All existing item (exclude deleted): " + toString(allKeys));
            }
            return allKeys;
        } catch (Throwable e) {
            logger.error("", e);
            throw new SyncSourceException(e);
        }
    }

    public SyncItemKey[] getNewSyncItemKeys(Timestamp since, Timestamp until)
            throws SyncSourceException {
        logger.debug("In getNewSyncItemKeys()...");
        since = lastSync == null ? since : lastSync;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Try to find a created sync items by " + since.getTime() + "/"
                        + until.getTime());
            }
            manager.determineItemsState(since);
            SyncItemKey[] created = manager.getCreatedItems();
            if (logger.isDebugEnabled()) {
                logger.debug("Connector find a new items with keys: " + toString(created));
            }
            return created;
        } catch (Throwable e) {
            logger.error("", e);
            throw new SyncSourceException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public SyncItemKey[] getDeletedSyncItemKeys(Timestamp since, Timestamp until)
            throws SyncSourceException {
        logger.debug("In getDeletedSyncItemKeys()...");
        since = lastSync == null ? since : lastSync;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Try to find a deleted sync items by " + since.getTime() + "/"
                        + until.getTime());
            }
//            Element response = zimbraPort.requestSyncronization(String.valueOf(lastToken));
//            Element deletedEl = response.element(ZConst.E_DELETED);
//            if (deletedEl == null)
//                return null;
//
//            String ids = deletedEl.attributeValue(ZConst.A_IDS);
//            SyncItemKey[] deleted = filterItems(servedItems.toArray(new String[0]), ids.split(","));
            manager.determineItemsState(since);
            SyncItemKey[] deleted = manager.getDeletedItems();
            if (logger.isDebugEnabled()) {
                logger.debug("Connector find a deleted items with keys: " + toString(deleted));
            }
            return deleted;
        } catch (Throwable e) {
            logger.error("", e);
            throw new SyncSourceException(e);
        }
    }

    
    public SyncItemKey[] getUpdatedSyncItemKeys(Timestamp since, Timestamp until)
            throws SyncSourceException {
        logger.debug("In getUpdatedSyncItemKeys()...");
        since = lastSync == null ? since : lastSync;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Try to find a updated sync items by " + since.getTime() + "/"
                        + until.getTime());
            }
            SyncItemKey[] updated = manager.getUpdatedItems();
            if (logger.isDebugEnabled()) {
                logger.debug("Connector find a updated items with keys: " + toString(updated));
            }
            return updated;
        } catch (Throwable e) {
            logger.error("", e);
            throw new SyncSourceException(e);
        }
    }

    public SyncItem getSyncItemFromId(SyncItemKey itemKey) throws SyncSourceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Try to get SyncItem by Id '" + itemKey.getKeyAsString() + "'");
        }
        try {
            Calendar calendar = manager.getItem(itemKey.getKeyAsString());
            SyncItem item = new SyncItemImpl(this, itemKey, SyncItemState.NEW);
            PhoneDependedConverter.getInstance().setContent(item, calendar, this);
            if (logger.isDebugEnabled()) {
                logger.debug("Recieved the sync item '" + item.getKey().getKeyAsString() + "':"
                        + item);
            }
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
        Calendar calendar;
        try {
            calendar = PhoneDependedConverter.getInstance().getCalendar(item, this);
            SyncItemKey[] twins = manager.getTwins(calendar);
            if (logger.isDebugEnabled()) {
                if (twins != null && twins.length > 0)
                    logger.debug("The twin's is zimbra calendar with id='"
                            + item.getKey().getKeyAsString() + "'=" + toString(twins));
                else
                    logger.debug("Can't find any twins for '" + item.getKey().getKeyAsString()
                            + "'");
            }
            return twins;
        } catch (Throwable e) {
            if (logger.isInfoEnabled()) {
                logger.error("Error occur then search twis: " + new String(item.getContent()));
            }
            logger.error("", e);
            throw new SyncSourceException(e);
        }
    }

    public SyncItem addSyncItem(SyncItem item) throws SyncSourceException {
        String keyAsString = item.getKey().getKeyAsString();
        if (logger.isDebugEnabled()) {
            logger.debug("Try to addSyncItem '" + keyAsString + "':" + item);
        }
        SyncItemImpl newSyncItem;
        Calendar calendar;
        try {
            calendar = PhoneDependedConverter.getInstance().getCalendar(item, this);
            keyAsString = manager.addItem(calendar);
            newSyncItem = new SyncItemImpl(this,
                                           keyAsString,
                                           null,
                                           SyncItemState.NEW,
                                           item.getContent(),
                                           null,
                                           item.getType(),
                                           null);
        } catch (Throwable e) {
            if (logger.isInfoEnabled()) {
                logger.error("Error occur then add synitem: " + new String(item.getContent()));
            }
            logger.error("", e);
            throw new SyncSourceException(e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("addSyncItem complete for '" + keyAsString + "':" + newSyncItem);
        }
        return newSyncItem;
    }

    public void removeSyncItem(SyncItemKey itemKey, Timestamp arg1, boolean soft)
            throws SyncSourceException {
        logger.debug("In removeSyncItem()...");
        try {
            manager.delItem(itemKey.getKeyAsString(), soft);
        } catch (Throwable e) {
            logger.error("", e);
            throw new SyncSourceException(e);
        }
    }

    public SyncItem updateSyncItem(SyncItem item) throws SyncSourceException {
        String keyAsString = item.getKey().getKeyAsString();
        if (logger.isDebugEnabled()) {
            logger.debug("updateSyncItem::Try to updateSyncItem '" + keyAsString + "':" + item);
        }
        if (context.getConflictResolution() == SyncContext.CONFLICT_RESOLUTION_CLIENT_WINS) {
            Calendar calendar;
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("updateSyncItem::Conflict Resolution - client wins ");
                }

                calendar = PhoneDependedConverter.getInstance().getCalendar(item, this);
                Property prop = calendar.getCalendarContent().getUid();
                if (prop == null)
                    calendar.getCalendarContent().setUid(new Property(keyAsString));
                keyAsString = manager.updItem(keyAsString, calendar);
                SyncItemImpl newSyncItem = new SyncItemImpl(this,
                                                            keyAsString,
                                                            null,
                                                            SyncItemState.UPDATED,
                                                            item.getContent(),
                                                            null,
                                                            item.getType(),
                                                            null);

                if (logger.isDebugEnabled()) {
                    logger.debug("updateSyncItem::Updated sync Item '" + keyAsString + "':"
                            + newSyncItem);
                }
                return newSyncItem;
            } catch (Throwable e) {
                if (logger.isInfoEnabled()) {
                    logger.error("Error occur then add synitem: " + new String(item.getContent()));
                }
                logger.error("", e);
                throw new SyncSourceException(e);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("updateSyncItem::Conflict Resolution - server wins, Should recive contact from server ");
            }
            SyncItem newSyncItem;
            newSyncItem = getSyncItemFromId(item.getKey());
            newSyncItem.setState(SyncItemState.UPDATED);
            newSyncItem.setType(item.getType());
            return newSyncItem;
        }
    }

    public boolean mergeSyncItems(SyncItemKey syncItemKey, SyncItem item)
            throws SyncSourceException {
        Calendar calendar;
        String keyAsString = syncItemKey.getKeyAsString();
        if (logger.isDebugEnabled()) {
            logger.debug("mergeSyncItems::Try to merge contacts '" + keyAsString + "':" + item);
        }
        try {
            calendar = PhoneDependedConverter.getInstance().getCalendar(item, this);
            boolean clientUpdateRequired = manager.mergeItems(keyAsString, calendar);
            if (clientUpdateRequired) {
                item = getSyncItemFromId(syncItemKey);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("mergeSyncItems:: complete for '" + keyAsString + "':" + item);
            }
            return clientUpdateRequired;
        } catch (Throwable e) {
            if (logger.isInfoEnabled()) {
                logger.error("Error occur then add synitem: " + new String(item.getContent()));
            }
            logger.error("", e);
            throw new SyncSourceException(e);
        }

    }

    public void endSync() throws SyncSourceException {
        // if zimbraPort.getLastToken()==null then wasn't one itteraction with zimbra server 
        if(zimbraPort.getLastToken()!=null){
            lastToken = Long.valueOf(zimbraPort.getLastToken());
            try {
                userTokenDao.updateToken(principal.getId(), lastToken);
            } catch (Throwable e) {
                logger.error("cann't commit token", e);
            }
        }
        super.endSync();
    }

    @Override
    protected void removeAllSyncItems() throws SyncSourceException {
        try {
            manager.setZimbraPort(zimbraPort);
            manager.removeAllItems();
        } catch (Throwable e) {
            logger.error("", e);
            throw new SyncSourceException(e);
        }
    }

    @Override
    protected void requestAllItems() throws IOException, SoapRequestException {
        manager.setZimbraPort(zimbraPort);
        manager.requestItemsForSync();
    }

    public boolean isTask() {
        return task;
    }

    public void setTask(boolean task) {
        this.task = task;
    }

    @Override
    public void commitSync() throws SyncSourceException {
        super.commitSync();
    }

}
