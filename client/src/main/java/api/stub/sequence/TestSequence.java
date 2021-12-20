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
package api.stub.sequence;


public class TestSequence
{
    public static void main(String[] args) {
        Sequence s1 = new Sequence() {
                public int kind() { return KIND_DNA; }
                public long length() { return 500; }
                public int get(long i) {
                    if (i < 100) return UNKNOWN;
                    if (i < 200) return DNA.A;
                    if (i < 300) return DNA.T;
                    if (i < 400) return DNA.C;
                    if (i < 500) return DNA.G;
                    return UNKNOWN;
                }
            };

        SubSequence sub = new SubSequence(s1, 100, 400);
        for(int i = 0; i < 400; ++i) {
            a(sub.get(i) == s1.get(100 + i));
            a(sub.get(i) != Sequence.UNKNOWN);
        }
        a(sub.get(10000) == Sequence.UNKNOWN);

//        DNASequenceStorage stor = new DNASequenceStorage(sub, true);
//        for(int i = 0; i < 400; ++i) {
//            a(stor.get(i) == sub.get(i));
//        }

        final int CHUNK_LENGTH = DNASequenceCache.CHUNK_BYTES * DNASequenceCache.BASES_PER_BYTE;
        final Sequence seqA = new Sequence() {
                public int kind() { return KIND_DNA; }
                public long length() { return CHUNK_LENGTH; }
                public int get(long location) { return DNA.A; }
            };
        final Sequence seqX = new Sequence() {
                public int kind() { return KIND_DNA; }
                public long length() { return CHUNK_LENGTH; }
                public int get(long location) { return UNKNOWN; }
            };
        final Sequence seqAX = new Sequence() {
                public int kind() { return KIND_DNA; }
                public long length() { return CHUNK_LENGTH; }
                public int get(long i) { return (i < CHUNK_LENGTH/2) ? DNA.A : UNKNOWN; }
            };
        final Sequence seqXA = new Sequence() {
                public int kind() { return KIND_DNA; }
                public long length() { return CHUNK_LENGTH; }
                public int get(long i) { return (i < CHUNK_LENGTH/2) ? UNKNOWN : DNA.A; }
            };
        final Sequence seqAXA = new Sequence() {
                public int kind() { return KIND_DNA; }
                public long length() { return CHUNK_LENGTH; }
                public int get(long i) {
                    if (i < CHUNK_LENGTH/3) return DNA.A;
                    if (i < 2*CHUNK_LENGTH/3) return UNKNOWN;
                    return DNA.A;
                }
            };

        final int CHUNK_BASES = DNASequenceCache.CHUNK_BYTES * DNASequenceCache.BASES_PER_BYTE;
        final int numChunks = DNASequenceCache.MAX_BYTES / DNASequenceCache.CHUNK_BYTES * 10;
        System.out.println("num entries in cache array: " + numChunks);
        System.out.println("max number of chunks: " + DNASequenceCache.MAX_BYTES / DNASequenceCache.CHUNK_BYTES);
        final boolean[] loadingFlags = new boolean[numChunks];
        final DNASequenceCache.SequenceLoader myLoader =
            new DNASequenceCache.SequenceLoader() {
                public long axisLength() { return numChunks * CHUNK_BASES - 15; }
                public Sequence load(long location, int length) {
                    int i = (int)(location / CHUNK_BASES);
                    a(!loadingFlags[i]);
                    loadingFlags[i] = true;
                    System.out.println("Loading index " + i);
                    try { Thread.sleep(10); }
                    catch(InterruptedException ex) {}
                    Sequence seq;
                    if (i == 5) seq = seqX;
                    else if (i == 10) seq = seqAXA;
                    else if (i == 15) seq = seqXA;
                    else if (i == 20) seq = seqAX;
                    else seq = seqA;
                    loadingFlags[i] = false;
                    return seq;
                }
            };

        final DNASequenceCache cache = new DNASequenceCache(myLoader);

        for(long i = 0; i < myLoader.axisLength(); ++i)
            cache.get(i);

        Thread[] threads = new Thread[10];
        for(int i = 0; i < threads.length; ++i) {
            threads[i] = new Thread() {
                public void run() {
                    try {
                        for(int i = 0; i < 100; ++i) {
                            long location = (long)(Math.random() * myLoader.axisLength());
                            for(int j = 0; j < 1000; ++j) {
                                cache.get(location + j);
                            }
                        }
                    }
                    catch(Exception ex) {
                        ex.printStackTrace();
                        a(false);
                    }
                }
            };
        }

        System.out.println("-----------Starting threads with full cache");
        for(int i = 0; i < threads.length; ++i) {
            threads[i].start();
        }
        for(int i = 0; i < threads.length; ++i) {
            try { threads[i].join(); }
            catch(InterruptedException ex) {}
        }

        a(cache.get(0) == DNA.A);
        a(cache.get(5*CHUNK_BASES - 1) == DNA.A);
        a(cache.get(5*CHUNK_BASES) == Sequence.UNKNOWN);
        a(cache.get(6*CHUNK_BASES-1) == Sequence.UNKNOWN);
        a(cache.get(10*CHUNK_BASES - 1) == DNA.A);
        a(cache.get(10*CHUNK_BASES) == DNA.A);
        a(cache.get(11*CHUNK_BASES-1) == DNA.A);
        a(cache.get(10*CHUNK_BASES+CHUNK_BASES/2-1) == Sequence.UNKNOWN);
        a(cache.get(15*CHUNK_BASES-1) == DNA.A);
        a(cache.get(15*CHUNK_BASES) == Sequence.UNKNOWN);
        a(cache.get(16*CHUNK_BASES-1) == DNA.A);
        a(cache.get(20*CHUNK_BASES-1) == DNA.A);
        a(cache.get(20*CHUNK_BASES) == DNA.A);
        a(cache.get(21*CHUNK_BASES-1) == Sequence.UNKNOWN);
        a(cache.get(21*CHUNK_BASES) == DNA.A);
    }

    private static void a(boolean assertion) {
        if (!assertion)
            throw new Error("Assertion failed!");
    }
}
