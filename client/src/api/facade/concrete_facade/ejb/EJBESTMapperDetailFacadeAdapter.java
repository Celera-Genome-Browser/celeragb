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
//* File:         EJBESTMapperDetailFacadeAdapter.java
//*
//* $Header$r: /cm/cvs/enterprise/src/api/facade/concrete_facade/ejb/EJBESTMapperDetailFacadeAdapter.java,v 1.1 2004/01/06 17:40:28 lblick Exp $
//*============================================================================
package api.facade.concrete_facade.ejb;

import api.facade.abstract_facade.annotations.ESTMapperDetailFacade;

/**
 * EJB session bean that implements the ESTMapperDetailFacade interface.
 *
 * @author        Lou Blick
 * @author        Lou.Blick
 * @author        x3443
 */
public class EJBESTMapperDetailFacadeAdapter extends EJBHitAlignmentDetailLoaderAdapter implements ESTMapperDetailFacade {

   public EJBESTMapperDetailFacadeAdapter(RemoteInterfacePool aPool, int ejbRetries) {
      super(aPool, ejbRetries);
   }
}
