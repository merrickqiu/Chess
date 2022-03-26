package com.example.chess;

import javafx.util.Pair;

import java.util.*;

public class Board {
    Piece[][] boardArray = {
            {Piece.BlackRook, Piece.BlackKnight, Piece.BlackBishop, Piece.BlackQueen, Piece.BlackKing, Piece.BlackBishop, Piece.BlackKnight, Piece.BlackRook},
            {Piece.BlackPawn, Piece.BlackPawn,   Piece.BlackPawn,   Piece.BlackPawn,  Piece.BlackPawn, Piece.BlackPawn,   Piece.BlackPawn,   Piece.BlackPawn},
            {Piece.Empty,     Piece.Empty,       Piece.Empty,       Piece.Empty,      Piece.Empty,     Piece.Empty,       Piece.Empty,       Piece.Empty},
            {Piece.Empty,     Piece.Empty,       Piece.Empty,       Piece.Empty,      Piece.Empty,     Piece.Empty,       Piece.Empty,       Piece.Empty},
            {Piece.Empty,     Piece.Empty,       Piece.Empty,       Piece.Empty,      Piece.Empty,     Piece.Empty,       Piece.Empty,       Piece.Empty},
            {Piece.Empty,     Piece.Empty,       Piece.Empty,       Piece.Empty,      Piece.Empty,     Piece.Empty,       Piece.Empty,       Piece.Empty},
            {Piece.WhitePawn, Piece.WhitePawn,   Piece.WhitePawn,   Piece.WhitePawn,  Piece.WhitePawn, Piece.WhitePawn,   Piece.WhitePawn,   Piece.WhitePawn},
            {Piece.WhiteRook, Piece.WhiteKnight, Piece.WhiteBishop, Piece.WhiteQueen, Piece.WhiteKing, Piece.WhiteBishop, Piece.WhiteKnight, Piece.WhiteRook},
    };
    Pair<Integer, Integer> passantablePawn = new Pair<>(-1, -1);
    boolean whiteTurn = true;
    // canCastle.get(color).get(kingSide);
    Map<Boolean, Map<Boolean, Boolean>> canCastle;
    public boolean justCastled = false;
    public boolean justPassanted = false;
    public Board() {
        canCastle = new HashMap<>();
        Map<Boolean, Boolean> whiteCastle = new HashMap<>();
        whiteCastle.put(false, true);
        whiteCastle.put(true, true);
        Map<Boolean, Boolean> blackCastle = new HashMap<>(whiteCastle);
        canCastle.put(true, whiteCastle);
        canCastle.put(false, blackCastle);
    }
    public Board(Piece[][] boardArray, boolean whiteTurn) {
        this();
        this.boardArray = boardArray;
        this.whiteTurn = whiteTurn;
    }


    public Board copy() {
        Piece[][] newBoard = new Piece[8][8];
        for(int i = 0; i < 8; i++) {
            newBoard[i] = boardArray[i].clone();
        }
        Board boardClone = new Board(newBoard, whiteTurn);
        boardClone.canCastle.put(true, new HashMap<>(canCastle.get(true)));
        boardClone.canCastle.put(false, new HashMap<>(canCastle.get(false)));
        return new Board(newBoard, whiteTurn);
    }


