package sudokuApp;

import com.example.sudoku.R;
import android.graphics.Color;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    private static final Logger logger = AppLogger.getLogger(MainActivity.class);

    private final SudokuBoard sudoku = new SudokuBoard();
    private final TextView[][] cells = new TextView[9][9];

    private TextView tvStatus;
    private int selectedRow = -1;
    private int selectedCol = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logger.info("MainActivity started");

        tvStatus = findViewById(R.id.tvStatus);
        createGrid();
        createDigitButtons();

        findViewById(R.id.btnNew).setOnClickListener(v -> startNewGame());
        findViewById(R.id.btnCheck).setOnClickListener(v -> checkSolution());
        findViewById(R.id.btnSolve).setOnClickListener(v -> {
            sudoku.revealSolution();
            updateUI(false);
        });
        findViewById(R.id.btnLang).setOnClickListener(v -> toggleLanguage());

        // Після зміни мови або повороту екрана відновлюємо гру, а не генеруємо нову
        if (savedInstanceState != null) {
            sudoku.restore(savedInstanceState.getIntArray("game"));
            updateUI(false);
        } else {
            startNewGame();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray("game", sudoku.save());
    }

    private void createGrid() {
        GridLayout grid = findViewById(R.id.gridBoard);
        float density = getResources().getDisplayMetrics().density;
        int thin = Math.round(1 * density);   // лінія між клітинками
        int thick = Math.round(3 * density);  // лінія між блоками 3x3

        // Ширина екрана мінус padding 16dp з двох боків мінус сума маргінів рядка
        int marginsInRow = 3 * thick + 6 * thin + thick;
        int cellSize = (getResources().getDisplayMetrics().widthPixels
                - Math.round(32 * density) - marginsInRow) / 9;

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                TextView cell = new TextView(this);
                cell.setGravity(Gravity.CENTER);
                cell.setTextSize(18);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize;
                params.height = cellSize;
                // Межі між блоками 3x3
                params.setMargins(
                        c % 3 == 0 ? thick : thin,
                        r % 3 == 0 ? thick : thin,
                        c == 8 ? thick : 0,
                        r == 8 ? thick : 0
                );
                cell.setLayoutParams(params);

                final int row = r, col = c;
                cell.setOnClickListener(v -> {
                    selectedRow = row;
                    selectedCol = col;
                    updateUI(false);
                });

                cells[r][c] = cell;
                grid.addView(cell);
            }
        }
    }

    private void createDigitButtons() {
        LinearLayout layout = findViewById(R.id.layoutDigits);
        for (int i = 0; i <= 9; i++) {
            Button btn = new Button(this);
            btn.setText(i == 0 ? "X" : String.valueOf(i));
            btn.setPadding(0, 0, 0, 0);
            btn.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            final int digit = i;
            btn.setOnClickListener(v -> enterDigit(digit));
            layout.addView(btn);
        }
    }

    private void startNewGame() {
        sudoku.generate(40); // 40 порожніх клітинок
        selectedRow = -1;
        selectedCol = -1;
        tvStatus.setText(R.string.msg_start);
        tvStatus.setTextColor(Color.BLACK);
        updateUI(false);
    }

    private void enterDigit(int digit) {
        if (selectedRow == -1 || sudoku.fixed[selectedRow][selectedCol]) {
            return;
        }
        final int row = selectedRow, col = selectedCol;
        sudoku.board[row][col] = digit;
        logger.fine(() -> "Cell (" + row + "," + col + ") = " + digit);
        updateUI(false);
    }

    private void checkSolution() {
        boolean hasEmpty = false;
        boolean hasErrors = false;

        // Перевірка за правилами: розв'язок правильний, якщо все заповнено і немає повторів
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (sudoku.board[r][c] == 0) hasEmpty = true;
                else if (!sudoku.isCorrect(r, c)) hasErrors = true;
            }
        }

        updateUI(true);

        if (hasErrors) {
            tvStatus.setText(R.string.msg_errors);
            tvStatus.setTextColor(Color.RED);
            logger.warning("Solution check failed: mistakes found");
        } else if (hasEmpty) {
            tvStatus.setText(R.string.msg_incomplete);
            tvStatus.setTextColor(Color.DKGRAY);
        } else {
            tvStatus.setText(R.string.msg_win);
            tvStatus.setTextColor(Color.parseColor("#2E7D32"));
            logger.info("Player solved the puzzle!");
        }
    }

    private void updateUI(boolean highlightErrors) {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                TextView cell = cells[r][c];
                int val = sudoku.board[r][c];
                cell.setText(val == 0 ? "" : String.valueOf(val));

                boolean isFixed = sudoku.fixed[r][c];
                cell.setTypeface(null, isFixed ? Typeface.BOLD : Typeface.NORMAL);
                cell.setTextColor(isFixed ? Color.BLACK : Color.BLUE);

                if (highlightErrors && !isFixed && !sudoku.isCorrect(r, c)) {
                    cell.setBackgroundColor(Color.parseColor("#FFCDD2"));
                    cell.setTextColor(Color.RED);
                } else if (r == selectedRow && c == selectedCol) {
                    cell.setBackgroundColor(Color.parseColor("#BBDEFB"));
                } else {
                    cell.setBackgroundColor(Color.WHITE);
                }
            }
        }
    }

    private void toggleLanguage() {
        // Поточна мова інтерфейсу (працює і тоді, коли мову в застосунку ще не вибирали)
        String current = getResources().getConfiguration().getLocales().get(0).getLanguage();
        String next = current.equals("uk") ? "en" : "uk";
        logger.info("Switching language to: " + next);
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(next));
    }
}