from typing import List
class Solution:
    def isValidSudoku(self, board: List[List[str]]) -> bool:
        row_set = [set() for _ in range(9)]
        col_set = [set() for _ in range(9)]
        box_set = [set() for _ in range(9)]

        for i in range(9):
            for j in range(9):
                num = board[i][j]
                if num == '.':
                    continue
                box_num = (i//3)*3 + (j//3)
                if num in row_set[i] or num in col_set[j] or num in box_set[box_num]:
                    return False
                
                row_set[i].add(num)
                col_set[j].add(num)
                box_set[box_num].add(num)

        return True