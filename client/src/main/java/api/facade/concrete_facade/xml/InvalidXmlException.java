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
package api.facade.concrete_facade.xml;

/**
 * Title:        InvaliidXmlException
 * Description:  Exception to convey info about failed XML parse.
 * @author Les Foster
 * @version $Id$
 */
public class InvalidXmlException extends Throwable {

  StringBuffer msg = null;
  String fileName = null;
  String title = null;

  /** Simple constructor to allow set of message. */
  public InvalidXmlException(StringBuffer msg, String fileName, String title) {
    super("XML Parse Failure");
    this.msg = msg;
    this.fileName = fileName;
    this.title = title;
  } // End constructor

  /** Returns the parse error output. */
  public StringBuffer getParseData() { return msg; }

  /** Tells which file this was on. */
  public String getFileName() { return fileName; }

  /** Can be used for special presentation. */
  public String getTitle() { return title; }
} // End class
