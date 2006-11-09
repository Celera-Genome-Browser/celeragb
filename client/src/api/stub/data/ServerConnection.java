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
package api.stub.data;

public class ServerConnection implements java.io.Serializable
{
    private String host;

    private int port;

    private String applicationServerName;

    private boolean useSSL;

    public ServerConnection()
    {
    }

    public ServerConnection
    (String host,
     int port,
     String applicationServerName,
     boolean useSSL)
    {
        this.host = host;
        this.port = port;
	this.applicationServerName = applicationServerName;
        this.useSSL = useSSL;
    }

    public ServerConnection
        (String host,
        int port,
	String applicationServerName)
    {
        this.host = host;
        this.port = port;
	this.applicationServerName = applicationServerName;
    }

    public ServerConnection
        (String host,
        int port)
    {
        this.host = host;
        this.port = port;
        this.applicationServerName = null;
    }

    public String getHost() { return host; }
    public int getPort() { return port;}
    public String getApplicationServerName() { return applicationServerName;}
    public boolean getUseSSL() { return useSSL;}
}
