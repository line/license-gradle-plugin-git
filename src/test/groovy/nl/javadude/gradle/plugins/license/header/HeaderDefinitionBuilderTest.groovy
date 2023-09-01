/*
 * Copyright 2023 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
/*
 * Copyright (C)2011 - Jeroen van Erp <jeroen@javadude.nl>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.javadude.gradle.plugins.license.header

import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

import static org.hamcrest.CoreMatchers.is
import static org.junit.Assert.assertThat

class HeaderDefinitionBuilderTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none()

  HeaderDefinitionBuilder builder = HeaderDefinitionBuilder.headerDefinition("freddie")
    .withFirstLine("5 September 1946")
    .withBeforeEachLine("Rock")
    .withEndLine("24 November 1991")
    .withFirstLineDetectionDetectionPattern("First day")
    .withLastLineDetectionDetectionPattern("Last day")
    .withSkipLinePattern("HIV")
    .multiline()
    .withBlankLines()

  @Test
  void setsType() {
    assertThat builder.build().type, is("freddie")
  }

  @Test
  void setsFirstLine() {
    assertThat builder.build().firstLine, is("5 September 1946")
  }

  @Test
  void setsEndLine() {
    assertThat builder.build().endLine, is("24 November 1991")
  }

  @Test
  void setsBeforeEachLine() {
    assertThat builder.build().beforeEachLine, is("Rock")
  }

  @Test
  void setsFirstLineDetectionPattern() {
    assertThat builder.build().isFirstHeaderLine("First day"), is(true)
  }

  @Test
  void setsLastLineDetectionPattern() {
    assertThat builder.build().isLastHeaderLine("Last day"), is(true)
  }

  @Test
  void setsMultiline() {
    assertThat builder.build().isMultiLine(), is(true)
  }

  @Test(expected = IllegalArgumentException.class)
  void exceptionWhenNoMultilineWithAllowingBlankLine() {
    builder.noMultiLine()

    builder.build()
  }

  @Test
  void allowsBlankLines() {
    assertThat builder.build().allowBlankLines(), is(true)
  }

  @Test
  void noBlankLines() {
    builder.withNoBlankLines()
    assertThat builder.build().allowBlankLines(), is(false)
  }

  @Test
  void setsSkipLines() {
    assertThat builder.build().isSkipLine("HIV"), is(true)
  }

  @Test
  void validatesOnBuild() {
    expectedException.expect(IllegalStateException)
    expectedException.expectMessage("missing for header definition")

    HeaderDefinitionBuilder.headerDefinition("farrokh_bulsara").withFirstLine(null).build()
  }
}
