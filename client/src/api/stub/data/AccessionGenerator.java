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
import java.util.HashMap;
import java.util.Map;

/**
 * This class will generate unique accession number starting at any value, with any prefix
 * Numbers will be unique only within the prefix
 * This class follows the singleton pattern.
 *
 * @author Peter Davies
 */

public class AccessionGenerator {

   private Map uniqueNumberGenerators=new HashMap();
   private static AccessionGenerator accessionGenerator=new AccessionGenerator();

   private AccessionGenerator(){} //enforce singleton

   public static AccessionGenerator getAccessionGenerator() {
     return accessionGenerator;
   }

   /**
    * This method will return a valid and unique accession string with the passed prefix
    * @return a complete accession String in the form CT1 if the prefix is CT
    */
   public String generateAccessionString (String prefix) {
      if (!uniqueNumberGenerators.containsKey(prefix))
          uniqueNumberGenerators.put(prefix, new UniqueNumberGenerator());
      UniqueNumberGenerator numGen=(UniqueNumberGenerator)uniqueNumberGenerators.get(prefix);
      return prefix+numGen.getNextValueAsString();
   }

   /**
    * Will set the initial value for a given prefix.
    *
    * @exception Throws IllegalAgrumentException is number passed has already
    * been used by the prefix passed
    */

   public void setInitialValueForPrefix(String prefix, BigInteger initialValue)
         throws IllegalArgumentException {
      if (!uniqueNumberGenerators.containsKey(prefix)) {
          uniqueNumberGenerators.put(prefix, new UniqueNumberGenerator(initialValue));
      }
      else {
          UniqueNumberGenerator numGen=(UniqueNumberGenerator)uniqueNumberGenerators.get(prefix);
          numGen.setNextValue(initialValue);
      }
   }

   /**
    * Will set the initial value for a given prefix.
    *
    * @exception Throws IllegalAgrumentException is number passed has already
    * been used by the prefix passed or if the initialValue passed is not a valid
    * number
    */
   public void setInitialValueForPrefix(String prefix, String initialValue)
         throws IllegalArgumentException {
      if (!uniqueNumberGenerators.containsKey(prefix)) {
          uniqueNumberGenerators.put(prefix, new UniqueNumberGenerator(initialValue)); // LLF: changed from prefix to initialValue in second param.
      }
      else {
          UniqueNumberGenerator numGen=(UniqueNumberGenerator)uniqueNumberGenerators.get(prefix);
          numGen.setNextValue(initialValue);
      }
   }

}