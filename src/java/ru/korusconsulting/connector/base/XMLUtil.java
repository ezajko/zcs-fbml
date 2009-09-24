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

public class XMLUtil {
    /**
     * Encode a string appropriately for XML. Lifted from ApacheSOAP 2.2
     * (org.apache.soap.Utils)
     * 
     * @param orig
     *            the String to encode
     * @return a String in which XML special chars are repalced by entities
     */
    public static String xmlEncodeString(String orig) {
        if (orig == null) {
            return "";
        }

        char[] chars = orig.toCharArray();

        // if the string doesn't have any of the magic characters, leave
        // it alone.
        boolean needsEncoding = false;

        search: for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case '&':
                case '"':
                case '\'':
                case '<':
                case '>':
                    needsEncoding = true;
                    break search;
            }
        }

        if (!needsEncoding)
            return orig;

        StringBuffer strBuf = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case '&':
                    strBuf.append("&amp;");
                    break;
                case '\"':
                    strBuf.append("&quot;");
                    break;
                case '\'':
                    strBuf.append("&apos;");
                    break;
                case '<':
                    strBuf.append("&lt;");
                    break;
                case '\r':
                    strBuf.append("&#xd;");
                    break;
                case '>':
                    strBuf.append("&gt;");
                    break;
                default:
                    if (((int) chars[i]) > 127) {
                        strBuf.append("&#");
                        strBuf.append((int) chars[i]);
                        strBuf.append(";");
                    } else {
                        strBuf.append(chars[i]);
                    }
            }
        }

        return strBuf.toString();
    }
}
