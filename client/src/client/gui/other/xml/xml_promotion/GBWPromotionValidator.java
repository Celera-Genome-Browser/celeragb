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
package client.gui.other.xml.xml_promotion;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.concrete_facade.xml.GenomeVersionParser;
import java.net.*;
import java.io.*;

/**
 * This class takes in a .gbw file and gets PROMOTION validated by remotely executing the Promotion Utility
 * version PU_1.1 developed by Tom Smith. In time .gbw files to be in snyc with the future versions of the Promotion
 * Utility the /cps directory needs to be checked out.
 * validateXML.jsp deployed on the internal servers dss011a and dss016a are executes the Promotion Utility.pl
 *
 */


public class GBWPromotionValidator {
    private static GBWPromotionValidator gbwpromotionvalidator = new GBWPromotionValidator();


    static public GBWPromotionValidator getGBWPromotionValidator() {
      return gbwpromotionvalidator;
    }

    public GenomeVersion getGenomeVersion(String gbwfilename){
        GenomeVersionParser gp=new GenomeVersionParser();
        GenomeVersion gv=gp.parseForGenomeVersion(gbwfilename);
        System.out.println(gv.getAssemblyVersion());
        System.out.println(gv.getSpecies().toString());
        return gv;
   }


   static public String validateGBW( URL url, String assemblyVersion, String species, String gbw ) {
     StringBuffer buffer = new StringBuffer();
     String line;
     HttpURLConnection connection;
     InputStream input;
     BufferedReader dataInput;

     try {
       connection = (HttpURLConnection)url.openConnection();
       String encodedContent = "assembly="+URLEncoder.encode(assemblyVersion)+"&"+
                               "species="+URLEncoder.encode(species)+"&"+
                               "gbw="+URLEncoder.encode(gbw);
       connection.setDoOutput(true);

       //Send the encoded content
       OutputStream os = connection.getOutputStream();
       os.write(encodedContent.getBytes());
       os.flush();
       os.close();

       input = connection.getInputStream();
       dataInput = new BufferedReader(new InputStreamReader(input));
       while ((line = dataInput.readLine()) != null) {
          buffer.append(line);
          buffer.append('\n');
       }
       input.close();
       connection.disconnect();
     }
     catch (Exception ex) {
       //Throw error?
//       ex.printStackTrace();
     }

     //Remove the header information.
     //1. Look for 10 '#' characters and remove everything before it
     //2. If no '#' characters exist then remove everything before the 'ERROR' keyword.

     String removeHeader = buffer.toString();
     int errorIndex = removeHeader.indexOf("ERROR");
     int headerEndIndex = removeHeader.indexOf("##########");

     int trimIndex = headerEndIndex>=0?headerEndIndex:errorIndex;

     return removeHeader.substring((trimIndex>=0?trimIndex:0));
  }

  public static void main(String argv[]) {
    try {
      File f = new File("C:/someone/hCGx.gbw");
      BufferedReader dataInput = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
      StringBuffer buffer = new StringBuffer();
      String line ="";
      while ((line = dataInput.readLine()) != null) {
        buffer.append(line);
        buffer.append('\n');
      }
      System.out.println(GBWPromotionValidator.validateGBW(new URL("http://www.xxxxx.com:NNN/broadcast/validateXML.jsp"),"UIDNN","Homo sapiens",buffer.toString()));
    } catch( Exception e ) {
      e.printStackTrace();
    }
  }

}