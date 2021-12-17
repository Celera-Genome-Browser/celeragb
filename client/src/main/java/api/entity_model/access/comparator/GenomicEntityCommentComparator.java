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
package api.entity_model.access.comparator;

import api.stub.data.GenomicEntityComment;

import java.util.Comparator;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author       Peter Davies
 * @version      $Id$
 *
 * Intended to be used by any class that would like to sort comments by their date
 */

public class GenomicEntityCommentComparator implements Comparator {
       private boolean ascending=true;

       /**
        * Default constructor uses ascending
        */
       public GenomicEntityCommentComparator () {
       }

       public GenomicEntityCommentComparator (boolean ascending) {
          this.ascending=ascending;
       }

       public int compare(Object o1, Object o2) {
         if ((!(o1 instanceof GenomicEntityComment)) || (!(o2 instanceof GenomicEntityComment))) return 0;
         GenomicEntityComment comment1= (GenomicEntityComment)o1;
         GenomicEntityComment comment2= (GenomicEntityComment)o2;
         if (ascending) return comment2.getCreationDateAsNumSecondsSimce1970()-comment1.getCreationDateAsNumSecondsSimce1970();
         else return comment2.getCreationDateAsNumSecondsSimce1970()-comment1.getCreationDateAsNumSecondsSimce1970();
       }

}