package ru.korusconsulting.test;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Properties;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import junit.framework.Assert;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.korusconsulting.connector.base.ZimbraPort;
import ru.korusconsulting.connector.exceptions.SoapRequestException;
import ru.korusconsulting.connector.funambol.CalendarUtils;
import ru.korusconsulting.connector.funambol.PhoneDependedConverter;
import ru.korusconsulting.connector.manager.CalendarManager;
import sun.util.calendar.ZoneInfo;

import com.funambol.common.pim.calendar.Calendar;
import com.funambol.common.pim.calendar.RecurrencePattern;
import com.funambol.common.pim.converter.ConverterException;
import com.funambol.framework.engine.source.SyncSourceException;
import com.funambol.framework.server.Sync4jUser;
import com.funambol.framework.tools.merge.MergeResult;
import com.funambol.syncclient.spdm.SimpleDeviceManager;

public class CalendarUtilsTest {
    private org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
    private ZimbraPort zimbraPort;
    private static Properties userCred;
    private static Properties settings;
    private CalendarManager manager = new CalendarManager();

    @BeforeClass
    public static void setUpBeforeClass() {
        System.setProperty(SimpleDeviceManager.PROP_DM_DIR_BASE,
                           "test/scripts/simpleSync");
        settings = Utils.load("test/settings.properties");
        userCred = Utils.load("test/user.properties");
        System.setProperty("javax.net.ssl.trustStore", settings.getProperty("keystoke"));
    }

    @Before
    public void setUp() throws Exception {
        zimbraPort = new ZimbraPort(new URL(settings.getProperty("zimbra-server")));
        Sync4jUser user = new Sync4jUser();
        user.setPassword(userCred.getProperty("password"));
        user.setUsername(userCred.getProperty("username"));
        zimbraPort.requestAutorization(user);
        manager.setZimbraPort(zimbraPort);
        manager.requestItemsForSync();
        manager.removeAllItems();
    }
    
    @Test 
    public void getLocalDate1Test() throws RuntimeException{
        java.util.Calendar cal= CalendarUtils.getInstance().getLocalDate("2008-02-26", 0);
        Assert.assertEquals(26,cal.get(java.util.Calendar.DAY_OF_MONTH));
        Assert.assertEquals(1,cal.get(java.util.Calendar.MONTH));// count with 0
        Assert.assertEquals(2008,cal.get(java.util.Calendar.YEAR));
    }
    
    @Test
    public void daylightTimeConversionTest() throws Throwable{
        StringBuilder htmlStream=new StringBuilder();
        htmlStream.append("<tz id='(GMT-08.00) Pacific Time (US &amp; Canada)'\n");
        htmlStream.append("                        stdoff='-480'\n");
        htmlStream.append("                        dayoff='-420'>\n");
        htmlStream.append("                        <standard min='0'\n");
        htmlStream.append("                                  wkday='1'\n");
        htmlStream.append("                                  sec='0'\n");
        htmlStream.append("                                  mon='11'\n");
        htmlStream.append("                                  hour='2'\n");
        htmlStream.append("                                  week='1'/>\n");
        htmlStream.append("                        <daylight min='0'\n");
        htmlStream.append("                                  wkday='1'\n");
        htmlStream.append("                                  sec='0'\n");
        htmlStream.append("                                  mon='3'\n");
        htmlStream.append("                                  hour='2'\n");
        htmlStream.append("                                  week='2'/>\n");
        htmlStream.append("                    </tz>\n");
        Document doc = reader.read(new StringReader(htmlStream.toString()));
        Element tz = doc.getRootElement();
        
        java.util.Calendar calendar=java.util.Calendar.getInstance();
        calendar.set(2008, 03, 01, 17, 00, 00);
        Assert.assertTrue(CalendarUtils.getInstance().dayLightTime(tz, calendar));
        
        
        SimpleTimeZone timeZone1=(SimpleTimeZone) CalendarUtils.getInstance().getTimeZone(tz);
        SimpleTimeZone timeZone2=((ZoneInfo) ZoneInfo.getTimeZone("Canada/Pacific")).getLastRuleInstance();
        Assert.assertEquals(timeZone1.getDSTSavings(), timeZone2.getDSTSavings());
        Assert.assertEquals(timeZone1.getRawOffset(), timeZone2.getRawOffset());
        
        String date=CalendarUtils.getInstance().correctTimeZone("20080401T080000Z", tz, TimeZone.getTimeZone("Europe/Moscow"));
        Assert.assertEquals("20080401T120000", date);
        
        date=CalendarUtils.getInstance().correctTimeZone("20080401T080000", tz, TimeZone.getTimeZone("Europe/Moscow"));
        Assert.assertEquals("20080401T190000", date);
        
        date=CalendarUtils.getInstance().correctTimeZone("20080412T063000Z", null, TimeZone.getTimeZone("Europe/Moscow"));
        Assert.assertEquals("20080412T103000", date);
    }
    
    @Test 
    public void getCorrectTimeZoneTest() throws ConverterException{
        String date = CalendarUtils.getInstance().correctTimeZone("20080226", null, TimeZone.getTimeZone("Europe/Moscow"));
        Assert.assertEquals("20080226", date);
    }

