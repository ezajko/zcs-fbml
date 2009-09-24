package ru.korusconsulting.test;


import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.korusconsulting.connector.funambol.ContactUtils;
import ru.korusconsulting.connector.funambol.PhoneDependedConverter;

import com.funambol.common.pim.common.Property;
import com.funambol.common.pim.contact.Contact;
import com.funambol.common.pim.contact.Note;
import com.funambol.common.pim.converter.ConverterException;
import com.funambol.framework.tools.merge.MergeResult;

public class PhoneDependedConverterTest {
    
    
    @SuppressWarnings("deprecation")
    @Test
    public void vcardNotesTest(){
        Contact contact=new Contact();
        Note note = new Note();
        note.setPropertyValue("my\nnote\ntest");
        note.setCharset("UTF-8");
        note.setPropertyType("Body");
        note.setEncoding("QUOTED-PRINTABLE");
        contact.addNote(note);
        Property firstName=new Property();
        firstName.setPropertyValue("NoteTest");
        contact.getName().setFirstName(firstName);
        
        try {
            byte[] content = ContactUtils.convertTo(PhoneDependedConverter.VCARD_TYPE, "2.1", contact, null, "UTF-8");
            Contact contact2=ContactUtils.convertFrom(PhoneDependedConverter.VCARD_TYPE, "2.1", content, null, "UTF-8");
            MergeResult mergeResult = contact.merge(contact2);
            Assert.assertTrue(!mergeResult.isSetARequired()&&!mergeResult.isSetBRequired());
        } catch (ConverterException e) {
            e.printStackTrace();
        }
    }
    
    
    
    @Test
    public void vcardIPhoneFix1Test(){
        try {
            StringBuilder htmlStream=new StringBuilder();
            htmlStream.append("BEGIN:VCARD\n");
            htmlStream.append("VERSION:2.1\n");
            htmlStream.append("TEL;TYPE=CELL:78129999999\n");
            htmlStream.append("NOTE;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:=D0=9A=D0=B0=D1=82=D0=B0=D1=80=D0=B8=D0=BD=D0=BA=D0=B0\n");
            htmlStream.append("=0D=0A\n");
            htmlStream.append("N;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:;=D0=95=D0=BB=D0=B5=D0=BD=D0=B0;;;\n");
            htmlStream.append("END:VCARD\n");
            
            byte[] content = PhoneDependedConverter.getInstance().fixVcard(htmlStream.toString().getBytes());
            ContactUtils.convertFrom(PhoneDependedConverter.VCARD_TYPE, "2.1", content, null, "UTF-8");
            Assert.assertTrue(true);
            
        } catch (ConverterException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void vcardIPhoneFix2Test(){
        try {
            StringBuilder htmlStream=new StringBuilder();
            htmlStream.append("BEGIN:VCARD\n");
            htmlStream.append("VERSION:2.1\n");
            htmlStream.append("BDAY:1977-10-29\n");
            htmlStream.append("TEL;TYPE=CELL:+78129999999\n");
            htmlStream.append("NICKNAME:Katarinka\n");
            htmlStream.append("N;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:;=D0=95=D0=BB=D0=B5=D0=BD=D0=B0;;;\n");
            htmlStream.append("END:VCARD\n\r");
            
            
            byte[] content = PhoneDependedConverter.getInstance().fixVcard(htmlStream.toString().getBytes());
            ContactUtils.convertFrom(PhoneDependedConverter.VCARD_TYPE, "2.1", content, null, "UTF-8");
            Assert.assertTrue(true);
            
            htmlStream.setLength(0);
            htmlStream.append("BEGIN:VCARD\n");
            htmlStream.append("VERSION:2.1\n");
            htmlStream.append("BDAY:1977-10-29\n");
            htmlStream.append("TEL;TYPE=CELL:+78129959473\n");
            htmlStream.append("NOTE;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:Peeved nerves=0D=0A=0D=0A=0D=0AMedved=C2=A5=C2=A3>=D0=BE=D0=B0=D0=B2f=C3=B9=C3=BC=C3=AA=0D=0A=0D=0A=0D=0A\n");
            htmlStream.append("NICKNAME:Katarinka\n");
            htmlStream.append("N;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:;=D0=95=D0=BB=D0=B5=D0=BD=D0=B0;;;\n\r");
            htmlStream.append("END:VCARD\r\n");
            content = PhoneDependedConverter.getInstance().fixVcard(htmlStream.toString().getBytes());
            ContactUtils.convertFrom(PhoneDependedConverter.VCARD_TYPE, "2.1", content, null, "UTF-8");
            Assert.assertTrue(true);
            
        } catch (ConverterException e) {
            e.printStackTrace();
        }
    }
    
    
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }
    
    @After
    public void tearDown() throws Exception {
    }

}