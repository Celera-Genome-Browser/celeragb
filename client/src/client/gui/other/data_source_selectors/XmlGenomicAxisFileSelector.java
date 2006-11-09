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
package client.gui.other.data_source_selectors;

import api.facade.concrete_facade.xml.GenomicAxisXmlFileOpenHandler;
import api.facade.facade_mgr.DataSourceSelector;
import api.facade.facade_mgr.FacadeManager;
import api.facade.facade_mgr.FacadeManagerBase;
import shared.io.ExtensionFileFilter;

import java.io.File;


public class XmlGenomicAxisFileSelector  extends XmlFileSelector implements DataSourceSelector{

 private/* static*/ ExtensionFileFilter xml_filter =
  new ExtensionFileFilter("Genome Browser Axis (*.gba)", ".gba");

  //---------------------IMPLEMENTATION of DataSourceSelector

  public void setDataSource(FacadeManagerBase facade, Object dataSource){
        GenomicAxisXmlFileOpenHandler g=new GenomicAxisXmlFileOpenHandler(facade);
        try {
           g.loadXmlFile((String)dataSource);
        }
        catch(Exception e){
          e.printStackTrace();
        }
        setFacadeProtocol();
  }

  /**
   * Allows user to return a data source for XML Contig facade manager.
   */
  public  void selectDataSource(FacadeManagerBase f){

    String file_name=null;
    File selected_file=null;

    try {
      String debugFileName=null;
      try {
         debugFileName=System.getProperty("x.genomebrowser.XMLAssemblyDebugFile");
      }
      catch (Exception ex) {} //shallow missing resource exception as it is expected
      if (debugFileName!=null) {
         File debugFile=new File(debugFileName);
         if (debugFile==null) throw new IllegalStateException("x.genomebrowser.XMLAssemblyDebugFile property set to file that cannot be found");
         selected_file=debugFile;
      }
      else selected_file = askUserForFile(this.xml_filter);

    /*
    This is no longer necessary, pass -Dx.genomebrowser.XMLAssemblyDebugFile=<filename> on the command line
      // NOTE: errors have been shown to occur in debugging JBuilder3.5,
      // particularly when a FileChooser is invoked.  One
      // way to get around this is to add the following line(s), and comment
      // out any call to "askUserForFile"
      //
      selected_file=new File("\\cvsfiles\\client-devel\\bin\\resource\\client\\XMLdata\\testGene.gba");
     */
      if (selected_file != null) {
        file_name = selected_file.getAbsolutePath();
        GenomicAxisXmlFileOpenHandler g=new GenomicAxisXmlFileOpenHandler(f);

        g.loadXmlFile(file_name);
        setFacadeProtocol();
      } // User chose a file.

    } catch(Exception e){
      e.printStackTrace();

    } // End catch block for opening assembly ".gba" file.

  } // End method: selectDataSource

  //This method is defined for all sub classes of XmlFileSelector.
  protected void setFacadeProtocol() {
    String protocolToAdd = new String(
            FacadeManager.getProtocolForFacade(api.facade.concrete_facade.xml.XmlGenomicAxisFacadeManager.class));
    FacadeManager.addProtocolToUseList(protocolToAdd);
  }

}
/*
 $Log$
 Revision 1.16  2002/11/08 14:13:36  lblick
 Moved from the package client.shared.file_chooser.

 Revision 1.15  2002/11/07 19:47:21  lblick
 Moved shared.file_chooser package to client.shared.file_chooser package.

 Revision 1.14  2002/11/07 16:10:26  lblick
 Removed obsolete imports and unused local variables.

 Revision 1.13  2002/07/18 22:18:26  tsaf
 Moved ExtensionFileFilter from api.concrete_facade.xml into the
 x.shared.file_chooser package so I coule use it.
 Updated all dependent files.

 Revision 1.12  2000/12/04 17:53:20  lfoster
 Fixed problem which led to files appearing twice when the debug property was set.

 */
