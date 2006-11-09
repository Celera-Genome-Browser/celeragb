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
package api.entity_model.model.genetics;


import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.management.ModelMgr;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.AlignmentNotAllowedException;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.EntityTypeSet;
import api.entity_model.model.fundtype.LoadFilter;
import api.entity_model.model.fundtype.LoadFilterStatus;
import api.entity_model.model.fundtype.LoadRequest;
import api.facade.abstract_facade.fundtype.GenomicEntityLoader;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.OID;
import shared.tools.computation.StatisticalModel;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author       Peter Davies
 * @version $Id$
 */



public class Species extends Axis {

   private LoadFilter chromosomeLoadFilter;
//  Species image properties:
    private static ResourceBundle speciesImagesResourceBundle;
    private static final String IMAGE_DIR = "/resource/client/images/";
    private Properties imageList = null;
//  Splice site probability properties:
    private ResourceBundle speciesSpliceProfilesResourceBundle;
    private Properties spliceProfileSettings = null;
    private Map spliceStatisticalModels = null;
    private boolean overridingSpliceProfiles = false;

//****************************************
//*  Public methods
//****************************************

//*  Construction
  public Species(OID oid, String displayName)
  {
    this(oid, displayName, 0);
  }

/**
 * @level developer
 */
  public Species( OID oid, String displayName, int magnitude)
  {
    this(oid, displayName, magnitude, null);
  }

/**
 * @level developer
 */
  public Species(OID oid, String displayName, int magnitude, FacadeManagerBase overrideDataLoader)
  {
    super(EntityType.getEntityTypeForName("_SPECIES"),oid, displayName, magnitude, overrideDataLoader);
  }


   /**
    * Get the genome version the regular way...
    */
   public GenomeVersion getGenomeVersion() {
      return ModelMgr.getModelMgr().getGenomeVersionContaining(this);
   }


    /**
     * Accepts visitors that implement GenomicEntityVisitor. acceptVisitorForSelf
     * is specialized by every subclass of GenomicEntity to call the appropriate
     * "visit...(...)" function on the passed visitor.
     * @param theVisitor the visitor.
     * @level developer
     */
    public void acceptVisitorForSelf(GenomicEntityVisitor theVisitor) {
        try {
                theVisitor.visitSpecies(this);
        }
        catch (Exception ex) {
                handleException(ex);
        }
    }

/**
 * @level developer
 */
    public Set getDefaultLoadFilters() {
       Set set= new HashSet(1);
       set.add(chromosomeLoadFilter);
       return set;
    }

/**
 * @level developer
 */
    public LoadFilter getChromosomeLoadFilter() {
       if (chromosomeLoadFilter==null) {
          chromosomeLoadFilter=new LoadFilter("Chromosomes on "+
            toString(),
            new EntityTypeSet
              (
                new EntityType[]{EntityType.getEntityTypeForName("_CHROMOSOME")}
              ));
       }
       return chromosomeLoadFilter;
    }

/**
 * @level developer
 */
    public LoadRequest getChromosomeLoadRequest() {
       return new LoadRequest(getChromosomeLoadFilter());
    }

    /** Returns path/filename corresponding to "keyString" -- species or chromosome name */
    public String getImageFilename(String keyString) {
        if (imageList==null) loadSpeciesImageProperties();
        if (imageList != null) {
            return (IMAGE_DIR + (String)imageList.get(keyString));
        }
        else {
            return (null);
        }
    }  // end public String getImageFilename()

    /**
     * This method is used to override the default splice profiles.
     */
    public void overrideDefaultSpliceProfiles(File acceptorFile, File donorFile, File neitherFile) {
      overridingSpliceProfiles=true;
      //System.out.println("Loading in the Settings.");
      try {
        spliceStatisticalModels = new HashMap();
        spliceStatisticalModels.put(StatisticalModel.ACCEPTOR,
          new StatisticalModel(acceptorFile));
        spliceStatisticalModels.put(StatisticalModel.DONOR,
          new StatisticalModel(donorFile));
        spliceStatisticalModels.put(StatisticalModel.NEITHER,
          new StatisticalModel(neitherFile));
      }
      catch(Exception e) {
          try {
              api.entity_model.management.ModelMgr.getModelMgr().handleException(e);
          }
          catch(Exception ex) {
              ex.printStackTrace();
          }
      }
    }

    /** Returns the species' StatisticalModel corresponding to splice model string -- donor/acceptor/neither */
    public Map getSpliceTypeStatisticalModels() {
        if (spliceProfileSettings==null && !overridingSpliceProfiles) loadDefaultSpliceProperties();
        return spliceStatisticalModels;
    }  // end public StatisticalModel getSpliceTypeStatisticalModel(String spliceType)


//****************************************
//*  Protected methods
//****************************************
    // REVISIT: NEed to move this back to protected (I think?) for now
    // so I can get stuff to compile and check it in make public JB 1-26-2000
//    protected Species () {
   /*public  Species () {
    }*/

    protected GenomicEntityMutator constructMyMutator(){
       return new SpeciesMutator();
    }

    protected int getPredictedNumberOfObservers(){
      return 1;
    }

