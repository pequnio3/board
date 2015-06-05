package com.blakec;

import com.blakec.graph.Path;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by blakec on 6/4/15.
 */
public class LevelTest {
    final BoardLoader boardLoader = new BoardLoader();

    /**
     * Tests level 1 capabilities
     * <p/>
     * Take in a board and a list of positions, verify the positions and print them out to console.
     */
    @Test
    public void test_level1() throws IOException {
        final Board board = boardLoader.loadBoardFromFile(BoardLoader.SIMPLE_BOARD_8x8);
        final List<Position> path = Lists.newArrayList(
                new Position(0, 0),
                new Position(1, 2),
                new Position(2, 4),
                new Position(3, 6),
                new Position(5, 5)
        );
        assertTrue(board.isValidSetOfMoves(path, true));
    }

    /**
     * Tests level 2/3 capabilities
     * <p/>
     * Take in a board and a start and end position.  Figure out a path.  This case it will always find shortest path.
     */
    @Test
    public void test_level_2_3() throws Exception {
        final Board board = boardLoader.loadBoardFromFile(BoardLoader.SIMPLE_BOARD_8x8);
        final Position start = new Position(0, 0);
        final Position end = new Position(7, 7);
        final Path p = board.computeShortestPath(start, end);
        assertEquals(7, p.getPath().size());
        assertEquals(6, p.getWeight(), 0.000001);
        assertTrue(board.isValidSetOfMoves(p));
    }


    /**
     * Tests level 4 capabilities
     * <p/>
     * Take in a board with special positions (rock, water, lava.. etc) and a start and end position.
     * Figure out the shortest path form start to end.
     */
    @Test
    public void test_level_4() throws Exception {
        final Board board = boardLoader.loadBoardFromFile(BoardLoader.SPECIAL_BOARD_32x32);
        final Position start = new Position(0, 0);
        final Position end = new Position(0, 31);
        final Path p = board.computeShortestPath(start, end);
        assertEquals(28, p.getPath().size());
        assertEquals(27, p.getWeight(), 0.000001);
        assertTrue(board.isValidSetOfMoves(p));
    }

    /**
     * Tests level 5 capabilities
     * <p/>
     * Take in a simple board and a start and end position.  Figure out the longest path from start to end.
     */
    @Test
    public void test_level_5() throws Exception {
        final Board board = boardLoader.loadBoardFromFile(BoardLoader.SIMPLE_BOARD_32x32);
        final Position start = new Position(0, 0);
        final Position end = new Position(10, 17);
        final Path p = board.computeLongestPath(start, end);
        assertEquals(1024, p.getPath().size());
        assertEquals(1023, p.getWeight(), 0.000001);
        assertTrue(board.isValidSetOfMoves(p));
    }
}