    @Test
    public void simpleTest() {
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
        appointment.append("                          loc='ìåñòî'\n");
        appointment.append("                          compNum='0'\n");
        appointment.append("                          apptId='772'\n");
        appointment.append("                          fb='B'\n");
        appointment.append("                          calItemId='772'\n");
        appointment.append("                          x_uid='4fb0038a-c5a6-4d43-bef7-5c7fb5cc6afd'\n");
        appointment.append("                          name='òåìà'\n");
        appointment.append("                          rsvp='0'\n");
        appointment.append("                          fba='B'\n");
        appointment.append("                          seq='0'\n");
        appointment.append("                          transp='O'>\n");
        appointment.append("                        <fr>Êîíòåíò</fr>\n");
        appointment.append("                        <desc>Êîíòåíò</desc>\n");
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

        try {
            Document doc = reader.read(new StringReader(appointment.toString()));
            Element appt = doc.getRootElement();
            Calendar cal = CalendarUtils.getInstance().asCalendar(appt, true, TimeZone.getTimeZone("Europe/Moscow"));
            Assert.assertEquals("ìåñòî",
                                cal.getEvent().getLocation().getPropertyValueAsString());
            Assert.assertEquals(false, cal.getEvent().isAllDay());
            Assert.assertEquals("20080130T090000",
                                cal.getEvent().getDtStart().getPropertyValue());
            Assert.assertEquals("20080130T093000",
                                cal.getEvent().getDtEnd().getPropertyValue());
            Assert.assertEquals("0", cal.getEvent().getAccessClass().getPropertyValue());
            byte[] content = CalendarUtils.convertTo(PhoneDependedConverter.SIFE_TYPE,
                                                     null,
                                                     cal,
                                                     null,
                                                     "UTF-8");
            Calendar cal2 = CalendarUtils.convertFrom(PhoneDependedConverter.SIFE_TYPE,
                                                      null,
                                                      content,
                                                      null,
                                                      "UTF-8");
            //            System.out.println(cal2.toString());
            if (cal2.getEvent().getCreated().getPropertyValue() == null) {
                //it's funambol bug
                Object created = cal.getEvent().getCreated().getPropertyValue();
                cal2.getEvent().getCreated().setPropertyValue(created);
            }
            Assert.assertEquals(cal.toString(), cal2.toString());
//            System.out.println(cal.toString());
        } catch (DocumentException e) {
            e.printStackTrace();
            Assert.assertTrue("Exception occur", false);
        } catch (ConverterException e) {
            e.printStackTrace();
            Assert.assertTrue("Exception occur", false);
        }

    }

    @Test
    public void sifc1Test() throws ConverterException, IOException, SoapRequestException, SyncSourceException {
        StringBuilder appointment2 = new StringBuilder();
        appointment2.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        appointment2.append("<appointment>\n");
        appointment2.append("<Folder>DEFAULT_FOLDER</Folder>\n");
        appointment2.append("<Start>20080221T230000Z</Start>\n");
        appointment2.append("<End>20080222T020000Z</End>\n");
        appointment2.append("<AllDayEvent>0</AllDayEvent>\n");
        appointment2.append("<BillingInformation/>\n");
        appointment2.append("<Body>eeeeeeeeeeeeeeee</Body>\n");
        appointment2.append("<BusyStatus>1</BusyStatus>\n");
        appointment2.append("<Categories/>\n");
        appointment2.append("<Companies/>\n");
        appointment2.append("<Importance>1</Importance>\n");
        appointment2.append("<IsRecurring>0</IsRecurring>\n");
        appointment2.append("<Location>dddddddddddddddddddddddddd</Location>\n");
        appointment2.append("<MeetingStatus>0</MeetingStatus>\n");
        appointment2.append("<Mileage/>\n");
        appointment2.append("<NoAging>0</NoAging>\n");
        appointment2.append("<OptionalAttendees/>\n");
        appointment2.append("<ReminderMinutesBeforeStart>10</ReminderMinutesBeforeStart>\n");
        appointment2.append("<ReminderSet>0</ReminderSet>\n");
        appointment2.append("<ReminderSoundFile/>\n");
        appointment2.append("<ReplyTime/>\n");
        appointment2.append("<Sensitivity>0</Sensitivity>\n");
        appointment2.append("<Subject>edededed</Subject>\n");
        appointment2.append("<UnRead>0</UnRead>\n");
        appointment2.append("</appointment>\n");

        byte[] content = appointment2.toString().getBytes();
        Calendar cal2 = CalendarUtils.convertFrom(PhoneDependedConverter.SIFE_TYPE,
                                                  null,
                                                  content,
                                                  null,
                                                  "UTF-8");
        String key = manager.addItem(cal2);
        manager.requestItemsForSync();
        Calendar cal = manager.getItem(key);

        MergeResult mr = cal2.merge(cal2);
        if (mr.isSetARequired() || mr.isSetBRequired()) {
            content = CalendarUtils.convertTo(PhoneDependedConverter.SIFC_TYPE,
                                              null,
                                              cal,
                                              null,
                                              "UTF-8");
            String appointment = new String(content);
            System.out.println(appointment2);
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println(appointment);
            
            Assert.assertTrue(false);
        }


    }
    
    @Test
    public void sifc2Test() throws ConverterException, IOException, SoapRequestException, SyncSourceException{
    	StringBuilder htmlStream=new StringBuilder();
    	htmlStream.append("<appointment>");
    	htmlStream.append("<AllDayEvent>0</AllDayEvent>");
    	htmlStream.append("<Start>20080310T180000Z</Start>");
    	htmlStream.append("<End>20080310T185900Z</End>");
    	htmlStream.append("<Subject>---0123---</Subject>");
    	htmlStream.append("<Location></Location>");
    	htmlStream.append("<Body></Body>");
    	htmlStream.append("<ReminderMinutesBeforeStart></ReminderMinutesBeforeStart>");
    	htmlStream.append("<ReminderSet>0</ReminderSet>");
    	htmlStream.append("<Sensitivity>0</Sensitivity>");
    	htmlStream.append("<BusyStatus>2</BusyStatus>");
    	htmlStream.append("<IsRecurring>1</IsRecurring>");
    	htmlStream.append("<RecurrenceType>0</RecurrenceType>");
    	htmlStream.append("<Interval>1</Interval>");
    	htmlStream.append("<PatternStartDate>20080310T180000Z</PatternStartDate>");
    	htmlStream.append("<NoEndDate>1</NoEndDate>");
    	htmlStream.append("<Exceptions>");
    	htmlStream.append("<ExcludeDate>20080311</ExcludeDate>");
    	htmlStream.append("<IncludeDate></IncludeDate>");
    	htmlStream.append("</Exceptions>");
    	htmlStream.append("</appointment>");
    	
    	byte[] content = htmlStream.toString().getBytes();
    	Calendar cal = CalendarUtils.convertFrom(PhoneDependedConverter.SIFE_TYPE,
                null,
                content,
                null,
                "UTF-8");
    	manager.addItem(cal);
    	
    	htmlStream.setLength(0);
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
    	content = htmlStream.toString().getBytes();
    	cal = CalendarUtils.convertFrom(PhoneDependedConverter.SIFE_TYPE,
                null,
                content,
                null,
                "UTF-8");
    	manager.addItem(cal);
    }
    
