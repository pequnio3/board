package com.blakec;

import com.blakec.graph.Graph;
import com.blakec.graph.Path;
import com.blakec.graph.Vertex;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Created by blakec on 6/2/15.
 */
public class Board {
    final static int SHORT_MOVE_DISTANCE = 1;
    final static int LONG_MOVE_DISTANCE = 2;
    final static int SUB_BOARD_SIZE = 8;
    // moving from prev board to next board doesn't change a dimension's position
    // this is the value that the prev linking positions dimension should be
    final static int PREV_LINK_STAY_POSITION = SUB_BOARD_SIZE / 2;
    // moving from prev board to next board increases change a dimension's position
    // this is the value that the prev linking positions dimension should be
    final static int PREV_LINK_INCREASE_POSITION = SUB_BOARD_SIZE - 1;
    // moving from prev board to next board decreases change a dimension's position
    // this is the value that the prev linking positions dimension should be
    final static int PREV_LINK_DECREASE_POSITION = 0;
    // moving from prev board to next board doesn't change a dimension's position
    // this is the value that the next linking positions dimension should be
    final static int NEXT_LINK_STAY_POSITION = SUB_BOARD_SIZE / 2 + LONG_MOVE_DISTANCE;
    // moving from prev board to next board decreases change a dimension's position
    // this is the value that the next linking positions dimension should be
    final static int NEXT_LINK_INCREASE_POSITION = 0;
    // moving from prev board to next board decreases change a dimension's position
    // this is the value that the next linking positions dimension should be
    final static int NEXT_LINK_DECREASE_POSITION = SUB_BOARD_SIZE - 1;

    final static double LAVA_COST = 5.0;
    final static double WATER_COST = 2.0;
    final static double DEFAULT_COST = 1.0;
    final static double TELEPORTER_COST = 0.0;

    protected static enum Movement {
        UP_LEFT(-LONG_MOVE_DISTANCE, -SHORT_MOVE_DISTANCE, true),
        UP_RIGHT(-LONG_MOVE_DISTANCE, SHORT_MOVE_DISTANCE, true),
        RIGHT_UP(-SHORT_MOVE_DISTANCE, LONG_MOVE_DISTANCE, false),
        RIGHT_DOWN(SHORT_MOVE_DISTANCE, LONG_MOVE_DISTANCE, false),
        DOWN_RIGHT(LONG_MOVE_DISTANCE, SHORT_MOVE_DISTANCE, true),
        DOWN_LEFT(LONG_MOVE_DISTANCE, -SHORT_MOVE_DISTANCE, true),
        LEFT_DOWN(SHORT_MOVE_DISTANCE, -LONG_MOVE_DISTANCE, false),
        LEFT_UP(-SHORT_MOVE_DISTANCE, -LONG_MOVE_DISTANCE, false);

        final int dColumns;
        final int dRows;
        // whether this direction involves moving the rows first, or the
        final boolean moveRowsFirst;

        Movement(int dRows, int dColumns, boolean moveRowsFirst) {
            this.dColumns = dColumns;
            this.dRows = dRows;
            this.moveRowsFirst = moveRowsFirst;
        }
    }

    final Position basePosition;
    final int width;
    final int height;
    final Set<Position> teleporters = Sets.newHashSet();
    final Set<Position> lava = Sets.newHashSet();
    final Set<Position> rocks = Sets.newHashSet();
    final Set<Position> barriers = Sets.newHashSet();
    final Set<Position> water = Sets.newHashSet();

    /**
     * @param board board represented by a grid of '.', 'W', 'R', 'B', 'T' characters.
     *              The height of the board is number of newline characters in the string.
     *              The width of the board is the number of characters in each line.  An error is
     *              thrown if the number of characters in a line differ from other lines.
     *              <p/>
     *              Upper left is 0,0.  Bottom right is width,height.
     */
    public Board(final String board) throws IOException {
        basePosition = new Position(0, 0);
        final List<String> rows = Lists.newArrayList(board.replaceAll(" ", "").split("\n"));
        height = rows.size();
        if (rows.isEmpty()) {
            throw new IOException("The board cannot have 0 rows.");
        }
        width = rows.get(0).length();
        for (int r = 0; r < rows.size(); r++) {
            final String row = rows.get(r);
            if (row.length() != width) {
                throw new IOException("The input string has rows with unequal lengths.");
            }
            for (int c = 0; c < row.length(); c++) {
                final Position p = new Position(r, c);
                final char value = row.charAt(c);
                switch (value) {
                    case 'W':
                        // this is a water position
                        water.add(p);
                        break;
                    case 'R':
                        // this is a rock position
                        rocks.add(p);
                        break;
                    case 'B':
                        // this is a barrier position
                        barriers.add(p);
                        break;
                    case 'T':
                        // this is a teleporter position
                        teleporters.add(p);
                        break;
                    case 'L':
                        // this is a lava position
                        lava.add(p);
                        break;
                    default:
                        //do nothing
                        break;
                }
            }
        }

    }

