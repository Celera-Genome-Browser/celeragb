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
package api.facade.concrete_facade.xml;

/**
 * Title:        Genome Browser Client
 * Description:  Facade Implementation in XML for Species
 * @author Les Foster
 * @version $Id$
 */

import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.LoadRequest;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.genetics.SpeciesLoader;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.GenomicProperty;
import api.stub.data.NoData;
import api.stub.data.OID;
import api.stub.geometry.Range;
import api.stub.sequence.Sequence;


/**
 * @todo This class should extend an XmlAxis class that defines all the things
 * an axis can do so that dont have to repeat this code in Species, Chromosome
 * Genomic Axis and eventually Congig
 *
 * When queried, gets alignments for a species.
 */
public class XmlSpeciesFacade extends XmlGenomicFacade implements SpeciesLoader {

  /** Build this with a Genomic Axis Facade manager, so it can add loader. */
  public XmlSpeciesFacade(GenomeVersionSpace genomeVersionSpace) {
      setGenomeVersionSpace(genomeVersionSpace);
  } // End constructor

  /** Returns alignments to chromosome entities. */
  public Alignment[] loadAlignmentsToEntities(OID entityOID, LoadRequest loadRequest) {
    GenomeVersionSpace genomeVersionSpace = getGenomeVersionSpace();
    if (genomeVersionSpace == null) {
        FacadeManager.handleException(new IllegalStateException("No genome version space for species facade"));
        return new Alignment[0];
    } // Not found

    XmlLoader speciesLoader = genomeVersionSpace.getLoaderForSpecies(entityOID);

    // Now grab the alignments for the species.
    if (speciesLoader == null) {
        return new Alignment[0];
    } // The genome version space had no knowledge of the species.
    else {
        return new Alignment[] {
          ((GenomicAxisXmlLoader)speciesLoader).getAlignmentForChromosome(genomeVersionSpace.getSpeciesOf(entityOID))
        };
    } // Source found

  } // End method: loadAlignmentsToEntities

  // Unused thus far...
  public Sequence getNucleotideSeq(OID genomicOID,Range nucleotideRange,
      boolean gappedSequence) {
    return null;
  }

  public Alignment[] getAlignmentsToAxes(OID entityOID) {
    return new Alignment[0];
  }

  /** Dummy this out. Used by other clients breaking in "in the middle" of the hierarchy. */
  public GenomeVersion[] getGenomeVersions(OID oid) throws NoData {
    return new GenomeVersion[0];
  } // End method; getGenomeVersion

  /** Properties getter overridden to apply 'heuristic bail' */
  public GenomicProperty[] getProperties(OID genomicOID, EntityType dynamicType, boolean deepload) {

    // Test: did the genome version of the chromosome OID originate from
    // this facade instance's facade manager?
    int genomeVersionIdOfOid = genomicOID.getGenomeVersionId();
    if (getSourcedGenomeVersionOidForId(genomeVersionIdOfOid) == null)
      return EMPTY_GENOMIC_PROPERTY_ARRAY;
    else
      return super.generatePropertiesForOID(genomicOID);
  } // End method: getProperties

} // End class: XmlSpeciesFacade
