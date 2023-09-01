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

import com.hierynomus.gradle.license.tasks.LicenseCheck
import com.hierynomus.gradle.license.tasks.LicenseFormat
import nl.javadude.gradle.plugins.license.header.HeaderDefinitionBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.SourceSetContainer

class LicensePlugin implements Plugin<Project> {
    private static Logger logger = Logging.getLogger(LicensePlugin)

    static final String LICENSE_TASK_BASE_NAME = 'license'
    static final String FORMAT_TASK_BASE_NAME = 'licenseFormat'

    private Project project
    private LicenseExtension extension

    protected Task baseCheckTask
    protected Task baseFormatTask

    @Override
    void apply(Project project) {
        this.project = project
        extension = createExtension(project)

        project.plugins.with {
            withType(JavaBasePlugin).configureEach {
                configureJava()
            }
        }

        configureTaskRule()

        baseCheckTask = project.task(LICENSE_TASK_BASE_NAME)
        baseFormatTask = project.task(FORMAT_TASK_BASE_NAME)

        baseCheckTask.group = baseFormatTask.group = "License"
        baseCheckTask.description = "Checks for header consistency."
        baseFormatTask.description = "Applies the license found in the header file."

        // Add license checking into check lifecycle, since its a type of code quality plugin

        project.plugins.withType(JavaBasePlugin).configureEach {
            linkTasks(project)
        }
    }

    private static LicenseExtension createExtension(Project project) {
        final LicenseExtension extension = project.extensions.create(LICENSE_TASK_BASE_NAME, LicenseExtension)
        extension.with {
            // Default for extension
            header = project.rootProject.file("LICENSE")
            headerURI = null
            ignoreFailures = false
            dryRun = false
            skipExistingHeaders = false
            useDefaultMappings = true
            strictCheck = false
            encoding = System.properties['file.encoding']
            headerDefinitions = project.container(HeaderDefinitionBuilder)
        }

        logger.info("The license extension is created: ${extension}")
        return extension
    }

    /**
     * We'll be creating the tasks by default based on the source sets, but users could define their
     * own, and we'd still want it configured.
     * TODO: Confirm that user defined tasks will get this configuration, it'd have to be lazily evaluated
     */
    private void configureTaskRule() {
        project.tasks.withType(License).configureEach { License task ->
            logger.info("Applying the default license extension to the task: ${task.path}")
            configureTaskDefaults(task)
        }
    }

    private void configureTaskDefaults(License task) {
        task.conventionMapping.with {
            // Defaults for task, which will delegate to project's License extension
            // These can still be explicitly set by the user on the individual tasks
            header = { extension.header }
            headerURI = { extension.headerURI }
            ignoreFailures = { extension.ignoreFailures }
            dryRun = { extension.dryRun }
            skipExistingHeaders = { extension.skipExistingHeaders }
            useDefaultMappings = { extension.useDefaultMappings }
            strictCheck = { extension.strictCheck }
            inheritedProperties = { extension.ext.properties }
            inheritedMappings = { extension.internalMappings }
            excludes = { extension.excludePatterns }
            includes = { extension.includePatterns }
            encoding = { extension.encoding }
            headerDefinitions = { extension.headerDefinitions }
            inceptionYear = { extension.inceptionYear }
        }
    }

    private void configureJava() {
        configureSourceSetRule((SourceSetContainer) project.sourceSets, { sourceSet -> sourceSet.allSource })
    }

    /**
     * Dynamically create a task for each sourceSet, and register with check
     */
    private void configureSourceSetRule(SourceSetContainer sourceSetContainer,
                                        Closure<Iterable<File>> sourceSetSources) {
        // This follows the other check task pattern
        sourceSetContainer.all { sourceSet ->
            def sourceSetTaskName = "${LICENSE_TASK_BASE_NAME}${sourceSet.name.capitalize()}"
            logger.info("Adding ${sourceSetTaskName} task for sourceSet ${sourceSet.name}")

            final License checkTask = project.tasks.create(sourceSetTaskName, LicenseCheck)
            configureForSourceSet(sourceSet, checkTask, sourceSetSources)

            // Add independent license task, which will perform format
            def sourceSetFormatTaskName = "${FORMAT_TASK_BASE_NAME}${sourceSet.name.capitalize()}"
            final License formatTask = project.tasks.create(sourceSetFormatTaskName, LicenseFormat)
            configureForSourceSet(sourceSet, formatTask, sourceSetSources)

            // TODO Add independent clean task to remove headers
        }
    }

    private static void configureForSourceSet(sourceSet, License task, Closure<Iterable<File>> sourceSetSources) {
        task.with {
            // Explicitly set description
            description = "Scanning license on ${sourceSet.name} files"
        }

        // Default to all source files from SourceSet
        task.source = sourceSetSources(sourceSet)
    }

    private void linkTasks(Project project) {
        project.tasks[JavaBasePlugin.CHECK_TASK_NAME].dependsOn baseCheckTask
        project.tasks.withType(LicenseCheck).configureEach { lt ->
            baseCheckTask.dependsOn lt
        }
        project.tasks.withType(LicenseFormat).configureEach { lt ->
            baseFormatTask.dependsOn lt
        }
    }
}
