package com.ziehro.tetrite;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

public class Tetromino {
    public enum Type {
        I, J, L, O, S, T, Z
    }

    private Type type;
    char[][] shape;
    Point position;
    private static final char[][][] SHAPES = {
            {
                    {'I'},
                    {'I'},
                    {'I'},
                    {'I'},
            },
            {
                    {'O', 'O'},
                    {'O', 'O'},
            },
            {
                    {'T'},
                    {'T', 'T'},
                    {'T'},
            },
            {
                    {'L'},
                    {'L'},
                    {'L', 'L'},
            },
            {
                    {'J', 'J'},
                    {'J'},
                    {'J'},
            },
            {
                    {'S', 'S'},
                    {'S', 'S'},
            },
            {
                    {'Z'},
                    {'Z', 'Z'},
                    {'Z'},
            }
    };

    public Tetromino(Type type, char[][] shape) {
        this.type = type;
        this.shape = shape;
        this.position = new Point(LetterTetrisView.boardWidth / 2 - shape[0].length / 2, 0); // Default starting position, can be adjusted
    }

    public Type getType() {
        return type;
    }

    public char[][] getShape() {
        return shape;
    }

    public void draw(Canvas canvas, Paint paint, int blockSize) {

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    int x = (position.x + j) * blockSize;
                    int y = (position.y + i) * blockSize;
                    paint.setColor(Color.WHITE);
                    paint.setTextSize(blockSize);


                    canvas.drawText(String.valueOf(shape[i][j]), x, y + blockSize, paint);
                }
            }
        }
    }

    // Implement other methods such as rotation and collision detection
}

