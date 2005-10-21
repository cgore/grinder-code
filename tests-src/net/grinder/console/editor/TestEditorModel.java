// Copyright (C) 2004, 2005 Philip Aston
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

package net.grinder.console.editor;

import java.io.File;
import java.io.FileWriter;

import net.grinder.console.common.Resources;
import net.grinder.console.common.ResourcesImplementation;
import net.grinder.console.distribution.AgentCacheState;

import net.grinder.testutility.AbstractFileTestCase;
import net.grinder.testutility.CallData;
import net.grinder.testutility.DelegatingStubFactory;
import net.grinder.testutility.RandomStubFactory;


/**
 * Unit test for {@link EditorModel}.
 *
 * @author Philip Aston
 * @version $Revision$
 */
public class TestEditorModel extends AbstractFileTestCase {

  private static final Resources s_resources =
      new ResourcesImplementation("net.grinder.console.swingui.resources.Console");

  public void testConstruction() throws Exception {
    final StringTextSource.Factory stringTextSourceFactory =
      new StringTextSource.Factory();

    final DelegatingStubFactory textSourceFactoryStubFactory =
      new DelegatingStubFactory(stringTextSourceFactory);
    final TextSource.Factory textSourceFactory =
      (TextSource.Factory)textSourceFactoryStubFactory.getStub();

    final EditorModel editorModel =
      new EditorModel(s_resources, textSourceFactory, null);

    textSourceFactoryStubFactory.assertSuccess("create");
    textSourceFactoryStubFactory.assertNoMoreCalls();
    assertNotNull(stringTextSourceFactory.getLast().getText());
    assertNull(editorModel.getSelectedBuffer());
    assertEquals(1, editorModel.getBuffers().length);
    editorModel.selectDefaultBuffer();
    final Buffer defaultBuffer = editorModel.getSelectedBuffer();
    assertEquals(defaultBuffer, editorModel.getBuffers()[0]);
  }

  public void testSelectDefaultBuffer() throws Exception {

    final EditorModel editorModel =
      new EditorModel(s_resources, new StringTextSource.Factory(), null);

    final RandomStubFactory listener1StubFactory =
      new RandomStubFactory(EditorModel.Listener.class);
    final EditorModel.Listener listener1 =
      (EditorModel.Listener)listener1StubFactory.getStub();

    final RandomStubFactory listener2StubFactory =
      new RandomStubFactory(EditorModel.Listener.class);
    final EditorModel.Listener listener2 =
      (EditorModel.Listener)listener2StubFactory.getStub();

    editorModel.addListener(listener1);
    editorModel.addListener(listener2);

    editorModel.selectDefaultBuffer();

    assertNotNull(editorModel.getSelectedBuffer());
    assertNull(editorModel.getSelectedBuffer().getFile());
    listener1StubFactory.assertSuccess("bufferChanged", Buffer.class);
    listener1StubFactory.assertNoMoreCalls();
    listener2StubFactory.assertSuccess("bufferChanged", Buffer.class);
    listener2StubFactory.assertNoMoreCalls();

    // Select same buffer is a noop.
    editorModel.selectDefaultBuffer();

    listener1StubFactory.assertNoMoreCalls();
    listener2StubFactory.assertNoMoreCalls();
  }

