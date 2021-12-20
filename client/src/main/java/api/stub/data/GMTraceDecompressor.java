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

import java.io.*;
import java.math.BigInteger;

/**
 * The GMTraceDecompressor is a trace files decompressor for trace files
 * that were compressed using Gene Myer's compression algorithm.  Thus where
 * the name GM came from.  The decompressor takes a compress trace object
 * and decompress it into a uncompressed trace object. The uncompressed object
 * can be retrieved as a stream.
 */
public class GMTraceDecompressor implements java.io.Serializable {
   private int BigBits = 1;
   private int MaskBits = 1;
   private int SignBit = 1;
   private int MaxBits;
   private int SignAdjust;
   private Trace trace;
   private CompressedTrace cmpTrace;

   private char[] int2Base = { 'A', 'C', 'G', 'T', 'N' };
   private int[] base2Int = new int[256]; /* Map A,C,G,T,N to 0-4 */

   //advance bit computation
   int cval;
   int bitp;
   int traceCtr;

   public GMTraceDecompressor(Object cmp) {
      if (cmp instanceof CompressedTrace)
         cmpTrace = (CompressedTrace) cmp;
      trace = new Trace();
   }

   public GMTraceDecompressor(Object cmp, Object trace) {
      if (cmp instanceof CompressedTrace)
         cmpTrace = (CompressedTrace) cmp;
      if (trace instanceof Trace)
         this.trace = (Trace) trace;
   }

   public Trace getTrace() {
      return trace;
   }

   /**
    * readCmpTrace reads a compressed trace file and store
    * it in a compress data object.
    */
   public void readCmpTrace(InputStream in) {
      BigInteger bi;
      try {
         byte[] buf = new byte[4];

         in.read(buf);
         bi = new BigInteger(buf);
         cmpTrace.setVersion(bi.intValue());

         //System.out.println("Version: " + bi);
         in.read(buf);
         bi = new BigInteger(buf);
         cmpTrace.setRange(bi.intValue());
         trace.setMaxTraceValue(bi.intValue());
         //System.out.println("cmp_rng: " + bi);

         in.read(buf);
         bi = new BigInteger(buf);
         cmpTrace.setSeqLength(bi.intValue());
         trace.setSeqLength(bi.intValue());
         //System.out.println("seq_len: " + bi);

         in.read(buf);
         bi = new BigInteger(buf);
         cmpTrace.setTraceLength(bi.intValue());
         trace.setTraceLength(bi.intValue());
         //System.out.println("smp_len: " + bi);

         in.read(buf);
         bi = new BigInteger(buf);
         int lblSize = bi.intValue();
         //System.out.println("Labels size: " + bi);

         in.read(buf);
         bi = new BigInteger(buf);
         int aTraceSize = bi.intValue();
         //System.out.println("a_trace.size: " + bi);

         in.read(buf);
         bi = new BigInteger(buf);
         int cTraceSize = bi.intValue();
         //System.out.println("c_trace.size " + bi);

         in.read(buf);
         bi = new BigInteger(buf);
         int gTraceSize = bi.intValue();
         //System.out.println("g_trace.size: " + bi);

         in.read(buf);
         bi = new BigInteger(buf);
         int tTraceSize = bi.intValue();
         //System.out.println("t_trace.size: " + bi);

         int[] intArray = new int[lblSize];
         for (int i = 0; i < lblSize; i++) {
            intArray[i] = in.read();
            //System.out.println("labels: " + intArray[i]);
         }
         cmpTrace.setTraceLabel(intArray);

         intArray = new int[aTraceSize];
         for (int i = 0; i < aTraceSize; i++) {
            intArray[i] = in.read();
            //System.out.println("a_trace: " + intArray[i]);
         }
         cmpTrace.setATrace(intArray);

         intArray = new int[cTraceSize];
         for (int i = 0; i < cTraceSize; i++) {
            intArray[i] = in.read();
            //System.out.println("c_trace: " + intArray[i]);
         }
         cmpTrace.setCTrace(intArray);

         intArray = new int[gTraceSize];
         for (int i = 0; i < gTraceSize; i++) {
            intArray[i] = in.read();
            //System.out.println("g_trace: " + intArray[i]);
         }
         cmpTrace.setGTrace(intArray);

         intArray = new int[tTraceSize];
         for (int i = 0; i < tTraceSize; i++) {
            intArray[i] = in.read();
            //System.out.println("t_trace: " + intArray[i]);
         }
         cmpTrace.setTTrace(intArray);

         in.close();
      }
      catch (Exception e) {
      }

      //These shall be called from somewhere else
   }

