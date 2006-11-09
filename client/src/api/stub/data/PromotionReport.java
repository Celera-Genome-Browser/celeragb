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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PromotionReport implements java.io.Serializable
{
  private boolean promotable;
  private List details = new ArrayList();
  private String successMsg = null;

  public PromotionReport(boolean promotable, String msg)
  {
    this.promotable = promotable;

    if (promotable == true)
    {
      successMsg = msg;
    }
    else
    {
      if (!msg.equals(""))
      {
        details.add(msg);
      }
    }
  }

  public boolean wasPromotable()
  {
    return promotable;
  }

  public List getReportDetails()
  {
    /*Iterator iter = details.iterator();
    String reportDetails = "";

    if (promotable)
    {
      reportDetails += "Promotion successfull with the following warnings: ";
      reportDetails += "\n";
    }
    else
    {
      reportDetails += "Promotion NOT successfull. The following errors occured: ";
      reportDetails += "\n";
    }

    String currentMsg = null;
    while (iter.hasNext())
    {
      currentMsg = (String)iter.next();
      reportDetails += currentMsg;
      reportDetails += "\n";
    }
    return reportDetails;*/

    if (promotable)
    {
      details.add(0, "\n" + successMsg + " passed all promotion checks");
    }
    else
    {
      details.add(0, "\n" + successMsg + " failed promotion check with " +
        details.size() + " errors");
    }

    return details;
  }

  public void addFatalError(String msg)
  {
    details.add(msg);
    promotable = false;
  }

  public void addWarning(String msg)
  {
    details.add(msg);
  }

  public void incorperateSubReport(PromotionReport subReport)
  {
    // Only take on the promotable flag if not already in failed state
    if (promotable)
    {
      promotable = subReport.wasPromotable();
    }
    details.addAll(subReport.getReportDetails());
  }

  public String toString()
  {
    String reportString = "";
    if (promotable)
    {
      reportString += "Report on successfull promotion with " + details.size() + " warnings";
    }
    else
    {
      reportString += "Report on UNSUCCESSFULL promotion with " + details.size() + " errors";
    }

    String message = null;
    Iterator messageIter = details.iterator();
    while (messageIter.hasNext())
    {
      message = (String)messageIter.next();
      reportString += "\n";
      reportString += message;
    }

    return reportString;
  }
}
