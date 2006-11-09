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

/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
*********************************************************************/
import java.sql.CallableStatement;


/**
 * Represents a single instruction in a possible collection of instructions
 * required to promote an annotation to persistent storage.
 */
public class PromoteInstruction {
    private String sqlString = null;
    private CallableStatement promoteStatement = null;
    private String errorOnFailure = null;
    private boolean mandatory = true;

    public PromoteInstruction(String sqlString, String errorOnFailure, 
                              boolean mandatory) {
        this.sqlString = sqlString;
        this.errorOnFailure = errorOnFailure;
        this.mandatory = mandatory;
    }

    public PromoteInstruction(CallableStatement promoteStatement, 
                              String errorOnFailure, boolean mandatory) {
        this.promoteStatement = promoteStatement;
        this.errorOnFailure = errorOnFailure;
        this.mandatory = mandatory;
    }

    // REVISIT: Leaving the option to get an sql string rather than
    // a callable statement so that the transition between promotion using
    // dynamic sql and callable statements can be made over time. Clients
    // to this method will have to check for a null return while in
    // transition
    public String getSqlString() {
        return sqlString;
    }

    public CallableStatement getPromoteCallableStatement() {
        return promoteStatement;
    }

    public String getErrorOnFailure() {
        return errorOnFailure;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * An instruction is considered equal if the sql string that it
     * proposes to execute is exactly equal to that of another object
     */
    public boolean equals(Object otherIns) {
        if ((otherIns == null) || !(otherIns instanceof PromoteInstruction)) {
            return false;

            //throw new IllegalArgumentException("Illegal type passed to equals " +
            //" method of PromoteInstruction");
        } else {
            PromoteInstruction otherPromoteIns = (PromoteInstruction) otherIns;

            return this.sqlString.equals(otherPromoteIns.sqlString);
        }
    }

    public String toString() {
        String asString = "SQL: [" + sqlString + "],  [" + errorOnFailure + 
                          "], [" + mandatory + "]";

        return asString;
    }

    public boolean isDynamic() {
        return (promoteStatement == null);
    }
}