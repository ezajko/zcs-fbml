package ru.korusconsulting.test;

import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.TimeZone;

import junit.framework.Assert;

import org.dom4j.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.korusconsulting.connector.base.ZimbraPort;
import ru.korusconsulting.connector.funambol.PhoneDependedConverter;
import ru.korusconsulting.connector.manager.CalendarManager;

import com.funambol.common.pim.calendar.Calendar;
import com.funambol.common.pim.common.Property;
import com.funambol.common.pim.contact.Contact;
import com.funambol.common.pim.contact.Note;
import com.funambol.common.pim.converter.ConverterException;
import com.funambol.framework.engine.SyncItemKey;
import com.funambol.framework.server.Sync4jUser;
import com.funambol.syncclient.spdm.SimpleDeviceManager;
import com.funambol.syncclient.spds.SyncManager;

public class CalendarClientTest {

    private SyncManager syncManager;
    private ZimbraPort zimbraPort;
    private static Properties userCred;
    private static Properties settings;
    private CalendarManager calManager;
    private String tempKey;

    @BeforeClass
    public static void setUpBeforeClass() {
        System.setProperty(SimpleDeviceManager.PROP_DM_DIR_BASE, "test/scripts/simpleSync");
        settings = Utils.load("test/settings.properties");
        userCred = Utils.load("test/user.properties");
        System.setProperty("javax.net.ssl.trustStore", settings.getProperty("keystoke"));
    }

    @Before
    public void setUp() throws Exception {
        Utils.replaceScript("", userCred);
        syncManager = SyncManager.getSyncManager("");
        zimbraPort = new ZimbraPort(new URL(settings.getProperty("zimbra-server")));
        Sync4jUser user = new Sync4jUser();
        user.setPassword(userCred.getProperty("password"));
        user.setUsername(userCred.getProperty("username"));
        zimbraPort.requestAutorization(user);
        calManager = new CalendarManager();
        calManager.setTask(false);
        calManager.setZimbraPort(zimbraPort);
        calManager.setLastToken(Long.valueOf(zimbraPort.getLastToken()));
        calManager.setPhoneTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        calManager.requestItemsForSync();
        calManager.removeAllItems();
    }