    //Returns if the move was successful
    public boolean move(int row1, int col1, int row2, int col2) {
        if (isWhite(row1, col1) != whiteTurn) {
            return false;
        }

        if (isCastle(row1, col1, row2, col2)) {
            handleCastle(row1, col1, row2, col2);
            justCastled = true;
            displayStatus();
            return true;
        } else {
            justCastled = false;
        }

        if (boardArray[row1][col1] == Piece.WhiteRook || boardArray[row1][col1] == Piece.BlackRook) {
            if (col1 == 7) {
                canCastle.get(whiteTurn).put(true, false);
            } else if (col1 == 0) {
                canCastle.get(whiteTurn).put(false, false);
            }
        }
        if (boardArray[row1][col1] == Piece.WhiteKing || boardArray[row1][col1] == Piece.BlackKing) {
            canCastle.get(whiteTurn).put(true, false);
            canCastle.get(whiteTurn).put(false, false);
        }

        if (isEnPassant(row1, col1, row2, col2)) {
            moveAnywhere(row1, col1, row2, col2);
            boardArray[row1][col2] = Piece.Empty;
            passantablePawn = new Pair<>(-1, -1);
            justPassanted = true;
            displayStatus();
            return true;
        } else {
            justPassanted = false;
        }


        if (legalMove(row1, col1, row2, col2)) {
            moveAnywhere(row1, col1, row2, col2);
            if (boardArray[row2][col2] == Piece.WhitePawn || boardArray[row2][col2] == Piece.BlackPawn) {
                passantablePawn = new Pair<>(row2, col2);
            } else {
                passantablePawn = new Pair<>(-1, -1);
            }
            displayStatus();
            return true;
        }
        return false;
    }

    public void displayStatus() {
        if (checkmate(whiteTurn)) {
            System.out.println("CHECKMATED " + getColorString(whiteTurn));
        }
        if (stalemate(whiteTurn)) {
            System.out.println("STALEMATE!");
        }
        if (inCheck(whiteTurn)) {
            System.out.println("CHECKED " + getColorString(whiteTurn));
        }
    }

    public boolean isEnPassant(int row1, int col1, int row2, int col2) {
        int newRow = whiteTurn ? row1-1 : row1+1;

        Piece pawn = whiteTurn ? Piece.WhitePawn : Piece.BlackPawn;
        Piece enemyPawn = whiteTurn ? Piece.BlackPawn : Piece.WhitePawn;
        return passantablePawn.getKey() == row1 &&
                passantablePawn.getValue() == col2 &&
                row2 == newRow && Math.abs(col2-col1) == 1 &&
                boardArray[row1][col1] == pawn && boardArray[row2][col2] == Piece.Empty &&
                boardArray[row1][col2] == enemyPawn;
    }
    public boolean isCastle(int row1, int col1, int row2, int col2) {
        if (boardArray[row1][col1] == Piece.WhiteKing || boardArray[row1][col1] == Piece.BlackKing) {
            if (Math.abs(col2 - col1) == 2) {
                boolean kingSide = col2 - col1 > 0;
                return canCastle(whiteTurn, kingSide);
            }
        }
        return false;
    }

    private boolean canCastle(boolean white, boolean kingSide) {
        if (!canCastle.get(white).get(kingSide)) {
            return false;
        }
        int kingRow = white ? 7 : 0;
        int kingColumn = 4;
        int dx = kingSide ? 1 : -1;
        Board testBoard = copy();
        boolean firstMove = testBoard.move(kingRow, kingColumn, kingRow, kingColumn + dx);
        testBoard.whiteTurn = !testBoard.whiteTurn;
        boolean secondMove = testBoard.move(kingRow, kingColumn + dx, kingRow, kingColumn + 2*dx);
        return firstMove && secondMove;
    }

    public void handleCastle(int row1, int col1, int row2, int col2) {
        boolean kingSide = col2 - col1 > 0;
        int rookRow = whiteTurn ? 7 : 0;
        int rookCol = kingSide ? 7 : 0;
        int newRookCol = kingSide ? col2-1 : col2 + 1;
        moveAnywhere(row1, col1, row2, col2);
        moveAnywhere(rookRow, rookCol, rookRow, newRookCol);
        canCastle.get(whiteTurn).put(true, false);
        canCastle.get(whiteTurn).put(false, false);
        whiteTurn = !whiteTurn;
        System.out.println("CASTLED!");
    }
    private String getColorString(boolean white) {
        return white ? "WHITE" : "BLACK";
    }

    public void moveAnywhere(int row1, int col1, int row2, int col2) {
        boardArray[row2][col2] = boardArray[row1][col1];
        boardArray[row1][col1] = Piece.Empty;
        whiteTurn = !whiteTurn;
    }

