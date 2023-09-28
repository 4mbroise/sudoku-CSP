package org.example;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class Main {
    static int L = 9;
    public static void main(String[] args) throws CsvValidationException, IOException {

        SudokuRepository repo = readSudoKuCsv("sudoku_cluewise.csv");

        solve(repo.getRandomSudoku(17));

        System.out.println("END");
    }


    public static  void solve(String sudokuAsString) {
        Model model = new Model("Sudoku");

        IntVar[][] sudokuBoard = new IntVar[L][L];
        for (int i = 0; i<L; i++) {
            if (i==3 || i==6){
                System.out.println("------+-------+------");
            }
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

                if(j==3 || j == 6) {
                    System.out.print("| ");
                }
                if(cellValue != 0){
                    System.out.print(cellValue);
                } else{
                    System.out.print(" ");
                }

                System.out.print(" ");
            }
            System.out.println();
        }

        System.out.println(sudokuBoard[0][0]);

    }

    public static SudokuRepository readSudoKuCsv(String file) throws IOException, CsvValidationException {

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
