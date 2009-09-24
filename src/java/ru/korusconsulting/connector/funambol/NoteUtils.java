/*
 * Copyright (C) 2008 KorusConsulting
 * 
 * Author: Roman Bliznets <RBliznets@korusconsulting.ru>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package ru.korusconsulting.connector.funambol;

import java.io.ByteArrayInputStream;
import java.util.TimeZone;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import com.funambol.common.pim.converter.ConverterException;
import com.funambol.common.pim.converter.NoteToSIFN;
//import com.funambol.common.pim.converter.NoteToVnote;
import com.funambol.common.pim.note.Note;
import com.funambol.common.pim.sif.SIFNParser;

public class NoteUtils {

    public static Element asElement(Note n, DocumentFactory documentFactory,
            String namespace, boolean b) {
        Element note = documentFactory.createElement("note", namespace);
        //TODO 
        
        return note;
    }

    public static Note asNote(Element noteEl) {
        // TODO Auto-generated method stub
        return null;
    }

    public static byte[] convertTo(String type, String version, Note n,
            TimeZone timezone, String charset) throws ConverterException {
        byte[] content = null;
        if (PhoneDependedConverter.SIFN_TYPE.equals(type)) {
            NoteToSIFN conv = new NoteToSIFN(timezone, charset);
            content = conv.convert(n).getBytes();
        } else if (PhoneDependedConverter.VNOTE_TYPE.equals(type)) {
            throw new RuntimeException("Don't support at this time");
            //uncomment line below and attache related library
            //NoteToVnote conv = new NoteToVnote(timezone, charset);
            //content = conv.convert(n).getBytes();
        } else {
            throw new ConverterException("Unsupported type:" + type);
        }
        return content;
    }

    public static Note convertFrom(String type, String version, byte[] content,
            TimeZone timezone, String charset) throws ConverterException {
        Note n = null;
        if (PhoneDependedConverter.SIFN_TYPE.equals(type)) {
            SIFNParser sifnParser;
            try {
                sifnParser = new SIFNParser(new ByteArrayInputStream(content));
                n = sifnParser.parse();
            } catch (Throwable e) {
                throw new ConverterException(e);
            }
        }
        //        else if (PhoneDependedConverter.VCARD_TYPE.equals(type)) {
        //            VNoteParser vnoteParser = new VNoteParser(new ByteArrayInputStream(content));
        //            try {
        //                com.funambol.common.pim.vnote.VNoteReader reader;
        //                VNote vnote;
        //                n = vnoteParser.VNote();
        //            } catch (ParseException e) {
        //                throw new ConverterException(e);
        //            }
        //        } 
        else {
            throw new ConverterException("Unsupported type:" + type);
        }
        return n;
    }
    
//    private static void addAttr(String attrName, Property prop, Element comp) {
//        if (prop != null)
//            addAttr(attrName, prop.getPropertyValueAsString(), comp);
//    }
//
//    private static void addAttr(String attrName, String val, Element comp) {
//        if (val != null && !val.trim().equals(""))
//            comp.addAttribute(attrName, val);
//    }
}
