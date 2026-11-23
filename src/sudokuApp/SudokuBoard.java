package sudokuApp;

import com.example.sudoku.BuildConfig;
import android.util.Log;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class SudokuBoard {

    private static final Logger logger = AppLogger.getLogger(SudokuBoard.class);

    public final int[][] board = new int[9][9];
    public final int[][] solution = new int[9][9];
    public final boolean[][] fixed = new boolean[9][9];

    public void generate(int emptyCells) {
        logger.info("Generating new Sudoku board...");
        for (int i = 0; i < 9; i++) {
            Arrays.fill(board[i], 0);
            Arrays.fill(fixed[i], false);
        }

        // 1. Заповнюємо діагональні блоки 3x3 випадковими числами
        for (int i = 0; i < 9; i += 3) {
            fillBox(i, i);
        }

        // 2. Розв'язуємо решту сітки та зберігаємо правильну відповідь
        solve(0, 0);
        for (int r = 0; r < 9; r++) {
            System.arraycopy(board[r], 0, solution[r], 0, 9);
        }

        // 3. Видаляємо вказану кількість клітинок
        Random random = new Random();
        int removed = 0;
        while (removed < emptyCells) {
            int r = random.nextInt(9);
            int c = random.nextInt(9);
            if (board[r][c] != 0) {
                board[r][c] = 0;
                removed++;
            }
        }

        // 4. Фіксуємо початкові підказки
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                fixed[r][c] = (board[r][c] != 0);
            }
        }
        logger.info("Board generated successfully");
    }

    private void fillBox(int startRow, int startCol) {
        List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Collections.shuffle(nums);
        int idx = 0;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                board[startRow + r][startCol + c] = nums.get(idx++);
            }
        }
    }

    private boolean solve(int row, int col) {
        if (row == 9) return true;
        int nextRow = (col == 8) ? row + 1 : row;
        int nextCol = (col + 1) % 9;

        if (board[row][col] != 0) {
            return solve(nextRow, nextCol);
        }

        for (int num = 1; num <= 9; num++) {
            if (isValid(row, col, num)) {
                board[row][col] = num;
                if (solve(nextRow, nextCol)) return true;
                board[row][col] = 0;
            }
        }
        return false;
    }

    private boolean isValid(int row, int col, int num) {
        for (int i = 0; i < 9; i++) {
            if (board[row][i] == num || board[i][col] == num) return false;
        }
        int boxRow = row - row % 3;
        int boxCol = col - col % 3;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[boxRow + r][boxCol + c] == num) return false;
            }
        }
        return true;
    }

    /** true, якщо цифра в (r, c) не повторюється в рядку, стовпці та блоці 3x3 (порожня клітинка - true). */
    public boolean isCorrect(int r, int c) {
        int v = board[r][c];
        if (v == 0) return true;
        for (int i = 0; i < 9; i++) {
            if (i != c && board[r][i] == v) return false;
            if (i != r && board[i][c] == v) return false;
        }
        int br = r - r % 3, bc = c - c % 3;
        for (int i = br; i < br + 3; i++) {
            for (int j = bc; j < bc + 3; j++) {
                if ((i != r || j != c) && board[i][j] == v) return false;
            }
        }
        return true;
    }

    public void revealSolution() {
        for (int r = 0; r < 9; r++) {
            System.arraycopy(solution[r], 0, board[r], 0, 9);
        }
        logger.info("Solution revealed");
    }

    // 81 значення board + 81 solution + 81 fixed
    public int[] save() {
        int[] data = new int[243];
        for (int i = 0; i < 81; i++) {
            int r = i / 9, c = i % 9;
            data[i] = board[r][c];
            data[81 + i] = solution[r][c];
            data[162 + i] = fixed[r][c] ? 1 : 0;
        }
        return data;
    }

    public void restore(int[] data) {
        for (int i = 0; i < 81; i++) {
            int r = i / 9, c = i % 9;
            board[r][c] = data[i];
            solution[r][c] = data[81 + i];
            fixed[r][c] = data[162 + i] == 1;
        }
    }
}