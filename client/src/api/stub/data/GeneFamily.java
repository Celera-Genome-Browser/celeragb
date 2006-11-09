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

public class GeneFamily implements java.io.Serializable
{
    public OID id;

    public String created_by;

    public String date_created;

    public String accession;

    public String authority;

    public String comment;

    public GeneFamily()
    {
    }

    public GeneFamily
        (OID id,
        String created_by,
        String date_created,
        String accession,
        String authority,
        String comment)
    {
        this.id = id;
        this.created_by = created_by;
        this.date_created = date_created;
        this.accession = accession;
        this.authority = authority;
        this.comment = comment;
    }

    public OID getOid() { return id; }
    public String getCreatedBy() { return created_by; }
    public String getDateCreated() { return date_created; }
    public String getAccession() { return accession; }
    public String getAuthority() { return authority; }
    public String getComment() { return comment; }
}
