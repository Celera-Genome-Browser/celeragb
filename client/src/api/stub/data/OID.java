// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Copyright (c) 1999 - 2006 Applera Corporation.
// 301 Merritt 7 
// P.O. Box 5435 
// Norwalk, CT 06856-5435 USA
//
// This is free software; you can redistribute it and/or modify it under the 
// terms of the GNU Lesser General Public License as published by the 
// Free Software Foundation; version 2.1 of the License.
//
// This software is distributed in the hope that it will be useful, but 
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License 
// along with this software; if not, write to the Free Software Foundation, Inc.
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package api.stub.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Vector;


public class OID implements java.io.Serializable, Cloneable
{
    public static final String INTERNAL_DATABASE_NAMESPACE="$INTERNAL_DATABASE";
    public static final String SCRATCH_NAMESPACE="$SCRATCH";
    public static final String CLIENT_GENERATED_NAMESPACE="$CLIENT_GENERATED";
    public static final String SERVER_GENERATED_NAMESPACE="$SERVER_GENERATED";
    public static final String API_GENERATED_NAMESPACE="$API_GENERATED";
    public static final String UNKNOWN_NAMESPACE="$UNKNOWN";
    public static final short NULL_GENOME_VER_IDENTIFIER=0;
    private static final int MAX_NAMESPACES=Short.MAX_VALUE*2+1;

    //18446744073709551615 is 2^64-1 or Max Unsigned Long
    private static final BigInteger MAX_IDENTIFIER =
        new BigInteger("18446744073709551615");
    private static final Vector nameSpaceTable=new Vector();
    private static final int NULL_OID_IDENTIFIER=0;

    private long identifier;
    private short nameSpaceNumber;

    // NOTE: Implementation decision rational
    // This has to be an int if we want to read from the database
    // rather than from a file as that is what hash code returns
    // and we want to be able to read assemblies from databases
    // and always get the same hash code so that bookmarks will work
    // The only other solution is to have an external file that maintains a
    // unique identifier for each genome version. Keeping this up to date
    // and in sync would be a major problem. Memory requirements might override
    // this...
    private int genomeVersionIdentifier;

//*********** Class level methods ***********
    static {
       nameSpaceTable.add(INTERNAL_DATABASE_NAMESPACE);
       nameSpaceTable.add(SCRATCH_NAMESPACE);
       nameSpaceTable.add(CLIENT_GENERATED_NAMESPACE);
       nameSpaceTable.add(SERVER_GENERATED_NAMESPACE);
       nameSpaceTable.add(API_GENERATED_NAMESPACE);
       nameSpaceTable.add(UNKNOWN_NAMESPACE);
    }

    private static int convertNameSpaceNumberToIndex(short nameSpaceNumber) {
      return nameSpaceNumber-Short.MIN_VALUE;
    }

    private static short convertIndexToNameSpaceNumber(int index) {
      return (short)(Short.MIN_VALUE+index);
    }

    private static short getNameSpaceNumberExceptionIfNotFound(String nameSpace) {
        int index=nameSpaceTable.indexOf(nameSpace);
        if (index>-1) return convertIndexToNameSpaceNumber(index);
        else throw new IllegalArgumentException("NameSpace "+nameSpace+"not found");
    }

    private static short getNameSpaceNumberAddIfNotFound(String nameSpace) {
        int index=nameSpaceTable.indexOf(nameSpace);
        if (index>-1) return convertIndexToNameSpaceNumber(index);
        else {
           if (nameSpaceTable.size()==MAX_NAMESPACES)
              throw new IllegalStateException("OID cannot hold more than "+
                  MAX_NAMESPACES+" name spaces. Failed to create new name space.");
           nameSpaceTable.add(nameSpace);
           return convertIndexToNameSpaceNumber(nameSpaceTable.size()-1);
        }
    }

    private static String getNameSpaceString (short nameSpaceNumber) {
       return (String) nameSpaceTable.elementAt(convertNameSpaceNumberToIndex(nameSpaceNumber));
    }

//***************  Instance level methods ********

    public OID() {
      this(
        INTERNAL_DATABASE_NAMESPACE,
        Integer.toString(NULL_OID_IDENTIFIER),
        NULL_GENOME_VER_IDENTIFIER);
    }

//  Removed - null Genome Version ID causes significant problems
//    we are removing this constructor to break code that creates null GV ids
//    PED 6/20/01
//    public OID (String identifier) {
//      this(INTERNAL_DATABASE_NAMESPACE, identifier, NULL_GENOME_VER_IDENTIFIER);
//    }

    public OID (String identifier, int genVerId) {
      this(INTERNAL_DATABASE_NAMESPACE, identifier, genVerId);
    }

//  Removed - null Genome Version ID causes significant problems
//    we are removing this constructor to break code that creates null GV ids
//    PED 6/20/01
//    public OID (String nameSpace, String identifier)
//    {
//      this(nameSpace, identifier, NULL_GENOME_VER_IDENTIFIER);
//    }

