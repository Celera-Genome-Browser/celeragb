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
package api.facade.abstract_facade.fundtype;

   /**
    * Constants for know EnityType values for convenience of switch statements
    * In EntityModel, Facade, and Server layers
    */
public interface EntityTypeConstants {
   public static final int Genome_Version                      = -1;
   public static final int Start_Codon_Start_Position          = -4;
   public static final int StopCodon                           = -5;
   public static final int Translation_Start_Position          = -6;
   public static final int Miscellaneous                       = -9;

   public static final int Chromosome                          = 1;
   public static final int Genomic_Axis                        = 2;
   public static final int NonPublic_Transcript                   = 3;
   public static final int NonPublic_Gene                         = 4;
   public static final int NonPublic_Protein                      = 5;
   public static final int SNP                                 = 6;
   public static final int Conserved_Segment                   = 7;
   public static final int Transcription_Factor_Binding_Site   = 8;
   public static final int Exon                                = 9;
   public static final int Transcript_Translation_Position     = 10;
   public static final int Fragment                            = 11;
   public static final int Contig                              = 12;
   public static final int NonPublic_EST                          = 14;
   public static final int Species                             = 15;
   public static final int External_Chromosome                 = 101;
   public static final int External_Scaffold                   = 102;
   public static final int External_Transcript                 = 103;
   public static final int External_Gene                       = 104;
   public static final int External_Protein                    = 105;
   public static final int External_SNP                        = 106;
   public static final int External_Fragment                   = 111;
   public static final int External_Contig                     = 112;
   public static final int DB_EST                              = 114;
   public static final int External_STS_Primier                = 124;
   public static final int External_Repeat_Library             = 126;
   public static final int External_Clone                      = 127;
   public static final int BlastX_Hit                          = 201;
   public static final int BlastN_Hit                          = 202;
   public static final int Repeat_Masker_Hit                   = 203;
   public static final int TRIEST                              = 204;
   public static final int Sim4_Hit                            = 205;
   public static final int Genewise_Peptide_Hit                = 206;
   public static final int EPCR_Hit                            = 207;
   public static final int tRNAScan                            = 208;
   public static final int tBlastX                             = 209;
   public static final int tBlastN                             = 210;
   public static final int Atalanta_Hit                        = 216;
   public static final int ESTMapper_Hit                       = 217;
   public static final int GRAIL                               = 301;
   public static final int Genscan_Feature                     = 302;
   public static final int FgenesH                             = 303;
   public static final int Twinscan                            = 304;
   public static final int Otto                                = 305;
   public static final int CpG_Island                          = 306;
   public static final int Glimmer                             = 307;
   public static final int High_Scoring_Pair                   = 401;
   public static final int Sim4_Feature_Detail                 = 405;
   public static final int Genewise_Peptide_Hit_Part           = 406;
   public static final int Atalanta_Feature_Detail             = 408;
   public static final int ESTMapper_Feature_Detail            = 409;
   public static final int GRAIL_Detail                        = 411;
   public static final int Genscan_Feature_Detail              = 412;
   public static final int FgenesH_Detail                      = 413;
   public static final int Twinscan_Feature_Detail             = 414;
   public static final int Otto_Detail                         = 415;
   public static final int STS_Alignment                       = 421;
   public static final int Clone_Alignment                     = 422;
}

