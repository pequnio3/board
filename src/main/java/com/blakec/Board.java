package com.blakec;

import com.blakec.graph.Graph;
import com.blakec.graph.Path;
import com.blakec.graph.Vertex;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by blakec on 6/2/15.
 * <p/>
 * Board that represents an M x N grid that a knight can move around.
 */
public class Board implements Vertex {
    final Logger logger = Logger.getLogger(String.valueOf(Board.class));
    /**
     * The length of the short part of a knight's movement.
     */
    final static int SHORT_MOVE_DISTANCE = 1;
    /**
     * The length of the long part of a knight's movement.
     */
    final static int LONG_MOVE_DISTANCE = 2;
    /**
     * When dividing a board into sub boards, this is the size of the sub board's height and width
     */
    final static int SUB_BOARD_SIZE = 8;
    /**
     * Movewise Cost of landing on lava.
     */
    final static double LAVA_COST = 5.0;
    final static char LAVA_CHAR = 'L';
    /**
     * Movewise Cost of landing on wayer.
     */
    final static double WATER_COST = 2.0;
    final static char WATER_CHAR = 'W';
    /**
     * Movewise Cost of landing on a space.
     */
    final static double DEFAULT_COST = 1.0;
    final static char DEFAULT_CHAR = '.';
    /**
     * Movewise Cost of landing on teleporter.
     */
    final static double TELEPORTER_COST = 0.0;
    final static char TELEPORTER_CHAR = 'T';

    final static char ROCK_CHAR = 'R';
    final static char BARRIER_CHAR = 'B';
    final static char KNIGHT_CHAR = 'K';

    /**
     * Position of the boards origin.  For most boards this is (0,0).  However when finding longest path
     * boards will be broken into sub boards that have their base position be each sub boards origin in the
     * larger board.
     */
    final Position basePosition;
    /**
     * width of the board.
     */
    final int width;
    /**
     * height of the board.
     */
    final int height;
    /**
     * Set of position's representing locations where teleporters reside.
     */
    final Set<Position> teleporters = Sets.newHashSet();
    /**
     * Set of position's representing locations where lava resides.
     */
    final Set<Position> lava = Sets.newHashSet();
    /**
     * Set of position's representing locations where rocks reside.
     */
    final Set<Position> rocks = Sets.newHashSet();
    /**
     * Set of position's representing locations where barriers reside.
     */
    final Set<Position> barriers = Sets.newHashSet();
    /**
     * Set of position's representing locations where water resides.
     */
    final Set<Position> water = Sets.newHashSet();

    final Map<Character, Set<Position>> characterToSpecialPosition = ImmutableMap.<Character, Set<Position>>builder()
            .put(TELEPORTER_CHAR, teleporters)
            .put(LAVA_CHAR, lava)
            .put(ROCK_CHAR, rocks)
            .put(BARRIER_CHAR, barriers)
            .put(WATER_CHAR, water)
            .build();

