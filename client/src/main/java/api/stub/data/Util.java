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
/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
 *********************************************************************/
package api.stub.data;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPInputStream;


public class Util
{

  public static final String FEATURE_UNGROUPED = "Ungrouped";
  private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

  public static int convertDateTimeAsStringToSecondsSince1970(String dateString)
    throws java.text.ParseException
  {
    Date aDate = dateTimeFormat.parse(dateString);
    int secondsSince1970 = (int)(aDate.getTime() / 1000L);
    return secondsSince1970;
  }

  public static String convertSecondsSince1970ToDateTimeString(int secondsSince1970)
  {
    long miliSince1970 = secondsSince1970 * 1000L;

    Date aDate = new Date(miliSince1970);
    String dateAsString = dateTimeFormat.format(aDate);
    return dateAsString;
  }

  public static String getDateTimeStringNow() {
    return dateTimeFormat.format(new Date());
  }


  static public String gunZip(byte zippedBytes[]) throws IOException
  {
    StringBuffer sUnzipped = new StringBuffer(1024*1024);

    BufferedReader zin = new BufferedReader(
                  new InputStreamReader(
                    new GZIPInputStream(
                      new ByteArrayInputStream(zippedBytes))));

    String sTmp;
    while ((sTmp = zin.readLine()) != null)
    {
      sUnzipped.append(sTmp + "\n");
    }

    return sUnzipped.toString();
  }

}

