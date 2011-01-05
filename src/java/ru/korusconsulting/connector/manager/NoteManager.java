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
package ru.korusconsulting.connector.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import ru.korusconsulting.connector.base.ZConst;
import ru.korusconsulting.connector.exceptions.SoapRequestException;

import com.funambol.common.pim.note.Note;
import com.funambol.common.pim.utility.PDIMergeUtils;
import com.funambol.framework.engine.SyncItemKey;
import com.funambol.framework.tools.merge.MergeResult;

public class NoteManager extends Manager<Note> {
    public static final String A_NOTE_ID = "id";

    public void removeAllItems() throws IOException, SoapRequestException{
        zimbraPort.requestRemoveAllItems(itemsResponse, A_NOTE_ID);
        itemsResponse.elements().retainAll(new ArrayList<Element>());//remove all
    }
    
    public void requestItemsForSync() throws IOException, SoapRequestException {
//        itemsResponse = zimbraPort.requestAllCalendars();
    }

    public SyncItemKey[] getTwins(Note contact) {
        ArrayList<String> twins = new ArrayList<String>();
//        String subject = CalendarUtils.getSubject(calendar);
//
//        for (Iterator iterator = itemsResponse.elementIterator(); iterator.hasNext();) {
//            Element item = (Element) iterator.next();
//            String id = item.attributeValue(A_CALENDAR_ID);
//            
//            boolean twin = true;
//            twin=name.equalsIgnoreCase(item.attributeValue("name"));
//            if (twin) {
//                twins.add(id);
//            }
//        }
        return listAsKeyArray(twins);
    }

    @Override
    public String addItem(Note note) throws IOException, SoapRequestException {
        Element createdNote = zimbraPort.requestCreateNote(note);
        return createdNote.attributeValue(A_NOTE_ID);
    }

    @Override
    public Note getItem(String key, Object... args) throws IOException,
            SoapRequestException {
        if (!isExist(key))
            return null;
        Note note = zimbraPort.requestNoteById(key);
        return note;
    }

    @Override
    public boolean mergeItems(String key, Note clientNote) throws IOException,
            SoapRequestException {
        Note serverContact = zimbraPort.requestNoteById(key);
        MergeResult mergeResult = merge(clientNote,serverContact);
        if (mergeResult.isSetBRequired()) {
            updItem(key, serverContact);
        }
        return mergeResult.isSetARequired();
    }

    @Override
    public String updItem(String key, Note calendar) throws IOException,
            SoapRequestException {
        String oldFolderId=null;
        List<Element> items=itemsResponse.elements();
        for (int i=0;i<items.size(); i++) {
            Element cn = (Element) items.get(i);
            if(cn.attributeValue(A_NOTE_ID).equals(key)){
                oldFolderId=cn.attributeValue(ZConst.A_FOLDER);
            }
        }
        if (oldFolderId==null) {
            // can't find contact with key 
            // we have to create Contact
            key = addItem(calendar);
        } else {
            zimbraPort.requestModifyNote(key, calendar, oldFolderId);
        }
        return key;
    }

    public String extractKeyFromElement(Element element) {
        String id = element.attributeValue(A_NOTE_ID);
        return id;
    }

    
    private MergeResult merge(Note noteA, Note noteB) {

        MergeResult eventMergeResult = new MergeResult("Note");

        if (noteB == null) {
            throw new IllegalStateException("The given event must be not null");
        }

        //
        // MergeResult used for each fields
        //
        MergeResult result = null;

        // date
        result = PDIMergeUtils.compareProperties(noteA.getDate(), noteB.getDate());
        if (result.isSetARequired()) {
            noteA.setDate(noteB.getDate());
        } else if (result.isSetBRequired()) {
            noteB.setDate(noteA.getDate());
        }
        eventMergeResult.addMergeResult(result, "Date");

        // subject
        result = PDIMergeUtils.compareProperties(noteA.getSubject(), noteB.getSubject());
        if (result.isSetARequired()) {
            noteA.setSubject(noteB.getSubject());
        } else if (result.isSetBRequired()) {
            noteB.setSubject(noteA.getSubject());
        }
        eventMergeResult.addMergeResult(result, "Subject");
        
        // textDescription
        result = PDIMergeUtils.compareProperties(noteA.getTextDescription(), noteB.getTextDescription());
        if (result.isSetARequired()) {
            noteA.setTextDescription(noteB.getTextDescription());
        } else if (result.isSetBRequired()) {
            noteB.setTextDescription(noteA.getTextDescription());
        }
        eventMergeResult.addMergeResult(result, "TextDescription");
        
        return eventMergeResult;
    }
}
