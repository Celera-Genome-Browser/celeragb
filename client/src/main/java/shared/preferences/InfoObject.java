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
package shared.preferences;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 *               This interface was created to efficiently relate all
 *               info objects of the system to the PreferenceManager.
 * @author
 * @version $Id$
 */
import java.util.Properties;


public abstract class InfoObject {
    protected String name;
    protected boolean isDirty = false;
    protected String keyBase = new String();
    protected String sourceFile = new String("Unknown");

    /**
     * The client's InfoObjects are cloned from the default and user
     * InfoObjects that are loaded from the files.
     */
    public abstract Object clone();

    public abstract String getKeyName();

    /**
     * This method is so the object will provide the formatted properties
     * for the writeback mechanism.
     */
    public abstract Properties getPropertyOutput();

    /**
     * Provides the unique name of the object itself.
     * This name is not in <filename>.properties format but a readable string.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        isDirty = true;
        this.name = name;
        this.keyBase = PreferenceManager.getKeyForName(name, true);
    }

    /**
     * As Info objects need to track their own modified ("dirty") state for writeback,
     * this method is a check for that state.
     */
    public boolean hasChanged() {
        return isDirty;
    }

    /**
     * This method is to figure out which file this object's information should
     * go to.
     */
    public String getSourceFile() {
        return sourceFile;
    }

    /**
     * This method determines the file that changes will go to.
     */
    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    /**
     * Default toString will return the Info name.
     */
    public String toString() {
        return name;
    }
}