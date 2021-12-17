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
package api.facade.concrete_facade.gff3.feature_facade;

import api.entity_model.management.PropertyMgr;
import api.entity_model.model.fundtype.AlignableGenomicEntity;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.annotations.FeatureFacade;
import api.facade.abstract_facade.annotations.HitAlignmentFacade;
import api.facade.abstract_facade.fundtype.GenomicEntityLoader;
import api.facade.concrete_facade.gff3.DataLoader;
import api.facade.concrete_facade.gff3.GenomeVersionSpace;
import api.facade.concrete_facade.shared.PropertySource;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.*;
import shared.util.DeflineParser;
import shared.util.GANumericConverter;

import java.util.*;

/**
 * This facade is the base for several other "API Facades" used by the XML
 * facade manager.  It generalizes the chores of handling loaders which can
 * contain read XML data, and properties creation and storage.
 */
public class Gff3GenomicFacade implements GenomicEntityLoader {

	private static String NULL_EDITING_CLASS_STRING = "";
	protected static final char UNKNOWN_BASE_LETTER = 'N';
	private static final String GA_NAME_PROP = "axis_name";

	private GenomeVersionSpace genomeVersionSpace = null;

	protected static final GenomicProperty[] EMPTY_GENOMIC_PROPERTY_ARRAY = new GenomicProperty[0];

	/**
	 * Allows external set of the genome version space: from whence all data comes.
	 */
	public void setGenomeVersionSpace(GenomeVersionSpace genomeVersionSpace) {
		this.genomeVersionSpace = genomeVersionSpace;
	} // End method

	/**
	 * @todo move the logic that exists in XmlFeatureFacade for handling
	 * alias retrieval to here.
	 */
	public GenomicEntityAlias[] getAliases( OID featureOID ) throws NoData
	{
		return new GenomicEntityAlias[0];
	}

	/**
	 * @todo move the logic that exists in XmlFeatureFacade for handling
	 * comment retrieval to here.
	 */
	public GenomicEntityComment[] getComments( OID featureOID ) throws NoData
	{
		return new GenomicEntityComment[0];
	}

	/**
	 * Returns the properties array associated with the oid.  Entity typ and
	 * the deep load flag are each ignored.
	 *
	 * @param genomicOID the OID of the object for which to get properties.
	 * @param dynamicType is the type of the object associated with genomicOID.
	 * @param deepLoad true means a full load, false means a lazy load.
	 */
	public GenomicProperty[] getProperties(OID genomicOid, EntityType dynamicType, boolean deepLoad) {
		Map inherittedMap = inheritProperties(genomicOid);
		GenomicProperty[] returnArray = new GenomicProperty[inherittedMap.size()];
		inherittedMap.values().toArray(returnArray);
		return returnArray;
	} // End method: getProperties

	/**
	 * Somewhere, the caller found an expandable property, and passed its name
	 * into here along with an OID.  This call will resolve this property
	 * to its next level of child properties.
	 *
	 * @param OID the OID of the object which has the property.
	 * @param String propertyName the name of the property to expand.
	 * @param dynamicType is the type of the object inputting its OID.
	 * @param deepLoad true is a full load, false is a lazy load.
	 * @return GenomicPropery[] the child properties of the property.
	 */
	public GenomicProperty[] expandProperty(OID genomicOID, String propertyName, EntityType dynamicType, boolean deepLoad)
	throws NoData {

		GenomicProperty[] firstProperties = EMPTY_GENOMIC_PROPERTY_ARRAY;
		GenomicProperty[] laterProperties = null;

		DataLoader loader = null;
		PropertySource propertySource = null;
		List childList = null;
		List sourceList = null;

		// Will find all subproperties from all loaders with 'knowledge' of the feature
		// and will take the in first-found take precedence over later, in the event
		// of any clashes of property names.
		for (Iterator it = getGff3LoadersForFeature(genomicOID).iterator(); it.hasNext(); ) {
			loader = (DataLoader)it.next();
			sourceList = loader.getPropertySources(genomicOID);
			for (Iterator sources = sourceList.iterator(); (childList == null) && it.hasNext(); ) {
				propertySource = (PropertySource)sources.next();

				// Now, resolve the list of subproperties, given the name.
				childList = propertySource.findChildrenFor(propertyName);
				laterProperties = buildGenomicPropertiesFromPropertySources(childList);

				firstProperties = concatUniquelyNamed(firstProperties, laterProperties);
			} // For all iterations
		} // Loader associated.

		// Get the properties array, one way or another.
		if (firstProperties.length == 0) {

			firstProperties = createFakeProperties(genomicOID);

		} // Not found or created.

		return firstProperties;

	} // End method: expandProperty