    /**
     * Creates an all blank board.  I.e. no special spaces.
     *
     * @param baseRow base row coordinates of top left. useful for sub boards.
     * @param baseCol base column coordinates of top left.  useful for sub boards.
     * @param width   width of board.
     * @param height  height of board.
     */
    public Board(int baseRow, int baseCol, int width, int height) {
        basePosition = new Position(baseRow, baseCol);
        this.width = width;
        this.height = height;
    }

    /**
     * Computes the longest path between the start position and end position.
     *
     * @param start Start position.
     * @param end   End position.
     * @returns Shortest Path from start to end.
     */
    public Path computeLongestPath(final Position start, final Position end) throws Exception {
        if (!isValidPosition(start)) {
            throw new Exception("The start position " + start + " is invalid.  Either off the board or starting on barrier or rock.");
        }
        if (!isValidPosition(end)) {
            throw new Exception("The end position " + end + " is invalid.  Either off the board or starting on barrier or rock.");
        }
        if (width % 8 != 0 || height % 8 != 0) {
            throw new Exception("The board must have width and height be multiples of eight or the longest path is too slow.");
        }

        final Graph<Board> graph = generateSubBoardGraph();

        int numRowsOfSubBoards = height / SUB_BOARD_SIZE;
        int numColsOfSubBoards = width / SUB_BOARD_SIZE;

        // subboard that the starting position is in
        final Board startingBoard = getSubBoard(start);
        final Vertex<Board> startVertex = new Vertex<Board>(startingBoard);

        // subboard that the end position is in
        final Board endBoard = getSubBoard(end);

        final Vertex<Board> endVertex = new Vertex<Board>(endBoard);
        Path<Board> path = graph.computeLongestPath(startVertex, endVertex, numRowsOfSubBoards * numColsOfSubBoards);
        //if()
        return path;
    }

    protected Position computeLinkPosition(Board prev, Board next, boolean gettingPrevPosition) throws Exception {
        // ensure boards are next to each other

        int rowDirection = (next.getBasePosition().getR() / SUB_BOARD_SIZE) - (prev.getBasePosition().getR() / SUB_BOARD_SIZE);
        int colDirection = (next.getBasePosition().getC() / SUB_BOARD_SIZE) - (prev.getBasePosition().getC() / SUB_BOARD_SIZE);
        if (Math.abs(rowDirection) > 1 || Math.abs(colDirection) > 1) {
            throw new Exception("These boards not next to each other.prev" + prev + " next: " + next);
        }

        int r = rowDirection == 1 ? PREV_LINK_INCREASE_POSITION : rowDirection == -1 ? PREV_LINK_DECREASE_POSITION : PREV_LINK_STAY_POSITION;
        int c = colDirection == 1 ? PREV_LINK_INCREASE_POSITION : colDirection == -1 ? PREV_LINK_DECREASE_POSITION : PREV_LINK_STAY_POSITION;
        if (!gettingPrevPosition) {
            r = rowDirection == 1 ? NEXT_LINK_INCREASE_POSITION : rowDirection == -1 ? NEXT_LINK_DECREASE_POSITION : NEXT_LINK_STAY_POSITION;
            c = colDirection == 1 ? NEXT_LINK_INCREASE_POSITION : colDirection == -1 ? NEXT_LINK_DECREASE_POSITION : NEXT_LINK_STAY_POSITION;
        }

        return prev.getBasePosition().add(new Position(r, c));
    }

    /**
     * Returns the subboard that this position resides in.
     *
     * @param p position.
     * @return subboard that this position resides in.
     */
    protected Board getSubBoard(final Position p) {
        return new Board(
                (p.getR() / SUB_BOARD_SIZE) * SUB_BOARD_SIZE,
                (p.getC() / SUB_BOARD_SIZE) * SUB_BOARD_SIZE,
                SUB_BOARD_SIZE,
                SUB_BOARD_SIZE);
    }

