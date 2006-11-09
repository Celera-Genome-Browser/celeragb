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
package api.facade.concrete_facade.xml;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.facade_mgr.FacadeManager;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.OID;

 public class WorkspaceXmlFileCloseHandler {
   FacadeManagerBase facadeManager;


   public WorkspaceXmlFileCloseHandler(FacadeManagerBase facadeManager){
    this.facadeManager=facadeManager;
  }


   private GenomeVersion getGenomeVersion(String gbwfilename){
        GenomeVersionParser gp=new GenomeVersionParser();
        GenomeVersion gv=gp.parseForGenomeVersion(gbwfilename);
        return gv;
   }

  public void unLoadXmlFile(String xmlFile)  {


    FacadeManager.removeProtocolFromUseList( FacadeManager.getProtocolForFacade(api.facade.concrete_facade.xml.XmlWorkspaceFacadeManager.class));

    GenomeVersionSpace genomeVersionSpace = ((XmlWorkspaceFacadeManager)facadeManager).getGenomeVersionSpace();
    OID speciesOID=getGenomeVersion(xmlFile).getSpecies().getOid();
    XmlLoader loader=((FileGenomeVersionSpace)genomeVersionSpace).getLoaderForSpecies(speciesOID);
      ((FileGenomeVersionSpace)genomeVersionSpace).removeLoader(loader);


  }

}