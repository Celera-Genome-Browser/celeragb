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
/**
 * 45 West Gude Drive, Rockville, Maryland, 20852, U.S.A.
 * 
 */

package api.stub.data;

/**
 * Trace data structure.  This class works with CompressedTrace
 * class.
 * 
 * @author David C. Wu
 * @version 0.0
 *
 * <p>
 * @see reference page
 */

public class Trace  implements java.io.Serializable
{
    private int seqLength;                  // length of sequence and position arrays
    private String sequence;                // base sequece
    private int[] calls;                    // trace positions of base calls
    private int[] aTrace;                  // a-trace values at each scan
    private int[] cTrace;                  // c-trace values at each scan
    private int[] gTrace;                  // g-trace values at each scan
    private int[] tTrace;                  // t-trace values at each scan
    private int traceLength;                      // length of trace arrays
    private int maxTraceValue;              // maximum trace value
    
    public int[] getCalls() {
	return calls;
    }
    
    public String getSequence() {
	return sequence;
    }
    
    public int[] getATrace() {
	return aTrace;
    }
    
    public int[] getCTrace() {
	return cTrace;
    }
    
    public int[] getGTrace() {
	return gTrace;
    }
    
    public int[] getTTrace() {
	return tTrace;
    }
    
    public int getSeqLength() {
	return seqLength;
    }
    
    public int getMaxTraceValue() {
	return maxTraceValue;
    }
    
    public int getTraceLength() {
	return traceLength;
    }
    
    public void setSeqLength(int len) {
	seqLength = len;
    }
    
    public void setTraceLength(int len) {
	aTrace = new int[len];
	cTrace = new int[len];
	gTrace = new int[len];
	tTrace = new int[len];
	traceLength = len;
    }
    
    public void setMaxTraceValue(int max) {
	maxTraceValue = max;
    }
    
    public void setSequence(String seq) {
	sequence = seq;
    }
    
    public void setCalls(int[] calls) {
	this.calls = calls;
    }
    
    public void printContents() {
	System.out.println("Range: " + getMaxTraceValue());
	System.out.println("seqLength: " + getSeqLength());
	System.out.println("TraceLength: " + getTraceLength());
	
	System.out.println("Bases:");
	System.out.println(sequence);
	System.out.println("Calls");
	for (int i=0; i<calls.length; i++)
	    System.out.print(calls[i]);
	
	System.out.println("\n" + "aTrace:");
	for (int i=0; i<aTrace.length; i++)
	    System.out.print(aTrace[i]);
	
	System.out.println("\n" + "cTrace:");
	for (int i=0; i<cTrace.length; i++)
	    System.out.print(cTrace[i]);
	
	System.out.println("\n" + "gTrace:");
	for (int i=0; i<gTrace.length; i++)
	    System.out.print(gTrace[i]);
	
	System.out.println("\n" + "tTrace:");
	for (int i=0; i<tTrace.length; i++)
	    System.out.print(tTrace[i]);
    }
}

