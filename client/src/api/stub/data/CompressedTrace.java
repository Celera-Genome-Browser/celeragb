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
 * Compressed data structure for trace data
 * 
 * @author David C. Wu
 * @version 0.0
 *
 * <p>
 * @see reference page
 */
public class CompressedTrace  implements java.io.Serializable
{
    private int version;
    private int range;                // compression range used
    private int seqLength;            // length of decompressed sequence
    private int traceLength;          // length of decompressed trace arrays
    private int traceLabel[];         // encoded base sequece with positions
    private int aTrace[];             // encoded a-trace
    private int cTrace[];             // encoded a-trace
    private int gTrace[];             // encoded a-trace
    private int tTrace[];             // encoded a-trace
    
    public void setVersion(int v) {
	version = v;
    }
    
    public void setRange(int r) {
	range = r;
    }
    public void setSeqLength(int l) {
	seqLength = l;
    }
    public void setTraceLength(int l) {
	traceLength = l;
    }
    
    public void setTraceLabel(int[] label) {
	traceLabel = label;
    }
    
    public void setATrace(int[] trace) {
	aTrace = trace;
    }
    
    public void setCTrace(int[] trace) {
	cTrace = trace;
    }
    
    public void setGTrace(int[] trace) {
	gTrace = trace;
    }
    
    public void setTTrace(int[] trace) {
	tTrace = trace;
    }
    
    public int getVersion() {
	return version;
    }

    public int getRange() {
	return range;
    }
    
    public int getSeqLength() {
	return seqLength;
    }
    
    public int getTraceLength() {
	return traceLength;
    }
    
    public int[] getTraceLabel() {
	return traceLabel;
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
    
    public void printContents() {
	System.out.println("cmp_rng: " + range);
	System.out.println("seq_len: " + seqLength);
	System.out.println("smp_len: " + traceLength);
	System.out.println("Labels size: " + traceLabel.length);
	System.out.println("a_trace.size: " + aTrace.length);
	System.out.println("c_trace.size: " + cTrace.length);
	System.out.println("g_trace.size: " + gTrace.length);
	System.out.println("t_trace.size: " + tTrace.length);
	
	System.out.println("labels:");
	for (int i=0; i<traceLabel.length; i++)
	    System.out.print(traceLabel[i]);
	
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

