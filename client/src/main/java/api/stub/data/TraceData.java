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
 *		  Confidential -- Do Not Distribute                        *
 *********************************************************************
 CVS_ID:  $Id$
 *********************************************************************/
package api.stub.data;

import java.io.InputStream;

/**
 * The TraceData class serves a middle man which figure out what kind
 * of trace stream data it received.  If the stream is compressed stream
 * using Gene Myer's trace file compressing program, then it will decompress
 * it into a decompressedStream.  For other streams such as abi, uncompressed
 * scf 2 and scf3, the trace stream is directly pass out to the viewer.
 * There may a GZIP or GNUZIP trace stream, than this will decompress it.
 */
public class TraceData {
     private InputStream decompressedStream;
     private String fileName;
     private int scfType;

     public TraceData() {
     }

     public TraceData(String f) {
          fileName  = f;
     }

     /**
      * Constructor to scf type of trace files
      */
     public TraceData(InputStream stream, boolean isCompressed, int scfType ) {
          if (isCompressed) {
               decompress(stream);
               this.scfType = scfType;
          }
          else {
               decompressedStream = stream;
          }
     }

     public void decompress(InputStream traceStream ) {
          Trace trace = new Trace();
          CompressedTrace cmpTrace = new CompressedTrace();
          GMTraceDecompressor d = new GMTraceDecompressor(cmpTrace, trace);

          try {
               d.readCmpTrace(traceStream);
               d.setupCompressionParams(cmpTrace.getRange());
               d.decompressLabel();
               d.decompressTrace(cmpTrace.getATrace(), trace.getATrace());
               d.decompressTrace(cmpTrace.getCTrace(), trace.getCTrace());
               d.decompressTrace(cmpTrace.getGTrace(), trace.getGTrace());
               d.decompressTrace(cmpTrace.getTTrace(), trace.getTTrace());
               //decompressedStream = d.getSCFStream (trace, scfType);

          } catch (Exception e) {
               System.err.println ("ERROR: Failed to convert CMP to SCF");
               e.printStackTrace (System.err);
          }
     } // end of decompress

     public InputStream getDecompressedStream() {
          return decompressedStream;
     }
}

/*
$Log$
Revision 1.5  2002/11/07 16:07:58  lblick
Removed obsolete imports and unused local variables.

Revision 1.4  2000/04/03 23:30:51  tsmith
Modifications to Code Base to support EJB Server

Revision 1.3  2000/03/31 21:51:47  tsaf
Cleaned out "dead code"

Revision 1.2  2000/03/22 20:15:45  dwu
Change name Decompress to GMTraceDecompressor.

Revision 1.1  2000/03/17 21:57:25  dwu
Add TraceData class to handle trace data.

*/
