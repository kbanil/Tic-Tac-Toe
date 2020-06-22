package tictactoe;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        TicTacToe ticTacToe = new TicTacToe(3);
        System.out.println(ticTacToe);
        boolean hasGameEnded = false;
        while (!hasGameEnded) {
            scanner = new Scanner(System.in);
            System.out.print("Enter the coordinates:");
            final String xCoordinateStr = scanner.next();
            if (!isNumber(xCoordinateStr)) {
                System.out.println("You should enter numbers!");
                continue;
            }
            final String yCoordinateStr = scanner.next();
            if (!isNumber(yCoordinateStr)) {
                System.out.println("You should enter numbers!");
                continue;
            }
            final TicTacToe.Validation validation = ticTacToe.mark(xCoordinateStr, yCoordinateStr);
            if (validation.isValid) {
                System.out.println(ticTacToe);
                if (ticTacToe.state == TicTacToe.State.X_WINS || ticTacToe.state == TicTacToe.State.O_WINS || ticTacToe.state == TicTacToe.State.DRAW) {
                    System.out.println(ticTacToe.state.message);
                    hasGameEnded = true;
                }
            } else {
                System.out.println(validation.message);
            }
        }
    }

    private static boolean isNumber(String number) {
        try {
            Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private static class TicTacToe {
        private final CharMatrix matrix;
        private State state = State.DRAW;
        private char nextChar = 'X';

        private TicTacToe(final int size) {
            matrix = new CharMatrix(size);
        }

        private TicTacToe(String field) {
            matrix = new CharMatrix(field);
        }

        private void analyze() {
            for (Iterator<Character> iterator : matrix.iterators()) {
                state = state.and(check(iterator));
            }
            if (Math.abs(matrix.noOfX - matrix.noOfO) > 1) {
                state = state.and(State.IMPOSSIBLE);
            }
        }

        private void analyze(int rowId, int colId) {
            for (Iterator<Character> iterator : matrix.iterators(rowId, colId)) {
                state = state.and(check(iterator));
            }
        }

        public Validation mark(final String xCoordinateStr, final String yCoordinateStr) {
            final int rowId = getRowId(Integer.parseInt(yCoordinateStr));
            final int colId = getColumnId(Integer.parseInt(xCoordinateStr));
            if (rowId < 0 || rowId >= matrix.size || colId < 0 || colId >= matrix.size) {
                return new Validation(false, "Coordinates should be from 1 to 3!");
            }
            if (matrix.get(rowId, colId) != '_') {
                return new Validation(false, "This cell is occupied! Choose another one!");
            }
            matrix.set(rowId, colId, nextChar);
            nextChar = nextChar == 'X' ? 'O' : 'X';
            analyze(rowId, colId);
            return new Validation(true, "Successful");
        }

        private State check(Iterator<Character> iterator) {
            char previous = 0;
            State state = State.DRAW;
            boolean allEquals = true;
            while (iterator.hasNext()) {
                final char c = iterator.next();
                if (allEquals) {
                    if (previous != 0) {
                        if (c != previous) {
                            allEquals = false;
                        }
                    }
                }
                previous = c;
                if (c == '_') {
                    allEquals = false;
                    state = state.and(State.NOT_FINISHED);
                }
            }
            if (allEquals && previous == 'X') {
                state = state.and(State.X_WINS);
            } else if (allEquals && previous == 'O') {
                state = state.and(State.O_WINS);
            }
            return state;
        }

        private int getRowId(int yCoordinate) {
            return matrix.size - yCoordinate;
        }

        private int getColumnId(int xCoordinate) {
            return xCoordinate - 1;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("---------\n");
            for (int i = 0; i < matrix.size; i++) {
                sb.append("| ");
                for (int j = 0; j < matrix.size; j++) {
                    sb.append(matrix.get(i, j)).append(" ");
                }
                sb.append("|\n");
            }
            sb.append("---------\n");

            //Comment out for now - Required later
            //sb.append(state.message);
            return sb.toString();
        }

        private enum State {
            NOT_FINISHED("Game not finished") {
                @Override
                State and(State other) {
                    switch (other) {
                        case NOT_FINISHED:
                        case DRAW:
                            return this;
                        default:
                            return other.and(this);
                    }
                }
            },
            DRAW("Draw") {
                @Override
                State and(State other) {
                    if (other == State.DRAW) {
                        return this;
                    }
                    return other.and(this);
                }
            },
            X_WINS("X wins") {
                @Override
                State and(State other) {
                    switch (other) {
                        case IMPOSSIBLE:
                        case O_WINS:
                            return IMPOSSIBLE;
                        default:
                            return this;
                    }
                }
            },
            O_WINS("O wins") {
                @Override
                State and(State other) {
                    switch (other) {
                        case IMPOSSIBLE:
                        case X_WINS:
                            return IMPOSSIBLE;
                        default:
                            return this;
                    }
                }
            },
            IMPOSSIBLE("Impossible") {
                @Override
                State and(State other) {
                    return this;
                }
            };

            private final String message;

            State(String message) {
                this.message = message;
            }

            abstract State and(State other);
        }

        private static class Validation {
            private final boolean isValid;
            private final String message;

            private Validation(boolean isValid, String message) {
                this.isValid = isValid;
                this.message = message;
            }
        }
    }

    private static class CharMatrix {
        private final int size;
        private final char[][] matrix;
        private int noOfX;
        private int noOfO;

        private CharMatrix(int size) {
            this.size = size;
            matrix = new char[size][size];
            for (int i = 0; i < size; i++) {
                Arrays.fill(matrix[i], '_');
            }
        }

        private CharMatrix(String field) {
            size = (int) Math.sqrt(field.length());
            matrix = new char[size][size];
            for (int i = 0; i < field.length(); i++) {
                final int m = i / size;
                final int n = i % size;
                final char c = field.charAt(i);
                matrix[m][n] = c;
                if (c == 'X') {
                    noOfX++;
                } else if (c == 'O') {
                    noOfO++;
                }
            }
        }

        private char get(int x, int y) {
            return matrix[x][y];
        }

        public void set(int rowId, int colId, char x) {
            matrix[rowId][colId] = x;
        }

        private Iterator<Character>[] iterators() {
            Iterator<Character>[] iterators = new Iterator[size * 2 + 2];
            List<Iterator<Character>> list = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                list.add(new RowIterator(i));
                list.add(new ColumnIterator(i));
            }
            list.add(new DiagonalIterator(1));
            list.add(new DiagonalIterator(2));
            return list.toArray(iterators);
        }

        private Iterator<Character>[] iterators(int rowId, int colId) {
            Iterator<Character>[] iterators = new Iterator[2];
            List<Iterator<Character>> list = new ArrayList<>();
            list.add(new RowIterator(rowId));
            list.add(new ColumnIterator(colId));
            //If coordinate belongs in left diagonal
            if (rowId == colId) {
                list.add(new DiagonalIterator(1));
            }
            //if coordinate belongs in right diagonal
            if (rowId + colId == size - 1) {
                list.add(new DiagonalIterator(2));
            }
            return list.toArray(iterators);
        }

        private class RowIterator implements Iterator<Character> {
            private final int rowId;
            private int position = 0;

            private RowIterator(int rowId) {
                this.rowId = rowId;
            }

            @Override
            public boolean hasNext() {
                return position < size;
            }

            @Override
            public Character next() {
                return matrix[rowId][position++];
            }
        }

        private class ColumnIterator implements Iterator<Character> {
            private final int colId;
            private int position = 0;

            private ColumnIterator(int colId) {
                this.colId = colId;
            }

            @Override
            public boolean hasNext() {
                return position < size;
            }

            @Override
            public Character next() {
                return matrix[position++][colId];
            }
        }

        private class DiagonalIterator implements Iterator<Character> {
            private final int id;
            private int position = 0;

            private DiagonalIterator(int id) {
                this.id = id;
            }

            @Override
            public boolean hasNext() {
                return position < size;
            }

            @Override
            public Character next() {
                char item = 0;

                if (id == 1) {
                    item = matrix[position][position];
                } else if (id == 2) {
                    item = matrix[position][size - 1 - position];
                }
                position++;
                return item;
            }
        }

    }
}
