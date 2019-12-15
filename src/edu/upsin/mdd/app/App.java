/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.upsin.mdd.app;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Pedro Arnoldo Machado Durán
 * Universidad Politécnica de Sinaloa - Maestría en Ciencias Aplicadas - Materia: Minería de Datos
 * 
 */
public class App extends javax.swing.JFrame {
    private final String
            rulePreffixTxtToolTextTip
            , ruleSuffixTxtToolTextTip;
    private int
            numberOfItems
            , numberOfTransactions
            ,truePositive
            , trueNegative
            , falsePositive
            , falseNegative;
    private boolean
            correctArguments
            , isDatatablePresent
            , isDatatableCorrect
            ,areTablesCalculated
            , isTableEnabled
            , firstRowAsTitle;
    private String
            imgDir
            , imgFile
            , imgFilePath;
    private Integer[][] data;
    private Boolean[][] booleanData;
    private final DefaultTableModel tableModel;
    private ArrayList<javax.swing.JRadioButton> significanceLevels;
    private final HashMap<Double, Double> piValues;
    private HashMap<Integer, Integer> greatersThanNumberOfItems;
    
    /**
     * CONSTRUCTOR DE LA CLASE USADO PARA INICIALIZAR LOS ATRIBUTOS DE LA MISMA
     */
    public App() {
        this.numberOfItems = 8;
        this.numberOfTransactions = 10;
        this.truePositive = 0;
        this.trueNegative = 0;
        this.falsePositive = 0;
        this.falseNegative = 0;
        this.isDatatablePresent = false;
        this.areTablesCalculated = false;
        this.isTableEnabled = true;
        this.firstRowAsTitle = true;
        this.imgDir =
                "src" + File.separator
                + "edu" + File.separator
                + "upsin" + File.separator
                + "mdd" + File.separator
                +"img" + File.separator;
        this.imgFile = "proyect_icon.png";
        this.imgFilePath =
                this.imgDir
                + this.imgFile;
        
        this.rulePreffixTxtToolTextTip =
                "Por favor ingresa los elementos del antecedente separados por coma";
        this.ruleSuffixTxtToolTextTip =
                "Por favor ingresa los elementos del concecuente separados por coma";
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(this.imgFilePath));
        this.tableModel = new DefaultTableModel();
        this.greatersThanNumberOfItems = new HashMap<>();
        this.piValues = new HashMap<>();
        
        //PI VALUES CON 2 GRADOS DE LIBERTAD
        this.piValues.put(0.1, 2.71);
        this.piValues.put(0.05, 3.84);
        this.piValues.put(0.01, 6.63);
        
        System.out.println("*********************************************************");
        System.err.println("CONFIGURATION");
        System.out.println("*********************************************************");
        System.err.println("APPLICATION ICON: " + this.imgFilePath);
        
