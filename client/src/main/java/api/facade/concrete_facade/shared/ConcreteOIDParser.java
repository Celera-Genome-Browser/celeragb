package api.facade.concrete_facade.shared;

import java.util.StringTokenizer;

import shared.util.GANumericConverter;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.OID;
import api.stub.data.OIDGenerator;
import api.stub.data.ReservedNameSpaceMapping;

/**
 * Knows how to create a proper OID as required by the loaders.
 * 
 * @author Leslie L Foster
 */
public class ConcreteOIDParser implements OIDParser {
	private int mGenomeVersionId;
	
	/** Setter to allow external push in of the genome version's id. */
	public void setGenomeVersionId( int genomeVersionId ) {
		mGenomeVersionId = genomeVersionId;
	}
	
	/**
	 * Builds a genomic axis OID with all restrictions and translations required by
	 * this loader. (misnamed method)
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
	@Override
	public OID parseContigOID(String idstr) {

		OID returnOID = parseOIDorGA(idstr);

		// Enforce the restriction that the OID be non-scratch.
		if (returnOID.isScratchOID()) {
			returnOID = null;
			FacadeManager.handleException(
					new IllegalArgumentException("Illegal namespace contig ID "+idstr+": entered in XML file."));
		} // Test for scratch

		return returnOID;
	} // End method: parseContigOIDTemplateMethod

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
	@Override
	public OID parseFeatureOID(String idstr) {

		OID returnOID = parseOIDGeneric(idstr);

		// Enforce the restriction that the OID be non-scratch.
		if (returnOID.isScratchOID()) {
			returnOID = null;
			FacadeManager.handleException(
					new IllegalArgumentException("Illegal namespace feature ID "+idstr+": entered in XML file."));
		} // Test for scratch

		return returnOID;
	} // End method: parseFeatureOIDTemplateMethod

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
	@Override
	public OID parseEvidenceOID(String idstr) {

		OID returnOID = parseOIDGeneric(idstr);

		// Enforce the restriction that the OID be non-scratch.
		if (returnOID.isScratchOID()) {
			returnOID = null;
			FacadeManager.handleException(
					new IllegalArgumentException("Illegal namespace evidence ID "+idstr+": entered in XML file."));
		} // Test for scratch

		return returnOID;
	} // End method: parseEvidenceOIDTemplateMethod

	/**
	 * Builds an OID with no special restrictions or translations.
	 *
	 * If no special restrictions are required in a subclass for
	 * a particular type of OID handling, this method may be called
	 * by the parseXxxxxxOIDTemplateMethod implementation.
	 *
	 * @param String idstr of format "PREFIX:dddddddddd"
	 *   where the d's represent a decimal (long) number.
	 * @return OID an OID in either the specified or an unknown namespace.
	 */
	@Override
	public OID parseOIDGeneric(String idstr) {

		String[] strArrayForId = null;
		OID returnOID = null;

		char startchr = idstr.charAt(0);

		// Two possible formats: id="CCCC:ddddddddddd" or
		//    id="ddddddd".  First contains a namespace prefix,
		//    and the second is just the digits.
		//
		if(Character.isLetter(startchr)){
			strArrayForId=processIdStringForPrefix(idstr);
			String oid = strArrayForId[1];
			String namespacePrefix =
				ReservedNameSpaceMapping.translateToReservedNameSpace(strArrayForId[0]);
			returnOID = new OID(namespacePrefix, oid, getGenomeVersionId());

			// Need a running counter for certain kinds of OIDs.
			if (returnOID.isScratchOID()) {
				// Test this read OID against the current highest OID, and make it the
				// new highest if it is higher.
				try {
					OIDGenerator.getOIDGenerator().setInitialValueForNameSpace
					(OID.SCRATCH_NAMESPACE, Long.toString(1 + returnOID.getIdentifier()));
				} // End try block
				catch (IllegalArgumentException iae) {
					// Ignoring here: we only wish to seed the generator with highest known value.
				} // End catch block for seed.
			} // Found another scratch.

		} // Proper alphabetic prefix.
		//else if (Character.isDigit(startchr)) {
		//oidlong = Long.parseLong(idstr);
		//returnOID = new OID(OID.UNKNOWN_NAMESPACE,Long.toString(oidlong), getGenomeVersionId());
		//} // Found digit as first character.
		else {
			if (idstr.indexOf(':') >= 0) {
				FacadeManager.handleException( new IllegalArgumentException( "This application is expecting a namespace prefix beginning with an alphabetic character in its XML IDs.\nYou specified '"
						+idstr+"'."));
			} // Prefix invalid
			else {
				// NOTE: as of 5/7/2001, we found that the EJB/db are getting confused
				//       about non-internal OIDs.  TO fix this, we are precluding non-
				//       internal database OIDs from being sent there.  Unfortunately,
				//       this also requires no-prefix OIDs no longer be accepted.
				returnOID = new OID(OID.INTERNAL_DATABASE_NAMESPACE, idstr, getGenomeVersionId());
				//	        JCVI LLF, 10/20/2006
				//	        FacadeManager.handleException( new IllegalArgumentException( "This application is expecting a namespace prefix in its XML IDs.\nYou specified '"
				//	                                            +idstr+"'.\nIf this is an internal database ID, please change that to 'INTERNAL:"
				//	                                            +idstr+"'.\nIf not, prefix it with a namespace of your own."
				//	                                            +((getLoadedFileNames().length >= 0) ? "\nSee "+getLoadedFileNames()[0] : "")
				//	                                            ));
			} // No prefix at all.
			// oidlong=Long.parseLong(idstr);
			// returnOID=new OID(OID.UNKNOWN_NAMESPACE,Long.toString(oidlong));
			// throw new IllegalArgumentException("Invalid OID "+idstr+": expecting a letter as first character.  Example:");
		} // Found unexpected character as first character.

		return returnOID;
	} // End method: parseOIDGeneric

