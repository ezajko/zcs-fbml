package ru.korusconsulting.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import junit.framework.Assert;
import ru.korusconsulting.connector.funambol.ContactUtils;
import ru.korusconsulting.connector.funambol.PhoneDependedConverter;

import com.funambol.common.pim.contact.Contact;
import com.funambol.common.pim.contact.Photo;
import com.funambol.common.pim.converter.ConverterException;

public class TestContacts {
    public static HashMap<String, Contact> contacts = new HashMap<String, Contact>();

    static {
        File contactsDir = new File("test/contacts");
        File[] listFiles = contactsDir.listFiles();
        for (File file : listFiles) {
            FileInputStream fis;
            Properties prop = new Properties();
            String name = file.getName();
            int lastIndexOf = name.lastIndexOf('.');
            if (lastIndexOf == -1) {
                continue;
            }
            if (!"properties".equalsIgnoreCase(name.substring(lastIndexOf + 1))) {
                continue;
            }
            try {
                fis = new FileInputStream(file);
                prop.load(fis);
                fis.close();
                Contact c = ContactUtils.createContactFromProperties(prop);
                String photo = prop.getProperty("photo");
                if (photo != null && !photo.trim().equals("")) {
                    File photoFile = new File(photo);
                    fis = new FileInputStream(photoFile);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte buffer[] = new byte[1024];
                    int readed;
                    while ((readed = fis.read(buffer, 0, 1024)) != -1) {
                        baos.write(buffer, 0, readed);
                    }
                    fis.close();
                    Photo photoObject = new Photo();
                    photoObject.setImage(baos.toByteArray());
                    c.getPersonalDetail().setPhotoObject(photoObject);
                }
                contacts.put(name.substring(0, lastIndexOf), c);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Contact get() {
        if (contacts.size() == 0)
            return null;

        int max = contacts.size();
        double random = Math.abs(Math.sinh(Math.exp(System.currentTimeMillis())));
        int cur = (int) (random * max);
        cur = Math.min(cur, max - 1);
        Iterator<Contact> iter = contacts.values().iterator();
        for (int i = 0; i < cur; i++) {
            iter.next();
        }
        return iter.next();
    }
    
    public static Contact get(String name) {
        return contacts.get(name);
    }
    
    public static boolean equals(Contact c1, Contact c2) throws Throwable{
        try {
            String uid1 = c1.getUid();
            String uid2 = c2.getUid();
            c1.setUid("0");
            c2.setUid("0");
//            c1.getPersonalDetail().setPhotoObject(null);
//            c2.getPersonalDetail().setPhotoObject(null);
            Photo photo1 = c1.getPersonalDetail().getPhotoObject();
            Photo photo2 = c2.getPersonalDetail().getPhotoObject();
            c1.getPersonalDetail().setPhotoObject(null);
            c2.getPersonalDetail().setPhotoObject(null);
            byte[] b1 = ContactUtils.convertTo(PhoneDependedConverter.VCARD_TYPE, null, c1, null, "UTF-8");
            byte[] b2 = ContactUtils.convertTo(PhoneDependedConverter.VCARD_TYPE, null, c2, null, "UTF-8");
            String s1 = new String(b1);
            String s2 = new String(b2);
            c1.setUid(uid1);
            c2.setUid(uid2);
            c1.getPersonalDetail().setPhotoObject(photo1);
            c2.getPersonalDetail().setPhotoObject(photo2);
            if((photo1==null&&photo2!=null)||(photo2==null&&photo1!=null)) {
                System.out.println("photo1 not equals photo2");
                return false;
            }
            boolean eq = s1.equals(s2);
            if(!eq){
                System.out.println(s1);
                System.out.println();
                System.out.println(s2);
            }
            return eq;
        } catch (ConverterException e) {
            throw new Throwable(e);
        }
    }
    
    public static void assertEquals(ArrayList<Contact> expected, Contact... contacts) throws Throwable{
        Assert.assertTrue("Cann't find any contacts in the zimbra",expected.size()>0);
        Assert.assertTrue("Too many contacts in the zimbra",expected.size()<contacts.length+1);
        Assert.assertTrue("Too small contacts in the zimbra",expected.size()==contacts.length);
        boolean rows[]=new boolean[expected.size()];
        boolean columns[]=new boolean[expected.size()];
        boolean eqGlobal=true;
        boolean eq;
        for (int i=0;i<expected.size();i++) {
            eq=false;
            for(int j=0;i<expected.size();i++){
                if(rows[i]||columns[j]){
                    eq=true;
                    continue;
                }
                eq=equals(contacts[i],expected.get(j));
                if(eq){
//                    contacts[i].setUid(expected.get(j).getUid());
                    rows[i]=true;
                    columns[j]=true;
                    break;
                }
            }
            eqGlobal&=eq;
            
        }
        Assert.assertTrue("Contact in the zimbra not equals contact that was loaded",eqGlobal);
    }
}