    @Test
    public void sifc3Test() throws ConverterException, IOException, SoapRequestException, DocumentException{
        StringBuilder htmlStream=new StringBuilder();
        htmlStream.append("<appt id='8636'");
        htmlStream.append("      uid='37e889b9-0420-4960-a8dd-9b2d8ff30c57'");
        htmlStream.append("      f=''");
        htmlStream.append("      d='1205599369000'");
        htmlStream.append("      rev='29457'");
        htmlStream.append("      t=''");
        htmlStream.append("      s='2177'");
        htmlStream.append("      md='1205599369'");
        htmlStream.append("      ms='29457'");
        htmlStream.append("      l='10'>");
        htmlStream.append("    <inv id='8635'");
        htmlStream.append("         seq='0'");
        htmlStream.append("         compNum='0'");
        htmlStream.append("         type='appt'>");
        htmlStream.append("        <tz id='(GMT+03.00) Moscow / St. Petersburg / Volgograd'");
        htmlStream.append("            stdoff='180'");
        htmlStream.append("            dayoff='240'>");
        htmlStream.append("            <standard min='0'");
        htmlStream.append("                      wkday='1'");
        htmlStream.append("                      sec='0'");
        htmlStream.append("                      mon='10'");
        htmlStream.append("                      hour='3'");
        htmlStream.append("                      week='-1'/>");
        htmlStream.append("            <daylight min='0'");
        htmlStream.append("                      wkday='1'");
        htmlStream.append("                      sec='0'");
        htmlStream.append("                      mon='3'");
        htmlStream.append("                      hour='2'");
        htmlStream.append("                      week='-1'/>");
        htmlStream.append("        </tz>");
        htmlStream.append("        <comp uid='37e889b9-0420-4960-a8dd-9b2d8ff30c57'");
        htmlStream.append("              d='1205599369000'");
        htmlStream.append("              allDay='1'");
        htmlStream.append("              status='CONF'");
        htmlStream.append("              isOrg='1'");
        htmlStream.append("              class='PUB'");
        htmlStream.append("              loc=''");
        htmlStream.append("              compNum='0'");
        htmlStream.append("              apptId='8636'");
        htmlStream.append("              fb='F'");
        htmlStream.append("              calItemId='8636'");
        htmlStream.append("              x_uid='37e889b9-0420-4960-a8dd-9b2d8ff30c57'");
        htmlStream.append("              priority='5'");
        htmlStream.append("              name='ÐÐ°Ñ‚Ð°Ð»Ð¸Ñ Ð‘Ñ‹ÑÑ‚Ñ€Ð¾Ð²Ð°&apos;s Birthday'");
        htmlStream.append("              rsvp='0'");
        htmlStream.append("              fba='F'");
        htmlStream.append("              seq='0'");
        htmlStream.append("              transp='O'>");
        htmlStream.append("            <xprop name='REMINDER'>");
        htmlStream.append("                <xparam name='ACTIVE'");
        htmlStream.append("                        value='true'/>");
        htmlStream.append("                <xparam name='MINUTES'");
        htmlStream.append("                        value='15'/>");
        htmlStream.append("                <xparam name='SOUNDFILE'");
        htmlStream.append("                        value=''/>");
        htmlStream.append("                <xparam name='OPTIONS'");
        htmlStream.append("                        value='0'/>");
        htmlStream.append("                <xparam name='INTERVAL'");
        htmlStream.append("                        value='0'/>");
        htmlStream.append("                <xparam name='REPEATCOUNT'");
        htmlStream.append("                        value='0'/>");
        htmlStream.append("            </xprop>");
        htmlStream.append("            <desc/>");
        htmlStream.append("            <recur>");
        htmlStream.append("                <add>");
        htmlStream.append("                    <rule freq='YEA'>");
        htmlStream.append("                        <interval ival='1'/>");
        htmlStream.append("                        <bymonthday modaylist='28'/>");
        htmlStream.append("                        <bymonth molist='5'/>");
        htmlStream.append("                    </rule>");
        htmlStream.append("                    <dates>");
        htmlStream.append("                        <dtval>");
        htmlStream.append("                            <s d='19720528'/>");
        htmlStream.append("                        </dtval>");
        htmlStream.append("                    </dates>");
        htmlStream.append("                </add>");
        htmlStream.append("            </recur>");
        htmlStream.append("            <s d='19720528'/>");
        htmlStream.append("            <e d='19720528'/>");
        htmlStream.append("        </comp>");
        htmlStream.append("    </inv>");
        htmlStream.append("    <replies/>");
        htmlStream.append("</appt>");

        Document doc = reader.read(new StringReader(htmlStream.toString()));
        Element appt = doc.getRootElement();
        Calendar cal = CalendarUtils.getInstance().asCalendar(appt, true, TimeZone.getTimeZone("Europe/Moscow"));
    }
    
    @Test(expected = NullPointerException.class)
    public void vCalendarNullCharsetTest() throws ConverterException{
        StringBuilder vcal=new StringBuilder();
        vcal.append("BEGIN:VCALENDAR\n");
        vcal.append("VERSION:1.0\n");
        vcal.append("BEGIN:VEVENT\n");
        vcal.append("DTSTART:20061207T071000Z\n");
        vcal.append("DTEND:20061207T091000Z\n");
        vcal.append("DESCRIPTION:\n");
        vcal.append("SUMMARY;ENCODING=QUOTED-PRINTABLE:Tandl=E4kare\n");
        vcal.append("LOCATION:\n");
        vcal.append("LAST-MODIFIED:20080304T200516Z\n");
        vcal.append("X-SONYERICSSON-DST:0\n");
        vcal.append("X-IRMC-LUID:000000010029\n");
        vcal.append("AALARM:\n");
        vcal.append("DALARM:\n");
        vcal.append("END:VEVENT\n");
        vcal.append("END:VCALENDAR\n");
        
        CalendarUtils.convertFrom(PhoneDependedConverter.VCAL_TYPE, "1.0", vcal.toString().getBytes(), null, null);
    }
    