  public void testSelectBufferForFile() throws Exception {
    final StringTextSource.Factory stringTextSourceFactory =
      new StringTextSource.Factory();

    final DelegatingStubFactory textSourceFactoryStubFactory =
      new DelegatingStubFactory(stringTextSourceFactory);
    final TextSource.Factory textSourceFactory =
      (TextSource.Factory)textSourceFactoryStubFactory.getStub();

    final EditorModel editorModel =
      new EditorModel(s_resources, textSourceFactory, null);

    textSourceFactoryStubFactory.resetCallHistory();

    final File file1 = createFile("myfile.txt", "blah");
    final File file2 = createFile("anotherFile.py", "Some stuffb");

    final RandomStubFactory listener1StubFactory =
      new RandomStubFactory(EditorModel.Listener.class);
    final EditorModel.Listener listener1 =
      (EditorModel.Listener)listener1StubFactory.getStub();

    final RandomStubFactory listener2StubFactory =
      new RandomStubFactory(EditorModel.Listener.class);
    final EditorModel.Listener listener2 =
      (EditorModel.Listener)listener2StubFactory.getStub();

    editorModel.addListener(listener1);
    editorModel.addListener(listener2);

    editorModel.selectBufferForFile(file1);

    assertNotNull(editorModel.getSelectedBuffer());
    assertEquals(file1, editorModel.getSelectedBuffer().getFile());
    textSourceFactoryStubFactory.assertSuccess("create");
    textSourceFactoryStubFactory.assertNoMoreCalls();

    final CallData callData = listener1StubFactory.getCallData();
    assertEquals("bufferAdded", callData.getMethodName());
    final Object[] parameters = callData.getParameters();
    assertEquals(1, parameters.length);
    final Buffer bufferForFile1 = (Buffer)parameters[0];
    assertSame(bufferForFile1, editorModel.getSelectedBuffer());
    listener1StubFactory.assertSuccess("bufferChanged", bufferForFile1);
    listener1StubFactory.assertNoMoreCalls();

    listener2StubFactory.assertSuccess("bufferAdded", bufferForFile1);
    listener2StubFactory.assertSuccess("bufferChanged", bufferForFile1);
    listener2StubFactory.assertNoMoreCalls();

    // Select same buffer is a noop.
    editorModel.selectBufferForFile(file1);

    assertSame(bufferForFile1, editorModel.getSelectedBuffer());
    listener1StubFactory.assertNoMoreCalls();
    listener2StubFactory.assertNoMoreCalls();

    editorModel.selectBufferForFile(file2);

    assertNotSame(bufferForFile1, editorModel.getSelectedBuffer());
    textSourceFactoryStubFactory.assertSuccess("create");
    textSourceFactoryStubFactory.assertNoMoreCalls();
    listener1StubFactory.assertSuccess("bufferAdded", Buffer.class);
    listener1StubFactory.assertSuccess("bufferChanged", bufferForFile1);
    listener1StubFactory.assertSuccess("bufferChanged", Buffer.class);
    listener1StubFactory.assertNoMoreCalls();
    listener2StubFactory.assertSuccess("bufferAdded", Buffer.class);
    listener2StubFactory.assertSuccess("bufferChanged", bufferForFile1);
    listener2StubFactory.assertSuccess("bufferChanged", Buffer.class);
    listener2StubFactory.assertNoMoreCalls();

    editorModel.selectBufferForFile(file1);

    textSourceFactoryStubFactory.assertNoMoreCalls();
    assertSame(bufferForFile1, editorModel.getSelectedBuffer());
    listener1StubFactory.assertSuccess("bufferChanged", Buffer.class);
    listener1StubFactory.assertSuccess("bufferChanged", bufferForFile1);
    listener1StubFactory.assertNoMoreCalls();
    listener2StubFactory.assertSuccess("bufferChanged", Buffer.class);
    listener2StubFactory.assertSuccess("bufferChanged", bufferForFile1);
    listener2StubFactory.assertNoMoreCalls();

    final StringTextSource textSource1 =
      (StringTextSource)bufferForFile1.getTextSource();

    textSource1.markDirty();
    textSource1.markDirty();
    textSource1.markDirty();
    textSource1.markDirty();
    listener1StubFactory.assertSuccess("bufferChanged", bufferForFile1);
    listener1StubFactory.assertNoMoreCalls();
    listener2StubFactory.assertSuccess("bufferChanged", bufferForFile1);
    listener2StubFactory.assertNoMoreCalls();
  }

