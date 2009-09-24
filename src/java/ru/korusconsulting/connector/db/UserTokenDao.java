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
package ru.korusconsulting.connector.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.sql.DataSource;

import com.funambol.framework.tools.DBTools;
import com.funambol.framework.tools.DataSourceTools;

public class UserTokenDao {
    private DataSource dataSource;
    private String jndiDataSourceName;

    /**
     * 
     * @param _jndiDataSourceName
     *                String
     * @throws Exception
     */
    public UserTokenDao(String _jndiDataSourceName) throws Exception {

        try {

            this.jndiDataSourceName = _jndiDataSourceName;
            dataSource = DataSourceTools.lookupDataSource(this.jndiDataSourceName);
        } catch (Exception e) {
            throw new Exception("Error creating UserTokenDao Object ", e);
        }
    }

    public int createToken(long principalId, long token) throws Throwable {
        Connection connection = null;
        PreparedStatement ps = null;
        int result = 0;

        try {

            connection = dataSource.getConnection();
            ps = connection.prepareStatement(Query.CREATE_TOKEN);

            ps.setLong(1, principalId);
            ps.setLong(2, token);
            result = ps.executeUpdate();

            ps.close();
            ps = null;

        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            DBTools.close(connection, ps, null);
        }

        return result;
    }

    public int updateToken(long principalId, long token) throws Throwable {
        Connection connection = null;
        PreparedStatement ps = null;
        int result = 0;

        try {

            connection = dataSource.getConnection();
            ps = connection.prepareStatement(Query.UPDATE_TOKEN);

            ps.setLong(1, token);
            ps.setLong(2, principalId);
            result = ps.executeUpdate();
            
            ps.close();
            ps = null;

        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            DBTools.close(connection, ps, null);
        }

        return result;
    }

    public long getToken(long principalId) throws Throwable {
        Connection connection = null;
        PreparedStatement ps = null;
        long result = -1;

        try {

            connection = dataSource.getConnection();
            ps = connection.prepareStatement(Query.GET_TOKEN);

            ps.setLong(1, principalId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getLong(1);
            }
            rs.close();
            ps.close();
            ps = null;

        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            DBTools.close(connection, ps, null);
        }
        if(result==-1){
            createToken(principalId, 0);
            result=0;
        }
        return result;
    }
    
    public ArrayList<String> getClientMapping(long principalId, String sourceName) throws Throwable {
        Connection connection = null;
        PreparedStatement ps = null;
        ArrayList<String> result = new ArrayList<String>(75);

        try {

            connection = dataSource.getConnection();
            ps = connection.prepareStatement(Query.GET_CLIENTMAPPING);

            ps.setLong(1, principalId);
            ps.setString(2, sourceName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getString(1));
            }
            rs.close();
            ps.close();
            ps = null;

        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            DBTools.close(connection, ps, null);
        }

        return result;
    }
}
