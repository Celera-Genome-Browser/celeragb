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

import api.entity_model.model.fundtype.EntityType;
public final class FeatureCategory implements java.io.Serializable
{
  public static final FeatureCategory PRECOMPUTE = new FeatureCategory(0);
  public static final FeatureCategory CURATION   = new FeatureCategory(1);
  public static final FeatureCategory MAP        = new FeatureCategory(2);

  public boolean equals(Object obj) {
    boolean retVal = false;
    if (obj instanceof FeatureCategory) {
      retVal = this.catNum == ((FeatureCategory)obj).catNum;
    }
    return retVal;
  }

  private int catNum;

  private FeatureCategory() {}

  private FeatureCategory(int catNum) {
    this.catNum = catNum;
  }

  public final int getCategoryCode()
  {
    return catNum;
  }

  /**
   * Returns the correct feature category given an array of EntityType objects
   * or NULL if there is no matching feature category.
   */
  static public FeatureCategory findCategoryForEntityTypes(EntityType[] type)
  {
    // REVISIT:
    // HACK for quick turnaround for testing just return precomputed
    // for now
    return PRECOMPUTE;
  }

}