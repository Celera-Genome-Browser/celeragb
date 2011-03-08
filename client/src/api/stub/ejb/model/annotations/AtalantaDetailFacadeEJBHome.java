// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Copyright (c) 1999 - 2006 Applera Corporation.
// 301 Merritt 7 
// P.O. Box 5435 
// Norwalk, CT 06856-5435 USA
//
// This is free software; you can redistribute it and/or modify it under the 
// terms of the GNU Lesser General Public License as published by the 
// Free Software Foundation; version 2.1 of the License.
//
// This software is distributed in the hope that it will be useful, but 
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License 
// along with this software; if not, write to the Free Software Foundation, Inc.
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//*============================================================================
//* 45 West Gude Drive, Rockville, Maryland, 20850, U.S.A.
//*
//* File:         AtalantaDetailFacadeEJBHome.java
//*
//* $Header$r: /cm/cvs/enterprise/src/api/stub/ejb/model/annotations/AtalantaDetailFacadeEJBHome.java,v 1.1 2003/12/30 18:59:53 lblick Exp $
//*============================================================================
package api.stub.ejb.model.annotations;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import java.rmi.RemoteException;

/**
 * Home interface definition for all AtalantaDetailFacadeEJBHome objects.
 *
 * @author        Lou Blick
 * @author        Lou.Blick
 * @author        x3443
 */
public interface AtalantaDetailFacadeEJBHome extends EJBHome {
   public AtalantaDetailFacadeEJB create() throws CreateException, RemoteException;
}
