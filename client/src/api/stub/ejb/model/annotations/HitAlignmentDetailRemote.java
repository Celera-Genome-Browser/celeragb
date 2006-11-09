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
package api.stub.ejb.model.annotations;

import api.stub.data.NoData;
import api.stub.data.OID;
import api.stub.data.SubjectDefinition;

import java.rmi.RemoteException;

public interface HitAlignmentDetailRemote extends FeatureFacade {

  String getQueryAlignedResidues( OID alignmentChildOID ) throws NoData, RemoteException;
  String getSubjectAlignedResidues( OID alignmentChildOID ) throws NoData, RemoteException;
  SubjectDefinition [] getSubjectDefinitions( OID featureOID ) throws NoData, RemoteException;
}
