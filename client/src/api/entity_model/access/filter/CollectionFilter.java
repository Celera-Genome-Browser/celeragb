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
package api.entity_model.access.filter;

import java.util.Comparator;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author       Peter Davies
 * @version      $Id$
 *
 * This class is a base class for other abstract filters defined in this
 * package.  It should not and cannot be directly subclassed outside of this
 * package.  Instead subClass one of the less abstract classes.
 */

public abstract class CollectionFilter {

  private Comparator comparator;

  public CollectionFilter(){}

  //Should only be directly sub-classes in this package --PED 1/18/01
  CollectionFilter(Comparator comparator) {
     this.comparator=comparator;
  }

  /**
   * This method can be overridden to return false for sorted only collection.
   * However, it is easier to call createSortOnlyCollectionFilter to achieve
   * this.
   */
  public boolean requestFilteredCollection() {
     return true;
  }

  public final Comparator getComparator() {
     return comparator;
  }

  public final boolean requestSortedCollection() {
     return (comparator!=null);
  }

}