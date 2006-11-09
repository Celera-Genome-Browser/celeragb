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
package api.entity_model.model.alignment;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author       Peter Davies
 * @version      $Id$
 */

import java.util.ArrayList;

public class AlignmentNotAllowedException extends Exception {

  private ArrayList reasons=new ArrayList(3);
  private Alignment disAllowedAlignment;
  private boolean isDuplcateAlignment = false;
  private static final String lineSep=System.getProperty("line.separator");



  /**
   * Constructor for duplicate alignments...
   */
  public AlignmentNotAllowedException(boolean duplicateAlignment, Alignment disAllowedAlignment) {
    this("Duplicate Alignment", disAllowedAlignment);
    this.isDuplcateAlignment = true;
  }

  /**
   * General constructor...
   */
  public AlignmentNotAllowedException(String initialReason, Alignment disAllowedAlignment) {
     addReason(initialReason);
     this.disAllowedAlignment=disAllowedAlignment;
  }

  public void addReason(String reason) {
     reasons.add(reason);
  }

  public String[] getReasons() {
     return (String[])reasons.toArray(new String[0]);
  }

  public boolean isDuplcateAlignment() {
    return isDuplcateAlignment;
  }

  public Alignment getDisAllowedAlignment() {
     return disAllowedAlignment;
  }

  public String getMessage() {
     StringBuffer sb=new StringBuffer("An alignment was disallowed between ");
     sb.append(" axis "+disAllowedAlignment.getAxis());
     sb.append(" and entity "+disAllowedAlignment.getEntity());
     if (reasons.size()>1) sb.append(", for the following reasons: ");
     else sb.append(", for the following reason: ");
     for (int i=0;i<reasons.size();i++) {
       sb.append(reasons.get(i)+lineSep);
     }
     return sb.toString().trim();
  }
}
