package org.example;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.FileReader;

public class Main {
    public static void main(String[] args) {

        readSudoKuCsv("sudoku_cluewise.csv");

        System.out.println("Hello world!");
    }


    public static SudokuRepositoory readSudoKuCsv(String file) {
        try {

            SudokuRepositoory repo = new SudokuRepositoory();

            FileReader fileReader = new FileReader(file);
            CSVReader csvReader = new CSVReaderBuilder(fileReader).withSkipLines(1).build();

            String[] nextRecord;

            while ((nextRecord = csvReader.readNext()) != null) {
                for (String cell : nextRecord) {
                    System.out.print(cell + "\t");
                }

                String sudoku = nextRecord[0];
                String nbClue = nextRecord[2] ;

                repo.add(Integer.parseInt(nbClue), sudoku);

                System.out.println();
                break;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
