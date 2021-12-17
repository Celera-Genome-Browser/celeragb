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
import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is simply a PrintStream onto which you can attach other
 * PrintStream so that when you write something to this, it gets written
 * to all attached Streams as well
 */
public class MultiPrintStream extends PrintStream
{
  private List additionalStreams = new LinkedList();
  private boolean onOffState = true;

  /**
   * Create the stream just as you would create a PrintStream
   * @see java.io.PrintStream#PrintStream(OutputStream,boolean)
   */
  public MultiPrintStream(final OutputStream out, final boolean autoFlush)
  {
    super(out,autoFlush);
  }

  /**
   * Create the stream just as you would create a PrintStream
   * @see java.io.PrintStream#PrintStream(OutputStream)
   */
  public MultiPrintStream(final OutputStream out)
  {
    super(out);
  }

  /**
   * Add an additional stream to be written to
   * @param ps a valid PrintStream to which to write
   */
  public void addStream(final PrintStream ps)
  {
    this.additionalStreams.add(ps);
  }

  /**
   * Remove one of the additional PrintStreams previously added
   * @param ps is the PrintStream reference to removed for this MultiPrintStream
   */
  public boolean removeStream(final PrintStream ps) {
    return this.additionalStreams.remove(ps);
  }

  /**
   * Overriden method used to affect all added streams
   * @see java.io.PrintStream#checkError()
   */
  public boolean checkError()
  {
    if (super.checkError()) {
      return true;
    }
    for (Iterator iter = this.additionalStreams.iterator();
         iter.hasNext(); ) {
      final PrintStream ps = (PrintStream)iter.next();
      if (ps.checkError()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Overriden method used to affect all added streams
   * @see java.io.PrintStream#close()
   */
  public void close()
  {
    super.close();
    for (Iterator iter = this.additionalStreams.iterator();
         iter.hasNext(); ) {
      final PrintStream ps = (PrintStream)iter.next();
      ps.close();
    }
  }

  /**
   * Overriden method used to affect all added streams
   * @see java.io.PrintStream#flush()
   */
  public void flush()
  {
    super.flush();
    for (Iterator iter = this.additionalStreams.iterator();
         iter.hasNext(); ) {
      final PrintStream ps = (PrintStream)iter.next();
      ps.flush();
    }
  }

  /**
   * Turn the object on or off (true or false).
   * When on, it prints as normal.
   * When off, it will print nothing.
   * @param onOffState true=on, false=off
   */
  public void setOnOffState(final boolean onOffState)
  {
    this.onOffState = onOffState;
  }

  /**
   * Get the On-Off state
   * @see #setOnOffState(boolean)
   */
  public boolean getOnOffState()
  {
    return this.onOffState;
  }

  /**
   * Overridden write method used to fork the printed information to all streams.
   * Since all print and println methods call this, they do not need to be
   * overridden themselves
   * @see java.io.PrintStream#write(byte[],int,int)
   */
  public void write(final byte[] buf, final int off, final int len)
  {
    if (! this.getOnOffState()) {
      return;
    }
    super.write(buf,off,len);
    for (Iterator iter = this.additionalStreams.iterator();
         iter.hasNext(); ) {
      final PrintStream ps = (PrintStream)iter.next();
      ps.write(buf,off,len);
    }
  }

  /**
   * Overridden write method used to fork the printed int to all streams.
   * @see java.io.PrintStream#write(int)
   */
  public void write(final int b)
  {
    if (! this.getOnOffState()) {
      return;
    }
    super.write(b);
    for (Iterator iter = this.additionalStreams.iterator();
         iter.hasNext(); ) {
      final PrintStream ps = (PrintStream)iter.next();
      ps.write(b);
    }
  }

}