	//--------------------------------------------HELPER METHODS
	/**
	 * Gives subclasses access to the genome version space.
	 */
	protected GenomeVersionSpace getGenomeVersionSpace() {
		return genomeVersionSpace;
	} // End method

	/**
	 * Retruns all data sources lying in the genome version whose id is given,
	 * AND which may represent axes.
	 */
	protected Iterator getDataSourcesRepresentingGenomicAxes(int versionID) {
		if (genomeVersionSpace == null)
			throw new IllegalStateException("No genome version space in XML for facade "+this.getClass().getName());
		return genomeVersionSpace.getDataSourcesRepresentingGenomicAxes(versionID);
	} // End method

	/**
	 * Returns all sources of features and contigs which SHOULD be queried
	 * given the current state of the XML facade.
	 */
	protected List<DataLoader> getFeatureSourcesInGenomeVersion(int versionID) {
		if (genomeVersionSpace == null)
			throw new IllegalStateException("No genome version space in XML for facade "+this.getClass().getName());
		//return genomeVersionSpace.getGenomicAxisInputSources(versionID);
		return genomeVersionSpace.getSearchableInputSources(versionID);
	} // End method

	/**
	 * Returns all possible sources of searchable data, regardless of
	 * the current state of the XML facade.
	 */
	protected Iterator getSearchSourcesInGenomeVersion(int versionID) {
		if (genomeVersionSpace == null)
			throw new IllegalStateException("No genome version space in XML for facade "+this.getClass().getName());
		return genomeVersionSpace.getSearchableInputSources(versionID).iterator();
	} // End method

	/**
	 * Returns iterator for all loaders with knowledge of the feature whose OID is given.
	 */
	protected List<DataLoader> getGff3LoadersForFeature(OID featureOID) {
		ArrayList<DataLoader> list = new ArrayList<DataLoader>();

		for(DataLoader nextLoader: getFeatureSourcesInGenomeVersion(featureOID.getGenomeVersionId()) ) {
			if ((nextLoader != null) && nextLoader.featureExists(featureOID))
				list.add(nextLoader);

		} //for

		return list;

	} // End method

	/**
	 * Returns all loaders which have features aligning to the
	 * genomic axis whose OID is given as parameter.
	 *
	 * @param OID genomicAxisOID OID of alignment.
	 * @return Iterator over all relevant loaders.
	 */
	protected List<DataLoader> getGff3LoadersForGenomicAxis(OID genomicAxisOID){

		List<DataLoader> loadersForGenomicAxis = new ArrayList<DataLoader>(); // there can be multiple feature files with the same genomic axis OID
		DataLoader nextLoader = null;

		for (Iterator<DataLoader> it = genomeVersionSpace.getGenomicAxisInputSources(genomicAxisOID.getGenomeVersionId()); it.hasNext(); ) {
			nextLoader = it.next();
			if (nextLoader.getReferencedOIDSet().contains(genomicAxisOID))
				loadersForGenomicAxis.add(nextLoader);

		} //for
		return loadersForGenomicAxis;

	} // End method

	/**
	 * Navigation needs to have the OID of a genome version.  Return null
	 * if it is not found in the space.
	 */
	protected OID getSourcedGenomeVersionOidForId(int genomeVersionId) {
		GenomeVersion[] sourcedGenomeVersions = genomeVersionSpace.getGenomeVersions();
		if ((sourcedGenomeVersions == null) || (sourcedGenomeVersions.length == 0))
			return null;

		OID returnOid = null;
		for (int i = 0; (i < sourcedGenomeVersions.length) && (returnOid == null); i++) {
			if (sourcedGenomeVersions[i].getOid().getGenomeVersionId() == genomeVersionId)
				returnOid = sourcedGenomeVersions[i].getOid();
		} // For all sourced versions

		return returnOid;
	} // End method

