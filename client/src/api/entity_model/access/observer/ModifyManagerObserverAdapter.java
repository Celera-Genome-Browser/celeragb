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
package api.entity_model.access.observer;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

public abstract class ModifyManagerObserverAdapter implements ModifyManagerObserver {


  public void noteCanUndo(String undoCommandName) {  }
  public void noteCanRedo(String redoCommandName) {  }
  public void noteNoUndo() {  }
  public void noteNoRedo() {  }
  public void noteCommandDidStart(String commandName) { }
  public void noteCommandDidFinish(String commandName, int commandKind) { }
  public void noteCommandPreconditionException(String commandName) { }
  public void noteCommandExecutionException(String commandName) { }
  public void noteCommandPostconditionException(String commandName) { }
  public void noteCommandStringHistoryListNonEmpty(){}
  public void noteCommandStringHistoryListEmpty(){}

}