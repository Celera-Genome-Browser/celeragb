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

import java.io.OutputStream;

public class MultiPrintStreamLogger extends MultiPrintStream implements Logger
{
  private TraceStateLogger traceStateLogger;

  public MultiPrintStreamLogger(final String traceName, final LogLineHeader llheader,
                                final OutputStream out, final boolean autoFlush)
  {
    super(out,autoFlush);
    traceStateLogger = new TraceStateLogger(traceName,llheader,this,autoFlush);
  }

  public MultiPrintStreamLogger(final String traceName, final LogLineHeader llheader,
                                final OutputStream out)
  {
    super(out,true);
    traceStateLogger = new TraceStateLogger(traceName,llheader,this,true);
  }


  /** @see shared.log.Logger#writeLog(String) */
  public void writeLog(final String message)
  {
    this.traceStateLogger.writeLog(message);
  }

  /** @see shared.log.Logger#writeLog(Throwable) */
  public void writeLog(final Throwable error)
  {
    this.traceStateLogger.writeLog(error);
  }

  /** @see shared.log.Logger#writeLog(Throwable,String) */
  public void writeLog(final Throwable error, final String message)
  {
    this.traceStateLogger.writeLog(message);
  }
}