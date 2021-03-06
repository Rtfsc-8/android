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
package com.android.tools.idea.tests.gui.instantapp;

import com.android.tools.idea.instantapp.InstantAppUrlFinder;
import com.android.tools.idea.model.MergedManifest;
import com.android.tools.idea.tests.gui.framework.*;
import com.android.tools.idea.tests.gui.framework.fixture.InspectCodeDialogFixture;
import com.android.tools.idea.tests.gui.framework.fixture.newProjectWizard.ConfigureAndroidProjectStepFixture;
import com.android.tools.idea.tests.gui.framework.fixture.newProjectWizard.NewProjectWizardFixture;
import com.android.tools.idea.tests.gui.framework.fixture.npw.NewActivityWizardFixture;
import com.intellij.openapi.module.Module;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static com.android.tools.idea.npw.FormFactor.MOBILE;
import static com.android.tools.idea.testing.FileSubject.file;
import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;

/**
 * Test that newly created Instant App projects do not have errors in them
 */
@RunIn(TestGroup.PROJECT_WIZARD)
@RunWith(GuiTestRunner.class)
public class NewInstantAppTest {
  @Rule public final GuiTestRule guiTest = new GuiTestRule();
  @Rule public final ScreenshotsDuringTest screenshotsRule = new ScreenshotsDuringTest();

  //Not putting this in before() as I plan to add some tests that work on non-default projects.
  private void createAndOpenDefaultAIAProject(@NotNull String projectName, @Nullable String featureModuleName,
                                              @Nullable String activityName) {
    //TODO: There is some commonality between this code, the code in NewProjectTest and further tests I am planning, but there are also
    //      differences. Once AIA tests are completed this should be factored out into the NewProjectWizardFixture
    NewProjectWizardFixture newProjectWizard = guiTest.welcomeFrame()
      .createNewProject();

    newProjectWizard
      .getConfigureAndroidProjectStep()
      .enterCompanyDomain("test.android.com")
      .enterApplicationName(projectName)
      .wizard()
      .clickNext() // Complete project configuration
      .getConfigureFormFactorStep()
      .selectMinimumSdkApi(MOBILE, "23")
      .selectInstantAppSupport(MOBILE)
      .wizard()
      .clickNext(); // Complete form factor configuration

    if (featureModuleName != null) {
      newProjectWizard
        .getConfigureInstantModuleStep()
        .enterFeatureModuleName(featureModuleName);
    }

    newProjectWizard
      .clickNext() // Complete configuration of Instant App Module
      .chooseActivity(activityName == null ? "Empty Activity" : activityName)
      .clickNext() // Complete "Add Activity" step
      .clickFinish();

    guiTest.ideFrame()
      .waitForGradleProjectSyncToFinish()
      .findRunApplicationButton().waitUntilEnabledAndShowing(); // Wait for the toolbar to be ready

    guiTest.ideFrame()
      .getProjectView()
      .selectAndroidPane()
      .clickPath(featureModuleName == null ? "feature" : featureModuleName);
  }

  @RunIn(TestGroup.UNRELIABLE)  // b/71515855
  @Test
  public void testNoWarningsInDefaultNewInstantAppProjects() throws IOException {
    String projectName = "Warning";
    createAndOpenDefaultAIAProject(projectName, null, null);

    String inspectionResults = guiTest.ideFrame()
      .openFromMenu(InspectCodeDialogFixture::find, "Analyze", "Inspect Code...")
      .clickOk()
      .getResults();

    verifyOnlyExpectedWarnings(inspectionResults,
                               "    Android Lint: Security\n" +
                               "        AllowBackup/FullBackupContent Problems\n" +
                               "            AndroidManifest.xml\n" +
                               "                On SDK version 23 and up, your app data will be automatically backed up and restored on app install. Consider adding the attribute 'android:fullBackupContent' to specify an '@xml' resource which configures which files to backup. More info: <a href=\"https://developer.android.com/training/backup/autosyncapi.html\">https://developer.android.com/training/backup/autosyncapi.html</a>\n" +
                               "    Android Lint: Usability\n" +
                               "        Missing support for Firebase App Indexing\n" +
                               "            AndroidManifest.xml\n" +
                               "                App is not indexable by Google Search; consider adding at least one Activity with an ACTION-VIEW intent filter. See issue explanation for more details.\n" +
                               "    Declaration redundancy\n" +
                               "        Redundant throws clause\n" +
                               "            ExampleInstrumentedTest\n" +
                               "            ExampleUnitTest\n" +
                               "                The declared exception 'Exception' is never thrown\n" +
                               "        Unnecessary module dependency\n" +
                               "            feature\n" +
                               "                Module 'feature' sources do not depend on module 'base' sources\n" +
                               "    XML\n" +
                               "        Unused XML schema declaration\n" +
                               "            AndroidManifest.xml\n" +
                               "                Namespace declaration is never used\n" +
                               "        XML tag empty body\n" +
                               "            strings.xml\n" +
                               "                XML tag has empty body\n"
    );
  }

  @Test
  public void testCanBuildDefaultNewInstantAppProjects() throws IOException {
    createAndOpenDefaultAIAProject("BuildApp", null, null);

    guiTest.ideFrame().getEditor()
      .open("base/build.gradle") // Check "base" dependencies
      .moveBetween("application project(':app')", "")
      .moveBetween("feature project(':feature')", "")
      .open("feature/build.gradle") // Check "feature" dependencies
      .moveBetween("implementation project(':base')", "")
      .open("app/build.gradle") // Check "app" dependencies
      .moveBetween("implementation project(':feature')", "")
      .moveBetween("implementation project(':base')", "")
      .open("base/src/main/AndroidManifest.xml")
      .moveBetween("android:name=\"aia-compat-api-min-version\"", "")
      .moveBetween("android:value=\"1\"", "");

    assertThat(guiTest.ideFrame().invokeProjectMake().isBuildSuccessful()).isTrue();
  }