    @Test
    public void task1Test() throws ConverterException, IOException, SoapRequestException, SyncSourceException{
    	StringBuilder htmlStream=new StringBuilder();
		htmlStream.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    	htmlStream.append("<task>");
    	htmlStream.append("<Folder>DEFAULT_FOLDER</Folder>");
    	htmlStream.append("<StartDate>2008-02-20</StartDate>");
    	htmlStream.append("<ActualWork>0</ActualWork>");
    	htmlStream.append("<BillingInformation/>");
    	htmlStream.append("<Body>some body");
    	htmlStream.append("</Body>");
    	htmlStream.append("<Categories>business</Categories>");
    	htmlStream.append("<Companies/>");
    	htmlStream.append("<Complete>1</Complete>");
    	htmlStream.append("<ContactNames/>");
    	htmlStream.append("<DateCompleted>2008-02-21</DateCompleted>");
    	htmlStream.append("<DueDate>2008-02-20</DueDate>");
    	htmlStream.append("<Importance>1</Importance>");
    	htmlStream.append("<IsRecurring>0</IsRecurring>");
    	htmlStream.append("<Mileage/>");
    	htmlStream.append("<NoAging>0</NoAging>");
    	htmlStream.append("<Owner>Valeriy Gelenava</Owner>");
    	htmlStream.append("<PercentComplete>100</PercentComplete>");
    	htmlStream.append("<ReminderSet>0</ReminderSet>");
    	htmlStream.append("<ReminderSoundFile/>");
    	htmlStream.append("<ReminderTime/>");
    	htmlStream.append("<Role/>");
    	htmlStream.append("<SchedulePlusPriority/>");
    	htmlStream.append("<Sensitivity>0</Sensitivity>");
    	htmlStream.append("<Status>2</Status>");
    	htmlStream.append("<Subject>my subject</Subject>");
    	htmlStream.append("<TeamTask>0</TeamTask>");
    	htmlStream.append("<TotalWork>0</TotalWork>");
    	htmlStream.append("<UnRead>0</UnRead>");
    	htmlStream.append("</task>");
    	byte[] content;
    	content= htmlStream.toString().getBytes();
		Calendar cal = CalendarUtils.convertFrom(PhoneDependedConverter.SIFE_TYPE,
                null,
                content,
                null,
                "UTF-8");
    	manager.addItem(cal);
    }
    
    @Test(expected=NullPointerException.class)
    public void task2Test() throws Throwable{
        // TODO I don't remember that check this test
        // looks like it's was issue of  jsse of funambol, but now it doesn't
        StringBuilder htmlStream=new StringBuilder();
        htmlStream.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        htmlStream.append("<task>\n");
        htmlStream.append("<Folder>DEFAULT_FOLDER</Folder>\n");
        htmlStream.append("<StartDate/>\n");
        htmlStream.append("<ActualWork>0</ActualWork>\n");
        htmlStream.append("<BillingInformation/>\n");
        htmlStream.append("<Body>sssssss</Body>\n");
        htmlStream.append("<Categories/>\n");
        htmlStream.append("<Companies/>\n");
        htmlStream.append("<Complete>0</Complete>\n");
        htmlStream.append("<ContactNames/>\n");
        htmlStream.append("<DateCompleted/>\n");
        htmlStream.append("<DueDate>2008-03-23</DueDate>\n");
        htmlStream.append("<Importance>1</Importance>\n");
        htmlStream.append("<IsRecurring>1</IsRecurring>\n");
        htmlStream.append("<Mileage/>\n");
        htmlStream.append("<NoAging>0</NoAging>\n");
        htmlStream.append("<Owner>Ð‘Ð»Ð¸Ð·Ð½ÐµÑ† Ð Ð¾Ð¼Ð°Ð½</Owner>\n");
        htmlStream.append("<PercentComplete>0</PercentComplete>\n");
        htmlStream.append("<ReminderSet>0</ReminderSet>\n");
        htmlStream.append("<ReminderSoundFile/>\n");
        htmlStream.append("<ReminderTime>20080323T090000</ReminderTime>\n");
        htmlStream.append("<Role/>\n");
        htmlStream.append("<SchedulePlusPriority/>\n");
        htmlStream.append("<Sensitivity>0</Sensitivity>\n");
        htmlStream.append("<Status>0</Status>\n");
        htmlStream.append("<Subject>Task</Subject>\n");
        htmlStream.append("<TeamTask>0</TeamTask>\n");
        htmlStream.append("<TotalWork>0</TotalWork>\n");
        htmlStream.append("<UnRead>0</UnRead>\n");
        htmlStream.append("<RecurrenceType>1</RecurrenceType>\n");
        htmlStream.append("<Interval>1</Interval>\n");
        htmlStream.append("<MonthOfYear>0</MonthOfYear>\n");
        htmlStream.append("<DayOfMonth>0</DayOfMonth>\n");
        htmlStream.append("<DayOfWeekMask>7</DayOfWeekMask>\n");
        htmlStream.append("<Instance>0</Instance>\n");
        htmlStream.append("<PatternStartDate>2008-03-23</PatternStartDate>\n");
        htmlStream.append("<NoEndDate>0</NoEndDate>\n");
        htmlStream.append("<PatternEndDate>20080412T200000Z</PatternEndDate>\n");
        htmlStream.append("<Occurrences>10</Occurrences>\n");
        htmlStream.append("</task>\n");
        byte[] content;
        content= htmlStream.toString().getBytes();
        Calendar cal = CalendarUtils.convertFrom(PhoneDependedConverter.SIFE_TYPE,
                null,
                content,
                null,
                "UTF-8");
        manager.addItem(cal);
    }
    
