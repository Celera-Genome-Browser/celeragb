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
/*********************************************************************
  *********************************************************************
    CVS_ID:  $Id$
  *********************************************************************/
package api.facade.concrete_facade.xml;

import api.facade.abstract_facade.annotations.BlastHitFacade;

/**
 * Super class for all blast hit facades.
 */
public class XmlBlastHitFacade extends XmlHitAlignmentFacade implements BlastHitFacade {

  /**
   * Returns ???
   */
  /*public String getRawAlignmentText (OID featureOID) throws BlastHitError, NoData {
    return ( "*********FAKE DATA***********\n" +
	     "Query: 11755 STP-TTSRKSRRRSNLFIPSSSKKADKEK---EPKSSELGSGRSIPIKQGYLYKRSSKSL 11588\nSTP +  R ++RR++LF  ++ + +D EK   + +    GSGR+IPIKQ +L KRS  SL\nSbjct:   298 STPGSLHRAAKRRTSLF--ANRRGSDSEKRSLDSRGETTGSGRAIPIKQSFLLKRSGNSL 355\n\nQuery: 11587 NKEWKKKYVTLCDDGRLTYHPSLHDYMDDVHGKEIPLQYVTVKVPGQKPRGSKSIITNSA 11408\nNKEWKKKYVTL  +G L YHPS++DY+   HGKE+ L   TVKVPG++P  + S    SA\nSbjct:   356 NKEWKKKYVTLSSNGFLLYHPSINDYIHSTHGKEMDLLRTTVKVPGKRPPRAISAFGPSA 415\n\nQuery: 11407 LTSSLMANGQRAQNTLSDGIGCLTLAKDNQRKLSEKLSLLGAGSIAAGAGGEPLKSNSSQ 11228\n+ L+ +    Q  + +G+              E  + + + S +  +   P    S  \nSbjct:   416 SINGLVKDMSTVQ--MGEGL--------------EATTPMPSPSPSPSSLQPPPDQTSKH 459\n\nQuery: 11227 QTSGDEGIAMSNSNSQTFIAGEVANAGNKLEAQTPNVKKRHRRMKSSSVK----ANEADD 11060\n" +
	     "D  +A + S   T  +G+++    +    +P VKK+ R+  ++  K    A +A++\nSbjct:   460 LLKPDRNLARALSTDCT-PSGDLSPLSRE-PPPSPMVKKQRRKKLTTPSKTEGSAGQAEE 517\n\nQuery: 11059 NDGYEFYIVSLDSKQWHFEAANSEERDEWVAAVEQEIFKSLQSIESSKTKQATST--DLA 10886\n+ +EF IVS   + WHFEAA+ EERD WV A+E +I  SLQ  ESSK K  T +  +  \nSbjct:   518 -ENFEFLIVSSTGQTWHFEAASFEERDAWVQAIESQILASLQCCESSKVKLRTDSQSEAV 576\n "
	     );
  } */// End method: getRawAlignmentText

} // End class: XmlBlastHitFacade

/*
  $Log$
  Revision 1.5  2002/11/07 16:06:14  lblick
  Removed obsolete imports and unused local variables.

  Revision 1.4  2002/09/27 20:34:43  wenmn
  comment out getRawAlignmentText()

  Revision 1.3  2000/09/21 22:08:13  lfoster
  Making XML facades conform more closely to their implemented abstract_facade
  interfaces.  Theory: if it ain't in interface, it ain't called.  Afterwards, found that
  XmlSim4HitFacade and XmlLapHitFacade are empty, but may need properties
  added to them if possible.  Also moved some code from HSP to HitAlignment,
  because it is not used in the HSP abstract facade anymore.

  Revision 1.2  2000/09/06 20:05:27  lfoster
  Cleaned up confusion over individual versus summary expect value.  Was just
  presenting a property of Expect, with no clear source.

  Revision 1.1  2000/07/14 22:14:03  jbaxenda
  Class to replace BLASTX and BLASTN

  Revision 1.5  2000/06/19 18:44:24  BhandaDn
  Changes to reflect modifications in XmlGenomicFacade

  Revision 1.4  2000/03/31 21:32:43  tsaf
  Cleaned out "dead code"

  Revision 1.3  2000/03/17 17:32:41  mharris
  Fixes for blast hit alignment.

  Revision 1.2  2000/03/17 16:58:09  mharris
  Fixes for blast hit alignment.

  Revision 1.1  2000/02/17 23:54:28  simpsomd
  Changes to support Protocol-independence and cleanup

  Revision 1.3  2000/02/11 15:38:38  pdavies
  Changes for new Package Structure

  Revision 1.2  1999/10/07 21:53:43  BaxendJn
  Added interfaces for complex feature property retrieval and
  regenerated.

  Revision 1.1  1999/10/06 20:58:26  BaxendJn
  New xml based facades to match new feature subtypes

  Revision 1.1  1999/09/05 20:09:31  gregg
  Adding new facades for data loaded from GAME XML files.

*/
