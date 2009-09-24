package ru.korusconsulting.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.funambol.syncclient.spdm.DMException;
import com.funambol.syncclient.spdm.SimpleDeviceManager;
import com.funambol.syncclient.spds.SyncException;

public class Utils {
    public static void replaceScript(String name, Properties user) throws IOException, SyncException, DMException{
        Properties script=new Properties();
        String scriptFile = System.getProperty(SimpleDeviceManager.PROP_DM_DIR_BASE)+"/"+name+"/spds/syncml.properties";
        FileInputStream fileInputStream = new FileInputStream(scriptFile);
        script.load(fileInputStream);
        fileInputStream.close();
        if(user==null){
            user=new Properties();
            user.put("username", "guest");
            user.put("password", "guest");
        }
        script.putAll(user);
        synchronized (Utils.class) {
            FileOutputStream fileOutputStream = new FileOutputStream(scriptFile);
            script.store(fileOutputStream, null);
            fileOutputStream.close();
        }
        
    }
    
    public static Properties load(String fileName){
        Properties props = new Properties();
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(fileName);
            props.load(fileInputStream);
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }
   
}
