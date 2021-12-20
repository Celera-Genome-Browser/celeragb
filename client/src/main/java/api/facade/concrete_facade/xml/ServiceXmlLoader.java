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
/**
 * Title:        Data source or loader for XML service URL.
 * Description:  Given a designated URL, will return features aligning to axes
 *               found in URL queries.
 * Company:      []<p>
 * @author Les Foster
 * @version  CVS_ID:  $Id$
 */
package api.facade.concrete_facade.xml;

import api.entity_model.access.report.SubjectSequenceReport;
import api.entity_model.model.alignment.Alignment;
import api.facade.concrete_facade.shared.FeatureCriterion;
import api.facade.concrete_facade.shared.OIDParser;
import api.facade.concrete_facade.shared.feature_bean.CompoundFeatureBean;
import api.facade.concrete_facade.shared.feature_bean.FeatureBean;
import api.facade.concrete_facade.shared.feature_bean.SimpleFeatureBean;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.OID;
import api.stub.geometry.Range;
import api.stub.sequence.Sequence;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

/**
 *  This loader will parse data from a URL store the data that was retrieved
 *  from it in class variables for retrieval.  Data must be in GAME XML
 *  format.
 *
 *  Current assumptions:
 *     New Loader instance needs to be created for each XML URL registered.
 */
public class ServiceXmlLoader extends SAXLoaderBase {

  //------------------------------------CONSTANTS
  private final static String LOAD = "load";
  private final static String SEARCH = "search";
  private final static String REPORT = "report";
  private final static String ELABORATE = "elaborate";
  private final static String SEQUENCE = "sequence";

  private final static boolean debug = false;

  // These were all required to be supported prior to the initial use of
  // a supported actions request.
  private final static String[] DEFAULT_PROTOCOL_SUPPORT = { LOAD, SEARCH };

  //------------------------------------INSTANCE VARIABLES
  private String urlString;
  private String[] protocolSupport;     // What actions does the servlet support.

  //------------------------------------CONSTRUCTORS
  /**
   * No-args constructor prepares for full parse.
   */
  public ServiceXmlLoader() {
    super();
  } // End constructor

  //------------------------------------PUBLIC INTERFACE METHODS
  /** Sets or registers the URL string to address queries. */
  public void setURL(String urlString) {
    this.urlString = urlString;
    getProtocolSupport();
  } // End method: setURL

  /** Carry out a search for the given type of object with the given value. */
  public String[] searchFor(int searchType, String searchTarget, OID oidForGenomeVersionAndSpecies) {
    if (! protocolSupports(SEARCH)) {
      return new String[0];
    } // Test for protocol support level.

    if (urlString == null)
      throw new IllegalStateException("ERROR: cannot search from URL until URL is set");

    String finalURL = null;
    String[] responseArray = null;

    finalURL = new RequestFormatter(urlString).formatSearchFor(searchType, searchTarget, oidForGenomeVersionAndSpecies);

    // Now have a nicely formatted URL.
    //
    try {
      //System.out.println("Contacting "+finalURL);
      URL url = new URL(finalURL);
      BufferedReader urlReader = new BufferedReader(new InputStreamReader(url.openStream()));
      List responseList = new ArrayList();
      String nextResponse = null;
      while (null != (nextResponse = urlReader.readLine())) {
        responseList.add(nextResponse);
      } // For all lines of input.

      responseArray = new String[responseList.size()];
      responseList.toArray(responseArray);

    } // End try block
    catch (Exception ex) {
      FacadeManager.handleException(ex);
      return new String[0];
    } // End catch block for net communication.

    return responseArray;

  } // End method

  //------------------------------------INTERFACE METHOD OVERRIDES
  /** Returns array containing the url prefix string. */
  public String[] getLoadedFileNames(){
     return new String[]{urlString};
  } // End method

