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
package api.entity_model.access.report;


import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Title:        Bag of Data for Blast Parameters For Interactive blast
 * Description:  All optional parameters to blast, as found in CDS.
 * @author Les Foster
 * @version $Id$
 */

public final class BlastParameters implements java.io.Serializable{
  private final BlastAlgorithm selectedAlgorithm;
  private final BlastDatabase selectedDatabase;
  private final TreeMap argValueMap;

  //-----------------------------CONSTRUCTORS

  /**
   * Sets all the default values used for blast to those specfied by
   * scientists at the organization which developed this application
   * as the preferred defaults
   */
  public BlastParameters(final BlastAlgorithm aSearchAlgorithm, final BlastDatabase aSelectedDatabase,
      final TreeMap argValueMap) {
    this.selectedAlgorithm = aSearchAlgorithm;
    this.selectedDatabase = aSelectedDatabase;
    this.argValueMap = argValueMap;
  }

  public BlastDatabase getDatabase() {
    return selectedDatabase;
  }

  public String getAlgorithmName() {
    return selectedAlgorithm.getAlgorithmName();
  }

  public int getEntityTypeProduced() {
    return selectedAlgorithm.getEntityTypeCodeForMatch();
  }


  /**
   * Returns a string that contains the flags and values for those flags
   * that represent the current settings for this BlastParameters object.
   * This string can be passed directly to the blastall program.  These keys
   * need the "-" placed before the args as shown below.
   */
  public String toCmdLineString() {
    final StringBuffer params = new StringBuffer();
	for (Iterator it = argValueMap.keySet().iterator(); it.hasNext(); ) {
	  final String tmpSwitch = (String)it.next();
	  final String tmpValue = (String)argValueMap.get(tmpSwitch);
	  if (tmpSwitch==null || tmpSwitch.equals("")) params.append(" -"+tmpValue);
	  else params.append(" -"+tmpSwitch+" "+tmpValue);
	}
    return params.toString();
  }


  /**
   * Returns a map of String keys and String values each key is a flag that
   * could be used as an attribute and each value the attributes value when
   * setting AppOptions in SRS application objects.  The keys have no "-" in front
   * of the args, and the values are unchanged.
   */
  public Map toSRSAppOptsArgs() {
    return argValueMap;
  }


  // Inner class for defined set of algorithms
  public static final class BlastAlgorithm implements java.io.Serializable {
    private final int resultingEntityTypeCode;
    private final String name;
    private final List availableDatabases;
    private final String supportedFlags;

    public BlastAlgorithm(final int aCode, final String aName, final List databaseList,
                          final String aSupportedFlagsString) {
      resultingEntityTypeCode = aCode;
      name = aName;
      availableDatabases = databaseList;
      supportedFlags = aSupportedFlagsString;
      System.out.println("Supported flags: "+supportedFlags);
    }

    /**
     * Returns a list of BlastDatabase instances that represent the databases
     * against which the currently logged in user, can search using the
     * algorithm represented by this instance.
     */
    public List getAvailableDatabases() {
      return Collections.unmodifiableList(availableDatabases);
    }

    public String getAlgorithmName() {
      return this.toString().toLowerCase();
    }

    public int getEntityTypeCodeForMatch() {
      return resultingEntityTypeCode;
    }

    public boolean supportsFlag(final String flag) {
      return (supportedFlags.indexOf(flag) != -1);
    }

    public int numberOfFlagsSupported() {
      return supportedFlags.length();
    }


    public String toString() {
      return name;
    }

  }

  // Inner class for defined set of algorithms
  public static final class BlastDatabase implements java.io.Serializable {
    private final boolean externalDatabase;
    private final String name;
    private final String pathToBlastAppDatabank;
    private final String lsfQueueName;

    public BlastDatabase(final boolean isExternal, final String aDbName, final String aPath, final String lsfQueue) {
      externalDatabase = isExternal;
      name = aDbName;
      pathToBlastAppDatabank = aPath;
      lsfQueueName = lsfQueue;
    }

    public boolean isExternalDatabase() {
      return externalDatabase;
    }

    public String toString() {
      return name;
    }

    /**
     * Returns the credentials needed to locate the database. In some cases
     * the database will be an full blown RDBMS in which case a login string
     * will be returned. In other cases the database may be a simple flat file
     * in which case an absolute path string will be returned and permission
     * to access the database file is assumed.
     */
    public String getDatabaseCredentials() {
      return pathToBlastAppDatabank;
    }

    /**
     * Returns the name of the LSF queue associated with this database.
     */
    public String getLSFQueueName() {
      return lsfQueueName;
    }
  }

} // End class
