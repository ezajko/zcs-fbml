/*
 * Copyright (C) 2008 KorusConsulting Author: Roman Bliznets
 * <RBliznets@korusconsulting.ru> This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA
 * 02139, USA.
 */
package ru.korusconsulting.connector.funambol;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import ru.korusconsulting.connector.base.XMLUtil;

import com.funambol.common.pim.common.Property;
import com.funambol.common.pim.contact.BusinessDetail;
import com.funambol.common.pim.contact.Contact;
import com.funambol.common.pim.contact.ContactDetail;
import com.funambol.common.pim.contact.Email;
import com.funambol.common.pim.contact.Note;
import com.funambol.common.pim.contact.PersonalDetail;
import com.funambol.common.pim.contact.Phone;
import com.funambol.common.pim.contact.Title;
import com.funambol.common.pim.contact.WebPage;
import com.funambol.common.pim.converter.ContactToSIFC;
import com.funambol.common.pim.converter.ContactToVcard;
import com.funambol.common.pim.converter.ConverterException;
import com.funambol.common.pim.sif.SIFCParser;
import com.funambol.common.pim.vcard.ParseException;
import com.funambol.common.pim.vcard.VcardParser;
import com.funambol.framework.logging.FunambolLogger;
import com.funambol.framework.logging.FunambolLoggerFactory;

public class ContactUtils {
    public static final String FILE_AS = "fileAs";
    public static final String SPOUSE = "spouse";
    public static final String PROFESSION = "profession";
    public static final String NICKNAME = "nickname";
    public static final String MANAGER_NAME = "managerName";
    public static final String OFFICE = "office";
    public static final String INITIALS = "initials";
    public static final String GENDER = "gender";
    public static final String DEPARTMENT = "department";
    public static final String CHILDREN = "children";
    public static final String BIRTHDAY = "birthday";
    public static final String ASSISTANT_NAME = "assistantName";
    public static final String ANNIVERSARY = "anniversary";
    public static final String WORK_URL = "workURL";
    public static final String WORK_STREET = "workStreet";
    public static final String WORK_STATE = "workState";
    public static final String WORK_POSTAL_CODE = "workPostalCode";
    public static final String WORK_PHONE2 = "workPhone2";
    public static final String WORK_PHONE = "workPhone";
    public static final String WORK_FAX = "workFax";
    public static final String WORK_COUNTRY = "workCountry";
    public static final String WORK_CITY = "workCity";
    public static final String PAGER = "pager";
    public static final String OTHER_PHONE = "otherPhone";
    public static final String OTHER_FAX = "otherFax";
    public static final String NOTES = "notes";
    public static final String NAME_SUFFIX = "nameSuffix";
    public static final String NAME_PREFIX = "namePrefix";
    public static final String MOBILE_PHONE = "mobilePhone";
    public static final String MIDDLE_NAME = "middleName";
    public static final String LAST_NAME = "lastName";
    public static final String JOB_TITLE = "jobTitle";
    public static final String HOME_URL = "homeURL";
    public static final String HOME_STREET = "homeStreet";
    public static final String HOME_STATE = "homeState";
    public static final String HOME_POSTAL_CODE = "homePostalCode";
    public static final String HOME_PHONE2 = "homePhone2";
    public static final String HOME_PHONE = "homePhone";
    public static final String HOME_FAX = "homeFax";
    public static final String HOME_COUNTRY = "homeCountry";
    public static final String HOME_CITY = "homeCity";
    public static final String FIRST_NAME = "firstName";
    public static final String EMAIL3 = "email3";
    public static final String EMAIL2 = "email2";
    public static final String EMAIL = "email";
    public static final String COMPANY_PHONE = "companyPhone";
    public static final String COMPANY = "company";
    public static final String CAR_PHONE = "carPhone";

    private static FunambolLogger logger = FunambolLoggerFactory.getLogger("funambol.zimbra.internal.CalendarUtils");

