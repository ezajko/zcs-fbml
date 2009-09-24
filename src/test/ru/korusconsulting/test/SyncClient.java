package ru.korusconsulting.test;

import com.funambol.syncclient.common.logging.Logger;
import com.funambol.syncclient.spdm.DMException;
import com.funambol.syncclient.spds.SyncException;
import com.funambol.syncclient.spds.SyncManager;
import com.funambol.syncclient.spds.event.SyncEvent;
import com.funambol.syncclient.spds.event.SyncItemEvent;
import com.funambol.syncclient.spds.event.SyncItemListener;
import com.funambol.syncclient.spds.event.SyncListener;
import com.funambol.syncclient.spds.event.SyncSourceEvent;
import com.funambol.syncclient.spds.event.SyncSourceListener;
import com.funambol.syncclient.spds.event.SyncStatusEvent;
import com.funambol.syncclient.spds.event.SyncStatusListener;
import com.funambol.syncclient.spds.event.SyncTransportEvent;
import com.funambol.syncclient.spds.event.SyncTransportListener;

/**
 * The main window for the SyncClient Demo GUI.
 *
 * @version $Id: MainWindow.java,v 1.2 2007/12/22 14:01:20 nichele Exp $
 */
public class SyncClient
implements SyncItemListener       ,
           SyncListener           ,
           SyncSourceListener     ,
           SyncStatusListener     ,
           SyncTransportListener  {

    /**
     * Notify Synchronization begin
     *
     * @param event
     */
    public void syncBegin(SyncEvent event) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.debug("SyncEvent - Sync begin - date: " + event.getDate());
        }
    }

    /**
     * Notify Synchronization end
     *
     * @param event
     */
    public void syncEnd(SyncEvent event) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.debug("SyncEvent - Sync end - date: " + event.getDate());
        }
    }

    /**
     * Notify send initialization message
     *
     * @param event
     */
    public void sendInitialization(SyncEvent event) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.debug("SyncEvent - Send initialization - date: " +
                         event.getDate()                            );
        }
    }

    /**
     * Notify send modification message
     *
     * @param event
     */
    public void sendModification(SyncEvent event) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.debug("SyncEvent - Send modification - date: " +
                         event.getDate()                          );
        }
    }

    /**
     * Notify send finalization message
     *
     * @param event
     */
    public void sendFinalization(SyncEvent event) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.debug("SyncEvent - Send finalization - date: " +
                         event.getDate()                          );
        }
    }

    /**
     * Notify that the engine encountered a not blocking error
     *
     * @param event
     */
    public void syncError(SyncEvent event) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.debug("SyncEvent - Sync error - date: " +
                         event.getDate    ()               +
                         " - message: "                    +
                         event.getMessage ()               +
                         " - cause: "                      +
                         event.getCause().getMessage()     );
        }
    }

    /**
     * Notify a syncSource begin synchronization
     *
     * @param event
     */
    public void syncBegin(SyncSourceEvent event) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.debug("SyncSourceEvent - Sync begin - date: " +
                         event.getDate      ()                   +
                         " - sourceUri: "                        +
                         event.getSourceUri ()                   +
                         " - sync mode: "                        +
                         event.getSyncMode  ()                   );
        }
    }

    /**
     * Notify a syncSource end synchronization
     *
     * @param event
     */
    public void syncEnd(SyncSourceEvent event) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.debug("SyncSourceEvent - Sync end - date: " +
                         event.getDate      ()                 +
                         " - sourceUri: "                      +
                         event.getSourceUri ()                 +
                         " - sync mode: "                      +
                         event.getSyncMode  ()                 );
        }
    }

    /**
     * Notify SyncStransport begin send data
     *
     * @param event
     */
    public void sendDataBegin(SyncTransportEvent event) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.debug
                ("SyncTransportEvent - Send data begin - data length: " +
                 event.getData()                                       );
        }
    }

    /**
     * Notify SyncStransport end send data
     *
     * @param event
     */
    public void sendDataEnd(SyncTransportEvent event) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.debug
                ("SyncTransportEvent - Send data end - data length: " +
                 event.getData()                                      );
        }
    }

    /**
     * Notify SyncStransport begin receive data
     *
     * @param event
     */
    public void receiveDataBegin(SyncTransportEvent event) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.debug
                ("SyncTransportEvent - Receive data begin - data length: " +
                 event.getData()                                           );
        }
    }

    /**
     * Notify SyncStransport receiving data
     *
     * @param event
     */
    public void dataReceived(SyncTransportEvent event) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.debug
                ("SyncTransportEvent - Data received - data length: " +
                 event.getData()                                      );
        }
    }

    /**
     * Notify SyncStransport end receive data
     *
     * @param event
     */
    public void receiveDataEnd(SyncTransportEvent event) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.debug
                ("SyncTransportEvent - Received data end - data length: " +
                 event.getData()                                          );
        }
    }

    /**
     * Notify an item added by the server
     *
     * @param event
     */
    public void itemAddedByServer(SyncItemEvent event) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.debug
                ("SyncItemEvent - Item added by server - sourceUri: " +
                 event.getSourceUri()                                 +
                 " - key: "                                           +
                 event.getItemKey().getKeyAsString()                  );
        }
    }

    /**
     * Notify an item deleted by the server
     *
     * @param event
     */
    public void itemDeletedByServer(SyncItemEvent event) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.debug
                ("SyncItemEvent - Item deleted by server - sourceUri: " +
                 event.getSourceUri()                                   +
                 " - key: "                                             +
                 event.getItemKey().getKeyAsString()                    );
        }
    }

    /**
     * Notify an item updated by the server
     *
     * @param event
     */
    public void itemUpdatedByServer(SyncItemEvent event) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.debug
                ("SyncItemEvent - Item updated by server - sourceUri: " +
                 event.getSourceUri()                                   +
                 " - key: "                                             +
                 event.getItemKey().getKeyAsString()                   );
        }
    }

    /**
     * Notify an item added by the client
     *
     * @param event
     */
    public void itemAddedByClient(SyncItemEvent event) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.debug
                ("SyncItemEvent - Item added by client  - sourceUri: " +
                 event.getSourceUri()                                  +
                 " - key: "                                            +
                 event.getItemKey().getKeyAsString()                   );
        }
    }

    /**
     * Notify an item delete by the client
     *
     * @param event
     */
    public void itemDeletedByClient(SyncItemEvent event) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.debug
                ("SyncItemEvent - Item deleted by client - sourceUri: " +
                 event.getSourceUri()                                  +
                 " - key: "                                            +
                 event.getItemKey().getKeyAsString()                   );
        }
    }

    /**
     * Notify an item updated by the client
     *
     * @param event
     */
    public void itemUpdatedByClient(SyncItemEvent event) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.debug
                ("SyncItemEvent - Item updated by client - sourceUri: " +
                 event.getSourceUri()                                  +
                 " - key: "                                            +
                 event.getItemKey().getKeyAsString()                   );
        }
    }

    /**
     * Notify a status received from the server
     *
     * @param event
     */
    public void statusReceived (SyncStatusEvent event) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.debug
                ("SyncStatusEvent - Received status - command: " +
                 event.getCommand()                              +
                 " - status: "                                   +
                 event.getStatusCode()                           +
                 " - sourceUri: "                                +
                 event.getSourceUri()                            +
                 " - key: "                                      +
                 event.getItemKey().getKeyAsString()             );
        }
    }

    /**
     * Notify create a status to send to the server
     *
     * @param event
     */
    public void statusToSend (SyncStatusEvent event) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.debug
                ("SyncStatusEvent - Status to send - command: " +
                 event.getCommand()                             +
                 " - status: "                                  +
                 event.getStatusCode()                          +
                 " - sourceUri: "                               +
                 event.getSourceUri()                           +
                 " - key: "                                     +
                 event.getItemKey().getKeyAsString()            );
        }
    }

    //-------------------------------------------------------- Protected methods

   
    /**
     * Manages the contact synchronization, based on the configuration
     * parameters provided by the DeviceManager
     */
    public void synchronize() {
            SyncManager syncManager;
            try {
                syncManager = SyncManager.getSyncManager("");
                
                syncManager.addSyncItemListener     (this);
                syncManager.addSyncListener         (this);
                syncManager.addSyncSourceListener   (this);
                syncManager.addSyncStatusListener   (this);
                syncManager.addSyncTransportListener(this);
                
                syncManager.sync();
            } catch (SyncException e) {
                e.printStackTrace();
            } catch (DMException e) {
                e.printStackTrace();
            }

            
    }

    
   

}