  /**
   *  Return either query or subject aligned residues for simple feature.
   *  May use what is already cached, or cache again.
   */
  public synchronized String getQueryAlignedResidues(OID oid) {
    // See below: protocol support test must be done after super has
    // been queried.
    if (debug)
       System.out.println("Asking for query alignmed residues");

    String returnString = super.getQueryAlignedResidues(oid);
    if (returnString == null) {
      SimpleFeatureBean model = (SimpleFeatureBean)getFeatureHandler().getOrLoadModelForOid(oid);
      if (model != null) {
        if (protocolSupports(ELABORATE)) {
          cacheAlignmentTextsForHierarchyOf(model);
          returnString = model.getQueryAlignment();
        } // Can/need to elaborate on the model.
      } // Got the model.
    } // Need to check remotely.
    return returnString;
  } // End method: getQueryAlignedResidues

  public synchronized String getSubjectAlignedResidues(OID oid) {
    // See below: protocol support test must be done after super has
    // been queried.
    if(debug)
      System.out.println("Asking for subject aligned residues");

    String returnString = super.getSubjectAlignedResidues(oid);
    if (returnString == null || returnString.length() == 0) {
      SimpleFeatureBean model = (SimpleFeatureBean)getFeatureHandler().getOrLoadModelForOid(oid);
      if (model != null) {
        if (protocolSupports(ELABORATE)) {
          cacheAlignmentTextsForHierarchyOf(model);
          returnString = model.getSubjectAlignment();
        } // Can/need to elaborate on the model.
      } // Got the model.
    } // Need to check remotely.
    if (debug)
      System.out.println("Returning /"+returnString+"/");
    return returnString;
  } // End method: getSubjectAlignedResidues

  /**
   * Returns subject sequence of the subject sequence OID given.  Repeat:
   * this is for the subject sequence, not for a feature.
   * @See getSubjectSequenceOids(OID oid);
   */
  public Sequence getSubjectSequence(OID subjectSequenceOid) {
    if (! protocolSupports(SEQUENCE)) {
      return null;
    } // Test for protocol support level.

    loadFeaturesIfNeeded();
    SequenceLoader sequenceLoader = new SequenceLoader();
    String finalURL = new RequestFormatter(urlString).formatSubjectSequenceRequestFor(subjectSequenceOid);
    if (debug)
      System.out.println("Finding sequence using "+finalURL);
    return sequenceLoader.getSequence(subjectSequenceOid.toString(), finalURL);
  } // End method: getSubjectSequence

  /** Override of SAX loader method to invoke URLs as needed. */
  public List getRootFeatures(OID entityOID, Set rangesOfInterest, boolean humanCurated) {
    if (! protocolSupports(LOAD)) {
      return Collections.EMPTY_LIST;
    } // Test for protocol support level.

    loadFeaturesIfNeeded();
    Range nextRange = null;
    for (Iterator it = rangesOfInterest.iterator(); it.hasNext(); ) {
      nextRange = (Range)it.next();
      loadXml(entityOID, nextRange, humanCurated);
    } // For all ranges.
    return super.getRootFeatures(entityOID, rangesOfInterest, humanCurated);
  } // End method

  //------------------------------------TEMPLATE METHOD OVERRIDES
  /** Initial load is never needed for this type of source. */
  protected void loadInitialIfNeeded() {
    // Do nothing
  } // End method

  /** Prep steps to initialize this loader object for use  Done here to avoid order dependency. */
  protected void loadFeaturesIfNeeded() {
    // Ensures that the driving handler is ready.
    if (getFeatureHandler() == null) {
      DrivingHandler featureHandler = new DrivingHandler((OIDParser)this);
      setFeatureHandler(featureHandler);
    } // No handler established.
  } // End method

  /** Returns a report of all features with the subject sequence of OID given. */
  public void addToSubjSeqRpt(String subjSeqId, OID genomeVersionOid, SubjectSequenceReport report) {
    if (urlString == null)
      throw new IllegalStateException("ERROR: cannot report until URL is set");

    if (subjSeqId.indexOf(":") == -1)
      subjSeqId = "INTERNAL:" + subjSeqId;

    // Prepare the request.
    String finalURL = null;
    finalURL = new RequestFormatter(urlString).formatReportOn(subjSeqId, genomeVersionOid);

    // Execute the request.
    try {
System.out.println("Loading subj seq rpt lines via "+finalURL);
      SubjSeqRptHandler handler = new SubjSeqRptHandler(  finalURL,
                                                          null,
                                                          (OIDParser)this);
      OID subjSeqOid = parseFeatureOID(subjSeqId);
      Set subjSeqOids = new HashSet();
      subjSeqOids.add(subjSeqOid);
      handler.getRptLines(subjSeqOids, report);

    } // End try block for URL comms.
    catch (Exception ex) {
      throw new IllegalStateException("ERROR: failed to generate subject sequence report on OID "+subjSeqId);
    } // End catch block for URL comms.

  } // End method

