// Copyright (C) 2000 Paco Gomez
// Copyright (C) 2000, 2001, 2002 Philip Aston
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

import java.io.PrintWriter;

import net.grinder.common.GrinderException;
import net.grinder.common.GrinderProperties;
import net.grinder.common.Logger;
import net.grinder.common.Test;
import net.grinder.communication.CommunicationDefaults;
import net.grinder.communication.Message;
import net.grinder.communication.RegisterStatisticsViewMessage;
import net.grinder.communication.Sender;
import net.grinder.communication.UnicastSender;
import net.grinder.engine.EngineException;
import net.grinder.plugininterface.PluginProcessContext;
import net.grinder.statistics.CommonStatisticsViews;
import net.grinder.statistics.ExpressionView;
import net.grinder.statistics.StatisticsView;
import net.grinder.statistics.TestStatisticsFactory;



/**
 * @author Philip Aston
 * @version $Revision$
 * @stereotype singleton
 **/
class ProcessContext implements PluginProcessContext
{
    private final String m_grinderID;
    private final GrinderProperties m_properties;
    private final GrinderProperties m_pluginParameters;
    private final boolean m_recordTime;
    private final LoggerImplementation m_loggerImplementation;
    private final Logger m_processLogger;
    private final Sender m_consoleSender;
    private final PluginRegistry m_pluginRegistry;
    private final TestRegistry m_testRegistry;

    private boolean m_shouldWriteTitleToDataWriter;

    private final TestStatisticsFactory m_testStatisticsFactory =
	TestStatisticsFactory.getInstance();

    ProcessContext(String grinderID, GrinderProperties properties)
	throws GrinderException
    {
	m_grinderID = grinderID;
	m_properties = properties;

	m_pluginParameters =
	    properties.getPropertySubset("grinder.plugin.parameter.");

	m_recordTime = properties.getBoolean("grinder.recordTime", true);

	final boolean appendLog =
	    properties.getBoolean("grinder.appendLog", false);

	m_loggerImplementation =
	    new LoggerImplementation(m_grinderID, 
				     properties.getProperty(
					 "grinder.logDirectory", "."),
				     properties.getBoolean(
					 "grinder.logProcessStreams", true),
				     appendLog);

	m_processLogger = m_loggerImplementation.createProcessLogger();

	m_shouldWriteTitleToDataWriter = !appendLog;

	if (properties.getBoolean("grinder.reportToConsole", true)) {
	    final String consoleAddress =
		properties.getProperty("grinder.consoleAddress",
				       CommunicationDefaults.CONSOLE_ADDRESS);

	    final int consolePort =
		properties.getInt("grinder.console.consolePort",
				  CommunicationDefaults.CONSOLE_PORT);

	    if (consoleAddress != null && consolePort > 0) {
		m_consoleSender =
		    new UnicastSender(getGrinderID(), consoleAddress,
				      consolePort);
	    }
	    else {
		throw new EngineException(
		    "Unable to report to console: " +
		    "console address or console port not specified");
	    }
	}
	else {
	    // Null Sender implementation.
	    m_consoleSender =
		new Sender() {
		    public void send(Message message) {}
		    public void flush() {}
		    public void queue(Message message) {}
		    public void shutdown() {}
		};
	}

	m_pluginRegistry = new PluginRegistry(this);
	m_testRegistry =
	    new TestRegistry(m_pluginRegistry, getConsoleSender());
    }

    void initialiseDataWriter()
    {
	if (m_shouldWriteTitleToDataWriter) {
	    final PrintWriter dataWriter =
		m_loggerImplementation.getDataWriter();

	    dataWriter.print("Thread, Run, Test");

	    final ExpressionView[] detailExpressionViews =
		CommonStatisticsViews.getDetailStatisticsView()
		.getExpressionViews();

	    for (int i=0; i<detailExpressionViews.length; ++i) {
		dataWriter.print(", " +
				 detailExpressionViews[i].getDisplayName());
	    }

	    dataWriter.println();
	}
    }

    final Sender getConsoleSender()
    {
	return m_consoleSender;
    }

    final LoggerImplementation getLoggerImplementation()
    {
	return m_loggerImplementation;
    }

    final PluginRegistry getPluginRegistry()
    {
	return m_pluginRegistry;
    }

    final TestRegistry getTestRegistry()
    {
	return m_testRegistry;
    }

    public final String getGrinderID()
    {
	return m_grinderID;
    }

    public final GrinderProperties getProperties()
    {
	return m_properties;
    }

    public GrinderProperties getPluginParameters()
    {
	return m_pluginParameters;
    }

    public final void output(String message)
    {
	m_processLogger.output(message);
    }

    public final void output(String message, int where)
    {
	m_processLogger.output(message, where);
    }

    public final void error(String message)
    {
	m_processLogger.error(message);
    }
    
    public final void error(String message, int where)
    {
	m_processLogger.error(message, where);
    }

    public final PrintWriter getOutputLogWriter()
    {
	return m_processLogger.getOutputLogWriter();
    }

    public final PrintWriter getErrorLogWriter()
    {
	return m_processLogger.getErrorLogWriter();
    }

    public String createFilename(String prefix)
    {
	return
	    m_loggerImplementation.getFilenameFactory().createFilename(prefix);
    }

    public String createFilename(String prefix, String suffix)
    {
	return
	    m_loggerImplementation.getFilenameFactory().createFilename(prefix,
								       suffix);
    }

    public final boolean getRecordTime()
    {
	return m_recordTime;
    }

    protected final TestStatisticsFactory getTestStatisticsFactory()
    {
	return m_testStatisticsFactory;
    }

    public void registerSummaryStatisticsView(StatisticsView statisticsView)
	throws GrinderException
    {
	CommonStatisticsViews.getSummaryStatisticsView().add(statisticsView);

	getConsoleSender().queue(
	    new RegisterStatisticsViewMessage(statisticsView));
    }

    public void registerDetailStatisticsView(StatisticsView statisticsView)
	throws GrinderException
    {
	CommonStatisticsViews.getDetailStatisticsView().add(statisticsView);
    }
}