    /*
     *
     * initial set of field names...
     *
     * id/modified-date present in the data model but not listed here
     *
     * -------- general -------- assistantPhone callbackPhone carPhone company
     * companyPhone email email2 email3 fileAs (1 is default) 1 = Last, First 2 =
     * First Last 3 = Company 4 = Last, First (Company) 5 = First Last (Company)
     * 6 = Company (Last, First) 7 = Company (First Last)
     *
     * firstName homeCity homeCountry homeFax homePhone homePhone2
     * homePostalCode homeState homeStreet homeURL jobTitle lastName middleName
     * mobilePhone namePrefix nameSuffix notes otherCity otherCountry otherFax
     * otherPhone otherPostalCode otherState otherStreet otherURL pager workCity
     * workCountry workFax workPhone workPhone2 workPostalCode workState
     * workStreet workURL
     *
     * ----------------------------------- other details
     * (may-or-may-not-implement all or some)
     * ----------------------------------- anniversary assistantName birthday
     * children customerId department gender initials office managerName
     * nickname profession spouse
     *
     */
    /**
     * Convert Contact object into Element for zimbra soap request
     *
     * @param c -
     *            the contact object
     * @param df -
     *            document factory
     * @param namespace -
     *            namespace of element
     * @param u
     *            update flag - if present then add empty <a n="..."/> elements
     * @return element of document
     */
    public static Element asElement(Contact c, DocumentFactory df, String namespace, boolean u) {
        Element contact = df.createElement("cn", namespace);

        BusinessDetail businessDetail = c.getBusinessDetail();
        PersonalDetail personalDetail = c.getPersonalDetail();
        List<Phone> bisPhones = businessDetail.getPhones();
        List<Phone> perPhones = personalDetail.getPhones();

        List webPages;

        // addContactAttr("assistantPhone",
        // find(bisPhones,Def.ASSISTANT_PHONE_NUM), df, contact, u);
        // addContactAttr("callbackPhone", find(phones,Def.ASSISTANT_PHONE_NUM),
        // df, contact, u);
        addContactAttr(CAR_PHONE, find(perPhones, Def.PERSONAL_CAR_PHONE_NUM), df, contact, u);
        addContactAttr(COMPANY, businessDetail.getCompany(), df, contact, u);
        addContactAttr(COMPANY_PHONE, find(bisPhones, Def.COMPANY_MAIN_PHONE_NUM), df, contact, u);
        List<Email> emails;
        emails = personalDetail.getEmails();
        addContactAttr(EMAIL, find(emails, Def.PERSONAL_EMAIL_1_ADDRESS), df, contact, u);
        addContactAttr(EMAIL2, find(emails, Def.PERSONAL_EMAIL_2_ADDRESS), df, contact, u);
        emails = businessDetail.getEmails();
        addContactAttr(EMAIL3, find(emails, Def.EMAIL_3_ADDRESS), df, contact, u);
        addContactAttr(FIRST_NAME, c.getName().getFirstName(), df, contact, u);
        addContactAttr(HOME_CITY, personalDetail.getAddress().getCity(), df, contact, u);
        addContactAttr(HOME_COUNTRY, personalDetail.getAddress().getCountry(), df, contact, u);
        addContactAttr(HOME_FAX, find(perPhones, Def.PERSONAL_HOME_FAX_NUMBER), df, contact, u);
        addContactAttr(HOME_PHONE, find(perPhones, Def.PERSONAL_HOME_PHONE_NUM), df, contact, u);
        addContactAttr(HOME_PHONE2, find(perPhones, Def.PERSONAL_HOME_2_PHONE_NUM), df, contact, u);
        addContactAttr(HOME_POSTAL_CODE, personalDetail.getAddress().getPostalCode(), df, contact,
                u);
        addContactAttr(HOME_STATE, personalDetail.getAddress().getState(), df, contact, u);
        addContactAttr(HOME_STREET, personalDetail.getAddress().getStreet(), df, contact, u);
        webPages = personalDetail.getWebPages();
        if (webPages != null) {
            Property webPage = find(webPages, Def.PERSONAL_WEBPAGE);
            webPage = isEmpty(webPage) ? find(webPages, Def.PERSONAL_WEBPAGE_RESERVE) : webPage;
            addContactAttr(HOME_URL, webPage, df, contact, u);
        }
        addContactAttr(JOB_TITLE, find(businessDetail.getTitles(), Def.BUSINESS_TITLE), df,
                contact, u);
        addContactAttr(LAST_NAME, c.getName().getLastName(), df, contact, u);
        addContactAttr(MIDDLE_NAME, c.getName().getMiddleName(), df, contact, u);
        addContactAttr(MOBILE_PHONE, find(perPhones, Def.PERSONAL_MOBILE_PHONE_NUM), df, contact, u);
        addContactAttr(NAME_PREFIX, c.getName().getSalutation(), df, contact, u);
        addContactAttr(NAME_SUFFIX, c.getName().getSuffix(), df, contact, u);
        List notes = c.getNotes();
        if (notes != null) {
            StringBuilder note = new StringBuilder();
            for (int i = 0; i < notes.size(); i++) {
                note.append(((Note) notes.get(i)).getPropertyValueAsString());
            }
            addContactAttr(NOTES, note.toString(), df, contact, u);
        }
        // addContactAttr("otherCity" , (Property)null, df, contact, u);
        // addContactAttr("otherCountry" , (Property)null, df, contact, u);
        addContactAttr(OTHER_FAX, find(perPhones, Def.PERSONAL_OTHER_FAX_NUMBER), df, contact, u);
        addContactAttr(OTHER_PHONE, find(perPhones, Def.PERSONAL_OTHER_PHONE_NUM), df, contact, u);
        // addContactAttr("otherPostalCode" , (Property)null, df, contact, u);
        // addContactAttr("otherState" , (Property)null, df, contact, u);
        // addContactAttr("otherStreet" , (Property)null, df, contact, u);
        // addContactAttr("otherURL" , (Property)null, df, contact, u);
        addContactAttr(PAGER, find(bisPhones, Def.PAGER_NUMBER), df, contact, u);
        addContactAttr(WORK_CITY, businessDetail.getAddress().getCity(), df, contact, u);
        addContactAttr(WORK_COUNTRY, businessDetail.getAddress().getCountry(), df, contact, u);
        addContactAttr(WORK_FAX, find(bisPhones, Def.BUSINESS_FAX_NUM), df, contact, u);
        addContactAttr(WORK_PHONE, find(bisPhones, Def.BUSINESS_PHONE_NUM), df, contact, u);
        addContactAttr(WORK_PHONE2, find(bisPhones, Def.BUSINESS_2_PHONE_NUM), df, contact, u);
        addContactAttr(WORK_POSTAL_CODE, businessDetail.getAddress().getPostalCode(), df, contact,
                u);
        addContactAttr(WORK_STATE, businessDetail.getAddress().getState(), df, contact, u);
        addContactAttr(WORK_STREET, businessDetail.getAddress().getStreet(), df, contact, u);
        webPages = businessDetail.getWebPages();
        if (webPages != null)
            addContactAttr(WORK_URL, (Property) (webPages.isEmpty() ? null : webPages.get(0)), df,
                    contact, u);

        addContactAttr(ANNIVERSARY, personalDetail.getAnniversary(), df, contact, u);
        addContactAttr(ASSISTANT_NAME, businessDetail.getAssistant(), df, contact, u);
        addContactAttr(BIRTHDAY, personalDetail.getBirthday(), df, contact, u);
        addContactAttr(CHILDREN, personalDetail.getChildren(), df, contact, u);
        // addContactAttr("customerId" , c.getBusinessDetail().getDepartment(),
        // df, contact, u);
        addContactAttr(DEPARTMENT, businessDetail.getDepartment(), df, contact, u);
        addContactAttr(GENDER, personalDetail.getGender(), df, contact, u);
        addContactAttr(INITIALS, c.getName().getInitials(), df, contact, u);
        addContactAttr(OFFICE, businessDetail.getOfficeLocation(), df, contact, u);
        addContactAttr(MANAGER_NAME, businessDetail.getManager(), df, contact, u);
        addContactAttr(NICKNAME, c.getName().getNickname(), df, contact, u);
        addContactAttr(PROFESSION, businessDetail.getRole(), df, contact, u);
        addContactAttr(SPOUSE, personalDetail.getSpouse(), df, contact, u);

        if (isEmpty(c.getName().getFirstName()) && isEmpty(c.getName().getLastName())
                && isEmpty(c.getName().getMiddleName()) && !isEmpty(businessDetail.getCompany())) {
            // 3 = Company
            addContactAttr(FILE_AS, "3", df, contact, u);
        }

        return contact;
    }

