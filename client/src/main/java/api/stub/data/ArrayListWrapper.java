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

/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
 *********************************************************************/



/**
 * A wrapper for an ArrayList to be used when passing such a list through JMS.
 * For some reason JMS barfs on sending the ArrayList itself with a ClassNotFound
 * excpetion even though the class that it cant find IS in the classpath of the
 * server.
 *
 * Hopefully this will be a temporary solution.
 * @author James Baxendale <james.baxendale>
 * @version 1.0
 */
import java.util.ArrayList;

public class ArrayListWrapper implements java.io.Serializable
{
  private ArrayList list = null;
  private String message = null;

  // set to true when all done processing and this is the last message from a
  // chunked query
  private boolean queryDone = false;

  public ArrayListWrapper(ArrayList listToWrap)
  {
    list = listToWrap;
  }

  public ArrayListWrapper(ArrayList listToWrap, String p_message)
  {
    list = listToWrap;
    this.message = p_message;
  }

  public ArrayList getArrayList()
  {
    return list;
  }

  public String getErrorMessage() {
    return message;
  }

  public void setErrorMessage(String p_message) {
    this.message = p_message;
  }

  public boolean isDone() {
    return queryDone;
  }

  public void setIsDone() {
    queryDone = true;
  }
}

