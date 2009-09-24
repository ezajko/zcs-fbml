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
package ru.korusconsulting.connector.base;

import java.util.HashMap;
import java.util.List;

import org.dom4j.Element;

public class FolderHolder {
    private static final String DEFAULT_FOLDER_CONTACTS = "Contacts";
    private static final String DEFAULT_FOLDER_CAL = "Calendar";
    private static final String DEFAULT_FOLDER_TASKS = "Tasks";
    private static final String DEFAULT_FOLDER_NOTES = "Notebook";
    static final String DEFAULT_FOLDER = "DEFAULT_FOLDER";
    private static final String A_VIEW = "view";
    private static final String A_NAME = "name";
    private static final String A_ID = "id";
    private static final String TRASH_FOLDER_NAME = "Trash";
    public static final String NOTE = "wiki";
    public static final String TASK = "task";
    public static final String APPOINTMENT = "appointment";
    public static final String CONTACT = "contact";
    private Element rootFolder;
    private String trashFolderId;
    private HashMap<String, Element> contactFolders = new HashMap<String, Element>();//id->fodler
    private HashMap<String, Element> contactFolders_ = new HashMap<String, Element>();//name->folder
    private HashMap<String, Element> calendarFolders = new HashMap<String, Element>();//id->fodler
    private HashMap<String, Element> calendarFolders_ = new HashMap<String, Element>();//name->folder
    private HashMap<String, Element> taskFolders = new HashMap<String, Element>();//id->fodler
    private HashMap<String, Element> taskFolders_ = new HashMap<String, Element>();//name->folder
    private HashMap<String, Element> noteFolders = new HashMap<String, Element>();//id->fodler
    private HashMap<String, Element> noteFolders_ = new HashMap<String, Element>();//name->folder
    private Element contactDefaultFolder = null;
    private Element calendarDefaultFolder = null;
    private Element taskDefaultFolder = null;
    private Element noteDefaultFolder = null;

    public FolderHolder() {
    }

    public Element getRootFolder() {
        return rootFolder;
    }

    public void setRootFolder(Element rootFolder) {
        this.rootFolder = rootFolder;
        rootFolder.detach();
        contactDefaultFolder = null;
        calendarDefaultFolder = null;
        taskDefaultFolder = null;
        noteDefaultFolder = null;
        contactFolders.clear();
        contactFolders_.clear();
        calendarFolders.clear();
        calendarFolders_.clear();
        taskFolders.clear();
        taskFolders_.clear();
        noteFolders.clear();
        noteFolders_.clear();
        recurInit(rootFolder);
    }

    @SuppressWarnings("unchecked")
	private void recurInit(Element parent) {
        List<Element> folders = parent.elements();
        if (folders.size() > 0) {
            for (int i = 0; i < folders.size(); i++) {
                recurInit(folders.get(i));
            }
        } 
        notify(parent);
    }

    public String getTrashFolderId() {
        return trashFolderId;
    }

    public String getIdByName(String name, String type) {
        Element node = null;
        if (CONTACT.equals(type)) {
            if (DEFAULT_FOLDER.equals(name)) {
                node = contactDefaultFolder;
            } else
                node = contactFolders_.get(name);
        } else if (APPOINTMENT.equals(type)) {
            if (DEFAULT_FOLDER.equals(name)) {
                node = calendarDefaultFolder;
            } else
                node = calendarFolders_.get(name);
        } else if (TASK.equals(type)) {
            if (DEFAULT_FOLDER.equals(name)) {
                node = taskDefaultFolder;
            } else
                node = taskFolders_.get(name);
        } else if (NOTE.equals(type)) {
            if (DEFAULT_FOLDER.equals(name)) {
                node = noteDefaultFolder;
            } else
                node = noteFolders_.get(name);
        }
        if (node == null) {
            return null;
        }
        return node.attributeValue(A_ID);
    }
    
    public String getDefaultFolderId(String type){
        return getIdByName(DEFAULT_FOLDER, type);
    }
    public String getNameById(String id, String type) {
        Element node = null;
        if (CONTACT.equals(type)) {
            if(contactDefaultFolder.attributeValue(ZConst.A_ID).equals(id)){
                return null;
            }
            node = contactFolders.get(id);
        } else if (APPOINTMENT.equals(type)) {
            if(calendarDefaultFolder.attributeValue(ZConst.A_ID).equals(id)){
                return null;
            }
            node = calendarFolders.get(id);
        } else if (TASK.equals(type)) {
            if(taskDefaultFolder.attributeValue(ZConst.A_ID).equals(id)){
                return null;
            }
            node = taskFolders.get(id);
        } else if (NOTE.equals(type)) {
            if(noteDefaultFolder.attributeValue(ZConst.A_ID).equals(id)){
                return null;
            }
            node = noteFolders.get(id);
        }
        if (node == null) {
            return null;
        }
        return node.attributeValue(A_NAME);
    }

    public void notify(Element folder) {
        String id = folder.attributeValue(A_ID);
        String name = folder.attributeValue(A_NAME);
        String view = folder.attributeValue(A_VIEW);
        if (CONTACT.equals(view)) {
            if (contactDefaultFolder == null || DEFAULT_FOLDER_CONTACTS.equals(name)) {
                contactDefaultFolder = folder;
            }
            contactFolders.put(id, folder);
            contactFolders_.put(name, folder);
        } else if (APPOINTMENT.equals(view)) {
            if (calendarDefaultFolder == null || DEFAULT_FOLDER_CAL.equals(name)) {
                calendarDefaultFolder = folder;
            }
            calendarFolders.put(id, folder);
            calendarFolders_.put(name, folder);
        } else if (TASK.equals(view)) {
            if (taskDefaultFolder == null || DEFAULT_FOLDER_TASKS.equals(name)) {
                taskDefaultFolder = folder;
            }
            taskFolders.put(id, folder);
            taskFolders_.put(name, folder);
        } else if (NOTE.equals(view)) {
            if (noteDefaultFolder == null || DEFAULT_FOLDER_NOTES.equals(name)) {
                noteDefaultFolder = folder;
            }
            noteFolders.put(id, folder);
            noteFolders_.put(name, folder);
        }
        if (TRASH_FOLDER_NAME.equals(name)) {
            trashFolderId = id;
        }
    }
}
