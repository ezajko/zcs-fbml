package ru.korusconsulting.test;

import java.util.ArrayList;
import java.util.HashMap;

import com.funambol.syncclient.spds.engine.SyncItem;
import com.funambol.syncclient.spds.engine.SyncItemState;

public class SyncItemManager {
    private ArrayList<SyncItem> syncItems = new ArrayList<SyncItem>();
    protected TestSyncSource syncSource;
    private ArrayList<OnLoad> listeners=new ArrayList<OnLoad>(); 

    public SyncItemManager(String name) {
        register(name, this);
    }

    public boolean add(SyncItem o) {
        o.setState(SyncItemState.NEW);
        return syncItems.add(o);
    }

    public SyncItem remove(String key) {
        SyncItem o = get(key);
        o.setState(SyncItemState.DELETED);
        return o;
    }

    public SyncItem update(SyncItem element) {
        SyncItem o = get(element.getKey().getKeyAsString());
        if (o == null)
            return null;
        int index = syncItems.indexOf(o);
        element.setState(SyncItemState.UPDATED);
        return syncItems.set(index, element);
    }

    public SyncItem get(String key) {
        for (SyncItem item : syncItems) {
            if (item.getKey().getKeyAsString().equals(key)) {
                return item;
            }
        }
        return null;
    }

    public void commit() {
        SyncItem item;
        for (int i = 0; i < syncItems.size(); i++) {
            item = syncItems.get(i);
            if (item.getState() == SyncItemState.DELETED) {
                syncItems.remove(i);
            } else if (item.getState() == SyncItemState.NEW
                    || item.getState() == SyncItemState.UPDATED
                    || item.getState() == SyncItemState.SYNCHRONIZED) {
                item.setState(SyncItemState.SYNCHRONIZED);
            } else {
                throw new RuntimeException("Unexpected state of item:" + item);
            }
        }
    }

    public ArrayList<SyncItem> getByState(char state) {
        ArrayList<SyncItem> items = new ArrayList<SyncItem>();
        for (int i = 0; i < syncItems.size(); i++) {
            SyncItem item = syncItems.get(i);
            if (item.getState() == state || state == -1) {
                items.add(item);
            }
        }
        return items;
    }

    public TestSyncSource getSyncSource() {
        return syncSource;
    }

    public void setSyncSource(TestSyncSource syncSource) {
        this.syncSource = syncSource;
    }

    private static HashMap<String, SyncItemManager> managers = new HashMap<String, SyncItemManager>();

    public static void register(String name, SyncItemManager manager) {
        managers.put(name, manager);
    }

    public static void unregister(String name) {
        managers.remove(name);
    }

    public static SyncItemManager getManager(String name) {
        return managers.get(name);
    }
    
    public void onLoad(){
        ArrayList<OnLoad> tmp=(ArrayList<OnLoad>) listeners.clone();
        for(OnLoad listener:tmp){
            listener.onLoad();
        }
    }
    
    
    
    public interface OnLoad{
        public void onLoad();
    }

    

    public boolean addListener(OnLoad o) {
        return listeners.add(o);
    }

    public boolean removeListener(Object o) {
        return listeners.remove(o);
    }
}
