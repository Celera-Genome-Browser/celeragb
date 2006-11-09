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
package shared.util;

/**
 * Title:        Genome Browser
 * Description:
 * @author Peter Davies
 * @version $Id$
 */
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 *
 * READ CAREFULLY TO UNDERSTAND SIDE EFFECTS OF USING THIS CLASS!!
 *
 * This class is designed to be a light weight implementation of a HashMap.
 * To do it's job, it uses a single file on the disk as it's backing store.
 * While this is very efficient in terms of memory use, it is very slow
 * relitive to a in memory Map.  In testing, this class is about 10-100 times
 * slower than a HashMap.
 *
 * The real advantage is the fact that all values are serialized to disk, and take
 * no memory.  Only a small index (2 ints per key/value pair) is maintained in memory.
 * Because of the index, it is best to put only large objects into this Map.  Adding
 * a large group of Integers will require more memory than the Integers themselves
 * as the index is 2 ints.
 *
 * This class is also designed to accept values that are not referenced anywhere else
 * in the system.  The idea here is that once you put a value into this Map, it should
 * be garbage collected by the system and it's memory freed.  If this is not possible,
 * then don't use this class, use a HashMap as you will not get the memory savings,
 * but will take the performance penality.
 *
 * Also, keep in mind...  When an object is deserialized, a new instance is created.
 * This means,  That you can put the key "Test1" with Object1 as a value.  When you
 * get "Test1", you will get Object2 (not Object1) with the same state as Object1.
 * If you get "Test1" again, you will get Object3, again with the same state, but 3
 * different physical instances.  Therefore, get("Test1") == get("Test1") WILL be false,
 * while with a normal Map, this would be true.  If Object1's class has a .equals method
 * defined that will compare it's state, then .equals will be true for the two instances.
 *
 * It can be further assumed that objects returned from this may may take more memory than
 * their initial representation.  For example, if Object1 has a reference to Object2 and
 * Object1 is put in this Map, when retrieved, new Object3 (with Object1's state) will have
 * a reference to new Object4 (with Object2's state).  Please think about the implications
 * of this before using this Map.
 *
 * All objects added to this map as values MUST be serializable.  This will be enforced
 * via IllegalArguementException.
 *
 *
 */
public class LtWeightHashMap implements Map {
    private HashMap indexMap = new HashMap();
    private RandomAccessFile file;
    private int bytesWritten;
    private File trueFile;

    public LtWeightHashMap() {
        try {
            trueFile = File.createTempFile("gb_" + 
                                           Integer.toHexString(this.hashCode()), 
                                           ".tmp");
            file = new RandomAccessFile(trueFile, "rw");
            trueFile.deleteOnExit();

            //System.runFinalizersOnExit(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void finalize() {
        try {
            file.close();
        } catch (Exception ex) {
        }

        if (!trueFile.delete()) {
            System.out.println("Left tmp file");
        }
    }

    public int size() {
        return indexMap.size();
    }

    public boolean isEmpty() {
        return indexMap.isEmpty();
    }

    public boolean containsKey(Object key) {
        return indexMap.containsKey(key);
    }

    public boolean containsValue(Object value) {
        /**@todo: Implement this java.util.Map method*/
        throw new java.lang.UnsupportedOperationException(
                "Method containsValue() not yet implemented.");
    }

    public Object get(Object key) {
        Index idx = (Index) indexMap.get(key);

        if (idx == null) {
            return null;
        }

        try {
            file.seek(idx.start);

            byte[] serObj = new byte[idx.length];
            file.read(serObj);

            ByteArrayInputStream baIs = new ByteArrayInputStream(serObj);
            ObjectInputStream iStream = new ObjectInputStream(baIs);

            return iStream.readObject();
        } catch (Exception ex) {
            ex.printStackTrace();

            return null;
        }
    }

    public Object put(Object key, Object value) {
        if (!(value instanceof Serializable)) {
            throw new IllegalArgumentException(
                    "Values passed in to the LtWeightHashMap, must be" + 
                    " Serializable");
        }

        Object rtnObject = null;

        if (indexMap.containsKey(key)) {
            rtnObject = get(key);
        }

        try {
            ByteArrayOutputStream baOs = new ByteArrayOutputStream();
            ObjectOutputStream oStream = new ObjectOutputStream(baOs);
            oStream.writeObject(value);
            oStream.flush();
            file.seek(bytesWritten);

            byte[] serObj = baOs.toByteArray();
            indexMap.put(key, new Index(bytesWritten, serObj.length));


            // System.out.println("Put index of: "+bytesWritten+", length: "+serObj.length+" for: "+key);
            file.write(serObj);
            bytesWritten += serObj.length;
            oStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return rtnObject;
    }

    public Object remove(Object key) {
        Object rtnObject = null;

        if (indexMap.containsKey(key)) {
            rtnObject = get(key);
            indexMap.remove(key);
        }

        return rtnObject;
    }

    public void putAll(Map t) {
        /**@todo: Implement this java.util.Map method*/
        throw new java.lang.UnsupportedOperationException(
                "Method putAll() not yet implemented.");
    }

    public void clear() {
        indexMap.clear();
    }

    public Set keySet() {
        return indexMap.keySet();
    }

    public Collection values() {
        /**@todo: Implement this java.util.Map method*/
        throw new java.lang.UnsupportedOperationException(
                "Method values() not yet implemented.");
    }

    public Set entrySet() {
        /**@todo: Implement this java.util.Map method*/
        throw new java.lang.UnsupportedOperationException(
                "Method entrySet() not yet implemented.");
    }

    class Index {
        private int start;
        private int length;

        public Index(int start, int length) {
            this.start = start;
            this.length = length;
        }
    }

    class ShutdownThread extends Thread {
        public void run() {
            LtWeightHashMap.this.finalize();
        }
    }
}