	/**
	 * Builds an OID with no special restrictions or translations,
	 * and can translate a GA number into an OID.
	 *
	 * @param String idstr of format "PREFIX:dddddddddd"
	 *   where the d's represent a decimal (long) number.
	 * @return OID an OID in either the specified or an unknown namespace.
	 */
	protected OID parseOIDorGA(String idstr) {

		String[] strArrayForId = null;
		long oidLong = 0L;
		OID returnOID = null;

		char startchr = idstr.charAt(0);

		// Two possible formats: id="CCCC:ddddddddddd" or
		//    id="ddddddd".  First contains a namespace prefix,
		//    and the second is just the digits.
		//
		if(Character.isLetter(startchr)){
			strArrayForId=processIdStringForPrefix(idstr);

			if (strArrayForId[1].startsWith(GANumericConverter.GA_PREFIX))
				oidLong = GANumericConverter.getConverter().getOIDValueForGAName(strArrayForId[1]);
			else
				oidLong = Long.parseLong(strArrayForId[1]);

			String namespacePrefix =
				ReservedNameSpaceMapping.translateToReservedNameSpace(strArrayForId[0]);

			returnOID = new OID(namespacePrefix,Long.toString(oidLong), getGenomeVersionId());

			// Need a running counter for certain kinds of OIDs.
			if (returnOID.isScratchOID()) {
				// Test this read OID against the current highest OID, and make it the
				// new highest if it is higher.
				try {
					OIDGenerator.getOIDGenerator().setInitialValueForNameSpace(OID.SCRATCH_NAMESPACE,Long.toString(++oidLong));
				} // End try block.
				catch (IllegalArgumentException iae) {
					// Ignoring here: we only wish to seed the generator with highest known value.
				} // End catch block for seed.
			} // Found another scratch.

		} // Proper alphabetic prefix.
		else if (Character.isDigit(startchr)) {
			oidLong = Long.parseLong(idstr);
			returnOID = new OID(OID.UNKNOWN_NAMESPACE,Long.toString(oidLong), getGenomeVersionId());
		} // Found digit as first character.
		else {
			// System.err.println("ERROR: unexpected initial character in input OID "+idstr);
			throw new IllegalArgumentException("Invalid OID "+idstr+": this protocol expects a letter as first character");
		} // Found unexpected character as first character.
		return returnOID;
	} // End method: parseOIDorGA

	/**
	 * Returns genome version id.  May be overridden from subclass.
	 */
	protected int getGenomeVersionId() {
		return mGenomeVersionId;
	} // End method

	/**
	 * Primarily a 'facility' for use of a parseXxxxOIDTemplateMethod,
	 * this method breaks up an id= attribute value into the
	 * pieces needed to build an Object ID.  Should remain protected
	 * visibility so that implementing subclasses of this abstract
	 * class, can call this method!
	 *
	 * @param String idstr The id= attribute value
	 * @return String[] first member is namespace prefix,
	 *   the second member is the set of digits, which should
	 *   make the OID unique in the name space.
	 */
	protected String[] processIdStringForPrefix(String idstr){
		StringTokenizer s=new StringTokenizer(idstr,":");
		String[] nameSpaceIdarray=new String[2];
		nameSpaceIdarray[0]=s.nextToken();
		nameSpaceIdarray[1]=s.nextToken();
		return nameSpaceIdarray;

	} // End method: processIdStringForPrefix

}