   /**
    * decompressLabel gets label information from the
    * compres data object and outputs it into the trace
    * object.
    */
   public void decompressLabel() {
      int len = cmpTrace.getSeqLength();
      int label[] = cmpTrace.getTraceLabel();
      int v = 0, p = 0;
      int cnt = 0;
      StringBuffer sb = new StringBuffer();
      int[] calls = new int[len];
      //System.out.println("UNCOMPRESSING BASES "+ len);

      for (int i = 0; i < len; i++) {
         //System.out.println("labels.code: " + label[cnt]);
         v = label[cnt++];
         while (v == 255) {
            p += 50;
            v = label[cnt++];
         }
         p += v / 5;
         //System.out.println("p: " + p + " v: " + v);
         sb.append(int2Base[v % 5]);
         calls[i] = p;
         //System.out.println("Label decompress " + int2Base[v%5] +
         //" @ " + p);
      }
      trace.setSequence(sb.toString());
      trace.setCalls(calls);

   }

   /**
    * Translated from c code:
    * Compute Advance bit.  
    */
   public void computeAdvanceBit(int[] cmptrace) {
      if (bitp >= 7) {
         cval = cmptrace[traceCtr++];
         bitp = 0;
      }
      else {
         cval >>= 1;
         bitp += 1;
      }
      //System.out.println("bitp: " + bitp + " cval: " + cval);
   }

   /**
   * Translated from c code:
   * Decompress trace data from compress data object.  
   */
   public void decompressTrace(int[] compressedTrace, int[] trace) {
      int i, j;
      int v0 = 0;
      int v1 = 0;
      int v2 = 0;
      int[] cmptrace = compressedTrace;

      int bit2delta[] = { 0, 1, -1, 2, -2, 3, -3 };
      int smplen = cmpTrace.getTraceLength();

      bitp = 7;
      traceCtr = 0;
      //System.out.println("TRACE UNCOMPRESSION OF " + smplen + " POINTS");
      for (i = 0; i < smplen; i++) {
         for (j = 0; j < 7; j++) {
            computeAdvanceBit(cmptrace);
            if ((cval & 1) == 1) {
               break;
            }
         }
         if (j < 7) {
            v2 = bit2delta[j];
         }
         else {
            v2 = 0;

            for (j = 0; j < BigBits; j++) {
               computeAdvanceBit(cmptrace);
               v2 = (v2 << 1) | (cval & 1);
            }
            if ((v2 & SignBit) == SignBit) {
               v2 -= SignAdjust;
            }
         }
         v1 += v2;
         v0 += v1;
         trace[i] = v0;
         //System.out.println("v0: " + trace[i]);
         //System.out.println(i + ": " + v2);
      }
   }

   /**
    * setupCompressParams to compression range
    */
   public void setupCompressionParams(int compress_range) {
      int n;
      int last_range = 0;

      if (last_range == compress_range)
         return;

      BigBits = 1;
      SignBit = 1;
      MaskBits = 1;
      for (n = compress_range; n > 0; n >>= 1) {
         BigBits += 1;
         SignBit <<= 1;
         MaskBits |= SignBit;
      }
      MaxBits = 7 + BigBits;
      SignAdjust = (SignBit << 1);

      for (n = 0; n < 5; n++)
         base2Int[int2Base[n]] = n;

      last_range = compress_range;

      /*
        #ifdef DEBUG
        printf("BIGBITS = %d SIGNBIT = %x MASKBITS = %x\n",BigBits,SignBit,MaskBits);
        printf("MAXBITS = %d SIGNADJUST = %x\n",MaxBits,SignAdjust);
        #endif
      */
   }

