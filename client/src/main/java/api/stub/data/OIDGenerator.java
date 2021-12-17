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

package api.stub.data;

import shared.util.UniqueNumberGenerator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is a generator for ALL OIDs.  OIDs should not be constructed from outside this class.
 * This class follows the singleton pattern
 *
 * @author Peter Davies
 */

public class OIDGenerator {

   private Map uniqueNumberGenerators=new HashMap();
   private OID aNullOID = new OID();
   private static OIDGenerator oidGenerator=new OIDGenerator();
   private List listeners;

   private OIDGenerator(){} //enforce singleton

   public static OIDGenerator getOIDGenerator() {
     return oidGenerator;
   }

   public void addListener(OIDGeneratorListener listener) {
      if (listeners==null) listeners=new ArrayList();
      listeners.add(listener);
   }

   public void removeListener(OIDGeneratorListener listener) {
      if (listeners==null) return;
      listeners.remove(listener);
      if (listeners.size()==0) listeners=null;
   }

   /**
//  Removed - null Genome Version ID causes significant problems
//    we are removing this constructor to break code that creates null GV ids
//    PED 6/20/01
    *
    * This method will generate a unique OID in the passed nameSpace
    * @return the new OID
    * @todo This version should be removed in favor of the version
    * below that takes a genomeVersion id.
    */
//   public OID generateOIDInNameSpace (String nameSpace) {
//      if (!uniqueNumberGenerators.containsKey(nameSpace))
//          uniqueNumberGenerators.put(nameSpace, new UniqueNumberGenerator());
//      UniqueNumberGenerator numGen=(UniqueNumberGenerator)uniqueNumberGenerators.get(nameSpace);
//      String nextValue=numGen.getNextValueAsString();
//      postOIDGenerated(nameSpace);
//      return new OID(nameSpace,nextValue);
//   }

   public OID generateOIDInNameSpace (String nameSpace, int genomeVerId) {
      synchronized (this) {
        if (!uniqueNumberGenerators.containsKey(nameSpace))
          uniqueNumberGenerators.put(nameSpace, new UniqueNumberGenerator());
      }
      UniqueNumberGenerator numGen=(UniqueNumberGenerator)uniqueNumberGenerators.get(nameSpace);
      String nextValue=numGen.getNextValueAsString();
      postOIDGenerated(nameSpace);
      return new OID(nameSpace,nextValue,genomeVerId);
   }

   /**
//  Removed - null Genome Version ID causes significant problems
//    we are removing this constructor to break code that creates null GV ids
//    PED 6/20/01    *
    * This method will generate a unique OID in the OID.SCRATCH_NAMESPACE nameSpace
    * @return the new OID
    */
//   public OID generateScratchOID () {
//      return generateOIDInNameSpace(OID.SCRATCH_NAMESPACE);
//   }

   /**
    * This method will generate a unique OID in the OID.SCRATCH_NAMESPACE nameSpace
    * and with the passed GenomeVersionID
    * @return the new OID
    */
   public OID generateScratchOIDForGenomeVersion (int genomeVerId) {
      return generateOIDInNameSpace(OID.SCRATCH_NAMESPACE,genomeVerId);
   }

   /**
    * Source for genome version id value required to create OIDs for genome versions.
    * @return int genome version ID to be used in all OIDs under the genome version.
    */
   public int getGenomeVersionId(String speciesName, String datasourceName, long assemblyVer) {
      String keyString =
      speciesName + datasourceName + assemblyVer;
      return keyString.hashCode();
   } // End method: getGenomeVersionId

   /**
    * This method will allow you to set an initial value for a nameSpace.
    * @exception IllegalArgumentException will be throw if initialValue is less than
    * the current value
    */
   public void setInitialValueForNameSpace(String nameSpace, BigInteger initialValue)
         throws IllegalArgumentException {
      synchronized (this) {
          if (!uniqueNumberGenerators.containsKey(nameSpace)) {
              uniqueNumberGenerators.put(nameSpace, new UniqueNumberGenerator(initialValue));
          }
          else {
              UniqueNumberGenerator numGen=(UniqueNumberGenerator)uniqueNumberGenerators.get(nameSpace);
              numGen.setNextValue(initialValue);
          }
      }
      postNewInitialValue(nameSpace,initialValue);
   }

   /**
    * This method will allow you to set an initial value for a nameSpace.
    * @exception IllegalArgumentException will be throw if initialValue is less than
    * the current value, or if the String is not a valid number
    */
   public void setInitialValueForNameSpace(String nameSpace, String initialValue)
         throws IllegalArgumentException {
      synchronized (this) {
        if (!uniqueNumberGenerators.containsKey(nameSpace)) {
            uniqueNumberGenerators.put(nameSpace, new UniqueNumberGenerator(initialValue));
        }
        else {
            UniqueNumberGenerator numGen=(UniqueNumberGenerator)uniqueNumberGenerators.get(nameSpace);
            numGen.setNextValue(initialValue);
        }
      }
      postNewInitialValue(nameSpace,initialValue);
   }

   /**
    * @return A valid null OID for .equals comparision.  Cannot do == compares on OIDs ever!!
    */
   public OID getANullOID() {
     return aNullOID;
   }

   /**
   * Reserved for Server use to create OIDs from the Database
   * @return new OID
   */
   public OID getInternalDatabaseOID(String identifier, int genomeVerId) {
      return new OID(OID.INTERNAL_DATABASE_NAMESPACE,identifier, genomeVerId);
   }

   private void postNewInitialValue(String nameSpace,String initialValue) {
      postNewInitialValue(nameSpace,new BigInteger(initialValue));
   }

   private void postNewInitialValue(String nameSpace,BigInteger initialValue) {
      if (listeners!=null) {
         OIDGeneratorListener[] listenerArray= new OIDGeneratorListener[listeners.size()];
         listeners.toArray(listenerArray);
         for (int i=0;i<listenerArray.length;i++) {
            listenerArray[i].newInitialValueForNameSpace(nameSpace,initialValue);
         }
      }
   }

   private void postOIDGenerated(String nameSpace) {
      if (listeners!=null) {
         OIDGeneratorListener[] listenerArray= new OIDGeneratorListener[listeners.size()];
         listeners.toArray(listenerArray);
         for (int i=0;i<listenerArray.length;i++) {
            listenerArray[i].newOIDGenerated(nameSpace);
         }
      }
   }


}