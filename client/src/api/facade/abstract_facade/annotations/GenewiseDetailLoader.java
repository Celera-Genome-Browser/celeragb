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
package api.facade.abstract_facade.annotations;

public interface GenewiseDetailLoader extends HitAlignmentDetailLoader {

   //--------------------------------------------------------------------------
   // Definition of property name constants that can be user in calls to
   // getProperty inherited from GenomicFacade.
   //
   // NOTE: Changes to these constants must be duplicated in the PL/SQL
   // package API_PROP_NAME_PKG.
   //--------------------------------------------------------------------------
   public static final String SUBJECT_LEFT_PROP       = "subject_left";
   public static final String SUBJECT_RIGHT_PROP      = "subject_right";
   public static final String PHASE_PROP              = "phase";
}