	/**
	 * Tells if the genome version is worth searching.  There may be no features
	 * or sourced axes known to this facade.
	 */
	protected boolean isGenomeVersionAvailableToSearch(int genomeVersionId) {
		return genomeVersionSpace.hasSearchableInputSourcesInGenomeVersion(genomeVersionId);
	} // End method

	/**
	 * If this axis is found to be "known" to the genome version as being originated
	 * from it, then generate properties.
	 * Produces a new set of properties for the OID given.  This consists
	 * of a single property based on the OIDs identifying number.  Also
	 * makes a property calculated from the OID.
	 */
	protected GenomicProperty[] generatePropertiesForGenomicAxisOID(OID genomicAxisOID) {
		int genomeVersionIdOfAxisOid = genomicAxisOID.getGenomeVersionId();
		if (getSourcedGenomeVersionOidForId(genomeVersionIdOfAxisOid) == null)
			return EMPTY_GENOMIC_PROPERTY_ARRAY;

		// Checks whether a genomic axis loader was made, with this OID as its axis.
		Iterator it = genomeVersionSpace.getInputSourcesForAxisOID(genomicAxisOID);
		if (! it.hasNext())
			return EMPTY_GENOMIC_PROPERTY_ARRAY;

		String idstr;
		idstr = genomicAxisOID.getIdentifierAsString();

		if (genomicAxisOID.isInternalDatabaseOID())
			return new GenomicProperty[] {
				// A property may be generated from the OID's string.
				new GenomicProperty(GA_NAME_PROP, NULL_EDITING_CLASS_STRING,
						GANumericConverter.getConverter().getGANameForOIDSuffix(idstr),
						false, ControlledVocabUtil.getNullVocabIndex()),
						// ...and another is directly using that string.
						new GenomicProperty(FeatureFacade.GENOMIC_AXIS_ID_PROP, NULL_EDITING_CLASS_STRING,
								idstr, false, ControlledVocabUtil.getNullVocabIndex())
		}; // End Property array definition.
		else
			return new GenomicProperty[] {
				new GenomicProperty(FeatureFacade.GENOMIC_AXIS_ID_PROP, NULL_EDITING_CLASS_STRING,
						idstr, false, ControlledVocabUtil.getNullVocabIndex())
		}; // End Property array definition.

	} // End method

	/**
	 * Produces a new set of properties for the OID given.
	 * This method is set "protected" so it may be called by subclasses!
	 */
	protected GenomicProperty[] generatePropertiesForOID(OID genomicOID) {
		String idstr;
		if (genomicOID == null) {
			idstr = "NO ID";
		}
		else {
			idstr = genomicOID.getIdentifierAsString();
		}
		GenomicProperty idprop =
			new GenomicProperty(FeatureFacade.FEATURE_ID_PROP, NULL_EDITING_CLASS_STRING,
					idstr, false, ControlledVocabUtil.getNullVocabIndex());
		idprop.setIsComputed(true);

		GenomicProperty[] properties = new GenomicProperty[1];
		properties[0] = idprop;
		return properties;
	} // End method: generatePropertiesForOID

	/**
	 * Takes a list of properties and an array of properties, and constructs
	 * a single array from the two--in which properties named in first list
	 * override any of the same name in second.
	 */
	protected GenomicProperty[] concatUniquelyNamed(GenomicProperty[] hiPriorityProperties,
			GenomicProperty[] loPriorityProperties) {

		Map propsMap = new HashMap();
		List finalProperties = new ArrayList();

		propsMap = new HashMap();
		for (int i = 0; i < hiPriorityProperties.length; i++) {
			propsMap.put(hiPriorityProperties[i].getName(), hiPriorityProperties[i]);
			finalProperties.add(hiPriorityProperties[i]);
		} // For all hi-pris.

		// Add any members of second array that do not conflict with members
		// of the first.
		for (int i = 0; i < loPriorityProperties.length; i++) {
			if (! propsMap.containsKey(loPriorityProperties[i].getName()))
				finalProperties.add(loPriorityProperties[i]);
		} // For all lo-pris.

		GenomicProperty[] returnArray = new GenomicProperty[finalProperties.size()];
		finalProperties.toArray(returnArray);
		return returnArray;

	} // End method: concatUniquelyNamed

