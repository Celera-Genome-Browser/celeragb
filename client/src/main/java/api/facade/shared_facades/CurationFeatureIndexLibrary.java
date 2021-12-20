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

package api.facade.shared_facades;

import api.entity_model.model.fundtype.EntityType;
import api.facade.abstract_facade.annotations.FeatureIndexLibrary;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.stub.data.AccessionGenerator;
import api.stub.data.FeatureIndex;
import api.stub.data.OID;

import java.math.BigDecimal;
import java.util.ResourceBundle;

/**
 * This class is used when users make annotations.
 */
public class CurationFeatureIndexLibrary implements FeatureIndexLibrary {

  static private String transcriptPrefix  = "CT";
  static private String genePrefix        = "CG";


  static {
    ResourceBundle rb=ResourceBundle.getBundle( System.getProperty( "x.genomebrowser.AccessionProperties" ) );
    try {
      transcriptPrefix = rb.getString( "Transcript_Accession_Prefix" );
      genePrefix = rb.getString( "Gene_Accession_Prefix" );
      String initialGeneValue = rb.getString( "Initial_Gene_Number" );
      String initialTranscriptValue = rb.getString( "Initial_Transcript_Number" );
      AccessionGenerator.getAccessionGenerator().setInitialValueForPrefix( transcriptPrefix, initialTranscriptValue );
      AccessionGenerator.getAccessionGenerator().setInitialValueForPrefix( genePrefix, initialGeneValue );
    }
    catch ( Exception ex ) {
      System.err.print( "ERROR: CurationFeatureIndexLibrary static constructor cannot find resource.client.Accession file. Using defaults (CG and CT)" );
    }
  }



  /**
   * This "multiplex" method acts as a standardized entry point with a feature
   * type as its switching variable.
   */
  public FeatureIndex generateIndexForType( OID genomeOid, EntityType aType, BigDecimal assemblyVersion ) {
    if ( aType.value() == EntityTypeConstants.NonPublic_Gene ) {
      return ( this.getGeneIndex() );
    }
    else if ( aType.value() == EntityTypeConstants.NonPublic_Transcript ) {
      return ( this.getTranscriptIndex() );
    }
    else {
      return ( null );
    }
  }


  /**
   * Returns a new Feature Index representing a Gene.
   */
  private FeatureIndex getGeneIndex() {

    // Generate a new accession number (unique acros the loaded scratch space).
    // Simultaneously position the "generator" for the next such accession.
    String accession = AccessionGenerator.getAccessionGenerator().generateAccessionString( genePrefix );

    // Generate a new OID to cover this transcript, and add that to
    // the collection of "knowns" so it may be queried later for properties.
//    OID oid = OIDGenerator.getOIDGenerator().generateScratchOID();

    return ( new FeatureIndex( accession ) );
  }


  /**
   * Returns the index of the transcript.  Does so by producing a new one each
   * time this method is called!
   */
  private FeatureIndex getTranscriptIndex() {

    // Generate a new accession number (unique across the loaded
    // scratch space).  Also position "generator" for next unique acc no.
    String accession = AccessionGenerator.getAccessionGenerator().generateAccessionString( transcriptPrefix );

    // Create a feature index using the accession number generated from the
    // above interaction.
    return ( new FeatureIndex( accession ) );
  }

}
