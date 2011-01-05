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
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.sql.Timestamp;
import java.util.TimeZone;

import ru.korusconsulting.connector.base.FunambolConnector;
import ru.korusconsulting.connector.base.ZimbraPort;
import ru.korusconsulting.connector.config.ConnectorConfig;
import ru.korusconsulting.connector.exceptions.SoapRequestException;

import com.funambol.framework.core.AlertCode;
import com.funambol.framework.core.StatusCode;
import com.funambol.framework.engine.SyncItemKey;
import com.funambol.framework.engine.source.AbstractSyncSource;
import com.funambol.framework.engine.source.SyncContext;
import com.funambol.framework.engine.source.SyncSource;
import com.funambol.framework.engine.source.SyncSourceException;
import com.funambol.framework.logging.FunambolLogger;
import com.funambol.framework.logging.FunambolLoggerFactory;
import com.funambol.framework.security.Sync4jPrincipal;
import com.funambol.framework.server.LastTimestamp;
import com.funambol.framework.server.Sync4jUser;
import com.funambol.framework.server.store.NotFoundException;
import com.funambol.framework.server.store.PersistentStoreException;
import com.funambol.framework.tools.beans.BeanException;
import com.funambol.framework.tools.beans.BeanInitializationException;
import com.funambol.framework.tools.beans.LazyInitBean;
import com.funambol.server.config.Configuration;

