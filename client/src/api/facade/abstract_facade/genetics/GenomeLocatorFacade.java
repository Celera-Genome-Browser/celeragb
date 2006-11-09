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

/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
 *********************************************************************/

import api.entity_model.model.genetics.GenomeVersion;

/**
 * Remote interface for the GenomeLocator bean.
 *
 * @author James Baxendale <james.baxendale>
 * @version 1.0
 */
public interface GenomeLocatorFacade {
   /**
    * Get the collection of all GenomeVersions that are available to
    * be browsed by this user.
    */
   public GenomeVersion[] getAvailableGenomeVersions();

   /**
    * Finds the latest, in chronological terms, version of the
    * genome for the locators species. The species is supplied as part of the
    * create call on the home interface.
    *
    * @return GenomeVersion object that identifies the latest version of the
    * species genome.
    */
   public GenomeVersion latestGenomeForSpecies(String speciesName);

   /**
    * Returns the genome version that corresponds with the version number
    * supplied.
    *
    * @param versionNumber is a unique version identifier within the context
    * of a species.
    *
    * @return GenomeVersion object that identifies the requested version if
    * such a version exists. Throw a RemoteException if the requested version
    * does not exist.
    */
   public GenomeVersion getNthGenomeForSpecies(String speciesName, long versionNumber);
}
