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

import java.io.PrintStream;

/**
 * This class delegates to SimpleTraceLogger by holding on to its default traceName.
 * You can create one of these giving it a trace name, than use it as a
 * Logger without its callers needing to know to which trace it is set
 */
public final class TraceStateLogger implements Logger
{
  private String traceName;
  private TraceLogger traceLogger;

  /**
   * Like SimpleLogger constructor but adds a traceName which is held as
   * part of this objects state
   * @param traceName is the name associated with this objects log trace PrintStream
   * @see shared.log.SimpleTraceLogger#SimpleTraceLogger
   */
  public TraceStateLogger(final String traceName,
                          final LogLineHeader logLineHeader,
                          final PrintStream printStream,
                          final boolean onOffState)
  {
    this.traceLogger = new SimpleTraceLogger(logLineHeader, printStream, onOffState);
    this.traceName = traceName;
  }

  /**
   * Like SimpleLogger constructor but adds a traceName which is held as
   * part of this objects state
   * @param traceName is the name associated with this objects log trace PrintStream
   * @see shared.log.SimpleTraceLogger#SimpleTraceLogger
   */
  public TraceStateLogger(final String traceName,
                          final LogLineHeader logLineHeader,
                          final PrintStreamGetter printStreamGetter,
                          final boolean onOffState)
  {
    this.traceLogger = new SimpleTraceLogger(logLineHeader, printStreamGetter, onOffState);
    this.traceName = traceName;
  }


  /** Get this object's trace name */
  public String getTraceName()
  {
    return traceName;
  }


  /** @see shared.log.Logger#writeLog(String) */
  public void writeLog(final String message)
  {
    this.traceLogger.writeLog(traceName,message);
  }

  /** @see shared.log.Logger#writeLog(Throwable) */
  public void writeLog(final Throwable error)
  {
    this.traceLogger.writeLog(traceName,error);
  }

  /** @see shared.log.Logger#writeLog(Throwable,String) */
  public void writeLog(final Throwable error, final String message)
  {
    this.traceLogger.writeLog(traceName,error,message);
  }


}