    @Test
    public void sifc4Test() throws Throwable
    {
        StringBuilder htmlStream=new StringBuilder();
        htmlStream.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        htmlStream.append("<appointment>\n");
        htmlStream.append("<Folder>DEFAULT_FOLDER</Folder>\n");
        htmlStream.append("<Start>20080412T063000Z</Start>\n");
        htmlStream.append("<End>20080412T150000Z</End>\n");
        htmlStream.append("<AllDayEvent>0</AllDayEvent>\n");
        htmlStream.append("<BillingInformation/>\n");
        htmlStream.append("<Body>sdhhcjsjkdnc\n");
        htmlStream.append("</Body>\n");
        htmlStream.append("<BusyStatus>1</BusyStatus>\n");
        htmlStream.append("<Categories/>\n");
        htmlStream.append("<Companies>test@korusconsulting.ru</Companies>\n");
        htmlStream.append("<Importance>2</Importance>\n");
        htmlStream.append("<IsRecurring>0</IsRecurring>\n");
        htmlStream.append("<Location>aaaa</Location>\n");
        htmlStream.append("<MeetingStatus>0</MeetingStatus>\n");
        htmlStream.append("<Mileage/>\n");
        htmlStream.append("<NoAging>0</NoAging>\n");
        htmlStream.append("<OptionalAttendees/>\n");
        htmlStream.append("<ReminderMinutesBeforeStart>15</ReminderMinutesBeforeStart>\n");
        htmlStream.append("<ReminderSet>0</ReminderSet>\n");
        htmlStream.append("<ReminderSoundFile/>\n");
        htmlStream.append("<ReplyTime/>\n");
        htmlStream.append("<Sensitivity>0</Sensitivity>\n");
        htmlStream.append("<Subject>ssss</Subject>\n");
        htmlStream.append("<UnRead>0</UnRead>\n");
        htmlStream.append("</appointment>\n");

        
        byte[] content = htmlStream.toString().getBytes();
        Calendar cal = CalendarUtils.convertFrom(PhoneDependedConverter.SIFE_TYPE,
                null,
                content,
                null,
                "UTF-8");
        manager.addItem(cal);
    }
    
    @Test
    public void taskRecuring5Test() throws ConverterException, IOException, SoapRequestException, SyncSourceException{
        StringBuilder htmlStream=new StringBuilder();
        htmlStream.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        htmlStream.append("<task>\n");
        htmlStream.append("<SIFVersion>1.1</SIFVersion>\n");
        htmlStream.append("<ActualWork>0</ActualWork>\n");
        htmlStream.append("<BillingInformation/>\n");
        htmlStream.append("<Body>sssssss\n");
        htmlStream.append("</Body>\n");
        htmlStream.append("<Categories/>\n");
        htmlStream.append("<Companies/>\n");
        htmlStream.append("<Complete>0</Complete>\n");
        htmlStream.append("<DateCompleted/>\n");
        htmlStream.append("<DueDate>2008-02-24</DueDate>\n");
        htmlStream.append("<Folder>DEFAULT_FOLDER</Folder>\n");
        htmlStream.append("<Importance>1</Importance>\n");
        htmlStream.append("<IsRecurring>1</IsRecurring>\n");
        htmlStream.append("<Mileage/>\n");
        htmlStream.append("<PercentComplete>0</PercentComplete>\n");
        htmlStream.append("<ReminderSet>0</ReminderSet>\n");
        htmlStream.append("<ReminderSoundFile/>\n");
        htmlStream.append("<ReminderTime/>\n");
        htmlStream.append("<Sensitivity>0</Sensitivity>\n");
        htmlStream.append("<StartDate/>\n");
        htmlStream.append("<Status>0</Status>\n");
        htmlStream.append("<Subject>Task</Subject>\n");
        htmlStream.append("<TeamTask>0</TeamTask>\n");
        htmlStream.append("<TotalWork>0</TotalWork>\n");
        htmlStream.append("<DayOfMonth>0</DayOfMonth>\n");
        htmlStream.append("<DayOfWeekMask>7</DayOfWeekMask>\n");
        htmlStream.append("<Instance>0</Instance>\n");
        htmlStream.append("<Interval>1</Interval>\n");
        htmlStream.append("<MonthOfYear>0</MonthOfYear>\n");
        htmlStream.append("<NoEndDate>0</NoEndDate>\n");
        htmlStream.append("<Occurrences>22</Occurrences>\n");
        htmlStream.append("<PatternEndDate>2008-04-13</PatternEndDate>\n");
        htmlStream.append("<PatternStartDate>2008-02-20</PatternStartDate>\n");
        htmlStream.append("<RecurrenceType>1</RecurrenceType>\n");
        htmlStream.append("</task>\n");

        byte[] content;
        content= htmlStream.toString().getBytes();
        Calendar cal = CalendarUtils.convertFrom(PhoneDependedConverter.SIFE_TYPE,
                null,
                content,
                null,
                "UTF-8");
        manager.addItem(cal);
    }
    
    @Test
    public void vtask3Test() throws Throwable{
        StringBuilder htmlStream=new StringBuilder();
        htmlStream.append("BEGIN:VCALENDAR\n");
        htmlStream.append("VERSION:1.0\n");
        htmlStream.append("BEGIN:VTODO\n");
        htmlStream.append("DUE:20080331T00000\n");
        htmlStream.append("SUMMARY;CHARSET=UTF-8:test\n");
        htmlStream.append("DESCRIPTION;CHARSET=UTF-8:test\n");
        htmlStream.append("PRIORITY:2\n");
        htmlStream.append("STATUS:NEEDS ACTION\n");
        htmlStream.append("END:VTODO\n");
        htmlStream.append("END:VCALENDAR\n");
        byte[] content;
        content= htmlStream.toString().getBytes();
        Calendar cal = CalendarUtils.convertFrom(PhoneDependedConverter.ICAL_TYPE,
                null,
                content,
                null,
                "UTF-8");
        manager.addItem(cal);
    }
    
