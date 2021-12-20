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

import java.util.ArrayList;
import java.util.Map;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author Les Foster
 * @version $Id$
 */
public class ArrayListElementStacker implements ElementStacker {

  //------------------------------MEMBER VARIABLES
  private ArrayList mAncestry;
  private String mCurrentElement;

  //------------------------------CONSTRUCTORS
  public ArrayListElementStacker() {
    mAncestry = new ArrayList();
  } // End constructor

  //------------------------------IMPLEMENTATION OF ElementStacker
  /**
   * Tracking for ancestry stacks.
   */
  public void popAncestry() {
    if (mAncestry.isEmpty())
      throw new IllegalStateException("Tried to pop element stack when no elements remain");

    mAncestry.remove(mAncestry.size() - 1);

    // Keep track of current element.
    if (mAncestry.isEmpty())
      mCurrentElement = null;
    else
      mCurrentElement = ((StartElementData)mAncestry.get(mAncestry.size()-1)).getElementName();

  } // End method: popAncestorAttributes

  public void pushAncestry(String lName, Map lAttributesMap) {
    // Keep track of current element.
    mCurrentElement = lName;
    mAncestry.add(new StartElementData(lName, lAttributesMap));
  } // End method: pushAncestry

  /** Convenience method to return current element. */
  public String getCurrentElement() {
    return mCurrentElement;
  } // End method: getCurrentElement

  /** Returns the "deepest" ancestor number. */
  public int getMaxAncestor() {
    return mAncestry.size() - 1;
  } // End method: getMaxAncestor

  /**
   * Returns the N-level ancestor element name.
   *
   * @param int lLevel the level, starting from the end, of element to get.
   * @return String element name.
   */
  public String retrieveAncestorLIFO(int lLevel) {
    String returnVal = null;
    try {
      returnVal = ((StartElementData)mAncestry.get(mAncestry.size() - 1 - lLevel)).getElementName();
    } catch (Exception lEX) {
      // Re-throw taylored exception.
      throw new IllegalStateException("Ancestry level of "+lLevel
                  +" was requested, when number of ancestors is "+mAncestry.size()+
                  " resulting in the exception "+lEX.toString());
    } // End catch block for ancestry.
    return returnVal;
  } // End method: retrieveAncestorLIFO

  /**
   * Returns the N-level ancestry-attributes map.
   *
   * @param int lLevel the level, starting from the end, of element to get.
   * @return Map attributes map.
   */
  public Map retrieveAncestorAttributesLIFO(int lLevel) {
    Map returnVal = null;
    try {
      returnVal = ((StartElementData)mAncestry.get(mAncestry.size() - 1 - lLevel)).getAttributes();
    } catch (Exception lEX) {
      // Re-throw taylored exception.
      throw new IllegalStateException("Ancestry level of "+lLevel
                  +" was requested, when number of ancestors is "+mAncestry.size()+
                  " resulting in the exception "+lEX.toString());
    } // End catch block for ancestry.
    return returnVal;
  } // End method: retrieveAncestorAttributesLIFO

  //-------------------------------------INNER CLASSES
  /** Holder for element data, to allow use of single stack. */
  class StartElementData {
    private String mElementName;
    private Map mAttributes;

    /** Constructor keeps all info required. */
    public StartElementData(String lElementName, Map lElementAttributes) {
      mElementName = lElementName;
      mAttributes = lElementAttributes;
    } // End constructor

    /** Getters to essentially hand back what was given in constructor. */
    public String getElementName() { return mElementName; }

    public Map getAttributes() { return mAttributes; }

  } // End class: StartElementData

} // End class
