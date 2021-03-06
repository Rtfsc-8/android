/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.android.tools.idea.gradle.dsl.model;

import com.android.tools.idea.gradle.dsl.api.android.BaseCompileOptionsModel;
import com.android.tools.idea.gradle.dsl.api.values.GradleNullableValue;
import com.android.tools.idea.gradle.dsl.model.values.GradleNullableValueImpl;
import com.android.tools.idea.gradle.dsl.parser.elements.BaseCompileOptionsDslElement;
import com.android.tools.idea.gradle.dsl.parser.java.JavaVersionDslElement;
import com.intellij.pom.java.LanguageLevel;
import org.jetbrains.annotations.NotNull;

import static com.android.tools.idea.gradle.dsl.parser.elements.BaseCompileOptionsDslElement.SOURCE_COMPATIBILITY_ATTRIBUTE_NAME;
import static com.android.tools.idea.gradle.dsl.parser.elements.BaseCompileOptionsDslElement.TARGET_COMPATIBILITY_ATTRIBUTE_NAME;

/**
 * Base compile options model that only have sourceCompatibility / targetCompatibility fields.
 */
public abstract class BaseCompileOptionsModelImpl extends GradleDslBlockModel implements BaseCompileOptionsModel {
  private final boolean myUseAssignment;

  public BaseCompileOptionsModelImpl(@NotNull BaseCompileOptionsDslElement dslElement, boolean useAssignment) {
    super(dslElement);
    myUseAssignment = useAssignment;
  }

  @NotNull
  @Override
  public GradleNullableValue<LanguageLevel> sourceCompatibility() {
    JavaVersionDslElement javaVersionElement =
      myDslElement.getPropertyElement(SOURCE_COMPATIBILITY_ATTRIBUTE_NAME, JavaVersionDslElement.class);
    if (javaVersionElement == null) {
      return new GradleNullableValueImpl<>(myDslElement, null);
    }
    return new GradleNullableValueImpl<>(javaVersionElement, javaVersionElement.getVersion());
  }

  @NotNull
  @Override
  public BaseCompileOptionsModel setSourceCompatibility(@NotNull LanguageLevel languageLevel) {
    return setLanguageLevel(SOURCE_COMPATIBILITY_ATTRIBUTE_NAME, languageLevel);
  }

  @NotNull
  @Override
  public BaseCompileOptionsModel removeSourceCompatibility() {
    myDslElement.removeProperty(SOURCE_COMPATIBILITY_ATTRIBUTE_NAME);
    return this;
  }

  @NotNull
  @Override
  public GradleNullableValue<LanguageLevel> targetCompatibility() {
    JavaVersionDslElement javaVersionElement =
      myDslElement.getPropertyElement(TARGET_COMPATIBILITY_ATTRIBUTE_NAME, JavaVersionDslElement.class);
    if (javaVersionElement == null) {
      return new GradleNullableValueImpl<>(myDslElement, null);
    }
    return new GradleNullableValueImpl<>(javaVersionElement, javaVersionElement.getVersion());
  }

  @NotNull
  @Override
  public BaseCompileOptionsModel setTargetCompatibility(@NotNull LanguageLevel languageLevel) {
    return setLanguageLevel(TARGET_COMPATIBILITY_ATTRIBUTE_NAME, languageLevel);
  }

  @NotNull
  @Override
  public BaseCompileOptionsModel removeTargetCompatibility() {
    myDslElement.removeProperty(TARGET_COMPATIBILITY_ATTRIBUTE_NAME);
    return this;
  }

  @NotNull
  private BaseCompileOptionsModel setLanguageLevel(@NotNull String type, @NotNull LanguageLevel languageLevel) {
    JavaVersionDslElement element = myDslElement.getPropertyElement(type, JavaVersionDslElement.class);
    if (element == null) {
      element = new JavaVersionDslElement(myDslElement, type, myUseAssignment);
      myDslElement.setNewElement(type, element);
    }
    element.setVersion(languageLevel);
    return this;
  }
}
