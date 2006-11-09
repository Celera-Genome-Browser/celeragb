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
 ********************************************************************/

/**
 * Singleton utility class for manipulating sequence associated with a feature
 */
package api.stub.sequence;


import java.util.Comparator;
import java.util.StringTokenizer;


public class SequenceUtil {
  private static SequenceUtil sequenceUtil=new SequenceUtil();

  /**
   * Constructor is private to enforce singleton.
   */
  private SequenceUtil() {
  }


  /**
   * Get the singlton SequenceUtil instance.
   */
  static final public SequenceUtil getSequenceUtil() {return sequenceUtil;}


  /**
   * Get the translated region for a sequence starting a startPosition through stopPostion
   * @param sequence the spliced sequence String
   * @param startPosition the start position.
   * @param stopPosition the stop position.
   */
  public String getTranslatedRegion(String sequence, int startPosition, int stopPosition) {
    // Check args...
    if (sequence == null) return null;
    if (stopPosition < (startPosition + 3)) return null;

    String codon;
    int startPos = Math.max(0,startPosition);
    int stopPos = Math.min(sequence.length()-2, stopPosition);
    for (int i=startPos; i < stopPos; i+=3 ) {
      codon = sequence.substring(i, i+3);
      if (codon.equalsIgnoreCase("TAA") || codon.equalsIgnoreCase("TGA") || codon.equalsIgnoreCase("TAG")) {
        return sequence.substring(startPos, i+3);
      }
    }
    return sequence.substring(startPos, stopPos);
  }


  /**
   * Get the translated region for a sequence starting a startPosition through sequence.length() - 1.
   * @param sequence the spliced sequence String
   * @param startPosition the start position.
   */
  public String getTranslatedRegion(String sequence, int startPosition) {
    // Check args...
    if (sequence == null) return null;
    return this.getTranslatedRegion(sequence, startPosition, sequence.length() - 1);
  }


  /**
   * Get the translated region for a sequence starting at 0 through sequence.length() - 1.
   * @param sequence the spliced sequence String
   */
  public String getTranslatedRegion(String sequence) {
    // Check args...
    if (sequence == null) return null;
    return this.getTranslatedRegion(sequence, 0, sequence.length() - 1);
  }




   /**
    * Get all occurrences of a pattern or multiple patterns in an input string.
    * Multiple patterns can be specified using the | character. So for example
    *   findPatternLocations(sequence, "TGA|TAA|TAG", 0) would find the position of all stops
    * int the sequence.
    * @param sequence the sequence to be searched
    * @param startIndex the index to start searching from
    * @param pattern the pattern(s) to be found.
    * @return an array containing the indices of the pattern. If no occurrences of the pattern are found then a zero length array
    * is returned.
    */
    public int[] findPatternLocations(String sequence, String pattern,  int startIndex) {

      if ((sequence == null) || (pattern == null) || (startIndex >= sequence.length())) return new int[0];

      if (startIndex < 0) startIndex = 0;

      //java.util.ArrayList locations = new java.util.ArrayList();
      java.util.SortedSet locations = new java.util.TreeSet(new NumberComparator(true));

      StringTokenizer t = new StringTokenizer(pattern, "|", false);
      String[] searchPatternArr = new String[t.countTokens()];
      for (int i=0; i < searchPatternArr.length; i++) {
         searchPatternArr[i] = t.nextToken();
      }

      int pos;
      for (int p=0; p < searchPatternArr.length; p++) {
        pos = startIndex;
        while ((pos>=0) && (pos < sequence.length())) {
          pos = sequence.indexOf(searchPatternArr[p], pos);
          if (pos >= 0) {
            locations.add(new Integer(pos));
            //System.out.println("pos=" + pos);
            pos += 1;
          }
          else break;
        }
      }

      //System.out.println("findPattern: locations.size=" + locations.size());
      Object[] objArr = locations.toArray();
      int[] arr = new int[objArr.length];
      for (int i=0; i < objArr.length; i++) {
        arr[i] = ((Integer) objArr[i]).intValue();
      }
      return arr;
    }


   /**
    * Get the index of the first in frame stop.
    * @return the index if a stop is found. -1 if no stop found.
    */
   public int findFirstInFrameStop(String sequence, int startPosition) {
     String codon;
     int startPos = 0;
     if (sequence == null) return -1;
     if (startPosition >= 0) startPos = startPosition;
     for (int i=startPos; i < sequence.length()-2; i+=3 ) {
       codon = sequence.substring(i, i+3);
       if (codon.equalsIgnoreCase("TAA") || codon.equalsIgnoreCase("TGA") || codon.equalsIgnoreCase("TAG")) {
         return i;
       }
     }
     return -1;  //no in frame stop found
   }


   /**
    * Private inner class...
    * @todo: Either we don't need this, OR it's general purpose enough to share.
    */
   private class NumberComparator implements Comparator {
      private boolean ascending;

      public NumberComparator(boolean ascending) {
        this.ascending = ascending;
      }

      public int compare(Object obj1, Object obj2) {
        if ((obj1 instanceof Integer) && (obj2 instanceof Integer)) {
          Integer i1 = (Integer) obj1;
          Integer i2 = (Integer) obj2;

          if (i1.intValue() == i2.intValue()) return 0;

          if (ascending) {
            if (i1.intValue() < i2.intValue()) return -1;
            else return 1;
          }
          else {
            if (i1.intValue() > i2.intValue()) return -1;
            else return 1;
          }
        }
        return 0;
      }
   }
}