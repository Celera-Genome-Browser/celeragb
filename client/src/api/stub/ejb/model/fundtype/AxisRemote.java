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
package api.stub.ejb.model.fundtype;


import api.entity_model.access.report.BlastParameters;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.fundtype.LoadRequest;
import api.stub.data.OID;
import api.stub.geometry.Range;
import api.stub.sequence.Sequence;

public interface AxisRemote extends api.stub.ejb.model.fundtype.GenomicFacade
{
  public Alignment[] loadAlignmentsToEntities(OID entityOID, LoadRequest loadRequest)
    throws java.rmi.RemoteException;

  Sequence getNucleotideSeq
    (OID genomicOID,
     Range nucleotideRange,
     boolean gappedSequence)
    throws java.rmi.RemoteException;

  /**
   * Performs a blast of the querySequence against the specified range on this
   * axis. Any Similarities found will be returned asynchronously through a
   * different chanel.
   *
   * @param axisOID object identifier of this axis
   * @param range range on the axis
   * @param querySeq either a nucleotide or protein sequence that should
   * be blasted against the sequence of this axis between <range> for
   * similarities. The information should be supplied in a valid FASTA file
   * format (you're on your own for
   * definition of the FASTA file format). Basically it is a single def line
   * and then multiple lines of sequence, each of which should not exceed
   * 80 nucleotides in length. The Sequence class is capable of formating the
   * sequence in FASTA style via the toFASTAFormat method.
   *
   * @param blastParameters optional parameters that the client can set to
   * control how the blast algorithm is applied.
   *
   */
  public String runBlast( OID axisOID, Range range, Sequence subjectSeq, Sequence querySeq,
                        String groupTag,
                        BlastParameters blastParameters)
    throws java.rmi.RemoteException;

  /**
   * Returns an array of BlastAlgorithm instances that are available for use
   * when blasting axis sequence (either as query or subject depending on
   * whether the blast against internal or external databases).
   */
  public BlastParameters.BlastAlgorithm[] getAvailableBlastAlgorithms(OID axisOID)
    throws java.rmi.RemoteException;
}
