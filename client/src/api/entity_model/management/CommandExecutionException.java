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
package api.entity_model.management;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * This exception is used to note that though a command's preconditions were
 * valid, the command failed during execution.
 * The exception should only be thrown AFTER the command "validate postconditions"
 * phase DURING the command "execution" phase, and after some changes may
 * have been made to the subject / target of the command.  The subject features
 * may be in an unrelaiable state.
 * @author Jay T. Schira
 * @version $Id$
 */

public class CommandExecutionException extends CommandException {

  public CommandExecutionException(Command aSourceCommand, String executionString) {
    super(aSourceCommand, "Error in Command Execution! " + executionString +
      "\nData in unpredictable state.");
  }
}