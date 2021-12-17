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
package client.gui.framework.bookmark;

import api.entity_model.management.ModelMgr;
import api.entity_model.model.annotation.CuratedGene;
import api.entity_model.model.annotation.CuratedTranscript;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.assembly.Contig;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.genetics.Chromosome;
import api.entity_model.model.genetics.GenomeVersion;
import api.entity_model.model.genetics.Species;
import api.facade.abstract_facade.annotations.FeatureFacade;
import api.facade.abstract_facade.annotations.GeneFacade;
import api.facade.abstract_facade.annotations.TranscriptFacade;
import api.stub.data.InvalidPropertyFormat;
import api.stub.data.NavigationPath;
import api.stub.data.OID;
import shared.preferences.InfoObject;
import shared.preferences.PreferenceManager;

import java.util.Properties;


public class BookmarkInfo extends InfoObject {

  private static final String OID_IDENTIFIER    = "OIDIdentifier";
  private static final String OID_NAMESPACE     = "OIDNamespace";
  private static final String GENOME_VERSION_ID = "GenomeVersionID";
  private static final String NAME              = "Name";
  private static final String SEARCH_VALUE      = "SearchValue";
  private static final String TYPE              = "Type";
  private static final String SPECIES           = "Species";
  private static final String URL_STRING        = "URL";
  private static final String COMMENTS          = "Comments";

  private OID oid;
  private String species="";
  private String searchValue="";
  private String type="";
  private String bookmarkURLText="";
  private String comments="";

  public BookmarkInfo(String keyBase, Properties inputProperties, String sourceFile) {
    int genomeVersionID = 0;
    String oidIdentifier = "";
    String oidNamespace  = "";

    this.keyBase=keyBase;
    this.sourceFile=sourceFile;
    String tmpString = new String("");

    tmpString = (String)inputProperties.getProperty(keyBase+"."+SEARCH_VALUE);
    if (tmpString!=null) searchValue=tmpString;
    else searchValue="Unknown";

    tmpString = (String)inputProperties.getProperty(keyBase+"."+TYPE);
    if (tmpString!=null) type=tmpString;
    else type="Unknown";

    tmpString = (String)inputProperties.getProperty(keyBase+"."+SPECIES);
    if (tmpString!=null) species=tmpString;
    else species="Unknown";

    tmpString = (String)inputProperties.getProperty(keyBase+"."+URL_STRING);
    if (tmpString!=null) bookmarkURLText=tmpString;
    else bookmarkURLText="Unknown";

    tmpString = (String)inputProperties.getProperty(keyBase+"."+COMMENTS);
    if (tmpString!=null) comments=tmpString;
    else comments="Unknown";

    tmpString = (String)inputProperties.getProperty(keyBase+"."+OID_IDENTIFIER);
    if (tmpString!=null) oidIdentifier=tmpString;
    else oidIdentifier="Unknown";

    tmpString = (String)inputProperties.getProperty(keyBase+"."+OID_NAMESPACE);
    if (tmpString!=null) oidNamespace=tmpString;
    else oidNamespace="Unknown";

    tmpString = (String)inputProperties.getProperty(keyBase+"."+GENOME_VERSION_ID);
    if (tmpString!=null) {
      Integer tmpInt = new Integer(tmpString);
      genomeVersionID=tmpInt.intValue();
    }
    else genomeVersionID=0;

    tmpString = (String)inputProperties.getProperty(keyBase+"."+NAME);
    if (tmpString!=null) name=tmpString;
    else name="Unknown";

    this.oid = new OID(oidNamespace, oidIdentifier, genomeVersionID);
  }


  public BookmarkInfo(GenomicEntity bookmarkEntity) {
    this.oid=bookmarkEntity.getOid();
    getBookmarkAttributesForEntity(bookmarkEntity);
    this.name = this.oid.getIdentifierAsString();
    this.species = bookmarkEntity.getGenomeVersion().getSpecies().toString();
    this.keyBase=PreferenceManager.getKeyForName(name, true);
    this.bookmarkURLText = getURLForEntity(bookmarkEntity);
  }


  // This constructor should only be used for the clone.
  private BookmarkInfo(String keyBase, String name, String sourceFile, OID oid,
    String bookmarkURLText, String comments, String searchValue, String type,
    String species){
    this.oid = oid;
    this.keyBase = keyBase;
    this.name = name;
    this.searchValue = searchValue;
    this.type = type;
    this.species = species;
    this.sourceFile = sourceFile;
    this.bookmarkURLText = bookmarkURLText;
    this.comments = comments;
  }


  public OID getOid() {
      return oid;
  }


  public String getComments() { return comments; }
  void setComments(String newComments) {
    isDirty=true;
    if (newComments!=null) this.comments = newComments;
  }


  public String getDisplayName() {
    return type + ":     " + searchValue + "     " + oid.toString();
  }


  /**
   * @todo Should this be an official URL class instead of a string?
   * No big deal to change it later as it will get constructed from properties
   * and written to properties anyway.
   */
  public String getBookmarkURLText() { return bookmarkURLText; }
  void setBookmarkURLText(String bookmarkURLText) {
    isDirty=true;
    if (bookmarkURLText!=null) this.bookmarkURLText = bookmarkURLText;
  }


