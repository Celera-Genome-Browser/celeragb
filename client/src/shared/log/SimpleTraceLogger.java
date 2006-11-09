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
 * This class is a simple implementation of the TraceLogger interface.
 * All log traces are written out to the same PrintStream held by this object
 */
public final class SimpleTraceLogger implements TraceLogger, PrintStreamGetter
{
  private PrintStream       printStream = null;
  private PrintStreamGetter printStreamGetter = null;
  private final LogLineHeader logLineHeader;
  private boolean           onOffState;

  /**
   * Constructor
   * @param logLineHeader is an interface to an object than can produce
   *        and appropriate header line to be prepended to any log message
   * @param printStream is where the log messages will be printed
   * @param onOffState is whether the SimpleTraceLogger is on (true) or off (false).
   *        When it is off, no messages will be written to the printStream
   */
  public SimpleTraceLogger(final LogLineHeader logLineHeader,
                           final PrintStream printStream,
                           final boolean onOffState)
  {
    this.logLineHeader  = logLineHeader;
    this.printStream    = printStream;
    this.onOffState     = onOffState;
  }

  /**
   * Constructor
   * @param logLineHeader is an interface to an object than can produce
   *        and appropriate header line to be prepended to any log message
   * @param printStreamGetter fetches the PrintStream where the log messages will be printed
   * @param onOffState is whether the SimpleTraceLogger is on (true) or off (false).
   *        When it is off, no messages will be written to the printStream
   */
  public SimpleTraceLogger(final LogLineHeader logLineHeader,
                           final PrintStreamGetter printStreamGetter,
                           final boolean onOffState)
  {
    this.logLineHeader      = logLineHeader;
    this.printStreamGetter  = printStreamGetter;
    this.onOffState         = onOffState;
  }

  /** Get the object that is providing headers that are prepended to log line entries */
  public LogLineHeader getLogLineHeader()
  {
    return this.logLineHeader;
  }

  /**
   * Turn the logger on or off
   * @param onOffState turns the logger on if true, off if false.
   *        When the logger is off it will not print anything.
   */
  public void setOnOffState(final boolean onOffState)
  {
    this.onOffState = onOffState;
  }

  /** Get the stream to which messages are being printed */
  public PrintStream getPrintStream()
  {
    return (printStreamGetter != null) ?
      this.printStreamGetter.getPrintStream() :
      this.printStream;
  }

  /**
   * Writes a log entry complete with a line header.
   * Prints nothing if the onOffState is set to false.
   * @see shared.log.TraceLogger#writeLog(String,String)
   */
  public void writeLog(final String traceName, final String message)
  {
    if (onOffState == false) {
      return;
    }
    final String header = logLineHeader.getLogLineHeader(traceName);
    final PrintStream ps = this.getPrintStream();
    synchronized(ps) {
      ps.print(header);
      ps.println(message);
    }
  }

  /**
   * Writes a log entry complete with a line header.
   * Prints nothing if the onOffState is set to false.
   * @see shared.log.TraceLogger#writeLog(String,Throwable)
   */
  public void writeLog(final String traceName, final Throwable error)
  {
    if (onOffState == false) {
      return;
    }
    final String header = logLineHeader.getLogLineHeader(traceName);
    final PrintStream ps = this.getPrintStream();
    synchronized(ps) {
      ps.print(header);
      error.printStackTrace(ps);
    }
  }

  /**
   * Writes a log entry complete with a line header.
   * Prints nothing if the onOffState is set to false.
   * @see shared.log.TraceLogger#writeLog(String,Throwable,String)
   */
  public void writeLog(final String traceName, final Throwable error, final String message)
  {
    if (onOffState == false) {
      return;
    }
    final String header = logLineHeader.getLogLineHeader(traceName);
    final PrintStream ps = this.getPrintStream();
    synchronized(ps) {
      ps.print(header);
      ps.println(message);
      error.printStackTrace(ps);
    }
  }

}