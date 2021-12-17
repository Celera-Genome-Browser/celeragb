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
package api.facade.abstract_facade.genetics;

import api.entity_model.access.report.GenomeVersionAlignmentReport;
import api.entity_model.access.report.PropertyReport;
import api.entity_model.access.report.SubjectSequenceReport;
import api.facade.abstract_facade.fundtype.GenomicEntityLoader;
import api.stub.data.InvalidPropertyFormat;
import api.stub.data.NavigationPath;
import api.stub.data.OID;

public interface GenomeVersionLoader extends GenomicEntityLoader {

   public GenomeVersionAlignmentReport generateAlignmentReportForEntity( OID entityOID );
   public PropertyReport generatePropertyReport( OID genomeVerOID, OID [] entityOIDs, String [] propNames );
   public SubjectSequenceReport generateSubjectSequenceReportForEntity(OID entityOID);

   /**
    * This version of requesting a navigation path with return only those
    * paths that exist within the Genome Version identified by genomeOID
    */
   public NavigationPath[] getNavigationPath( OID genomeOID, String targetType, String target) throws InvalidPropertyFormat;

   /**
    * This version will return all navigation paths in all released genome
    * versions known to the system for mixed species if they are available.
    * NOTE: Effectively this method represents a class level loader
    * of Genome Version Loader.
    */
   public NavigationPath[] getNavigationPath( String targetType, String target ) throws InvalidPropertyFormat;

   /**
    * Gets the index of the controlled vocabulary that can be used to provide
    * the targetType argument for each of the getNavigationPath methods of
    * GenomeVersionLoader
    */
   public String getNavigationVocabIndex();
}