	/** Creates props in a fashion that can be passed down to subclasses. */
	protected Map inheritProperties(OID genomicOid) {
		Map returnProperties = new HashMap();

		// Collect props from all loaders, but keep last prop if same prop name.
		DataLoader loader = null;
		List propertyList = new ArrayList();
		int loaderCount = 0;
		for (Iterator<DataLoader> it = getGff3LoadersForFeature(genomicOid).iterator(); it.hasNext(); ) {
			loader = (DataLoader)it.next();
			propertyList = loader.getPropertySources(genomicOid);
			addFromGenomicPropertySources(propertyList, returnProperties);
			loaderCount ++;
		} // For all found loaders.

		// Generate OID-associated props, but only if this entity was known to one
		// or more loaders.
		if (loaderCount > 0) {
			GenomicProperty[] generalProperties = generatePropertiesForOID(genomicOid);
			for (int i = 0; i < generalProperties.length; i++) {
				returnProperties.put(generalProperties[i].getName(), generalProperties[i]);
			} // For all general props.
		} // Need to supply properties.

		return returnProperties;

	} // End method: inheritProperties

	/** Util function to add props to map, keyed by name. */
	protected void addPropertyListToMap(List propertyList, Map returnMap) {
		GenomicProperty nextProp = null;
		for (Iterator it = propertyList.iterator(); it.hasNext(); ) {
			nextProp = (GenomicProperty)it.next();
			returnMap.put(nextProp.getName(), nextProp);
		} // For all iterations
	} // End method: addPropertyListToMap

	/** Adds all property-source-generated genomic properties to the return map. */
	private void addFromGenomicPropertySources(List propertySources, Map propertyReturnMap) {
		PropertySource nextPropertySource = null;

		for (Iterator it = propertySources.iterator(); it.hasNext(); ) {
			nextPropertySource = (PropertySource)it.next();
			//System.out.println("Adding property "+nextPropertySource.getName()+" "+nextPropertySource.createGenomicProperty());
			propertyReturnMap.put(nextPropertySource.getName(), nextPropertySource.createGenomicProperty(false));
		} // For all iterations.

	} // End method: addFromGenomicPropertySources

	/**
	 * Helper method to aid in adjusting from a list of one type to an array of another.
	 */
	private GenomicProperty[] buildGenomicPropertiesFromPropertySources(List propertySources) {

		// Dump out if nothing found.
		if (propertySources == null)
			return EMPTY_GENOMIC_PROPERTY_ARRAY;

		PropertySource nextPropertySource = null;
		GenomicProperty[] returnProps = new GenomicProperty[propertySources.size()];

		// Make a genomic property from each source, and place that into the
		// return array.
		int propCount = 0;
		for (Iterator it = propertySources.iterator(); it.hasNext(); ) {
			nextPropertySource = (PropertySource)it.next();
			returnProps[propCount++] = nextPropertySource.createGenomicProperty(false);
		} // For all iterations.

		return returnProps;
	} // End method: buildGenomicPropertiesFromPropertySources

	/** Simple helper for subclasses to avoid re-declaring same string over. */
	protected String getNullEditingClassString() { return NULL_EDITING_CLASS_STRING; }

	/** Given an Oid, find its assembly version through the facade mgr. */
	protected String getAssemblyVersion(OID genomicOid) {
		if (genomicOid == null)
			return null;
		GenomeVersionInfo info = FacadeManager.getGenomeVersionInfo(genomicOid.getGenomeVersionId());
		return info.getAssemblyVersionAsString();
	} // End method