    public OID (String nameSpace, String identifier, int genVerId) {
      // Need to convert an UNSIGNED long value represented as a string
      // into a SIGNED long which will then be used to construct a new
      // OID and return it
      BigInteger tempBigInt;
      try {
         tempBigInt = new BigInteger(identifier.trim());
      }
      catch (NumberFormatException nfEx) {  //throw new exception is we get a string
         throw new IllegalArgumentException ("Identifier: "+identifier+" passed "+
            "to the constructor of OID must be an Integer in range 0 - 18446744073709551615 (2^64-1).");
      }
      if (tempBigInt.signum()==-1) throw new IllegalArgumentException ("Identifier: "+identifier+" passed "+
          "to the constructor of OID must be an Integer in range 0 - 18446744073709551615 (2^64-1).");

      if (tempBigInt.compareTo(MAX_IDENTIFIER)>0) throw new IllegalArgumentException ("Identifier: "+identifier+" passed "+
          "to the constructor of OID must be an Integer in range 0 - 18446744073709551615 (2^64-1).");

      this.identifier = tempBigInt.longValue();
      nameSpace=nameSpace.toUpperCase();
      nameSpaceNumber=getNameSpaceNumberAddIfNotFound(nameSpace);
      genomeVersionIdentifier = genVerId;
    }


    /**
     * Support cloning.
     */
    public Object clone() {
      try {
        OID myClone;
        myClone = (OID)super.clone();
        // myClone.displayName = new String(this.displayName);
        // ...
        myClone.identifier = this.identifier;
        myClone.nameSpaceNumber = this.nameSpaceNumber;
        myClone.genomeVersionIdentifier = this.genomeVersionIdentifier;
        return myClone;
      }
      catch (CloneNotSupportedException e) {
        throw new InternalError(e.toString());
      }
    }


    public String getIdentifierAsString() {
      // If the OID is positive then it can be translated directly
      long longVal = this.getIdentifier();
      if (longVal >= 0) {
        return Long.toString(longVal);
      }
      else {
        String hexStr = Long.toHexString(longVal);
        BigInteger big = new BigInteger(hexStr,16);
        return big.toString();
      }
    }

    public String getNameSpaceAsString(){
      return getNameSpaceString(this.getNameSpaceNumber());
    }

    public int hashCode()
    {
      return (int)getIdentifier();
    }

    public boolean equals (Object otherOID) {
      if (otherOID instanceof OID)
      {
        return (((OID)otherOID).getIdentifier() == this.getIdentifier() &&
                ((OID)otherOID).getNameSpaceNumber() == this.getNameSpaceNumber() &&
                ((OID)otherOID).genomeVersionIdentifier == this.genomeVersionIdentifier);
      }
      else
      {
        return false;
      }
    }


    public String toString() {
      return ReservedNameSpaceMapping.translateFromReservedNameSpace(this.getNameSpaceAsString())+":"+this.getIdentifierAsString();
    }

    /** Return the internal value as a BigDecimal.
     *  Useful for setting an OID value in a JDBC CallableStatement
     *  in order to take care of sign problems.
     */
    public BigDecimal toBigDecimal() {
      return new BigDecimal(this.getIdentifierAsString());
    }

    public boolean isNull() {
     return (getIdentifier()==NULL_OID_IDENTIFIER &&
             isInternalDatabaseOID());
    }

    public boolean isNullGenomeVersionId() {
       return genomeVersionIdentifier==NULL_GENOME_VER_IDENTIFIER;
    }

    public void setGenomeVersionIdIfNull(int id) {
       if (!isNullGenomeVersionId()) throw new
          IllegalStateException("You cannot change the GenomeVersionID, unless "+
           "it is null!");
       genomeVersionIdentifier=id;
    }

    public void setGenomeVersionIdToNull() {
       genomeVersionIdentifier=NULL_GENOME_VER_IDENTIFIER;
    }

    public boolean isInternalDatabaseOID() {
       return getNameSpaceString(nameSpaceNumber).equals(INTERNAL_DATABASE_NAMESPACE);
    }

    public boolean isScratchOID() {
       return getNameSpaceString(nameSpaceNumber).equals(SCRATCH_NAMESPACE);
    }

    public boolean isClientGeneratedOID() {
       return getNameSpaceString(nameSpaceNumber).equals(CLIENT_GENERATED_NAMESPACE);
    }

    public boolean isServerGeneratedOID() {
       return getNameSpaceString(nameSpaceNumber).equals(SERVER_GENERATED_NAMESPACE);
    }

    public boolean isAPIGeneratedOID() {
       return getNameSpaceString(nameSpaceNumber).equals(API_GENERATED_NAMESPACE);
    }

    public final long getIdentifier() {
      return identifier;
    }

    private final short getNameSpaceNumber() {
      return nameSpaceNumber;
    }

    public final int getGenomeVersionId()
    {
      return genomeVersionIdentifier;
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
      /*
       * Take care of this class's fields first by calling
       * defaultReadObject
       */
      in.defaultReadObject();


      /**
       * Have to ensure that this OID does not introduce a new unknown namepspace
       * If the namespace of this oid is unknown in this process then add it but
       * be sure to use the next available namespace number not it's own.
       * Check for name collisions (i.e a namespace already used in the
       * client that has a different id than that of the server object).
       * To allow this to be determined writeObject for OID was overriden to
       * send the string name of the OID's namespace after all objects
       * written as part of standard serialization.
       */
      String writtenNamespace = (String)in.readObject();
      this.nameSpaceNumber = getNameSpaceNumberAddIfNotFound(writtenNamespace);
    }

    private void writeObject(ObjectOutputStream out)
        throws IOException, ClassNotFoundException {
      /*
       * Take care of this class's fields first by calling
       * defaultWriteObject
       */
      out.defaultWriteObject();


      /**
       * Now also write the namespace for this oid. We dont store this as
       * an attribute that would be automatically written to save on memory
       * but still need it to disambiguate between two processes and the
       * list of namepsaces they keep.
       */
      out.writeObject(this.getNameSpaceAsString());
    }
}
