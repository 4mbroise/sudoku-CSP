package org.example;

import com.opencsv.exceptions.CsvValidationException;
import org.chocosolver.solver.search.measure.MeasuresRecorder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainChoco {
    public static void main(String[] args) throws CsvValidationException, IOException {

        ChocoLNS choco = new ChocoLNS();

        System.out.println("Reading sudokus");

        //SudokuRepository repo = readSudokuCsv("sudoku_cluewise.csv");
        SudokuRepository repo = choco.readHexaSudokuCsv("hexaSudoku.csv");
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
                measures.add(choco.solve(sudokuAsString, Integer.toString(counter)));

                counter++;

                if(counter % 500 == 0) {
                    System.out.println(counter+"/"+numberOfProblemToTest * sudokuDificultyToTest.size());
                }

            }
            results.put(difficulty, measures);
        }

        choco.computeAndPrintMeans(results);
    }
}
