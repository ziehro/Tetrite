package com.ziehro.tetrite;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class LetterTetrisView extends View {

    private int blockSize;
    public static int boardWidth = 10;
    private int boardHeight = 16;
    private Paint paint;
    private Tetromino currentTetromino;
    private char[][] gameBoard;
    private Random random;
    Set<String> dictionary;
    private ScheduledExecutorService executorService;

    private Handler handler = new Handler(Looper.getMainLooper());
    private AtomicBoolean running = new AtomicBoolean(false);
    private static final String SHARED_PREFERENCES_NAME = "tetris_preferences";
    private static final String KEY_GAME_SPEED = "game_speed";
    private TrieNode dictionaryTrie = new TrieNode();
    AtomicBoolean isPaused = new AtomicBoolean(false);
    private Dialog progressDialog;
    private void showProgressDialog() {
        progressDialog = new Dialog(getContext());
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.show();
    }
    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        // Calculate blockSize to fit the screen
        blockSize = Math.min(width / boardWidth, height / boardHeight);
    }

    public LetterTetrisView(Context context) throws IOException {
        super(context);
        init();
        // Other initialization code here, if needed
    }

    public LetterTetrisView(Context context, AttributeSet attrs) throws IOException {
        super(context, attrs);
        init();
        paint = new Paint();
        gameBoard = new char[boardHeight][boardWidth];
        random = new Random();
        currentTetromino = createRandomTetromino();

        loadDictionary();
        startGame();

    }
    private void init() {

    }

    private Runnable gameLoop = () -> {
        if (!running.get()) return;
        updateGame();
        postInvalidate();
    };
    boolean gameOver = false;



    public void startGame() {

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        int gameSpeed = sharedPreferences.getInt(KEY_GAME_SPEED, 500); // Use a default value if no speed setting is stored

        executorService = Executors.newSingleThreadScheduledExecutor();
        running.set(true);
        executorService.scheduleAtFixedRate(gameLoop, 0, gameSpeed, TimeUnit.MILLISECONDS);
    }
    public void stopGame() {
        running.set(false);

        if (executorService != null) {
            executorService.shutdown();
            try {
                executorService.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public void resetGame() {
        // Clear the game board
        for (int i = 0; i < boardHeight; i++) {
            for (int j = 0; j < boardWidth; j++) {
                gameBoard[i][j] = 0;
            }
        }

        // Create a new random Tetromino and reset the game state
        currentTetromino = createRandomTetromino();

        // Restart the game loop
        if (!running.get()) {
            startGame();
        }

        // Redraw the game board
        invalidate();
    }
    public void pauseGame() {
        if (!isPaused.get()) {
            stopGame();
            isPaused.set(true);
        }
    }
    public void resumeGame() {
        if (isPaused.get()) {
            startGame();
            isPaused.set(false);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (gameOver) {
            //stopGame();
            char[][] board = gameBoard;
            Map<String, List<int[]>> foundWords = findWords(board, dictionary);
            Toast.makeText(getContext(),"Found: " + foundWords , Toast.LENGTH_SHORT ).show();
            highlightFoundWords(canvas, paint, foundWords);

            return;
        } else {
            for (int i = 0; i < boardHeight; i++) {
                for (int j = 0; j < boardWidth; j++) {
                    paint.setColor(Color.BLACK);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawRect(j * blockSize, i * blockSize, (j + 1) * blockSize, (i + 1) * blockSize, paint);
                    paint.setColor(Color.WHITE);
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawRect(j * blockSize, i * blockSize, (j + 1) * blockSize, (i + 1) * blockSize, paint);
                }
            }

            // Draw tetromino
            currentTetromino.draw(canvas, paint, blockSize);

            // Draw letters on the game board
            for (int i = 0; i < boardHeight; i++) {
                for (int j = 0; j < boardWidth; j++) {
                    if (gameBoard[i][j] != 0) {
                        paint.setColor(Color.WHITE);
                        paint.setTextSize(blockSize);

                        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
                        float textVerticalOffset = (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent;
                        canvas.drawText(String.valueOf(gameBoard[i][j]), j * blockSize + textVerticalOffset / 2, (i + 1) * blockSize - textVerticalOffset / 2, paint);
                    }
                }
            }
        }
    }

    public void highlightFoundWords(Canvas canvas, Paint paint, Map<String, List<int[]>> foundWords) {
        for (int y = 0; y < gameBoard.length; y++) {
            for (int x = 0; x < gameBoard[y].length; x++) {
                boolean isHighlighted = false;

                for (List<int[]> wordPositions : foundWords.values()) {
                    for (int[] pos : wordPositions) {
                        if (pos[0] == y && pos[1] == x) {
                            isHighlighted = true;
                            break;
                        }
                    }
                    if (isHighlighted) {
                        break;
                    }
                }

                if (isHighlighted) {
                    paint.setColor(Color.YELLOW);
                    //Toast.makeText(getContext(),"Blue? " + paint.getColor() , Toast.LENGTH_SHORT ).show();
                } else {
                    paint.setColor(Color.BLACK);
                }

                // Draw letter
                paint.setTextSize(blockSize);
                canvas.drawText(String.valueOf(gameBoard[y][x]), x * blockSize, (y + 1) * blockSize, paint);
            }
        }
    }


    private Tetromino createRandomTetromino() {
        // Create a random tetromino with random letter tiles

        Tetromino.Type[] tetrominoTypes = Tetromino.Type.values();
        Tetromino.Type randomType = tetrominoTypes[random.nextInt(tetrominoTypes.length)];
        char[][] shape = createShape(randomType);
        char[][] letterShape = new char[shape.length][shape[0].length];

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[0].length; j++) {
                if (shape[i][j] != '0') {
                    letterShape[i][j] = (char) ('A' + random.nextInt(26));
                } else {
                    letterShape[i][j] = 0;
                }
            }
        }
        return new Tetromino(randomType, letterShape);
    }


    private char[][] createShape(Tetromino.Type type) {
        switch (type) {
            case I:
                return new char[][]{
                        {'0', '0', '0', '0'},
                        {'I', 'I', 'I', 'I'},
                        {'0', '0', '0', '0'},
                        {'0', '0', '0', '0'}
                };
            case J:
                return new char[][]{
                        {'J', '0', '0'},
                        {'J', 'J', 'J'},
                        {'0', '0', '0'}
                };
            case L:
                return new char[][]{
                        {'0', '0', 'L'},
                        {'L', 'L', 'L'},
                        {'0', '0', '0'}
                };
            case O:
                return new char[][]{
                        {'O', 'O'},
                        {'O', 'O'}
                };
            case S:
                return new char[][]{
                        {'0', 'S', 'S'},
                        {'S', 'S', '0'},
                        {'0', '0', '0'}
                };
            case T:
                return new char[][]{
                        {'0', 'T', '0'},
                        {'T', 'T', 'T'},
                        {'0', '0', '0'}
                };
            case Z:
                return new char[][]{
                        {'Z', 'Z', '0'},
                        {'0', 'Z', 'Z'},
                        {'0', '0', '0'}
                };
            default:
                throw new IllegalArgumentException("Invalid tetromino type");
        }
    }

    public void move(int dx, int dy) {
        if (canMove(dx, dy)) {
            currentTetromino.position.x += dx;
            currentTetromino.position.y += dy;
            invalidate(); // Redraw the view
        }
    }

    private boolean canMove(int dx, int dy) {
        int newX, newY;

        for (int i = 0; i < currentTetromino.getShape().length; i++) {
            for (int j = 0; j < currentTetromino.getShape()[0].length; j++) {

                if (currentTetromino.getShape()[i][j] != 0) {
                    newX = currentTetromino.position.x + j + dx;
                    newY = currentTetromino.position.y + i + dy;

                    // Check for out-of-bounds or collision with existing blocks
                    if (newX < 0 || newX >= boardWidth || newY >= boardHeight || gameBoard[newY][newX] != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void rotate() {
        if (canRotate()) {
            char[][] rotatedShape = rotateMatrix(currentTetromino.getShape());
            currentTetromino.shape = rotatedShape;
            invalidate(); // Redraw the view
        }
    }

    private void clearFullRows() {
        for (int i = boardHeight - 1; i >= 0; ) {
            boolean isRowFull = true;
            for (int j = 0; j < boardWidth; j++) {
                if (gameBoard[i][j] == 0) {
                    isRowFull = false;
                    break;
                }
            }

            if (isRowFull) {
                for (int k = i; k > 0; k--) {
                    for (int j = 0; j < boardWidth; j++) {
                        gameBoard[k][j] = gameBoard[k - 1][j];
                    }
                }
                // Clear the top row
                for (int j = 0; j < boardWidth; j++) {
                    gameBoard[0][j] = 0;
                }
            } else {
                i--; // Move to the next row only if the current row is not full
            }
        }
    }


    private void mergeTetrominoToBoard() {
        for (int i = 0; i < currentTetromino.shape.length; i++) {
            for (int j = 0; j < currentTetromino.shape[0].length; j++) {
                if (currentTetromino.shape[i][j] != 0) {
                    int x = currentTetromino.position.x + j;
                    int y = currentTetromino.position.y + i;
                    paint.setColor(Color.WHITE);
                    gameBoard[y][x] = currentTetromino.shape[i][j];
                }
            }
        }
    }

    private boolean canRotate() {
        char[][] rotatedShape = rotateMatrix(currentTetromino.getShape());
        int newX, newY;
        for (int i = 0; i < rotatedShape.length; i++) {
            for (int j = 0; j < rotatedShape[0].length; j++) {
                if (rotatedShape[i][j] != 0) {
                    newX = currentTetromino.position.x + j;
                    newY = currentTetromino.position.y + i;

                    // Check for out-of-bounds or collision with existing blocks
                    if (newX < 0 || newX >= boardWidth || newY >= boardHeight || gameBoard[newY][newX] != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private char[][] rotateMatrix(char[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        char[][] rotated = new char[cols][rows];

        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                rotated[j][rows - 1 - i] = matrix[i][j];
            }
        }
        return rotated;
    }

    public Map<String, List<int[]>> findWords(char[][] board, Set<String> dictionary) {
        showProgressDialog();
        int rows = board.length;
        int cols = board[0].length;
        Map<String, List<int[]>> foundWords = new HashMap<>();
        boolean[][] visited = new boolean[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                findWordsInBoardHelper(board, visited, dictionary, "", i, j, foundWords);
            }
        }
        dismissProgressDialog();
        return foundWords;
    }


    private void updateGame() {

        if(!isGameOver()) {
        if (canMove(0, 1)) {
           move(0, 1);
          } else {
            mergeTetrominoToBoard();
            clearFullRows();
            currentTetromino = createRandomTetromino();
          }
         }else stopGame();

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopGame();
    }

    private boolean isGameOver() {
        Tetromino testTetromino = createRandomTetromino();
        Point spawnPosition = new Point(boardWidth / 2 - testTetromino.shape[0].length / 2, 0);

        for (int i = 0; i < testTetromino.shape.length; i++) {
            for (int j = 0; j < testTetromino.shape[i].length; j++) {
                if (testTetromino.shape[i][j] != 0) {
                    int x = spawnPosition.x + j;
                    int y = spawnPosition.y + i;

                    if (x < 0 || x >= boardWidth || y < 0 || y >= boardHeight) {
                        Log.d("GameOverCheck", "Here xy 0");
                        return true;
                    }

                    if (gameBoard[y][x] != 0) {
                        Log.d("GameOverCheck", "Here xy 1");
                        return true;
                    }
                }
            }
        }
        return false;
    }




    private void findWordsInBoardHelper(char[][] board, boolean[][] visited, Set<String> dictionary, String currentWord, int i, int j, Map<String, List<int[]>> foundWords) {

        if (i < 0 || i >= board.length || j < 0 || j >= board[0].length || visited[i][j]) {
            return;
        }

        currentWord += board[i][j];

        visited[i][j] = true;
        currentWord = currentWord.trim().replaceAll("\"", "").replaceAll("[^a-zA-Z]", "");
        if (dictionaryTrie.search(currentWord)) {
            Log.d("Char to Lowercase: ", currentWord );
            foundWords.putIfAbsent(currentWord, new ArrayList<>());
            foundWords.get(currentWord).add(new int[]{i, j});
        }

        int[] rowOffsets = {-1, 0, 1, -1, 1, -1, 0, 1};
        int[] colOffsets = {-1, -1, -1, 0, 0, 1, 1, 1};

        for (int k = 0; k < 8; k++) {
            findWordsInBoardHelper(board, visited, dictionary, currentWord, i + rowOffsets[k], j + colOffsets[k], foundWords);
        }

        visited[i][j] = false;
    }

    private boolean isPrefix(Set<String> dictionary, String prefix) {
        for (String word : dictionary) {
            if (word.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }



    private void loadDictionary() {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.dictionary2);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                // Trim, remove quotation marks, and keep only alphabetical characters
                String word = line.trim().replaceAll("\"", "").replaceAll("[^a-zA-Z]", "");
                dictionaryTrie.insert(word);
            }
            reader.close();
            Log.d("GameOverCheck", String.valueOf(dictionaryTrie));
        } catch (IOException e) {
            Log.e("Hereio", "Error loading dictionary", e);
        }
    }


}
