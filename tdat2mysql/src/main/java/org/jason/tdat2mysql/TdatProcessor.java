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
package org.jason.tdat2mysql;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processing application for NASA TDAT files. NASA uses some funky database for their heasarc
 * files, and has utilities for importing and exporting the files in Transportable Data Aggregate Table
 * (TDAT) format. However, most of the rest of us use RDBMS databases (and don't run 6 billion dollars
 * over budget on must-pay major projects while expecting taxpayers to suck it up).
 * <p/>
 * Since TDAT is not used outside NASA, the data somehow has to be converted into SQL. This utility
 * reads the TDAT file, figures out the DDL to create the table and DML to insert the data, and outputs it
 * into a text file. This allows users to tweak the output for whatever flavor RDBMS they want.
 * <p/>
 * As written, the code is not thread-safe since StringBuilder is not thread safe. If you want to turn this
 * code into a library, you should probably convert the StringBuilder to StringBuffer. (Hmmm, wouldn't it have
 * been nice if both StringBuilder and StringBuffer inherited from the same parent interface?)
 * <p/>
 * I stole the isEmpty() method from Apache Commons StringUtils and isNumeric() from commons NumberUtils to
 * kill my external dependencies. All other code is my own.
 *
 * @author Jason Ferguson
 * @since 0.1
 */
public class TdatProcessor {

    // regex patterns
    // find the name of the table in the file
    private static final String tableNameRegexPattern = "heasarc_(.+)$";

    /**
     * Convert an HEASARC file to SQL format. I'm sure I could be much more efficient, but hey, this is NASA data
     * so I need to get in the proper spirit
     *
     * @param filename String representing the name of the heasarc tdat file to process
     * @return a Map<String, String>
     */
    @SuppressWarnings({"EmptyCatchBlock"})
    private static Map<String, String> createColumnMap(String filename) {

        // create a linked hashmap mapping field names to their column types. Use LHM because I'm picky and
        // would prefer to preserve the order
        Map<String, String> columnMap = new LinkedHashMap<String, String>();

        // define the regex patterns
        // one regex to bind them, one regex to find them, one regex to bring them all and in the darkness bind them
        Pattern columnTypePattern = Pattern.compile("field\\[(.*)\\] = (float|int|char)([1-9][0-9]?)?(:\\.([0-9]))?");

        try {
            Scanner scanner = new Scanner(new FileInputStream(filename));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (line.indexOf("field[") != -1) {

                    String columnName = null;
                    String columnType = null;
                    String columnPrecision = null;
                    String columnScale = null;
                    Matcher columnTypeMatcher = columnTypePattern.matcher(line);

                    if (columnTypeMatcher.lookingAt()) {
                        int count = columnTypeMatcher.groupCount();
                        if (count > 1) {
                            columnName = columnTypeMatcher.group(1);
                            columnType = columnTypeMatcher.group(2);
                        }
                        if (count > 2) {
                            columnPrecision = columnTypeMatcher.group(3);
                        }
                        if (count >= 5) {
                            columnScale = columnTypeMatcher.group(5);
                        }
                    }

                    if (columnPrecision == null || columnPrecision.isEmpty()) {
                        columnPrecision = "8";
                    }

                    if (columnScale == null || columnScale.isEmpty()) {
                        columnScale = "4";
                    }

                    if (columnType.equals("int")) {
                        int precision = Integer.parseInt(columnPrecision);
                        if (precision <= 4) {
                            columnMap.put(columnName, "INTEGER");
                        } else {
                            columnMap.put(columnName, "BIGINT");
                        }
                    } else if (columnType.equals("float")) {
                        if (columnPrecision.equals("8") && columnPrecision.equals("8")) {
                            columnScale = "4";
                        }
                        columnMap.put(columnName, "DECIMAL(" + columnPrecision + "," + columnScale + ")");
                    } else {
                        // for some reason I have to use column scale here, not column precision, probably because of
                        // the order of the regex groups
                        columnMap.put(columnName, "VARCHAR(" + columnPrecision + ")");
                    }
                }

                if (line.indexOf("<DATA>") != -1) {
                    scanner.close();
                    break;
                }
            }

            scanner.close();
        } catch (FileNotFoundException e) {

        }

