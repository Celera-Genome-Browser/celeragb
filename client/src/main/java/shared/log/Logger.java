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
package shared.log;

/**
 * This is an interface to a generic traced logging mechanism
*/
public interface Logger
{
  /**
   * Write the message to the log
   * @param message is the message to be written
   */
  public void writeLog(String message);

  /**
   * Write the error to the log
   * @param error is the Exception to be written
   */
  public void writeLog(Throwable error);

  /**
   * Write the error to the log
   * @param error is the Exception to be written
   * @param message is the additional message to be written
   */
  public void writeLog(Throwable error, String message);
}
