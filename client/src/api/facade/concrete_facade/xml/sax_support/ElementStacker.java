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
 * Title:        Genome Browser<p>
 * Description:  Element stacker will help to track what the ancestors of an element are
 *               in a SAX handler environment<p>
 * Company:      []<p>
 * @author Les Foster
 * @version
 */
package api.facade.concrete_facade.xml.sax_support;

import java.util.Map;

/**
 * Tracking mechanism for element names and attributes, so that
 * parent and other ancestor tags can be found when needed in a SAX parse.
 */
public interface ElementStacker {

    /**
     * Tracking for ancestry stacks.
     */
    public void popAncestry();

    public void pushAncestry(String lName, Map lAttributesMap);

    public String retrieveAncestorLIFO(int lAncestorNumber);

    public Map retrieveAncestorAttributesLIFO(int lAncestorAttributesNumber);

    public String getCurrentElement();

    public int getMaxAncestor();

} // End interface: ElementStacker