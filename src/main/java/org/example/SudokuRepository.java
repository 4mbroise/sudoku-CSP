package org.example;

import java.util.*;

public class SudokuRepository {

    private static final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private Map<Integer, List<List<Integer>>> repository = new HashMap<Integer, List<List<Integer>>>();
    private Random random = new Random();

    public void add(int clueNumber, String sudoku) {

        List<Integer> sudokuAsIntList = new ArrayList<>();

        for(char c:sudoku.toCharArray()){
            if(alphabet.contains(String.valueOf(c))){
                sudokuAsIntList.add(9+1+alphabet.indexOf(c));
            } else {
                sudokuAsIntList.add(Integer.parseInt(String.valueOf(c)));
            }
        }

        if(!this.repository.containsKey(clueNumber)){
            this.repository.put(clueNumber, new ArrayList<List<Integer>>());
        }

        this.repository.get(clueNumber).add(sudokuAsIntList);
    }

    public List<List<Integer>> getSudokus(int nbClues) {
        return this.repository.get(nbClues);
    }

    public List<List<Integer>> getSudokus(int nbClues, int nbSudoku) {
        return this.repository.get(nbClues).subList(0, nbSudoku);
    }

    public List<Integer> getRandomSudoku(int clueNumber) {

        List<List<Integer>> sudokuList = this.repository.get(clueNumber);

        return sudokuList.get( random.nextInt(sudokuList.size()) );
    }

    public Map<Integer, List<List<Integer>>> getRepository() {
        return repository;
    }

}
