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
/**********************************************************************
**********************************************************************/

/**********************************************************************
$Source$cm/cvs/enterprise/src/client/gui/other/accession_numbers/SYS_UIDerrorMessages.java,v $
$Revision$
$Date$
$Name$
$Author$
$Log$
Revision 1.1  2000/03/29 21:03:43  dwu
New GUI interface for easy testing of Accession number servers.

Revision 1.2  1999/08/18 19:57:15  BaxendJn
Compiles now I hope. Had cvs stuff in file that should not
have been.

Revision 1.1  1999/08/18 17:13:32  BaxendJn
Code for retrieving a new UID from a known UID server.

Revision 1.1  1999/06/14 19:40:09  gorokhmn
Created

Revision 1.1  1999/05/21 22:50:29  gorokhmn
New files in project (Ask Sean)

Revision 1.2  1999/01/13 14:42:33  sdmurphy
version 0 prelim

Revision 1.1  1998/12/30 20:28:41  sdmurphy
renamed from UidErrorMesssages.java

Revision 1.1  1998/12/18 17:57:43  sdmurphy
java class for handling error msgs

**********************************************************************/

/**********************************************************************
Module:

Description:

Assumptions:

**********************************************************************/

package client.gui.other.accession_numbers;

public class SYS_UIDerrorMessages 
{

   public static void ReportError(String message)
   {
      System.err.print("SYS_UIDerrorMessage: ");
      System.err.println(message);
   }

   public static void FatalError(String message)
   {
      System.err.print("SYS_UIDerrorMessage: ");
      System.err.println(message);
      System.err.println("Exiting...");
      System.exit(-1);
   }
}
