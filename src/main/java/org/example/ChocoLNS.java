package org.example;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.loop.lns.INeighborFactory;
import org.chocosolver.solver.search.loop.lns.neighbors.INeighbor;
import org.chocosolver.solver.search.measure.MeasuresRecorder;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMax;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.DomOverWDeg;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.search.strategy.selectors.variables.Occurrence;
import org.chocosolver.solver.search.strategy.selectors.variables.Random;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.IntVar;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

public class ChocoLNS {
    static int L = 16;
    IntVar[] flatIntVarArray;

    public static void main(String[] args) throws CsvValidationException, IOException {

        System.out.println("Reading sudokus");

        //SudokuRepository repo = readSudokuCsv("sudoku_cluewise.csv");
        SudokuRepository repo = readHexaSudokuCsv("hexaSudoku.csv");
        //SudokuRepository repo = readHexaSudokuCsv("sudoku_n25.csv");

        repo.getRepository().keySet().forEach( (clues -> System.out.println(clues+" clues : "+(repo.getRepository().get(clues).size())+" sudokus")));

        //System.exit(0);


        System.out.println();
        System.out.println("#####################################################################################");
        System.out.println();


        List<Integer> sudokuDificultyToTest = new ArrayList<>();


        // 9x9
        //sudokuDificultyToTest.add(17);
        //sudokuDificultyToTest.add(40);
        //sudokuDificultyToTest.add(80);

        // 16x16
        sudokuDificultyToTest.add(91);

        // 25x25
        //sudokuDificultyToTest.add(268);

        int numberOfProblemToTest = 110;

        System.out.println("End reading sudokus, begining benchmark for "+sudokuDificultyToTest.size()+" difficulties ("+numberOfProblemToTest+" sudokus tested by difficulty)");
        System.out.println();

        Map<Integer, List<MeasuresRecorder>> results = new HashMap();

        int counter = 0;

        for (Integer difficulty: sudokuDificultyToTest) {
            List<List<Integer>> sudokuAsStringList = repo.getSudokus(difficulty, numberOfProblemToTest);
            List<MeasuresRecorder> measures = new ArrayList<>();

            for (List<Integer> sudokuAsString: sudokuAsStringList) {
                measures.add(solve(sudokuAsString, Integer.toString(counter)));

                counter++;

                if(counter % 500 == 0) {
                    System.out.println(counter+"/"+numberOfProblemToTest * sudokuDificultyToTest.size());
                }

            }
            results.put(difficulty, measures);
        }

        computeAndPrintMeans(results);

    }