  /** Required to avoid behavior in use in base class. */
  protected synchronized void loadAxisOverRangeSet(OID axisOID, Set rangesOfInterest, boolean humanCurated) {
  } // End method

  /** Returns NO genomic axis OID. A signal that no axis is defined here. */
  public OID getGenomicAxisOID() { return null; }

  /** Force "no knowledge" of genomic axis alignment. */
  public Alignment getGenomicAxisAlignment() { return null; }

  //------------------------------------HELPER METHODS
  /**
   * Calls remotely for alignment string returns (in form of XML hierarchy),
   * so that all objects in the feature hierarchy containing the OID in
   * question can get alignment texts, if they exists remotely.
   */
  private void cacheAlignmentTextsForHierarchyOf(SimpleFeatureBean model) {
    OID oid = model.getOID();

    String finalURL = new RequestFormatter(urlString).formatAlignmentRequestFor(oid, model.getAxisOfAlignment());

    if (debug)
      System.out.println("Requesting cached align texts with "+finalURL);

    SingleFeatureHandler singleFeatureHandler = new SingleFeatureHandler(finalURL, null, getFeatureHandler().getOIDParser());
    FeatureCriterion criterion = new AllChildrenCriterion();
    List featureList = singleFeatureHandler.getModelsForCriterion(criterion);

    // Add the subject and query alignment text to all siblings, including
    // the one in question.
    for (Iterator it = featureList.iterator(); it.hasNext(); ) {
      SimpleFeatureBean nextModel = (SimpleFeatureBean)it.next();
      SimpleFeatureBean cachedModel = (SimpleFeatureBean)getFeatureHandler().getOrLoadModelForOid(nextModel.getOID());

      cachedModel.setSubjectAlignment(nextModel.getSubjectAlignment());
      cachedModel.setQueryAlignment(nextModel.getQueryAlignment());

    } // For all child features
  } // End method

  /** Given the previously-set URL, format the url with params, and load the result. */
  private void loadXml(OID axisOid, Range rangeOfInterest, boolean curation) {
    if (urlString == null)
      throw new IllegalStateException("ERROR: cannot load from URL until URL is set");

    int start = rangeOfInterest.getStart();
    int end = rangeOfInterest.getEnd();

    String finalURL = null;
    finalURL = new RequestFormatter(urlString).formatLoadFor(axisOid, start, end, curation);

    //System.gc();
    //System.out.println("Memory in use before URL feature load: "+
    //(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));

    getFeatureHandler().accumulateFeatures(finalURL);

    //System.gc();
    //System.out.println("Memory in use after URL feature load: "+
    //(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));

  } // End method

  /**
   * Probe implementation servlet/CGI to get its supported protocol actions.
   * Exception or error output will be interpretted as default level.
   */
  private void getProtocolSupport() {
    if (urlString == null) {
      protocolSupport = DEFAULT_PROTOCOL_SUPPORT;
    } // No url.  Default protocol level.
    else {
      List supportedActions = new ArrayList();
      try {
        // Format a URL to request the version from the servlet.
        String finalUrl = new RequestFormatter(urlString).formatProtocolSupportRequest();

        // Start the URL.
        URL url = new URL(finalUrl);
        BufferedReader urlReader = new BufferedReader(new InputStreamReader(url.openStream()));

        // Assume string is required value.  If not, allow exception to occur.
        String buffer = null;
        while (null != (buffer = urlReader.readLine())) {
          if (buffer.indexOf("Invalid action") == -1) {
            supportedActions.add(buffer);
          } // So far, no proof the servlet rejects the request.
          else {
            // Handle in exception block, and abandon any 'actions'
            throw new IllegalStateException();
          } // Servlet has rejected the request
        } // For all lines of input.

        // Setup final protocol support actions.
        protocolSupport = new String[supportedActions.size()];
        supportedActions.toArray(protocolSupport);

      } // End try to estab. prot lvl.
      catch (Exception ex) {
        protocolSupport = DEFAULT_PROTOCOL_SUPPORT;    // Default level.
      } // End catch
    } // Have URL.

    if (debug) {
      System.out.println("Service on URL "+urlString+" understands protocol actions:");
      for (int i = 0; i < protocolSupport.length; i++) {
        System.out.println(protocolSupport[i]);
      } // For all supported actions
    } // Debug
  } // End method

