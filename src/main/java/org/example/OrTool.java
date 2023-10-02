package org.example;

import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.Solver;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.IntVar;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OrTool {
    static int L = 9;
    public static void main(String[] args) throws CsvValidationException, IOException {

        Loader.loadNativeLibraries();
        SudokuRepository repo = readSudokuCsv("sudoku_cluewise.csv");


        List<Integer> sudokuDificultyToTest = new ArrayList<>();
        sudokuDificultyToTest.add(17);
        sudokuDificultyToTest.add(40);
        sudokuDificultyToTest.add(80);

        int numberOfProblemToTest = 1000;

        System.out.println(solve(repo.getRandomSudoku(17)).responseStats());


        Map<Integer, List<CpSolver>> results = new HashMap();

        int counter = 0;


        for (Integer difficulty: sudokuDificultyToTest) {
            List<String> sudokuAsStringList = repo.getSudokus(difficulty, numberOfProblemToTest);
            List<CpSolver> solvers = new ArrayList<>();

            for (String sudokuAsString: sudokuAsStringList) {
                solvers.add(solve(sudokuAsString));

                counter++;

                System.out.println(counter+"/"+numberOfProblemToTest * sudokuDificultyToTest.size());

            }
            results.put(difficulty, solvers);
        }

        computeAndPrintMeans(results);

    }



    public static void computeAndPrintMeans(Map<Integer, List<CpSolver>> results) {

        System.out.println("################################################################################");
        System.out.println();

        for (Integer difficulty : results.keySet()) {
            List<CpSolver> cpSolvers = results.get(difficulty);

            SummaryStatistics numConflicts = new SummaryStatistics();
            SummaryStatistics numBranches = new SummaryStatistics();
            SummaryStatistics NumBinaryPropagations = new SummaryStatistics();
            SummaryStatistics NumRestarts = new SummaryStatistics();
            SummaryStatistics userTime = new SummaryStatistics();
            SummaryStatistics wallTime = new SummaryStatistics();
            SummaryStatistics DeterministicTime = new SummaryStatistics();

            for (CpSolver cpSolver : cpSolvers) {
                // In nanosecond
                numConflicts.addValue(cpSolver.numConflicts());
                numBranches.addValue(cpSolver.numBranches());
                NumBinaryPropagations.addValue(cpSolver.response().getNumBinaryPropagations());
                NumRestarts.addValue(cpSolver.response().getNumRestarts());
                userTime.addValue(cpSolver.userTime());
                wallTime.addValue(cpSolver.wallTime());
                DeterministicTime.addValue(cpSolver.response().getDeterministicTime());
            }

            System.out.println("SUDOKU, " + difficulty + " clues");
            System.out.println("\t - Num Conflicts: ");
            System.out.println("\t\t - Mean : " + numConflicts.getMean());
            System.out.println("\t\t - Min : " + numConflicts.getMin());
            System.out.println("\t\t - Max : " + numConflicts.getMax());
            System.out.println("\t - Num Branches : ");
            System.out.println("\t\t - Mean : " + numBranches.getMean());
            System.out.println("\t\t - Min : " + numBranches.getMin());
            System.out.println("\t\t - Max : " + numBranches.getMax());
            System.out.println("\t - Num Binary Propagations : ");
            System.out.println("\t\t - Mean : " + NumBinaryPropagations.getMean());
            System.out.println("\t\t - Min : " + NumBinaryPropagations.getMin());
            System.out.println("\t\t - Max : " + NumBinaryPropagations.getMax());
            System.out.println("\t - Num Restarts : ");
            System.out.println("\t\t - Mean : " + NumRestarts.getMean());
            System.out.println("\t\t - Min : " + NumRestarts.getMin());
            System.out.println("\t\t - Max : " + NumRestarts.getMax());
            System.out.println("\t - User Time : ");
            System.out.println("\t\t - Mean : " + userTime.getMean());
            System.out.println("\t\t - Min : " + userTime.getMin());
            System.out.println("\t\t - Max : " + userTime.getMax());
            System.out.println("\t - Wall Time : ");
            System.out.println("\t\t - Mean : " + wallTime.getMean());
            System.out.println("\t\t - Min : " + wallTime.getMin());
            System.out.println("\t\t - Max : " + wallTime.getMax());
            System.out.println("\t - Deterministic Time : ");
            System.out.println("\t\t - Mean : " + DeterministicTime.getMean());
            System.out.println("\t\t - Min : " + DeterministicTime.getMin());
            System.out.println("\t\t - Max : " + DeterministicTime.getMax());

            System.out.println();
            System.out.println("################################################################################");
            System.out.println();

        }
    }




    public static CpSolver solve(String sudokuAsString) {
        CpModel model = new CpModel();

        IntVar[][] sudokuBoard = new IntVar[L][L];
        for (int i = 0; i<L; i++) {
            for(int j = 0; j<L; j++) {
                int cellValue = Character.getNumericValue(sudokuAsString.charAt(i*9+j));
                // Unknown value => we create a variable
                if(cellValue == 0){
                    sudokuBoard[i][j] = model.newIntVar(1L,9L, "X_"+i+"_"+j);
                }
                // Knwon value => we create a constant
                else {
                    sudokuBoard[i][j] = model.newConstant(cellValue);
                }
            }
        }

        /*
        printSudokuBoardInit(sudokuBoard);
        System.out.println();
         */

        
        // Constraints
        for (int i = 0; i < L; i++) {
            // Row
            model.addAllDifferent(getVarsOfRow(sudokuBoard, i));
            // Columns
            model.addAllDifferent(getVarsOfColumn(sudokuBoard, i));
            // Block
            model.addAllDifferent(getVarsOfBlock(sudokuBoard, i));
        }


        CpSolver solver = new CpSolver();

        solver.getParameters().setEnumerateAllSolutions(false);

        solver.getParameters().setNumSearchWorkers(1);
        solver.getParameters().setCpModelPresolve(false);

        CpSolverStatus status = solver.solve(model);


        if(!(status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE)){
            System.out.println("\t NO SOLUTION FOUND");
            System.out.println("\t\t "+sudokuAsString);
        } else {
            //printSudokuBoardSolution(sudokuBoard, solver);
        }



        return solver;

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

    public static void printSudokuBoardInit(IntVar[][] sudokuBoard){
        for(int i = 0; i<L; i++) {
            if (i==3 || i==6){
                System.out.println("------+-------+------");
            }
            for (int j = 0; j < L; j++) {
                IntVar cell = sudokuBoard[i][j];

                if(j==3 || j == 6) {
                    System.out.print("| ");
                }

                boolean isConstant = cell.displayBounds().length() <= 1;

                if(isConstant) {
                    System.out.print(cell.displayBounds());
                } else {
                    System.out.print(" ");
                }

                System.out.print(" ");
            }
            System.out.println();
        }
    }

    public static void printSudokuBoardSolution(IntVar[][] sudokuBoard, CpSolver solver){
        for(int i = 0; i<L; i++) {
            if (i==3 || i==6){
                System.out.println("------+-------+------");
            }
            for (int j = 0; j < L; j++) {
                IntVar cell = sudokuBoard[i][j];

                if(j==3 || j == 6) {
                    System.out.print("| ");
                }

                System.out.print(solver.value(cell));

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
