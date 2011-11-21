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
package org.jason.tdat2json;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Replacement for original Tdat2Json, which eventually turned into a steaming pile of gabage full of
 * nasty if statements, etc. If you really want to see it, you can look at the 0.1 tag, but I'm not exactly
 * proud of it.
 *
 * @author Jason Ferguson
 * @since 0.2
 */
public class Tdat2Json {

    public static final Map<String, String> catalogLocations;
    public static final List<Pattern> excludedPatterns;
    // XML files with what to exclude. Mayber later

    static {
        catalogLocations = new HashMap<String, String>();
        catalogLocations.put("class", "http://heasarc.gsfc.nasa.gov/FTP/heasarc/dbase/dump/heasarc_class.tdat.gz");
        catalogLocations.put("cns3", "http://heasarc.gsfc.nasa.gov/FTP/heasarc/dbase/dump/heasarc_cns3.tdat.gz");
        catalogLocations.put("hd", "http://heasarc.gsfc.nasa.gov/FTP/heasarc/dbase/dump/heasarc_hd.tdat.gz");
        catalogLocations.put("bsc", "http://heasarc.gsfc.nasa.gov/FTP/heasarc/dbase/dump/heasarc_bsc5p.tdat.gz");
        catalogLocations.put("bd", "http://heasarc.gsfc.nasa.gov/FTP/heasarc/dbase/dump/heasarc_bd.tdat.gz");
        catalogLocations.put("sao", "http://heasarc.gsfc.nasa.gov/FTP/heasarc/dbase/dump/heasarc_sao.tdat.gz");
        catalogLocations.put("ppm", "http://heasarc.gsfc.nasa.gov/FTP/heasarc/dbase/dump/heasarc_ppm.tdat.gz");
        catalogLocations.put("tycho2", "http://heasarc.gsfc.nasa.gov/FTP/heasarc/dbase/dump/heasarc_tycho2.tdat.gz");
        catalogLocations.put("hipparcos", "http://heasarc.gsfc.nasa.gov/FTP/heasarc/dbase/dump/heasarc_hipnewcat.tdat.gz");
        catalogLocations.put("messier", "http://heasarc.gsfc.nasa.gov/FTP/heasarc/dbase/dump/heasarc_messier.tdat.gz");
        catalogLocations.put("ngc2000", "http://heasarc.gsfc.nasa.gov/FTP/heasarc/dbase/dump/heasarc_ngc2000.tdat.gz");
        catalogLocations.put("ugc", "http://heasarc.gsfc.nasa.gov/FTP/heasarc/dbase/dump/heasarc_ugc.tdat.gz");

        excludedPatterns = new ArrayList<Pattern>();
        excludedPatterns.add(Pattern.compile("crl_"));

    }

