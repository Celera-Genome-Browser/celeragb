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
package api.facade.abstract_facade.assembly;

import api.facade.abstract_facade.fundtype.GenomicEntityLoader;

public interface ContigFacade extends GenomicEntityLoader, AssemblyProduct {
   public static final String CONTIG_ID_PROP = "contig_id";
   public static final String DISPLAY_NAME_PROP = "display_name";
   public static final String GAPPED_LENGTH_PROP = "gapped_length";
   public static final String UNGAPPED_LENGTH_PROP = "ungapped_length";
}
