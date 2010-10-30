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
package ru.korusconsulting.connector.base;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.funambol.framework.logging.FunambolLogger;
import com.funambol.framework.logging.FunambolLoggerFactory;

public class ConnectionUtils {
    public static void writeToStream(byte[] data, OutputStream urlOutputStream)
            throws IOException {
        BufferedOutputStream os = null;
        try {
            os = new BufferedOutputStream(urlOutputStream);
            os.write(data);
            os.flush();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (urlOutputStream != null) {
                    urlOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static byte[] download(String address, ConnectionContext ccontext)
            throws IOException {
        ByteArrayOutputStream out = null;
        HttpURLConnection conn = null;
        InputStream in = null;
        String token = ccontext.getAuthToken();
        try {
            URL url = new URL(address);
            out = new ByteArrayOutputStream();
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Cookie", "ZM_AUTH_TOKEN=" + token);
            in = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int numRead;
            while ((numRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, numRead);
            }
            return out.toByteArray();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
            	FunambolLogger logger = FunambolLoggerFactory.getLogger("funambol.zimbra.manager");
            	logger.error("Downloading from Zimbra went wrong", ioe);
            }
        }
    }

    public static String upload(String fileName, byte[] fileContent,
            ConnectionContext ccontext, String servletUploadURL) throws IOException {
        String token = ccontext.getAuthToken();

        URL url = new URL(servletUploadURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="
                + boundary);
        conn.setRequestProperty("Cookie", "ZM_AUTH_TOKEN=" + token);
        DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

        dos.writeBytes(twoHyphens + boundary + lineEnd);
        dos.writeBytes("Content-Disposition: form-data; name=\"upload\";"
                + " filename=\"" + fileName + "\"" + lineEnd);
        dos.writeBytes(lineEnd);
        dos.write(fileContent);
        dos.writeBytes(lineEnd);
        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

        dos.flush();
        dos.close();

        byte buffer[] = new byte[1024];
        int readed;
        InputStream is = conn.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((readed = is.read(buffer, 0, buffer.length)) != -1) {
            baos.write(buffer, 0, readed);
        }

        String result = new String(baos.toByteArray());
        //200,'client_token','server_token'
        String params[] = result.split(",");
        if (params.length != 3) {
            throw new IOException("Unexpected result of upload:" + result);
        }
        String aid = params[2].substring(1, params[2].lastIndexOf('\''));
        return aid;

    }

/*    @Deprecated
    public static String uploadViaHTTPClient(String fileName, byte[] fileContent,
            ConnectionContext ccontext, String servletUploadURL) throws HttpException,
            IOException {
        String token = ccontext.getAuthToken();
        PostMethod filePost = new PostMethod(servletUploadURL);
        try {
            Part[] parts = { new FilePart(fileName, new ByteArrayPartSource(fileName,
                                                                            fileContent)) };
            filePost.setRequestEntity(new MultipartRequestEntity(parts,
                                                                 filePost.getParams()));
            filePost.setRequestHeader("Cookie", "ZM_AUTH_TOKEN=" + token);
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(8000);
            client.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
            int status = client.executeMethod(filePost);
            if (status == HttpStatus.SC_OK) {
                String result = filePost.getResponseBodyAsString();
                //200,'client_token','server_token'
                String params[] = result.split(",");
                if (params.length != 3) {
                    throw new HttpException("Unexpected result of upload:" + result);
                }
                String aid = params[2].substring(1, params[2].lastIndexOf('\''));
                return aid;
            } else {
                throw new HttpException("Upload Error file Error:"
                        + filePost.getStatusText());
            }
        } finally {
            filePost.releaseConnection();
        }
    }*/

    private final static String lineEnd = "\r\n";
    private final static String twoHyphens = "--";
    private final static String boundary = "*****";
}
