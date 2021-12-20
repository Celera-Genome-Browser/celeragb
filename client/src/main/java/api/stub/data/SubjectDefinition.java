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

public class SubjectDefinition implements java.io.Serializable
{
    public short definitionOrder;

    public String accession = "";

    public String authority = "";

    public String species = "";

    public String description = "";

    public OID subjectSeqId;

/**
 * @level developer
 */
    public SubjectDefinition()
    {
    }

/**
 * @level developer
 */
    public SubjectDefinition
        (short definitionOrder,
        String accession,
        String authority,
        String species,
        String description)
    {
        this.definitionOrder = definitionOrder;
        this.accession = accession;
        this.authority = authority;
        this.species = species;
        this.description = description;
        subjectSeqId = new OID();
    }

/**
 * @level developer
 */
    public SubjectDefinition
      ( SubjDefnKey aKey,
        short definitionOrder,
        String alternate_accession,
        String species,
        String description)
    {
        this.definitionOrder = definitionOrder;
        this.species = species;
        this.description = description;
        aKey.becomeKeyFor(this);
    }

    public short getDefinitionOrder() { return definitionOrder; }
    public String getAccession() { return accession; }
    public String getAuthority() { return authority; }
    public String getSpecies() { return species; }
    public String getDescription() { return description; }

    public String toString()
    {
      String ret = "";
      ret += "(Def order " + definitionOrder + ", " +
        "accession " + accession + ", " +
        "authority " + authority + ", " +
        "species " + species + ", " + ")";
      ret += "\n Description " + description;

      return ret;
    }

/**
 * @level developer
 */
    static public class SubjDefnKey
    {
      private OID subjDefID;
      private String accessionNum;
      private String authority;

      public SubjDefnKey(OID aSubjDefId, String accessNum, String auth)
      {
        subjDefID = aSubjDefId; accessionNum = accessNum; authority = auth;
      }

      public boolean equals(Object anObject)
      {
        if (anObject == null)
        {
          return false;
        }
        else
        {
          SubjDefnKey aSD = (SubjDefnKey)anObject;
          return (  (aSD != null) &&
                  (aSD.subjDefID.equals(this.subjDefID)) &&
                  (aSD.accessionNum.equals(this.accessionNum)) &&
                  (aSD.authority.equals(this.authority)) );

        }
      }

      void becomeKeyFor(SubjectDefinition aSubjDef)
      {
        aSubjDef.subjectSeqId = this.subjDefID;
        aSubjDef.accession = this.accessionNum;
        aSubjDef.authority = this.authority;
      }

    };
}