  public void testSelectNewBuffer() throws Exception {
    final StringTextSource.Factory stringTextSourceFactory =
      new StringTextSource.Factory();

    final DelegatingStubFactory textSourceFactoryStubFactory =
      new DelegatingStubFactory(stringTextSourceFactory);
    final TextSource.Factory textSourceFactory =
      (TextSource.Factory)textSourceFactoryStubFactory.getStub();

    final RandomStubFactory listener1StubFactory =
      new RandomStubFactory(EditorModel.Listener.class);
    final EditorModel.Listener listener1 =
      (EditorModel.Listener)listener1StubFactory.getStub();

    final EditorModel editorModel =
      new EditorModel(s_resources, textSourceFactory, null);

    editorModel.selectDefaultBuffer();
    final Buffer defaultBuffer = editorModel.getSelectedBuffer();

    editorModel.addListener(listener1);

    textSourceFactoryStubFactory.resetCallHistory();

    editorModel.selectNewBuffer();
    textSourceFactoryStubFactory.assertSuccess("create");
    textSourceFactoryStubFactory.assertNoMoreCalls();

    final Buffer buffer1 = editorModel.getSelectedBuffer();
    assertNotSame(buffer1, defaultBuffer);

    listener1StubFactory.assertSuccess("bufferAdded", buffer1);
    listener1StubFactory.assertSuccess("bufferChanged", defaultBuffer);
    listener1StubFactory.assertSuccess("bufferChanged", buffer1);
    listener1StubFactory.assertNoMoreCalls();

    editorModel.selectNewBuffer();

    final Buffer buffer2 = editorModel.getSelectedBuffer();
    assertNotSame(buffer2, buffer1);

    listener1StubFactory.assertSuccess("bufferAdded", buffer2);
    listener1StubFactory.assertSuccess("bufferChanged", buffer1);
    listener1StubFactory.assertSuccess("bufferChanged", buffer2);
    listener1StubFactory.assertNoMoreCalls();
  }

  private File createFile(String name, String text) throws Exception {
    final File file = new File(getDirectory(), name);
    final FileWriter out = new FileWriter(file);
    out.write(text);
    out.close();

    return file;
  }

  public void testIsBoringFile() throws Exception {
    final EditorModel editorModel =
      new EditorModel(s_resources, new StringTextSource.Factory(), null);

    final File[] boring = {
      new File("some.class"),
      new File("~temporary"),
      new File("#BLAH BLAH"),
      new File("furble.exe"),
      new File("PIC.GIF"),
      new File("dfadhklfda.Jpeg"),
      new File("dfadhklfda.jpg"),
      new File("dfadhklfda.tiff"),
    };

    for (int i = 0; i < boring.length; ++i) {
      assertTrue("Is boring: " + boring[i],
                 editorModel.isBoringFile(boring[i]));
    }

    final File[] notBoring = {
      null,
      new File("Script.Py"),
      new File("some.java"),
      new File("my.properties"),
      new File("README"),
      new File("info.text"),
    };

    for (int i = 0; i < notBoring.length; ++i) {
      assertTrue("Isn't boring: " + notBoring[i],
                 !editorModel.isBoringFile(notBoring[i]));
    }
  }

  public void testIsPythonFile() throws Exception {
    final EditorModel editorModel =
      new EditorModel(s_resources, new StringTextSource.Factory(), null);

    final File[] python = {
      new File("my file.py"),
      new File(".blah.py"),
      new File("python.PY"),
      new File("~python.py"),
    };

    for (int i = 0; i < python.length; ++i) {
      assertTrue("Is python: " + python[i],
                 editorModel.isPythonFile(python[i]));
    }

    final File[] notPython = {
      null,
      new File("script.python"),
      new File("script.py "),
      new File("foo.bah"),
      new File("x.text"),
    };

    for (int i = 0; i < notPython.length; ++i) {
      assertTrue("Isn't python: " + notPython[i],
                 !editorModel.isPythonFile(notPython[i]));
    }
  }

