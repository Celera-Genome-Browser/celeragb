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
package client.gui.other.fasta;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */


 /**
 * A sequence in FASTA format begins with a single-line description, followed by lines of sequence data.
 * The description line is distinguished from the sequence data by a greater-than (">") symbol in the first column.
 * It is recommended that all lines of text be shorter than 80 characters in length.
 * For more details see the ncbi site at :http://www.ncbi.nlm.nih.gov/BLAST/fasta.html
 * This class is trying to model the fasta format as described above
 */


public class FastaObject {
   private String seqStr;
   private String defline;
   public static final int FASTA_CHAR_PER_LINE=80;
  // private boolean isProteinSequence;


   public FastaObject (String  seqStr, String defline/*, boolean  isProteinSequence*/) {
      this.seqStr= seqStr;
      this.defline= defline;
    //  this.isProteinSequence= isProteinSequence;

   }
/**
> > >/id= /org= /description= /evidence= /length /ga_name=
> > /ga_uid= /alignment=
*/
   public String getFastaDefline(){
     return defline;



   }

   public String getFastaSeqString(){
     return seqStr;

   }
/*
   public boolean isFastaSeqProteinSeq(){

     return isProteinSequence;
   }
*/




}