        return columnMap;
    }

    /**
     * Read the TDAT and get the table name
     *
     * @param filename String representing the filename
     * @return String representing the table name from the TDAT file
     */
    @SuppressWarnings({"EmptyCatchBlock"})
    public static String getTableName(String filename) {

        String tableName = null;
        Pattern tableNamePattern = Pattern.compile(tableNameRegexPattern);

        try {
            Scanner scanner = new Scanner(new FileInputStream(filename));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.indexOf("table_name = ") != -1) {
                    Matcher tableNameMatcher = tableNamePattern.matcher(line);
                    if (tableNameMatcher.find()) {
                        tableName = tableNameMatcher.group(1);
                        break;
                    } else {
                        System.out.println("Table name pattern not found");
                        scanner.close();
                        System.exit(0);
                    }

                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {

        }

        return tableName;
    }

    /**
     * Creates the DML table definition (i.e. CREATE TABLE blahblah ( etc etc) based on the name and the
     * column definitions.
     *
     * @param tablename         String representing what to name the table
     * @param columnDefinitions Map containing the column names and data types for the table
     * @return String representing the DML table creation statement
     */
    public static String createTableDefinition(String tablename, Map<String, String> columnDefinitions, boolean mysqlSpecific) {
        StringBuilder sb = new StringBuilder();
        if (mysqlSpecific) {
            sb.append("DROP TABLE IF EXISTS ").append(tablename).append(";\r\n");
        }
        sb.append("CREATE TABLE ").append(tablename).append("(\r\n");
        StringBuilder columns = new StringBuilder();
        for (String fieldname : columnDefinitions.keySet()) {
            // dec is a reserved word in mysql, change name. Do it for ra to be consistent
            if (fieldname.equalsIgnoreCase("dec")) {
                columns.append("decdeg").append(" ").append(columnDefinitions.get(fieldname)).append(",\r\n");
            } else if (fieldname.equalsIgnoreCase("ra")) {
                columns.append("radec").append(" ").append(columnDefinitions.get(fieldname)).append(",\r\n");
            } else {
                columns.append(fieldname).append(" ").append(columnDefinitions.get(fieldname)).append(",\r\n");
            }
        }
        String strColumns = columns.toString();
        sb.append(strColumns.substring(0, strColumns.lastIndexOf(",")));
        if (mysqlSpecific) {
            sb.append("\r\n) ENGINE=InnoDB CHARACTER SET=utf8;\r\n");
        } else {
            sb.append("\r\n);\r\n");
        }

        return sb.toString();
    }

    // stolen from apache commons

    // Empty checks
    //-----------------------------------------------------------------------

    /**
     * <p>Checks if a String is empty ("") or null.</p>
     * <p/>
     * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     * </pre>
     * <p/>
     * <p>NOTE: This method changed in Lang version 2.0.
     * It no longer trims the String.
     * That functionality is available in isBlank().</p>
     *
     * @param str the String to check, may be null
     * @return <code>true</code> if the String is empty or null
     */
    public static boolean isEmpty(String str) {
        return (str == null || str.length() == 0);
    }

    // stolen from commons-lang... license is the same, and this will kill my external dependencies. Yay.

    /**
     * <p>Checks whether the String a valid Java number.</p>
     * <p/>
     * <p>Valid numbers include hexadecimal marked with the <code>0x</code>
     * qualifier, scientific notation and numbers marked with a type
     * qualifier (e.g. 123L).</p>
     * <p/>
     * <p><code>Null</code> and empty String will return
     * <code>false</code>.</p>
     *
     * @param str the <code>String</code> to check
     * @return <code>true</code> if the string is a correctly formatted number
     */
    public static boolean isNumber(String str) {
        if (isEmpty(str)) {
            return false;
        }
        char[] chars = str.toCharArray();
        int sz = chars.length;
        boolean hasExp = false;
        boolean hasDecPoint = false;
        boolean allowSigns = false;
        boolean foundDigit = false;
        // deal with any possible sign up front
        int start = (chars[0] == '-') ? 1 : 0;
        if (sz > start + 1) {
            if (chars[start] == '0' && chars[start + 1] == 'x') {
                int i = start + 2;
                if (i == sz) {
                    return false; // str == "0x"
                }
                // checking hex (it can't be anything else)
                for (; i < chars.length; i++) {
                    if ((chars[i] < '0' || chars[i] > '9')
                            && (chars[i] < 'a' || chars[i] > 'f')
                            && (chars[i] < 'A' || chars[i] > 'F')) {
                        return false;
                    }
                }
                return true;
            }
        }
        sz--; // don't want to loop to the last char, check it afterwords
        // for type qualifiers
        int i = start;
        // loop to the next to last char or to the last char if we need another digit to
        // make a valid number (e.g. chars[0..5] = "1234E")
        while (i < sz || (i < sz + 1 && allowSigns && !foundDigit)) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                foundDigit = true;
                allowSigns = false;

            } else if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                hasDecPoint = true;
            } else if (chars[i] == 'e' || chars[i] == 'E') {
                // we've already taken care of hex.
                if (hasExp) {
                    // two E's
                    return false;
                }
                if (!foundDigit) {
                    return false;
                }
                hasExp = true;
                allowSigns = true;
            } else if (chars[i] == '+' || chars[i] == '-') {
                if (!allowSigns) {
                    return false;
                }
                allowSigns = false;
                foundDigit = false; // we need a digit after the E
            } else {
                return false;
            }
            i++;
        }
        if (i < chars.length) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                // no type qualifier, OK
                return true;
            }
            if (chars[i] == 'e' || chars[i] == 'E') {
                // can't have an E at the last byte
                return false;
            }
            if (!allowSigns
                    && (chars[i] == 'd' || chars[i] == 'D'
                    || chars[i] == 'f' || chars[i] == 'F')) {
                return foundDigit;
            }
            if (chars[i] == 'l' || chars[i] == 'L') {
                // not allowing L with an exponent
                return foundDigit && !hasExp;
            }
            // last character is illegal
            return false;
        }
        // allowSigns is true iff the val ends in 'E'
        // found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
        return !allowSigns && foundDigit;
    }

    /**
     * Convert the PSV (pipe-separated values) from the TDAT <DATA> section into a mess of insert statements. Note
     * that importing the statements takes a LONG time in MySQL, LOAD DATA INFILE is much, MUCH faster.
     *
     * @param filename   String representing the filename of the TDAT file.
     * @param tableName  String representing the name of the table
     * @param columnDefs Map<String, String> with the column names and data types
     * @return a List<String> containing the SQL insert statements
     */
    @SuppressWarnings({"UnusedAssignment", "EmptyCatchBlock"})
    public static List<String> createInsertStatements2(String filename, String tableName, Map<String, String> columnDefs) {

        List<String> results = new ArrayList<String>();

        // build the beginning of the insert statement
        StringBuilder insertStatement = new StringBuilder("INSERT INTO " + tableName + "(");
        StringBuilder columnList = new StringBuilder();
        for (String fieldname : columnDefs.keySet()) {
            columnList.append(fieldname).append(",");
        }

        String columns1 = columnList.toString();
        insertStatement.append(columns1.substring(0, columns1.length() - 1));   // append the column list to the INSERT
        insertStatement.append(") VALUES ("); // append the close PAREN, VALUES, and open paren

        //Pattern dataPattern = Pattern.compile("(.+)\\|^");
        Pattern dataPattern = Pattern.compile("^(.*?\\|)*$");

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher dataMatcher = dataPattern.matcher(line);
                if (dataMatcher.lookingAt()) {
                    StringBuilder newInsert = new StringBuilder(insertStatement);
                    String[] parts = line.split("\\|");
                    StringBuilder values = new StringBuilder();
                    for (String part : parts) {
                        if (isNumber(part)) {
                            values.append(part);
                        } else if (part.length() == 0) {
                            values.append("NULL");
                        } else {
                            values.append("'").append(part).append("'");
                        }
                        values.append(",");

                    }
                    newInsert.append(values.substring(0, values.length() -1));
                    newInsert.append(");");
                    results.add(newInsert.toString());
                }
            }

            reader.close();
        } catch (FileNotFoundException e) {

        } catch (IOException ex) {

        }

        return results;
    }

    @SuppressWarnings({"UnusedAssignment", "EmptyCatchBlock"})
    public static int getDataStartLine(String filename) {

        int counter = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line = null;
            while (!((line = reader.readLine()).equals("<DATA>"))) {
                counter++;
            }
            reader.close();

        } catch (FileNotFoundException e) {

        } catch (IOException ex) {

        }

        return ++counter;
    }

    /**
     * Create the LOAD DATA INFILE statement to load the data. It's assumed that a primary key does NOT exist on
     * the table, but that the table exists.
     *
     * @param filename   String representing the filename of the HEASARC file
     * @param tableName  String representing the name of the table
     * @param columnDefs Map<String, String> containing the column names and data types
     * @return a String representing the LOAD DATA INFILE statement
     */
    public static String createLoadInfileStatement(String filename, String tableName, Map<String, String> columnDefs) {

        int startLine = getDataStartLine(filename);

        StringBuilder stmt = new StringBuilder();
        stmt.append("LOAD DATA LOCAL INFILE '").append(filename).append("' INTO TABLE ").append(tableName).append("\r\n");
        stmt.append("FIELDS TERMINATED BY \"|\" \r\n");
        stmt.append("IGNORE ").append(startLine).append(" LINES;\r\n");

        return stmt.toString();
    }

    /**
     * Add the ID column to the table. This method is intended to be run AFTER the LOAD DATA INFILE, and may take
     * awhile based on the size of the table. Get comfortable and grab a caffinated beverage, especially if you
     * are importing Tycho, Hipparcos, etc. (Just wait... GAIA launches in 2013 and has a 10x larger dataset)
     *
     * @param tableName String representing the name of the table
     * @return String representing the Alter statement
     */
    public static String createAlterStatement(String tableName) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ").append(tableName).append(" ADD COLUMN ID BIGINT AUTO_INCREMENT PRIMARY KEY;\r\n");
        return sb.toString();
    }

    /**
     * Create the primary key on the table. This method is intended to be run after the LOAD DATA INFILE has run, and
     * after the ID column has been created. This one might take awhile too.
     *
     * @param tableName String representing the name of the table
     * @return String containing the ALTER statement
     */
    public static String createConstraintStatement(String tableName) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ").append(tableName).append(" ADD CONSTRAINT pk_").append(tableName.substring(0, 3));
        sb.append(" PRIMARY KEY(ID);");

        return sb.toString();
    }

    public static void main(String[] args) {

        // don't like how long this main method is, but at least the code works...

        if (args.length < 1) {
            System.out.println("You must provide a filename for the TDAT input file.");
            System.exit(0);
        }
        String filename1 = args[0];
        // mysql gets grouchy about the windows file separator, so replace a single slash with a double
        if (System.getProperty("os.name").indexOf("Windows") != -1) {
            filename1 = filename1.replace("\\", "\\\\");
        }

        String outputdir;
        if (args.length == 1) {
            outputdir = ".\\";
        } else {
            outputdir = args[1];
            if (System.getProperty("os.name").indexOf("Windows") != -1) {
                outputdir = outputdir.replace("\\", "\\\\");
            }
        }

        Console console = System.console();

        String tableName = getTableName(filename1);
        Map<String, String> columnMap = createColumnMap(filename1);

        String mysqlSpecific = console.readLine("Do you wish to generate MySQL-Specific extensions? (Y/N, Default Y) ");
        String tableDef;
        List<String> insertStatements = new ArrayList<String>();
        String loadStatement = "";
        if (mysqlSpecific.isEmpty() || mysqlSpecific.substring(0, 1).equalsIgnoreCase("N")) {
            tableDef = createTableDefinition(tableName, columnMap, false);
            insertStatements = createInsertStatements2(filename1, tableName, columnMap);
        } else {
            tableDef = createTableDefinition(tableName, columnMap, true);
            // offer user the option to generate SQL statements even in mysql (SLOW AS HELL FOR LARGE TDATS)
            String slowWay = console.readLine("Do you wish to generate insert statements (not recommended, LOAD DATA INFILE is faster) (Y/N)? ");
            if (slowWay.isEmpty() || !slowWay.substring(0, 1).equalsIgnoreCase("Y")) {
                loadStatement = createLoadInfileStatement(filename1, tableName, columnMap);
            } else {
                insertStatements = createInsertStatements2(filename1, tableName, columnMap);
            }
        }

        String alterStatement = createAlterStatement(tableName);
        String firstColumn = (String) columnMap.keySet().toArray()[0];
        String deleteEnd = "DELETE FROM " + tableName + " WHERE " + firstColumn + "='<END>';";

        String dropColumns = console.readLine("Do you wish to drop columns from the generated table (Y/N)? ");
        StringBuilder dropStatement = new StringBuilder();
        if (dropColumns.substring(0, 1).equalsIgnoreCase("Y")) {
            dropStatement.append("ALTER TABLE ").append(tableName).append(" ");
            for (String column : columnMap.keySet()) {
                // make sure they don't get to drop the ID column
                if (!column.equalsIgnoreCase("ID")) {
                    String query = console.readLine("Drop column " + column + " (Y/N)? ");
                    if (query.substring(0, 1).equalsIgnoreCase("Y")) {
                        dropStatement.append("DROP COLUMN ").append(column).append(", ");
                    }
                }
            }
        }

        // kill the trailing comma
        String dropColumnStatement = null;
        if (dropStatement.length() > 0) {
            dropColumnStatement = dropStatement.substring(0, dropStatement.lastIndexOf(",")) + ";";
        }

        try {
            Writer writer = new BufferedWriter(new FileWriter(outputdir + tableName + ".sql"));
            writer.write(tableDef);
            writer.write("\r\n");
            if (!loadStatement.isEmpty()) {
                writer.write(loadStatement);
            } else {
                if (!insertStatements.isEmpty()) {
                    for (String stmt : insertStatements) {
                        writer.write(stmt);
                        writer.write("\r\n");
                    }
                }
            }
            writer.write("\r\n");
            writer.write(alterStatement);
            writer.write("\r\n");
            writer.write(deleteEnd);
            writer.write("\r\n");
            writer.write("\r\n");

            if (dropColumnStatement != null) {
                writer.write(dropColumnStatement);
                writer.write("\r\n");
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("IOException: Program terminated.");
            System.exit(0);
        }

        System.out.println("File output to " + outputdir + tableName + ".sql");


    }
}