    protected Graph<Board> generateSubBoardGraph() {
        final Graph<Board> graph = new Graph<Board>();
        int numRowsOfSubBoards = height / SUB_BOARD_SIZE;
        int numColsOfSubBoards = width / SUB_BOARD_SIZE;
        for (int r = 0; r < height / SUB_BOARD_SIZE; r++) {
            for (int c = 0; c < width / SUB_BOARD_SIZE; c++) {
                final Board board = new Board(r * SUB_BOARD_SIZE, c * SUB_BOARD_SIZE, SUB_BOARD_SIZE, SUB_BOARD_SIZE);
                final Set<Board> neighborSubBoards = generateNeighborSubBoards(r, c, numRowsOfSubBoards, numColsOfSubBoards);
                for (Board neighbor : neighborSubBoards) {
                    graph.addEdge(new Vertex<Board>(board), new Vertex<Board>(neighbor), 1);
                }
            }
        }
        return graph;
    }

    protected Set<Board> generateNeighborSubBoards(final int r,
                                                   final int c,
                                                   final int numRowsOfSubBoards,
                                                   final int numColsOfSubBoards) {
        final Set<Board> subBoards = Sets.newHashSet();
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    // the board itself is not a neighbor
                    continue;
                }
                final int baseRow = r + dr;
                final int baseCol = c + dc;
                if (baseRow < 0 || baseRow >= numRowsOfSubBoards ||
                        baseCol < 0 || baseCol >= numColsOfSubBoards) {
                    // this sub board is out of bounds.
                    continue;
                }
                final Board subBoard = new Board(baseRow * SUB_BOARD_SIZE, baseCol * SUB_BOARD_SIZE, SUB_BOARD_SIZE, SUB_BOARD_SIZE);
                subBoards.add(subBoard);
            }
        }
        return subBoards;
    }

    protected Path computeLongestPathSmall(final Position start, final Position end) throws Exception {
        if (!isValidPosition(start)) {
            throw new Exception("The start position " + start + " is invalid.  Either off the board or starting on barrier or rock.");
        }
        if (!isValidPosition(end)) {
            throw new Exception("The end position " + end + " is invalid.  Either off the board or starting on barrier or rock.");
        }
        Graph graph = generateGraph();
        boolean arePositionsSameColor = isPositionWhite(start) == isPositionWhite(end);
        // every move that is made changes the knights position to a different color (black or white)
        // if the final position is the same color this means the knight has to make an even number of moves to get there
        // if the board has an even number of squares on it, the knight can't get to every square and end on then target
        // thus the max number of possible moves is has to be odd and one less than the number of even squares
        boolean isNumberOfSquaresEven = width * height % 2 == 0;
        int maxPathSize = isNumberOfSquaresEven && arePositionsSameColor ? width * height - 1 : width * height;
        return graph.computeLongestPath(new Vertex(start), new Vertex(end), maxPathSize);
    }

    /**
     * Determines whether the position would be white or black.
     *
     * @param p
     * @return
     */
    protected boolean isPositionWhite(final Position p) {
        return basePosition.add(p).getC() % 2 == basePosition.add(p).getR() % 2;
    }

    public boolean isValidSetOfMoves(final List<Position> moves) {
        if (moves.isEmpty()) {
            // no move is a valid move
            return true;
        }

        Position cur = moves.remove(0);
        for (final Position next : moves) {
            if (!isValidMove(cur, next)) {
                return false;
            }
            cur = next;
        }
        return true;
    }

    /**
     * Determines whether moving from start to end is a valid move according to a Knights movement
     *
     * @param start
     * @param end
     * @return
     */
    protected boolean isValidMove(final Position start, final Position end) {
        int dRow = Math.abs(end.getR() - start.getR());
        int dCol = Math.abs(end.getC() - start.getC());
        return (dRow == LONG_MOVE_DISTANCE && dCol == SHORT_MOVE_DISTANCE) || (dRow == SHORT_MOVE_DISTANCE && dCol == LONG_MOVE_DISTANCE);
    }

    /**
     * Computes the shortest path between the start position and end position.
     *
     * @param start Start position.
     * @param end   End position.
     * @returns Shortest Path from start to end.
     */
    public Path computeShortestPath(final Position start, final Position end) throws Exception {
        if (!isValidPosition(start)) {
            throw new Exception("The start position is invalid.  Either off the board or starting on barrier or rock.");
        }
        if (!isValidPosition(end)) {
            throw new Exception("The end position is invalid.  Either off the board or starting on barrier or rock.");
        }
        Graph graph = generateGraph();
        return graph.computeShortestPath(new Vertex<Position>(start), new Vertex<Position>(end));
    }

    /**
     * Generates all the moves possible on this board.
     *
     * @return Graph representing all possible moves a knight can make on the board.
     */
    protected Graph<Position> generateGraph() {
        final Graph<Position> graph = new Graph<Position>();
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                final Position position = new Position(r, c);
                final Set<Position> nextPositions = generatePossibleKnightMoves(position);
                for (Position nextPosition : nextPositions) {
                    final double cost = computeCostOfMove(position, nextPosition);
                    graph.addEdge(new Vertex<Position>(position), new Vertex<Position>(nextPosition), cost);
                }
            }
        }
        return graph;
    }

    /**
     * Generates a Set of valid single moves a knight can make from a single position.
     *
     * @param p position.
     * @return all valid moves a knight can make from that position.
     */
    protected Set<Position> generatePossibleKnightMoves(final Position p) {
        final Set<Position> possibleMoves = Sets.newHashSet();
        for (Movement d : Movement.values()) {
            final Position possiblePosition = moveDirection(p, d);
            if (isValidPosition(possiblePosition) && !doesMoveHitBarrier(p, d)) {
                // only keep this move if it is valid
                possibleMoves.add(possiblePosition);
            }
        }

        if (teleporters.contains(p)) {
            // if p a teleporter then the knight can move to all other teleporters.
            possibleMoves.addAll(teleporters);
        }
        return possibleMoves;
    }

    protected static Position moveDirection(final Position p, final Movement movement) {
        return new Position(p.getR() + movement.dRows, p.getC() + movement.dColumns);
    }

    /**
     * Checks if the move from start in given direction moves through a barrier
     *
     * @param start start position.
     * @param d     direction.
     * @return true if move hits a barrier.
     */
    protected boolean doesMoveHitBarrier(final Position start, final Movement d) {
        final Set<Position> positionPath = Sets.newHashSet();

        final int firstMoveDistance = d.moveRowsFirst ? d.dRows : d.dColumns;
        for (int i = 1; i <= firstMoveDistance; i++) {
            int r = d.moveRowsFirst ? start.getR() + i : start.getR();
            int c = d.moveRowsFirst ? start.getC() : start.getC() + i;
            final Position newPos = new Position(r, c);
            positionPath.add(newPos);
        }

        final int secondMoveDistance = d.moveRowsFirst ? d.dColumns : d.dRows;
        for (int i = 1; i <= secondMoveDistance; i++) {
            int r = d.moveRowsFirst ? start.getR() : start.getR() + i;
            int c = d.moveRowsFirst ? start.getC() + i : start.getC();
            final Position newPos = new Position(r, c);
            positionPath.add(newPos);
        }
        return !Sets.intersection(barriers, positionPath).isEmpty();
    }

    protected boolean isValidPosition(final Position p) {
        if (p.getC() < 0 || p.getC() >= width) {
            return false;
        }
        if (p.getR() < 0 || p.getR() >= height) {
            return false;
        }
        if (barriers.contains(p)) {
            return false;
        }
        if (rocks.contains(p)) {
            return false;
        }
        return true;
    }

    /**
     * Calculates the cost of moving from the start position to the end position.
     * Cost may change based on obstacles
     *
     * @param start start position
     * @param end   end position
     * @return
     */
    protected double computeCostOfMove(final Position start, final Position end) {
        if (teleporters.contains(start) && teleporters.contains(end)) {
            return TELEPORTER_COST;
        }
        if (lava.contains(end)) {
            return LAVA_COST;
        }
        if (water.contains(end)) {
            return WATER_COST;
        }
        return DEFAULT_COST;
    }

    public Position getBasePosition() {
        return basePosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Board that = (Board) o;

        if (height != that.height) return false;
        if (width != that.width) return false;
        if (barriers != null ? !barriers.equals(that.barriers) : that.barriers != null) return false;
        if (basePosition != null ? !basePosition.equals(that.basePosition) : that.basePosition != null) return false;
        if (lava != null ? !lava.equals(that.lava) : that.lava != null) return false;
        if (rocks != null ? !rocks.equals(that.rocks) : that.rocks != null) return false;
        if (teleporters != null ? !teleporters.equals(that.teleporters) : that.teleporters != null) return false;
        if (water != null ? !water.equals(that.water) : that.water != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = basePosition != null ? basePosition.hashCode() : 0;
        result = 31 * result + width;
        result = 31 * result + height;
        result = 31 * result + (teleporters != null ? teleporters.hashCode() : 0);
        result = 31 * result + (lava != null ? lava.hashCode() : 0);
        result = 31 * result + (rocks != null ? rocks.hashCode() : 0);
        result = 31 * result + (barriers != null ? barriers.hashCode() : 0);
        result = 31 * result + (water != null ? water.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Board{" +
                "basePosition=" + basePosition +
                '}';
    }
}