        initComponents();
        customizeComponents();
    }
    
    /**
     * METODO UTILIZADO PARA PODER AGREGAR ATRIBUTOS A LOS ELEMENTOS GENERADOS POR NETBEANS
     * , DE MANERA PROGRAMÁTICA, ESTE METODO ES LLAMADO DESPUES DEL METODO INIT COMPONENTS
     * DEBIDO A QUE ES ESE MÉTODO EL QUE SE ENCARGA DE CREAR LOS WIDGETS CARGADOS EN LA VISTA
     * DE DISEÑO.
     */
    public void customizeComponents() {
        this.correctArguments = true;
        
        //SE AGRUPAN LOS RADIO BUTTON DETERMINAR EL VALOR DEL P VALUE
        this.chiSquareRadioGroup.add(this.significance001Radio);
        this.chiSquareRadioGroup.add(this.significance005Radio);
        this.chiSquareRadioGroup.add(this.significance01Radio);
        
        this.significance005Radio.setSelected(true);
        
        /*
        SE AGREGAN LOS RADIO BUTTON A UN ArrayList PARA PODER RECORRERLAS
        POR MEDIO DE UN CICLO Y DETERMINAR CUAL ESTA SELECCIONADA EN EL MÉTODO
        getSignificanceLevel
        */
        this.significanceLevels = new ArrayList<>();
        this.significanceLevels.add(significance001Radio);
        this.significanceLevels.add(significance005Radio);
        this.significanceLevels.add(significance01Radio);
        
        this.rulePreffixTxt.setToolTipText(this.rulePreffixTxtToolTextTip);
        this.ruleSuffixTxt.setToolTipText(this.ruleSuffixTxtToolTextTip);
        
        this.dataTable.setEnabled(this.isTableEnabled);
        
        //SE FIJA UN MODELO A LA JTABLE
        this.dataTable.setModel(this.tableModel);
    }
    
    /** 
     * GENERA UNA MATRIZ DE CEROS, ESTE MÉTODO SOLO SIRVIO COMO PRUEBA
     * PARA SABER COMO RELLENAR LA JTABLE
     */
    private Integer[][] initializeTableWithZeros() {
        ArrayList<Integer> zerosList = new ArrayList<>();
        Integer[][] zerosTable = new Integer[this.numberOfTransactions][];
        for(int t=0; t<this.numberOfTransactions; t++) {
            for(int i=0; i<this.numberOfItems; i++){
                zerosList.add(0);
            }
            zerosTable[t] =  zerosList.toArray(new Integer[zerosList.size()]);
        }
        return zerosTable;
    }
    
    /** 
     * GENERA UNA TABLA CON VALORES ALEATORIOS HACIENDO UN RECORRIDO POR COLUMNAS Y NO POR FILAS
     * , DEBIDO A QUE SI SE RELLENA POR FILAS EN LUGAR DE POR COLUMNAS LOS VALORES GENERADOS
     * NO SON TAN ALEATORIOS.
     */
    private Integer[][] generateRandomTable(Integer[][] matrix) {
        Integer[][] randomMatrix = new Integer[matrix.length][matrix[0].length];
        for(int i=0;i<randomMatrix[0].length;i++) {
            for(int j=0; j<randomMatrix.length;j++) {
                //SE AGREGAN A LA LISTA VALORES ALEATORIOS ENTEROS ENTRE CERO Y UNO DEBIDO A QUE 2 ES EL LÍMITE
                randomMatrix[j][i] = new Random().nextInt(2);
            }
        }
        return randomMatrix;
    }
    
    /**
     * METODO UTILIZADO EN UNA FASE TEMPRANA DE DESARROLLO CON EL PROPÓSITO DE GENERAR
     * UNA MATRIZ TRANSPUESTA A PARTIR DE UNA TABLA ALEATORIA ORIGINAL. ESTO SE HACÍA
     * PORQUE LA TABLA ALEATORIA GENERADA EN UNA FASE TEMPRANA NO ERA TAN ALEATORIA
     * DEBIDO A QUE RELLENANDO FILA POR FILA LOS VALORES NO ERAN TAN ALEATORIOS
     * , ESTO SE ARREGLO GENERANDO UNA MATRIZ ALEATORIA QUE SE RECORRE POR COLUMNAS Y NO POR FILAS
     * 
     * @param matrix
     * @return 
     */
    private Integer[][] getMatrixTranspose(Integer[][] matrix) {
        Integer[][] transposeMatrix = new Integer[matrix[0].length][matrix.length];
        for(int i=0;i<matrix.length;i++) {
                for(int j=0; j<matrix[0].length; j++){
                    transposeMatrix[j][i] = matrix[i][j];
                }
            }
        return transposeMatrix;   
    }
    
    /**
     * SE GENERA UNA MATRIZ BOOLEANA A PARTIR DE UNA MATRIZ RANDOM GENERADA
     * CON EL PROPOSITO DE PODER HACER LA OPERACION BOOLEANA
     * result = result && this.booleanData[i][j]; EN EL MÉTODO getBooleanResult
     * @param matrix
     * @return 
     */
    private Boolean[][] getBooleanTable(Integer[][] matrix) {
        Boolean[][] booleanTable = new Boolean[matrix.length][matrix[0].length];
        for(int i=0;i<matrix.length;i++) {
            for(int j=0;j<matrix[0].length;j++) {
                switch(matrix[i][j]) {
                    case 0:
                       booleanTable[i][j] = false;
                       break;
                    case 1:
                        booleanTable[i][j] = true;
                }
            }
        }
        return booleanTable;
    }
    
    /**
     * SE OBTIENEN LOS INDICES DE UNA EXPRESIÓN Y SE ORDENAN ASCENDENTEMENTE
     * PARA PODER DETERMINAR LAS COLUMNAS QUE SERAN EVALUADAS
     * 
     * @param text
     * @return 
     */
    private ArrayList<Integer> getOrdererIndexes(String text) {
        ArrayList<Integer> indexes = new ArrayList<>();
        text = text.replaceAll("[^0-9,]", "");      
        int i=0;
        for(String digit : text.split(",")) {
            i++;
            if(!digit.equals("")) {
                int number = Integer.parseInt(digit);
                
                    //SOLO SE REGRESAN AQUELLOS NUMEROS MENORES AL NUMERO TOTAL DE COLUMNAS
                if(number <= this.numberOfItems && number > 0) {
                    indexes.add(number);
                } else {
                    //SE GUARDAN AQUELLOS INDICES MAYORES AL NUMERO DE COLUMNAS EXISTENTES
                    this.greatersThanNumberOfItems.put(number, i);
                }
            }
        }
        //LOS INDICES SE ORDENAN PARA NO PEDIRLE AL USUARIO QUE LOS INGRESE ORDENADOS
        Collections.sort(indexes);
        return indexes;
    }
    
    /**
     * OBTIENE LAS COLUMNAS QUE SERAN EVALUADAS A PARTIR DE UNA EXPRESIÓN
     * , LA CUAL ES OBTENIDA DE UNA CAJA DE TEXTO
     * 
     * @param indexes
     * @param inputBox
     * @return 
     */
    public Boolean[] getIndexesResults(ArrayList indexes, javax.swing.JTextArea inputBox) {
        Boolean[] indexesResults = null;
        if(!inputBox.getText().isEmpty()) {
                indexes = this.getOrdererIndexes(inputBox.getText());               
                indexesResults = this.getBooleanResult(indexes);
        }
        return indexesResults;
    }
    
    /**
     * OBTIENE EL RESULTADO BOOLEANO DE CADA RENGLON SOLO DE AQUELLAS COLUMNAS INDICADAS
     * , TAL RESULTADO ES OBTENIDO GRACIAS A LA ECUASIÓN result = result && this.booleanData[i][j];
     * 
     * @param indexes: LAS COLUMNAS INDICADAS
     * @return 
     */
    public Boolean[] getBooleanResult(ArrayList<Integer> indexes) {
        Boolean[] results = new Boolean[this.booleanData.length];
        boolean result = true;
        int index = 0;
        for(int i=0; i<this.booleanData.length;i++) {
            result = true;
            index = 0;
            
            /*
            SE UTILIZO UN CLCLO WHILE DEBIDO A QUE ERA NECESARIO
            NO INCREMENTAR LA POSICIÓN DE LA CELDA MIENTRAS
            EL INDICE DEL PREFIJO FUERA EL MISMO
            */
            int j=0;
            while(j<this.booleanData[i].length) {
                if(index < indexes.size()) {
                    /*
                    SE COMPARA EL INDICE j CON (preffixes.get(index)-1) PORQUE PARA LA MAQUINA EL INDICE J EQUIVALE A (preffixes.get(index)-1)
                    POR EJEMPLO LA COLUMNA QUE ES 2 PARA EL HUMANO, EN REALIDAD ES 1 PARA LA MÁQUINA 
                    */
                    if(j == (indexes.get(index)-1)){
                        result = result && this.booleanData[i][j];
                        index++;
                    } else {
                        j++;
                    }
                } else {
                    j++;
                }
            }
            results[i] = result;
        }
        return results;
    }
    
    public int getNumberOfItems(String text) {
        int numberOfItems = 0;
        
        text = text.replaceAll("[^0-9]", "");
        numberOfItems = Integer.parseInt(text);
        
        return numberOfItems;
    }
    
    /**
     * CONSTRUYE UN tableModel A PARTIR DE UN ARREGLO DE NUMEROS ENTEROS RECIBIDO
     */
    public void buidRandomTable() {
        System.out.println("*********************************************************");
        System.err.println("GENERATING RANDOM TABLE");
        System.out.println("*********************************************************"); 
        
        String numberOfItemsStr = JOptionPane.showInputDialog(App.this
                    , "Por Favor ingrese el numero de registros deseado");
        
        this.numberOfTransactions = this.getNumberOfItems(numberOfItemsStr);
        
        if(this.numberOfItems == 0) {
            this.numberOfItems = 0;
            JOptionPane.showMessageDialog(
                    App.this
                    , "Por Favor ingrese un numero de elementos mayor a cero");
        }
        
        this.clearTableModel();
        this.isDatatablePresent = true;
        
        this.numberOfItems = 8;
        
        for(int i=1; i<=this.numberOfItems; i++) {
            this.tableModel.addColumn("I"+i);
        }
        
        this.data = new Integer[this.numberOfTransactions][this.numberOfItems];
        this.data = this.generateRandomTable(this.data);
        this.booleanData = this.getBooleanTable(data);
        
        for (Integer[] row : this.data) {
            this.tableModel.addRow(row);
        }
    }
    
    /**
     * 
     * CALCULA LA PROBABILIDAD DE UN ELEMENTO DADO A PARTIR DEL NUMERO TOTAL DE REGISTROS
     * , SE USA p COMO IDENTIFICADOR DEBIDO A QUE ASÍ SE REPRESENTA GENERALMENTE DICHA FUNCIÓN
     * 
     * @param x
     * @return 
     */
    public double p(int x) {
        double probability = 0.0;
        //Fue necesario castear ambos argumentos enteros a double para poder realizar la división
        probability = ((double)x)/((double)this.numberOfTransactions);
        return probability;
    }
    
    /**
     * CALCULA EL FACTOR DE DEPENDENCIA DE UN ELEMENTO
     * DE UNA TABLA DE CONTINGENCIA
     * 
     * @param dividend
     * @param divider
     * @return 
     */
    public double dependencyFactor(double dividend, double divider) {
        double df = 0.0;
        //Es necesario solo realizar la división cuando el dividendo es diferente de cero
        if(divider != 0) {
            df = dividend/divider;
        }
        
        DecimalFormat format = new DecimalFormat("#.####");
        String x  = format.format(df);
        //Se uso value of y no parseDouble debido a que lo unico que se desea es extraer el valor
        return Double.valueOf(x);
    }
    
    public Integer[][] refreshData() {
        Integer[][] refreshedData = new Integer[this.data.length][this.data[0].length];
        this.isDatatableCorrect = true;
        
        tableModelLoop:for(int i=0; i<data.length; i++) {
            for(int j=0; j<data[i].length; j++) {
                String text = this.tableModel.getValueAt(i, j).toString();
                text = text.replaceAll("[^0-9,]", "");
                if(text.isEmpty()) {
                    this.isDatatableCorrect = false;
                    break tableModelLoop;
                } else {
                    int number = Integer.parseInt(text);
                    if(number != 0 && number != 1) {
                        this.isDatatableCorrect = false;
                        break tableModelLoop;
                    } else {
                        refreshedData[i][j] = number;
                    }
                }
            }
        }
        return refreshedData;
    }
    
    /**
     * CALCULA TANTO LA TABLA DE DEPENDENCIA COMO LA TABLA DE FACTORES DE DEPENDENCIA
     * 
     */
    public void calculateTables() {
        System.out.println("*********************************************************");
        System.err.println("CALCULATING TABLES");
        System.out.println("*********************************************************");
        
        ArrayList<Integer> preffixes = null;
        ArrayList<Integer> suffixes = null;
        Boolean[] preffixesResults = null;
        Boolean[] suffixesResults = null;
        
        if(this.isDatatablePresent == true) {
            this.truePositive = 0;
            this.trueNegative = 0;
            this.falsePositive = 0;
            this.falseNegative = 0;
            
            //SE RECARGA LA MATRIZ DATA USANDO EL MÉTODO refreshData
            this.data = this.refreshData();
            
            //VALIDAR QUE LAS TABLAS SOLO TENGAN UNO Y CERO
            if(this.isDatatableCorrect) {
                preffixesResults = this.getIndexesResults(preffixes, this.rulePreffixTxt);
                suffixesResults = this.getIndexesResults(suffixes, this.ruleSuffixTxt);
            
                if(greatersThanNumberOfItems.size() > 0) {
                    this.correctArguments = false;
                } else {
                    this.correctArguments = true;
                }
            
                if(this.correctArguments != false) {
                    this.areTablesCalculated = true;
                    if(preffixesResults != null && suffixesResults != null) {
                        for(int i=0; i<booleanData.length;i++) {    
                            if(preffixesResults[i] == true && suffixesResults[i] == true) {
                                this.truePositive++;
                            } else if(preffixesResults[i] == true && suffixesResults[i] == false) {
                                this.trueNegative++;
                            } else if(preffixesResults[i] == false && suffixesResults[i] == true) {
                                this.falsePositive++;
                            } else if(preffixesResults[i] == false && suffixesResults[i] == false) {
                                this.falseNegative++;
                            }
                        }
                    }
                } else {
                    String message = "Existen argumentos erroneos en la Regla de Decisición: ";
                
                    for(Map.Entry entry : this.greatersThanNumberOfItems.entrySet()) {
                        message +=  entry.getKey() + " "; 
                        System.out.println("entry: " + entry.getKey());  
                    }
                
                    this.greatersThanNumberOfItems.clear();
                
                    JOptionPane.showMessageDialog(
                        App.this
                        , message);
                }    
            
                int totalTrue = this.truePositive + this.trueNegative;
                int totalFalse = this.falsePositive + this.falseNegative;
                int totalPositive = this.truePositive + this.falsePositive;
                int totalNegative = this.trueNegative + this.falseNegative;  
                
                double tpConfidence = this.calculateDivision(this.truePositive, (totalTrue + totalFalse));
                double tnConfidence = this.calculateDivision(this.trueNegative, (totalTrue + totalFalse));
                double fpConfidence = this.calculateDivision(this.falsePositive, (totalTrue + totalFalse));
                double fnConfidence = this.calculateDivision(this.falseNegative, (totalTrue + totalFalse));
                
                double tpCoberture = this.calculateDivision(this.truePositive, totalTrue);
                double tnCoberture = this.calculateDivision(this.trueNegative, totalTrue);
                double fpCoberture = this.calculateDivision(this.falsePositive, totalFalse);
                double fnCoberture = this.calculateDivision(this.falseNegative, totalFalse);
                
                double truePositiveDependencyFactor =
                    this.dependencyFactor(p(truePositive),(p(totalTrue)*p(totalPositive)));
                double trueNegativeDependencyFactor =
                    this.dependencyFactor(p(trueNegative),(p(totalTrue)*p(totalNegative)));
                double falsePositiveDependencyFactor =
                    this.dependencyFactor(p(falsePositive),(p(totalFalse)*p(totalPositive)));
                double falseNegativeDependencyFactor =
                    this.dependencyFactor(p(falseNegative),(p(totalFalse)*p(totalNegative)));
            
                this.truePositiveTxt.setText(Integer.toString(this.truePositive));
                this.trueNegativeTxt.setText(Integer.toString(this.trueNegative));
                this.falsePositiveTxt.setText(Integer.toString(this.falsePositive));
                this.falseNegativeTxt.setText(Integer.toString(this.falseNegative));
            
                this.totalTrueTxt.setText(Integer.toString(totalTrue));
                this.totalFalseTxt.setText(Integer.toString(totalFalse));
                this.totalPositiveTxt.setText(Integer.toString(totalPositive));
                this.totalNegativeTxt.setText(Integer.toString(totalNegative));
                this.totalTxt.setText(Integer.toString(totalTrue + totalFalse));
            
                this.tpConfidenceTxt.setText(new DecimalFormat("#.####").format(tpConfidence));
                this.tnConfidenceTxt.setText(new DecimalFormat("#.####").format(tnConfidence));
                this.fpConfidenceTxt.setText(new DecimalFormat("#.####").format(fpConfidence));
                this.fnConfidenceTxt.setText(new DecimalFormat("#.####").format(fnConfidence));
                
                this.tpCobertureTxt.setText(new DecimalFormat("#.####").format(tpCoberture));
                this.tnCobertureTxt.setText(new DecimalFormat("#.####").format(tnCoberture));
                this.fpCobertureTxt.setText(new DecimalFormat("#.####").format(fpCoberture));
                this.fnCobertureTxt.setText(new DecimalFormat("#.####").format(fnCoberture));
                
                this.dependencyFactorTPTxt.setText(new DecimalFormat("#.####").format(truePositiveDependencyFactor));
                this.dependencyFactorTNTxt.setText(new DecimalFormat("#.####").format(trueNegativeDependencyFactor));
                this.dependencyFactorFPTxt.setText(new DecimalFormat("#.####").format(falsePositiveDependencyFactor));
                this.dependencyFactorFNTxt.setText(new DecimalFormat("#.####").format(falseNegativeDependencyFactor));
                
                if(this.rulePreffixTxt.getText().isEmpty() && this.ruleSuffixTxt.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(
                        App.this
                        , "Por Favor ingrese una regla antecedente y una regla consecuente para poder calcular las tablas");
                } else if(this.rulePreffixTxt.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(
                        App.this
                        , "Por Favor ingrese una regla antecedente para poder calcular las tablas");
                } else if(this.ruleSuffixTxt.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(
                        App.this
                    ,   "Por Favor ingrese una regla consecuente para poder calcular las tablas");
                }
            } else {
                JOptionPane.showMessageDialog(
                    App.this
                    , "La tabla de datos es incorrecta, por favor asegurese que los valores ingresados sean solo ceros o unos");
                
                this.cleanCalculatedTables();
            }
        } else {
            JOptionPane.showMessageDialog(
                    App.this
                    , "Por Favor cargue un archivo o bien genere una tabla aleatoria para poder calcular las tablas");
        }
    }
    
    /**
     * VACÍA EL CONTENIDO DEL tableModel
     */
    private void clearTableModel() {
        this.tableModel.setRowCount(0);
        this.tableModel.setColumnCount(0);
    }
    
    public void cleanCalculatedTables() {
        this.truePositiveTxt.setText("");
        this.trueNegativeTxt.setText("");
        this.falsePositiveTxt.setText("");
        this.falseNegativeTxt.setText("");
        this.totalPositiveTxt.setText("");
        this.totalNegativeTxt.setText("");
        this.totalTrueTxt.setText("");
        this.totalFalseTxt.setText("");
        this.totalTxt.setText("");
        
        this.dependencyFactorTPTxt.setText("");
        this.dependencyFactorTNTxt.setText("");
        this.dependencyFactorFPTxt.setText("");
        this.dependencyFactorFNTxt.setText("");
    }
    
    /**
     * LIMPIA EL CONTENIDO DE LA JTable, LA TABLA DE CONTIGENCIA
     * Y DE LA TABLA DE FACTORES DE DEPENDENCIA
     */
    public void cleanTables() {
        System.out.println("*********************************************************");
        System.err.println("CLEANING TABLE");
        System.out.println("*********************************************************");
        
        this.clearTableModel();
        this.isDatatablePresent = false;
        this.areTablesCalculated = false;
        
        this.cleanCalculatedTables();
    }
    
    /**
     * 
     */
    public void loadFile() {
        System.out.println("*********************************************************");
        System.err.println("LOADING SHEET FROM FILE");
        System.out.println("*********************************************************");
        
        this.clearTableModel();
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.showOpenDialog(this);
        
        File file = fileChooser.getSelectedFile();
        XSSFWorkbook workbook;
        XSSFSheet sheet;
        int rows = 0;
        int columns = 0;
        
        if(file != null) {
            try {
                System.err.println("FILE LOADED: " + file.getCanonicalPath());
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            try {
                workbook = new XSSFWorkbook(new FileInputStream(file));
                sheet = workbook.getSheetAt(0);
                rows = sheet.getPhysicalNumberOfRows();
                columns = sheet.getRow(0).getPhysicalNumberOfCells();
                
                this.isDatatablePresent = true;
                
                System.out.println("NUMBER OF ROWS: " + rows);
                System.out.println("NUMBER OF COLUMNS: " + columns);
                
                this.numberOfItems = columns;
                this.numberOfTransactions = rows;
                
                if(this.firstRowAsTitle == true) {
                    for(int i=0; i<columns; i++) {
                        String columnName = sheet.getRow(0).getCell(i).toString();
                        this.tableModel.addColumn(columnName);
                    }
                    
                    //AL SER EL PRIMER ELEMENTO EL TITULO DE LA COLUMNA LA CARDINALIDAD EQUIVALE AL NUMERO DE REGISTROS - 1
                    this.data = new Integer[rows-1][columns];
                    
                    for(int i=0; i<data.length; i++) {
                        for(int j=0; j<data[i].length; j++) {
                            this.data[i][j] = (int) Double.parseDouble(sheet.getRow(i+1).getCell(j).toString());
                        }
                    }   
                    this.booleanData = this.getBooleanTable(data);
                    for (Integer[] row : this.data) {
                        this.tableModel.addRow(row);
                    }
                    this.dataTable.setEnabled(this.isTableEnabled);
                }        
            } catch (FileNotFoundException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * 
     * @param radios
     * @return 
     */
    public String getSignificanceLevel(ArrayList<javax.swing.JRadioButton> radios) {
        String significanceLevel = "";
        
        for(javax.swing.JRadioButton radio : radios) {
            if(radio.isSelected()){
                significanceLevel = radio.getText();
            }
        }
        return significanceLevel;
    }
    
    public double calculateDivision(double dividend, double divider) {
        double expected = 0.0;
        if(divider != (double)0) {
            expected = (double)dividend/(double)divider;
        }
        return expected;
    }
    
    /**
     * 
     * @return 
     */
    public double calculateChiQuare() {
        double chiSquare = 0.0;
        
        ArrayList<Integer> o = new ArrayList<>();
        ArrayList<Double> e = new ArrayList<>();
        
        o.add(this.truePositive);
        o.add(this.trueNegative);
        o.add(this.falsePositive);
        o.add(this.falseNegative);
        
        e.add(this.calculateDivision(
                    (Integer.parseInt(this.totalPositiveTxt.getText())
                            * Integer.parseInt(this.totalTrueTxt.getText()))
                    ,Integer.parseInt(this.totalTxt.getText())));
        
        e.add(this.calculateDivision(
                    (Integer.parseInt(this.totalNegativeTxt.getText())
                            * Integer.parseInt(this.totalTrueTxt.getText()))
                    ,Integer.parseInt(this.totalTxt.getText())));
        
        e.add(this.calculateDivision(
                    (Integer.parseInt(this.totalPositiveTxt.getText())
                            * Integer.parseInt(this.totalFalseTxt.getText()))
                    ,Integer.parseInt(this.totalTxt.getText())));
        
        e.add(this.calculateDivision(
                    (Integer.parseInt(this.totalNegativeTxt.getText())
                            * Integer.parseInt(this.totalFalseTxt.getText()))
                    ,Integer.parseInt(this.totalTxt.getText())));
        
        //un ciclo como es en la computación la implementación de la especificación matemática conocida como sumatoria
        for(int i=0; i<e.size(); i++) {
            chiSquare += this.calculateDivision(Math.pow(o.get(i) - e.get(i), 2), e.get(i));
        }
        return chiSquare;
    }
    
    /**
     * 
     */
    public void makeConclusion() {
        System.out.println("*********************************************************");
        System.err.println("CALCULATING CHI SQUARE");
        System.out.println("*********************************************************");
        
        String significanceLevelStr = this.getSignificanceLevel(significanceLevels);
        
        if(this.isDatatablePresent) {
            if(significanceLevelStr.isEmpty()) {
                JOptionPane.showMessageDialog(
                    App.this
                    , "Por Favor elige un nivel de significancia para poder realizar la prueba");
            } else {
                if(this.areTablesCalculated == true) {
                    double chiSquare = this.calculateChiQuare();
                    double significanceLevel = Double.parseDouble(significanceLevelStr);
                    double chiSquareTableResult = this.piValues.get(significanceLevel);
                
                    System.out.println("SIGNIFICANCE LEVEL: " + significanceLevelStr);
                    System.out.println("CHI-SQUARE RESULT: " + chiSquare);
                
                    this.conclusionTxt.setText(
                        "El valor calculado chi-cuadrada es " + new DecimalFormat("#.####").format(chiSquare)
                                + ", mientras que el valor chi-cuadrada obtenida de la tabla es " + chiSquareTableResult);
                
                    if(chiSquareTableResult <= chiSquare) {
                        this.conclusionTxt.append(
                            "\n Por lo tanto se rechaza la hipotesis nula y"
                                    + " se concluye que hay una asociación estadísticamente significativa entre las variables.");
                    } else {
                        this.conclusionTxt.append(
                            "\n Por lo tanto no se rechaza la hipotesis nula y"
                                    + " se declara que no hay suficiente evidencia para concluir que las variables están asociadas");
                    }
                } else {
                    JOptionPane.showMessageDialog(
                    App.this
                    , "Para realizar prueba chi-cuadrada es necesario calcular la Tabla de contingencia");
                }
            }
        } else {
            JOptionPane.showMessageDialog(
                    App.this
                    , "Para realizar la prueba es necesario"
                            + " \n 1. Cargar una tabla desde un archivo xlsx o bien generar una tabla aleatoria"
                            + " \n 2. Calcular Su tabla de contingencia por medio del boton calcular tablas");
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        chiSquareRadioGroup = new javax.swing.ButtonGroup();
        mainScroll = new javax.swing.JScrollPane();
        mainPanel = new javax.swing.JPanel();
        rightPanel = new javax.swing.JPanel();
        ruleSuffixScroll = new javax.swing.JScrollPane();
        ruleSuffixTxt = new javax.swing.JTextArea();
        ruleSuffixLbl = new javax.swing.JLabel();
        rulePreffixScroll = new javax.swing.JScrollPane();
        rulePreffixTxt = new javax.swing.JTextArea();
        rulePreffixLbl = new javax.swing.JLabel();
        implicationSymbolLbl = new javax.swing.JLabel();
        calculateTablesBtn = new javax.swing.JButton();
        DecisionRuleLbl = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        factorDependencycontingencyTableLbl = new javax.swing.JLabel();
        dependencyFactorTPTxt = new javax.swing.JTextField();
        dependencyFactorTNTxt = new javax.swing.JTextField();
        dependencyFactorFPTxt = new javax.swing.JTextField();
        dependencyFactorFNTxt = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        contigencyTableLbl = new javax.swing.JLabel();
        truePositiveTxt = new javax.swing.JTextField();
        trueNegativeTxt = new javax.swing.JTextField();
        falsePositiveTxt = new javax.swing.JTextField();
        falseNegativeTxt = new javax.swing.JTextField();
        totalTrueTxt = new javax.swing.JTextField();
        totalFalseTxt = new javax.swing.JTextField();
        totalPositiveTxt = new javax.swing.JTextField();
        totalNegativeTxt = new javax.swing.JTextField();
        totalTxt = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        tpConfidenceTxt = new javax.swing.JTextField();
        tnConfidenceTxt = new javax.swing.JTextField();
        fpConfidenceTxt = new javax.swing.JTextField();
        fnConfidenceTxt = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        tpCobertureTxt = new javax.swing.JTextField();
        tnCobertureTxt = new javax.swing.JTextField();
        fpCobertureTxt = new javax.swing.JTextField();
        fnCobertureTxt = new javax.swing.JTextField();
        leftPanel = new javax.swing.JPanel();
        loadFileBtn = new javax.swing.JButton();
        generateRandomTableBtn = new javax.swing.JButton();
        cleanTableBtn = new javax.swing.JButton();
        dataTableScroll = new javax.swing.JScrollPane();
        dataTable = new javax.swing.JTable();
        bottomPanel = new javax.swing.JPanel();
        calculateChiSquareBtn = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        significance001Radio = new javax.swing.JRadioButton();
        significance005Radio = new javax.swing.JRadioButton();
        significance01Radio = new javax.swing.JRadioButton();
        conclussionScroll = new javax.swing.JScrollPane();
        conclusionTxt = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        ruleSuffixTxt.setColumns(20);
        ruleSuffixTxt.setRows(5);
        ruleSuffixScroll.setViewportView(ruleSuffixTxt);

        ruleSuffixLbl.setText("Consecuente");

        rulePreffixTxt.setColumns(20);
        rulePreffixTxt.setRows(5);
        rulePreffixScroll.setViewportView(rulePreffixTxt);

        rulePreffixLbl.setText("Antecedente");

        implicationSymbolLbl.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        implicationSymbolLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        implicationSymbolLbl.setText("→");

        calculateTablesBtn.setText("Calcular Tablas");
        calculateTablesBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calculateTablesBtnActionPerformed(evt);
            }
        });

        DecisionRuleLbl.setText("Regla de Decisión");

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        factorDependencycontingencyTableLbl.setText(" Factores de Dependencia");

        dependencyFactorTPTxt.setEditable(false);

        dependencyFactorTNTxt.setEditable(false);

        dependencyFactorFPTxt.setEditable(false);

        dependencyFactorFNTxt.setEditable(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(dependencyFactorFPTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(dependencyFactorFNTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(dependencyFactorTPTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(dependencyFactorTNTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(factorDependencycontingencyTableLbl)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(factorDependencycontingencyTableLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dependencyFactorTPTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dependencyFactorTNTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dependencyFactorFPTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dependencyFactorFNTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        contigencyTableLbl.setText("Tabla de Contingencia");

        truePositiveTxt.setEditable(false);

        trueNegativeTxt.setEditable(false);

        falsePositiveTxt.setEditable(false);

        falseNegativeTxt.setEditable(false);

        totalTrueTxt.setEditable(false);

        totalFalseTxt.setEditable(false);

        totalPositiveTxt.setEditable(false);

        totalNegativeTxt.setEditable(false);

        totalTxt.setEditable(false);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(truePositiveTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(falsePositiveTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(totalPositiveTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(falseNegativeTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                            .addComponent(trueNegativeTxt)
                            .addComponent(totalNegativeTxt))
                        .addGap(26, 26, 26)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(totalTrueTxt, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                            .addComponent(totalFalseTxt, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(totalTxt, javax.swing.GroupLayout.Alignment.LEADING)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(80, 80, 80)
                        .addComponent(contigencyTableLbl)))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addComponent(contigencyTableLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(truePositiveTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(trueNegativeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(totalTrueTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(falsePositiveTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(falseNegativeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(totalFalseTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(totalPositiveTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(totalNegativeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(totalTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        contigencyTableLbl.getAccessibleContext().setAccessibleName("");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Confianza"));

        tpConfidenceTxt.setEditable(false);

        tnConfidenceTxt.setEditable(false);

        fpConfidenceTxt.setEditable(false);

        fnConfidenceTxt.setEditable(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fpConfidenceTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tpConfidenceTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, 28, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(fnConfidenceTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                    .addComponent(tnConfidenceTxt))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tpConfidenceTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tnConfidenceTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fpConfidenceTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fnConfidenceTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Cobertura"));

        tpCobertureTxt.setEditable(false);

        tnCobertureTxt.setEditable(false);

        fpCobertureTxt.setEditable(false);

        fnCobertureTxt.setEditable(false);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(fpCobertureTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                    .addComponent(tpCobertureTxt))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tnCobertureTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fnCobertureTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tpCobertureTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tnCobertureTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fpCobertureTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fnCobertureTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout rightPanelLayout = new javax.swing.GroupLayout(rightPanel);
        rightPanel.setLayout(rightPanelLayout);
        rightPanelLayout.setHorizontalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addGap(231, 231, 231)
                .addComponent(DecisionRuleLbl)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addComponent(rulePreffixScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(implicationSymbolLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(56, 56, 56)
                        .addComponent(ruleSuffixScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(191, 191, 191))
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(calculateTablesBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 681, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(rightPanelLayout.createSequentialGroup()
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(rightPanelLayout.createSequentialGroup()
                                .addComponent(rulePreffixLbl)
                                .addGap(285, 285, 285)
                                .addComponent(ruleSuffixLbl)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        rightPanelLayout.setVerticalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, rightPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(DecisionRuleLbl)
                .addGap(18, 18, 18)
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ruleSuffixLbl)
                    .addComponent(rulePreffixLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(implicationSymbolLbl)
                    .addComponent(rulePreffixScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ruleSuffixScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(calculateTablesBtn)
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        loadFileBtn.setText("Cargar Archivo");
        loadFileBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadFileBtnActionPerformed(evt);
            }
        });

        generateRandomTableBtn.setText("Generar Tabla Aleatoria");
        generateRandomTableBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateRandomTableBtnActionPerformed(evt);
            }
        });

        cleanTableBtn.setText("Limpiar Tabla");
        cleanTableBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cleanTableBtnActionPerformed(evt);
            }
        });

        dataTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        dataTableScroll.setViewportView(dataTable);

        javax.swing.GroupLayout leftPanelLayout = new javax.swing.GroupLayout(leftPanel);
        leftPanel.setLayout(leftPanelLayout);
        leftPanelLayout.setHorizontalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(dataTableScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(leftPanelLayout.createSequentialGroup()
                        .addComponent(loadFileBtn)
                        .addGap(18, 18, 18)
                        .addComponent(generateRandomTableBtn)
                        .addGap(18, 18, 18)
                        .addComponent(cleanTableBtn)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        leftPanelLayout.setVerticalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(loadFileBtn)
                    .addComponent(generateRandomTableBtn)
                    .addComponent(cleanTableBtn))
                .addGap(18, 18, 18)
                .addComponent(dataTableScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 296, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        loadFileBtn.getAccessibleContext().setAccessibleName("loadFileBtn");
        generateRandomTableBtn.getAccessibleContext().setAccessibleName("generateRandomTableBtn");
        cleanTableBtn.getAccessibleContext().setAccessibleName("cleanTableBtn");

        bottomPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        calculateChiSquareBtn.setText("Calcular χ²");
        calculateChiSquareBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calculateChiSquareBtnActionPerformed(evt);
            }
        });

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Nivel de Significancia"));

        significance001Radio.setText("0.01");

        significance005Radio.setText("0.05");

        significance01Radio.setText("0.1");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(significance001Radio)
                .addGap(18, 18, 18)
                .addComponent(significance005Radio)
                .addGap(18, 18, 18)
                .addComponent(significance01Radio)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(significance001Radio)
                    .addComponent(significance005Radio)
                    .addComponent(significance01Radio))
                .addContainerGap())
        );

        conclusionTxt.setColumns(20);
        conclusionTxt.setRows(5);
        conclussionScroll.setViewportView(conclusionTxt);

        javax.swing.GroupLayout bottomPanelLayout = new javax.swing.GroupLayout(bottomPanel);
        bottomPanel.setLayout(bottomPanelLayout);
        bottomPanelLayout.setHorizontalGroup(
            bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bottomPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(calculateChiSquareBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(conclussionScroll))
                .addContainerGap())
        );
        bottomPanelLayout.setVerticalGroup(
            bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, bottomPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(calculateChiSquareBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(conclussionScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(bottomPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(leftPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rightPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 721, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 10, Short.MAX_VALUE))))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rightPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(leftPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bottomPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        mainScroll.setViewportView(mainPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 1175, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainScroll)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void loadFileBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadFileBtnActionPerformed
        this.loadFile();
    }//GEN-LAST:event_loadFileBtnActionPerformed

    private void generateRandomTableBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateRandomTableBtnActionPerformed
        this.buidRandomTable();
    }//GEN-LAST:event_generateRandomTableBtnActionPerformed
    
    private void cleanTableBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cleanTableBtnActionPerformed
        this.cleanTables();
    }//GEN-LAST:event_cleanTableBtnActionPerformed

    private void calculateTablesBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calculateTablesBtnActionPerformed
        this.calculateTables();
    }//GEN-LAST:event_calculateTablesBtnActionPerformed

    private void calculateChiSquareBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calculateChiSquareBtnActionPerformed
        this.makeConclusion();
    }//GEN-LAST:event_calculateChiSquareBtnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(App.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new App().setVisible(true);
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel DecisionRuleLbl;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JButton calculateChiSquareBtn;
    private javax.swing.JButton calculateTablesBtn;
    private javax.swing.ButtonGroup chiSquareRadioGroup;
    private javax.swing.JButton cleanTableBtn;
    private javax.swing.JTextArea conclusionTxt;
    private javax.swing.JScrollPane conclussionScroll;
    private javax.swing.JLabel contigencyTableLbl;
    private javax.swing.JTable dataTable;
    private javax.swing.JScrollPane dataTableScroll;
    private javax.swing.JTextField dependencyFactorFNTxt;
    private javax.swing.JTextField dependencyFactorFPTxt;
    private javax.swing.JTextField dependencyFactorTNTxt;
    private javax.swing.JTextField dependencyFactorTPTxt;
    private javax.swing.JLabel factorDependencycontingencyTableLbl;
    private javax.swing.JTextField falseNegativeTxt;
    private javax.swing.JTextField falsePositiveTxt;
    private javax.swing.JTextField fnCobertureTxt;
    private javax.swing.JTextField fnConfidenceTxt;
    private javax.swing.JTextField fpCobertureTxt;
    private javax.swing.JTextField fpConfidenceTxt;
    private javax.swing.JButton generateRandomTableBtn;
    private javax.swing.JLabel implicationSymbolLbl;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JButton loadFileBtn;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JScrollPane mainScroll;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JLabel rulePreffixLbl;
    private javax.swing.JScrollPane rulePreffixScroll;
    private javax.swing.JTextArea rulePreffixTxt;
    private javax.swing.JLabel ruleSuffixLbl;
    private javax.swing.JScrollPane ruleSuffixScroll;
    private javax.swing.JTextArea ruleSuffixTxt;
    private javax.swing.JRadioButton significance001Radio;
    private javax.swing.JRadioButton significance005Radio;
    private javax.swing.JRadioButton significance01Radio;
    private javax.swing.JTextField tnCobertureTxt;
    private javax.swing.JTextField tnConfidenceTxt;
    private javax.swing.JTextField totalFalseTxt;
    private javax.swing.JTextField totalNegativeTxt;
    private javax.swing.JTextField totalPositiveTxt;
    private javax.swing.JTextField totalTrueTxt;
    private javax.swing.JTextField totalTxt;
    private javax.swing.JTextField tpCobertureTxt;
    private javax.swing.JTextField tpConfidenceTxt;
    private javax.swing.JTextField trueNegativeTxt;
    private javax.swing.JTextField truePositiveTxt;
    // End of variables declaration//GEN-END:variables
}