/**
 * Copyright 2011 Jason Ferguson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jason.heasarcutils.catalogparser.util.io;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.jason.heasarcutils.catalogparser.ui.event.ImportFromDatEvent;
import org.jason.heasarcutils.catalogparser.ui.event.ImportFromTdatEvent;
import org.jason.heasarcutils.catalogparser.util.Catalog;

import java.io.*;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * @author Jason Ferguson
 * @since 0.2
 */
public class DataImporter {

    private EventBus eventBus;

    @Inject
    public DataImporter(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void importFromTdat(ImportFromTdatEvent e) {
        Catalog catalog = e.getCatalog();

        try {
            BufferedReader reader = getReader(catalog.getUrl());
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }

    public void importFromDat(ImportFromDatEvent e) {
        Catalog catalog = e.getCatalog();
    }

    private BufferedReader getReader(final String fileUrl) throws IOException {
        final String filename = getFilename(fileUrl);
        final URL url = new URL(fileUrl);
        final InputStream stream;
        if (new File(filename).isFile()) {
            stream = ClassLoader.getSystemResourceAsStream(fileUrl);
            System.out.println("Using tdat header from classes directory");
        } else {
            stream = url.openStream();
        }
        final GZIPInputStream gzipStream = new GZIPInputStream(stream);
        final InputStreamReader gzipStreamReader =
                new InputStreamReader(gzipStream, "UTF-8");
        final BufferedReader reader = new BufferedReader(gzipStreamReader);
        return reader;
    }

    protected String getFilename(String url) {
        return url.substring(url.lastIndexOf('/') + 1, url.length() - 1);
    }
}
