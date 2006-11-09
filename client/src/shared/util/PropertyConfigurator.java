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
package shared.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TimeZone;


/**
 * The purpose of this class is to have a singleton from which to get properties.
 * At startup, read from a default file, then add overrides files,
 * command line params, etc.
 */
public class PropertyConfigurator {
    private static Properties props = new Properties();
    private static final String DEFAULT_PROPS_FILE = "resource.server.shared.ServerConfig";
    private static final String EJB_SRV_CONN_FILE = "resource.server.EJBServerConnection";
    private static final String DEPLOYMENT_PROPS_FILE = "resource.server.shared.DeployedBuild";
    private static final String POST_DEPLOY_PROP_FILE = "PostDeployConfig";
    private static final String CMD_LINE_OVERRIDE_CONFIG = "AppServerConfig";
    private static final String TIMEZONE_PROP = "appserver.timezone";
    private static final String SET_TIMEZONE_PROP = "appserver.timezone.doSetTimezone";

    static {
        //Server Specific setup:
        Properties sysProps = System.getProperties();
        String override = sysProps.getProperty(CMD_LINE_OVERRIDE_CONFIG);

        if (override != null) {
            // Get the Default properties
            try {
                PropertyConfigurator.add(DEFAULT_PROPS_FILE);
            } catch (MissingResourceException ex) {
                ex.printStackTrace();
            }

            // Get the EJB server connection props
            try {
                PropertyConfigurator.add(EJB_SRV_CONN_FILE);
            } catch (MissingResourceException ex) {
                ex.printStackTrace();
            }

            // Get the Deployment Properties
            try {
                PropertyConfigurator.add(DEPLOYMENT_PROPS_FILE);
            } catch (MissingResourceException ex) {
                // Do not report, this file optionally exists
            }

            //Last Override
            try {
                PropertyConfigurator.add(override);
            } catch (MissingResourceException ex) {
                ex.printStackTrace();
            }

            // Get Post-Deployment properties (See jsp.PostDeployConfig.jsp)
            try {
                PropertyConfigurator.add(POST_DEPLOY_PROP_FILE);
            } catch (MissingResourceException ex) {
                // Do not report, this file optionally exists
            }


            // Command line setting override all others
            PropertyConfigurator.add(sysProps);

            //System.out.println(PropertyConfigurator.getProperties().toString()); // DEBUG
            // Be sure that the server is in the correct timezone
            if (PropertyConfigurator.getProperties()
                                    .getProperty(SET_TIMEZONE_PROP, "false")
                                    .equalsIgnoreCase("true")) {
                String timezoneSetting = PropertyConfigurator.getProperties()
                                                             .getProperty(TIMEZONE_PROP, 
                                                                          "EST");
                TimeZone.setDefault(TimeZone.getTimeZone(timezoneSetting));
            }
        }
    }

    private PropertyConfigurator() {
    }

    public static Properties getProperties() {
        return props;
    }

    public static void add(String resourceBundleName)
                    throws MissingResourceException {
        ResourceBundle rbundle = ResourceBundle.getBundle(resourceBundleName);
        PropertyConfigurator.add(rbundle);
    }

    public static void add(ResourceBundle rbundle) {
        for (Enumeration enum = rbundle.getKeys(); enum.hasMoreElements();) {
            String key = (String) enum.nextElement();
            String val = rbundle.getString(key);


            //      System.out.println("Adding property key: " + key + " value " + val); // DEBUG
            PropertyConfigurator.props.setProperty(key, val);
        }
    }

    public static void add(Properties newProps) {
        for (Iterator iter = newProps.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            String val = (String) entry.getValue();


            //      System.out.println("Adding property key: " + key + " value " + val); // DEBUG
            PropertyConfigurator.props.setProperty(key, val);
        }
    }
}