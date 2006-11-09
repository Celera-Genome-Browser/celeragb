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
package shared.db.rdbms;

import api.entity_model.model.fundtype.EntityType;
import api.stub.data.OID;

import java.math.BigDecimal;

import java.sql.CallableStatement;
import java.sql.ResultSet;


public interface RdbmsAccess {
    /**
     * Prepare a CallableStatement that can be executed via executeQuery
     * @param stmt is the callable statement (stored procedure call) with
     *        the '?' character in it where parameters should go.
     *        Put the parameters in noramally via CallableStament.setXXX()
     * @return CallableStatement prepared from the input statement
     */
    public CallableStatement prepareCallableStatement(String stmt, 
                                                      String datasourceName)
                                               throws Exception;

    /**
     * Execute an SQL query
     * @param sql is a well-formed sql statment that returns results.
     * @return ResultSet that is returned from the query
     * @exception Exception covers SQLException and any container-specific
     *            exception that might be thrown when the SQL execution fails
     */
    public ResultSet executeQuery(String sql, String datasourceName)
                           throws Exception;

    /**
     * Execute an CallableStatement (stored procedure)
     * @param callStmt is a well-formed statment with its parameters filled in
     *           that returns results.
     * @param cursorIndexInStatement is the index of the oracle cursor
     * @return ResultSet that is returned from the query
     * @exception Exception covers SQLException and any container-specific
     *            exception that might be thrown when the SQL execution fails
     */
    public ResultSet executeQuery(CallableStatement callStmt, 
                                  int cursorIndexInStatement, 
                                  String datasourceName)
                           throws Exception;

    /**
     * Release the JDBC resources used by an callable statement as
     * some callable statements dont generate ResultSets that can be
     * used as an argument to the executeComplete that takes a result set
     * argument
     * @param rs identifies the JDBC resources to be released.
     * @param datasourceName logical name for the database connection pool
     *        in which the resources should be released
     */
    public void executeComplete(ResultSet rs, String datasourceName);

    /**
     * Release the JDBC resources used by an callable statement as
     * some callable statements dont generate ResultSets that can be
     * used as an argument to the executeComplete that takes a result set
     * argument
     * @param cs identifies the JDBC resources to be released.
     * @param datasourceName logical name for the database connection pool
     *        in which the resources should be released
     */
    public void executeComplete(CallableStatement cs, String datasourceName);

    /**
     * Execute an SQL update/insert/delete
     * @param sql is a well-formed sql statment that does not return a ResultSet.
     * @return int that is returned from the query (usually the number of rows affected)
     * @exception Exception covers SQLException and any container-specific
     *            Exception that might be thrown when the SQL execution fails
     */
    public int executeUpdate(String sql, String datasourceName)
                      throws Exception;

    /**
     * Execute an Stored Procedure update/insert/delete
     * @param sql is a Stored Procedure that does not return a ResultSet.
     * @return int that is returned from the query (usually the number of rows affected)
     * @exception Exception covers SQLException and any container-specific
     *            Exception that might be thrown when the SQL execution fails
     */
    public int executeUpdate(CallableStatement callStmt, String datasourceName)
                      throws Exception;

    /**
     * Obtain the id to be passed to the DB for procedural authorization checks
     */
    public String getRdbmsSecurityId();

    /**
     * Obtain an accession number for an entity with the specified type
     */
    public String generateAccessionNumberFor(OID genomeOid, EntityType type, 
                                             BigDecimal assemblyVersion);
}