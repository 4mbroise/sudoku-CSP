package org.example;

import java.util.*;

public class SudokuRepositoory {
    private Map<Integer, List<String>> repository = new HashMap<Integer, List<String>>();
    private Random random = new Random();

    public void add(int clueNumber, String sudoku) {
        if(!this.repository.containsKey(clueNumber)){
            this.repository.put(clueNumber, new ArrayList<String>());
        }
        this.repository.get(clueNumber).add(sudoku);
    }

    public String getRandomSudoku(int clueNumber) {

        List<String> sudokuList = this.repository.get(clueNumber);

        return sudokuList.get( random.nextInt(sudokuList.size()) );
    }

}
