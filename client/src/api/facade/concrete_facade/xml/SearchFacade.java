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
 * Title:        Genome Browser<p>
 * Description:  <p>
 * @author Les Foster
 * @version $Id$
 */
package api.facade.concrete_facade.xml;

import api.facade.concrete_facade.xml.sax_support.CEFParseHelper;
import api.facade.concrete_facade.xml.sax_support.ElementContext;
import api.facade.concrete_facade.xml.sax_support.FeatureHandlerBase;
import api.facade.concrete_facade.xml.sax_support.GenomicsExchangeHandler;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.NavigationNode;

import java.util.Map;

public class SearchFacade extends GenomicsExchangeHandler {

   String fileName = null;
   
   /** Filename constructor.  Parses an input file. */
   public SearchFacade(String fileName) {
      super();
      this.fileName = fileName;
      setElementStacker(new LinkedListElementStacker());
      setFeatureHandler(new LocalFeatureHandler());
   } // End constructor

   //---------------------------------INNER CLASSES
   class LocalFeatureHandler extends FeatureHandlerBase {
      private String mostRecentID = null;
      private int mostRecentPathType = -1;

      /**
       * Called on subclass when element start tag encountered.
       *
       * @param String lName the name of the element.
       * @param AttributeList lAttrs the collection of tag attributes.
       */
      public void startElementTemplateMethod(ElementContext context) {
         // Decode which element is requested, sans any string comparisons.
         int foundCode = CEFParseHelper.getCEFParseHelper().translateToElementCode(context.currentElement());
         if (foundCode == CEFParseHelper.UNINTERESTING_ELEMENT_CODE)
            return;

         if (foundCode == CEFParseHelper.FEATURE_SET_CODE) {
            mostRecentPathType = NavigationNode.CURATED;
            mostRecentID = (String) context.ancestorAttributesNumber(0).get(ID_ATTRIBUTE);
         } // compound human curation
         else if (foundCode == CEFParseHelper.RESULT_SET_CODE) {
            mostRecentPathType = NavigationNode.PRECOMPUTE_HIGH_PRI;
            mostRecentID = (String) context.ancestorAttributesNumber(0).get(ID_ATTRIBUTE);
         } // compound precompute
         else if (foundCode == CEFParseHelper.FEATURE_SPAN_CODE) {
            mostRecentPathType = NavigationNode.CURATED;
            mostRecentID = (String) context.ancestorAttributesNumber(0).get(ID_ATTRIBUTE);
         } // simple human curation
         else if (foundCode == CEFParseHelper.RESULT_SPAN_CODE) {
            mostRecentPathType = NavigationNode.PRECOMPUTE_HIGH_PRI;
            mostRecentID = (String) context.ancestorAttributesNumber(0).get(ID_ATTRIBUTE);
         } // simple precompute
         else if (foundCode == CEFParseHelper.ANNOTATION_CODE) {
            mostRecentPathType = NavigationNode.CURATED;
            mostRecentID = (String) context.ancestorAttributesNumber(0).get(ID_ATTRIBUTE);
         } // gene curation
         else if (foundCode == CEFParseHelper.SEQ_RELATIONSHIP_CODE) {
            // Establish: is this the query?
            if (((String) context.ancestorAttributesNumber(0).get(TYPE_ATTRIBUTE)).equals(QUERY_SEQ_RELATIONSHIP)) {
               // Grab its interesting attribute.
               String queryID = (String) context.ancestorAttributesNumber(0).get(ID_ATTRIBUTE);

               // Now have enough for a partial path.
               System.out.println("AXIS: (" + queryID + ")");
               System.out.println("      TYPE: " + mostRecentPathType + " (" + mostRecentID + ")");
            } // Query.
         } // Sequence Relationship
      } // End class: startElementTemplateMethod

      /**
       * Called on subclass when element end tag was encountered.
       *
       * @param String lName the element which just ended.
       */
      public void endElementTemplateMethod(ElementContext context) {
      } // End class: endElementTemplateMethod

      /**
       * Called on subclass for character content.  This one need not
       * be overridden.  If it is, implementation should make super call.
       *
       * @param char[] lCharacters the whole buffer being constructed.
       * @param int lStart the starting point within the buffer.
       * @param int lLength the ending point within the buffer.
       */
      public void charactersTemplateMethod(char[] lCharacters, int lStart, int lLength, ElementContext lContext) {
         //super.charactersTemplateMethod(lCharacters, lStart, lLength, lContext);
         // Stow the content to designated buffers as it occurs.
      } // End method: charactersTemplateMethod

      /**
       * Returning the fill buffer map allows values to be assigned to buffers
       * local to current handler.
       */
      public Map getFillBufferMapTemplateMethod() {
         return null;
      }

      //------------------------------IMPLEMENTATION OF ExceptionHandler
      /** Simply delegates to the facade manager. */
      public void handleException(Exception lException) {
         FacadeManager.handleException(lException);
      } // End method: handleException

   } // End class: LocalFeatureHandler

   public void loadFile() {
      super.loadFile( this.fileName );
   }
   
   public static void main(String[] args) {
      if (args.length == 0) {
         System.out.println("USAGE: java SearchFacade <filename>");
         System.exit(0);
      } // No args.

      SearchFacade sf = new SearchFacade(args[0]);
      sf.loadFile();
      sf = null;
   } // End main
} // End class: SearchFacade