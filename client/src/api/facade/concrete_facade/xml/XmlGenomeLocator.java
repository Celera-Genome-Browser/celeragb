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
 * Description:  Genome Locator implementation for XML path<p>
 * Company:      []<p>
 * @author Peter Davies
 * @version
 */
package api.facade.concrete_facade.xml;

import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.genetics.GenomeLocatorFacade;

/**
 * Simple implementation returns same info each call.
 */
public class XmlGenomeLocator implements GenomeLocatorFacade {

  // Location in which to find the genome versions.
  private GenomeVersionSpace genomeVersionSpace = null;

  /**
   * Build the locator with sufficient information to "source up"
   * all genome versions.
   */
  XmlGenomeLocator(GenomeVersionSpace genomeVersionSpace) {
      this.genomeVersionSpace = genomeVersionSpace;
  } // End constructor

  /**
   * Returns all genome versions represented by loaders in the collection.
   */
  public GenomeVersion[] getAvailableGenomeVersions() {
      return genomeVersionSpace.getGenomeVersions();
  } // End method: getAvailableGenomeVersions

//NOTE: not sure these have a future....
  public GenomeVersion latestGenomeForSpecies(String speciesName) {
     return null;
  }

  public GenomeVersion getNthGenomeForSpecies(String speciesName, long versionNumber) {
     return null;
  }

} // End class: XmlGenomomeLocator
