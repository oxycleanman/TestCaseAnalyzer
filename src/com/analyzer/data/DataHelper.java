package com.analyzer.data;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import javax.print.URIException;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * This class handles all data retrieval and saving for the application
 *
 * Written by Chris Leatherwood
 * 02/05/2017
 *
 */
public class DataHelper {

    private static final String CONFIGFILE = "config.txt";
    public static ObservableList<String> updatedDataSheets = FXCollections.observableArrayList();
    public static List<String> keywords = new ArrayList<>();
    public static String directory;

    public DataHelper() {

    }



    /**
     * This method searches for the "RunManager.xls" file, and if located it takes the search string and
     * finds the test case in the "RunManager.xls" file. Using data from the "RunManager.xls" file, this method
     * then finds the correct data sheet and extracts keyword and data field information and returns it in a
     * Map.
     *
     * Data is mapped as follows:
     * "datasheet" = the name of the datasheet the test uses
     * "vdi" = the name of the VDI the test is set to run on
     * "keywords" = an ObservableList<String> of keywords used by the test
     * "data" = data points used by the test, formatted [Column Header] = [Data Value]
     *
     * @param searchString description: String being searched for, Test Case Name
     * @return description: Returns ObservableMap<String, ObservableList<String>>
     * @throws IOException description: Standard IOException
     */
    public static Service<ObservableMap<String, ObservableList<String>>> getTestData(String searchString) {
        //Return a service that will search the datasheets to avoid freezing the UI
        Service<ObservableMap<String, ObservableList<String>>> service = new Service<ObservableMap<String, ObservableList<String>>>() {
            @Override
            protected Task<ObservableMap<String, ObservableList<String>>> createTask() {
                return new Task<ObservableMap<String, ObservableList<String>>>() {
                    @Override
                    protected ObservableMap<String, ObservableList<String>> call() throws InterruptedException, URISyntaxException, IOException {
                        ObservableMap<String, ObservableList<String>> results = FXCollections.observableHashMap();
                        String fileName = directory + "\\RunManager.xls";
                        String dataSheet = null;

                        try {
                            FileInputStream file = new FileInputStream(fileName);
                            HSSFWorkbook workbook = new HSSFWorkbook(file);

                            //Initialize results map and add keys

                            results.put("datasheet", FXCollections.observableArrayList());
                            results.put("vdi", FXCollections.observableArrayList());
                            results.put("keywords", FXCollections.observableArrayList());
                            results.put("data", FXCollections.observableArrayList());
                            updateProgress(5, 100);
                            Row foundRow = null;

                            int numOfSheets = workbook.getNumberOfSheets();
                            Map<Sheet, List<Row>> resultMap = new HashMap<Sheet, List<Row>>();

                            for (int i = 0; i < numOfSheets; i++) {
                                HSSFSheet sheet = workbook.getSheetAt(i);
                                Iterator<Row> rowIterator = sheet.iterator();
                                while (rowIterator.hasNext()) {
                                    Row row = rowIterator.next();

                                    Iterator<Cell> cellIterator = row.cellIterator();

                                    while (cellIterator.hasNext()) {
                                        Cell cell = cellIterator.next();

                                        if (cell.getCellTypeEnum().equals(CellType.STRING) && (cell.getStringCellValue().trim().equalsIgnoreCase(searchString.trim()) || cell.getStringCellValue().trim().contains(searchString.trim()))) {
                                            if (!resultMap.containsKey(cell.getSheet())) {
                                                resultMap.put(cell.getSheet(), new ArrayList<Row>());
                                            }
                                            List<Row> tempList = resultMap.get(cell.getSheet());
                                            if (!tempList.contains(cell.getRow())) {
                                                tempList.add(cell.getRow());
                                                resultMap.put(cell.getSheet(), tempList);
                                            }
                                        }

                                    }
                                }
                            }
                            updateProgress(20, 100);
                            for (Sheet sheet : resultMap.keySet()) {
                                ObservableList<String> tempList = results.get("vdi");
                                if (sheet.getSheetName().contains("RMS")) {
                                    tempList.add(sheet.getSheetName());
                                    results.put("vdi", tempList);
                                }
                            }
                            updateProgress(40, 100);
                            if (resultMap.containsKey(workbook.getSheet("TOTAL"))) {
                                Row row = resultMap.get(workbook.getSheet("TOTAL")).get(0);
                                Cell cell = row.getCell(0);
                                dataSheet = cell.getStringCellValue().trim() + ".xls";
                                ObservableList<String> tempList = results.get("datasheet");
                                tempList.add(dataSheet);
                                results.put("datasheet", tempList);
                            }

                            file.close();
                            updateProgress(60, 100);
                            if (dataSheet != null) {
                                fileName = directory + "\\TestData\\Excel\\" + dataSheet;

                                file = new FileInputStream(fileName);
                                workbook = new HSSFWorkbook(file);

                                //Get keyword data
                                HSSFSheet sheet = workbook.getSheet("Keywords");

                                Iterator<Row> rowIterator = sheet.iterator();

                                while (rowIterator.hasNext()) {
                                    Row row = rowIterator.next();

                                    Iterator<Cell> cellIterator = row.cellIterator();

                                    while (cellIterator.hasNext()) {
                                        Cell cell = cellIterator.next();

                                        if (cell.getCellTypeEnum().equals(CellType.STRING) && (cell.getStringCellValue().trim().equalsIgnoreCase(searchString.trim()) || cell.getStringCellValue().trim().contains(searchString.trim()))) {
                                            foundRow = cell.getRow();
                                            break;
                                        }

                                    }
                                    if (foundRow != null) {
                                        break;
                                    }
                                }

                                if (foundRow != null) {
                                    Iterator<Cell> cellIterator = foundRow.cellIterator();
                                    while (cellIterator.hasNext()) {
                                        Cell cell = cellIterator.next();
                                        if (cell.getColumnIndex() == 0) {
                                            continue;
                                        } else {
                                            if (!cell.getStringCellValue().trim().isEmpty()) {
                                                ObservableList<String> tempList = results.get("keywords");
                                                tempList.add(cell.getStringCellValue().trim());
                                                results.put("keywords", tempList);
                                            }
                                        }
                                    }
                                }

                                sheet = workbook.getSheet("DataSheet");
                                Map<String, String> testDataMap = new HashMap<>();
                                Map<Integer, String> headerMap = new HashMap<>();
                                updateProgress(80, 100);
                                //Get data points and add to map using headerMap to get keys
                                Iterator<Row> dataRowIterator = sheet.rowIterator();
                                Integer matchRowIndex = null;
                                while (dataRowIterator.hasNext()) {
                                    Row row = dataRowIterator.next();
                                    Iterator<Cell> cellIterator = row.cellIterator();

                                    while (cellIterator.hasNext()) {
                                        Cell cell = cellIterator.next();
                                        if (cell.getRowIndex() == 0) {
                                            //assign header values to header map
                                            if (!cell.getStringCellValue().trim().isEmpty()) {
                                                headerMap.put(cell.getColumnIndex(), cell.getStringCellValue().trim());
                                            }
                                        } else {
                                            if (cell.getCellTypeEnum().equals(CellType.STRING) && (cell.getStringCellValue().trim().equalsIgnoreCase(searchString.trim()) || cell.getStringCellValue().trim().contains(searchString.trim()))) {
                                                matchRowIndex = cell.getRowIndex();
                                                break;
                                            }
                                        }
                                    }
                                    if (matchRowIndex != null) {
                                        break;
                                    }
                                }

                                if (matchRowIndex != null) {
                                    //Once we have the headerMap and correct row index for our test case, populate the data points
                                    Row row = sheet.getRow(matchRowIndex);
                                    Iterator<Cell> cellIterator = row.cellIterator();
                                    while (cellIterator.hasNext()) {
                                        Cell cell = cellIterator.next();
                                        if (cell.getColumnIndex() > 0) {
                                            if (cell.getCellTypeEnum().equals(CellType.STRING) && headerMap.containsKey(cell.getColumnIndex())) {
                                                testDataMap.put(headerMap.get(cell.getColumnIndex()), cell.getStringCellValue());
                                            } else if (cell.getCellTypeEnum().equals(CellType.BOOLEAN) && headerMap.containsKey(cell.getColumnIndex())) {
                                                testDataMap.put(headerMap.get(cell.getColumnIndex()), String.valueOf(cell.getBooleanCellValue()));
                                            } else if (cell.getCellTypeEnum().equals(CellType.NUMERIC) && headerMap.containsKey(cell.getColumnIndex())) {
                                                testDataMap.put(headerMap.get(cell.getColumnIndex()), String.valueOf(cell.getNumericCellValue()));
                                            } else if (cell.getCellTypeEnum().equals(CellType.BLANK) && headerMap.containsKey(cell.getColumnIndex())) {
                                                testDataMap.put(headerMap.get(cell.getColumnIndex()), "");
                                            } else {
                                                System.out.println("Ignored data in cell " + cell.getAddress());
                                            }
                                        }
                                    }
                                }

                                for (String key : testDataMap.keySet()) {
                                    ObservableList<String> tempList = results.get("data");
                                    tempList.add(key + ": " + testDataMap.get(key));
                                    results.put("data", tempList);
                                }

                                //Close the file stream once we've searched the file
                                file.close();
                                results.put("searchString", FXCollections.observableArrayList());
                                ObservableList<String> tempList = results.get("searchString");
                                tempList.add(searchString);
                                results.put("searchString", tempList);
                            } else {
                                if (!results.containsKey("errors")) {
                                    results.put("errors", FXCollections.observableArrayList());
                                }
                                ObservableList<String> tempList = results.get("errors");
                                tempList.add("No Results Found");
                                results.put("errors", tempList);
                                return results;
                            }
                        } catch (IOException e) {
                            if (!results.containsKey("errors")) {
                                results.put("errors", FXCollections.observableArrayList());
                            }
                            ObservableList<String> tempList = results.get("errors");
                            tempList.add(e.getMessage());
                            results.put("errors", tempList);
                            return results;
                        }
                        updateProgress(100, 100);
                        return results;
                    }
                };
            }
        };

        return service;
    }

