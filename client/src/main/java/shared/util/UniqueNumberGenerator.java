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

package shared.util;

import java.math.BigInteger;

/**
 * This class generates unique numbers starting at a certain value (default of 1)
 */
public class UniqueNumberGenerator {

  private BigInteger lastGivenNumber;
  private static final BigInteger ONE= new BigInteger("1");

  //Start generating numbers at 0
  public UniqueNumberGenerator() {
    lastGivenNumber=new BigInteger("0");
  }

  public UniqueNumberGenerator(long initialValue) {
    lastGivenNumber=longToBigInt(--initialValue);
  }

  public UniqueNumberGenerator(BigInteger initialValue) {
    lastGivenNumber=initialValue.subtract(ONE);
  }

  public UniqueNumberGenerator(String initialValue) {
   try{
    lastGivenNumber=new BigInteger(initialValue).subtract(ONE);
   }
    catch (NumberFormatException nfEx) {
       throw new IllegalArgumentException("Value passed must be an integer!!");
    }
  }

  public void setNextValue(long nextValue) throws IllegalArgumentException {
    BigInteger newLastNumber = longToBigInt(--nextValue);
    synchronized(this) {
        if (newLastNumber.subtract(lastGivenNumber).signum()<0) throw
          new IllegalArgumentException("Cannot set the next value to a number already given." +
             "Last Given Value: "+this+" Number passed: "+nextValue);
        lastGivenNumber = newLastNumber;
    }
  }

  public void setNextValue(BigInteger nextValue) throws IllegalArgumentException {
    BigInteger newLastNumber = nextValue.subtract(ONE);
    synchronized(this) {
        if (newLastNumber.subtract(lastGivenNumber).signum()<0) throw
          new IllegalArgumentException("Cannot set the next value to a number already given." +
             "Last Given Value: "+this+" Number passed: "+nextValue);
        lastGivenNumber = newLastNumber;
    }
  }

  public void setNextValue(String nextValue) throws IllegalArgumentException {
    BigInteger newLastNumber;
    try {
       newLastNumber = new BigInteger(nextValue).subtract(ONE);
    }
    catch (NumberFormatException nfEx) {
       throw new IllegalArgumentException("Value passed must be an integer!!");
    }
    synchronized(this) {
        if (newLastNumber.subtract(lastGivenNumber).signum()<0) throw
          new IllegalArgumentException("Cannot set the next value to a number already given." +
             "Last Given Value: "+this+" Number passed: "+nextValue);
        lastGivenNumber = newLastNumber;
    }
  }

  public BigInteger getNextValueAsBigInteger() {
    synchronized(this) {
        lastGivenNumber=lastGivenNumber.add(ONE);
        return lastGivenNumber;
    }
  }

  public String getNextValueAsString() {
    synchronized(this) {
        lastGivenNumber=lastGivenNumber.add(ONE);
        return lastGivenNumber.toString();
    }
  }

  private BigInteger longToBigInt(long longValue) {
     return new BigInteger(Long.toString(longValue));
  }

  public String toString() {
    return lastGivenNumber.toString() ;
  }
}