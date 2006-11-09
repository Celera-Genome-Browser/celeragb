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
package client.gui.other.xml.xml_writer;

/**
 * Title:        Your Product Name
 * Description:  This is the main Browser in the System
 * @author       Peter Davies
 * @version
 */

import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.management.ModelMgr;
import api.entity_model.model.annotation.CuratedExon;
import api.entity_model.model.annotation.CuratedGene;
import api.entity_model.model.annotation.CuratedTranscript;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.annotation.Workspace;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.OID;
import api.stub.data.ReservedNameSpaceMapping;
import client.gui.framework.session_mgr.SessionMgr;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 * This class has the responsibility of copying any existing data from the open .gbw file that
 * has not either been modified in the client model or loaded into the client model and
 * explictly removed from the workspace by the user.
 */

public class XMLCopier {

  private static final String ENCODING = "UTF-8";

  private String gbwNewFileName;
  private boolean fileWasCopied;
  private List axesList;
  private GenomeVersion entityModel;
  private boolean appendToExistingGame;
  private String species;
  private long assembly;


  XMLCopier(String gbwNewFileName,List axesList,boolean appendToExistingGame, String species, long assembly) {
     this.gbwNewFileName=gbwNewFileName;
     this.axesList=axesList;
     this.appendToExistingGame=appendToExistingGame;
     this.species=species;
     this.assembly=assembly;
     GenomicEntity axis=(GenomicEntity)axesList.get(0);  //assume all axes are in same model
     entityModel=axis.getGenomeVersion();
  }

  /**
   * Check whether workspace file needs to have changes IN the workspace copied
   * back into it, and carry this out if so.
   */
  boolean copyFileIfNecessary() {
    if (!gbwFileNeedsCopying()) return false;
    try {


      Document document=getOriginalDocument();
      removeDeletedElements(document);
      removeUpdatedElements (document);
     // FileWriter writer=createGameFile(gbwNewFileName,species,assembly);
       FileWriter writer=new FileWriter(gbwNewFileName);
      //XmlWriteContext lContext = new XmlWriteContext (writer);
       Element  lElement=document.getDocumentElement();
       XMLSerializer serializer = new XMLSerializer(new OutputFormat(Method.XML, ENCODING, true));
       serializer.setOutputCharStream(writer);
       NodeList childrenOfGameRoot=lElement.getChildNodes();
       for(int i=0;i<childrenOfGameRoot.getLength();i++){
         if(childrenOfGameRoot.item(i) instanceof Element){
           Element child=(Element)(childrenOfGameRoot.item(i));
           if (!child.getNodeName().equals("date")&&(!child.getNodeName().equals("program"))&&(!child.getNodeName().equals("version"))) {
             serializer.serialize(child);
           }
         }
       }
      writer.flush();
      writer.close();
      fileWasCopied=true;
    }
    catch (Exception ex) {
      SessionMgr.getSessionMgr().handleException(ex);
    }
    return fileWasCopied;
  }




  boolean gbwFileNeedsCopying() {
     return findOriginalGBWFileName()!=null;
  }

  boolean fileWasCopied () {
    return fileWasCopied;
  }


  private void removeDeletedElements(Document document) {
     /**
      *@todo Replace that null with the line after, when the getOIDS... is implemented.
      */
     Set removeSet=null;

    //entityModel.getOIDsOfFeaturesMarkedAsDeletedFromWorkSpace();

    Workspace workspace=null;
    GenomeVersion model=null;
    for(Iterator i=ModelMgr.getModelMgr().getSelectedGenomeVersions().iterator();i.hasNext();){
      model=(GenomeVersion)i.next();
      if(model.hasWorkspace()){
        workspace= model.getWorkspace();
        break;
      }
    }
     removeSet=workspace.getOIDsOfFeaturesDeletedThisSession();
     removeOidsFromDocument(document, removeSet);
  }



  private void removeUpdatedElements(Document document) {

    Workspace workspace=null;
    GenomeVersion model=null;
    for(Iterator i=ModelMgr.getModelMgr().getSelectedGenomeVersions().iterator();i.hasNext();){
      model=(GenomeVersion)i.next();
      if(model.hasWorkspace()){
        workspace= model.getWorkspace();
        break;
      }
    }
     Set oidList=workspace.getWorkspaceOids();

     removeOidsFromDocument(document, oidList);

  }

  private void removeOidsFromDocument(Document document, Collection oids) {
     if (oids.size()==0) return;
     removeOIDsInNodeListAndOIDListFromDocument(document, document.getElementsByTagName("annotation"), oids);
     removeOIDsInNodeListAndOIDListFromDocument(document, document.getElementsByTagName("feature_set"), oids);
     removeOIDsInNodeListAndOIDListFromDocument(document, document.getElementsByTagName("feature_span"), oids);
  }