public abstract class ZimbraSyncSource extends AbstractSyncSource implements SyncSource,
        Serializable, LazyInitBean {
    private String zimbraUrl = null;
    transient protected ZimbraPort zimbraPort;
    transient protected FunambolLogger logger = null;
    transient protected Sync4jPrincipal principal = null;
    protected String charset;
    transient protected SyncContext context;
    protected TimeZone timeZone;
    protected Timestamp lastSync;
    transient protected ConnectorConfig config;
    
    /**
     * Called by the engine to notify an operation status.
     * @param operationName the name of the operation.
     *        One between:
     *        - Add
     *        - Replace
     *        - Delete
     * @param status the status of the operation
     * @param keys the keys of the items
     */
    public void setOperationStatus(String operation, int statusCode, SyncItemKey[] keys) {
        StringBuffer message = new StringBuffer("[ZimbraSyncSource.setOperationStatus] Status:'");

        message.append(StatusCode.getStatusDescription(statusCode))
               .append("' for a '")
               .append(operation)
               .append("'")
               .append(" for this items: ");
        for (int i = 0; i < keys.length; i++) {
            message.append(", " + keys[i].getKeyAsString());
        }
        message.append(" (" + keys.toString() + ")");

        Throwable a = new Throwable();
        
        logger.info(message.toString());
        
        if (logger.isTraceEnabled()) {
        	//Stacktrace to String, see: http://www.javapractices.com/topic/TopicAction.do?Id=78
            Writer result = new StringWriter();
            PrintWriter printWriter = new PrintWriter(result);
            a.printStackTrace(printWriter);
            
            logger.trace(result.toString());
        }
    }

    /**
     * Called before any other synchronization method. To interrupt the sync
     * process, throw a SyncSourceException.
     *
     * @param syncContext the context of the sync.
     *
     * @see SyncContext
     *
     * @throws SyncSourceException to interrupt the process with an error
     */
    @Override
    public void beginSync(SyncContext syncContext) throws SyncSourceException {
        this.context = syncContext;
        logger.debug("!------------BEGIN SYNCHRONIZATION-------------!");
        if (getZimbraUrl() == null) {
            throw new SyncSourceException("The Zimbra URL Can't be null, FATAL Error occured");
        }
        this.principal = context.getPrincipal();
        String tz = principal.getDevice().getTimeZone();
        timeZone = tz != null ? TimeZone.getTimeZone(tz) : TimeZone.getDefault();
        charset = principal.getDevice().getCharset();
        
        if (logger.isDebugEnabled()) {
            logger.debug("Context's username: " + context.getPrincipal().getUsername());
            logger.debug("Context sync mode: " + context.getSyncMode());
            logger.debug("Context conflict resolution: " + context.getConflictResolution());
            logger.debug("Context query: " + context.getSourceQuery());
            logger.debug("Context filter clause: " + context.getFilterClause());
            logger.debug("Device's Id: " + principal.getDeviceId());

            logger.debug("Device's charset: " + charset);
            logger.debug("Device's timezone: " + timeZone);
        }
        commonSync();
        
        super.beginSync(syncContext);
    }

    /**
     * In this stage determine common variables for base class and children classes can use custom initialize 
     * @throws SyncSourceException
     */
    protected void commonSync() throws SyncSourceException {
        int syncMode = context.getSyncMode();
        try {
            // I think the best approach will be if we will create pool of our
            // connections
            zimbraPort = new ZimbraPort(new URL(getZimbraUrl()));
            Sync4jUser user = ((Sync4jPrincipal) principal).getUser();
            try{
                zimbraPort.requestAutorization(user);
            }catch (IOException e) {
                logger.error("I/O Error ", e);
                throw new SyncSourceException("Access denied, check userName and Password", StatusCode.INVALID_CREDENTIALS);
            }
            requestAllItems();
        } catch (java.io.FileNotFoundException e) {
            logger.error("I/O Error ", e);
            throw new SyncSourceException("Zimbra Server not found from url:"+getZimbraUrl(), StatusCode.SERVICE_UNAVAILABLE);
        }catch (IOException e) {
            logger.error("I/O Error ", e);
            throw new SyncSourceException(e);
        } catch (SoapRequestException e) {
            logger.error("", e);
            throw new SyncSourceException(e);
        }

        if (syncMode == AlertCode.REFRESH_FROM_CLIENT) {
            if (logger.isDebugEnabled()) {
                logger.debug("Performing REFRESH_FROM_CLIENT (203)");
            }
            removeAllSyncItems();
        }
        
        //TODO: we are doing nothing with lastSync, so why set it?
       /* LastTimestamp last = new LastTimestamp(principal.getId(), getSourceURI());
        try {
            Configuration.getConfiguration().getStore().read(last);
        } catch (NotFoundException e) {
            last.end = System.currentTimeMillis();
        } catch (PersistentStoreException e) {
            logger.error("", e);
            throw new SyncSourceException("Error reading last timestamp", e);
        }
        lastSync = new Timestamp(last.end);*/
    }

    
    
    protected abstract void requestAllItems() throws IOException, SoapRequestException;

    protected abstract void removeAllSyncItems() throws SyncSourceException;

    public String getZimbraUrl() {
        return zimbraUrl;
    }

    public void setZimbraUrl(String zimbraUrl) {
        this.zimbraUrl = zimbraUrl;
    }
    
    /**
     * Commits the changes applied during the sync session. If the underlying
     * datastore can not commit the changes, a SyncSourceException is thrown.
     *
     * @throws SyncSourceException if the changes cannot be committed
     */
    public void commitSync() throws SyncSourceException {
        logger.debug("!------------ COMMIT SYNC --------------------!");
        // if(lastSync!=null){
        // logger.debug("Commit Sync with time: "+ System.currentTimeMillis());
        // lastSync.setTime(System.currentTimeMillis());
        // }
        super.commitSync();
    }

    /**
     * Called after the modifications have been applied.
     *
     * @throws SyncSourceException to interrupt the process with an error
     */
    public void endSync() throws SyncSourceException {
        logger.debug("!------------END SYNCHRONIZATION-------------!");
        if (zimbraPort != null)
            zimbraPort.close();
        context = null;

        super.endSync();
    }

    public String getCharset() {
        return charset;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public Sync4jPrincipal getPrincipal() {
        return principal;
    }

	/**
	 * Returns a timestamp aligned to UTC
	 */
	protected Timestamp normalizeTimestamp(Timestamp t) {
		return new Timestamp(t.getTime()
				- getTimeZone().getOffset(t.getTime()));
	}

    public void init() throws BeanInitializationException {
        logger = FunambolLoggerFactory.getLogger("funambol.zimbra");
        logger.info("LOAD ZimbraConnector " + FunambolConnector.getInstance().getVersion());
        logger.debug("Source URI: " + getSourceURI());
        try {
            config = ConnectorConfig.getConfigInstance();
        } catch (BeanException e) {
            logger.error("", e);
            throw new BeanInitializationException("Can't initialize bean, because can't get config instance");
        }
    }

    protected static String toString(SyncItemKey[] keys) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < keys.length; i++) {
            sb.append(keys[i].getKeyAsString());
            if (i != keys.length - 1) {
                sb.append(",");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    public ConnectorConfig getConfig() {
        return config;
    }

}