    public static Service<Boolean> writeTestData(ObservableMap<String, ObservableList<String>> updatedResults) throws IOException {
        //Return service that will write data back to the datasheets to avoid freezing the UI
        Service<Boolean> service = new Service<Boolean>() {
            @Override
            protected Task<Boolean> createTask() {
                return new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws IOException {
                        //Update datasheet with changed values
                        String fileName = directory + "\\TestData\\Excel\\" + updatedResults.get("datasheet").get(0);
                        String searchString = updatedResults.get("searchString").get(0);
                        try {
                            FileInputStream file = new FileInputStream(fileName);
                            HSSFWorkbook workbook = new HSSFWorkbook(file);
                            Boolean needToWriteFile = false;

                            //Cell style for highlighting changed cells
                            HSSFCellStyle style = workbook.createCellStyle();
                            style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
                            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                            Integer rowToUpdate = null;

                            HSSFSheet sheet = workbook.getSheet("DataSheet");
                            Iterator<Row> rowIterator = sheet.iterator();
                            HashMap<String, Integer> headerMap = new HashMap<String, Integer>();
                            updateProgress(20, 100);
                            while (rowIterator.hasNext()) {
                                Row row = rowIterator.next();

                                Iterator<Cell> cellIterator = row.cellIterator();

                                while (cellIterator.hasNext()) {
                                    Cell cell = cellIterator.next();
                                    if (cell.getRowIndex() == 0) {
                                        //assign header values to header map
                                        headerMap.put(cell.getStringCellValue().trim(), cell.getColumnIndex());
                                    }
                                    if (cell.getCellTypeEnum().equals(CellType.STRING) && (cell.getStringCellValue().trim().equalsIgnoreCase(searchString.trim()) || cell.getStringCellValue().trim().contains(searchString.trim()))) {
                                        rowToUpdate = cell.getRowIndex();
                                        break;
                                    }
                                }
                                if (rowToUpdate != null) {
                                    break;
                                }
                            }
                            updateProgress(40, 100);
                            //Get row to update and update necessary cells
                            if (rowToUpdate != null) {
                                Row row = sheet.getRow(rowToUpdate);
                                ObservableList<String> dataValues = updatedResults.get("data");
                                for (String data : dataValues) {
                                    //Split data values on the colon, index 0 is the header, index 1 is the data
                                    List<String> splitValues = new ArrayList<>();
                                    splitValues = Arrays.asList(data.split(": "));
                                    if (splitValues.size() == 0) {
                                        continue;
                                    } else {
                                        Integer index = headerMap.get(splitValues.get(0).trim());
                                        Cell cell = row.getCell(index);
                                        if (splitValues.size() > 1) {
                                            if (cell.getCellTypeEnum().equals(CellType.STRING)) {
                                                cell.setCellValue(splitValues.get(1).trim());
                                                cell.setCellStyle(style);
                                                needToWriteFile = true;
                                            } else if (cell.getCellTypeEnum().equals(CellType.BOOLEAN)) {
                                                cell.setCellValue(Boolean.valueOf(splitValues.get(1).trim()));
                                                cell.setCellStyle(style);
                                                needToWriteFile = true;
                                            } else if (cell.getCellTypeEnum().equals(CellType.NUMERIC)) {
                                                cell.setCellValue(Double.valueOf(splitValues.get(1).trim()));
                                                cell.setCellStyle(style);
                                                needToWriteFile = true;
                                            } else if (cell.getCellTypeEnum().equals(CellType.BLANK)) {
                                                cell.setCellValue(splitValues.get(1).trim());
                                                cell.setCellStyle(style);
                                                needToWriteFile = true;
                                            } else {
                                                System.out.println("Failed to update cell " + cell.getAddress() + " with " + splitValues.get(1).trim());
                                            }
                                        } else {
                                            cell.setCellValue("");
                                            cell.setCellStyle(style);
                                            needToWriteFile = true;
                                        }
                                    }
                                }
                                //Highlight first cell of row
                                Cell cell = row.getCell(0);
                                cell.setCellStyle(style);
                            }
                            updateProgress(60, 100);

                            //Update keywords sheet
                            rowToUpdate = null;

                            sheet = workbook.getSheet("Keywords");
                            rowIterator = sheet.iterator();
                            //Find the row that needs to be updated by searching the sheet for our searchString
                            while (rowIterator.hasNext()) {
                                Row row = rowIterator.next();

                                Iterator<Cell> cellIterator = row.cellIterator();

                                while (cellIterator.hasNext()) {
                                    Cell cell = cellIterator.next();
                                    if (cell.getCellTypeEnum().equals(CellType.STRING) && (cell.getStringCellValue().trim().equalsIgnoreCase(searchString.trim()) || cell.getStringCellValue().trim().contains(searchString.trim()))) {
                                        rowToUpdate = cell.getRowIndex();
                                        break;
                                    }
                                }
                                if (rowToUpdate != null) {
                                    break;
                                }
                            }
                            updateProgress(80, 100);
                            //Update cells
                            if (rowToUpdate != null) {
                                Row row = sheet.getRow(rowToUpdate);
                                ObservableList<String> keywordValues = updatedResults.get("keywords");
                                //Update each cell and add highlight
                                for (int i = 1; i <= keywordValues.size(); i++) {
                                    Cell cell = row.getCell(i);
                                    cell.setCellValue(keywordValues.get(i-1));
                                    cell.setCellStyle(style);
                                }

                                //Highlight first cell of row
                                Cell cell = row.getCell(0);
                                cell.setCellStyle(style);
                            }

                            file.close();
                            if (needToWriteFile) {
                                FileOutputStream outputFile = new FileOutputStream(fileName);
                                workbook.write(outputFile);
                                outputFile.close();
                                System.out.println("SUCCESSFULLY UPDATED " + fileName);
                                if (!updatedDataSheets.contains(updatedResults.get("datasheet").get(0))) {
                                    updatedDataSheets.add(0, updatedResults.get("datasheet").get(0));
                                }
                                updateProgress(100, 100);
                                return true;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            return false;
                        }
                        return false;
                    }
                };
            }
        };

        return service;
    }

