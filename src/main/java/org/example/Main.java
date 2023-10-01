package org.example;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.measure.MeasuresRecorder;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Main {
    static int L = 9;
    public static void main(String[] args) throws CsvValidationException, IOException {

        SudokuRepository repo = readSudokuCsv("sudoku_cluewise.csv");


        List<Integer> sudokuDificultyToTest = new ArrayList<>();
        sudokuDificultyToTest.add(17);
        sudokuDificultyToTest.add(40);
        sudokuDificultyToTest.add(80);

        int numberOfProblemToTest = 60000;

        Map<Integer, List<MeasuresRecorder>> results = new HashMap();

        int counter = 0;

        for (Integer difficulty: sudokuDificultyToTest) {
            List<String> sudokuAsStringList = repo.getSudokus(difficulty, numberOfProblemToTest);
            List<MeasuresRecorder> measures = new ArrayList<>();

            for (String sudokuAsString: sudokuAsStringList) {
                measures.add(solve(sudokuAsString));

                counter++;

                //System.out.println(counter+"/"+numberOfProblemToTest * sudokuDificultyToTest.size());

            }
            results.put(difficulty, measures);
        }

        computeAndPrintMeans(results);

    }

    public static void computeAndPrintMeans(Map<Integer, List<MeasuresRecorder>> results) {

        System.out.println("################################################################################");
        System.out.println();

        for(Integer difficulty : results.keySet()){
            List<MeasuresRecorder> measures = results.get(difficulty);

            SummaryStatistics resolutionTimeStatsNano = new SummaryStatistics();
            SummaryStatistics resolutionTimeStats = new SummaryStatistics();
            SummaryStatistics nodeStats = new SummaryStatistics();
            SummaryStatistics backTrackStats = new SummaryStatistics();
            SummaryStatistics backJumpStats = new SummaryStatistics();
            SummaryStatistics failStats = new SummaryStatistics();
            SummaryStatistics restartStats = new SummaryStatistics();

            for (MeasuresRecorder measure: measures) {
                // In nanosecond
                resolutionTimeStatsNano.addValue( measure.getTimeCountInNanoSeconds());
                resolutionTimeStats.addValue( measure.getTimeCount());
                nodeStats.addValue( measure.getNodeCount() );
                backTrackStats.addValue( measure.getBackTrackCount());
                backJumpStats .addValue( measure.getBackjumpCount());
                failStats.addValue(measure.getFailCount());
                restartStats.addValue(measure.getRestartCount());
            }

            System.out.println("SUDOKU, "+difficulty+" clues");
            System.out.println("\t - Resolution Time Mean: "+resolutionTimeStatsNano.getMean() +" ns");
            System.out.println("\t - Resolution Time : "+resolutionTimeStats.getMean() +" s");
            System.out.println("\t\t - Mean : "+resolutionTimeStats.getMean() +" s");
            System.out.println("\t\t - Min : "+resolutionTimeStats.getMin() +" s");
            System.out.println("\t\t - Max : "+resolutionTimeStats.getMax() +" s");
            System.out.println("\t - Nodes : ");
            System.out.println("\t\t - Mean : "+nodeStats.getMean());
            System.out.println("\t\t - Min : "+nodeStats.getMin());
            System.out.println("\t\t - Max : "+nodeStats.getMax());
            System.out.println("\t - Backtracks : ");
            System.out.println("\t\t - Mean : "+backTrackStats.getMean());
            System.out.println("\t\t - Min : "+backTrackStats.getMin());
            System.out.println("\t\t - Max : "+backTrackStats.getMax());
            System.out.println("\t - Backjumps : ");
            System.out.println("\t\t - Mean : " + backJumpStats.getMean());
            System.out.println("\t\t - Min : " + backJumpStats.getMin());
            System.out.println("\t\t - Max : " + backJumpStats.getMax());
            System.out.println("\t - Fails : ");
            System.out.println("\t\t - Mean : "+failStats.getMean());
            System.out.println("\t\t - Min : "+failStats.getMin());
            System.out.println("\t\t - Max : "+failStats.getMax());
            System.out.println("\t - Restarts : ");
            System.out.println("\t\t - Mean : "+restartStats.getMean());
            System.out.println("\t\t - Min : "+restartStats.getMin());
            System.out.println("\t\t - Max : "+restartStats.getMax());

            System.out.println();
            System.out.println("################################################################################");
            System.out.println();

        }
    }


    public static MeasuresRecorder solve(String sudokuAsString) {
        Model model = new Model("Sudoku");

        IntVar[][] sudokuBoard = new IntVar[L][L];
        for (int i = 0; i<L; i++) {
            for(int j = 0; j<L; j++) {
                int cellValue = Character.getNumericValue(sudokuAsString.charAt(i*9+j));
                // Unknown value => we create a variable
                if(cellValue == 0){
                    sudokuBoard[i][j] = model.intVar("X_"+i+"_"+j,1,9);
                }
                // Knwon value => we create a constant
                else {
                    sudokuBoard[i][j] = model.intVar("X_"+i+"_"+j, cellValue);
                }
            }
        }

        
        // Constraints
        for (int i = 0; i < L; i++) {
            // Row
            model.allDifferent(getVarsOfRow(sudokuBoard, i)).post();
            // Columns
            model.allDifferent(getVarsOfColumn(sudokuBoard, i)).post();
            // Block
            model.allDifferent(getVarsOfBlock(sudokuBoard, i)).post();
        }

        //printSudokuBoard(sudokuBoard);

        model.getSolver().solve();
        return model.getSolver().getMeasures();

    }


    public static IntVar[] getVarsOfRow(IntVar[][] sudokuBoard , int row) {
        IntVar[] varsOfRow = new IntVar[L];

        for (int i = 0; i < L; i++) {
            varsOfRow[i] = sudokuBoard[row][i];
        }

        return varsOfRow;
    }

    public static IntVar[] getVarsOfColumn(IntVar[][] sudokuBoard ,int column) {
        IntVar[] varsOfColumn = new IntVar[L];

        for (int i = 0; i < L; i++) {
            varsOfColumn[i] = sudokuBoard[i][column];
        }

        return varsOfColumn;
    }

    public static IntVar[] getVarsOfBlock(IntVar[][] sudokuBoard ,int block) {
        IntVar[] varsOfBlock = new IntVar[L];

        int startColumnIndex    = (block % 3) * 3;
        int startRowIndex      = (block / 3) * 3;

        int varCount = 0;
        for (int j = startRowIndex; j < startRowIndex + 3; j++) {
            for (int i = startColumnIndex; i < startColumnIndex + 3 ; i++) {
                varsOfBlock[varCount] = sudokuBoard[j][i];
                varCount++;
            }
        }
        return varsOfBlock;
    }

    public static void printSudokuBoard(IntVar[][] sudokuBoard){
        for(int i = 0; i<L; i++) {
            if (i==3 || i==6){
                System.out.println("------+-------+------");
            }
            for (int j = 0; j < L; j++) {
                IntVar cell = sudokuBoard[i][j];

                if(j==3 || j == 6) {
                    System.out.print("| ");
                }

                if(cell.isInstantiated()) {
                    System.out.print(cell.getValue());
                } else {
                    System.out.print(" ");
                }
                System.out.print(" ");
            }
            System.out.println();
        }
    }

    public static SudokuRepository readSudokuCsv(String file) throws IOException, CsvValidationException {

        SudokuRepository repo = new SudokuRepository();

        FileReader fileReader = new FileReader(file);
        CSVReader csvReader = new CSVReaderBuilder(fileReader).withSkipLines(1).build();

        String[] nextRecord;

        while ((nextRecord = csvReader.readNext()) != null) {
            String sudoku = nextRecord[0];
            String nbClue = nextRecord[2] ;

            repo.add(Integer.parseInt(nbClue), sudoku);

            //System.out.println();
        }

        return repo;

    }

}