  /**
   * This method returns a classical URL bookmark to the calling class and allows
   * for external navigation the feature in question.  This mechanism uses the OID
   * search as the OID is unique to a genome version.
   */
  public static String getURLForEntity(GenomicEntity ge) {
    return "http://localhost:30000/?action=search&unknown_oid="+ge.getOid()+"&redir=204";
  }


  public String getSpecies() { return species; }
  void setSpecies(String species) {
    isDirty = true;
    this.species = species;
  }

  public String getBookmarkType() { return type; }
  void setBookmarkType(String type) {
    isDirty = true;
    this.type = type;
  }

  public String getSearchValue() { return searchValue; }
  void setSearchValue(String searchValue) {
    isDirty = true;
    this.searchValue = searchValue;
  }

  public String toString() {
     return getDisplayName();
  }


  public Object clone() {
    BookmarkInfo tmpInfo = new BookmarkInfo(this.keyBase, this.name, this.sourceFile,
      (OID)oid.clone(), this.bookmarkURLText, this.comments, this.searchValue,
      this.type, this.species);
    return tmpInfo;
  }


  public String getKeyName(){
    return "Bookmark." + keyBase;
  }


  /**
   * This method is so the object will provide the formatted properties
   * for the writeback mechanism.
   */
  public Properties getPropertyOutput(){
    Properties outputProperties=new Properties();
    String key = getKeyName()+".";

    outputProperties.put(key+NAME,name);
    outputProperties.put(key+SEARCH_VALUE,searchValue);
    outputProperties.put(key+TYPE,type);
    outputProperties.put(key+SPECIES,species);
    outputProperties.put(key+URL_STRING, bookmarkURLText);
    outputProperties.put(key+COMMENTS, comments);
    outputProperties.put(key+OID_NAMESPACE, oid.getNameSpaceAsString());
    outputProperties.put(key+OID_IDENTIFIER, oid.getIdentifierAsString());
    outputProperties.put(key+GENOME_VERSION_ID, Integer.toString(oid.getGenomeVersionId()));

    return outputProperties;
  }


  /**
   * This method provides the name to use for the bookmark menu item and any other
   * request to create a bookmark.
   */
  private void getBookmarkAttributesForEntity(GenomicEntity ge) {
    if (ge instanceof Species) {
      type = "Species";
      if (ge.getDisplayName()!=null && !ge.getDisplayName().equals(""))
        searchValue = ge.getDisplayName();
    }
    else if (ge instanceof Chromosome) {
      type = "Chromosome";
      if (ge.getDisplayName()!=null && !ge.getDisplayName().equals(""))
        searchValue = ge.getDisplayName();
    }
    else if (ge instanceof GenomicAxis) {
      type = "Genomic Axis";
      if (ge.getDisplayName()!=null && !ge.getDisplayName().equals(""))
        searchValue = ge.getDisplayName();
    }
    else if (ge instanceof Contig) {
      type = "Contig";
      searchValue = "Contig";
    }
    else if (ge instanceof CuratedGene) {
      type = "Gene";
      if (propertyExists(GeneFacade.GENE_ACCESSION_PROP, ge))
        searchValue = ge.getProperty(GeneFacade.GENE_ACCESSION_PROP).getInitialValue();
      else if (propertyExists(FeatureFacade.GROUP_TAG_PROP, ge))
        searchValue = ge.getProperty(FeatureFacade.GROUP_TAG_PROP).getInitialValue();
    }
    else if (ge instanceof CuratedTranscript) {
      type = "Transcript";
      if (propertyExists(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP, ge)) {
        searchValue = ge.getProperty(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP).getInitialValue();
      }
      else if (propertyExists(FeatureFacade.GROUP_TAG_PROP, ge))
        searchValue = ge.getProperty(FeatureFacade.GROUP_TAG_PROP).getInitialValue();
    }
    else if (ge instanceof Feature) {
      type = "Feature";
      if (propertyExists(FeatureFacade.GROUP_TAG_PROP, ge))
        searchValue = ge.getProperty(FeatureFacade.GROUP_TAG_PROP).getInitialValue();
    }
  }


  /**
   * Helper method that checks if a property needed to name a bookmark exists.
   */
  private boolean propertyExists(String propertyName, GenomicEntity lastSelection) {
    if (lastSelection.getProperty(propertyName)!=null &&
        lastSelection.getProperty(propertyName).getInitialValue()!=null &&
        !lastSelection.getProperty(propertyName).getInitialValue().equals("")) return true;
    else return false;
  }


  public NavigationPath getNavigationPath() throws InvalidPropertyFormat {
      NavigationPath[] paths;
      if (getGenomeVersion()==null) return null;
      else paths=getGenomeVersion().getNavigationPathsToOIDInThisGenomeVersion(oid);
      return paths.length==0 ? null : paths[0];
  }


  private GenomeVersion getGenomeVersion() {
     return ModelMgr.getModelMgr().getGenomeVersionById(oid.getGenomeVersionId());
  }
}