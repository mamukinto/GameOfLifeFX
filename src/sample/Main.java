package sample;

import javafx.application.Application;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class Main extends Application {

    private static double WIDTH = 1000;
    private static double HEIGHT = 1000;
    private static double CELL_SIZE = 100;
    private static List<Rectangle> filledCells = new ArrayList<>();
    private static List<Rectangle> cells = new ArrayList<>();

    private static final Canvas canvas = new Canvas(2500, 2500);
    private static final GraphicsContext g = canvas.getGraphicsContext2D();


    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setScene(scene);
        stage.show();
        initGrid(WIDTH, HEIGHT, CELL_SIZE);

        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            WIDTH = newVal.doubleValue();
            initGrid(WIDTH, HEIGHT, CELL_SIZE);
        });

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            HEIGHT = newVal.doubleValue();
            initGrid(WIDTH, HEIGHT, CELL_SIZE);
        });

        scene.setOnKeyPressed(press -> {
            if (press.getCode().equals(KeyCode.UP)) {
                CELL_SIZE += 10;
                initGrid(WIDTH, HEIGHT, CELL_SIZE);
            }
            if (press.getCode().equals(KeyCode.DOWN)) {
                CELL_SIZE -= 10;
                initGrid(WIDTH, HEIGHT, CELL_SIZE);
            }
            if (press.getCode().equals(KeyCode.SPACE)) {
                startGame();
            }
            if (press.getCode().equals(KeyCode.X)) {
                showNeighbours();
            }
        });

        scene.setOnMouseClicked(click -> {
            if (click.getButton() == MouseButton.PRIMARY) {
                paintCell(click.getX(), click.getY());
            } else {
                removeCell(click.getX(), click.getY());
            }
        });
    }

    private static void showNeighbours() {
        cells.forEach(cell -> {
            g.setFont(new Font("Arial", 50));
            g.setTextAlign(TextAlignment.CENTER);
            g.setTextBaseline(VPos.CENTER);
            if (isFilled(cell)) {
                g.setFill(Color.web("white"));
            }
            g.fillText(
                    String.valueOf(countCellsAround(cell)),
                    Math.round(cell.getX() + CELL_SIZE / 2),
                    Math.round(cell.getY() + CELL_SIZE / 2)
            );
            g.setFill(Color.web("black"));
        });
    }

    private static boolean isFilled(Rectangle cell) {
        boolean isFilled = false;
        for (Rectangle filledCell : filledCells) {
            if (cell.getX() == filledCell.getX() && cell.getY() == filledCell.getY()) {
                isFilled = true;
            }
        }
        return isFilled;
    }

    private static void paintCell(double x, double y) {
        boolean duplicate = false;

        for (Rectangle filledCell : filledCells) {
            if (filledCell.getX() == x && filledCell.getY() == y) {
                duplicate = true;
            }
        }

        if (!duplicate) {
            int cellX = (int) (x / CELL_SIZE);
            int cellY = (int) (y / CELL_SIZE);
            cellX = (int) (cellX * CELL_SIZE);
            cellY = (int) (cellY * CELL_SIZE);
            g.setFill(Color.web("black"));
            g.fillRect(cellX, cellY, CELL_SIZE, CELL_SIZE);
            filledCells.add(new Rectangle(cellX, cellY, CELL_SIZE, CELL_SIZE));
        }
    }

    private static void removeCell(double x, double y) {
        int cellX = (int) (x / CELL_SIZE);
        int cellY = (int) (y / CELL_SIZE);
        cellX = (int) (cellX * CELL_SIZE);
        cellY = (int) (cellY * CELL_SIZE);
        g.setFill(Color.web("white"));
        g.fillRect(cellX, cellY, CELL_SIZE, CELL_SIZE);
        g.strokeRect(cellX, cellY, CELL_SIZE, CELL_SIZE);

        Rectangle toRemove = null;
        for (Rectangle filledCell : filledCells) {
            if (filledCell.getX() == cellX && filledCell.getY() == cellY) {
                toRemove = filledCell;
            }
        }
        filledCells.remove(toRemove);
    }


    private static void initGrid(double w, double h, double sz) {
        g.clearRect(0, 0, w, h);
        filledCells.clear();
        cells.clear();
        for (int i = 0; i < w; i += CELL_SIZE) {
            for (int j = 0; j < h; j += CELL_SIZE) {
                g.strokeRect(i, j, CELL_SIZE, CELL_SIZE);
                cells.add(new Rectangle(i, j, CELL_SIZE, CELL_SIZE));
            }
        }
    }


    private static void startGame() {
        class Refresh extends TimerTask {
            public void run() {
                cells.forEach(cell -> {
                    System.out.println(countCellsAround(cell));
                    if (countCellsAround(cell) == 3) {
                        paintCell(cell.getX(),cell.getY());
                        g.setFill(Color.web("green"));
                        g.fillRect(cell.getX(),cell.getY(),CELL_SIZE,CELL_SIZE);
                        g.setFill(Color.web("black"));
                    }
                    else if (countCellsAround(cell) < 2 || countCellsAround(cell) > 3) {
                        removeCell(cell.getX(),cell.getY());
                    }
                });
            }
        }
        Timer timer = new Timer();
        TimerTask refresh = new Refresh();
        timer.schedule(refresh, 0, 2500);
    }

    private static int countCellsAround(Rectangle cell) {
        int count = 0;
        for (Rectangle filledCell : filledCells) {
            if (cell.getX() + CELL_SIZE == filledCell.getX() && cell.getY() == filledCell.getY()) {
                count++; // right
            }
            if (cell.getX() - CELL_SIZE == filledCell.getX() && cell.getY() == filledCell.getY()) {
                count++; // left
            }
            if (cell.getY() + CELL_SIZE == filledCell.getY() && cell.getX() == filledCell.getX()) {
                count++; // bottom
            }
            if (cell.getY() - CELL_SIZE == filledCell.getY() && cell.getX() == filledCell.getX()) {
                count++; // top
            }


            if (cell.getY() + CELL_SIZE == filledCell.getY() && cell.getX() + CELL_SIZE == filledCell.getX()) {
                count++; // right bottom
            }
            if (cell.getY() - CELL_SIZE == filledCell.getY() && cell.getX() + CELL_SIZE == filledCell.getX()) {
                count++; // right top
            }
            if (cell.getY() + CELL_SIZE == filledCell.getY() && cell.getX() - CELL_SIZE == filledCell.getX()) {
                count++; // left bottom
            }
            if (cell.getY() - CELL_SIZE == filledCell.getY() && cell.getX() - CELL_SIZE == filledCell.getX()) {
                count++; // left top
            }

        }
        return count;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
