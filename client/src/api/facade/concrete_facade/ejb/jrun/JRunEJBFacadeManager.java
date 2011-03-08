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
package api.facade.concrete_facade.ejb.jrun;

import api.facade.concrete_facade.ejb.AbstractEJBFacadeManager;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;


/**
 * DOCUMENT ME!
 *
 * @version $Revision$
 * @author $author$
 */
public class JRunEJBFacadeManager extends AbstractEJBFacadeManager {
    private final static String CONTEXT_FACTORY = "jrun.naming.JRunContextFactory";

    protected void initialize() {
    }

    protected void shutdown() {
    }

    protected boolean getDefaultSharingOfInterfacesAllowed() {
        return true;
    } //Allowed in WebLogic

    protected String hostPortToUrl(String host, String port, boolean useSSL) {
        if (useSSL) {
            //return ("t3s://" + host + ":" + port);
            throw new UnsupportedOperationException("SSL not supported yet");
        } else {
            return (host + ":" + port);
        }
    }

    protected String hostPortToUrl(String host, int port, boolean useSSL) {
        return (hostPortToUrl(host, Integer.toString(port), useSSL));
    }

    protected Context getEJBContextRemoteService(String url, String userId, 
                                                 String password)
                                          throws Exception {
        Context ctx = null;

        Properties props = new Properties();


        // Matches the java.naming.factory.initial property
        // in the server's SERVER-INF/jndi.properties file.
        props.put(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY);


        // Matches the java.naming.provider.url property 
        // in the server's SERVER-INF/jndi.properties file.
        // Can also be a comma-delimited list.
        props.put(Context.SECURITY_PRINCIPAL, userId);
        props.put(Context.SECURITY_CREDENTIALS, password);
        props.put(Context.PROVIDER_URL, url);
        ctx = new InitialContext(props);

        return ctx;
    }
}