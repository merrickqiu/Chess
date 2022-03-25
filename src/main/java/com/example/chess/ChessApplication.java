package com.example.chess;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChessApplication extends Application {
    public final String PATH = "src/main/resources/com/example/chess/Chess_Images/";
    public final int SQUARE_LENGTH = 100;
    public final Paint DARK_COLOR = Color.DARKGOLDENROD;
    public final Paint LIGHT_COLOR = Color.WHEAT;
    public final Paint HIGHLIGHT_COLOR = Color.LIGHTGREEN;
    Map<Piece, String> imageNames = new HashMap<>() {{
        put(Piece.WhitePawn, "whitePawn.png");
        put(Piece.WhiteKnight, "whiteKnight.png");
        put(Piece.WhiteBishop, "whiteBishop.png");
        put(Piece.WhiteRook, "whiteRook.png");
        put(Piece.WhiteQueen, "whiteQueen.png");
        put(Piece.WhiteKing, "whiteKing.png");
        put(Piece.BlackPawn, "blackPawn.png");
        put(Piece.BlackKnight, "blackKnight.png");
        put(Piece.BlackBishop, "blackBishop.png");
        put(Piece.BlackRook, "blackRook.png");
        put(Piece.BlackQueen, "blackQueen.png");
        put(Piece.BlackKing, "blackKing.png");
    }};
    ImageView[][] imageArray = new ImageView[8][8];
    ImageView currentlyHeld;
    int startRow;
    int startColumn;




    @Override
    public void start(Stage stage) throws IOException {
        // Create Board
        Board board = new Board();
        Canvas checkeredCanvas = new Canvas(8* SQUARE_LENGTH, 8* SQUARE_LENGTH);
        Pane boardPane = new Pane(checkeredCanvas);

        GraphicsContext checkeredContext = checkeredCanvas.getGraphicsContext2D();
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                // Board Squares
                checkeredContext.setFill((i+j)%2 == 0 ? LIGHT_COLOR : DARK_COLOR);
                checkeredContext.fillRect(j* SQUARE_LENGTH, i* SQUARE_LENGTH, SQUARE_LENGTH, SQUARE_LENGTH);

                // Board Pieces
                Piece piece= board.boardArray[i][j];
                if (piece != Piece.Empty) {
                    ImageView image = generatePieceImageView(piece);
                    image.setX(j*SQUARE_LENGTH);
                    image.setY(i*SQUARE_LENGTH);
                    boardPane.getChildren().add(image);
                    imageArray[i][j] = image;
                }
            }
        }


        EventHandler<MouseEvent> dragDetectedHandler = event -> {
            int row = (int) event.getY() / SQUARE_LENGTH;
            int column = (int) event.getX() / SQUARE_LENGTH;
            currentlyHeld = imageArray[row][column];
            if (currentlyHeld == null) {
                return;
            }
            imageArray[row][column] = null;
            checkeredContext.setFill(HIGHLIGHT_COLOR);
            checkeredContext.fillRect(column* SQUARE_LENGTH, row* SQUARE_LENGTH, SQUARE_LENGTH, SQUARE_LENGTH);
            startRow = row;
            startColumn = column;
        };
        EventHandler<MouseEvent> dragHandler = event -> {
            if (currentlyHeld == null) {
                dragDetectedHandler.handle(event);
            }
            currentlyHeld.setX(event.getX()-SQUARE_LENGTH/2.0);
            currentlyHeld.setY(event.getY()-SQUARE_LENGTH/2.0);
        };
        EventHandler<MouseEvent> dropHandler = event -> {
            int row = (int) event.getY() / SQUARE_LENGTH;
            int column = (int) event.getX() / SQUARE_LENGTH;

            if (board.move(startRow, startColumn, row, column)) {
                //System.out.println("Successful Move");
                currentlyHeld.setX(column*SQUARE_LENGTH);
                currentlyHeld.setY(row*SQUARE_LENGTH);
                if (imageArray[row][column] != null) {
                    boardPane.getChildren().remove(imageArray[row][column]);
                }
                imageArray[row][column] = currentlyHeld;
                if (board.justCastled) {
                    int oldCol, newCol;
                    if (column == 6) {
                        oldCol = 7;
                        newCol = 5;
                    } else if (column == 2) {
                        oldCol = 0;
                        newCol = 3;
                    } else {
                        throw new RuntimeException("isCastle is not working");
                    }
                    ImageView rookImage = imageArray[row][oldCol];
                    rookImage.setX(newCol*SQUARE_LENGTH);
                    imageArray[row][oldCol] = null;
                    imageArray[row][newCol] = rookImage;
                }
            } else {
                System.out.println("Reset Move");
                currentlyHeld.setX(startColumn*SQUARE_LENGTH);
                currentlyHeld.setY(startRow*SQUARE_LENGTH);
                imageArray[startRow][startColumn] = currentlyHeld;
            }

            currentlyHeld = null;
            checkeredContext.setFill((startRow+startColumn)%2 == 0 ? LIGHT_COLOR : DARK_COLOR);
            checkeredContext.fillRect(startColumn* SQUARE_LENGTH, startRow* SQUARE_LENGTH, SQUARE_LENGTH, SQUARE_LENGTH);
        };
        EventHandler<MouseEvent> clickHandler = event -> {
            if (currentlyHeld == null) {
                dragDetectedHandler.handle(event); 
            } else {
                dropHandler.handle(event);
            }
        };
        MouseHandler mouseHandler = new MouseHandler(clickHandler, dragHandler, dropHandler);
        boardPane.addEventHandler(MouseEvent.ANY, mouseHandler);

        Group root = new Group();
        root.getChildren().add(boardPane);

        Scene scene = new Scene(root, 800, 800);
        stage.setTitle("Chess App");
        stage.setScene(scene);
        stage.show();
    }

    private ImageView generatePieceImageView(Piece piece) throws IOException {
        if (piece == Piece.Empty) {
            System.err.println("Generated empty image");
            return new ImageView();
        }
        FileInputStream imageStream = new FileInputStream(PATH + imageNames.get(piece));
        Image image = new Image(imageStream);
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        return imageView;
    }

    public static void main(String[] args) {
        launch();
    }
}