    public static void computeAndPrintMeans(Map<Integer, List<MeasuresRecorder>> results) {

        System.out.println();
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
            System.out.println("\t - Resolution Time : "+resolutionTimeStatsNano.getMean() / (10E9) +" s");
            System.out.println("\t\t - Mean : "+resolutionTimeStatsNano.getMean() / (10E9) +" s");
            System.out.println("\t\t - Min : "+resolutionTimeStatsNano.getMin() / (10E9) +" s");
            System.out.println("\t\t - Max : "+resolutionTimeStatsNano.getMax() / (10E9) +" s");
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


    public static MeasuresRecorder solve(List<Integer> sudokuAsIntegerList, String id) {
        Model model = new Model(id);
        model.getSolver().hardReset();

        IntVar[][] sudokuBoard = new IntVar[L][L];

        IntVar[] flatIntVarArray = new IntVar[L*L];

        for (int i = 0; i<L; i++) {
            for(int j = 0; j<L; j++) {
                int cellValue = sudokuAsIntegerList.get(i*L+j);
                // Unknown value => we create a variable
                if(cellValue == 0){
                    sudokuBoard[i][j] = model.intVar("X_"+i+"_"+j,1,L);
                }
                // Knwon value => we create a constant
                else {
                    sudokuBoard[i][j] = model.intVar("X_"+i+"_"+j, cellValue);
                }
                flatIntVarArray[i*L+j] = sudokuBoard[i][j];
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

        ToDoubleFunction<IntVar> convertIntVarToDouble = (intVar) -> intVar.getValue() + 5d;
        ToIntFunction<IntVar> convertIntVarToInt = (intVar) -> intVar.getValue() + 5;

        //Search.

        //Search.intVarSearch(new ClausesBased(model, flatIntVarArray, convertIntVarToDouble, convertIntVarToInt),new IntDomainMin(),  flatIntVarArray)

        // Random
        IntStrategy Random_IntDomainMin = Search.intVarSearch(new Random<IntVar>(0), new IntDomainMin(), flatIntVarArray);
        IntStrategy Random_IntDomainMax = Search.intVarSearch(new Random<IntVar>(0), new IntDomainMax(), flatIntVarArray);
        IntStrategy Random_IntDomaiMiddle = Search.intVarSearch(new Random<IntVar>(0), new IntDomainMiddle(IntDomainMiddle.FLOOR), flatIntVarArray);
        IntStrategy Random_IntDomaiMedian= Search.intVarSearch(new Random<IntVar>(0), new IntDomainMiddle(IntDomainMiddle.FLOOR), flatIntVarArray);

        // First Fail
        IntStrategy FirstFail_IntDomainMin = Search.intVarSearch(new FirstFail(model), new IntDomainMin(), flatIntVarArray);
        IntStrategy FirstFail_IntDomainMax = Search.intVarSearch(new FirstFail(model), new IntDomainMax(), flatIntVarArray);
        IntStrategy FirstFail_IntDomaiMiddle = Search.intVarSearch(new FirstFail(model), new IntDomainMiddle(IntDomainMiddle.FLOOR), flatIntVarArray);
        IntStrategy FirstFail_IntDomaiMedian= Search.intVarSearch(new FirstFail(model), new IntDomainMiddle(IntDomainMiddle.FLOOR), flatIntVarArray);

        // Occurence
        IntStrategy Occurence_IntDomainMin = Search.intVarSearch(new Occurrence<IntVar>(), new IntDomainMin(), flatIntVarArray);
        IntStrategy Occurence_IntDomainMax = Search.intVarSearch(new Occurrence<IntVar>(), new IntDomainMax(), flatIntVarArray);
        IntStrategy Occurence_IntDomaiMiddle = Search.intVarSearch(new Occurrence<IntVar>(), new IntDomainMiddle(IntDomainMiddle.FLOOR), flatIntVarArray);
        IntStrategy Occurence_IntDomaiMedian= Search.intVarSearch(new Occurrence<IntVar>(), new IntDomainMiddle(IntDomainMiddle.FLOOR), flatIntVarArray);

        // DomOverWDeg
        IntStrategy DomOverWDeg_IntDomainMin = Search.intVarSearch(new DomOverWDeg<IntVar>(flatIntVarArray,0), new IntDomainMin(), flatIntVarArray);
        IntStrategy DomOverWDeg_IntDomainMax = Search.intVarSearch(new DomOverWDeg<IntVar>(flatIntVarArray,0), new IntDomainMax(), flatIntVarArray);
        IntStrategy DomOverWDeg_IntDomaiMiddle = Search.intVarSearch(new DomOverWDeg<IntVar>(flatIntVarArray,0), new IntDomainMiddle(IntDomainMiddle.FLOOR), flatIntVarArray);
        IntStrategy DomOverWDeg_IntDomaiMedian= Search.intVarSearch(new DomOverWDeg<IntVar>(flatIntVarArray,0), new IntDomainMiddle(IntDomainMiddle.FLOOR), flatIntVarArray);


        model.getSolver().setSearch(
                Occurence_IntDomaiMedian
        );

        //System.out.println("TIME : "+ Calendar.getInstance().getTimeInMillis());

        //System.out.println("Solving");

        //model.getSolver().setLNS(INeighborFactory.random(flatIntVarArray), new FailCounter(model.getSolver(), 100));

        model.getSolver().solve();
        model.getSolver().getMeasures().stopStopwatch();
        //System.out.println("TIME : "+ Calendar.getInstance().getTimeInMillis());


        /*
        for (IntVar[] intVars : sudokuBoard) {
            for (IntVar intVar : intVars) {
                System.out.println(intVar);
            }
        }
         */

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

        int squaredL = (int) Math.sqrt(L);

        IntVar[] varsOfBlock = new IntVar[L];

        int startColumnIndex    = (block % squaredL) * squaredL;
        int startRowIndex      = (block / squaredL) * squaredL;

        int varCount = 0;
        for (int j = startRowIndex; j < startRowIndex + squaredL; j++) {
            for (int i = startColumnIndex; i < startColumnIndex + squaredL ; i++) {
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

    public static SudokuRepository readHexaSudokuCsv(String file) throws IOException, CsvValidationException {

        SudokuRepository repo = new SudokuRepository();

        FileReader fileReader = new FileReader(file);
        CSVReader csvReader = new CSVReaderBuilder(fileReader).withSkipLines(1).build();

        String[] nextRecord;

        while ((nextRecord = csvReader.readNext()) != null) {
            String sudoku = nextRecord[0];
            String nbClue = String.valueOf((L*L) - ((int) sudoku.codePoints().filter(ch -> ch == '0').count()));
            repo.add(Integer.parseInt(nbClue), sudoku);

            //System.out.println();
        }

        return repo;

    }

}
