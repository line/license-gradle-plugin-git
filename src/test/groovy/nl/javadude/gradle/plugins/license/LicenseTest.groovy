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

package nl.javadude.gradle.plugins.license

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class LicenseTest {

    License task

    @Before
    void setupProject() {
        Project project = ProjectBuilder.builder().withProjectDir(new File("testProject")).build()
        project.apply plugin: 'java'
        project.plugins.apply(LicensePlugin)
        task = (License) project.tasks['licenseMain']
    }

    @Test
    void shouldUseUriHeader() {
        def uri = new URI("https://www.gnu.org/licenses/lgpl.txt")
        def file = new File('LICENSE')

        // When
        task.header = file

        // Then
        assert task.resolveURI().getPath().endsWith("LICENSE")

        // When
        task.headerURI = uri

        // Then
        assert task.resolveURI() == uri
    }
}

