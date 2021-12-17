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
package api.stub.sequence;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */
import java.util.ArrayList;
import java.util.List;

public class SplicedDNASequence implements Sequence{
    private Sequence sequence;
    private List ranges;
    private List r = new ArrayList();
   // private long length = 0;

  public SplicedDNASequence(Sequence s, List ranges){
      this.sequence = s;
      this.ranges = ranges;
      computeRangesOnSplicedSequence();
      /*
      for(Iterator iter = ranges.iterator(); iter.hasNext();){
            SplicedRange r = (SplicedRange)iter.next();
            length = length +r.getMagnitude();
        }
	*/
  }
  public int kind(){
      return sequence.kind();
   }

    /**
     * Return the length of this sequence.
     */
   public long length(){
    /*
        long length = 0 ;
        // length will be equal to the
        // sum of all the exon ranges

	System.out.println("length  "+ length);
      */
        SplicedRange sr = (SplicedRange)r.get(r.size()-1);
        long length = sr.getEnd()+1;
     	return length;
    }

    private void computeRangesOnSplicedSequence(){
         //List r1 = new ArrayList();
         for(int i=0; i<ranges.size(); i++){
	      if(ranges.get(i) instanceof SplicedRange){
             //   System.out.println("av range is spliced range");
		SplicedRange range = (SplicedRange)ranges.get(i);
                System.out.println("actual range "+ "start"+range.getStart() + " end"+range.getEnd());
	    if(i==0){
              System.out.println("mapped range " +"start  0" + " end"+( range.getMagnitude()-1));
              r.add(new SplicedRangeImpl(0, range.getMagnitude()-1));
            }else{
              long s = ((SplicedRange)r.get(i-1)).getEnd()+1;
              long e = s+range.getMagnitude()-1;
              System.out.println("mapped range " +"start"+s + " end"+e);
	      r.add(new SplicedRangeImpl(s, e));
            }
	      }
        }
	/*
	SplicedRange sr = (SplicedRange )(r.get(r.size()-1));
	long lastStart = sr.getStart();
	long lastEnd = sr.getEnd();
	r.remove(sr);
	r.add(new SplicedRangeImpl(lastStart, lastEnd +1));
        */


//        return r1;

    }


     /**
      * Return the value at the given location.
      */
   public int get(long location){

       long pos = 0;
       for(int j = 0; j< r.size(); j++){
          SplicedRange sr = (SplicedRange)r.get(j);
          if(location >= sr.getStart() && location <=sr.getEnd()){
	    pos  = ((((SplicedRange)ranges.get(j)).getStart())+ location);
            break;

          }
        }

	   return sequence.get(pos);
    }



  private class SplicedRangeImpl extends SplicedRange{
      long start;
      long end ;
      SplicedRangeImpl(long start, long end){
         this.start = start;
         this.end = end;
      }
      public long getStart(){ return start;}
      public long getEnd(){return end;}
      public long getMagnitude(){ return Math.abs(end-start);}

  }

}