    @Test
    public void simpleSyncTest() throws Throwable {
        final CalendarItemManager calItemManager = new CalendarItemManager("Calendars");
        final Calendar calendar = TestCalendars.get();

        SyncItemManager.OnLoad onLoad = new SyncItemManager.OnLoad() {
            public void onLoad() {
                try {
                    Assert.assertNotNull(calendar);
                    Assert.assertEquals(PhoneDependedConverter.SIFE_TYPE,
                                        calItemManager.getSyncSource().getType());
                    tempKey=calItemManager.addCalendar(calendar);
                } catch (ConverterException e) {
                    e.printStackTrace();
                }
            }
        };
        calItemManager.addListener(onLoad);
        syncManager.sync();
        calItemManager.removeListener(onLoad);
        calManager.removeAllItems();
        calItemManager.update(tempKey,calendar);
        syncManager.sync();
        
        ArrayList<Calendar> allCalendars = calManager.getAllCalendars();
        TestCalendars.assertEquals(allCalendars, calendar);
    }

/*    @Test
    public void complexSyncTest() throws Throwable {
        ru.korusconsulting.connector.manager.ContactManager cm = new ru.korusconsulting.connector.manager.ContactManager();
        cm.setZimbraPort(zimbraPort);
        final ContactItemManager contactManager = new ContactItemManager("Contacts");
        syncManager.sync();
        // ----------------------------------------------------
        Assert.assertEquals(PhoneDependedConverter.VCARD_TYPE, contactManager.getSyncSource()
                                                                             .getType());

        final Contact c1 = TestContacts.get("complex1");
        final Contact c2 = TestContacts.get("photoTest1");
        final Contact c3 = TestContacts.get("simple");
        contactManager.addContact(c1);
        contactManager.addContact(c3);
        syncManager.sync();
        // ----------------------------------------------------
        cm.requestItemsForSync();
        ArrayList<Contact> allContacts = zimbraPort.requestAllContact(false);
        TestContacts.assertEquals(allContacts, c1, c3);
        contactManager.addContact(c2);
        SyncItemKey[] keys = cm.getTwins(c3);
        zimbraPort.requestDeleteContact(keys[0].getKeyAsString(), true);
        c1.getBusinessDetail().getAddress().getCountry().setPropertyValue("Р.Ф.");
        contactManager.update(c1);
        syncManager.sync();
        // ----------------------------------------------------
        allContacts = zimbraPort.requestAllContact(false);
        TestContacts.assertEquals(allContacts, c1, c2);
    }

    @Test
    public void complex2SyncTest() throws Throwable {
        ru.korusconsulting.connector.manager.ContactManager cm = new ru.korusconsulting.connector.manager.ContactManager();
        cm.setZimbraPort(zimbraPort);
        final ContactItemManager contactManager = new ContactItemManager("Contacts");
        syncManager.sync();
        // ----------------------------------------------------
        Assert.assertEquals(PhoneDependedConverter.VCARD_TYPE, contactManager.getSyncSource()
                                                                             .getType());

        final Contact c1 = TestContacts.get("complex1");
        final Contact c2 = TestContacts.get("photoTest1");
        final Contact c3 = TestContacts.get("simple");
        contactManager.addContact(c1);
        contactManager.addContact(c3);
        syncManager.sync();
        // ----------------------------------------------------
        cm.requestItemsForSync();
        ArrayList<Contact> allContacts = zimbraPort.requestAllContact(false);
        TestContacts.assertEquals(allContacts, c1, c3);
        contactManager.addContact(c2);
        SyncItemKey[] keys = cm.getTwins(c3);
        zimbraPort.requestDeleteContact(keys[0].getKeyAsString(), true);
        c1.getBusinessDetail().getAddress().getCountry().setPropertyValue("Р.Ф.");
        contactManager.update(c1);
        syncManager.sync();
        // ----------------------------------------------------
        allContacts = zimbraPort.requestAllContact(true);
        TestContacts.assertEquals(allContacts, c1, c2, c3);
    }

    @Test
    public void simpleFolderTest() throws Throwable {
        ru.korusconsulting.connector.manager.ContactManager cm = new ru.korusconsulting.connector.manager.ContactManager();
        cm.setZimbraPort(zimbraPort);
        final ContactItemManager contactManager = new ContactItemManager("Contacts");
        syncManager.sync();
        // ----------------------------------------------------
        Assert.assertEquals(PhoneDependedConverter.VCARD_TYPE, contactManager.getSyncSource()
                                                                             .getType());

        final Contact c1 = TestContacts.get("complex1");
        final Contact c2 = TestContacts.get("photoTest1");
        final Contact c3 = TestContacts.get("simple");
        c1.setFolder("FolderForC1");
        c2.setFolder("FolderForC2");
        c1.setCategories(new Property("Личное,Бизнесс"));
        c2.setCategories(new Property("Бизнесс"));
        c3.setCategories(new Property("Личное"));
        contactManager.addContact(c1);
        contactManager.addContact(c3);
        syncManager.sync();
        // ----------------------------------------------------
        cm.requestItemsForSync();
        zimbraPort.requestAllFolders();// refresh folders before continue
        ArrayList<Contact> allContacts = zimbraPort.requestAllContact(false);
        TestContacts.assertEquals(allContacts, c1, c3);
        contactManager.addContact(c2);
        c1.setFolder("FolderForC2");
        c3.setFolder("FolderForC3");
        c3.setCategories(new Property("Личное,Бизнесс"));
        contactManager.update(c1);
        contactManager.update(c3);
        syncManager.sync();
        // ----------------------------------------------------
        zimbraPort.requestAllFolders();// refresh folders before continue
        allContacts = zimbraPort.requestAllContact(true);
        TestContacts.assertEquals(allContacts, c1, c2, c3);
    }
*/
    @After
    public void tearDown() throws Exception {
        syncManager = null;
        Utils.replaceScript("", null);
        if (zimbraPort != null)
            zimbraPort.close();
    }

}
