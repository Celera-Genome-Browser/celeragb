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
package api.facade.concrete_facade.xml.sax_support;

/**
 * Title:        Source for GenomicProperty instances based on information read from XML.
 * Description:  Means of supplying properties on demand.  Encapsulates
 *               property and its child properties, and provides factory
 *               for GenomicProperty<p>
 * Company:      []<p>
 * @author Les Foster
 * @version
 */

import api.stub.data.ControlledVocabUtil;
import api.stub.data.FlaggedGenomicProperty;
import api.stub.data.GenomicProperty;

import java.io.Serializable;
import java.util.*;

/**
 * Source for a genomic property.  Can supply expansions, etc.
 */
public class PropertySource implements Serializable {

    //---------------------------------CLASS CONSTANTS
    private static final String NULL_EDITING_CLASS_STRING = "";

    //---------------------------------CLASS VARIABLES
    private static Map controlledMap;
    private static Map editingClassMap;
    private static Map forcedValueMap;
    private static Set flaggedPropertyTriggers;

    //---------------------------------INSTANCE VARIABLES
    private String propertyName;
    private String propertyValue;
    private List childList;
    private boolean sourcesEditableProperties;

    //---------------------------------INSTANCE SETTERS
    /** Sets mappings used in constructing properties. Keys are all prop names. */
    public static void setPropertyMaps(Map lControlledMap, Map lEditingClassMap, Map lForcedValueMap) {
        controlledMap = lControlledMap;
        editingClassMap = lEditingClassMap;
        forcedValueMap = lForcedValueMap;
    } // End method

    /** Sets the triggering property names for creating flagged properties. */
    public static void setFlaggedPropertyTriggers(Set lFlaggedPropertyTriggers) {
        flaggedPropertyTriggers = lFlaggedPropertyTriggers;
    } // End method

    //---------------------------------CONSTRUCTORS
    /**
     * Gets a name for the property, as well as its initial value.
     */
    public PropertySource(String propertyName, String propertyValue, boolean sourcesEditableProperties) {
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.sourcesEditableProperties = sourcesEditableProperties;
    } // End constructor

    //---------------------------------PUBLIC INTERFACE
    /**
     * Allows external addition of a child, which will be exposed via
     * expansion.
     */
    public void addChildSource(PropertySource childSource) {
        if (childList == null)
            childList = new ArrayList();
        childList.add(childSource);
    } // End method: addChildSource

    /**
     * Allows external addition of a list of child sources, with no
     * sanity checking to ensure that that is really what is added!
     */
    public void addChildSources(List childSources) {
        if (childList == null)
            childList = new ArrayList();
        childList.addAll(childSources);
    } // End method: addChildSources

    /**
     * Creational method to make a real property.  This is setup
     * here to avoid having dependencies between the loader and the
     * abstract facades.
     */
    public GenomicProperty createGenomicProperty(boolean createFlaggedProperty) {
        boolean editable = false;
        if (propertyName.equalsIgnoreCase("id"))
            editable = false;
        else
            editable = isEditable();
        GenomicProperty returnableProperty;
        if (createFlaggedProperty) {
          returnableProperty = new FlaggedGenomicProperty(
                propertyName,
                makeEditingClass(propertyName),
                makePropertyValue(propertyName, propertyValue),
                editable,
                makeControlledVocabularyIndex(propertyName)
          );
        }
        else {
          returnableProperty = new GenomicProperty(
                propertyName,
                makeEditingClass(propertyName),
                makePropertyValue(propertyName, propertyValue),
                editable,
                makeControlledVocabularyIndex(propertyName)
          );
        }

        // Generate the subprops.
        if (isExpandable()) {
            this.preLoadProps(returnableProperty);
        } // Must pre-load sub properties.

        return returnableProperty;
    } // End method: createGenomicProperty

    /**
     * Returns the name of the property this source is describing.
     */
    public String getName() {
        return propertyName;
    } // End method: getName

    /** Returns "initial value" w/out producing a property. */
    public String getValue() {
        return propertyValue;
    } // End method: getValue

    /**
     * Return the direct descendents of this property source.
     */
    public List getChildSources() {
        return childList;
    } // End method: getChildSources

    /**
     * Return the list of child properties for the property name given.
     */
    public List findChildrenFor(String propertyName) {
        List nextLevel = getChildSources();
        if (propertyName.equals(this.getName()))
           return nextLevel;

        if (nextLevel == null)
            return null;

        PropertySource propertySource = null;
        List targetList = null;
        for (Iterator it = nextLevel.iterator(); (targetList == null) && it.hasNext(); ) {
            propertySource = (PropertySource)it.next();

            // Going depth-first.  Do not expect very many properties, so
            // response time should not reflect this oddity.
            targetList = propertySource.findChildrenFor(propertyName);

        } // For all iterations.
        return targetList;
    } // End method: findChildrenFor

    //---------------------------------HELPER METHODS
    /**
     * Gets expandability for this property.
     */
    private boolean isExpandable() {
        return (childList != null);
    } // End method: isExpandable

