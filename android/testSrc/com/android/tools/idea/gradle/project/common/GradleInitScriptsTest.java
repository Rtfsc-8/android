/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.idea.gradle.project.common;

import com.android.tools.idea.gradle.util.EmbeddedDistributionPaths;
import com.intellij.testFramework.LightPlatformTestCase;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.android.tools.idea.testing.FileSubject.file;
import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;
import static com.intellij.openapi.util.io.FileUtil.loadFile;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Tests for {@link GradleInitScripts}.
 */
public class GradleInitScriptsTest extends LightPlatformTestCase {
  @Mock private GradleInitScripts.ContentCreator myContentCreator;

  private File myInitScriptPath;
  private GradleInitScripts myInitScripts;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    initMocks(this);
    myInitScripts = new GradleInitScripts(EmbeddedDistributionPaths.getInstance(), myContentCreator);
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      if (myInitScriptPath != null) {
        boolean deleted = myInitScriptPath.delete();
        if (!deleted) {
          // We need to delete the file, otherwise the file names will end with numbers (e.g. "asLocalRepo1.gradle".
          fail(String.format("Failed to delete file '%1$s'", myInitScriptPath.getPath()));
        }
      }
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      super.tearDown();
    }
  }

  public void testAddLocalMavenRepoInitScriptCommandLineArgTo() throws IOException {
    String content = "Test";
    when(myContentCreator.createLocalMavenRepoInitScriptContent(any())).thenReturn(content);

    List<String> args = new ArrayList<>();
    myInitScripts.addLocalMavenRepoInitScriptCommandLineArg(args);
    assertThat(args).hasSize(2);

    assertEquals("--init-script", args.get(0));
    String initScriptTextPath = args.get(1);
    assertNotNull(initScriptTextPath);

    myInitScriptPath = new File(initScriptTextPath);
    assertAbout(file()).that(myInitScriptPath).isFile();
    assertEquals("sync.local.repo.gradle", myInitScriptPath.getName());

    String actual = loadFile(myInitScriptPath);
    assertEquals(content, actual);
  }

  public void testAddLocalMavenRepoInitScriptCommandLineArgToWhenFailedToCreateContent() {
    when(myContentCreator.createLocalMavenRepoInitScriptContent(any())).thenReturn(null);

    List<String> args = new ArrayList<>();
    myInitScripts.addLocalMavenRepoInitScriptCommandLineArg(args);
    assertThat(args).isEmpty();
  }

  public void testAddApplyJavaLibraryPluginInitScriptCommandLineArg() throws IOException {
    String content = "Test";
    when(myContentCreator.createApplyJavaLibraryPluginInitScriptContent()).thenReturn(content);

    List<String> args = new ArrayList<>();
    myInitScripts.addApplyJavaLibraryPluginInitScriptCommandLineArg(args);
    assertThat(args).hasSize(2);

    assertEquals("--init-script", args.get(0));
    String initScriptTextPath = args.get(1);
    assertNotNull(initScriptTextPath);

    myInitScriptPath = new File(initScriptTextPath);
    assertAbout(file()).that(myInitScriptPath).isFile();
    assertEquals("sync.java.lib.gradle", myInitScriptPath.getName());

    String actual = loadFile(myInitScriptPath);
    assertEquals(content, actual);
  }
}