  public void testCloseBufferAndIsABufferDirty() throws Exception {
    final EditorModel editorModel =
      new EditorModel(s_resources, new StringTextSource.Factory(), null);

    final RandomStubFactory listenerStubFactory =
      new RandomStubFactory(EditorModel.Listener.class);
    final EditorModel.Listener listener =
      (EditorModel.Listener)listenerStubFactory.getStub();

    final File file1 = createFile("myfile.txt", "blah");
    final File file2 = createFile("another.py", "blah");

    editorModel.selectBufferForFile(file1);
    final Buffer buffer1 = editorModel.getSelectedBuffer();

    editorModel.selectBufferForFile(file2);
    final Buffer buffer2 = editorModel.getSelectedBuffer();

    editorModel.selectDefaultBuffer();
    final Buffer defaultBuffer = editorModel.getSelectedBuffer();

    assertEquals(3, editorModel.getBuffers().length);

    assertTrue(!editorModel.isABufferDirty());

    editorModel.addListener(listener);

    editorModel.closeBuffer(defaultBuffer);

    listenerStubFactory.assertSuccess("bufferChanged", defaultBuffer);
    listenerStubFactory.assertSuccess("bufferChanged", buffer2);
    listenerStubFactory.assertSuccess("bufferRemoved", defaultBuffer);
    listenerStubFactory.assertNoMoreCalls();

    assertEquals(2, editorModel.getBuffers().length);

    assertEquals(buffer2, editorModel.getSelectedBuffer());

    assertTrue(!editorModel.isABufferDirty());

    ((StringTextSource)buffer1.getTextSource()).markDirty();

    assertTrue(editorModel.isABufferDirty());
    listenerStubFactory.assertSuccess("bufferChanged", buffer1);
    listenerStubFactory.assertNoMoreCalls();

    editorModel.closeBuffer(buffer1);

    assertTrue(!editorModel.isABufferDirty());
    listenerStubFactory.assertSuccess("bufferRemoved", buffer1);
    listenerStubFactory.assertNoMoreCalls();

    editorModel.closeBuffer(buffer1);
    editorModel.closeBuffer(defaultBuffer);
    listenerStubFactory.assertNoMoreCalls();

    editorModel.closeBuffer(buffer2);
    assertEquals(0, editorModel.getBuffers().length);

    assertTrue(!editorModel.isABufferDirty());

    listenerStubFactory.assertSuccess("bufferChanged", buffer2);
    listenerStubFactory.assertSuccess("bufferRemoved", buffer2);
    listenerStubFactory.assertNoMoreCalls();    
  }

  public void testSaveBufferAs() throws Exception {
    final StringTextSource.Factory stringTextSourceFactory =
      new StringTextSource.Factory();

    final RandomStubFactory agentCacheStateStubFactory =
      new RandomStubFactory(AgentCacheState.class);
    final AgentCacheState agentCacheState =
      (AgentCacheState)agentCacheStateStubFactory.getStub();

    final EditorModel editorModel =
      new EditorModel(s_resources, stringTextSourceFactory, agentCacheState);

    final RandomStubFactory listenerStubFactory =
      new RandomStubFactory(EditorModel.Listener.class);
    final EditorModel.Listener listener =
      (EditorModel.Listener)listenerStubFactory.getStub();

    editorModel.selectNewBuffer();
    final Buffer buffer = editorModel.getSelectedBuffer();
    stringTextSourceFactory.getLast().setText("Some text");

    final File file1 = new File(getDirectory(), "a file");
    final File file2 = new File(getDirectory(), "another  file");

    editorModel.addListener(listener);

    editorModel.saveBufferAs(buffer, file1);

    // Buffer changed because it is associated with a new file.
    listenerStubFactory.assertSuccess("bufferChanged", buffer);
    listenerStubFactory.assertNoMoreCalls();
    agentCacheStateStubFactory.assertSuccess("setOutOfDate",
                                             new Long(file1.lastModified()));
    agentCacheStateStubFactory.assertNoMoreCalls();

    editorModel.saveBufferAs(buffer, file1);
    listenerStubFactory.assertNoMoreCalls();
    agentCacheStateStubFactory.assertSuccess("setOutOfDate",
                                             new Long(file1.lastModified()));
    agentCacheStateStubFactory.assertNoMoreCalls();

    assertEquals(buffer, editorModel.getBufferForFile(file1));

    editorModel.saveBufferAs(buffer, file2);

    // Buffer changed because it is associated with a new file.
    listenerStubFactory.assertSuccess("bufferChanged", buffer);
    listenerStubFactory.assertNoMoreCalls();
    agentCacheStateStubFactory.assertSuccess("setOutOfDate",
                                             new Long(file2.lastModified()));
    agentCacheStateStubFactory.assertNoMoreCalls();

    assertNull(editorModel.getBufferForFile(file1));
    assertEquals(buffer, editorModel.getBufferForFile(file2));
  }

  public void testGetAndSetMarkedScript() throws Exception {
    final StringTextSource.Factory stringTextSourceFactory =
      new StringTextSource.Factory();

    final EditorModel editorModel =
      new EditorModel(s_resources, stringTextSourceFactory, null);

    assertNull(editorModel.getMarkedScript());

    final File f = new File(".");
    editorModel.setMarkedScript(f);
    assertSame(f, editorModel.getMarkedScript());
    editorModel.setMarkedScript(null);
    assertNull(editorModel.getMarkedScript());
  }
}
