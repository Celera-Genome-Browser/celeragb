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
package api.facade.concrete_facade.xml;

import java.util.Iterator;
import java.util.Map;

import api.facade.abstract_facade.annotations.GeneFacade;
import api.stub.data.OID;

/**
 * XML Implementation of the GeneFacade.  Returns all gene information found
 * in XML files, or made up here ;-)
 */
public class XmlGeneFacade extends XmlFeatureFacade implements GeneFacade {

   /** Used for passing props up/down inheritance hierarchy efficiently. */
   protected Map inheritProperties(OID featureOID) {

      Map returnProperties = null;
      Iterator loaders = null;

      if (featureOID == null) {
         returnProperties = java.util.Collections.EMPTY_MAP;
      }
      else {
         loaders = getXmlLoadersForFeature(featureOID);
         if (!loaders.hasNext()) {
            returnProperties = java.util.Collections.EMPTY_MAP;
         }
         else {
            returnProperties = super.inheritProperties(featureOID);
            String geneAccession = null;
            for (Iterator it = loaders; it.hasNext();) {
               geneAccession = ((XmlLoader)it.next()).getGeneacc(featureOID);
               if (geneAccession != null) {
                  break;
               }
            }
            
            if (geneAccession == null) {
               geneAccession = "null";
            }
            returnProperties.put(GeneFacade.GENE_ACCESSION_PROP, createGeneAccessionProperty(GeneFacade.GENE_ACCESSION_PROP, geneAccession));
         }
      }
      return (returnProperties);
   }
}