    public boolean legalMove(int row1, int col1, int row2, int col2) {
        if (moveInCheck(row1, col1, row2, col2)) {
            return false;
        }

        return pieceMoves(row1, col1)[row2][col2];
    }

    private boolean moveInCheck(int row1, int col1, int row2, int col2) {
        Board testBoard = copy();
        testBoard.moveAnywhere(row1, col1, row2, col2);
        return testBoard.inCheck(whiteTurn);
    }

    public boolean inCheck(boolean white) {
        Pair<Integer, Integer> kingPosition = white ? findPiece(Piece.WhiteKing) : findPiece(Piece.BlackKing);
        //Check if king is in check
        for(int i = 0; i < 8; i++) {
            for(int j=0; j < 8; j++) {
                if(isDifferentColor(i, j, kingPosition.getKey(), kingPosition.getValue()) &&
                        pieceMoves(i, j)[kingPosition.getKey()][kingPosition.getValue()]) {
                    return true;
                }
            }
        }
        return false;
    }
    public boolean stalemate(boolean white) {
        return getLegalMoves(white).size() == 0 && !inCheck(white);
    }
    public boolean checkmate(boolean white) {
        return getLegalMoves(white).size() == 0 && inCheck(white);
    }

    private List<List<Integer>> getLegalMoves(boolean white) {
        List<List<Integer>> legalMoves = new ArrayList<>();
        for(int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if(isColor(i, j, white)) {
                    boolean[][] moveArray = pieceMoves(i, j);
                    for (int k = 0; k < 8; k++) {
                        for (int l = 0; l < 8; l++) {
                            if (moveArray[k][l] && !moveInCheck(i, j, k, l)) {
                                legalMoves.add(List.of(i, j, k, l));
                            }
                        }
                    }
                }
            }
        }
        return legalMoves;
    }

    private Pair<Integer, Integer> findPiece(Piece piece)  {
        for(int i = 0; i < 8; i++) {
            for(int j=0; j < 8; j++) {
                if(boardArray[i][j] == piece) {
                    return new Pair<>(i, j);
                }
            }
        }
        throw new NoSuchElementException("Piece not found: " + piece);
    }

    private boolean[][] pieceMoves(int row, int col) {
        return switch (boardArray[row][col]) {
            case WhitePawn, BlackPawn -> pawnMoves(row, col);
            case WhiteKnight, BlackKnight -> knightMoves(row, col);
            case WhiteBishop, BlackBishop -> bishopMoves(row, col);
            case WhiteRook, BlackRook -> rookMoves(row, col);
            case WhiteQueen, BlackQueen -> queenMoves(row, col);
            case WhiteKing, BlackKing -> kingMoves(row, col);
            default -> throw new IllegalArgumentException(String.format("No piece at row:%d col:%d", row, col));
        };
    }

    private boolean[][] pawnMoves(int row, int col) {
        boolean[][] moves = new boolean[8][8];
        int rowDirection = isWhite(row, col) ? -1 : 1;
        int startRow = isWhite(row, col) ? 6 : 1;

        if (isEmpty(row + rowDirection, col)) {
            moves[row + rowDirection][col] = true;
            if(row == startRow && isEmpty(row + 2*rowDirection, col)) {
                moves[row + 2*rowDirection][col] = true;
            }
        }
        if (isDifferentColor(row, col, row+rowDirection, col+1)) {
            moves[row+rowDirection][col+1] = true;
        }
        if (isDifferentColor(row, col, row+rowDirection, col-1)) {
            moves[row+rowDirection][col-1] = true;
        }
        return moves;
    }

    private boolean[][] knightMoves(int row, int col) {
        boolean[][] moves = new boolean[8][8];
        for(int i : List.of(-1, 1)) {
            for(int j : List.of(-2, 2)) {
                if (isMovePosition(row, col, row+i, col+j)) {
                    moves[row+i][col+j] = true;
                }
                if (isMovePosition(row, col, row+j, col+i)) {
                    moves[row+j][col+i] = true;
                }
            }
        }
        return moves;
    }

    private boolean[][] bishopMoves(int row, int col) {
        boolean[][] moves = new boolean[8][8];
        for(int rowDirection : List.of(-1, 1)) {
            for (int colDirection : List.of(-1, 1)) {
                for(int i = 1; i < 8; i++) {
                    int row2 = row + i*rowDirection;
                    int col2 = col + i*colDirection;
                    if (isMovePosition(row, col, row2, col2)) {
                        moves[row2][col2] = true;
                        if (isDifferentColor(row, col, row2, col2)) {
                            break;
                        }
                        continue;
                    }
                    break;
                }
            }
        }
        return moves;
    }

    private boolean[][] rookMoves(int row, int col) {
        boolean[][] moves = new boolean[8][8];
        for (int c = 0; c < 4 ; c++) {
            boolean moveRow = c % 2 == 0;
            boolean positive = c > 1;
            for (int i = 1; i<8; i++) {
                int row2 = row + (moveRow ? (positive ? i: -i) : 0);
                int col2 = col + (moveRow ? 0 : (positive ? i: -i));
                if (isMovePosition(row, col, row2, col2)) {
                    moves[row2][col2] = true;
                    if (isDifferentColor(row, col, row2, col2)) {
                        break;
                    }
                    continue;
                }
                break;
            }
        }
        return moves;
    }

    private boolean[][] queenMoves(int row, int col) {
        boolean[][] moves = new boolean[8][8];
        boolean[][] bishopMoves = bishopMoves(row, col);
        boolean[][] rookMoves = rookMoves(row, col);
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                moves[i][j] = bishopMoves[i][j] || rookMoves[i][j];
            }
        }
        return moves;
    }

    private boolean[][] kingMoves(int row, int col) {
        boolean[][] moves = new boolean[8][8];
        for(int i = -1; i < 2; i++) {
            for(int j = -1; j < 2; j++) {
                if (isMovePosition(row, col, row+i, col+j)) {
                    moves[row+i][col+j] = true;
                }
            }
        }
        return moves;
    }

    private boolean inBoard(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
    private boolean isWhite(Piece piece) {
        return piece == Piece.WhitePawn || piece == Piece.WhiteKnight || piece == Piece.WhiteBishop ||
                piece == Piece.WhiteRook || piece == Piece.WhiteQueen || piece == Piece.WhiteKing;
    }

    private boolean isBlack(Piece piece) {
        return piece == Piece.BlackPawn || piece == Piece.BlackKnight || piece == Piece.BlackBishop ||
                piece == Piece.BlackRook || piece == Piece.BlackQueen || piece == Piece.BlackKing;
    }
    private boolean isColor(Piece piece, boolean white) {
        return white ? isWhite(piece) : isBlack(piece);
    }
    private boolean isEmpty(Piece piece) {
        return piece == Piece.Empty;
    }
    private boolean isWhite(int row, int col) {
        return inBoard(row, col) && isWhite(boardArray[row][col]);
    }
    private boolean isBlack(int row, int col) {
        return inBoard(row, col) && isBlack(boardArray[row][col]);
    }
    private boolean isColor(int row, int col, boolean white) {
        return inBoard(row, col) && isColor(boardArray[row][col], white);
    }
    private boolean isEmpty(int row, int col) {
        return inBoard(row, col) && isEmpty(boardArray[row][col]);
    }
    private boolean isSameColor(int row1, int col1, int row2, int col2) {
        return isWhite(row1, col1) && isWhite(row2, col2) || isBlack(row1, col1) && isBlack(row2, col2);
    }
    private boolean isDifferentColor(int row1, int col1, int row2, int col2) {
        return isWhite(row1, col1) && isBlack(row2, col2) || isBlack(row1, col1) && isWhite(row2, col2);
    }
    private boolean isMovePosition(int row1, int col1, int row2, int col2) {
        return inBoard(row2, col2) && !isSameColor(row1, col1, row2, col2);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Piece[] row : boardArray) {
            builder.append(Arrays.toString(row));
            builder.append("\n");
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        Board b = new Board();
        System.out.println(b.getLegalMoves(true));
    }
}
