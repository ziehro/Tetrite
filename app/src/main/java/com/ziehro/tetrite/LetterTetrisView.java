package com.ziehro.tetrite;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
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
    int maxLength = 10;
    private int boardWidth = 10;
    private int boardHeight = 8;
    private Paint paint;
    private Tetromino currentTetromino;
    private char[][] gameBoard;
    private Random random;
    private List<String> wordsToCheck;
    private static final long DROP_INTERVAL = 200; // Interval between drops in milliseconds
    private Handler gameHandler;
    //private Runnable gameLoop;
     Set<String> dictionary;
    private ScheduledExecutorService executorService;

    private Handler handler;
    private AtomicBoolean running = new AtomicBoolean(false);





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

        try {
            dictionary = loadDictionary();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception, e.g., show an error message to the user.
        }
        startGame();

    }
    private void init() {

    }

    private Runnable gameLoop = new Runnable() {
        @Override
        public void run() {
            if (!running.get()) return;

            updateGame();
            postInvalidate();
        }
    };


    public void startGame() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        running.set(true);
        executorService.scheduleAtFixedRate(gameLoop, 0, 100, TimeUnit.MILLISECONDS);
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





    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isGameOver()) {
            stopGame();
            char[][] board = gameBoard;
            //Toast.makeText(getContext(),"Hiyo: "  , Toast.LENGTH_SHORT ).show();
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
                    paint.setColor(Color.BLUE);
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
        int rows = board.length;
        int cols = board[0].length;
        Map<String, List<int[]>> foundWords = new HashMap<>();
        boolean[][] visited = new boolean[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                findWordsInBoardHelper(board, visited, dictionary, "", i, j, foundWords);
            }
        }

        return foundWords;
    }


    private void updateGame() {
        boolean gameOver = isGameOver();
        if(!gameOver) {
        if (canMove(0, 1)) {
           move(0, 1);
       } else {
            mergeTetrominoToBoard();
            clearFullRows();
         currentTetromino = createRandomTetromino();
      }
    }
        if (gameOver) {
            char[][] board = gameBoard;
            //Toast.makeText(getContext(),"Game Over " , Toast.LENGTH_SHORT ).show();
            Map<String, List<int[]>> foundWords = findWords(board, dictionary);
            stopGame();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopGame();
    }

    private boolean isGameOver() {
        Tetromino testTetromino = createRandomTetromino();
        Point spawnPosition = new Point(boardWidth / 2 - testTetromino.shape[0].length / 2, 0);

        // Check if there is enough space for the new Tetromino
        for (int i = 0; i < testTetromino.shape.length; i++) {
            for (int j = 0; j < testTetromino.shape[0].length; j++) {
                int x = spawnPosition.x + j;
                int y = spawnPosition.y + i;
                if (y < 0 || y >= gameBoard[0].length || x >= gameBoard.length || gameBoard[y][x] != 0) {
                    return true;
                }
                if (testTetromino.shape[i][j] != 0 && gameBoard[y][x] != 0) {
                    return true; // Game over, since there is no space for the new Tetromino
                }
            }
        }

        return false; // There is enough space for the new Tetromino, so the game is not over
    }


    private void findWordsInBoardHelper(char[][] board, boolean[][] visited, Set<String> dictionary, String currentWord, int i, int j, Map<String, List<int[]>> foundWords) {
        if (i < 0 || i >= board.length || j < 0 || j >= board[0].length || visited[i][j]) {
            return;
        }

        currentWord += board[i][j];
        if (!isPrefix(dictionary, currentWord)) {
            return;
        }

        visited[i][j] = true;

        if (dictionary.contains(currentWord)) {
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


    private Set<String> loadDictionary() throws IOException {
        Set<String> dict = new HashSet<>();
        Resources res = getResources();
        InputStream inputStream = res.openRawResource(R.raw.dictionary);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while ((line = reader.readLine()) != null) {
            dict.add(line.trim());
        }

        reader.close();
        return dict;
    }
}
