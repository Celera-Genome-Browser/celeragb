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
package api.facade.concrete_facade.xml.sax_support;

import java.io.Serializable;
import java.util.List;

/**
 * Title:        Genome Browser/XML Loading<p>
 * Description:  This is the main Browser in the System<p>
 * Convenience class to avoid having multiple mappings to describe
 * one set of data.
 * Company:      []<p>
 * @author Les Foster
 * @version
 */
public class ReplacedData implements Serializable {

    List replacedOIDList;
    String replacedType;
    String text;

    /**
     * Constructor takes all data that will later be returned.  Sort of
     * an "immutable" structure.
     */
    public ReplacedData(List replacedOIDList, String type, String text) {
      this.replacedOIDList = replacedOIDList;
      replacedType = type;
      this.text = text;
    } // End constructor

    /**
     * Simple getter methods to return the "structure data"
     */
    public List getOIDs () { return replacedOIDList; }
    public String getType() { return this.replacedType; }
    public String getText() { return this.text; }

} // End class: ReplacedData
