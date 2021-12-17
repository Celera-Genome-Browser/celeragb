package api.facade.concrete_facade.gff3;

import java.util.StringTokenizer;

import shared.util.GANumericConverter;
import api.stub.data.OID;
import api.stub.data.OIDGenerator;
import api.stub.data.ReservedNameSpaceMapping;

public class GffIdentifierOidHandler {
	/**
	 * Builds an OID with no special restrictions or translations,
	 * and can translate a GA number into an OID.
	 *
	 * @param String idstr of format "PREFIX:dddddddddd"
	 *   where the d's represent a decimal (long) number.
	 * @return OID an OID in either the specified or an unknown namespace.
	 */
	protected OID parseOIDorAlphaNum(String idstr, int genomeVersionId) {

		String[] strArrayForId = null;
		long oidLong = 0L;
		OID returnOID = null;

		char startchr = idstr.charAt(0);

		// Two possible formats: id="CCCC:ddddddddddd" or
		//    id="ddddddd".  First contains a namespace prefix,
		//    and the second is just the digits.
		//
		if(Character.isLetter(startchr)){
			strArrayForId = processIdStringForPrefix(idstr);

			if (strArrayForId[1].startsWith(GANumericConverter.GA_PREFIX)) {
				oidLong = GANumericConverter.getConverter().getOIDValueForGAName(strArrayForId[1]);
			}
			else if (Character.isDigit( strArrayForId[1].charAt( 0 ) ) ) {
				oidLong = Long.parseLong(strArrayForId[1]);    	
			}
			else {
				oidLong = strArrayForId[1].hashCode();
			}

			String namespacePrefix =
				ReservedNameSpaceMapping.translateToReservedNameSpace(strArrayForId[0]);

			returnOID = new OID(namespacePrefix,Long.toString(oidLong), genomeVersionId);

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
			returnOID = new OID(OID.UNKNOWN_NAMESPACE,Long.toString(oidLong), genomeVersionId);
		} // Found digit as first character.
		else {
			// System.err.println("ERROR: unexpected initial character in input OID "+idstr);
			throw new IllegalArgumentException("Invalid OID "+idstr+": this protocol expects a letter as first character");
		} // Found unexpected character as first character.
		return returnOID;
	} // End method: parseOIDorGA

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
	public String[] processIdStringForPrefix(String idstr){
		String[] rtnArray = idstr.split(":");
		String[] namespaceArray = new String[2];
		if ( rtnArray.length == 1 ) {
			namespaceArray[ 0 ] = Gff3_constants.NAME_SPACE_PREFIX;
			namespaceArray[ 1 ] = rtnArray[ 0 ];
		}
		else {
			namespaceArray[ 0 ] = rtnArray[ 0 ];
			namespaceArray[ 1 ] = rtnArray[ 1 ];
		}
		return namespaceArray;

	} // End method: processIdStringForPrefix

}
