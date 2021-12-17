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

public class GenomicEntityAlias implements java.io.Serializable
{
    public String name;

    public String type;

    public String rank;

    public String authority;

    public String status;

    public String source;


    public GenomicEntityAlias()
    {
    }


    public GenomicEntityAlias
        (String name,
        String type,
        String rank,
        String authority,
        String status,
        String source)
    {
        this.name = name;
        this.type = type;
        this.rank = rank;
        this.authority = authority;
        this.status = status;
        this.source = source;
    }


    /**
     * Support cloning.
     */
    public Object clone() {
      try {
        GenomicEntityAlias myClone;
        myClone = (GenomicEntityAlias)super.clone();
        myClone.name = new String(this.name);
        myClone.type = new String(this.type);
        myClone.rank = new String(this.rank);
        myClone.authority = new String(this.authority);
        myClone.status = new String(this.status);
        myClone.source = new String(this.source);
        return myClone;
      }
      catch (CloneNotSupportedException e) {
        throw new InternalError(e.toString());
      }
    }


    // General accessors...
    public String getName() { return name; }
    public String getType() { return type; }
    public String getRank() { return rank; }
    public String getAuthority() { return authority; }
    public String getStatus() { return status; }
    public String getSource() { return source; }
}
