import numpy as np
import string

def int2Str(a):
    l = [str(x) for x in range(0,10)]
    l.extend(list(string.ascii_uppercase))
    return l[a]

def str2Int(a):
    return int(a, 36)

def string2sudokuBoard(sudoku_string):
      size = int(len(sudoku_string) ** 0.5)
      sudoku_board = [[int(sudoku_string[i*size + j]) for j in range(size)] for i in range(size)]
      return sudoku_board

class Sudoku:
    
    def __init__(self, sudokuAsString):
        self.L = int(len(sudokuAsString) ** 0.5)
        self.sub_L = int(self.L ** 0.5)
        self.board = string2sudokuBoard(sudokuAsString)

        int2Str(2)

    def printBoard(self):
        for i in range(self.L):
            if i % self.sub_L == 0 and i != 0:
                print("-" * (self.L * 2 + self.sub_L ))
            for j in range(self.L):
                if j % self.sub_L == 0 and j != 0:
                    print("|", end=" ")
                if(self.board[i][j] == 0):  
                    print(" ", end=" ")
                else:
                    print(int2Str(self.board[i][j]), end=" ")
            print()

    def get_empty_cells(self):
        emptyCells = []

        for i in range(self.L):
            for j in range(self.L):
                if self.board[i][j] == 0:
                    emptyCells.append((i, j))
        return emptyCells
      
    def isValidMove(self, row, col, num, verbose=False):
        # Check if 'num' is not in the current row and column
        if num in self.board[col] or num in [self.board[i][row] for i in range(self.L)]:
            if verbose:
                print(str(num)+" not valid at ["+str(col+1)+","+str(row+1)+"] (row or column constraint)")
            return False

        # Check if 'num' is not in the current subgrid
        start_row, start_col = row - row % self.sub_L, col - col % self.sub_L
        for i in range(self.sub_L):
            for j in range(self.sub_L):
                if self.board[j + start_col][i + start_row] == num:
                    if verbose:
                        print(str(num)+" not valid at ["+str(col+1)+","+str(row+1)+"] (subgrid constraint)")
                    return False

        return True
    
    # Does the sudoku respect all the constraint ?
    def isValid(self):
        for i in range(self.L):
          for j in range(self.L):
              if self.board[j][i] != 0:
                  num = self.board[j][i]
                  self.board[j][i] = 0  # Temporarily remove the number for validation
                  if not self.isValidMove(i, j, num, verbose=True):
                      self.board[j][i] = num  # Restore the number
                      return False
                  self.board[j][i] = num  # Restore the number
        return True
    
    # Is the sudoku solved <=> All constraints respected && no 0 in the board
    def isSolved(self):
        for row in self.board:
            if 0 in row:
                return False

        return self.isValid()
    
    def getPossibleMoves(self):
        moves = {}
        for col in range(self.L):
            for row in range(self.L):
                if self.board[col][row] == 0:
                    moves[(col,row)] = []
                    for n in range(1,self.L + 1):
                        if self.isValidMove(row, col, n, verbose=False):
                            moves[(col,row)].append(n)
                else:
                    pass
        return moves
    
    def isThereAMove(self):
        moves = self.getPossibleMoves()
        if len(moves.keys) > 0:
            return True
        return False
    
    def doMove(self, row, col, number):
        if self.isValidMove(row, col, number, verbose=True):
            self.board[col][row] = number

    def doMove(self, coords, number):
        col, row = coords
        if self.isValidMove(row, col, number, verbose=True):
            self.board[col][row] = number

  