    public static Task loadKeywords() {
        Task loadKeywordsTask = new Task() {
            @Override
            protected Object call() throws Exception {
                //Load keyword data
                String fileName = directory + "\\RunManager.xls";
                List<String> dataSheets = new ArrayList<>();


                try {
                    FileInputStream file = new FileInputStream(fileName);
                    HSSFWorkbook workbook = new HSSFWorkbook(file);
                    HSSFSheet sheet = workbook.getSheet("TOTAL");
                    Integer numOfRows = sheet.getFirstRowNum() + sheet.getLastRowNum() - 1;
                    //Start i at 1 to skip the header row
                    for (Integer i = 1; i < numOfRows; i++) {
                        Cell cell = sheet.getRow(i).getCell(0);
                        if (!cell.getStringCellValue().trim().equals("") && !dataSheets.contains(cell.getStringCellValue().trim() + ".xls")) {
                            dataSheets.add(cell.getStringCellValue().trim() + ".xls");
                        }
                    }
                    System.out.println("Found " + dataSheets.size() + " data sheets");
                    file.close();
                    if (dataSheets.size() > 0) {
                        for (String dataSheet : dataSheets) {
                            System.out.println("Checking data sheet " + dataSheet);
                            fileName = directory + "\\TestData\\Excel\\" + dataSheet;
                            file = new FileInputStream(fileName);
                            workbook = new HSSFWorkbook(file);
                            sheet = workbook.getSheet("Keywords");
                            Iterator<Row> rowIterator = sheet.rowIterator();
                            while (rowIterator.hasNext()) {
                                Row row = rowIterator.next();
                                //Skip the header row
                                if (row.getRowNum() > 1) {
                                    Iterator<Cell> cellIterator = row.cellIterator();
                                    while (cellIterator.hasNext()) {
                                        Cell cell = cellIterator.next();
                                        if (!keywords.contains(cell.getStringCellValue().trim()) && cell.getColumnIndex() > 1) {
                                            keywords.add(cell.getStringCellValue().trim());
                                        }
                                    }
                                }
                            }
                            file.close();
                            updateProgress(dataSheets.indexOf(dataSheet), dataSheets.size());
                        }
                    }
                    keywords.sort((string1, string2) -> {return string1.compareToIgnoreCase(string2);});
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        return loadKeywordsTask;
    }

    public static ObservableList<String> getKeywords() {
        ObservableList<String> keywordList = FXCollections.observableList(keywords);
        return keywordList;
    }

    /**
     * Read the config file and return a key/value map of settings
     */
    public static Map<String, String> readConfig() throws IOException {
        Map<String, String> configSettings = new HashMap<String, String>();
        String input = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(CONFIGFILE))) {
            while ((input = reader.readLine()) != null) {
                String[] line = input.split("=");
                if (!configSettings.containsKey(line[0])) {
                    configSettings.put(line[0], line[1]);
                }
            }
            System.out.println("SUCCESSFULLY READ CONFIG FILE");
        } catch (IOException e) {
            System.out.println("ERROR WHILE READING CONFIG FILE:");
            e.printStackTrace();
        }

        return configSettings;
    }

    /**
     * Write the config file back to local storage
     */
    public static void writeConfig(Map<String, String> configSettings) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIGFILE))) {
            for (String key : configSettings.keySet()) {
                writer.write(key + "=" + configSettings.get(key));
                writer.newLine();
            }
            System.out.println("SUCCESSFULLY WROTE CONFIG FILE");
        } catch (IOException e) {
            System.out.println("ERROR WHILE WRITING CONFIG FILE:");
            e.printStackTrace();
        }
    }

}
