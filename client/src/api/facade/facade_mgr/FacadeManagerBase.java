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

package api.facade.facade_mgr;

import api.entity_model.model.fundtype.EntityType;
import api.facade.abstract_facade.annotations.FeatureFacade;
import api.facade.abstract_facade.annotations.FeatureIndexLibrary;
import api.facade.abstract_facade.assembly.ContigFacade;
import api.facade.abstract_facade.genetics.ControlledVocabService;
import api.facade.shared_facades.CurationFeatureIndexLibrary;

public abstract class FacadeManagerBase {

   public static final ConnectionStatus CONNECTION_STATUS_OK = new ConnectionStatus("Connection OK", false);
   public static final ConnectionStatus CONNECTION_STATUS_BAD_CREDENTIALS = new ConnectionStatus("Invalid Username/Password", true);
   public static final ConnectionStatus CONNECTION_STATUS_NO_CREDENTIALS = new ConnectionStatus("No defined Username/Password", false);
   public static final ConnectionStatus CONNECTION_STATUS_NO_DEFINED_INFORMATION_SERVICE =
      new ConnectionStatus("No Location for the Information Service was defined", false);
   public static final ConnectionStatus CONNECTION_STATUS_CANNOT_CONNECT = new ConnectionStatus("Cannot connect to Information Service", true);

   private FeatureIndexLibrary featureIndexLib = null;

   public abstract ContigFacade getContigFacade() throws Exception;

   public abstract ControlledVocabService getControlledVocabService() throws Exception;
   public abstract api.facade.abstract_facade.genetics.SpeciesLoader getSpecies() throws Exception;
   public abstract api.facade.abstract_facade.genetics.GenomeVersionLoader getGenomeVersion() throws Exception;
   public abstract api.facade.abstract_facade.genetics.ChromosomeLoader getChromosome() throws Exception;
   public abstract api.facade.abstract_facade.assembly.GenomicAxisLoader getGenomicAxis() throws Exception;
   public abstract api.facade.abstract_facade.genetics.GenomeLocatorFacade getGenomeLocator() throws Exception;

   public abstract String getDataSourceSelectorClass(); //note class must implement DataSourceSelector

   /**
    * This method will be called to initialize the FacadeManager instance.
    */
   public ConnectionStatus initiateConnection() {
      return CONNECTION_STATUS_OK;
   }

   public void prepareForSystemExit() {
   } //Override to receive system exit notification
   //     public void setGenomeVersion(api.entity_model.model.genetics.GenomeVersion genomeVersion){} //Override to receive notification of the GenomeVersion
   public boolean canAddMoreDataSources() {
      return false;
   } //indicates the ability for the protocol to accept more than 1 datasource

   public abstract String getServerName();

   public abstract FeatureFacade getFacade(EntityType featureType) throws Exception;

   /**
    * Return the open datasources for the facade. toString will be used to display the sources
    */
   public abstract Object[] getOpenDataSources();

   //     private static final int OBJECT_BD_GRTR_THAN_PARAM = 1;  // As used in BigDecimal.compareTo

   /**
    * Returns feature index library, which can generate indexes for
    * new curations.
    */
   public final FeatureIndexLibrary getFeatureIndexLibraryFacade() {

      if (featureIndexLib != null) {
         return featureIndexLib;
      }
      featureIndexLib = new CurationFeatureIndexLibrary();
      return featureIndexLib;
   } // End method: getFeatureIndexLibraryFacade

   /**
    * Returns feature index library, which can generate indexes for
    * new curations. This version IS guarenteed to return unique
    * indexes over time.
    * NOTE: Only facade managers that have the capability to provide
    * unique indexes over time will override this method. By defualt
    * a call to this method will result in a runtime e
    */
   public FeatureIndexLibrary getGloballUniqueIndexLibraryFacade() {
      throw new UnsupportedOperationException("This type of facade manager " + " does not support globally unique index generation");
   } // End method: getGloballUniqueIndexLibraryFacade

} // End class: FacadeManagerBase
