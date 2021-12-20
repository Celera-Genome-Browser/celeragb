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

import shared.util.Assert;

import java.util.ArrayList;


/**
 * @todo doc
 */
public class DNASequenceCache implements Sequence
{
    public static final int BASES_PER_BYTE = 4;

    /**
     * The approximate maximum number of bytes stored in the cache.
     */
    public static final int MAX_BYTES = 1024*1024;

    /**
     * The number of bytes per chunk.
     */
    public static final int CHUNK_BYTES = 25000;

    /**
     * The sequence loader interface.
     * The sequence loader must be provided by the client.
     */
    public static interface SequenceLoader
    {
        long axisLength();
        Sequence load(long location, int length);
    }

    private static final int BASES_PER_CHUNK = CHUNK_BYTES * BASES_PER_BYTE;
    private static final int MAX_CHUNKS = MAX_BYTES / CHUNK_BYTES;

    private final api.stub.sequence.DNASequenceCache.SequenceLoader loader;

    private ChunkSequence mostRecentlyUsed;
    private int chunkCount;
    private ArrayList array;

    /**
     * Create a new sequence cache with the given sequence loader.
     */
    public DNASequenceCache(api.stub.sequence.DNASequenceCache.SequenceLoader sequenceLoader) {
        loader = sequenceLoader;

        long arraySize = (loader.axisLength() + BASES_PER_CHUNK - 1) / BASES_PER_CHUNK;
        array = new ArrayList((int)arraySize);
        for(int i = 0; i < arraySize; ++i) {
            array.add(new LoaderSequence(i));
        }
    }

    public int kind() {
        return KIND_DNA;
    }

    public long length() {
        return loader.axisLength();
    }

    public int get(long location) {
        return sequence(location).get(location);
    }

    public boolean isSequenceAvailable(long location, int length) {
        int lastI = index(location + length - 1);
        for(int i = index(location); i <= lastI; ++i) {
            if (array.get(i) instanceof LoaderSequence)
                return false;
        }
        return true;
    }

    public void ensureDataAvailability(long location, int length) {
        int lastI = index(location + length - 1);
        for(int i = index(location); i <= lastI; ++i) {
            //"sequence(i)" is either a "ChunkSequence", or a "LoaderSequence".
            //Calling get on a ChunkSequence is of no consequence (the data is already ready).
            //Calling get on a LoaderSequence automatically loads the data.
            sequenceAt(i).get(location);
            location += BASES_PER_CHUNK;
        }
    }

    public synchronized void clear() {
        for(int i = 0; i < array.size(); ++i) {
            array.set(i, new LoaderSequence(i));
        }
    }

    private Sequence sequenceAt(int index) {
        return (Sequence)array.get(index);
    }

    private Sequence sequence(long location) {
        return (Sequence)array.get(index(location));
    }

    private int index(long location) {
        return (int)(location / BASES_PER_CHUNK);
    }

    private class LoaderSequence implements Sequence
    {
        private final int index;
        private Sequence loadedSequence;

        LoaderSequence(int index) {
            this.index = index;
        }

        public int kind() {
            return KIND_DNA;
        }

        public long length() {
            return loader.axisLength();
        }

        public int get(long location) {
            long myLocation = index * BASES_PER_CHUNK;
            int myLength = Math.min(BASES_PER_CHUNK, (int)(loader.axisLength() - myLocation));

            if (Assert.debug)
                Assert.vAssert(location >= myLocation && location < myLocation + myLength);

            synchronized(this) {
                if (loadedSequence == null) {
                    loadedSequence = loader.load(myLocation, myLength);
                    new ChunkSequence(index, loadedSequence);
                }
            }

            ChunkSequence sequence = (ChunkSequence)array.get(index);
            return sequence.get(location);
        }
    }

    private class ChunkSequence implements Sequence
    {
        private final int index;
        private Sequence sequence;
        private ChunkSequence moreUsed;
        private ChunkSequence lessUsed;

        ChunkSequence(int index, Sequence loadedSeq) {
            this.index = index;

            if (loadedSeq instanceof DNASequenceStorage)
                sequence = loadedSeq;
            else if (hasKnownBases(loadedSeq))
                sequence = DNASequenceStorage.create(loadedSeq);
            else
                sequence = new UnknownSequence(Sequence.KIND_DNA, BASES_PER_CHUNK);

            moreUsed = lessUsed = this;
            putInFront();
            array.set(index, this);
            incrChunkCount();
        }

        public int kind() {
            return KIND_DNA;
        }

        public long length() {
            return (index + 1) * BASES_PER_CHUNK;
        }

        public int get(long location) {
            if (this != mostRecentlyUsed)
                putInFront();
            return sequence.get(location - index * BASES_PER_CHUNK);
        }

        private void putInFront() {
            synchronized(DNASequenceCache.this) {
                if (mostRecentlyUsed != null) {
                    moreUsed.lessUsed = lessUsed;
                    lessUsed.moreUsed = moreUsed;

                    lessUsed = mostRecentlyUsed;
                    moreUsed = mostRecentlyUsed.moreUsed;
                    lessUsed.moreUsed = moreUsed.lessUsed = this;
                }
                mostRecentlyUsed = this;
            }
        }

        private void incrChunkCount() {
            synchronized(DNASequenceCache.this) {
                if (++chunkCount > MAX_CHUNKS)
                    mostRecentlyUsed.moreUsed.release();
            }
        }

        private void release() {
            //already synchronized
            lessUsed.moreUsed = moreUsed;
            moreUsed.lessUsed = lessUsed;
            lessUsed = moreUsed = null;
            array.set(index, new LoaderSequence(index));
            --chunkCount;
        }

        private boolean hasUnknownBases(Sequence seq) {
            for(int i = 0; i < BASES_PER_CHUNK; ++i) {
                if (seq.get(i) == UNKNOWN)
                    return true;
            }
            return false;
        }

        private boolean hasKnownBases(Sequence seq) {
            for(int i = 0; i < BASES_PER_CHUNK; ++i) {
                if (seq.get(i) != UNKNOWN)
                    return true;
            }
            return false;
        }
    }
}