	protected GenomicProperty[] createFakeProperties(OID genomicOID) {
		GenomicProperty[] properties = new GenomicProperty[2];
		GenomicProperty propA =
			new GenomicProperty("propA", NULL_EDITING_CLASS_STRING,
					String.valueOf(Math.random()), false, ControlledVocabUtil.getNullVocabIndex());
		properties[0] = propA;
		GenomicProperty propB =
			new GenomicProperty("propB", NULL_EDITING_CLASS_STRING,
					String.valueOf(Math.random()), false, ControlledVocabUtil.getNullVocabIndex());
		properties[1] = propB;
		return properties;
	}

	/** Make a set of unknown bases to return for residues request. */
	protected String createUnknownResidues(OID genomicOID, int startPos, int endPos) {
		int length = Math.abs(endPos - startPos) + 1;
		StringBuffer buf = new StringBuffer(length);
		for (int i=0; i < length; i++) {
			buf.append(UNKNOWN_BASE_LETTER);
		}
		return buf.toString();
	} // End method: createUnknownResidues

	/**
	 * Factory method to make the gene accession property the same way for
	 * multiple subclassing API facades.
	 *
	 * @param String propertyName the name needed for the prop.  Different
	 *    callers will wish to name it according to different abstract facade standards.
	 * @param String initialValue the value to be given.
	 * @return GenomicProperty final created property.  Should provide means of editing the gene.
	 */
	protected GenomicProperty createGeneAccessionProperty(String propertyName, String initialValue) {
		return new GenomicProperty(
				/* name */             propertyName,
				/* editing class */    /*"client.gui.components.annotation.ga_gene_curation.GeneCuration"*/"",
				/* initial value */    initialValue,
				/* editable */         false,
				/* vocab index */      ControlledVocabUtil.getNullVocabIndex());
	} // End method: createGeneAccessionProperty

	/**
	 * Convenience method for making property objects with the most common settings.
	 *
	 * @param String propertyName the name needed for the prop.  Different
	 *    callers will wish to name it according to different abstract facade standards.
	 * @param String initialValue the value to be given.
	 * @return GenomicProperty final created property.  Should provide means of editing the gene.
	 */
	protected GenomicProperty createDefaultSettingsProperty(String propertyName, String initialValue) {
		PropertyMgr propertyMgr = PropertyMgr.getPropertyMgr();
		return propertyMgr.constructPropertyFromScratch(propertyName, initialValue);
	} // End method: createDefaultSettingsProperty

	/**
	 * Uses the properties manager to generate properties, thus ensuring proper
	 * settings are made for get/set, rendering/editing, expandable/editable, and
	 * vocab (etc)
	 */
	protected GenomicProperty createWithPropsMgr(String propertyName, AlignableGenomicEntity entity, String initialValue) {
		if (initialValue == null)
			initialValue = "";
		PropertyMgr propertyMgr = PropertyMgr.getPropertyMgr();
		GenomicProperty baseProperty = createDefaultSettingsProperty(propertyName, initialValue);
		if (entity == null)
			return baseProperty;
		else
			return propertyMgr.handleProperty(  baseProperty,
					PropertyMgr.NEW_ENTITY, entity);
	} // End method

	/**
	 * Returns a subject definition if one exists in the string provided
	 * and for the OID provided.
	 *
	 * @param String description to search for the subject definition.
	 * @return SubjectDefinition
	 */
	protected SubjectDefinition makeSubjectDefinition(String description) {

		// Pull in any description line, and find its "start" marker.
		SubjectDefinition returnDefinition = null;
		if (description != null) {
			DeflineParser descriptionParser = new DeflineParser(description);
			if (descriptionParser.isInternalDefline()) {

				String subjDefStr = descriptionParser.get(descriptionParser.DEF_NAME);
				if (subjDefStr == null)
					subjDefStr = "";

				String orgStr = descriptionParser.get(descriptionParser.ORGANISM_NAME);
				if (orgStr == null)
					orgStr = "";

				String authority = null;
				String accession = null;
				Iterator it = descriptionParser.getAllAltIds();
				if (it.hasNext()) {
					authority = (String)it.next();
				} // Have an authority.
				else
					authority = "";

				if (it.hasNext()) {
					accession = (String)it.next();
				} // Have an accession.
				else
					accession = "";

				if (subjDefStr != null) {
					returnDefinition = new SubjectDefinition
					(
							(short)0,                   // Defin. order
							accession,                  // Accession
							authority,                  // Athority
							orgStr,                     // Species
							subjDefStr                 // Description
					);
				} // Found Value
			} // Right kind of description.
		} // Got one

		return returnDefinition;

	} // End method: getSubjectDefinition

