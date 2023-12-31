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

package nl.javadude.gradle.plugins.license.maven;

import com.mycila.maven.plugin.license.document.Document;
import com.mycila.maven.plugin.license.header.Header;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

/**
 * Check if the source files of the project have a valid license header
 *
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class LicenseCheckMojo implements CallbackWithFailure {
    Logger logger = Logging.getLogger(LicenseCheckMojo.class);
    private final File basedir;
    public final Collection<File> missingHeaders = new ConcurrentLinkedQueue<File>();

    /**
     * Whether to skip file where a header has been detected
     */
    protected boolean skipExistingHeaders = false;

    public LicenseCheckMojo(File basedir, boolean skipExistingHeaders) {
        this.basedir = basedir;
        this.skipExistingHeaders = skipExistingHeaders;
    }

    @Override
    public void onHeaderNotFound(Document document, Header header) {
        document.parseHeader();
        if (document.headerDetected() && skipExistingHeaders) {
            logger.info("Ignoring header in: {}", DocumentFactory.getRelativeFile(basedir, document));
            return;
        } else {
            logger.lifecycle("Missing header in: {}", DocumentFactory.getRelativeFile(basedir, document));
        }
        missingHeaders.add(document.getFile());
    }

    @Override
    public void onUnknownFile(Document document, Header header) {
        logger.error("Unknown file: {}", DocumentFactory.getRelativeFile(basedir, document));
    }

    @Override
    public void onExistingHeader(Document document, Header header) {
        logger.info("Header OK in: {}", DocumentFactory.getRelativeFile(basedir, document));
    }

    @Override
    public boolean hadFailure() {
        return !missingHeaders.isEmpty();
    }

    @Override
    public Collection<File> getAffected() {
        return missingHeaders;
    }

}