    protected int getPredictedNumberOfAlignmentsToEntities(){
      return 25;
    }

    protected void willAcceptAlignmentToAxis(Alignment alignmentToAxis)
       throws AlignmentNotAllowedException {
       throw new AlignmentNotAllowedException(
          "Species cannot be aligned to any axis",alignmentToAxis);
    }

    protected void willAcceptAlignmentToEntity(Alignment alignmentToEntity)
       throws AlignmentNotAllowedException{
       super.willAcceptAlignmentToEntity(alignmentToEntity);

       if (!(alignmentToEntity.getEntity() instanceof Chromosome &&
           alignmentToEntity.getClass()==Alignment.class))
       throw new AlignmentNotAllowedException(
       "Species can only accept Chromosomes with non-geometric alignments as "+
        "aligned entities",alignmentToEntity);
    }

    protected boolean willAcceptLoadRequestForAlignedEntities(LoadRequest loadRequest){
       if (loadRequest.isRangeRequest() || loadRequest.isBinRequest() ||
           !(loadRequest.getLoadFilter().getLoadFilterStatus().getClass()==LoadFilterStatus.class))
           return false;
       return true;
    }

    protected GenomicEntityLoader getDataLoader() {
      try {
       return getLoaderManager().getSpecies();
      }
      catch (Exception ex) {
        handleException(ex);
        return null;
      }
    }

//****************************************
//*  Package methods
//****************************************

//****************************************
//*  Private methods
//****************************************

    /** Retrieves and statically stores the contents listed within a species' image property file */
    private void loadSpeciesImageProperties() {
        String species = null;
        // Get the species' image property file name
        try {
            speciesImagesResourceBundle = ResourceBundle.getBundle(System.getProperty("x.genomebrowser.SpeciesChromoImages"));
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            speciesImagesResourceBundle = null;
        }
        if (speciesImagesResourceBundle != null) {
            // Add new species test here, corresponding line should be in SpeciesImagesFileMap.properties
            if (this.toString().toLowerCase().startsWith("homo")) {
                species = "homo";
            }
            else if (this.toString().toLowerCase().startsWith("dros")) {
                species = "dros";
            }
            else  {
                System.out.println("Error: species.toString() returned unrecognized species name.");
            }
            // Get the species' image properties (list of image file names)
            if (species != null) {
                try {
                    imageList = new Properties();
                    InputStream fileIn = this.getClass().getResourceAsStream(speciesImagesResourceBundle.getString(species));
                    imageList.load(fileIn);
                    fileIn.close();
                }
                catch(Exception e) {
                    System.out.println(e.getMessage());
                    imageList = null;
                }
            }
        }
    }  // end private void loadSpeciesImageProperties()

    /**
     * Retrieves and statically stores the contents listed within a species'
     * splice probability property file.
     */
    public void loadDefaultSpliceProperties() {
      //System.out.println("Loading in the Default Settings.");
      overridingSpliceProfiles=false;
      String species = null;
      // Get the species' splice probability property file name:
      try {
          speciesSpliceProfilesResourceBundle = ResourceBundle.getBundle(System.getProperty("x.genomebrowser.SpeciesSpliceProfiles"));
      }
      catch(Exception e) {
          System.out.println(e.getMessage());
          speciesSpliceProfilesResourceBundle = null;
      }
      if (speciesSpliceProfilesResourceBundle != null) {
          // Add new species test here, corresponding line should be in SpeciesSpliceFileMap.properties
          if (this.toString().toLowerCase().startsWith("homo")) {
              species = "homo";
          }
          else if (this.toString().toLowerCase().startsWith("dros")) {
              species = "dros";
          }
          else  {
              // Default to human. Splice site probability percentages are close enough to other species.
              System.out.println("Error: species.toString() returned unrecognized species name. Defaulting to human.");
              species = "homo";
          }
          // Get the species' splice probability properties
          if (species != null) {
              try {
                  spliceStatisticalModels = new HashMap();
                  spliceProfileSettings = new Properties();
                  InputStream fileIn = this.getClass().getResourceAsStream(speciesSpliceProfilesResourceBundle.getString(species));
                  spliceProfileSettings.load(fileIn);
                  spliceStatisticalModels.put(StatisticalModel.ACCEPTOR,
                    new StatisticalModel((String)spliceProfileSettings.
                    get("AcceptorModel")));
                  spliceStatisticalModels.put(StatisticalModel.DONOR,
                    new StatisticalModel((String)spliceProfileSettings.
                    get("DonorModel")));
                  spliceStatisticalModels.put(StatisticalModel.NEITHER,
                    new StatisticalModel((String)spliceProfileSettings.
                    get("NeitherModel")));
                  fileIn.close();
              }
              catch(Exception e) {
                  try {
                      api.entity_model.management.ModelMgr.getModelMgr().handleException(e);
                  }
                  catch(Exception ex) {
                      ex.printStackTrace();
                  }
              }
          }
      }
    }  // end private void loadSpeciesSpliceProperties()


//****************************************
//*  Inner Classes
//****************************************

     public class SpeciesMutator extends AxisMutator{
        protected SpeciesMutator() {}

    }

}
