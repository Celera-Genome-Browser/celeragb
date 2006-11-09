/*
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 1999 - 2006 Applera Corporation.
 301 Merritt 7 
 P.O. Box 5435 
 Norwalk, CT 06856-5435 USA

 This is free software; you can redistribute it and/or modify it under the 
 terms of the GNU Lesser General Public License as published by the 
 Free Software Foundation; version 2.1 of the License.

 This software is distributed in the hope that it will be useful, but 
 WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE. 
 See the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License 
 along with this software; if not, write to the Free Software Foundation, Inc.
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
*/
package api.stub.http;

import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;


/**
 * Convenience class for getting HttpConnection with login and proxy login information.<br>
 * If a login is not needed, leave the LoginName as null.<br>
 * If a proxy login is not needed, leave the ProxyLoginName as null.
 */
public final class HttpConnectionFactory
{
    private static final String BASIC_AUTH = "Basic ";

    private String urlSpec;
    private String serverLoginName;
    private String serverLoginPassword;

    private String proxyLoginName;
    private String proxyLoginPassword;

    public HttpURLConnection generateHttpURLConnection() throws MalformedURLException, IOException
    {

		

        final URL url = new URL(urlSpec);
        
//		System.out.println("---------------------------------------------------------------");
//		System.out.println("Using this URL:  " + url.toExternalForm());
//		System.out.println("---------------------------------------------------------------");
		
        final String serverAuthEncoding = this.generateAuthEncodedString(serverLoginName, serverLoginPassword);
        final String proxyAuthEncoding = this.generateAuthEncodedString(proxyLoginName, proxyLoginPassword);

        final HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        //final boolean isOk = HttpURLConnection.HTTP_OK == urlConn.getResponseCode();

        if (serverAuthEncoding != null)
        {
            urlConn.setRequestProperty("Authorization", serverAuthEncoding);
        }
        if (proxyAuthEncoding != null)
        {
            urlConn.setRequestProperty("Proxy-Authorization", proxyAuthEncoding);
        }

        return urlConn;
    }

    private static String generateAuthEncodedString(final String pUser, final String pPassword)
    {
        if (pUser == null)
        {
            return null;
        }
        final StringBuffer sbuf = new StringBuffer(pUser.length() + pPassword.length() + 1);
        sbuf.append(pUser);
        sbuf.append(':');
        sbuf.append(pPassword);
        final String userPassword = sbuf.toString();
        final String authEncoding = BASIC_AUTH + new BASE64Encoder().encode(userPassword.getBytes());
        return authEncoding;
    }


    public void setUrlSpec(final String pValue)
    {
        this.urlSpec = pValue;
    }

    public void setServerLoginName(final String pValue)
    {
        this.serverLoginName = pValue;
    }

    public void setServerLoginPassword(final String pValue)
    {
        this.serverLoginPassword = pValue;
    }

    /**
     * WARNING: this is static, so affects all instances
     */
    public static void setProxyHost(final String pValue)
    {
        System.setProperty("http.proxyHost", pValue);
        System.setProperty("https.proxyHost", pValue);
    }

    /**
     * WARNING: this is static, so affects all instances
     */
    public static void setProxyPort(final String pValue)
    {
        System.setProperty("http.proxyPort", pValue);
        System.setProperty("https.proxyPort", pValue);
    }

    public static void clearProxyLoginInfo() {
        final Properties sysProps = System.getProperties();
        sysProps.remove("http.proxyHost");
        sysProps.remove("https.proxyHost");
        sysProps.remove("http.proxyPort");
        sysProps.remove("https.proxyPort");
    }


    public void setProxyLoginName(final String pValue)
    {
        this.proxyLoginName = pValue;
    }

    public void setProxyLoginPassword(final String pValue)
    {
        this.proxyLoginPassword = pValue;
    }


    public String getUrlSpec()
    {
        return this.urlSpec;
    }

    public String getServerLoginName()
    {
        return this.serverLoginName;
    }

    public String getServerLoginPassword()
    {
        return this.serverLoginPassword;
    }

    public static String getProxyHost()
    {
        return System.getProperty("http.proxyHost");
    }

    public static String getProxyPort()
    {
        return System.getProperty("http.proxyPort");
    }

    public String getProxyLoginName()
    {
        return this.proxyLoginName;
    }

    public String getProxyLoginPassword()
    {
        return this.proxyLoginPassword;
    }

}