  private void removeOIDsInNodeListAndOIDListFromDocument(Document document, NodeList nodeList, Collection oids) {
     OID[] oidsToBeRemoved=new OID[oids.size()];
     oids.toArray(oidsToBeRemoved);
     Collection nodes = new ArrayList();
     for (int i = 0; i < nodeList.getLength(); i++) {
        nodes.add(nodeList.item(i));
     } // For all nodes to be checked

     String oidString;
     Node nextNode = null;
     for (Iterator it = nodes.iterator(); it.hasNext(); ) {
        nextNode = (Node)it.next();
        for (int j=0;j<oidsToBeRemoved.length;j++) {
           oidString = ((Element)nextNode).getAttribute("id");
          // System.out.println("ID: "+oidString+"  Namespace:"+ getInternalWorkSpaceFromXmlId(oidString)+ " ID: "+getIdentifierFromXmlId(oidString));
          // System.out.println("OID: "+ oidsToBeRemoved[j].getNameSpaceAsString()+ " : "+oidsToBeRemoved[j].getIdentifierAsString());
           if (getIdentifierFromXmlId(oidString).equals(oidsToBeRemoved[j].getIdentifierAsString()) &&
               getInternalWorkSpaceFromXmlId(oidString).equalsIgnoreCase(oidsToBeRemoved[j].getNameSpaceAsString())) {
             nextNode.getParentNode().removeChild(nextNode);
           }
        }
     }
  }

  private String getInternalWorkSpaceFromXmlId(String xmlID){
     if (xmlID.indexOf(":")==-1) return "";
     String externalWorkspace=xmlID.substring(0,xmlID.indexOf(":"));
     return ReservedNameSpaceMapping.translateToReservedNameSpace(externalWorkspace);
  }

  private String getIdentifierFromXmlId(String xmlID) {
     if (xmlID.indexOf(":")==-1) return xmlID;
     return xmlID.substring(xmlID.indexOf(":")+1,xmlID.length());
  }

  private String findOriginalGBWFileName() {
    Object[] dataSources=FacadeManager.getFacadeManager().getOpenDataSources();
    for (int i=0;i<dataSources.length;i++) {
       if (dataSources[i] != null) {
         if (dataSources[i].toString().toLowerCase().endsWith(".gbw")) return dataSources[i].toString();
       }
    }
    return null;
  }

  /**
   * Creates DOM document representing previously opened workspace file.
   */
  private Document getOriginalDocument() {
    String lInputfileName=findOriginalGBWFileName();
    if (lInputfileName.indexOf (":") <= 1) {
      lInputfileName = "file:" + lInputfileName;
    }

    DOMParser lParser = new DOMParser();

    Document lDocument = null;
    try {
      // Configure parser for: no deferred node expansion, and no validation.
      lParser.setFeature( "http://apache.org/xml/features/dom/defer-node-expansion", false );
      lParser.setFeature( "http://xml.org/sax/features/validation", false );
      lParser.parse(lInputfileName);

      // Gather the parsed data into a Document (DOM) object.
      lDocument = lParser.getDocument();

    } // End try
    catch (org.xml.sax.SAXException lSAXE) {
      System.err.println("ERROR: SAX Exception during parse");
      System.err.println("INFO: "+lSAXE.getMessage ());
      SessionMgr.getSessionMgr().handleException(lSAXE);

    } // Sax excep thrown
    catch (IOException lIOE) {
      System.err.println("ERROR: IO failure during parse");
      System.err.println("INFO: "+lIOE.getMessage ());
      SessionMgr.getSessionMgr().handleException(lIOE);

    } // IO excep thrown

    return lDocument;
  }

  private FileWriter createGameFile(String path, String species, long assemblyVersion) throws IOException{

       FileWriter writer=new FileWriter(path);
       return writer;



  }


  class AxisVisitor extends GenomicEntityVisitor {
    private List removeList;

    AxisVisitor(List removeList) {
      this.removeList=removeList;
    }

    public void visitCuratedGene(CuratedGene gene) {
        if (gene.isWorkspace()) buildRemoveList(gene);
    }
    public void visitCuratedTranscriptProxy(CuratedTranscript curatedTranscript) {
        if (curatedTranscript.isWorkspace()) buildRemoveList(curatedTranscript);
    }
    public void visitCuratedExonProxy(CuratedExon exon) {
        if (exon.isWorkspace()) buildRemoveList(exon);
    }

    private void buildRemoveList(Feature feature) {
         removeList.add(feature);
    }

  }
}