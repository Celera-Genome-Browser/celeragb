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
package api.entity_model.model.annotation;

import java.util.Set;


/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 *
 * The WorkspaceToken class is used by the Undo / Redo facility to effectively
 * capture a snap-shot of the Workspace.
 *
 * @author Jay T. Schira
 * @version $Id$
 */
public class WorkspaceToken {
    public Set rootOidSet; // The set of Oids that are the root features for the scope.
    public boolean isDirty;
    public byte[] allChangesToPromotedGVInBytes;

    // Will recreate this public HashMap oidToChangeTrace;  // snap shot of.
    public byte[] oidsDeletedThisSessionInBytes; // snapShot of.

    public WorkspaceToken() {
    }
}