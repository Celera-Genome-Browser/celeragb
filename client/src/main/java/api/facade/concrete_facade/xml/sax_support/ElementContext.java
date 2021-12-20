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

import java.util.Map;

/**
 * Class which can expose the current context of an element.
 */
public class ElementContext {

    private ElementStacker mElementStack;

    //---------------------------------CONSTRUCTORS
    /**
     * Constructor sets up the element stack.
     */
    public ElementContext (ElementStacker lElementStack) {
        mElementStack = lElementStack;
    } // End constructor.

    //---------------------------------PUBLIC INTERFACE METHODS
    /**
     * Returns the current element in effect.  Useful for
     * characters call, especially.  Consider this a facility.
     */
    public String currentElement() {
        return mElementStack.getCurrentElement();
    } // End method: currentElement

    /**
     * Returns the <emph>name</emph> of the ancestor at the level given.
     * CONVENTION: level 0 is the currently-open tag,
     *   level 1 is the parent, level 2 is the grandparent, etc.
     *
     * @param int lAncestryLevel level of ancestor whose name should be returned.
     * @return String the name of the tag of ancestor element at level given.
     */
    public String ancestorNumber(int lAncestryLevel) {
        return (String)mElementStack.retrieveAncestorLIFO(lAncestryLevel);
    } // End method: ancestorNumber

    /**
     * Returns the highest-numbered ancestor currently loaded.
     */
    public int maxAncestor() {
        return mElementStack.getMaxAncestor();
    } // End method: maxAncestor

    /**
     * Returns the <emph>attributes</emph> of the ancestor at the level given.
     * CONVENTION: level 0 is the currently-open tag,
     *   level 1 is the parent, level 2 is the grandparent, etc.
     *
     * @param int lAncestryLevel level of ancestor whose name should be returned.
     * @return Map the map of attributes for the ancestor at the level given.
     */
    public Map ancestorAttributesNumber(int lAncestryLevel) {
        return (Map)mElementStack.retrieveAncestorAttributesLIFO(lAncestryLevel);
    } // End method: ancestorAttributesNumber

} // End class: ElementContext
