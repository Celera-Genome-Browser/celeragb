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
package vizard.util;

import java.util.HashMap;
import java.util.HashSet;


/**
 * The purpose of the Preferences class is to help implement
 * interactive preference changes for a tree of objects.
 */
public class Preferences
{
    private HashMap map = new HashMap();

    private static class Value
    {
	public HashSet objects;
	public Object preferenceValue;

	public Value(ObjectWithPreferences object, Object preferenceValue) {
	    objects = new HashSet();
	    objects.add(object);
	    this.preferenceValue = preferenceValue;
	}
    }

    /**
     * Add the preference with key==prefName and value==prefValue.
     *
     * The given object is added to the list of objects whose behavior depends on the given
     * preference.
     */
    public void add(ObjectWithPreferences object, String prefName, Object prefValue) {
	Value v = (Value)map.get(prefName);
	if (v != null) {
	    if (Assert.debug)
		Assert.vAssert(v.preferenceValue == prefValue);
	    v.objects.add(object);
	    return;
	}
	map.put(prefName, new Value(object, prefValue));
    }

    /**
     * Return the preference value for the given key, or null.
     */
    public Object getValue(String prefName) {
	Value v = (Value)map.get(prefName);
	return (v == null) ? null : v.preferenceValue;
    }
}
