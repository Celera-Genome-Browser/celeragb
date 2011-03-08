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
package api.stub.ejb.model.genetics;
/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
 *********************************************************************/

import javax.ejb.CreateException;
import java.rmi.RemoteException;

/**
 * Home interface for the GenomeLocator bean. Clients make use of the
 * GenomeLocator bean to obtain a SpeciesIdentity required for the construction
 * of this bean.
 *
 * @author James Baxendale <james.baxendale>
 * @version 1.0
 */
public interface GenomeLocatorHome extends javax.ejb.EJBHome
{
  /**
   * Creates a GenomeLocator bean instance for the exclusive use of the
   * client that can be used to locate a particular genome version for the
   * given species.
   *
   * @return Returns the remote interface supported by the GenomeLocator bean
   * if the SpeciesIdentity provided is valid. Throws a RemoteException
   * otherwise.
   */
  public GenomeLocatorRemote create()
    throws CreateException, RemoteException;
}

/*
$Log$
Revision 1.1  2006/11/09 21:36:13  rjturner
Initial upload of source

Revision 1.6  2003/11/17 04:26:47  simpsomd
Minor updates recomended by static code analysis tool

Revision 1.5  2002/11/07 18:38:32  lblick
Removed obsolete imports and unused local variables.

Revision 1.4  2001/06/20 19:14:26  simpsomd
Update in a comment

Revision 1.3  2001/03/13 17:29:40  jbaxenda
Removing bizobj classes and any references to them from server and client code.

Revision 1.2  2001/02/06 23:01:14  tsaf
Here we go...

Revision 1.1.2.1  2001/01/26 15:47:45  jbaxenda
Reverted some code to get compilation working.

Revision 1.1  2000/11/10 20:16:16  wenmn
Move from concrete_facade/ejb/genetics

Revision 1.4  2000/10/18 22:46:24  grahamkj
For single server - specifically the cookie implementation

Revision 1.3  2000/10/18 15:26:45  grahamkj
New for the single server model

Revision 1.2  2000/10/04 15:15:51  simpsomd
Replaced usage of SpeciesIdentity with new Species bizobj

Revision 1.1  2000/02/22 03:10:32  jbaxenda
Remote and home interfaces that make up the EAP API

Revision 1.2  2000/02/14 21:46:46  jbaxenda
Added more complete documentation for javadoc
generation.

Revision 1.1  2000/01/26 20:47:48  jbaxenda
Home interface for the GenomeLocator session bean.

*/
