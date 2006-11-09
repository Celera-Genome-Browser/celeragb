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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


/**
* This class implements a Hashtable that allows for key collisions.  All elements
* within the Hashtable are Vectors.  Should a collision occur, the element will be
* added to the Vector.  All get methods will return the Vector for the key.  size
* and contains methods have been overridden to take the Vectors into account
*
*  Initially Written by: Peter Davies
*
*/
public class MultiHash extends Hashtable implements java.io.Serializable {
    /**
     * Put a value into the multiHash...
     * If we don't have a vector fo this key, create one.
     * Add this value to the vector...
     */
    public synchronized Object put(Object key, Object value) {
        Vector v;

        if (!this.containsKey(key)) {
            v = new Vector();
            v.add(value);
            super.put(key, v);

            return null;
        }

        v = (Vector) get(key);
        v.addElement(value);

        return v;
    }

    public int size() {
        int size = 0;

        for (Enumeration e = this.elements(); e.hasMoreElements();) {
            size += ((Vector) e.nextElement()).size();
        }

        return size;
    }

    public synchronized boolean contains(Object value) {
        boolean found = false;

        for (Enumeration e = this.elements(); e.hasMoreElements();) {
            found = ((Vector) e.nextElement()).contains(value);

            if (found) {
                break;
            }
        }

        return found;
    }
}

/*
$Log$
Revision 1.6  2003/03/05 20:20:33  grahamkj
test GB-123456789

Revision 1.5  2002/11/07 18:38:37  lblick
Removed obsolete imports and unused local variables.

Revision 1.4  2000/08/02 20:27:48  schirajt
Just added some comments.  Will add some methods later.

Revision 1.3  2000/04/20 01:53:50  pdavies
Added serializable so that it can be passed across the wire

Revision 1.2  2000/02/10 21:56:33  pdavies
Mods for new packaging

Revision 1.1  1999/08/20 14:24:46  DaviesPE
Initial add - moved MultiHash from access.util

Revision 1.1  1999/06/18 15:04:02  DaviesPE
Initial Checkin - Extends Hashtable to allow multiple values per key

Revision 1.4  1999/06/02 16:19:45  DaviesPE
Modified scaffoldPathAxis to be a ScaffoldPathAxis instead of Object and
modified explicitSelection to be a ProxyInterval instead of Object

Revision 1.3  1999/05/25 01:24:39  gregg

Bringing in internal modifications from 5-21-99.

Revision 1.4  1999/05/21 19:40:11  pdavies
Added documentation comments

Revision 1.3  1999/05/18 13:13:44  pdavies

*/
