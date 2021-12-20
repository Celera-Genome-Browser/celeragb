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
package client.gui.application.game_viewer;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;

public class XMLParser {
  private File file;
  private Document document;

  public XMLParser(File file) {
    this.file=file;
  }

  public Document getDocument() throws org.xml.sax.SAXException, IOException{
    if (document!=null) return document;

    DOMParser parser = new DOMParser();
    parser.setFeature( "http://apache.org/xml/features/dom/defer-node-expansion", false );
    parser.setFeature( "http://xml.org/sax/features/validation", false );

   // try {
      parser.parse("file:"+file.getAbsolutePath());

      // Gather the parsed data into a Document (DOM) object.
      document = parser.getDocument();
  /*  }
    catch (org.xml.sax.SAXException saxEx) {
      System.err.println("ERROR: SAX Exception during parse");
      System.err.println("INFO: "+saxEx.getMessage ());
      saxEx.printStackTrace();
    }
    catch (IOException ioEx) {
      System.err.println("ERROR: IO failure during parse");
      System.err.println("INFO: "+ioEx.getMessage ());
      ioEx.printStackTrace();
    }*/
    return document;
  }

}