    @Test
    public void recurence1Test() throws Throwable{
        StringBuilder htmlStream=new StringBuilder();
        htmlStream.append("<appt id=\"5600\" uid=\"01acafed-92d5-483c-9e5f-778ba0370b96\" \n");
        htmlStream.append("    f=\"e\" \n");
        htmlStream.append("    d=\"1207510159000\" \n");
        htmlStream.append("    rev=\"13998\" \n");
        htmlStream.append("    t=\"\" \n");
        htmlStream.append("    s=\"8143\" \n");
        htmlStream.append("    md=\"1207510159\" \n");
        htmlStream.append("    ms=\"13998\" \n");
        htmlStream.append("    l=\"10\">\n");
        htmlStream.append("    <inv id=\"5599\" \n");
        htmlStream.append("        seq=\"0\" \n");
        htmlStream.append("        compNum=\"0\" \n");
        htmlStream.append("        type=\"appt\">\n");
        htmlStream.append("        <tz id=\"(GMT+01.00) Amsterdam / Berlin / Bern / Rome / Stockholm / Vienna\" stdoff=\"60\" dayoff=\"120\">\n");
        htmlStream.append("            <standard min=\"0\" wkday=\"1\" sec=\"0\" mon=\"10\" hour=\"3\" week=\"-1\"/>\n");
        htmlStream.append("            <daylight min=\"0\" wkday=\"1\" sec=\"0\" mon=\"3\" hour=\"2\" week=\"-1\"/>\n");
        htmlStream.append("        </tz>\n");
        htmlStream.append("        <comp uid=\"01acafed-92d5-483c-9e5f-778ba0370b96\" \n");
        htmlStream.append("            d=\"1207510130000\" \n");
        htmlStream.append("            status=\"CONF\" \n");
        htmlStream.append("            isOrg=\"1\" \n");
        htmlStream.append("            class=\"PUB\" \n");
        htmlStream.append("            loc=\"\" \n");
        htmlStream.append("            compNum=\"0\" \n");
        htmlStream.append("            apptId=\"5600\" \n");
        htmlStream.append("            fb=\"B\" \n");
        htmlStream.append("            calItemId=\"5600\" \n");
        htmlStream.append("            x_uid=\"01acafed-92d5-483c-9e5f-778ba0370b96\" \n");
        htmlStream.append("            name=\"12-13 eccezione -1\" \n");
        htmlStream.append("            rsvp=\"0\" \n");
        htmlStream.append("            fba=\"B\" \n");
        htmlStream.append("            seq=\"0\" \n");
        htmlStream.append("            transp=\"O\">\n");
        htmlStream.append("            <at d=\"Name Surname\" \n");
        htmlStream.append("                a=\"GGG@GGG.GGG\" \n");
        htmlStream.append("                rsvp=\"1\" \n");
        htmlStream.append("                role=\"REQ\" \n");
        htmlStream.append("                ptst=\"NE\" \n");
        htmlStream.append("                url=\"GGG@GGG.GGG\"/>\n");
        htmlStream.append("            <at a=\"DDD@BBB.NNN\" \n");
        htmlStream.append("                rsvp=\"1\" \n");
        htmlStream.append("                role=\"REQ\" \n");
        htmlStream.append("                ptst=\"NE\" \n");
        htmlStream.append("                url=\"EEE@RRR.CCC\"/>\n");
        htmlStream.append("            <fr>Nuova richiesta di riunione: Oggetto:12-13 eccezione -1 Organizzatore: \"Name Surname\" &lt;AAA@BBB.CCC> Ora:12:00:00 - 13:00:00 GMT +01:00 ...</fr>\n");
        htmlStream.append("            <desc>Nuova richiesta diriunione:Oggetto: 12-13 eccezione -1</desc>\n");
        htmlStream.append("            <or d=\"Name Surname\" \n");
        htmlStream.append("                a=\"AAA@BBB.CCC\" \n");
        htmlStream.append("                url=\"AAA@BBB.CCC\"/>\n");
        htmlStream.append("            <recur>\n");
        htmlStream.append("                <add>\n");
        htmlStream.append("                    <rule freq=\"WEE\">\n");
        htmlStream.append("                        <until d=\"20080606\"/>\n");
        htmlStream.append("                        <interval ival=\"1\"/>\n");
        htmlStream.append("                        <byday><wkday day=\"SA\"/></byday>\n");
        htmlStream.append("                    </rule>\n");
        htmlStream.append("                </add>\n");
        htmlStream.append("            </recur>\n");
        htmlStream.append("            <s d=\"20080405T120000\" tz=\"(GMT+01.00) Amsterdam / Berlin / Bern / Rome / Stockholm /Vienna\"/>\n");
        htmlStream.append("            <e d=\"20080405T130000\" tz=\"(GMT+01.00) Amsterdam / Berlin /Bern / Rome / Stockholm / Vienna\"/>\n");
        htmlStream.append("        </comp>\n");
        htmlStream.append("    </inv>\n");
        htmlStream.append("    <inv id=\"5602\" \n");
        htmlStream.append("        recurId=\" \n");
        htmlStream.append("        TZID=(GMT+01.00) Amsterdam / Berlin / Bern / Rome / Stockholm /Vienna:20080405T120000\" \n");
        htmlStream.append("        seq=\"0\" \n");
        htmlStream.append("        compNum=\"0\" \n");
        htmlStream.append("        type=\"appt\">\n");
        htmlStream.append("        <tz id=\"(GMT+01.00) Amsterdam / Berlin / Bern / Rome / Stockholm / Vienna\" stdoff=\"60\" dayoff=\"120\">\n");
        htmlStream.append("            <standard min=\"0\" wkday=\"1\" sec=\"0\" mon=\"10\" hour=\"3\" week=\"-1\"/>\n");
        htmlStream.append("            <daylight min=\"0\" wkday=\"1\" sec=\"0\" mon=\"3\" hour=\"2\" week=\"-1\"/>\n");
        htmlStream.append("        </tz>\n");
        htmlStream.append("        <comp uid=\"01acafed-92d5-483c-9e5f-778ba0370b96\" \n");
        htmlStream.append("            ex=\"1\" \n");
        htmlStream.append("            d=\"1207510159000\" \n");
        htmlStream.append("            status=\"CONF\" \n");
        htmlStream.append("            isOrg=\"1\" \n");
        htmlStream.append("            class=\"PUB\" \n");
        htmlStream.append("            loc=\"\" \n");
        htmlStream.append("            compNum=\"0\" \n");
        htmlStream.append("            apptId=\"5600\" \n");
        htmlStream.append("            fb=\"B\" \n");
        htmlStream.append("            calItemId=\"5600\" \n");
        htmlStream.append("            x_uid=\"01acafed-92d5-483c-9e5f-778ba0370b96\" \n");
        htmlStream.append("            name=\"12-13 eccezione -1\" \n");
        htmlStream.append("            rsvp=\"0\" \n");
        htmlStream.append("            fba=\"B\" \n");
        htmlStream.append("            seq=\"0\" \n");
        htmlStream.append("            transp=\"O\">\n");
        htmlStream.append("            <at d=\"Name Surname\" a=\"DDD@BBB.NNN\" rsvp=\"1\" role=\"REQ\" ptst=\"NE\" url=\"DDD@BBB.NNN\"/>\n");
        htmlStream.append("            <at a=\"DDD@EEE.FFF\" rsvp=\"1\" role=\"REQ\" ptst=\"NE\" url=\"DDD@EEE.FFF\"/>\n");
        htmlStream.append("            <fr> stata modificata una singola istanzadella seguente riunione: Oggetto: 12-13 eccezione -1 Organizzatore: \"NameSurname\" &lt;AAA@BBB.CCC> ...</fr>\n");
        htmlStream.append("            <desc> stata modificata una singolaistanza della seguente riunione:Oggetto: 12-13 eccezione -1</desc>\n");
        htmlStream.append("            <or d=\"Name Surname\" a=\"AAA@BBB.CCC\" url=\"AAA@BBB.CCC\"/>\n");
        htmlStream.append("            <exceptId rangeType=\"1\" \n");
        htmlStream.append("                d=\"20080405T120000\" \n");
        htmlStream.append("                tz=\"(GMT+01.00) Amsterdam / Berlin /Bern / Rome / Stockholm / Vienna\"/>\n");
        htmlStream.append("            <s d=\"20080405T090000\" tz=\"(GMT+01.00) Amsterdam / Berlin / Bern / Rome / Stockholm /Vienna\"/>\n");
        htmlStream.append("            <e d=\"20080405T100000\" tz=\"(GMT+01.00) Amsterdam / Berlin /Bern / Rome / Stockholm /Vienna\"/>\n");
        htmlStream.append("        </comp>\n");
        htmlStream.append("    </inv>\n");
        htmlStream.append("    <replies/>\n");
        htmlStream.append("</appt>\n");

        Document doc = reader.read(new StringReader(htmlStream.toString()));
        Calendar calendar = CalendarUtils.getInstance().asCalendar(doc.getRootElement(), true, TimeZone.getTimeZone("Europe/Moscow"));
        RecurrencePattern rp = calendar.getCalendarContent().getRecurrencePattern();
        

    }
    
