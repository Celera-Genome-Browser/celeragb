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

import java.util.HashMap;
import java.util.Map;


/**
 * This class is used to write log entries to different Loggers
 * based on a name associated with each trace.
 * It acts as a Facade for multiple Loggers
 */
public class MultiLoggerFacade implements TraceLogger {
    public static final Logger NULL_LOGGER = new LoggerAdapter();

    /** Map of String traceNames to Logger objects */
    protected Map traces;

    /**
     * Contructor.
     * Calls initTraces which can be overriden by subclasses to change the
     * implementation of the traces Map.
     */
    public MultiLoggerFacade() {
        this.initTraces();
    }

    /**
     * This is called by the Contructor to initialize the traces Map.
     * The default implementation is a Hashtable. Subclasses can override
     * this method to change that implementation.
     */
    protected void initTraces() {
        this.traces = new HashMap();
    }

    /** Add a Logger */
    public void addLogger(final String traceName, final Logger logger) {
        if (logger != null) {
            traces.put(traceName, logger);
        }
    }

    /**
     * Remove a Logger
     * @param traceName is the name of the trace associated with the Logger
     * @return the Logger removed or null if it was not there
     */
    public Logger removeLogger(final String traceName) {
        return (Logger) traces.remove(traceName);
    }

    /**
     * Get the named Logger. If it does not exist, returns NULL_LOGGER
     * @param traceName is the name of the trace associated with the Logger
     *        to be retrieved
     * @return the Logger associated with the traceName or NULL_LOGGER
     *         if it does not exist. This method should never return null.
     */
    public Logger getLogger(final String traceName) {
        Logger logger = (Logger) traces.get(traceName);

        if (logger == null) {
            logger = NULL_LOGGER;
        }

        return logger;
    }

    /**
     * Write message to the specified log trace.
     * If the trace does not exist then nothing is done.
     * @see shared.log.Logger#writeLog
     */
    public void writeLog(final String traceName, final String message) {
        final Logger logger = (Logger) traces.get(traceName);

        if (logger != null) {
            logger.writeLog(message);
        }
    }

    /**
     * Write message to the specified log trace.
     * If the trace does not exist then nothing is done.
     * @see shared.log.Logger#writeLog
     */
    public void writeLog(final String traceName, final Throwable error) {
        final Logger logger = (Logger) traces.get(traceName);

        if (logger != null) {
            logger.writeLog(error);
        }
    }

    /**
     * Write message to the specified log trace.
     * If the trace does not exist then nothing is done.
     * @see shared.log.Logger#writeLog
     */
    public void writeLog(final String traceName, final Throwable error, final String message) {
        final Logger logger = (Logger) traces.get(traceName);

        if (logger != null) {
            logger.writeLog(error, message);
        }
    }
}