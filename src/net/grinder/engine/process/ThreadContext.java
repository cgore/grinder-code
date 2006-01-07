// Copyright (C) 2004 Philip Aston
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

package net.grinder.engine.process;

import java.util.List;

import net.grinder.common.FilenameFactory;
import net.grinder.common.ThreadLifeCycleListener;
import net.grinder.common.SSLContextFactory;
import net.grinder.plugininterface.PluginThreadContext;
import net.grinder.script.Statistics;


/**
 * Package scope.
 *
 * @author Philip Aston
 * @version $Revision$
 */
interface ThreadContext {

  ThreadLogger getThreadLogger();

  FilenameFactory getFilenameFactory();

  Statistics getScriptStatistics();

  PluginThreadContext getPluginThreadContext();

  Object invokeTest(TestData testData, TestData.Callable callable)
    throws ShutdownException;

  SSLContextFactory getThreadSSLContextFactory();

  void setThreadSSLContextFactory(SSLContextFactory threadSSLFactory);

  List getThreadLifeCycleListeners();

  void registerThreadLifeCycleListener(ThreadLifeCycleListener listener);
}

