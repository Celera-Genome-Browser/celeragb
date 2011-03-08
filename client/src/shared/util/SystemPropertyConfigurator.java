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

/**
 * Title:        Your Product Name<p>
 * Description:  This is the main Browser in the System<p>
 * @author Peter Davies
 * @version
 */
package shared.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * This class will read a properties file and add all key,value pairs to
 * the system properties if the individual property to be added does not already
 * exist.  This allows the application to specify a set of defaults and override
 * them at the command line.
 *
 * If the -DConfig=<filename> is not set at the commend line, this class will look for the
 * defaultConfigFile, which can be set with setDefaultConfigFile.
 */
public class SystemPropertyConfigurator {

    static private final String CONFIG_RESOURCE_NAME = "x.genomebrowser.Config";
    static private String defaultConfigFile;

    public static void setDefaultConfigFile(String filename) {
       defaultConfigFile=filename;
    }

    public static void loadConfiguration() {
       //Load CLient Config file
         String rbLocation=System.getProperty(CONFIG_RESOURCE_NAME);
         ResourceBundle rb=null;
         if (rbLocation==null) {
           try {
             rb=ResourceBundle.getBundle(defaultConfigFile);
             //Integration to PropertyConfigurator
             PropertyConfigurator.add(rb);
           }
           catch (MissingResourceException mrEx) {
              System.out.println("ERROR: -D"+CONFIG_RESOURCE_NAME+"=<config file relative to classpath>"+
                 " must be specified as a command line argument, or "+defaultConfigFile+
                 " must be present!!");
              System.exit(1);
           }
         }
         if (rb==null) {
           try {
             rb=ResourceBundle.getBundle(rbLocation);
             //Integration to PropertyConfigurator
             PropertyConfigurator.add(rb);
           }
           catch (MissingResourceException mrEx) {
              System.out.println("ERROR: -D"+CONFIG_RESOURCE_NAME+"=<config file relative to classpath>"+
                 " must be specified as a command line argument, or resource.client.DeveloperClientConfig.properties"+
                 " must be present!!");
              System.exit(1);
           }
         }
         Enumeration keys=rb.getKeys();
         String key;
         for ( ;keys.hasMoreElements();) {
            key=(String)keys.nextElement();
            if (System.getProperty(key)==null) {
              System.setProperty(key,rb.getString(key));
            }
         }
    }
}