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
package api.stub.data;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

public class GenomeVersionInfo implements java.io.Serializable {

  public static final int DATABASE_DATA_SOURCE=0;
  public static final int FILE_DATA_SOURCE=1;
  public static final int URL_DATA_SOURCE=2;

  private int genomeId;
  private String speciesName;
  private long assemblyVersion;
  private String dataSource;
  private int dataSourceType;

  // Host and port used to generate unique object identifiers when
  // new GenomicEntities are created within this genome version
  private String oidHost = null;
  private short oidPort = 0;

  // Host and port used to generate new gene accession numbers for new Genes
  // that are created within this GenomeVersion.
  private String geneAccessionHost = null;
  private short geneAccessionPort = 0;

  // Host and port used to generate new transccript accession numbers for new
  // transcripts that are created within this GenomeVersion.
  private String tranAccesionHost = null;
  private short tranAccessionPort = 0;


/**
 * @level developer
 */
  public GenomeVersionInfo( int genomeId, String speciesName, long assemblyVersion, String dataSource, int dataSourceType ) {
     if ( ( dataSourceType < 0 ) || ( dataSourceType > 2 ) ) {
      throw new IllegalArgumentException( "DataSourceType must be one of the statically defined constants" );
     }
     this.genomeId = genomeId;
     this.dataSourceType = dataSourceType;
     this.speciesName = speciesName;
     this.assemblyVersion = assemblyVersion;
     this.dataSource = dataSource;

  }

/**
 * @level developer
 */
  public GenomeVersionInfo( int genomeId,
                            String speciesName,
                            long assemblyVersion,
                            String dataSource,
                            int dataSourceType,
                            String aOidHostname,
                            short anOidPort,
                            String aGeneAccessionHost,
                            short aGeneAccessionPort,
                            String aTranAccessionHost,
                            short aTranAccessionPort ) {
    this( genomeId, speciesName, assemblyVersion, dataSource, dataSourceType );

    this.oidHost = aOidHostname;
    this.oidPort = anOidPort;
    this.geneAccessionHost = aGeneAccessionHost;
    this.geneAccessionPort = aGeneAccessionPort;
    this.tranAccesionHost = aTranAccessionHost;
    this.tranAccessionPort = aTranAccessionPort;
  }


  public int getGenomeVersionId() {
    return ( this.genomeId );
  }

  public String getSpeciesName() {
     return speciesName;
  }

  public String getAssemblyVersionAsString() {
     return Long.toString(getAssemblyVersion());
  }

  public long getAssemblyVersion() {
     return assemblyVersion;
  }

/**
 * @level developer
 */
  public String getDataSource() {
     return dataSource;
  }

/**
 * @level developer
 */
  public int getDataSourceType() {
     return dataSourceType;
  }

/**
 * @level developer
 */
  public boolean isDatabaseDataSource() {
     return dataSourceType==DATABASE_DATA_SOURCE;
  }

/**
 * @level developer
 */
  public boolean isFileDataSource() {
     return dataSourceType==FILE_DATA_SOURCE;
  }

/**
 * @level developer
 */
  public boolean isURLDataSource() {
     return dataSourceType==URL_DATA_SOURCE;
  }

  public String getOidHostname() {
    return oidHost;
  }

  public short getOidPort() {
    return oidPort;
  }

  public String getGeneAccessionHost() {
    return geneAccessionHost;
  }

  public short getGeneAccessionPort() {
    return geneAccessionPort;
  }

  public String getTranAccesionHost() {
    return tranAccesionHost;
  }

  public short getTranAccessionPort() {
    return tranAccessionPort;
  }


  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("Datasource: ");
    buf.append(this.getDataSource());
    buf.append(", Species: ");
    buf.append(this.getSpeciesName());
    buf.append(", AssemblyVersion: ");
    buf.append(this.getAssemblyVersion());
    return buf.toString();
  }

  public boolean hasPromoteServerInfo() {
    return (  (this.oidHost != null) &&
              (this.oidPort != 0) &&
              (this.geneAccessionHost != null) &&
              (this.geneAccessionPort != 0) &&
              (this.tranAccesionHost != null) &&
              (this.tranAccessionPort != 0) );
  }


   public static int calcGenomeVersionId( String speciesName, String dataSourceName, long assemblyVersion ) {
      String keyString = speciesName + dataSourceName + assemblyVersion;
      return ( keyString.hashCode() );
   }
}
