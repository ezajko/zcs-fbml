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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.funambol.framework.logging.FunambolLogger;
import com.funambol.framework.logging.FunambolLoggerFactory;


public class FunambolConnector {
    public final static String NAME="ZimbraFunambol Connector";
    private String version;
    private FunambolLogger logger;
    private static FunambolConnector instance;
    private FunambolConnector(){
        logger = FunambolLoggerFactory.getLogger("funambol.zimbra");
        InputStream is = FunambolConnector.class.getResourceAsStream("/zimbraconnector.properties");
        Properties prop=new Properties();
        try {
            prop.load(is);
            version=prop.getProperty("connector.version");
        } catch (IOException e) {
            logger.error(e);
        }
        if(version==null)version="unversioned";
    };
        
    public static FunambolConnector getInstance(){
        if(instance==null){
            instance=new FunambolConnector();
        }
        return instance;
    }

    public String getVersion() {
        return version;
    }
   
}