   /**
    * outputSCF takes a trace object and output it as
    * SCF trace file.  There are two version of trace file
    * format: 2.0 and 3.0.
    */
   public void outputSCF(Trace trace, int format) {
      int offset = 0;
      int bytes = 0;
      char[] buffer = new char[4];

      //write SCF header
      if (trace.getMaxTraceValue() < 256)
         bytes = 1;
      else
         bytes = 2;

      offset = 128;

      //get trace data from trace object
      int[] aTrace = trace.getATrace();
      int[] cTrace = trace.getCTrace();
      int[] gTrace = trace.getGTrace();
      int[] tTrace = trace.getTTrace();
      int[] calls = trace.getCalls();
      String sequence = trace.getSequence();
      int smp_len = trace.getTraceLength();
      int seq_len = trace.getSeqLength();

      //write out scf header info about size and length
      System.out.print(".scf");
      System.out.print(Int2Byte(smp_len, 4));
      System.out.print(Int2Byte(offset, 4));
      System.out.print(Int2Byte(seq_len, 4));
      System.out.print(Int2Byte(0, 4));
      System.out.print(Int2Byte(0, 4));

      offset += 4 * bytes * smp_len;
      System.out.print(Int2Byte(offset, 4));
      System.out.print(Int2Byte(0, 4));

      offset = 0;
      System.out.print(Int2Byte(offset, 4));

      if (format == 3)
         System.out.print("3.00");
      else
         System.out.print("2.00");

      System.out.print(Int2Byte(bytes, 4));
      System.out.print(Int2Byte(4, 4));
      System.out.print(Int2Byte(0, 4));
      System.out.print(Int2Byte(offset, 4));

      for (int i = 0; i < 18; i++) {
         System.out.print(Int2Byte(0, 4));
      }

      //Write SCF sample data (bytes = 1 : 2)
      if (format == 3) {
         SCF_Delta(aTrace, smp_len);
         SCF_Delta(cTrace, smp_len);
         SCF_Delta(gTrace, smp_len);
         SCF_Delta(tTrace, smp_len);

         for (int i = 0; i < smp_len; i++) {
            //Java use Unucide characters which is two bytes.
            System.out.print((char) aTrace[i]);
            System.out.print((char) cTrace[i]);
            System.out.print((char) gTrace[i]);
            System.out.print((char) tTrace[i]);
            /*
              System.out.print(Int2Byte(aTrace[i], bytes));
              System.out.print(Int2Byte(cTrace[i], bytes));
              System.out.print(Int2Byte(gTrace[i], bytes));
              System.out.print(Int2Byte(tTrace[i], bytes));
            */
         }

         SCF_Undelta(aTrace, smp_len);
         SCF_Undelta(cTrace, smp_len);
         SCF_Undelta(gTrace, smp_len);
         SCF_Undelta(tTrace, smp_len);
      }
      else {
         for (int i = 0; i < smp_len; i++) {
            System.out.print((char) aTrace[i]);
            System.out.print((char) cTrace[i]);
            System.out.print((char) gTrace[i]);
            System.out.print((char) tTrace[i]);
            //System.out.print(Int2Byte(aTrace[i], 1));
            //System.out.print(Int2Byte(cTrace[i], 1));
            //System.out.print(Int2Byte(gTrace[i], 1));
            //System.out.print(Int2Byte(tTrace[i], 1));
         }
      }

      /* Write SCF base data */
      if (format == 3) {
         for (int i = 0; i < seq_len; i++) {
            System.out.print(Int2Byte(calls[i], 4));
            System.out.print(Int2Byte(0, 4));
         }

         for (int i = 0; i < sequence.length(); i++)
            System.out.print(sequence.charAt(i));

         buffer[0] = buffer[1] = buffer[2] = '\0';

         for (int i = 0; i < seq_len; i++)
            System.out.print(buffer[0] + buffer[1] + buffer[2]);
      }
      else {
         for (int i = 0; i < seq_len; i++) {
            System.out.print(Int2Byte(calls[i], 4));
            System.out.print(Int2Byte(0, 4));
            buffer[0] = sequence.charAt(i);
            System.out.print(buffer);
         }
      }
   }

