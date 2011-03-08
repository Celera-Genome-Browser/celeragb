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
package api.facade.concrete_facade.ejb;

import api.entity_model.model.fundtype.EntityType;
import api.facade.abstract_facade.annotations.AtalantaDetailFacade;
import api.facade.abstract_facade.annotations.AtalantaHitFacade;
import api.facade.abstract_facade.annotations.ESTMapperDetailFacade;
import api.facade.abstract_facade.annotations.ESTMapperHitFacade;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.facade.abstract_facade.genetics.GenomeVersionLoader;
import api.facade.facade_mgr.ConnectionStatus;
import api.facade.facade_mgr.FacadeManager;
import api.stub.LoginProperties;
import api.stub.data.FatalCommError;
import api.stub.data.SystemError;
import shared.util.PropertyConfigurator;

import javax.ejb.EJBHome;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public abstract class AbstractEJBFacadeManager extends api.facade.facade_mgr.FacadeManagerBase {
   public static final int SERVER_SELECTION_MODE_DATASOURCES = 0;
   public static final int SERVER_SELECTION_MODE_ASSEMBLIES = 1;
   public static final int SERVER_SELECTION_MODE_NONE = 2;
   private static Class[] createMethodArgumentsClass = new Class[0];
   private static final String createMethodName = "create";
   private static Object[] createMethodArgumentsList = new Object[0];
   private static ResourceBundle componentResourceBundle;
   protected static ResourceBundle serverResourceBundle;
   private static int EJB_RETRIES = 3;

   static {
      try {
         //String propsFile = System.getProperty("x.genomebrowser.ServerConnectionProperties");
         String propsFile = PropertyConfigurator.getProperties().getProperty("x.genomebrowser.ServerConnectionProperties");
         serverResourceBundle = ResourceBundle.getBundle(propsFile);

         String rbName = PropertyConfigurator.getProperties().getProperty("x.genomebrowser.ServerComponentsProperties", "");
         componentResourceBundle = ResourceBundle.getBundle(rbName);
      }
      catch (java.util.MissingResourceException mre) {
         FacadeManager.handleException(mre);
      }
   }

   private boolean isDataSourceSet = false;
   private Map remoteInterfacePools = new HashMap();
   private Map adapters = new HashMap();
   private int defaultMaxInterfaces = 10;

   //the default maximum number of interfaces the client will allocate
   private int defaultMinInterfaces = 1;

   //the default minimum number of interfaces the client will allocate
   private String serverHttpURL = null;
   private String serverJndiURL = null;
   private Context initialContext;
   private String username;
   private String password;

   public AbstractEJBFacadeManager() {
      initialize();
   } //only instantiated by FacadeManager.

   public Object[] getOpenDataSources() {
      return new Object[] { System.getProperty("x.genomebrowser.ApplicationServer")};
   }

   /**
    * This method will be called if the previous one returns true.
    */
   public ConnectionStatus initiateConnection() {
      try {
         getInitialContextHelper();
         // KLUDGE!
         // This call is here in order to force authentication to occur
         // at this point. Retrieving an InitialContext does not
         // cause authentication to occur. Performing a lookup does!!!
         buildRemoteInterfacePoolFor(api.facade.abstract_facade.genetics.SpeciesLoader.class, defaultMinInterfaces, defaultMaxInterfaces, getInitialContext(), createMethodArgumentsClass, createMethodArgumentsList, getDefaultSharingOfInterfacesAllowed());

      }
      catch (AuthenticationException aEx) {
         if ((getUsername() == null) || getUsername().equals("")) {
            return CONNECTION_STATUS_NO_CREDENTIALS;
         }
         return CONNECTION_STATUS_BAD_CREDENTIALS;
      }
      catch (SecurityException secEx) {
         if ((getUsername() == null) || getUsername().equals("")) {
            return CONNECTION_STATUS_NO_CREDENTIALS;
         }
         return CONNECTION_STATUS_BAD_CREDENTIALS;
      }
      catch (Exception ex) {
         return new ConnectionStatus(ex.getMessage(), true);
      }

      return CONNECTION_STATUS_OK;
   }

   public String getDataSourceSelectorClass() {
      return "client.gui.other.data_source_selectors.GenomeVersionSelector";
   } //note class must implement DataSourceSelector

   public void prepareForSystemExit() {
      Collection pools = remoteInterfacePools.values();
      RemoteInterfacePool[] poolArray = (RemoteInterfacePool[])pools.toArray(new RemoteInterfacePool[0]);

      for (int i = 0; i < poolArray.length; i++) {
         poolArray[i].removeAllInterfaces();
      }

      shutdown();
   }

   public boolean canAddMoreDataSources() {
      return !isDataSourceSet;
   }

   public int getServerSelectionMode() {
      String mode = null;

      try {
         mode = serverResourceBundle.getString("ServerSelectionMode");
      }
      catch (MissingResourceException mrEx) {
         return this.SERVER_SELECTION_MODE_ASSEMBLIES;
      }

      if (mode.equalsIgnoreCase("Datasources")) {
         return SERVER_SELECTION_MODE_DATASOURCES;
      }

      if (mode.equalsIgnoreCase("None")) {
         return SERVER_SELECTION_MODE_NONE;
      }

      return this.SERVER_SELECTION_MODE_ASSEMBLIES;
   }

   public String getServerName() {
      return serverJndiURL;
   }

   public api.facade.abstract_facade.annotations.FeatureFacade getFacade(EntityType featureType) throws Exception {
      switch (featureType.value()) {
         case EntityTypeConstants.BlastN_Hit :
         case EntityTypeConstants.BlastX_Hit :
         case EntityTypeConstants.tBlastN :
         case EntityTypeConstants.tBlastX :
            return (this.getBlastHitFacade());

         case EntityTypeConstants.High_Scoring_Pair :
         case EntityTypeConstants.Sim4_Feature_Detail :
         case EntityTypeConstants.Genewise_Peptide_Hit_Part :
            return (this.getHitAlignmentDetailLoader());

         case EntityTypeConstants.Atalanta_Hit :
            return (this.getAtalantaHitFacade());

         case EntityTypeConstants.Atalanta_Feature_Detail :
            return (this.getAtalantaDetailFacade());
            
         case EntityTypeConstants.ESTMapper_Hit :
            return (this.getESTMapperHitFacade());
            
         case EntityTypeConstants.ESTMapper_Feature_Detail :
            return (this.getESTMapperDetailFacade());

         case EntityTypeConstants.Sim4_Hit :
         case EntityTypeConstants.Genewise_Peptide_Hit :
            return (this.getHitAlignmentFacade());

         case EntityTypeConstants.NonPublic_Gene :
            return (this.getGeneFacade());

         case EntityTypeConstants.NonPublic_Transcript :
            return (this.getTranscriptFacade());

         case EntityTypeConstants.Exon :
            return (this.getExonFacade());

         default :
            return (this.getFeatureFacade());
      }
   }

   public api.facade.abstract_facade.genetics.SpeciesLoader getSpecies() {
      if (adapters.containsKey(api.facade.abstract_facade.genetics.SpeciesLoader.class)) {
         return (api.facade.abstract_facade.genetics.SpeciesLoader)adapters.get(api.facade.abstract_facade.genetics.SpeciesLoader.class);
      }

      RemoteInterfacePool aPool = getInterfacePoolFor(api.facade.abstract_facade.genetics.SpeciesLoader.class);
      api.facade.abstract_facade.genetics.SpeciesLoader species;
      species = new api.facade.concrete_facade.ejb.EJBSpeciesAdapter(aPool, EJB_RETRIES);
      adapters.put(api.facade.abstract_facade.genetics.SpeciesLoader.class, species);

      return species;
   }

   public api.facade.abstract_facade.assembly.ContigFacade getContigFacade() throws Exception {
      if (adapters.containsKey(api.facade.abstract_facade.assembly.ContigFacade.class)) {
         return (api.facade.abstract_facade.assembly.ContigFacade)adapters.get(api.facade.abstract_facade.assembly.ContigFacade.class);
      }

      RemoteInterfacePool aPool = getInterfacePoolFor(api.facade.abstract_facade.assembly.ContigFacade.class, 2, defaultMaxInterfaces, getDefaultSharingOfInterfacesAllowed());
      api.facade.abstract_facade.assembly.ContigFacade contigFacade;
      contigFacade = new api.facade.concrete_facade.ejb.EJBContigFacadeAdapter(aPool, EJB_RETRIES);
      adapters.put(api.facade.abstract_facade.assembly.ContigFacade.class, contigFacade);

      return contigFacade;
   }

   public api.facade.abstract_facade.genetics.ControlledVocabService getControlledVocabService() throws Exception {
      if (adapters.containsKey(api.facade.abstract_facade.genetics.ControlledVocabService.class)) {
         return (api.facade.abstract_facade.genetics.ControlledVocabService)adapters.get(api.facade.abstract_facade.genetics.ControlledVocabService.class);
      }

      RemoteInterfacePool aPool = getInterfacePoolFor(api.facade.abstract_facade.genetics.ControlledVocabService.class);
      api.facade.abstract_facade.genetics.ControlledVocabService controlledVocabService;
      controlledVocabService = new api.facade.concrete_facade.ejb.EJBControlledVocabServiceAdapter(aPool, EJB_RETRIES);
      adapters.put(api.facade.abstract_facade.genetics.ControlledVocabService.class, controlledVocabService);

      return controlledVocabService;
   }

   private api.facade.abstract_facade.annotations.FeatureFacade getFeatureFacade() throws Exception {
      if (adapters.containsKey(api.facade.abstract_facade.annotations.FeatureFacade.class)) {
         return (api.facade.abstract_facade.annotations.FeatureFacade)adapters.get(api.facade.abstract_facade.annotations.FeatureFacade.class);
      }

      RemoteInterfacePool aPool = getInterfacePoolFor(api.facade.abstract_facade.annotations.FeatureFacade.class);
      api.facade.abstract_facade.annotations.FeatureFacade featFacade;
      featFacade = new api.facade.concrete_facade.ejb.EJBFeatureFacadeAdapter(aPool, EJB_RETRIES);
      adapters.put(api.facade.abstract_facade.annotations.FeatureFacade.class, featFacade);

      return featFacade;
   }

   private api.facade.abstract_facade.annotations.HitAlignmentDetailLoader getHitAlignmentDetailLoader() throws Exception {
      if (adapters.containsKey(api.facade.abstract_facade.annotations.HitAlignmentDetailLoader.class)) {
         return (api.facade.abstract_facade.annotations.HitAlignmentDetailLoader)adapters.get(api.facade.abstract_facade.annotations.HitAlignmentDetailLoader.class);
      }

      RemoteInterfacePool aPool = getInterfacePoolFor(api.facade.abstract_facade.annotations.HitAlignmentDetailLoader.class);
      api.facade.abstract_facade.annotations.HitAlignmentDetailLoader hitAlignmentDetailLoader;
      hitAlignmentDetailLoader = new api.facade.concrete_facade.ejb.EJBHitAlignmentDetailLoaderAdapter(aPool, EJB_RETRIES);
      adapters.put(api.facade.abstract_facade.annotations.HitAlignmentDetailLoader.class, hitAlignmentDetailLoader);

      return hitAlignmentDetailLoader;
   }

   private api.facade.abstract_facade.annotations.GeneFacade getGeneFacade() throws Exception {
      if (adapters.containsKey(api.facade.abstract_facade.annotations.GeneFacade.class)) {
         return (api.facade.abstract_facade.annotations.GeneFacade)adapters.get(api.facade.abstract_facade.annotations.GeneFacade.class);
      }

      RemoteInterfacePool aPool = getInterfacePoolFor(api.facade.abstract_facade.annotations.GeneFacade.class);
      api.facade.abstract_facade.annotations.GeneFacade geneFacade;
      geneFacade = new api.facade.concrete_facade.ejb.EJBGeneFacadeAdapter(aPool, EJB_RETRIES);
      adapters.put(api.facade.abstract_facade.annotations.GeneFacade.class, geneFacade);

      return geneFacade;
   }

   private AtalantaHitFacade getAtalantaHitFacade() throws Exception {
      if (adapters.containsKey(AtalantaHitFacade.class)) {
         return (AtalantaHitFacade)adapters.get(AtalantaHitFacade.class);
      }
      RemoteInterfacePool aPool = getInterfacePoolFor(AtalantaHitFacade.class);
      AtalantaHitFacade atalantaHitFacade = new EJBAtalantaHitFacadeAdapter(aPool, EJB_RETRIES);
      adapters.put(AtalantaHitFacade.class, atalantaHitFacade);
      return (atalantaHitFacade);
   }

   private AtalantaDetailFacade getAtalantaDetailFacade() throws Exception {
      if (adapters.containsKey(AtalantaDetailFacade.class)) {
         return (AtalantaDetailFacade)adapters.get(AtalantaDetailFacade.class);
      }

      RemoteInterfacePool aPool = getInterfacePoolFor(AtalantaDetailFacade.class);
      AtalantaDetailFacade atalantaDetailFacade;
      atalantaDetailFacade = new EJBAtalantaDetailFacadeAdapter(aPool, EJB_RETRIES);
      adapters.put(AtalantaDetailFacade.class, atalantaDetailFacade);
      return (atalantaDetailFacade);
   }
   
   private api.facade.abstract_facade.annotations.BlastHitFacade getBlastHitFacade() throws Exception {
      if (adapters.containsKey(api.facade.abstract_facade.annotations.BlastHitFacade.class)) {
         return (api.facade.abstract_facade.annotations.BlastHitFacade)adapters.get(api.facade.abstract_facade.annotations.BlastHitFacade.class);
      }

      RemoteInterfacePool aPool = getInterfacePoolFor(api.facade.abstract_facade.annotations.BlastHitFacade.class);
      api.facade.abstract_facade.annotations.BlastHitFacade blastNHitFacade;
      blastNHitFacade = new api.facade.concrete_facade.ejb.EJBBlastHitFacadeAdapter(aPool, EJB_RETRIES);
      adapters.put(api.facade.abstract_facade.annotations.BlastHitFacade.class, blastNHitFacade);

      return blastNHitFacade;
   }

   private ESTMapperHitFacade getESTMapperHitFacade() throws Exception {
      if (adapters.containsKey(ESTMapperHitFacade.class)) {
         return ((ESTMapperHitFacade)adapters.get(ESTMapperHitFacade.class));
      }
      RemoteInterfacePool aPool = getInterfacePoolFor(ESTMapperHitFacade.class);
      ESTMapperHitFacade estmapperHitFacade = new EJBESTMapperHitFacadeAdapter(aPool, EJB_RETRIES);
      adapters.put(ESTMapperHitFacade.class, estmapperHitFacade);
      return (estmapperHitFacade);
   }

   private ESTMapperDetailFacade getESTMapperDetailFacade() throws Exception {
      if (adapters.containsKey(ESTMapperDetailFacade.class)) {
         return ((ESTMapperDetailFacade)adapters.get(ESTMapperDetailFacade.class));
      }
      RemoteInterfacePool aPool = getInterfacePoolFor(ESTMapperDetailFacade.class);
      ESTMapperDetailFacade estmapperDetailFacade = new EJBESTMapperDetailFacadeAdapter(aPool, EJB_RETRIES);
      adapters.put(ESTMapperDetailFacade.class, estmapperDetailFacade);
      return (estmapperDetailFacade);
   }

   private api.facade.abstract_facade.annotations.HitAlignmentFacade getHitAlignmentFacade() throws Exception {
      if (adapters.containsKey(api.facade.abstract_facade.annotations.HitAlignmentFacade.class)) {
         return (api.facade.abstract_facade.annotations.HitAlignmentFacade)adapters.get(api.facade.abstract_facade.annotations.HitAlignmentFacade.class);
      }

      RemoteInterfacePool aPool = getInterfacePoolFor(api.facade.abstract_facade.annotations.HitAlignmentFacade.class);
      api.facade.abstract_facade.annotations.HitAlignmentFacade hitAlignmentFacade;
      hitAlignmentFacade = new api.facade.concrete_facade.ejb.EJBHitAlignmentFacadeAdapter(aPool, EJB_RETRIES);
      adapters.put(api.facade.abstract_facade.annotations.HitAlignmentFacade.class, hitAlignmentFacade);

      return hitAlignmentFacade;
   }

   private api.facade.abstract_facade.annotations.ExonFacade getExonFacade() throws Exception {
      if (adapters.containsKey(api.facade.abstract_facade.annotations.ExonFacade.class)) {
         return (api.facade.abstract_facade.annotations.ExonFacade)adapters.get(api.facade.abstract_facade.annotations.ExonFacade.class);
      }

      RemoteInterfacePool aPool = getInterfacePoolFor(api.facade.abstract_facade.annotations.ExonFacade.class);
      api.facade.abstract_facade.annotations.ExonFacade exonFacade;
      exonFacade = new api.facade.concrete_facade.ejb.EJBExonFacadeAdapter(aPool, EJB_RETRIES);
      adapters.put(api.facade.abstract_facade.annotations.ExonFacade.class, exonFacade);

      return exonFacade;
   }

   private api.facade.abstract_facade.annotations.TranscriptFacade getTranscriptFacade() throws Exception {
      if (adapters.containsKey(api.facade.abstract_facade.annotations.TranscriptFacade.class)) {
         return (api.facade.abstract_facade.annotations.TranscriptFacade)adapters.get(api.facade.abstract_facade.annotations.TranscriptFacade.class);
      }

      RemoteInterfacePool aPool = getInterfacePoolFor(api.facade.abstract_facade.annotations.TranscriptFacade.class);
      api.facade.abstract_facade.annotations.TranscriptFacade transcriptFacade;
      transcriptFacade = new api.facade.concrete_facade.ejb.EJBTranscriptFacadeAdapter(aPool, EJB_RETRIES);
      adapters.put(api.facade.abstract_facade.annotations.TranscriptFacade.class, transcriptFacade);

      return transcriptFacade;
   }

   /**
     * Returns feature index library, which can generate indexes for
     * new curations. This version IS guarenteed to return unique
     * indexes over time.
     * NOTE: Only facade managers that have the capability to provide
     * unique indexes over time will override this method. By defualt
     * a call to this method will result in a runtime e
     */
   public api.facade.abstract_facade.annotations.FeatureIndexLibrary getGloballUniqueIndexLibraryFacade() {
      if (adapters.containsKey(api.facade.abstract_facade.annotations.FeatureIndexLibrary.class)) {
         return (api.facade.abstract_facade.annotations.FeatureIndexLibrary)adapters.get(api.facade.abstract_facade.annotations.FeatureIndexLibrary.class);
      }

      RemoteInterfacePool aPool = getInterfacePoolFor(api.facade.abstract_facade.annotations.FeatureIndexLibrary.class);
      api.facade.abstract_facade.annotations.FeatureIndexLibrary featLibraryFacade;
      featLibraryFacade = new api.facade.concrete_facade.ejb.EJBFeatureIndexLibraryAdapter(aPool, EJB_RETRIES);
      adapters.put(api.facade.abstract_facade.annotations.FeatureIndexLibrary.class, featLibraryFacade);

      return featLibraryFacade;
   } // End method: getGloballUniqueIndexLibraryFacade

   public GenomeVersionLoader getGenomeVersion() throws Exception {
      if (adapters.containsKey(GenomeVersionLoader.class)) {
         return (GenomeVersionLoader)adapters.get(GenomeVersionLoader.class);
      }

      RemoteInterfacePool aPool = getInterfacePoolFor(GenomeVersionLoader.class);
      GenomeVersionLoader cGenomeVersion;
      cGenomeVersion = new api.facade.concrete_facade.ejb.EJBGenomeVersionAdapter(aPool, EJB_RETRIES);
      adapters.put(GenomeVersionLoader.class, cGenomeVersion);

      return cGenomeVersion;
   }

   public api.facade.abstract_facade.assembly.GenomicAxisLoader getGenomicAxis() throws Exception {
      if (adapters.containsKey(api.facade.abstract_facade.assembly.GenomicAxisLoader.class)) {
         return (api.facade.abstract_facade.assembly.GenomicAxisLoader)adapters.get(api.facade.abstract_facade.assembly.GenomicAxisLoader.class);
      }

      RemoteInterfacePool aPool = getInterfacePoolFor(api.facade.abstract_facade.assembly.GenomicAxisLoader.class, 5, defaultMaxInterfaces, getDefaultSharingOfInterfacesAllowed());
      api.facade.abstract_facade.assembly.GenomicAxisLoader cGenomicAxis;
      cGenomicAxis = new api.facade.concrete_facade.ejb.EJBGenomicAxisAdapter(aPool, EJB_RETRIES);
      adapters.put(api.facade.abstract_facade.assembly.GenomicAxisLoader.class, cGenomicAxis);

      return cGenomicAxis;
   }

   public api.facade.abstract_facade.genetics.ChromosomeLoader getChromosome() throws Exception {
      if (adapters.containsKey(api.facade.abstract_facade.genetics.ChromosomeLoader.class)) {
         return (api.facade.abstract_facade.genetics.ChromosomeLoader)adapters.get(api.facade.abstract_facade.genetics.ChromosomeLoader.class);
      }

      RemoteInterfacePool aPool = getInterfacePoolFor(api.facade.abstract_facade.genetics.ChromosomeLoader.class, 2, defaultMaxInterfaces, getDefaultSharingOfInterfacesAllowed());
      api.facade.abstract_facade.genetics.ChromosomeLoader cChromosome;
      cChromosome = new api.facade.concrete_facade.ejb.EJBChromosomeAdapter(aPool, EJB_RETRIES);
      adapters.put(api.facade.abstract_facade.genetics.ChromosomeLoader.class, cChromosome);

      return cChromosome;
   }

   public api.facade.abstract_facade.genetics.GenomeLocatorFacade getGenomeLocator() throws Exception {
      if (adapters.containsKey(api.facade.abstract_facade.genetics.GenomeLocatorFacade.class)) {
         return (api.facade.abstract_facade.genetics.GenomeLocatorFacade)adapters.get(api.facade.abstract_facade.genetics.GenomeLocatorFacade.class);
      }

      RemoteInterfacePool aPool = getInterfacePoolFor(api.facade.abstract_facade.genetics.GenomeLocatorFacade.class);
      api.facade.abstract_facade.genetics.GenomeLocatorFacade genomeLocator;
      genomeLocator = new api.facade.concrete_facade.ejb.EJBGenomeLocatorAdapter(aPool, EJB_RETRIES);
      adapters.put(api.facade.abstract_facade.genetics.GenomeLocatorFacade.class, genomeLocator);

      return genomeLocator;
   }

   private String getJNDIHomeInterfaceName(Class interfaceClass) {
      String valueString = null;

      try {
         valueString = componentResourceBundle.getString(interfaceClass.getName());
      }
      catch (MissingResourceException ex) {
         System.out.println("EJBFacadeManager: Warning!! Potential error - undefined resource string requested, " + interfaceClass.getName());
      }

      return valueString;
   }

   private RemoteInterfacePool getInterfacePoolFor(Class componentInterfaceClass) {
      return getInterfacePoolFor(componentInterfaceClass, defaultMinInterfaces, defaultMaxInterfaces, getDefaultSharingOfInterfacesAllowed());
   }

   private RemoteInterfacePool getInterfacePoolFor(Class componentInterfaceClass, int minInterfaces, int maxInterfaces, boolean allowSharingOfInterfaces) {
      return getInterfacePoolFor(componentInterfaceClass, minInterfaces, maxInterfaces, getInitialContext(), allowSharingOfInterfaces);
   }

   private RemoteInterfacePool getInterfacePoolFor(Class componentInterfaceClass, int minInterfaces, int maxInterfaces, Context context, boolean allowSharingOfInterfaces) {
      return getInterfacePoolFor(componentInterfaceClass, minInterfaces, maxInterfaces, context, createMethodArgumentsClass, createMethodArgumentsList, allowSharingOfInterfaces);
   }

   private RemoteInterfacePool getInterfacePoolFor(Class componentInterfaceClass, int minInterfaces, int maxInterfaces, Context context, Class[] createArgTypes, Object[] createArgs, boolean allowSharingOfInterfaces) {
      RemoteInterfacePool aPool;
      synchronized (this) {
         aPool = (RemoteInterfacePool)remoteInterfacePools.get(componentInterfaceClass);

         if (aPool == null) {
            try {
               buildRemoteInterfacePoolFor(componentInterfaceClass, minInterfaces, maxInterfaces, context, createArgTypes, createArgs, allowSharingOfInterfaces);
               aPool = (RemoteInterfacePool)remoteInterfacePools.get(componentInterfaceClass);
            }
            catch (AuthenticationException authEx) {
               throw new FatalCommError(authEx.getMessage());
            }
         }
      }

      return aPool;
   }

   private void buildRemoteInterfacePoolFor(Class componentInterfaceClass, int minInterfaces, int maxInterfaces, Context context, Class[] createArgTypes, Object[] createArgs, boolean allowSharingOfInterfaces) throws AuthenticationException {
      try {
         EJBHome proxy = (EJBHome)context.lookup(getJNDIHomeInterfaceName(componentInterfaceClass));
         Method createMethod = proxy.getClass().getMethod(createMethodName, createArgTypes);
         RemoteInterfacePool aPool = new RemoteInterfacePool(componentInterfaceClass, proxy, createMethod, createArgs, minInterfaces, maxInterfaces, allowSharingOfInterfaces);

         remoteInterfacePools.put(componentInterfaceClass, aPool);
      }
      catch (Exception se) {
         System.out.println("Received exception while contacting the Application Server for Information Service:\n" + se.toString() + ". Remote Object Name: " + getJNDIHomeInterfaceName(componentInterfaceClass));
         if (se instanceof AuthenticationException) {
            throw (AuthenticationException)se;
         }
         else if (se instanceof java.rmi.RemoteException || se instanceof javax.ejb.CreateException || se instanceof javax.naming.NamingException || se instanceof IllegalAccessException || se instanceof IllegalArgumentException || se instanceof InvocationTargetException || se instanceof NoSuchMethodException || se instanceof SecurityException) {
            throw new FatalCommError(se.getMessage());
         }
         else {
            System.out.println(se.getMessage());
            throw new SystemError(se.getMessage());
         }
      }
   }

   //  abstract InitialContext getEJBContextColocatedService();
   protected abstract boolean getDefaultSharingOfInterfacesAllowed();

   protected abstract Context getEJBContextRemoteService(String url, String userId, String password) throws Exception;

   //   public void setServerConnection(ServerConnection[] servCons) {
   //      Vector priorities = new Vector(servCons.length);
   //      for (int i = 0; i < servCons.length; i++) {
   //         serverUrlList.add(
   //            new UrlContainer(
   //               hostPortToUrl(servCons[i].getHost(), servCons[i].getPort(), servCons[i].getUseSSL()),
   //               servCons[i].getApplicationServerName(),
   //               servCons[i].getUseSSL()));
   //         priorities.add("1");
   //      }
   //      //shuffleURLs(serverUrlList, priorities);
   //   }
   public Context getInitialContext() {
      try {
         return getInitialContextHelper();
      }
      catch (Exception ex) {
         FacadeManager.handleException(ex);
      }

      return null;
   }

   /**
    * private method that will not trap the exceptions for initialConnection;
    */
   private Context getInitialContextHelper() throws SecurityException, NamingException, AuthenticationException {
      if (initialContext != null) {
         return initialContext;
      }

      String colocatedString = serverResourceBundle.getString("BeansAreColocated");

      if (colocatedString.equals("1")) {
         // JB: Try passing an empty hashtable to see if it forces
         // intercomponent calls to use the calling principle rather
         // than guest which is failing security check for visible
         // assemblies is some cases (eg Interactive Blast)
         initialContext = new InitialContext(new Hashtable());

         if (initialContext == null) {
            throw new javax.naming.NamingException("getInitialContext returned null");
         }

         return initialContext;
      }

      buildServerUrlList();
      System.out.println("Using URL to connect to JNDI Naming Service for Genomic Service: " + serverJndiURL);

      //props.put(Context.PROVIDER_URL, ((UrlContainer)serverUrlList.elementAt(0)).getUrl());
      try {
         initialContext = getEJBContextRemoteService(serverJndiURL, getUsername(), getPassword());

         if (initialContext == null) {
            throw new javax.naming.NamingException("getInitialContext returned null");
         }
      }
      catch (AuthenticationException authEx) {
         throw authEx;
      }
      catch (SecurityException secEx) {
         throw secEx;
      }
      catch (Exception ex) {
         System.out.println("Client had difficulty contacting the JNDI Naming Service for Genomic Service.");
         throw new FatalCommError(serverJndiURL, ex.getMessage());
      }

      if ((serverJndiURL != null) && (serverHttpURL != null)) {
         System.setProperty("x.genomebrowser.ApplicationServer", serverJndiURL);
         System.setProperty("x.genomebrowser.HttpServer", serverHttpURL);

         //PLEASE DO NOT CHANGE THIS.  THE CLIENT DOES NOT USE THE PropertyConfigurator.  THIS CODE IS NOT
         //EXECUTED IN THE SERVER AND CHANGING THIS BREAKS THE CLIENT!!!
      }

      return initialContext;
   }

   private String getUsername() {
      if (username != null) {
         return username;
      }

      username = PropertyConfigurator.getProperties().getProperty(LoginProperties.SERVER_LOGIN_NAME);

      return username;
   }

   private String getPassword() {
      if (password != null) {
         return password;
      }

      password = PropertyConfigurator.getProperties().getProperty(LoginProperties.SERVER_LOGIN_PASSWORD);

      return password;
   }

   private void buildServerUrlList() {
      try {
         serverJndiURL = serverResourceBundle.getString("JndiServer");
         serverHttpURL = serverResourceBundle.getString("HttpServer");
      }
      catch (java.util.MissingResourceException mre) {
         FacadeManager.handleException(mre);
      }
   }

   protected abstract void initialize();

   protected abstract void shutdown();

   Properties getSecureDefaultProperties(String username, String password) {
      Properties props = new Properties();
      props.put(Context.SECURITY_PRINCIPAL, username);
      props.put(Context.SECURITY_CREDENTIALS, password);

      return props;
   }
}