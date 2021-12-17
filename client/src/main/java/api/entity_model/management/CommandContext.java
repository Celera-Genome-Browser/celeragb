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
package api.entity_model.management;


import api.entity_model.model.fundtype.GenomicEntity;
import api.stub.geometry.Range;


/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author Jay T. Schira
 * @version $Id$
 */

public class CommandContext {
  /**
   * The primary entity that the command should work against.
   * This should always be non-null.
   */
  public GenomicEntity primaryEntity;

  /**
   * The secondary entity (if there is one) that the command should work against.
   */
  public GenomicEntity secondaryEntity;

  /**
   * If the command requires a range...
   */
  public Range range;

  /**
   * If the command requires an Axis position
   */
  public int position;

  /**
   * If the command requires a key, value pair.
   * The key and value attributes are primarily used by SetProperty
   */
  public String key;
  public String value;


  /**
   * Constructor...
   */
  public CommandContext(GenomicEntity primaryEntity, GenomicEntity secondaryEntity,
                        Range range, int pos,
                        String key, String value) {
    /**
     * @todo This class is not used by anyone and can be removed.
     */
    this.primaryEntity = primaryEntity;
    this.secondaryEntity = secondaryEntity;
    this.range = range;
    this.position = pos;
    this.key = key;
    this.value = value;
  }

}