    @Test
    public void timezoneDayOffsetTest() throws Throwable{
        StringBuilder htmlStream=new StringBuilder();
        htmlStream.append("<appt d='1222217750000'\n");
        htmlStream.append("                  ms='24271'\n");
        htmlStream.append("                  md='1222217750'\n");
        htmlStream.append("                  f=''\n");
        htmlStream.append("                  nextAlarm='1222365300000'\n");
        htmlStream.append("                  uid='0c6f6151-99fb-4ee7-b5ef-f13116dea6ee'\n");
        htmlStream.append("                  t=''\n");
        htmlStream.append("                  l='10'\n");
        htmlStream.append("                  s='2004'\n");
        htmlStream.append("                  id='12933'\n");
        htmlStream.append("                  rev='24271'>\n");
        htmlStream.append("                <inv type='appt'\n");
        htmlStream.append("                     seq='0'\n");
        htmlStream.append("                     compNum='0'\n");
        htmlStream.append("                     id='12932'>\n");
        htmlStream.append("                    <tz stdoff='-300'\n");
        htmlStream.append("                        id='(GMT-05.00) Bogota / Lima / Quito / Rio Branco'/>\n");
        htmlStream.append("                    <comp d='1222217750000'\n");
        htmlStream.append("                          class='PUB'\n");
        htmlStream.append("                          loc='ANTARTEC'\n");
        htmlStream.append("                          transp='O'\n");
        htmlStream.append("                          seq='0'\n");
        htmlStream.append("                          uid='0c6f6151-99fb-4ee7-b5ef-f13116dea6ee'\n");
        htmlStream.append("                          fb='B'\n");
        htmlStream.append("                          status='CONF'\n");
        htmlStream.append("                          apptId='12933'\n");
        htmlStream.append("                          fba='B'\n");
        htmlStream.append("                          isOrg='1'\n");
        htmlStream.append("                          calItemId='12933'\n");
        htmlStream.append("                          compNum='0'\n");
        htmlStream.append("                          rsvp='0'\n");
        htmlStream.append("                          x_uid='0c6f6151-99fb-4ee7-b5ef-f13116dea6ee'\n");
        htmlStream.append("                          name='Prueba'>\n");
        htmlStream.append("                        <alarm action='DISPLAY'>\n");
        htmlStream.append("                            <trigger>\n");
        htmlStream.append("                                <rel neg='1'\n");
        htmlStream.append("                                     m='5'\n");
        htmlStream.append("                                     related='START'/>\n");
        htmlStream.append("                            </trigger>\n");
        htmlStream.append("                            <desc/>\n");
        htmlStream.append("                        </alarm>\n");
        htmlStream.append("                        <desc/>\n");
        htmlStream.append("                        <or d='Gustavo Adolfo. Higa Miyashiro'\n");
        htmlStream.append("                            a='gustavo.higa@antartec.com'\n");
        htmlStream.append("                            url='gustavo.higa@antartec.com'/>\n");
        htmlStream.append("                        <s d='20080925T130000'\n");
        htmlStream.append("                           tz='(GMT-05.00) Bogota / Lima / Quito / Rio Branco'/>\n");
        htmlStream.append("                        <e d='20080925T140000'\n");
        htmlStream.append("                           tz='(GMT-05.00) Bogota / Lima / Quito / Rio Branco'/>\n");
        htmlStream.append("                    </comp>\n");
        htmlStream.append("                </inv>\n");
        htmlStream.append("                <replies/>\n");
        htmlStream.append("            </appt>\n");
        
        Document doc = reader.read(new StringReader(htmlStream.toString()));
        Calendar calendar = CalendarUtils.getInstance().asCalendar(doc.getRootElement(), true, TimeZone.getTimeZone("Europe/Moscow"));
        
        htmlStream=new StringBuilder();
        htmlStream.append("<appt d='1222217750000'\n");
        htmlStream.append("                  ms='24271'\n");
        htmlStream.append("                  md='1222217750'\n");
        htmlStream.append("                  f=''\n");
        htmlStream.append("                  nextAlarm='1222365300000'\n");
        htmlStream.append("                  uid='0c6f6151-99fb-4ee7-b5ef-f13116dea6ee'\n");
        htmlStream.append("                  t=''\n");
        htmlStream.append("                  l='10'\n");
        htmlStream.append("                  s='2004'\n");
        htmlStream.append("                  id='12933'\n");
        htmlStream.append("                  rev='24271'>\n");
        htmlStream.append("                <inv type='appt'\n");
        htmlStream.append("                     seq='0'\n");
        htmlStream.append("                     compNum='0'\n");
        htmlStream.append("                     id='12932'>\n");
        htmlStream.append("                    <tz stdoff='-300'\n");
        htmlStream.append("                        id='(GMT-05.00) Bogota / Lima / Quito / Rio Branco'/>\n");
        htmlStream.append("                    <comp d='1222217750000'\n");
        htmlStream.append("                          class='PUB'\n");
        htmlStream.append("                          loc='ANTARTEC'\n");
        htmlStream.append("                          transp='O'\n");
        htmlStream.append("                          seq='0'\n");
        htmlStream.append("                          uid='0c6f6151-99fb-4ee7-b5ef-f13116dea6ee'\n");
        htmlStream.append("                          fb='B'\n");
        htmlStream.append("                          status='CONF'\n");
        htmlStream.append("                          apptId='12933'\n");
        htmlStream.append("                          fba='B'\n");
        htmlStream.append("                          isOrg='1'\n");
        htmlStream.append("                          calItemId='12933'\n");
        htmlStream.append("                          compNum='0'\n");
        htmlStream.append("                          rsvp='0'\n");
        htmlStream.append("                          x_uid='0c6f6151-99fb-4ee7-b5ef-f13116dea6ee'\n");
        htmlStream.append("                          name='Prueba'>\n");
        htmlStream.append("                        <alarm action='DISPLAY'>\n");
        htmlStream.append("                            <trigger>\n");
        htmlStream.append("                                <rel neg='1'\n");
        htmlStream.append("                                     m='5'\n");
        htmlStream.append("                                     related='START'/>\n");
        htmlStream.append("                            </trigger>\n");
        htmlStream.append("                            <desc/>\n");
        htmlStream.append("                        </alarm>\n");
        htmlStream.append("                        <desc/>\n");
        htmlStream.append("                        <or d='Gustavo Adolfo. Higa Miyashiro'\n");
        htmlStream.append("                            a='gustavo.higa@antartec.com'\n");
        htmlStream.append("                            url='gustavo.higa@antartec.com'/>\n");
        htmlStream.append("                        <s d='20080925T130000'\n");
        htmlStream.append("                           tz='(GMT-05.00) Bogota / Lima / Quito / Rio Branco'/>\n");
        htmlStream.append("                        <e d='20080925T140000'\n");
        htmlStream.append("                           tz='(GMT-05.00) Bogota / Lima / Quito / Rio Branco'/>\n");
        htmlStream.append("                    </comp>\n");
        htmlStream.append("                </inv>\n");
        htmlStream.append("                <replies/>\n");
        htmlStream.append("            </appt>\n");
        doc = reader.read(new StringReader(htmlStream.toString()));
        calendar = CalendarUtils.getInstance().asCalendar(doc.getRootElement(), true, null);
    }
    
