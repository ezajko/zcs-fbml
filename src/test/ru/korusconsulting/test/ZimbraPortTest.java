package ru.korusconsulting.test;


import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Properties;
import java.util.TimeZone;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.korusconsulting.connector.base.SoapHelper;
import ru.korusconsulting.connector.base.XMLDocumentWriter;
import ru.korusconsulting.connector.base.ZimbraPort;
import ru.korusconsulting.connector.exceptions.SoapRequestException;
import ru.korusconsulting.connector.funambol.CalendarUtils;
import ru.korusconsulting.connector.funambol.PhoneDependedConverter;
import ru.korusconsulting.connector.manager.CalendarManager;

import com.funambol.common.pim.calendar.Calendar;
import com.funambol.common.pim.calendar.Event;
import com.funambol.common.pim.converter.ConverterException;
import com.funambol.framework.engine.source.SyncSourceException;
import com.funambol.framework.server.Sync4jUser;
import com.funambol.syncclient.spdm.SimpleDeviceManager;

public class ZimbraPortTest {
    private org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
    private ZimbraPort zimbraPort;
    private static Properties userCred;
    private static Properties settings;
    private CalendarManager manager = new CalendarManager();
    
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
            manager.setZimbraPort(zimbraPort);
            manager.requestItemsForSync();
            manager.removeAllItems();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void syncRequestTest() throws IOException, SoapRequestException, ConverterException, SyncSourceException{
        zimbraPort.requestAllCalendarsIds(false);
        StringBuilder htmlStream=new StringBuilder();
        htmlStream.append("<appointment>");
        htmlStream.append("<AllDayEvent>0</AllDayEvent>");
        htmlStream.append("<Start>20080311T180000Z</Start>");
        htmlStream.append("<End>20080311T190000Z</End>");
        htmlStream.append("<Subject>Bbseries</Subject>");
        htmlStream.append("<Location></Location>");
        htmlStream.append("<Body></Body>");
        htmlStream.append("<ReminderMinutesBeforeStart>15</ReminderMinutesBeforeStart>");
        htmlStream.append("<ReminderSet>1</ReminderSet>");
        htmlStream.append("<Sensitivity>0</Sensitivity>");
        htmlStream.append("<BusyStatus>2</BusyStatus>");
        htmlStream.append("<IsRecurring>1</IsRecurring>");
        htmlStream.append("<RecurrenceType>0</RecurrenceType>");
        htmlStream.append("<Interval>1</Interval>");
        htmlStream.append("<PatternStartDate>20080311T180000Z</PatternStartDate>");
        htmlStream.append("<NoEndDate>1</NoEndDate>");
        htmlStream.append("<Exceptions>");
        htmlStream.append("<ExcludeDate>20080314</ExcludeDate>");
        htmlStream.append("<IncludeDate></IncludeDate>");
        htmlStream.append("</Exceptions>");
        htmlStream.append("</appointment>");
        byte[] content = htmlStream.toString().getBytes();
        Calendar cal = CalendarUtils.convertFrom(PhoneDependedConverter.SIFE_TYPE,
                null,
                content,
                null,
                "UTF-8");
        String id=manager.addItem(cal);
        String lastToken=zimbraPort.getLastToken();
        zimbraPort.requestDeleteItem(id, false);
        try {
            tearDown();
            setUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Element result=zimbraPort.requestSyncronization(lastToken);
        
        XMLDocumentWriter writer = new XMLDocumentWriter();
        writer.write(result);
    }
    
    @Test
    public void ical1Test() throws Throwable{
        StringBuilder appointment = new StringBuilder();
        appointment.append("<appt id='772'\n");
        appointment.append("                  uid='4fb0038a-c5a6-4d43-bef7-5c7fb5cc6afd'\n");
        appointment.append("                  f=''\n");
        appointment.append("                  d='1201677511000'\n");
        appointment.append("                  rev='1915'\n");
        appointment.append("                  t=''\n");
        appointment.append("                  s='2156'\n");
        appointment.append("                  md='1201677511'\n");
        appointment.append("                  ms='1915'\n");
        appointment.append("                  l='10'>\n");
        appointment.append("                <inv id='771'\n");
        appointment.append("                     seq='0'\n");
        appointment.append("                     compNum='0'\n");
        appointment.append("                     type='appt'>\n");
        appointment.append("                    <tz id='(GMT+03.00) Moscow / St. Petersburg / Volgograd'\n");
        appointment.append("                        stdoff='180'\n");
        appointment.append("                        dayoff='240'>\n");
        appointment.append("                        <standard min='0'\n");
        appointment.append("                                  wkday='1'\n");
        appointment.append("                                  sec='0'\n");
        appointment.append("                                  mon='10'\n");
        appointment.append("                                  hour='3'\n");
        appointment.append("                                  week='-1'/>\n");
        appointment.append("                        <daylight min='0'\n");
        appointment.append("                                  wkday='1'\n");
        appointment.append("                                  sec='0'\n");
        appointment.append("                                  mon='3'\n");
        appointment.append("                                  hour='2'\n");
        appointment.append("                                  week='-1'/>\n");
        appointment.append("                    </tz>\n");
        appointment.append("                    <comp uid='4fb0038a-c5a6-4d43-bef7-5c7fb5cc6afd'\n");
        appointment.append("                          d='1201677511000'\n");
        appointment.append("                          status='CONF'\n");
        appointment.append("                          isOrg='1'\n");
        appointment.append("                          class='PUB'\n");
        appointment.append("                          loc='место'\n");
        appointment.append("                          compNum='0'\n");
        appointment.append("                          apptId='772'\n");
        appointment.append("                          fb='B'\n");
        appointment.append("                          calItemId='772'\n");
        appointment.append("                          x_uid='4fb0038a-c5a6-4d43-bef7-5c7fb5cc6afd'\n");
        appointment.append("                          name='тема'\n");
        appointment.append("                          rsvp='0'\n");
        appointment.append("                          fba='B'\n");
        appointment.append("                          seq='0'\n");
        appointment.append("                          transp='O'>\n");
        appointment.append("                        <fr>Контент</fr>\n");
        appointment.append("                        <desc>Контент</desc>\n");
        appointment.append("                        <or d='Roman Bliznets'\n");
        appointment.append("                            a='rbliznets@korusconsulting.ru'\n");
        appointment.append("                            url='rbliznets@korusconsulting.ru'/>\n");
        appointment.append("                        <s d='20080130T090000'\n");
        appointment.append("                           tz='(GMT+03.00) Moscow / St. Petersburg / Volgograd'/>\n");
        appointment.append("                        <e d='20080130T093000'\n");
        appointment.append("                           tz='(GMT+03.00) Moscow / St. Petersburg / Volgograd'/>\n");
        appointment.append("                    </comp>\n");
        appointment.append("                </inv>\n");
        appointment.append("                <replies/>\n");
        appointment.append("            </appt>\n");

        Document doc = reader.read(new StringReader(appointment.toString()));
        Element appt = doc.getRootElement();
        Calendar cal = CalendarUtils.getInstance().asCalendar(appt, true, TimeZone.getTimeZone("Europe/Moscow"));
        Element createdCalendar = zimbraPort.requestCreateCalendar(cal);
        String key = createdCalendar.attributeValue("invId");
//        try {
//            tearDown();
//            setUp();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        
        Element icalResponse=zimbraPort.requestIcal(key);
        icalResponse.element("ical");
    }

    @After
    public void tearDown() throws Exception {
        if (zimbraPort != null){
            zimbraPort.close();
            zimbraPort=null;
        }
    }

    
    
}