  @RunIn(TestGroup.UNRELIABLE)  // b/69171895
  @Test
  public void testCanBuildNewInstantAppProjectsWithLoginActivity() throws IOException {
    createAndOpenDefaultAIAProject("BuildApp", null, "Login Activity");
    guiTest.ideFrame().getEditor()
      .open("feature/src/main/res/layout/activity_login.xml")
      .open("feature/src/main/AndroidManifest.xml")
      .moveBetween("android:order=", "")
      .moveBetween("android:host=", "")
      .moveBetween("android:pathPattern=", "")
      .moveBetween("android:scheme=\"https", "")
      .moveBetween("android.intent.category.", "LAUNCHER");
    assertThat(guiTest.ideFrame().invokeProjectMake().isBuildSuccessful()).isTrue();
  }

  @Test
  public void newInstantAppProjectWithFullScreenActivity() throws Exception {
    createAndOpenDefaultAIAProject("BuildApp", null, "Fullscreen Activity");
    guiTest.ideFrame().getEditor()
      .open("feature/src/main/res/layout/activity_fullscreen.xml")
      .open("base/src/main/res/values/attrs.xml") // Make sure "Full Screen" themes, colors and styles are on the base module
      .moveBetween("ButtonBarContainerTheme", "")
      .open("base/src/main/res/values/colors.xml")
      .moveBetween("black_overlay", "")
      .open("base/src/main/res/values/styles.xml")
      .moveBetween("FullscreenTheme", "")
      .moveBetween("FullscreenActionBarStyle", "");
    assertThat(guiTest.ideFrame().invokeProjectMake().isBuildSuccessful()).isTrue();
  }

  @RunIn(TestGroup.UNRELIABLE)
  @Test // b/68122671
  public void addMapActivityToExistingIappModule() throws Exception {
    createAndOpenDefaultAIAProject("BuildApp", "feature", null);
    guiTest.ideFrame()
      .openFromMenu(NewActivityWizardFixture::find, "File", "New", "Google", "Google Maps Activity")
      .clickFinish();

    guiTest.ideFrame()
      .waitForGradleProjectSyncToFinish();

    assertAbout(file()).that(new File(guiTest.getProjectPath(), "base/src/debug/res/values/google_maps_api.xml")).isFile();
    assertAbout(file()).that(new File(guiTest.getProjectPath(), "base/src/release/res/values/google_maps_api.xml")).isFile();
  }

  @Test // b/68478730
  public void addMasterDetailActivityToExistingIappModule() throws Exception {
    createAndOpenDefaultAIAProject("BuildApp", "feature", null);
    guiTest.ideFrame()
      .openFromMenu(NewActivityWizardFixture::find, "File", "New", "Activity", "Master/Detail Flow")
      .clickFinish();

    String baseStrings = guiTest.ideFrame()
      .waitForGradleProjectSyncToFinish()
      .getEditor()
      .open("base/src/main/res/values/strings.xml")
      .getCurrentFileContents();

    assertThat(baseStrings).contains("title_item_detail");
    assertThat(baseStrings).contains("title_item_list");
  }

  @RunIn(TestGroup.UNRELIABLE)  // b/69171895
  @Test // b/68684401
  public void addFullscreenActivityToExistingIappModule() throws Exception {
    createAndOpenDefaultAIAProject("BuildApp", "feature", null);
    guiTest.ideFrame()
      .openFromMenu(NewActivityWizardFixture::find, "File", "New", "Activity", "Fullscreen Activity")
      .clickFinish();

    String baseStrings = guiTest.ideFrame()
      .waitForGradleProjectSyncToFinish()
      .getEditor()
      .open("base/src/main/res/values/strings.xml")
      .getCurrentFileContents();

    assertThat(baseStrings).contains("title_activity_fullscreen");
  }

  @Test
  public void testValidPathInDefaultNewInstantAppProjects() throws IOException {
    createAndOpenDefaultAIAProject("RouteApp", "routefeature", null);

    Module module = guiTest.ideFrame().getModule("routefeature");
    AndroidFacet facet = AndroidFacet.getInstance(module);
    assertThat(facet).isNotNull();
    assertThat(new InstantAppUrlFinder(MergedManifest.get(facet)).getAllUrls()).isNotEmpty();
  }

  @Test
  public void testCanCustomizeFeatureModuleInNewInstantAppProjects() throws IOException {
    createAndOpenDefaultAIAProject("SetFeatureNameApp", "testfeaturename", null);

    guiTest.ideFrame().getModule("testfeaturename");
  }

  // With warnings coming from multiple projects the order of warnings is not deterministic, also there are some warnings that show up only
  // on local machines. This method allows us to check that the warnings in the actual result are a sub-set of the expected warnings.
  // This is not a perfect solution, but this state where we have multiple warnings on a new project should only be temporary
  private static void verifyOnlyExpectedWarnings(@NotNull String actualResults, @NotNull String acceptedWarnings) {
    ArrayList<String> lines = new ArrayList<>(Arrays.asList(actualResults.split("\n")));

    // Ignore the first line of the error report
    for (String line : lines.subList(1, lines.size())) {
      assertThat(acceptedWarnings.split("\n")).asList().contains(line);
    }
  }
}
