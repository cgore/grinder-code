// Copyright (C) 2001, 2002, 2003, 2004, 2005, 2006 Philip Aston
// All rights reserved.
//
// This file is part of The Grinder software distribution. Refer to
// the file LICENSE which is part of The Grinder distribution for
// licensing details. The Grinder distribution is available on the
// Internet at http://grinder.sourceforge.net/
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.

package net.grinder.script;

import net.grinder.common.FilenameFactory;
import net.grinder.common.GrinderException;
import net.grinder.common.GrinderProperties;
import net.grinder.common.Logger;
import net.grinder.statistics.StatisticsView;


/**
 * Namespace for <code>grinder</code> script context object.
 *
 * @author Philip Aston
 * @version $Revision$
 */
public class Grinder {

  /**
   * Object that provides context services to scripts.
   */
  public static ScriptContext grinder;

  /**
   * Scripts can get contextual information through a global
   * <code>net.grinder.script.Grinder.grinder</code> object that
   * supports this interface.
   *
   * @author Philip Aston
   * @version $Revision$
   */
  public static interface ScriptContext {

    /**
     * Get an unique name for this worker process.
     *
     * @return The id.
     */
    String getProcessName();

    /**
     * Return the thread ID, or -1 if not called from a worker thread.
     * @return The thread ID.
     */
    int getThreadID();

    /**
     * Return the current run number, or -1 if not called from a
     * worker thread.
     *
     * @return An <code>int</code> value.
     */
    int getRunNumber();

    /**
     * Get a {@link net.grinder.common.Logger}.
     *
     * @return A <code>Logger</code>.
     */
    Logger getLogger();

    /**
     * Sleep for a time based on the meanTime parameter. The actual
     * time may be greater or less than meanTime, and is distributed
     * according to a pseudo normal distribution.
     *
     * @param meanTime Mean time in milliseconds.
     * @exception GrinderException If the sleep failed.
     */
    void sleep(long meanTime) throws GrinderException;

    /**
     * Sleep for a time based on the meanTime parameter. The actual
     * time may be greater or less than meanTime, and is distributed
     * according to a pseudo normal distribution.
     *
     * @param meanTime Mean time in milliseconds.
     * @param sigma The standard deviation, in milliseconds.
     * @exception GrinderException If the sleep failed.
     */
    void sleep(long meanTime, long sigma) throws GrinderException;

    /**
     * Get a {@link net.grinder.common.FilenameFactory} that can be
     * used to create unique filenames. The filenames depend upon the
     * process name and the thread used to call the
     * <code>FilenameFactory</code>.
     *
     * @return A <code>FilenameFactory</code>.
     */
    FilenameFactory getFilenameFactory();

    /**
     * Get the global properties for this agent/worker process set.
     *
     * @return The properties.
     */
    GrinderProperties getProperties();

    /**
     * Register a new "summary" statistics view. These views appear in
     * the worker process output log summaries and are displayed in the
     * console.
     *
     * @param statisticsView The new statistics view.
     * @exception GrinderException If the view could not be registered.
     */
    void registerSummaryStatisticsView(StatisticsView statisticsView)
      throws GrinderException;

    /**
     * Register a new "detail" statistics view which appears in the
     * worker process data logs. Each test invocation will have an entry
     * displayed for the detail statistics views.
     *
     * <p>You should call <code>registerSummaryStatisticsView</code>
     * from the top level of your script. It cannot be called from a
     * worker thread - the data logs are initialised by the time the
     * worker threads start.</p>
     *
     * @param statisticsView The new statistics view.
     * @exception GrinderException If the view could not be registered.
     * @exception InvalidContextException If called from a worker
     * thread.
     */
    void registerDetailStatisticsView(StatisticsView statisticsView)
      throws GrinderException, InvalidContextException;

    /**
     * Get the Statistics for the calling worker thread. This provides
     * access to the statistics of the last test invoked by the thread.
     *
     * @return The statistics.
     * @exception InvalidContextException If called from a non-worker
     * thread.
     */
    Statistics getStatistics() throws InvalidContextException;


    /**
     * Get an {@link SSLControl}. This can be used to create secure
     * sockets or to set the certificates that a worker thread should
     * use.
     *
     * @return The SSL control.
     */
    SSLControl getSSLControl();
  }
}
