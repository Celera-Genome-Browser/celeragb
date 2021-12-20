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
 * Title:        Chromosome Facade for browing app.<p>
 * Description:  Facade representing a chromsoome<p>
 * Company:      []<p>
 * @author Peter Davies
 * @version
 */
package api.facade.concrete_facade.xml;

import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.LoadRequest;
import api.facade.abstract_facade.genetics.ChromosomeLoader;
import api.stub.data.GenomicProperty;
import api.stub.data.OID;
import api.stub.geometry.Range;
import api.stub.sequence.Sequence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XmlChromosomeFacade extends XmlGenomicFacade implements ChromosomeLoader {

  /** Constructor takes all info required to get next level data. */
  XmlChromosomeFacade(GenomeVersionSpace genomeVersionSpace) {
      setGenomeVersionSpace(genomeVersionSpace);
  } // End constructor.

  /** Querying method for alignments to genomic axes. */
  public Alignment[] loadAlignmentsToEntities(OID entityOID, LoadRequest loadRequest) {
      Object nextIteration = null;
      List axisList = new ArrayList();
      Iterator it = getDataSourcesRepresentingGenomicAxes(entityOID.getGenomeVersionId());
      while (it.hasNext()) {
          nextIteration = it.next();
          if (nextIteration instanceof GenomicAxisXmlLoader) {
              // System.out.println("Adding loader for "+((GenomicAxisXmlLoader)nextIteration).getLoadedFileNames()[0]);
              axisList.add(((GenomicAxisXmlLoader)nextIteration).getGenomicAxisAlignment());
          } // Loader was right kind.
      } // FOr all loaders

      Alignment[] returnArray = new Alignment[axisList.size()];
      axisList.toArray(returnArray);

      return returnArray;
  } // End method: loadAlignmentsToEntities


  public Sequence getNucleotideSeq
    (OID genomicOID,
     Range nucleotideRange,
     boolean gappedSequence)
  {
    throw new RuntimeException("XmlChromosomeFacade::getNucleotideSeq not implemented");
  }

  public Alignment[] getAlignmentsToAxes(OID entityOID)
  {
    throw new RuntimeException("XmlChromosomeFacade::getAlignmentsToAxes not " +
      " implemented");
  }

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

}
