/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.android.tools.idea.editors.layoutInspector;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.android.AndroidTestCase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class LayoutFileDataTest extends AndroidTestCase {
  public void testParsingLayoutFile() throws IOException {
    File testData = Paths.get(getTestDataPath(), "editors/layoutInspector/LayoutCapture.li").toFile();
    VirtualFile layoutFile = LocalFileSystem.getInstance().findFileByIoFile(testData);
    LayoutFileData fileData = new LayoutFileData(layoutFile);

    assertNotNull(fileData.myBufferedImage);
    assertEquals(1920, fileData.myBufferedImage.getHeight());
    assertEquals(1080, fileData.myBufferedImage.getWidth());

    assertNotNull(fileData.myNode);
    assertEquals(3, fileData.myNode.getChildCount());
  }
}