    /**
     * Get the catalog from the remote location and write it to the local file system
     * <p/>
     * (So far, I don't know how to use NIO with a GZIS, that would make this less ugly)
     *
     * @param catalogName
     */
    private static void getRemoteCatalog(String catalogName) {

        String strUrl = catalogLocations.get(catalogName);

        File filename;
        if (System.getProperty("os.name").contains("Windows")) {
            filename = new File(".\\" + catalogName + ".tdat");
        } else {
            filename = new File("./" + catalogName + ".tdat");
        }

        if (!filename.exists()) {
            try {
                URL catalogURL = new URL(strUrl);
                GZIPInputStream gzipInputStream = new GZIPInputStream(new BufferedInputStream(catalogURL.openStream()));
                FileOutputStream fos = new FileOutputStream(catalogName + ".tdat");
                byte[] buffer = new byte[1024];
                int len;
                while ((len = gzipInputStream.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                gzipInputStream.close();
                fos.close();
            } catch (MalformedURLException e) {

            } catch (IOException e) {

            } finally {
                System.out.println("Completed writing " + catalogName + ".tdat");
            }
        }

    }

    private static Map<String, Object> parseCatalogXml(String catalogName) {

        SAXParserFactory factory = SAXParserFactory.newInstance();

        CatalogHandler handler = new CatalogHandler();

        try {
            SAXParser parser = factory.newSAXParser();
            parser.reset();

            InputStream is = ClassLoader.getSystemResourceAsStream(catalogName + ".xml");
            if (is == null) {
                throw new IllegalStateException("InputStream cannot be null.");
            }
            parser.parse(new InputSource(is), handler);
            is.close();
        } catch (SAXException ex) {
            ex.printStackTrace();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return handler.getConfigMap();

    }

    /**
     * Dumb little method to check if multiple values are null instead of just one at a time
     *
     * @param checkValues
     * @return
     */
    public static boolean isNull(Object... checkValues) {
        for (Object o : checkValues) {
            if (o != null) {
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> processPerCatalogExclusions(Map<String, String> map, Map<String, Object> configuration) {

        //Map<String, Object> configuration = parseCatalogXml(catalogName);
        Map<String, String> resultMap = new LinkedHashMap<String, String>(map);

        List<Pattern> exclusionPatterns = (List<Pattern>) configuration.get("exclusionPatterns");
        boolean dropEmpty = (Boolean) configuration.get("dropEmpty");
        List<String> fieldsToDrop = (List<String>) configuration.get("fieldsToDrop");
        Map<String, String> fieldsToCopy = (Map<String, String>) configuration.get("fieldsToCopy");
        Map<String, String> prefixFields = (Map<String, String>) configuration.get("fieldPrefixes");

        for (String key : map.keySet()) {
            // drop empty map fields
            if (map.get(key).length() == 0 && dropEmpty) {
                resultMap.remove(key);
                continue;
            }
            // drop excluded patterns
            for (Pattern pattern : exclusionPatterns) {
                Matcher matcher = pattern.matcher(key);
                if (matcher.find()) {
                    resultMap.remove(key);
                }
            }
            // deal with field renames
            if (fieldsToCopy.containsKey(key)) {
                String newKeyName = fieldsToCopy.get(key);
                String newKeyValue = map.get(key);
                resultMap.put(newKeyName, newKeyValue);
            }
            // drop poor, unwanted fields
            for (String fieldName : fieldsToDrop) {
                resultMap.remove(fieldName);
            }

            // deal with the prefixes
            for (String fieldToPrefix : prefixFields.keySet()) {
                if (resultMap.containsKey(fieldToPrefix)) {
                    // for some reason the prefix was prepended multiple times. Decided it wasn't really worth the
                    // time to run down the problem, so I got out the hammer...
                    if (resultMap.get(fieldToPrefix).indexOf(prefixFields.get(fieldToPrefix)) == -1) {
                        String oldValue = resultMap.get(fieldToPrefix);
                        String newValue = prefixFields.get(fieldToPrefix) + oldValue;
                        resultMap.put(fieldToPrefix, newValue);
                    }
                }
            }
        }

        return resultMap;

    }

    public static double resetScale(String value) {
        BigDecimal bd = new BigDecimal(value).setScale(4, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Process a TDAT file and output it as a .json file. I have to do alot here, since if I just try to pass back
     * a List<String> I tend to run out of heap space on the larger catalogs.
     *
     * @param catalogName name of the catalog being dealt with
     * @param config      Configuration Map containing data parsed from the XML
     */
    @SuppressWarnings("unchecked")
    private static void processTdatFile(String catalogName, Map<String, Object> config) {

        // regex to find the field names
        Pattern fieldNameRegexPattern = Pattern.compile("line\\[1\\] = (.*)");

        // regex to find the bang-separated values
        Pattern bsvValuesPattern = Pattern.compile("^(.*?\\|)*$");

        String[] fieldNames = null;
        try {
            Scanner scanner = new Scanner(new FileInputStream(catalogName + ".tdat"));

            // two loops, first is to find the field names, then we'll break out and go to the next to find
            // the values for those names
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Matcher fieldNameMatcher = fieldNameRegexPattern.matcher(line);
                if (fieldNameMatcher.find()) {
                    fieldNames = fieldNameMatcher.group(1).split("\\s");
                    break;
                }

            }
            if (fieldNames == null) {
                throw new IllegalStateException("Field Names cannot be null");
            }

            Writer writer;
            if (System.getProperty("os.name").contains("Windows")) {
                writer = new FileWriter(".\\" + catalogName + ".json");
            } else {
                writer = new FileWriter("./" + catalogName + ".json");
            }

            Pattern isNumericPattern = Pattern.compile("^[+-]?([0-9]*\\.?[0-9]+|[0-9]+\\.?[0-9]*)([eE][+-]?[0-9]+)?$");

            int lineCounter = 0;
            Map<String, String> fieldPrefixes = (Map<String, String>) config.get("fieldPrefixes");
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                // check to see if the line is a PSV (pipe separated value)
                Matcher matcher = bsvValuesPattern.matcher(line);
                if (matcher.find()) {
                    // split into an array
                    String[] columnValues = line.split("\\|");
                    Map<String, String> valueMap = new LinkedHashMap<String, String>();
                    for (int i = 0; i < columnValues.length; i++) {
                        valueMap.put(fieldNames[i], columnValues[i]);
                    }

                    valueMap = processPerCatalogExclusions(valueMap, config);

                    StringBuffer sb = new StringBuffer();
                    sb.append("{");

                    for (String key : valueMap.keySet()) {
                        String value = valueMap.get(key);
                        Matcher numberMatcher = isNumericPattern.matcher(value);
                        if (numberMatcher.find()) {
                            // use a big hammer approach to NOT set the scale of an integer value
                            if (value.indexOf(".") != -1) {
                                sb.append("\"").append(key).append("\":").append(resetScale(value)).append(",");
                            } else {
                                sb.append("\"").append(key).append("\":").append(value).append(",");
                            }
                        } else {
                            sb.append("\"").append(key).append("\":\"").append(valueMap.get(key)).append("\",");
                        }
                    }

                    sb = new StringBuffer(sb.substring(0, sb.length() - 1));
                    sb.append("}\r\n");
                    writer.write(sb.toString());
                    lineCounter++;
                }
                if (lineCounter % 5000 == 0) {
                    System.out.println("Wrote " + lineCounter + " lines");
                }
            }
            writer.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
    }


    /**
     * Process a catalog from an internet location into a JSON formatted file that can be imported into MongoDB
     *
     * @param catalogName name of catalog to turn into JSON output, must correspond to a key in the catalogLocations
     *                    map
     */
    public static void processCatalog(String catalogName, Map<String, Object> config) {

        if (!catalogLocations.containsKey(catalogName)) {
            throw new IllegalArgumentException("Catalog name not found in location map");
        }

        getRemoteCatalog(catalogName);
        processTdatFile(catalogName, config);
    }

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("You must provide the name of a catalog to process.");
            System.out.println("Available catalogs: ");
            for (String catalog : catalogLocations.keySet()) {
                System.out.println(catalog);
            }
            System.exit(0);
        }

        String catalogName = args[0];

        Map<String, Object> configuration = parseCatalogXml(catalogName);

        processCatalog(catalogName, configuration);

        File file1 = new File("heasarc_" + catalogName + "tdat.gz");
        file1.delete();

//        File file2 = new File(catalogName + ".tdat");
//        file2.delete();
    }
}
