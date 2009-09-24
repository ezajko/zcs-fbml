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

public interface Query {

	String CREATE_TOKEN = "insert into zimbra_chng_token (principalid, lastToken) values(?,?)";
	String UPDATE_TOKEN = "update zimbra_chng_token set lastToken=? where principalid=?";
	String GET_TOKEN = "select lastToken from zimbra_chng_token where principalid=?";
    String GET_CLIENTMAPPING = "select GUID from fnbl_client_mapping where principal=? and SYNC_SOURCE=?";

}
