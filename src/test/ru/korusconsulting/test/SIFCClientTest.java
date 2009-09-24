package ru.korusconsulting.test;

import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import junit.framework.Assert;

import org.dom4j.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.korusconsulting.connector.base.ZimbraPort;
import ru.korusconsulting.connector.funambol.PhoneDependedConverter;

import com.funambol.common.pim.common.Property;
import com.funambol.common.pim.contact.Contact;
import com.funambol.common.pim.contact.Note;
import com.funambol.common.pim.converter.ConverterException;
import com.funambol.framework.engine.SyncItemKey;
import com.funambol.framework.server.Sync4jUser;
import com.funambol.syncclient.spdm.SimpleDeviceManager;
import com.funambol.syncclient.spds.AuthenticationException;
import com.funambol.syncclient.spds.SyncException;
import com.funambol.syncclient.spds.SyncManager;
import com.funambol.syncclient.spds.UpdateException;

public class SIFCClientTest {

    private SyncManager syncManager;
    private ZimbraPort zimbraPort;
    private static Properties userCred;
    private static Properties settings;

    @BeforeClass
    public static void setUpBeforeClass() {
        System.setProperty(SimpleDeviceManager.PROP_DM_DIR_BASE,
                           "test/scripts/sifcSync");
        settings = Utils.load("test/settings.properties");
        userCred = Utils.load("test/user.properties");
        System.setProperty("javax.net.ssl.trustStore", settings.getProperty("keystoke"));
    }

    @Before
    public void setUp() throws Exception {
        try {
            Utils.replaceScript("", userCred);
            syncManager = SyncManager.getSyncManager("");
            zimbraPort = new ZimbraPort(new URL(settings.getProperty("zimbra-server")));
            Sync4jUser user = new Sync4jUser();
            user.setPassword(userCred.getProperty("password"));
            user.setUsername(userCred.getProperty("username"));
            zimbraPort.requestAutorization(user);
            Element contactsResponse = zimbraPort.requestAllContactIds();
            zimbraPort.requestRemoveAllItems(contactsResponse, "id");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void bug1958751Test() throws Throwable{
        ru.korusconsulting.connector.manager.ContactManager cm = new ru.korusconsulting.connector.manager.ContactManager();
        cm.setZimbraPort(zimbraPort);
        
        

        final Contact c1 = TestContacts.get("test1_1");
        final Contact c2 = TestContacts.get("test1_2");
        final Contact c3 = TestContacts.get("test1_3");
        final Contact c4 = TestContacts.get("test1_4");
        cm.addItem(c1);
        cm.addItem(c2);
        cm.addItem(c3);
        cm.addItem(c4);
        //----------------------------------------------------
        final ContactItemManager contactManager = new ContactItemManager("Contacts");
        syncManager.sync();
        //----------------------------------------------------
        Assert.assertEquals(PhoneDependedConverter.SIFC_TYPE,
                            contactManager.getSyncSource().getType());
        //----------------------------------------------------
        cm.requestItemsForSync();
        zimbraPort.requestAllFolders();//refresh folders before continue
        ArrayList<Contact> allContacts = zimbraPort.requestAllContact(false);
//        TestContacts.assertEquals(allContacts, c1, c2, c3, c4);
         
    }
    
    @After
    public void tearDown() throws Exception {
        syncManager = null;
        Utils.replaceScript("", null);
        if (zimbraPort != null)
            zimbraPort.close();
    }

}
