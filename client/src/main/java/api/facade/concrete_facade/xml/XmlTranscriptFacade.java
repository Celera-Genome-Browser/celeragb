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
/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
 *********************************************************************/
import api.facade.abstract_facade.annotations.TranscriptFacade;
import api.stub.data.ControlledVocabUtil;
import api.stub.data.GenomicProperty;
import api.stub.data.OID;

import java.util.Iterator;
import java.util.Map;

/**
 * Called by proxy intervals to get information on transcripts.
 */
public class XmlTranscriptFacade extends XmlFeatureFacade
  implements TranscriptFacade  {

  // NOTE: if retrieve evidence is called, it will be the superclass' method.
  //---------------------------------------HELPER METHODS
  /** Callable from subclasses to efficiently pass properties. */
  protected Map inheritProperties(OID featureOID) {
    // No OID-> pass request up the hierarchy.
    if (featureOID == null)
      return super.inheritProperties(featureOID);

    // No loader set for this feature, means "shine on" this request,
    // and let some other facade handle it.
    Iterator featureLoaders = getXmlLoadersForFeature(featureOID);
    if (! featureLoaders.hasNext())
      return java.util.Collections.EMPTY_MAP;

    XmlLoader featureLoader = null;

    Map returnProperties = super.inheritProperties(featureOID);

    String geneAcc = null;
    String transcriptAcc = null;

    boolean foundGeneAcc = false;
    boolean foundTranscriptAcc = false;

    // Go through all sources with info on the feature in question.  Pickup
    // the first occurence of each of the two properties required.
    while (featureLoaders.hasNext()) {
      featureLoader = (XmlLoader)featureLoaders.next();

      geneAcc = featureLoader.getGene(featureOID);
      if ((! foundGeneAcc) && (geneAcc != null)) {
        // Construct all properties specific to this type of facade.
        returnProperties.put(TranscriptFacade.GENE_ACCESSION_PROP, createGeneAccessionProperty(TranscriptFacade.GENE_ACCESSION_PROP, geneAcc));
        foundGeneAcc = true;
      } // Pick it up if in this loader.

      transcriptAcc = featureLoader.getTrscptacc(featureOID);
      if ((! foundTranscriptAcc) && (transcriptAcc != null)) {
        returnProperties.put (TranscriptFacade.TRANSCRIPT_ACCESSION_PROP, new GenomicProperty(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP, "",
          featureLoader.getTrscptacc(featureOID), true, ControlledVocabUtil.getNullVocabIndex()));
        foundTranscriptAcc = true;
      } // Pick it up if in this loader.

      // Break out when fully populated.
      if (foundGeneAcc && foundTranscriptAcc)
        break;

    } // For all loaders in the set.

    if (!foundGeneAcc)
      returnProperties.put(TranscriptFacade.GENE_ACCESSION_PROP, createGeneAccessionProperty(TranscriptFacade.GENE_ACCESSION_PROP, "null"));

    if (!foundTranscriptAcc)
      returnProperties.put(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP, new GenomicProperty(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP, "",
          "null", true, ControlledVocabUtil.getNullVocabIndex()));

    return returnProperties;
  } // End method: inheritProperties

} // End class: XmlTranscriptFacade
