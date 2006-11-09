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

public class ServerConfig implements java.io.Serializable
{

    private static final long serialVersionUID=1959105291027567556L;

    public static final String UNKNOWN = "None";

    private String productName;

    private boolean readOnly;

    private String species;

    private long assemblyVersion;

    private String detail;

    private String dataSourceName;

    private OID speciesOID;

    public ServerConfig()
    {
    }

    public ServerConfig
        (String dataSourceName,
         String productName,
         boolean readOnly,
         String species,
         long assemblyVersion,
         String detail,
         OID speciesOID )
    {
        this.dataSourceName = dataSourceName;
        this.productName = productName;
        this.readOnly = readOnly;
        this.species = species;
        this.assemblyVersion = assemblyVersion;
        this.detail = detail;
        this.speciesOID = speciesOID;
    }

    public String getDataSourceName() { return dataSourceName; }
    public String getProductName() { return productName; }
    public boolean getReadOnly() { return readOnly; }
    public String getSpecies() { return species; }
    public long getAssemblyVersion() { return assemblyVersion; }
    public String getDetail() { return detail; }
    public OID getSpeciesOID(){ return speciesOID; }
/*    public int hashCode() {
       try {
           return Integer.parseInt(assemblyVersion);
       }
       catch (NumberFormatException nfE) {
          return 1;
       }
    }*/

    public boolean equals(Object obj){
       if (! (obj instanceof ServerConfig)) return false;
       ServerConfig sc=(ServerConfig) obj;
       boolean retVal =
            sc.getSpecies().equals(species) &&
            sc.getAssemblyVersion() == assemblyVersion  &&
            sc.getDetail().equals(detail) &&
            sc.getProductName().equals(productName) &&
            sc.getReadOnly()==readOnly &&
            sc.getSpeciesOID().equals(speciesOID);
       return retVal;
    }

    public String toString()
    {
      String returnStr =
        "(" + productName + ", " +
        " read only: " + readOnly + ", " +
        " species: " + species + ", " +
        " assembly: " + assemblyVersion + ", " +
        " detail: " + detail + ", " +
        " datasource: " + dataSourceName +
        " speciesOID: " + speciesOID.toString() + ")";

      return returnStr;
    }
}
