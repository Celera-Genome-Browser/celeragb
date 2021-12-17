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

import java.text.SimpleDateFormat;
import java.util.Date;

public class GenomicEntityComment implements java.io.Serializable
{
    private String createdBy;
    private Date creationDate;
    private String comment;
    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");


    public GenomicEntityComment
        (String createdBy,
        Date creationDate,
        String comment)
    {
        this.createdBy = createdBy;
        this.creationDate = creationDate;
        this.comment = comment;
    }

    public GenomicEntityComment
        (String createdBy,
        String comment)
    {
        this(createdBy,new Date(),comment);
    }

    public GenomicEntityComment
        (String createdBy,
        long creationDateInNumSecondsSince1970,
        String comment)
    {
        this.createdBy = createdBy;
        this.creationDate = new Date(creationDateInNumSecondsSince1970*1000);
        this.comment = comment;
    }

    public GenomicEntityComment (String comment) {
      this(System.getProperty("user.name"),comment);
    }

    public GenomicEntityComment
        (String createdBy,
        String dateFormattedString,
        String comment)
    throws java.text.ParseException
    {
        this.createdBy = createdBy;
        this.creationDate = dateTimeFormat.parse(dateFormattedString);
        this.comment = comment;
    }


    /**
     * Support cloning.
     */
    public Object clone() {
      try {
        GenomicEntityComment myClone;
        myClone = (GenomicEntityComment)super.clone();
        myClone.createdBy = new String(this.createdBy);
        myClone.creationDate = (java.util.Date)this.creationDate.clone();
        myClone.comment = new String(this.comment);
        return myClone;
      }
      catch (CloneNotSupportedException e) {
        throw new InternalError(e.toString());
      }
    }


    // General accessors...
    public String getCreatedBy() { return createdBy; }
    public Date getCreationDate() { return creationDate; }
    public String getCreationDateAsString() { return dateTimeFormat.format(creationDate); }
    public int getCreationDateAsNumSecondsSimce1970() { return (int)creationDate.getTime()/1000; }
    public String getComment() { return comment; }

    public String toString () {
       return getCreationDateAsString()+" "+getCreatedBy()+" "+getComment();
    }
}