    public static Contact asContact(Element contact) {
        Properties prop = toProperties(contact);
        Contact c = createContactFromProperties(prop);
        // c.setUid(contact.attributeValue(HeaderConstants.A_ID));
        return c;

    }

    public static Properties toProperties(Element contact) {
        Properties prop = new Properties();
        List<Element> attributes = contact.elements("a");
        for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
            Element attr = (Element) iterator.next();
            prop.put(XMLUtil.xmlEncodeString(attr.attribute("n").getValue()), attr.getText());
        }
        return prop;
    }

    public static Contact createContactFromProperties(Properties prop) {
        Contact c = new Contact();
        BusinessDetail businessDetail = c.getBusinessDetail();
        PersonalDetail personalDetail = c.getPersonalDetail();

        // createPhone(businessDetail, prop.getProperty("assistantPhone"),
        // Def.ASSISTANT_PHONE_NUM);
        // createPhone(businessDetail, prop.getProperty("callbackPhone"),
        // Def.ASSISTANT_PHONE_NUM);
        createPhone(businessDetail, prop.getProperty(CAR_PHONE), Def.PERSONAL_CAR_PHONE_NUM);
        businessDetail.setCompany(getProperty(prop.getProperty(COMPANY)));
        createPhone(businessDetail, prop.getProperty(COMPANY_PHONE), Def.COMPANY_MAIN_PHONE_NUM);
        createEMail(personalDetail, prop.getProperty(EMAIL), Def.PERSONAL_EMAIL_1_ADDRESS);
        createEMail(personalDetail, prop.getProperty(EMAIL2), Def.PERSONAL_EMAIL_2_ADDRESS);
        createEMail(businessDetail, prop.getProperty(EMAIL3), Def.EMAIL_3_ADDRESS);
        c.getName().setFirstName(getProperty(prop.getProperty(FIRST_NAME)));
        personalDetail.getAddress().setCity(getProperty(prop.getProperty(HOME_CITY)));
        personalDetail.getAddress().setCountry(getProperty(prop.getProperty(HOME_COUNTRY)));
        createPhone(personalDetail, prop.getProperty(HOME_FAX), Def.PERSONAL_HOME_FAX_NUMBER);
        createPhone(personalDetail, prop.getProperty(HOME_PHONE), Def.PERSONAL_HOME_PHONE_NUM);
        createPhone(personalDetail, prop.getProperty(HOME_PHONE2), Def.PERSONAL_HOME_2_PHONE_NUM);
        personalDetail.getAddress().setPostalCode(getProperty(prop.getProperty(HOME_POSTAL_CODE)));
        personalDetail.getAddress().setState(getProperty(prop.getProperty(HOME_STATE)));
        personalDetail.getAddress().setStreet(getProperty(prop.getProperty(HOME_STREET)));
        createWebPage(personalDetail, prop.getProperty(HOME_URL), Def.PERSONAL_WEBPAGE);
        createWebPage(personalDetail, prop.getProperty(HOME_URL), Def.PERSONAL_WEBPAGE_RESERVE);
        String jobTitle = prop.getProperty(JOB_TITLE);
        if (jobTitle != null && !jobTitle.trim().equals("")) {
            Title title = new Title();
            title.setPropertyValue(jobTitle);
            title.setTitleType(Def.BUSINESS_TITLE);
            businessDetail.addTitle(title);
        }
        c.getName().setLastName(getProperty(prop.getProperty(LAST_NAME)));
        c.getName().setMiddleName(getProperty(prop.getProperty(MIDDLE_NAME)));
        createPhone(personalDetail, prop.getProperty(MOBILE_PHONE), Def.PERSONAL_MOBILE_PHONE_NUM);
        c.getName().setSalutation(getProperty(prop.getProperty(NAME_PREFIX)));
        c.getName().setSuffix(getProperty(prop.getProperty(NAME_SUFFIX)));
        String notes = prop.getProperty(NOTES);
        if (notes != null && !notes.trim().equals("")) {
            Note note = new Note();
	    note.setNoteType("Body");
            note.setPropertyValue(notes);
            c.addNote(note);
        }
        // createPhone(personalDetail, prop.getProperty("otherCity"),
        // Def.PERSONAL_OTHER_PHONE_NUM);
        // createPhone(personalDetail, prop.getProperty("otherCountry"),
        // Def.PERSONAL_OTHER_PHONE_NUM);
        createPhone(personalDetail, prop.getProperty(OTHER_FAX), Def.PERSONAL_OTHER_FAX_NUMBER);// FIXME
        createPhone(personalDetail, prop.getProperty(OTHER_PHONE), Def.PERSONAL_OTHER_PHONE_NUM);
        // createPhone(personalDetail, prop.getProperty("otherPostalCode"),
        // Def.PERSONAL_OTHER_PHONE_NUM);
        // createPhone(personalDetail, prop.getProperty("otherState"),
        // Def.PERSONAL_OTHER_PHONE_NUM);
        // createPhone(personalDetail, prop.getProperty("otherStreet"),
        // Def.PERSONAL_OTHER_PHONE_NUM);
        // createPhone(personalDetail, prop.getProperty("otherURL"),
        // Def.PERSONAL_OTHER_PHONE_NUM);
        createPhone(businessDetail, prop.getProperty(PAGER), Def.PAGER_NUMBER);
        businessDetail.getAddress().setCity(getProperty(prop.getProperty(WORK_CITY)));
        businessDetail.getAddress().setCountry(getProperty(prop.getProperty(WORK_COUNTRY)));
        createPhone(businessDetail, prop.getProperty(WORK_FAX), Def.BUSINESS_FAX_NUM);
        createPhone(businessDetail, prop.getProperty(WORK_PHONE), Def.BUSINESS_PHONE_NUM);
        createPhone(businessDetail, prop.getProperty(WORK_PHONE2), Def.BUSINESS_2_PHONE_NUM);
        businessDetail.getAddress().setPostalCode(getProperty(prop.getProperty(WORK_POSTAL_CODE)));
        businessDetail.getAddress().setState(getProperty(prop.getProperty(WORK_STATE)));
        businessDetail.getAddress().setStreet(getProperty(prop.getProperty(WORK_STREET)));
        createWebPage(businessDetail, prop.getProperty(WORK_URL), Def.BUSINESS_WEBPAGE);

        personalDetail.setAnniversary(prop.getProperty(ANNIVERSARY));
        businessDetail.setAssistant(prop.getProperty(ASSISTANT_NAME));
        personalDetail.setBirthday(prop.getProperty(BIRTHDAY));
        personalDetail.setChildren(prop.getProperty(CHILDREN));
        businessDetail.setDepartment(getProperty(prop.getProperty(DEPARTMENT)));
        // businessDetail.setDepartment(getProperty(prop.getProperty("customerId")));
        personalDetail.setGender(prop.getProperty(GENDER));
        c.getName().setInitials(getProperty(prop.getProperty(INITIALS)));
        businessDetail.setOfficeLocation(prop.getProperty(OFFICE));
        businessDetail.setManager(prop.getProperty(MANAGER_NAME));
        c.getName().setNickname(getProperty(prop.getProperty(NICKNAME)));
        businessDetail.setRole(getProperty(prop.getProperty(PROFESSION)));
        personalDetail.setSpouse(prop.getProperty(SPOUSE));
        return c;
    }

    private static void createEMail(ContactDetail contactDetail, String property, String type) {
        if (StringUtils.isBlank(property)) {
            return;
        }
        Email email = new Email();
        email.setEmailType(type);
        email.setPropertyValue(property);
        contactDetail.addEmail(email);
    }

    private static void createPhone(ContactDetail contactDetail, String property, String type) {
        if (StringUtils.isBlank(property)) {
            return;
        }
        Phone phone = new Phone();
        phone.setPhoneType(type);
        phone.setPropertyValue(property);
        contactDetail.addPhone(phone);
    }

    private static void createWebPage(ContactDetail contactDetail, String property, String type) {
        if (StringUtils.isBlank(property)) {
            return;
        }
        WebPage webPage = new WebPage();
        webPage.setWebPageType(type);
        webPage.setPropertyValue(property);
        contactDetail.addWebPage(webPage);
    }

    private static Property getProperty(String propValue) {
        Property prop = new Property();
        if (StringUtils.isBlank(propValue.toString()))
            return prop;// BUGFIX - If one of properties doesn't exist then
        // exception occur
        prop.setPropertyValue(propValue);
        return prop;
    }

    /**
     * Search Property from list by type
     *
     * @param list
     *            List of Properties
     * @param type
     *            type
     * @return Property from list
     */
    private static Property find(List list, String type) {
        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
            Property prop = (Property) iterator.next();

            if (prop instanceof Phone && ((Phone) prop).getPhoneType().equalsIgnoreCase(type)) {
                return prop;
            }
            if (prop instanceof Email && ((Email) prop).getEmailType().equalsIgnoreCase(type)) {
                return prop;
            }
            if (prop instanceof Title && ((Title) prop).getTitleType().equalsIgnoreCase(type)) {
                return prop;
            }
            if (prop instanceof WebPage && ((WebPage) prop).getWebPageType().equalsIgnoreCase(type)) {
                return prop;
            }
        }
        return null;
    }

    public static String getEmail(Contact c) {
        List emails = c.getPersonalDetail().getEmails();
        Property prop = find(emails, Def.PERSONAL_EMAIL_1_ADDRESS);
        return isEmpty(prop) ? null : prop.getPropertyValueAsString();
    }

    public static String getFirstName(Contact c) {
        Property prop = c.getName().getFirstName();
        return isEmpty(prop) ? null : prop.getPropertyValueAsString();
    }

    public static String getLastName(Contact c) {
        Property prop = c.getName().getLastName();
        return isEmpty(prop) ? null : prop.getPropertyValueAsString();
    }

    public static String getCompany(Contact c) {
        Property prop = c.getBusinessDetail().getCompany();
        return isEmpty(prop) ? null : prop.getPropertyValueAsString();
    }

    public static String getPhone(Contact c) {
    	List phones = c.getPersonalDetail().getPhones();
        Property prop = find(phones, Def.PERSONAL_MOBILE_PHONE_NUM);

        if (isEmpty(prop))
        	prop = find(phones, Def.BUSINESS_PHONE_NUM);
        if (isEmpty(prop))
        	prop = find(phones, Def.PERSONAL_MOBILE_PHONE_NUM);
        if (isEmpty(prop))
        	prop = find(phones, Def.PERSONAL_HOME_PHONE_NUM);
        if (isEmpty(prop))
        	prop = find(phones, Def.PERSONAL_OTHER_PHONE_NUM);
        return isEmpty(prop) ? null : prop.getPropertyValueAsString();
    }


    private static boolean isEmpty(Property prop) {
        return prop == null || prop.getPropertyValue() == null
                || StringUtils.isBlank(prop.getPropertyValueAsString());
    }

    private static void addContactAttr(String name,
                                       Property prop,
                                       DocumentFactory documentFactory,
                                       Element contact,
                                       boolean u) {
        if (prop == null && !u) {
            return;
        }
        String value = prop == null ? null : prop.getPropertyValueAsString();
        addContactAttr(name, value, documentFactory, contact, u);
    }

    private static void addContactAttr(String name,
                                       String value,
                                       DocumentFactory documentFactory,
                                       Element contact,
                                       boolean u) {
        if (StringUtils.isBlank(value) && !u) {
            return;
        }
        Element attr = documentFactory.createElement("a", contact.getNamespaceURI());
        {
            attr.add(documentFactory.createAttribute(attr, "n", name));
        }
        if (value != null)
            attr.setText(value);
        contact.add(attr);
    }

    private static class Def {
        // foundation field
        // BUSINESS
        public static final String BUSINESS_WEBPAGE = "BusinessWebPage";
        public static final String BUSINESS_2_WEBPAGE = "Business2WebPage";
        public static final String BUSINESS_3_WEBPAGE = "Business3WebPage";
        public static final String EMAIL_3_ADDRESS = "Email3Address";
        public static final String BUSINESS_EMAIL_2_ADDRESS = "BusinessEmail2Address";
        public static final String BUSINESS_EMAIL_3_ADDRESS = "BusinessEmail3Address";
        public static final String BUSINESS_PHONE_NUM = "BusinessTelephoneNumber";
        public static final String BUSINESS_2_PHONE_NUM = "Business2TelephoneNumber";
        public static final String BUSINESS_FAX_NUM = "BusinessFaxNumber";
        public static final String BUSINESS_TITLE = "JobTitle";
        public static final String PAGER_NUMBER = "PagerNumber";
        public static final String COMPANY_MAIN_PHONE_NUM = "CompanyMainTelephoneNumber";
        // PERSONAL
        public static final String PERSONAL_WEBPAGE = "HomeWebPage";
        public static final String PERSONAL_WEBPAGE_RESERVE = "WebPage";
        public static final String PERSONAL_EMAIL_1_ADDRESS = "Email1Address";
        public static final String PERSONAL_EMAIL_2_ADDRESS = "Email2Address";
        public static final String PERSONAL_MOBILE_PHONE_NUM = "MobileTelephoneNumber";
        public static final String PERSONAL_CAR_PHONE_NUM = "CarTelephoneNumber";
        public static final String PERSONAL_HOME_PHONE_NUM = "HomeTelephoneNumber";
        public static final String PERSONAL_HOME_2_PHONE_NUM = "Home2TelephoneNumber";
        public static final String PERSONAL_HOME_FAX_NUMBER = "HomeFaxNumber";
        public static final String PERSONAL_PRIMIRY_PHONE_NUM = "PrimaryTelephoneNumber";
        public static final String PERSONAL_OTHER_PHONE_NUM = "OtherTelephoneNumber";
        public static final String PERSONAL_OTHER_FAX_NUMBER = "OtherFaxNumber";
        // HomeEmail2Address

        // OTHER
        public static final String OTHER_WEBPAGE = "WebPage";
    }

    public static byte[] convertTo(String type,
                                   String version,
                                   Contact c,
                                   TimeZone timezone,
                                   String charset) throws ConverterException {
        byte[] content = null;
        if (PhoneDependedConverter.SIFC_TYPE.equals(type)) {
            ContactToSIFC conv = new ContactToSIFC(timezone, charset);
            content = conv.convert(c).getBytes();
        } else if (PhoneDependedConverter.VCARD_TYPE.equals(type)) {
            ContactToVcard conv = new ContactToVcard(timezone, charset);
            content = conv.convert(c).getBytes();
            if ("2.1".equals(version)) {
                content = PhoneDependedConverter.getInstance().vCardV3toV21(content, charset);
            }
        } else {
        	//TODO: add default type to the configuration of the syncsource
        	logger.error("Unsupported type: " + type + " there is no default contact sync protocol defined");
            throw new ConverterException("Unsupported type: " + type + " there is no default contact sync protocol defined");
        }
        return content;
    }

    public static Contact convertFrom(String type,
                                      String version,
                                      byte[] content,
                                      TimeZone timezone,
                                      String charset) throws ConverterException {
        Contact c = null;
        if(type == null) type = guessType(new String(content));
        if (PhoneDependedConverter.SIFC_TYPE.equals(type)) {
            SIFCParser sifcParser;
            try {
                sifcParser = new SIFCParser(new ByteArrayInputStream(content));
                c = sifcParser.parse();
            } catch (Throwable e) {
                throw new ConverterException(e);
            }
        } else if (PhoneDependedConverter.VCARD_TYPE.equals(type)) {
            VcardParser vcardParser = new VcardParser(new ByteArrayInputStream(content), null,
                    charset);
            try {
                c = vcardParser.vCard();
            } catch (ParseException e) {
                throw new ConverterException(e);
            }
        } else {
        	//TODO: add default type to the configuration of the syncsource
        	logger.error("Unsupported type: " + type + " there is no default contact sync protocol defined");
            throw new ConverterException("Unsupported type: " + type + " there is no default contact sync protocol defined");
        }
        return c;
    }

	/**
	 * Essaye de deviner le type d'un element en fonction de son contenu
	 *
	 * @param content Le contenu de l'élément
	 * @return Le type déterminé
	 */
	protected static String guessType(String content) {
		FunambolLogger log = FunambolLoggerFactory.getLogger("funambol.zimbra.internal.ContactUtils");
		if (log.isTraceEnabled()) log.trace("Guessing type based on content");
		String type=null;
		content = content.replaceAll("(?s)(\\A(\\n|\\r)*)|((\\n|\\r)*\\Z)", "");//Trims \r and \n at the beginning and the end of the string
		if (log.isDebugEnabled()) log.debug("Content : <<"+content+">>");
		if(content.matches("(?s)\\ABEGIN:VCARD(.*)END:VCARD\\Z")) {
			type = PhoneDependedConverter.VCARD_TYPE;
		}
		else if(content.matches("(?s)\\A<\\?xml(.*)<contact>(.*)<SIFVersion>(.*)</SIFVersion>(.*)</contact>\\Z")) {
			type = PhoneDependedConverter.SIFC_TYPE;
		}
		if (log.isTraceEnabled()) log.trace("Guessed type : "+type);
		return type;
	}

}