    /**
     * Gets editability for this property.
     */
    private boolean isEditable() {
        return sourcesEditableProperties;
    } // End method: isEditable

    /**
     * Loads all properties below the one given, as sub
     * properties OF the given prop.
     */
    private void preLoadProps(GenomicProperty expandableProperty) {

        boolean useFlaggedProperty = isFlaggedProperty(expandableProperty.getName());
        GenomicProperty[] subProps = new GenomicProperty[childList.size()];

        // Set subprops at this level.
        PropertySource nextSource = null;
        int i = 0;
        for (Iterator it = childList.iterator(); it.hasNext(); ) {
            nextSource = (PropertySource)it.next();
            // NOTE: this part could recursively trigger additional preloading
            //  at the next level, due to how the create is structured.
            subProps[i++] = nextSource.createGenomicProperty(useFlaggedProperty);
        } // For all sub properties.

        expandableProperty.setSubProperties(subProps);

    } // End method: preLoadProps

    /**
     * Returns flag of whether the prop should be created as flagged or otherwise.
     */
    boolean isFlaggedProperty(String propertyName) {
        // No triggers implies no flagged props.
        if (flaggedPropertyTriggers == null)
            return false;
        else
            return flaggedPropertyTriggers.contains(propertyName);
    } // End method: isFlaggedProperty

    /**
     * Given the name of the property apply any RULES [HINT: place to apply
     * the rules being created for props by the 'special team'] that place
     * a controlled vocabulary against the property.
     */
    private String makeControlledVocabularyIndex(String propertyName) {
        // Currently using a map of property name versus controlled vocab.
        if (controlledMap.containsKey(propertyName))
            return (String)controlledMap.get(propertyName);
        else
            return ControlledVocabUtil.getNullVocabIndex();
    } // End method: makeControlledVocabIndex

    /**
     * Given a property name, return the editing class string associated with it.
     */
    private String makeEditingClass(String propertyName) {
        if (editingClassMap.containsKey(propertyName))
            return (String)editingClassMap.get(propertyName);
        else
            return NULL_EDITING_CLASS_STRING;

    } // End method: makeEditingClass

    /**
     * Pulls in property name, and certain props will receive a made-up
     * value.
     */
    private String makePropertyValue(String propertyName, String propertyValue) {
        if (forcedValueMap.containsKey(propertyName))
            return (String)forcedValueMap.get(propertyName);
        else
            return propertyValue;
    } // End method: makePropertyValue

} // End method: PropertySource

/*
 $Log$
 Revision 1.1  2006/11/09 21:36:16  rjturner
 Initial upload of source

 Revision 1.4  2002/11/07 16:06:57  lblick
 Removed obsolete imports and unused local variables.

 Revision 1.3  2002/05/30 21:44:44  lfoster
 Moved some code out to XmlFacadeManager

 Revision 1.2  2002/05/24 23:32:58  tsaf
 Req 7.1.16.6 Flagged Genomic Property

 Revision 1.1  2002/04/05 19:06:58  lfoster
 Moved 8 classes from xml down to xml.sax_support.  Removed dep of PropertySource on abstract facades.

 Revision 1.17  2002/03/05 16:13:59  lfoster
 Subject Sequence Report now looking at property sources instead of just obsoleted "score" and "output" tags for sum e val and bit score.

 Revision 1.16  2002/02/22 23:43:51  tsaf
 Commented out debug statements.
 Added a return character.
 Added "Predicted ORF" curation flag.
 (These are all changes from Branch.)

 Revision 1.15.4.1  2002/02/22 18:08:39  tsaf
 Added the "Predicted ORF" curation flag.

 Revision 1.15  2002/01/21 17:47:59  BhandaDn
 Frame Shift and Translation Exception curation flags were not in the static list of variables
 and thats why the values for them were not obeying controlled Vocab upon gbw load

 Revision 1.14  2001/10/05 18:12:34  tsaf
 Attempting to break everything by calculating the GenomicProperty
 "isExpandable" attribute on-the-fly.

 Revision 1.13  2001/08/09 18:44:19  lfoster
 Pre-loading sub properties if property is expandable.

 Revision 1.12  2001/06/18 17:50:05  tsaf
 New table for expandable property.

 Revision 1.11  2001/04/27 11:57:20  pdavies
 Removed rendering class, getMethod and setMethod from GenomicProperty.  Made all attributes private and provided mutators where necessary.

 Revision 1.10  2001/03/07 23:28:58  jbaxenda
 Reinstated postConstruct to ensure curated features are pre-populated with thier
 properties. Also changed getProperties to pre-expand all expandable properties
 for Computed features.

 Revision 1.9  2001/02/08 19:27:48  tsaf
 Changed so XML would know to use Controlled Vocab with the new curation flags.

 Revision 1.8  2000/12/22 22:33:25  lfoster
 Next attempt at fully serializing the XML "models".  Should be no refs remaining to the models. [I hope]

 Revision 1.7  2000/12/15 21:25:09  lfoster
 Synchronized the changes from past week of branch--back onto the trunk code for XML.


 */