    /**
     * @param board board represented by a grid of '.', 'W', 'R', 'B', 'T' characters.
     *              The height of the board is number of newline characters in the string.
     *              The width of the board is the number of characters in each line.  An error is
     *              thrown if the number of characters in a line differ from other lines.
     *              <p/>
     *              Upper left is 0,0.  Bottom right is width,height.
     */
    public Board(final String board) throws IOException {
        // set base position to be 0,0,
        basePosition = new Position(0, 0);
        // read in the board string, split on newline for rows
        // confirm size of everything is appropriate
        final List<String> rows = Lists.newArrayList(board.replaceAll(" ", "").split("\n"));
        height = rows.size();
        if (rows.isEmpty()) {
            throw new IOException("The board cannot have 0 rows.");
        }
        width = rows.get(0).length();
        // iterate over each cell in the string grid to extract
        // position type.
        for (int r = 0; r < rows.size(); r++) {
            final String row = rows.get(r);
            if (row.length() != width) {
                throw new IOException("The input string has rows with unequal lengths.");
            }
            for (int c = 0; c < row.length(); c++) {
                final Position p = getBasePosition().add(new Position(r, c));
                final char positionType = row.charAt(c);
                if (characterToSpecialPosition.containsKey(positionType)) {
                    characterToSpecialPosition.get(positionType).add(p);
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

    /**********************************
     ********** LONGEST PATH *********
     **********************************/

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

        /*
         * take starting position
         * make 1-2 moves to get to adjacent sub graph.
         * add these positions to longest path.
         * make the position that lands in the adjacent sub graph the new start position.
         * Run normally, but remember to remove the start and possible second position from the graph created
         * by final sub graph
         */

        // create a graph of all the sub boards of this board.
        // this results in a graph where each node represents a sub board and that node is connected
        // by edges to other nodes that contain adjacent (diagonal included) sub graphs
        final Graph graph = generateSubBoardGraph();
        // number of rows of sub boards
        int numRowsOfSubBoards = height / SUB_BOARD_SIZE;
        // number of columns of sub rows
        int numColsOfSubBoards = width / SUB_BOARD_SIZE;

        // subboard that the starting position is in
        final Board startBoard = getSubBoard(start);
        // subboard that the end position is in
        final Board endBoard = getSubBoard(end);
        // compute the longest path from the starting board to the endboard
        // this path will end up running throguh all other sub boards.
        Path path = graph.computeLongestPath(startBoard, endBoard, numRowsOfSubBoards * numColsOfSubBoards);
        if (path.getPath().isEmpty()) {
            // this should never happen, but if it does return an unsolved path an log it.
            logger.severe("Empty path from start subgraph to end subgraph.  This should not happen please investigate.");
            return new Path(Lists.<Vertex>newArrayList(), -1);
        }

        // path of sub boards from start location to end location
        final List<Vertex> boardPath = path.getPath();
        // longest path that will accumulate over running a knights tour/longest path
        // in sub boards on the board path
        final List<Vertex> longestPath = Lists.newArrayList();
        // get the first board
        Board curBoard = (Board) boardPath.remove(0);
        // get the starting position on this board
        // first board this will be the global start position
        Position curStartPosition = start;
        Position curEndPosition;
        for (final Vertex nextVertex : boardPath) {
            // iterate through the sub boards
            // generate the appropriate positions on this board to enter
            // from the previous board
            // as well as positions to end on in order to move to next board
            final Board nextBoard = (Board) nextVertex;
            curEndPosition = computeLinkPosition(curBoard, nextBoard, curStartPosition, true);
            //compute the longest path on this sub board from the entrance position to the exit position
            final Path longestPathInSubBoard = curBoard.computeLongestPathBruteForce(curStartPosition, curEndPosition);
            // add these positions to the longest path
            longestPath.addAll(longestPathInSubBoard.getPath());
            // compute the position that the next board will start at based on coming from
            // the current board
            curStartPosition = computeLinkPosition(curBoard, nextBoard, curStartPosition, false);
            curBoard = nextBoard;
        }

        // get the position within the sub board that end is.
        curEndPosition = end;
        final Path longestPathInEndSubBoard = curBoard.computeLongestPathBruteForce(curStartPosition, curEndPosition);
        longestPath.addAll(longestPathInEndSubBoard.getPath());
        return new Path(longestPath, longestPath.size() - 1);
    }

    /**
     * Determines link positions when moving from prev board to next board.  These are predetermined positions for each board.  As a result, we
     * can choose these to guarantee quick execution of knight tour in each board that doesn't contain a global start or end position.
     * <p/>
     * There are 64 combinations of start and exit positions depending on how the path moves.
     * <p/>
     * NOTE: I have not tested all these combinations out so it is possible that the depth first search longest path will run slowly on
     * some combinations.
     *
     * @param prev                sub board that we are moving from.
     * @param next                sub board that we are moving to.
     * @param prevStartPosition   position that we started from in our prev board.  This is used to determine what color square
     *                            we should end on in the prev board and subsequently what color square we should land on in the
     *                            next sub board.
     * @param gettingPrevPosition true if we want to get the link Position on the prev board, otherwise the link position on the
     *                            next board
     * @return the position of the link.
     * @throws Exception if the two boards are too far apart.
     */
    protected Position computeLinkPosition(Board prev, Board next, Position prevStartPosition, boolean gettingPrevPosition) throws Exception {
        // determine which direction the next sub board is in comparison to the prev sub board.
        int rowDirection = (next.getBasePosition().getR() / SUB_BOARD_SIZE) - (prev.getBasePosition().getR() / SUB_BOARD_SIZE);
        int colDirection = (next.getBasePosition().getC() / SUB_BOARD_SIZE) - (prev.getBasePosition().getC() / SUB_BOARD_SIZE);
        if (Math.abs(rowDirection) > 1 || Math.abs(colDirection) > 1) {
            throw new Exception("These boards not next to each other.prev" + prev + " next: " + next);
        }
        //TODO(blakec) There is probably a more concise way to do this, but I'm spelling them out
        //TODO(blakec) for clarity of thought
        //TODO(blakec) Also figure out cleaner way to put in constants

        if (rowDirection == -1 && colDirection == 0) {
            // -1 0 - NORTH
            // if start is white, end on a black
            final Position localPrevLink = isPositionWhite(prevStartPosition) ? new Position(0, 5) : new Position(0, 4);
            final Position prevLink = prev.getBasePosition().add(localPrevLink);
            final Position nextLink = moveDirection(prevLink, Movement.LEFT_UP);
            return gettingPrevPosition ? prevLink : nextLink;
        } else if (rowDirection == -1 && colDirection == 1) {
            // -1 1 - NORTH EAST
            final Position localPrevLink = isPositionWhite(prevStartPosition) ? new Position(0, 7) : new Position(1, 7);
            final Position prevLink = prev.getBasePosition().add(localPrevLink);
            final Position nextLink = moveDirection(prevLink, Movement.UP_RIGHT);
            return gettingPrevPosition ? prevLink : nextLink;
        } else if (rowDirection == 0 && colDirection == 1) {
            //  0 1 - EAST
            final Position localPrevLink = isPositionWhite(prevStartPosition) ? new Position(4, 7) : new Position(5, 7);
            final Position prevLink = prev.getBasePosition().add(localPrevLink);
            final Position nextLink = moveDirection(prevLink, Movement.DOWN_RIGHT);
            return gettingPrevPosition ? prevLink : nextLink;
        } else if (rowDirection == 1 && colDirection == 1) {
            //  1 1 - SOUTH EAST
            final Position localPrevLink = isPositionWhite(prevStartPosition) ? new Position(6, 7) : new Position(7, 7);
            final Position prevLink = prev.getBasePosition().add(localPrevLink);
            final Position nextLink = moveDirection(prevLink, Movement.DOWN_RIGHT);
            return gettingPrevPosition ? prevLink : nextLink;
        } else if (rowDirection == 1 && colDirection == 0) {
            //  1 0 - SOUTH
            final Position localPrevLink = isPositionWhite(prevStartPosition) ? new Position(7, 4) : new Position(7, 5);
            final Position prevLink = prev.getBasePosition().add(localPrevLink);
            final Position nextLink = moveDirection(prevLink, Movement.RIGHT_DOWN);
            return gettingPrevPosition ? prevLink : nextLink;
        } else if (rowDirection == 1 && colDirection == -1) {
            //  1 -1 - SOUTH WEST
            final Position localPrevLink = isPositionWhite(prevStartPosition) ? new Position(7, 0) : new Position(6, 0);
            final Position prevLink = prev.getBasePosition().add(localPrevLink);
            final Position nextLink = moveDirection(prevLink, Movement.DOWN_LEFT);
            return gettingPrevPosition ? prevLink : nextLink;
        } else if (rowDirection == 0 && colDirection == -1) {
            //  0 -1 - WEST
            final Position localPrevLink = isPositionWhite(prevStartPosition) ? new Position(5, 0) : new Position(4, 0);
            final Position prevLink = prev.getBasePosition().add(localPrevLink);
            final Position nextLink = moveDirection(prevLink, Movement.DOWN_LEFT);
            return gettingPrevPosition ? prevLink : nextLink;
        } else {
            //  0 -1 - NORTH WEST
            Position localPrevLink = isPositionWhite(prevStartPosition) ? new Position(1, 0) : new Position(0, 0);
            Position prevLink = prev.getBasePosition().add(localPrevLink);
            Position nextLink = moveDirection(prevLink, Movement.UP_LEFT);
            return gettingPrevPosition ? prevLink : nextLink;
        }
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

    /**
     * Generate a sub board graph from current board.  This graph is composed of nodes each containing a sub board of the
     * large board.  Board nodes are connected if the sub boards are adjacent or diagonal to each other in the large board.
     * Each edge has a weight of 1.
     *
     * @return
     */
    protected Graph generateSubBoardGraph() {
        final Graph graph = new Graph();
        int numRowsOfSubBoards = height / SUB_BOARD_SIZE;
        int numColsOfSubBoards = width / SUB_BOARD_SIZE;
        for (int r = 0; r < height / SUB_BOARD_SIZE; r++) {
            for (int c = 0; c < width / SUB_BOARD_SIZE; c++) {
                final Board board = new Board(r * SUB_BOARD_SIZE, c * SUB_BOARD_SIZE, SUB_BOARD_SIZE, SUB_BOARD_SIZE);
                final Set<Board> neighborSubBoards = generateNeighborSubBoards(r, c, numRowsOfSubBoards, numColsOfSubBoards);
                for (Board neighbor : neighborSubBoards) {
                    graph.addEdge(board, neighbor, 1);
                }
            }
        }
        return graph;
    }

    /**
     * Generates the neighbors of this sub board.
     *
     * @param r                  row of sub board in large board.
     * @param c                  column of sub board in large board.
     * @param numRowsOfSubBoards number of rows of sub boards in the large board.
     * @param numColsOfSubBoards number of columns of sub boards in the large board.
     * @return a set of sub boards that are neighbors of this board.
     */
    protected Set generateNeighborSubBoards(final int r,
                                            final int c,
                                            final int numRowsOfSubBoards,
                                            final int numColsOfSubBoards) {
        final Set subBoards = Sets.newHashSet();
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

    /**
     * Computes the longest path from start to end within this board.  The graph traversal depth first search the Warnsdorf's rule as
     * a heuristic for choosing which neighbor to investigate next.  This will run until all paths are exhausted or a path with the max
     * number of hops is reached.
     * <p/>
     * NOTE:  Being a brute force depth first search algorithm, this could take a long time on larger boards.  Please limit useage to
     * 8x8 or smaller boards.
     *
     * @param start start position.
     * @param end   end position.
     * @return Path containing sequence of positions to achieve the longest path and the associated weight of the path.
     * @throws Exception
     */
    protected Path computeLongestPathBruteForce(final Position start, final Position end) throws Exception {
        if (!isValidPosition(start)) {
            throw new Exception("The start position " + start + " is invalid.  Either off the board or starting on barrier or rock.");
        }
        if (!isValidPosition(end)) {
            throw new Exception("The end position " + end + " is invalid.  Either off the board or starting on barrier or rock.");
        }
        Graph graph = generatePositionGraph();
        boolean arePositionsSameColor = isPositionWhite(start) == isPositionWhite(end);
        // every move that is made changes the knights position to a different color (black or white)
        // if the final position is the same color this means the knight has to make an even number of moves to get there
        // if the board has an even number of squares on it, the knight can't get to every square and end on then target
        // thus the max number of possible moves is has to be odd and one less than the number of even squares
        boolean isNumberOfSquaresEven = width * height % 2 == 0;
        int maxPathSize = isNumberOfSquaresEven && arePositionsSameColor ? width * height - 1 : width * height;
        return graph.computeLongestPath(start, end, maxPathSize);
    }

    /**********************************
     ********** SHORTEST PATH *********
     **********************************/

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
        Graph graph = generatePositionGraph();
        return graph.computeShortestPath(start, end);
    }

    /**
     * Generates a graph a knights movements.  Each node in the graph is a position a knight can land on.
     * Two nodes are connected by an edge if a knight can make a single jump to from the first node's board position
     * to the second node's board position.  The graph uses directed edges, however in the case of a board if a node A
     * has a directed edge to B, the B will have a directed edge to A.
     * <p/>
     * If the board position of the target node is a normal position the edge weight will be 1.0.
     * If the board position of the target node is not a normal position, its weight will be based on the landing position.
     * <p/>
     * If a node contains a teleporter position, it will also have edges to all other teleporters.
     *
     * @return Graph representing all possible moves a knight can make on the board.
     */
    protected Graph generatePositionGraph() {
        final Graph graph = new Graph();
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                final Position position = getBasePosition().add(new Position(r, c));
                if (!isValidPosition(position)) {
                    // this is not a valid position (probably a rock or barrier)
                    // and should not be included in our graph.
                    continue;
                }
                final Set<Position> nextPositions = generatePossibleKnightMoves(position);
                for (final Position nextPosition : nextPositions) {
                    final double cost = computeCostOfMove(position, nextPosition);
                    graph.addEdge(position, nextPosition, cost);
                }
            }
        }
        return graph;
    }

    /**************************************
     ********** MOVEMENT/POSITION *********
     **************************************/

    /**
     * Movements a knight can make.
     */
    protected static enum Movement {
        UP_LEFT(-LONG_MOVE_DISTANCE, -SHORT_MOVE_DISTANCE, true),
        UP_RIGHT(-LONG_MOVE_DISTANCE, SHORT_MOVE_DISTANCE, true),
        RIGHT_UP(-SHORT_MOVE_DISTANCE, LONG_MOVE_DISTANCE, false),
        RIGHT_DOWN(SHORT_MOVE_DISTANCE, LONG_MOVE_DISTANCE, false),
        DOWN_RIGHT(LONG_MOVE_DISTANCE, SHORT_MOVE_DISTANCE, true),
        DOWN_LEFT(LONG_MOVE_DISTANCE, -SHORT_MOVE_DISTANCE, true),
        LEFT_DOWN(SHORT_MOVE_DISTANCE, -LONG_MOVE_DISTANCE, false),
        LEFT_UP(-SHORT_MOVE_DISTANCE, -LONG_MOVE_DISTANCE, false);
        /**
         * Change in columns for knights movement.
         */
        final int dColumns;
        /**
         * Change in rows for a knights movement
         */
        final int dRows;
        // whether this direction involves moving the rows first, or the
        final boolean moveRowsFirst;

        Movement(int dRows, int dColumns, boolean moveRowsFirst) {
            this.dColumns = dColumns;
            this.dRows = dRows;
            this.moveRowsFirst = moveRowsFirst;
        }
    }

    /**
     * Determines whether the input list of positions is a valid set of moves for a knight.
     *
     * @param moves      list of moves.
     * @param printMoves if true the moves on the board will printed to be standard out.
     * @return true if the list of moves are possible by a single knight. false otherwise.
     */
    public boolean isValidSetOfMoves(final List<Position> moves, boolean printMoves) {
        if (moves.isEmpty()) {
            // no move is a valid move
            return true;
        }

        Position cur = moves.get(0);
        for (int i = 1; i < moves.size(); i++) {
            final Position next = moves.get(i);
            if (!isValidMove(cur, next)) {
                return false;
            }
            cur = next;
        }
        if (printMoves) {
            printKnightsPathOnBoard(moves);
        }
        return true;
    }

    /**
     * Determines whether the input list of positions is a valid set of moves for a knight.
     *
     * @param path path
     * @return true if the list of moves are possible by a single knight. false otherwise.
     */
    public boolean isValidSetOfMoves(final Path path, boolean printPath) {
        final List<Position> moves = Lists.newArrayList();
        for (Vertex v : path.getPath()) {
            moves.add((Position) v);
        }
        return isValidSetOfMoves(moves, printPath);
    }

    /**
     * Determines whether the input list of positions is a valid set of moves for a knight.
     *
     * @param path path
     * @return true if the list of moves are possible by a single knight. false otherwise.
     */
    public boolean isValidSetOfMoves(final Path path) {
        return isValidSetOfMoves(path, false);
    }

    /**
     * Determines whether the input list of positions is a valid set of moves for a knight.
     *
     * @param moves list of moves.
     * @return true if the list of moves are possible by a single knight. false otherwise.
     */
    public boolean isValidSetOfMoves(final List<Position> moves) {
        return isValidSetOfMoves(moves, false);
    }

    /**
     * Determines whether moving from start to end is a valid move according to a Knights movement and possible obstacles.
     *
     * @param start
     * @param end
     * @return
     */
    protected boolean isValidMove(final Position start, final Position end) {
        if (teleporters.contains(start) && teleporters.contains(end)) {
            // if you are moving from a teleporter to another teleporter this is a valid move.
            return true;
        }
        int dRow = Math.abs(end.getR() - start.getR());
        int dCol = Math.abs(end.getC() - start.getC());
        // true if the pure movement is in the L shape.
        boolean legalMovement = (dRow == LONG_MOVE_DISTANCE && dCol == SHORT_MOVE_DISTANCE) || (dRow == SHORT_MOVE_DISTANCE && dCol == LONG_MOVE_DISTANCE);

        Movement movement = null;
        for (Movement m : Movement.values()) {
            if (m.dRows == dRow && m.dColumns == dCol) {
                movement = m;
            }
        }
        if (movement == null) {
            return false;
        }

        boolean moveHitsBarrier = doesMoveHitBarrier(start, movement);
        boolean isValidStartPosition = isValidPosition(start);
        boolean isValidEndPosition = isValidPosition(end);
        return legalMovement && !moveHitsBarrier && isValidEndPosition && isValidStartPosition;
    }

    /**
     * Determines whether the position would be white or black.
     *
     * @param p
     * @return
     */
    protected boolean isPositionWhite(final Position p) {
        return p.getC() % 2 == p.getR() % 2;
    }

    /**
     * Generates a Set of valid single moves a knight can make from a single position.
     * This checks for moving over barriers and landing on unlandable positions.
     *
     * @param p position.
     * @return all valid moves a knight can make from that position.
     */
    protected Set generatePossibleKnightMoves(final Position p) {
        final Set possibleMoves = Sets.newHashSet();
        for (Movement d : Movement.values()) {
            final Position possiblePosition = moveDirection(p, d);
            if (isValidMove(p, possiblePosition)) {
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

    /**
     * Move the position according to the specified movement.
     *
     * @param p        position
     * @param movement movement.
     * @return the new position after moving.
     */
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
        final Set positionPath = Sets.newHashSet();

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

    /**
     * Checks if the position is on the board and isn't on top of a rock or barrier.
     *
     * @param p position
     * @return true if the position is valid, false otherwise.
     */
    protected boolean isValidPosition(final Position p) {
        if (p.getC() < getBasePosition().getC() || p.getC() >= getBasePosition().getC() + width) {
            return false;
        }
        if (p.getR() < getBasePosition().getR() || p.getR() >= getBasePosition().getR() + height) {
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

    /**
     * Returns the base position of this board.
     *
     * @return base position.
     */
    public Position getBasePosition() {
        return basePosition;
    }

    /**
     * Prints the board for each step the knight makes in the provided path..
     *
     * @param path
     */
    public void printKnightsPathOnBoard(final List<Position> path) {
        for (final Position p : path) {
            System.out.println("----- Knight at Position " + p + " -------");
            printBoardWithKnightAtPosition(p);
        }
    }

    /**
     * Prints the current board with the knight in the given position.
     *
     * @param knight knights position.
     */
    public void printBoardWithKnightAtPosition(Position knight) {
        StringBuilder boardBuilder = new StringBuilder();
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                final Position p = new Position(r, c);
                char positionType = getPositionType(p);
                if (p.equals(knight)) {
                    boardBuilder.append(KNIGHT_CHAR);
                } else {
                    boardBuilder.append(positionType);
                }
            }
            boardBuilder.append('\n');
        }
        System.out.println(boardBuilder.toString());
    }

    /**
     * Given a position determines the character position type of this position. I.E. lava or rocks.
     *
     * @param p position
     * @return character representing position type.
     */
    protected char getPositionType(final Position p) {
        for (Map.Entry<Character, Set<Position>> entry : characterToSpecialPosition.entrySet()) {
            final Character positionType = entry.getKey();
            final Set<Position> positions = entry.getValue();
            if (positions.contains(p)) {
                return positionType;
            }
        }
        return DEFAULT_CHAR;
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
