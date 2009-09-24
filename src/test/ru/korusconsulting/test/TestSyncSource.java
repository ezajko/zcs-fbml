package ru.korusconsulting.test;

import java.security.Principal;
import java.util.Date;

import com.funambol.syncclient.spds.SyncException;
import com.funambol.syncclient.spds.engine.SyncItem;
import com.funambol.syncclient.spds.engine.SyncItemState;
import com.funambol.syncclient.spds.engine.SyncSource;

public class TestSyncSource implements SyncSource {

    private String name = null;
    private String type = null;
    private String version = null;
    private String sourceURI = null;

    protected SyncItemManager manager;

    // ------------------------------------------------------------ Constructors

    public TestSyncSource() {

    }

    // ---------------------------------------------------------- Public methods

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSourceURI() {
        return sourceURI;
    }

    public void setSourceURI(String sourceURI) {
        this.sourceURI = sourceURI;
    }

    public SyncItem[] getAllSyncItems(Principal principal) throws SyncException {
        return manager.getByState((char) -1).toArray(new SyncItem[0]);
    }

    public SyncItem[] getDeletedSyncItems(Principal principal, Date since)
            throws SyncException {
        return manager.getByState(SyncItemState.DELETED).toArray(new SyncItem[0]);
    }

    public SyncItem[] getNewSyncItems(Principal principal, Date since)
            throws SyncException {
        return manager.getByState(SyncItemState.NEW).toArray(new SyncItem[0]);
    }

    public SyncItem[] getUpdatedSyncItems(Principal principal, Date since)
            throws SyncException {
        return manager.getByState(SyncItemState.UPDATED).toArray(new SyncItem[0]);
    }

    public void beginSync(int syncMode) throws SyncException {
        manager=SyncItemManager.getManager(name);
        manager.setSyncSource(this);
        manager.onLoad();
    }

    public void commitSync() throws SyncException {
        manager.commit();
    }

    public void removeSyncItem(Principal principal, SyncItem syncItem)
            throws SyncException {
        manager.remove(syncItem.getKey().getKeyAsString());

    }

    public SyncItem setSyncItem(Principal principal, SyncItem syncItem)
            throws SyncException {
        SyncItem updated = manager.update(syncItem);
        if (updated == null) {
            manager.add(syncItem);
            return syncItem;
        }
        return updated;
    }
    
    
}