    @Test
    public void task001Test() throws Throwable{
        StringBuilder htmlStream=new StringBuilder();
        htmlStream.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        htmlStream.append("            <task d='1237821512000' ms='2645' md='1237821512' f=''\n");
        htmlStream.append("                uid='60f8f516-1fa2-4880-8e29-3fcf34166f24' t='' l='15' s='1896'\n");
        htmlStream.append("                id='1433' rev='2645'>\n");
        htmlStream.append("                <inv type='task' seq='1' compNum='0' id='1432'>\n");
        htmlStream.append("                    <tz dayoff='120' stdoff='60'\n");
        htmlStream.append("                        id='(GMT+01.00) Amsterdam / Berlin / Bern / Rome / Stockholm / Vienna'>\n");
        htmlStream.append("                        <standard sec='0' hour='3' wkday='1' min='0'\n");
        htmlStream.append("                            mon='10' week='-1' />\n");
        htmlStream.append("                        <daylight sec='0' hour='2' wkday='1' min='0'\n");
        htmlStream.append("                            mon='3' week='-1' />\n");
        htmlStream.append("                    </tz>\n");
        htmlStream.append("                    <comp d='1237821512000' class='PUB' loc='Testort'\n");
        htmlStream.append("                        seq='1' uid='60f8f516-1fa2-4880-8e29-3fcf34166f24' status='INPR'\n");
        htmlStream.append("                        allDay='1' isOrg='1' calItemId='1433' percentComplete='20' url=''\n");
        htmlStream.append("                        compNum='0' rsvp='0' x_uid='60f8f516-1fa2-4880-8e29-3fcf34166f24'\n");
        htmlStream.append("                        priority='1' name='Test'>\n");
        htmlStream.append("                        <fr>Testtext</fr>\n");
        htmlStream.append("                        <desc>Testtext</desc>\n");
        htmlStream.append("                        <or d='Peter XXXX' a='unixfan@XXXX'\n");
        htmlStream.append("                            url='unixfan@XXXX' />\n");
        htmlStream.append("                        <s d='20090323' />\n");
        htmlStream.append("                        <e d='20090323' />\n");
        htmlStream.append("                    </comp>\n");
        htmlStream.append("                </inv>\n");
        htmlStream.append("                <replies />\n");
        htmlStream.append("            </task>\n");
        
        //This happen because invalid zimbra response 
        //comp tag has to contain fba attribute
        // busyStatus sets to invalid number= -1 (CalendarUtils.java:691) => null pointer exception while convert to calendar 
        Document doc = reader.read(new StringReader(htmlStream.toString()));
        Calendar calendar = CalendarUtils.getInstance().asCalendar(doc.getRootElement(), true, TimeZone.getTimeZone("Europe/Moscow"));
        CalendarUtils.convertTo(PhoneDependedConverter.ICAL_TYPE, null, calendar, TimeZone.getTimeZone("Europe/Moscow"), "UTF-8");
    }
    
    

    
    @After
    public void tearDown() throws Exception {
        if (zimbraPort != null)
            zimbraPort.close();
    }
}
