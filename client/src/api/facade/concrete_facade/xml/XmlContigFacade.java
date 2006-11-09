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
/*********************************************************************
  *********************************************************************
    CVS_ID:  $Id$
  *********************************************************************/

package api.facade.concrete_facade.xml;

import api.entity_model.model.fundtype.EntityType;
import api.facade.abstract_facade.assembly.ContigFacade;
import api.stub.data.GenomicProperty;
import api.stub.data.OID;
import api.stub.geometry.Range;
import api.stub.sequence.Sequence;

import java.util.Hashtable;
import java.util.Iterator;

public class XmlContigFacade extends XmlGenomicFacade
  implements ContigFacade {

 // static long unique_long = 10000L;
  static long unique_long=5000L;
  String contig_residues = null;
  Hashtable contigResiduesHash=new Hashtable();

  // DummyScaffoldFacade needs to set residues to use for contig
  void setContigResidues(String contig_residues, OID contigOID) {
    contigResiduesHash.put(contigOID,contig_residues);
  }

  // NOTE: this method is deprecated in the aggregator by its returning of null
  public Sequence getNucleotideSeq(OID genomicOID, Range nucleotideRange, boolean gappedSequence) {
    //    System.out.println("start = " + startNucleotidePos +
    //    		       ", end = " + endNucleotidePos);

    Sequence contig_residues=null;

    return contig_residues;
  }



  /**
   * Returns properties associated with the contig whose OID is given.
   */
  public GenomicProperty[] getProperties(OID parentOID, OID contigOID, EntityType dynamicType, boolean deepLoad) {

    XmlLoader loader = null;
    XmlLoader nextLoader = null;
    for (Iterator it = getFeatureSourcesInGenomeVersion(contigOID.getGenomeVersionId()); it.hasNext(); ) {
      nextLoader = (XmlLoader)it.next();
      if ((nextLoader.getContigAlignment() != null) && (nextLoader.getContigAlignment().getEntity() != null) && (nextLoader.getContigAlignment().getEntity().getOid().equals(contigOID)))
        loader = nextLoader;
    } // For all known loaders.

    GenomicProperty[] props = null;
    if (loader != null)
      props = this.generatePropertiesForOID(contigOID);
    else
      props = EMPTY_GENOMIC_PROPERTY_ARRAY;

    return props;

  } // End method: getProperties
} // End class: XmlContigFacade