  /**
   * Given an operation, and the detected protocol level of the implementation,
   * return whether that implementation's protocol will support the operation.
   */
  private boolean protocolSupports(String operation) {
    for (int i = 0; i < protocolSupport.length; i++) {
      if (protocolSupport[i].equals(operation))
        return true;
    } // For all actions.
    return false;
  } // End method

  //------------------------------------OVERRIDES TO TEMPLATE METHODS
  /**
   * Builds a contig OID with all restrictions and translations required by
   * this contig file DOM loader.
   *
   * Restrictions: contig OIDs may not be in the SCRATCH namespace!
   *
   * This method is used by the superclass, but implemented here in the
   * subclass.
   *
   * @param String idstr of format "PREFIX:dddddddddd"
   *   where the d's represent a decimal (long) number.
   * @return OID a Contig OID.
   */
  public OID parseContigOID(String idstr) {

    OID returnOID = parseOIDorGA(idstr);

    // Enforce the restriction that the OID be non-scratch.
    if (returnOID.isScratchOID()) {
        returnOID = null;
        FacadeManager.handleException(
          new IllegalArgumentException("Illegal namespace contig ID "+idstr+": entered in XML file."));
    } // Test for scratch

    return returnOID;
  } // End method

  /**
   * Builds a contig OID with all restrictions and translations required by
   * this contig file DOM loader.
   *
   * Restrictions: contig OIDs may not be in the SCRATCH namespace!
   *
   * This method is used by the superclass, but implemented here in the
   * subclass.
   *
   * @param String idstr of format "PREFIX:dddddddddd"
   *   where the d's represent a decimal (long) number.
   * @return OID a Contig OID.
   */
  public OID parseFeatureOID(String idstr) {

    OID returnOID = parseOIDGeneric(idstr);

    // Enforce the restriction that the OID be non-scratch.
    if (returnOID.isScratchOID()) {
        returnOID = null;
        FacadeManager.handleException(
          new IllegalArgumentException("Illegal namespace feature ID "+idstr+": entered in XML file."));
    } // Test for scratch

    return returnOID;
  } // End method

  /**
   * Builds a contig OID with all restrictions and translations required by
   * this contig file DOM loader.
   *
   * Restrictions: contig OIDs may not be in the SCRATCH namespace!
   *
   * This method is used by the superclass, but implemented here in the
   * subclass.
   *
   * @param String idstr of format "PREFIX:dddddddddd"
   *   where the d's represent a decimal (long) number.
   * @return OID a Contig OID.
   */
  public OID parseEvidenceOID(String idstr) {

    OID returnOID = parseOIDGeneric(idstr);

    // Enforce the restriction that the OID be non-scratch.
    if (returnOID.isScratchOID()) {
        returnOID = null;
        FacadeManager.handleException(
          new IllegalArgumentException("Illegal namespace evidence ID "+idstr+": entered in XML file."));
    } // Test for scratch

    return returnOID;
  } // End method

  //------------------------------------INNER CLASSES
  /** 'Single feature handler' criterion for getting all child models. */
  class AllChildrenCriterion implements FeatureCriterion {

    /**
     * Return all the children of the model given.
     */
    public List allMatchingIn(FeatureBean model) {
      if (model == null)
        return java.util.Collections.EMPTY_LIST;

      List returnList = new ArrayList();
      if (model instanceof SimpleFeatureBean)
        returnList.add(model);
      else
        returnList.addAll(((CompoundFeatureBean)model).getChildren());
      return returnList;
    } // End method

  } // End class

} // End class: FeatureXmlDomLoader
