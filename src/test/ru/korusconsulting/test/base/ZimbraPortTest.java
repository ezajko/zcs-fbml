package ru.korusconsulting.test.base;


import java.net.URL;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import ru.korusconsulting.connector.base.ZimbraPort;
import ru.korusconsulting.test.Utils;

import com.funambol.framework.server.Sync4jUser;
import com.funambol.syncclient.spdm.SimpleDeviceManager;

public class ZimbraPortTest {
    private ZimbraPort zimbraPort;
    private static Properties userCred;
    private static Properties settings;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty(SimpleDeviceManager.PROP_DM_DIR_BASE,
        "test/scripts/simpleSync");
        settings = Utils.load("test/settings.properties");
        userCred = Utils.load("test/user.properties");
        System.setProperty("javax.net.ssl.trustStore",settings.getProperty("keystoke"));
    }

    @Before
    public void setUp() throws Exception {
        try {
            zimbraPort = new ZimbraPort(new URL(settings.getProperty("zimbra-server")));
            Sync4jUser user = new Sync4jUser();
            user.setPassword(userCred.getProperty("password"));
            user.setUsername(userCred.getProperty("username"));
            zimbraPort.requestAutorization(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() throws Exception {
    }

}
