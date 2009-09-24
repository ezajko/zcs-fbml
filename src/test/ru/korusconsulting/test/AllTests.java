package ru.korusconsulting.test;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for ru.korusconsulting.test");
        //$JUnit-BEGIN$
        suite.addTest(new JUnit4TestAdapter(CalendarUtilsTest.class));
        suite.addTest(new JUnit4TestAdapter(PhoneDependedConverterTest.class));
        suite.addTest(new JUnit4TestAdapter(SyncClientTests.class));
        //$JUnit-END$
        return suite;
    }

}
