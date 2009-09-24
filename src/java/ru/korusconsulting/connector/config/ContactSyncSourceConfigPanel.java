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
package ru.korusconsulting.connector.config;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang.StringUtils;

import ru.korusconsulting.connector.funambol.CalendarSyncSource;
import ru.korusconsulting.connector.funambol.ZimbraSyncSource;


import com.funambol.admin.AdminException;
import com.funambol.admin.ui.SourceManagementPanel;
import com.funambol.framework.engine.source.ContentType;
import com.funambol.framework.engine.source.SyncSourceInfo;

public class ContactSyncSourceConfigPanel  extends SourceManagementPanel implements Serializable {
    public static final String NAME_ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-_.";
    private static final String DEFAULT_ZIMBRA_URL= "https://<hostname>/service/soap/";

    private JLabel panelName = new JLabel();
    private TitledBorder titledBorder1;
    private JLabel nameLabel = new JLabel();
    private JTextField nameValue = new JTextField();
    private JLabel sourceURILabel = new JLabel();
    private JTextField sourceURIValue = new JTextField();
    private JLabel zimbraURLLabel = new JLabel();
    private JTextField zimbraURLValue = new JTextField();
    private JButton button = new JButton();
    private ZimbraSyncSource syncSource;
    private JCheckBox taskCheckBox= new JCheckBox();

    /**
     * 
     */
    public ContactSyncSourceConfigPanel() {
        init();
    }

    /**
     * 
     */
    private void init() {
        this.setLayout(null);

        titledBorder1 = new TitledBorder("");

        panelName.setFont(titlePanelFont);
        panelName.setText("Edit Zimbra SyncSource");
        panelName.setBounds(new Rectangle(14, 5, 316, 28));
        panelName.setAlignmentX(SwingConstants.CENTER);
        panelName.setBorder(titledBorder1);

        int y = 60;
        int dy = 30;
        sourceURILabel.setText("Source URI: ");
        sourceURILabel.setFont(defaultFont);
        sourceURILabel.setBounds(new Rectangle(14, y, 150, 18));
        sourceURIValue.setFont(defaultFont);
        sourceURIValue.setBounds(new Rectangle(170, y, 350, 18));

        y += dy;
        nameLabel.setText("Name: ");
        nameLabel.setFont(defaultFont);
        nameLabel.setBounds(new Rectangle(14, y, 150, 18));
        nameValue.setFont(defaultFont);
        nameValue.setBounds(new Rectangle(170, y, 350, 18));

        y += dy;
        zimbraURLLabel.setText("Zimbra URL: ");
        zimbraURLLabel.setFont(defaultFont);
        zimbraURLLabel.setBounds(new Rectangle(14, y, 150, 18));
        zimbraURLValue.setFont(defaultFont);
        zimbraURLValue.setBounds(new Rectangle(170, y, 350, 18));
        
//        if(syncSource instanceof CalendarSyncSource){
        y += dy;
        taskCheckBox.setText("is Task? ");
        taskCheckBox.setFont(defaultFont);
        taskCheckBox.setBounds(new Rectangle(170, y, 150, 18));
        taskCheckBox.setEnabled(false);
//        }
        
        y += dy;
        button.setFont(defaultFont);
        button.setText("Add");
        button.setBounds(170, y, 70, 25);

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    validateValues();
                    getValues();
                    if (getState() == STATE_INSERT) {
                        ContactSyncSourceConfigPanel.this
                                .actionPerformed(new ActionEvent(
                                        ContactSyncSourceConfigPanel.this,
                                        ACTION_EVENT_INSERT, event
                                                .getActionCommand()));
                    } else {
                        ContactSyncSourceConfigPanel.this
                                .actionPerformed(new ActionEvent(
                                        ContactSyncSourceConfigPanel.this,
                                        ACTION_EVENT_UPDATE, event
                                                .getActionCommand()));
                    }
                } catch (Throwable e) {
                    notifyError(new AdminException(e.getMessage()));
                }
            }
        });

        this.add(panelName, null);
        this.add(nameLabel, null); this.add(nameValue, null);
        this.add(sourceURILabel, null); this.add(sourceURIValue, null);
        this.add(zimbraURLLabel); this.add(zimbraURLValue);
        this.add(taskCheckBox); 
        this.add(button, null);
    }

    /**
     * 
     */
    public void updateForm() {
//        if (!(getSyncSource() instanceof ContactSyncSource)) {
//            notifyError(new AdminException("This is not ZimbraSyncSource! Unable to process SyncSource values."));
//            return;
//        }
        
        if (getState() == STATE_INSERT) {
            button.setText("Add");
        } else if (getState() == STATE_UPDATE) {
            button.setText("Save");
        }

        this.syncSource = (ZimbraSyncSource)getSyncSource();

        sourceURIValue.setText(syncSource.getSourceURI());
        nameValue.setText(syncSource.getName());
        
//        if (syncSource != null && syncSource.getInfo() != null && syncSource.getInfo().getPreferredType() != null) {
//            typeValue.setSelectedItem(syncSource.getInfo().getPreferredType().getType());
//        }

//        if (this.syncSource.getSourceURI() != null) {
//            sourceURIValue.setEditable(false);
//        }
        
        zimbraURLValue.setText(syncSource.getZimbraUrl()==null?DEFAULT_ZIMBRA_URL:syncSource.getZimbraUrl().toString());
        
        if(syncSource instanceof CalendarSyncSource){
            taskCheckBox.setEnabled(true);
            taskCheckBox.setSelected(((CalendarSyncSource)syncSource).isTask());
        }
    }

    /**
     * Validate all values
     * @throws IllegalArgumentException
     */
    private void validateValues() throws IllegalArgumentException {
        String value = null;

        value = nameValue.getText();
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("Field 'Name' cannot be empty. Please provide a SyncSource name.");
        }

        if (!StringUtils.containsOnly(value, NAME_ALLOWED_CHARS.toCharArray())) {
            throw new IllegalArgumentException("Only the following characters are allowed for field 'Name':\n" + NAME_ALLOWED_CHARS);
        }

        value = sourceURIValue.getText();
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("Field 'Source URI' cannot be empty. Please provide a SyncSource URI.");
        }
        
        value = zimbraURLValue.getText();
        try {
            new URL(value);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid Zimbra URL("+value+"), message:"+ e.getLocalizedMessage());
        }
        
    }

    /**
     * Fill ZimbraSynSource value s
     */
    private void getValues() {
        ContentType[] contentTypes;
        syncSource.setSourceURI(sourceURIValue.getText().trim());
        
        syncSource.setName(nameValue.getText().trim());
        if(syncSource instanceof CalendarSyncSource){
            contentTypes=new ContentType[] { new ContentType("text/x-vcalendar", "1.0"),
                    new ContentType("text/x-vcalendar", "2.0"), 
                    new ContentType("text/calendar","1.0") };
        }
        else{
            contentTypes=new ContentType[] { new ContentType("text/x-vcard", "2.1"),
                new ContentType("text/vcard", "3.0"), 
                new ContentType("text/x-s4j-sifc","1.0") };
        }

//        JOptionPane.showMessageDialog(new JFrame(), "zimbraURLValue:"+zimbraURLValue.getText());
        syncSource.setZimbraUrl(zimbraURLValue.getText());
        syncSource.setInfo(new SyncSourceInfo(contentTypes, 0));
        
        if(syncSource instanceof CalendarSyncSource){
            ((CalendarSyncSource)syncSource).setTask(taskCheckBox.isSelected());
        }
    }

}
