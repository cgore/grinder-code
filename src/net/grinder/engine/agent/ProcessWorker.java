// Copyright (C) 2000 Paco Gomez
// Copyright (C) 2000, 2001, 2002, 2003, 2004, 2005 Philip Aston
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

package net.grinder.engine.agent;

import java.io.InputStream;
import java.io.OutputStream;

import net.grinder.common.WorkerIdentity;
import net.grinder.engine.common.EngineException;
import net.grinder.util.StreamCopier;


/**
 * This class knows how to start a child process. It redirects the
 * child process standard output and error streams to our streams.
 *
 * @author Paco Gomez
 * @author Philip Aston
 * @version $Revision$
 * @see net.grinder.engine.process.GrinderProcess
 *
 */
final class ProcessWorker implements Worker {

  private final WorkerIdentity m_workerIdentity;
  private final Process m_process;
  private final Thread m_stdoutRedirectorThread;
  private final Thread m_stderrRedirectorThread;

  /**
   * Constructor.
   *
   * @param workerName The process name.
   * @param commandArray Command line arguments.
   * @param outputStream Output stream to which child process stdout
   * should be redirected.
   * @param errorStream Output stream to which child process stderr
   * should be redirected.
   * @throws EngineException If an error occurs.
   */
  public ProcessWorker(WorkerIdentity workerIdentity,
                       String[] commandArray,
                       OutputStream outputStream,
                       OutputStream errorStream)
    throws EngineException {

    m_workerIdentity = workerIdentity;

    try {
      m_process = Runtime.getRuntime().exec(commandArray);
    }
    catch (Exception e) {
      throw new EngineException("Could not start process", e);
    }

    m_stdoutRedirectorThread =
      createRedirectorThread(m_process.getInputStream(), outputStream);

    m_stderrRedirectorThread =
      createRedirectorThread(m_process.getErrorStream(), errorStream);
  }

  /**
   * Return the worker name.
   *
   * @return The worker name.
   */
  public WorkerIdentity getIdentity() {
    return m_workerIdentity;
  }

  /**
   * Return an output stream connected to the input stream for the
   * child process.
   *
   * @return The stream.
   */
  public OutputStream getCommunicationStream() {
    return m_process.getOutputStream();
  }

  /**
   * Wait until the worker has completed. Return the exit status.
   *
   * @return See {@link net.grinder.engine.process.GrinderProcess} for
   * valid values.
   * @throws EngineException If an error occurs.
   * @throws InterruptedException If this thread is interrupted whilst
   * waiting.
   */
  public int waitFor() throws InterruptedException, EngineException {

    m_process.waitFor();

    m_stdoutRedirectorThread.join();
    m_stderrRedirectorThread.join();

    try {
      return m_process.exitValue();
    }
    catch (IllegalThreadStateException e) {
      // Can't happen.
      throw new EngineException("Unexpected exception", e);
    }
  }

  /**
   * Destroy the worker.
   */
  public void destroy() {
    m_process.destroy();

    try {
      m_stdoutRedirectorThread.join();
    }
    catch (InterruptedException e) {
      // Swallow.
    }

    try {
      m_stderrRedirectorThread.join();
    }
    catch (InterruptedException e) {
      // Swallow.
    }
  }

  private Thread createRedirectorThread(InputStream inputStream,
                                        OutputStream outputStream) {

    final Thread thread =
      new Thread(
        new StreamCopier(4096, true).getRunnable(inputStream, outputStream),
        "Stream redirector for process " + m_process);

    thread.setDaemon(true);
    thread.start();

    return thread;
  }
}