   /**
   * getSCFStream takes a trace object and returns an input stream
   * from which an SCF trace may be read.  There are two version of trace file
   * format: 2.0 and 3.0.
   */
   public StringReader getSCFStream(Trace trace, int format) {
      int offset = 0;
      int bytes = 0;
      StringBuffer scfString = new StringBuffer(40000);
      char[] buffer = new char[4];

      //write SCF header
      if (trace.getMaxTraceValue() < 256)
         bytes = 1;
      else
         bytes = 2;

      offset = 128;

      //get trace data from trace object
      int[] aTrace = trace.getATrace();
      int[] cTrace = trace.getCTrace();
      int[] gTrace = trace.getGTrace();
      int[] tTrace = trace.getTTrace();
      int[] calls = trace.getCalls();

      String sequence = trace.getSequence();
      int smp_len = trace.getTraceLength();
      int seq_len = trace.getSeqLength();

      //write out scf header info about size and length
      scfString.append(".scf");
      scfString.append(Int2Byte(smp_len, 4));
      scfString.append(Int2Byte(offset, 4));
      scfString.append(Int2Byte(seq_len, 4));
      scfString.append(Int2Byte(0, 4));
      scfString.append(Int2Byte(0, 4));

      offset += 4 * bytes * smp_len;
      scfString.append(Int2Byte(offset, 4));
      scfString.append(Int2Byte(0, 4));

      offset = 0;
      scfString.append(Int2Byte(offset, 4));

      if (format == 3)
         scfString.append("3.00");
      else
         scfString.append("2.00");

      scfString.append(Int2Byte(bytes, 4));
      scfString.append(Int2Byte(4, 4));
      scfString.append(Int2Byte(0, 4));
      scfString.append(Int2Byte(offset, 4));

      for (int i = 0; i < 18; i++) {
         scfString.append(Int2Byte(0, 4));
      }

      //Write SCF sample data (bytes = 1 : 2)
      if (format == 3) {
         SCF_Delta(aTrace, smp_len);
         SCF_Delta(cTrace, smp_len);
         SCF_Delta(gTrace, smp_len);
         SCF_Delta(tTrace, smp_len);

         for (int i = 0; i < smp_len; i++) {
            //Java use Unucide characters which is two bytes.
            scfString.append((char) aTrace[i]);
            scfString.append((char) cTrace[i]);
            scfString.append((char) gTrace[i]);
            scfString.append((char) tTrace[i]);
            /*
            scfString.append(Int2Byte(aTrace[i], bytes));
            scfString.append(Int2Byte(cTrace[i], bytes));
            scfString.append(Int2Byte(gTrace[i], bytes));
            scfString.append(Int2Byte(tTrace[i], bytes));
            */
         }

         SCF_Undelta(aTrace, smp_len);
         SCF_Undelta(cTrace, smp_len);
         SCF_Undelta(gTrace, smp_len);
         SCF_Undelta(tTrace, smp_len);
      }
      else {
         for (int i = 0; i < smp_len; i++) {
            scfString.append((char) aTrace[i]);
            scfString.append((char) cTrace[i]);
            scfString.append((char) gTrace[i]);
            scfString.append((char) tTrace[i]);
            //scfString.append(Int2Byte(aTrace[i], 1));
            //scfString.append(Int2Byte(cTrace[i], 1));
            //scfString.append(Int2Byte(gTrace[i], 1));
            //scfString.append(Int2Byte(tTrace[i], 1));
         }
      }

      /* Write SCF base data */
      if (format == 3) {
         for (int i = 0; i < seq_len; i++) {
            scfString.append(Int2Byte(calls[i], 4));
            scfString.append(Int2Byte(0, 4));
         }

         for (int i = 0; i < sequence.length(); i++)
            scfString.append(sequence.charAt(i));

         buffer[0] = buffer[1] = buffer[2] = '\0';

         for (int i = 0; i < seq_len; i++)
            scfString.append(buffer[0] + buffer[1] + buffer[2]);
      }
      else {
         for (int i = 0; i < seq_len; i++) {
            scfString.append(Int2Byte(calls[i], 4));
            scfString.append(Int2Byte(0, 4));
            buffer[0] = sequence.charAt(i);
            scfString.append(buffer);
         }
      }

      // Get a stream off of the string buffer.
      return ( new StringReader( scfString.toString() ) );
   } // End method: getSCFStream

   public char[] Int2Byte(int x, int size) {
      int i;
      char[] buffer = new char[4];

      for (i = size - 1; i >= 0; i--) {
         buffer[i] = (char) (0xFF & x);
         x >>= 8;
      }
      return buffer;
   }

   private void SCF_Delta(int[] trace, int len) {
      int p, v, i;

      p = 0;
      for (i = 0; i < len; i++) {
         v = trace[i];
         trace[i] -= p;
         p = v;
      }
      p = 0;
      for (i = 0; i < len; i++) {
         v = trace[i];
         trace[i] -= p;
         p = v;
      }
   }

   private void SCF_Undelta(int[] trace, int len) {
      int p, i;

      p = 0;
      for (i = 0; i < len; i++) {
         trace[i] += p;
         p = trace[i];
      }
      p = 0;
      for (i = 0; i < len; i++) {
         trace[i] += p;
         p = trace[i];
      }
   }

   public static void main(String[] arg) {
      int format = Integer.parseInt(arg[0]);
      File f = new File(arg[1]);

      if ((arg[0] == null) || (arg[1] == null)) {
         System.out.println("Usage: Decompress 2 or 3 filename");
         System.exit(0);

      }
      Trace trace = new Trace();
      CompressedTrace cmpTrace = new CompressedTrace();
      GMTraceDecompressor d = new GMTraceDecompressor(cmpTrace, trace);

      try {
         FileInputStream in = new FileInputStream(f);
         d.readCmpTrace(in);
         d.setupCompressionParams(cmpTrace.getRange());
         d.decompressLabel();
         d.decompressTrace(cmpTrace.getATrace(), trace.getATrace());
         d.decompressTrace(cmpTrace.getCTrace(), trace.getCTrace());
         d.decompressTrace(cmpTrace.getGTrace(), trace.getGTrace());
         d.decompressTrace(cmpTrace.getTTrace(), trace.getTTrace());
         d.outputSCF(trace, format);
         //ct.printContents();
         //trace.printContents();
      }
      catch (FileNotFoundException fe) {
      }
   }
}