	/**
	 * Reformats descriptive text into format expected for end-user presentation.
	 */
	protected String formatDescription(String descriptionRawText) {
		// If this is this browser's standard format, break it up and reformat, other
		// wise just return raw.
		DeflineParser descriptionParser = new DeflineParser(descriptionRawText);
		if (! descriptionParser.isInternalDefline()) {
			return descriptionRawText;
		} // Not parseable.

		StringBuffer returnValue = new StringBuffer(descriptionRawText.length());

		String defValue = descriptionParser.get(descriptionParser.DEF_NAME);
		String altValue = descriptionParser.get(descriptionParser.ALTERNATE_ID_NAME);
		String orgValue = descriptionParser.get(descriptionParser.ORGANISM_NAME);

		if (altValue != null) {
			returnValue.append("/altid=");
			for (Iterator it = descriptionParser.getAllAltIds(); it.hasNext(); ) {
				returnValue.append((String)it.next());
				returnValue.append(" ");
			} // For all alternative ids.
		} // Non-null alt value.

		if (defValue != null) {
			returnValue.append("/def=");
			returnValue.append(defValue.trim());
		} // Non-null def value.

		if (orgValue != null) {
			returnValue.append("[/org=");
			returnValue.append(orgValue.trim());
			returnValue.append("]");
		} // Non-null org value.

		return returnValue.toString();
	} // End method: formatDescription

	/** Create properties for alternative accession and accession. */
	protected GenomicProperty createAltAccProp(String description, Map returnMap) {
		DeflineParser descriptionParser = new DeflineParser(description);
		String altAccession = descriptionParser.getDefId();
		GenomicProperty altAccessionProp = null;
		if (altAccession != null) {
			altAccessionProp = createDefaultSettingsProperty(HitAlignmentFacade.ALT_ACCESSION_PROP, altAccession);
			returnMap.put(altAccessionProp.getName(), altAccessionProp);
		} // Got accession.

		return altAccessionProp;
	} // End method: createAltAccProp

	protected GenomicProperty createAccProp(String description, Map returnMap) {
		DeflineParser descriptionParser = new DeflineParser(description);

		String authority = null;
		String accession = null;

		Iterator it = descriptionParser.getAllAltIds();
		if (it.hasNext()) {
			authority = (String)it.next();
		} // Have an authority.
		else
			authority = "";

		if ( authority == null ) {
			authority = "";
		}

		if (it.hasNext())
			accession = (String)it.next();

		GenomicProperty accessionProp = null;
		if (accession != null) {
			accessionProp = createDefaultSettingsProperty(HitAlignmentFacade.ACCESSSION_NUM_PROP, accession);
			returnMap.put(accessionProp.getName(), accessionProp);
		} // Got accession.

		return accessionProp;
	} // End method: createAccProp

	/**
	 * Returns subject sequence length property value, which it calculates from value
	 * retreived from the loader given for the feature whose OID was given.
	 */
	protected String getSubjectSequenceLength(DataLoader featureLoader, OID featureOid) {
		String returnString = null;
		for (Iterator it = featureLoader.getSubjectSequenceOids(featureOid).iterator(); it.hasNext(); ) {
			OID subjectSequenceOid = (OID)it.next();
			returnString = featureLoader.getSequenceLength(subjectSequenceOid);
		} // For all iterations.

		return returnString;

	} // End method: getSubjectSequenceLength

} // End class: XmlGenomicFacade
