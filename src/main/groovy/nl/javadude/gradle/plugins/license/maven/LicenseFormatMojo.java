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
 * Reformat files with a missing header to add it
 *
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class LicenseFormatMojo implements CallbackWithFailure {
    Logger logger = Logging.getLogger(LicenseCheckMojo.class);
    File basedir;

    public LicenseFormatMojo(File basedir, boolean dryRun, boolean skipExistingHeaders) {
        this.basedir = basedir;
        this.dryRun = dryRun;
        this.skipExistingHeaders = skipExistingHeaders;
    }

    /**
     * Whether to create new files which have changes or to make them inline
     */
    protected boolean dryRun = false;

    /**
     * Whether to skip file where a header has been detected
     */
    protected boolean skipExistingHeaders = false;

    public final Collection<File> missingHeaders = new ConcurrentLinkedQueue<File>();

    public void onHeaderNotFound(Document document, Header header) {
        document.parseHeader();
        if (document.headerDetected()) {
            if (skipExistingHeaders) {
                logger.info("Keeping license header in: {}", DocumentFactory.getRelativeFile(basedir, document));
                return;
            } else
                document.removeHeader();
        }
        logger.lifecycle("Updating license header in: {}", DocumentFactory.getRelativeFile(basedir, document));
        document.updateHeader(header);
        missingHeaders.add(document.getFile());
        if (!dryRun) {
            document.save();
        } else {
            String name = document.getFile().getName() + ".licensed";
            File copy = new File(document.getFile().getParentFile(), name);
            logger.debug("Result saved to: {}", copy);
            document.saveTo(copy);
        }
    }

    @Override
    public void onUnknownFile(Document document, Header header) {
        logger.error("Unknown file: {}", DocumentFactory.getRelativeFile(basedir, document));
    }

    public void onExistingHeader(Document document, Header header) {
        logger.info("Header OK in: {}", DocumentFactory.getRelativeFile(basedir, document));
    }

    @Override
    public boolean hadFailure() {
        // Can't really fail, since we're actually modifying files
        return false;
    }

    @Override
    public Collection<File> getAffected() {
        return missingHeaders;
    }

}
