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
package api.stub.ejb.model.genetics;

import api.entity_model.access.report.GenomeVersionAlignmentReport;
import api.entity_model.access.report.PropertyReport;
import api.entity_model.access.report.SubjectSequenceReport;
import api.entity_model.model.genetics.Chromosome;
import api.stub.data.InvalidPropertyFormat;
import api.stub.data.NavigationPath;
import api.stub.data.NoData;
import api.stub.data.OID;

public interface GenomeVersion extends javax.ejb.EJBObject {

   Chromosome[] listChromosomes( long genomeVersion ) throws NoData, java.rmi.RemoteException;
   GenomeVersionAlignmentReport generateAlignmentReportForEntity( OID entityOID ) throws java.rmi.RemoteException;
   PropertyReport generatePropertyReport( OID genomeVerOID, OID [] entityOIDs, String [] propNames )  throws java.rmi.RemoteException;
   SubjectSequenceReport generateSubjectSequenceReportForEntity( OID entityOID ) throws java.rmi.RemoteException;
   NavigationPath[] getNavigationPath(OID genomeOID, String targetType, String target) throws InvalidPropertyFormat, java.rmi.RemoteException;
   NavigationPath[] getNavigationPath(String targetType, String target) throws InvalidPropertyFormat, java.rmi.RemoteException;
   String getNavigationVocabIndex() throws java.rmi.RemoteException;
}
