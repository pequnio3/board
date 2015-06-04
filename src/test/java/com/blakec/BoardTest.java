package com.blakec;

import com.blakec.graph.Path;
import com.blakec.graph.Vertex;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by blakec on 6/2/15.
 */
public class BoardTest {

    @Test
    public void test_basicBoard() throws Exception {
        final String board =
                "........\n" +
                        "........\n" +
                        "........\n" +
                        "........\n" +
                        "........\n" +
                        "........\n" +
                        "........\n" +
                        "........";

        Board knightBoard = new Board(board);
        Path p = knightBoard.computeShortestPath(new Position(0, 0), new Position(7, 7));
        System.out.println(p);

    }

    @Test
    public void test_basicBoardLongestPath_simple() throws Exception {
        final String board =
                "........\n" +
                        "........\n" +
                        "........\n" +
                        "........\n" +
                        "........\n" +
                        "........\n" +
                        "........\n" +
                        "........";

        Board knightBoard = new Board(board);
        final long start = System.currentTimeMillis();
        Path p = knightBoard.computeLongestPathBruteForce(new Position(7, 6), new Position(0, 7));
        final long end = System.currentTimeMillis();
        final long a = end - start;
        System.out.println();

    }

    @Test
    public void test_longestPath_8x8() throws Exception {
        final String board =
                "........\n" +
                        "........\n" +
                        "........\n" +
                        "........\n" +
                        "........\n" +
                        "........\n" +
                        "........\n" +
                        "........";

        Board knightBoard = new Board(board);
        final long start = System.currentTimeMillis();
        Path p = knightBoard.computeLongestPath(new Position(6, 7), new Position(0, 4));
        final long end = System.currentTimeMillis();
        final long a = end - start;
        System.out.println();
    }

    @Test
    public void test_longestPath_16x8() throws Exception {
        final String board =
                "........\n" +
                        "........\n" +
                        "........\n" +
                        "........\n" +
                        "........\n" +
                        "........\n" +
                        "........\n" +
                        "........\n" +
                        "........\n" +
                        "........\n" +
                        "........\n" +
                        "........\n" +
                        "........\n" +
                        "........\n" +
                        "........\n" +
                        "........";

        Board knightBoard = new Board(board);
        final long start = System.currentTimeMillis();
        Path p = knightBoard.computeLongestPath(new Position(0, 0), new Position(13, 4));
        List<Position> moves = Lists.newArrayList();
        for (Vertex v : p.getPath()) {
            Position po = (Position) v;
            moves.add(po);
        }
        boolean isValid = knightBoard.isValidSetOfMoves(moves);
        assertTrue(isValid);
    }

    @Test
    public void test_longestPath32x32() throws Exception {
        final String board =
                "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................";

        Board knightBoard = new Board(board);
        Path p = knightBoard.computeLongestPath(new Position(4, 20), new Position(21, 4));
        List<Position> moves = Lists.newArrayList();
        for (Vertex v : p.getPath()) {
            Position po = (Position) v;
            moves.add(po);
        }
        boolean isValid = knightBoard.isValidSetOfMoves(moves);
        assertTrue(isValid);
    }

    @Test
    public void test_32x32() throws Exception {
        final String board =
                "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................";

        Board knightBoard = new Board(board);
        Path p = knightBoard.computeShortestPath(new Position(0, 0), new Position(20, 23));
        System.out.println(p);
    }

    @Test
    public void test_32x32_special() throws Exception {
        final String board =
                "........B...LLL.................\n" +
                        "........B...LLL.................\n" +
                        "........B...LLL...LLL...........\n" +
                        "........B...LLL..LLL...RR.......\n" +
                        "........B...LLLLLLLL...RR.......\n" +
                        "........B...LLLLLL..............\n" +
                        "........B............RR.........\n" +
                        "........BB...........RR.........\n" +
                        "........WBB.....................\n" +
                        "...RR...WWBBBBBBBBBB............\n" +
                        "...RR...WW.........B............\n" +
                        "........WW.........B......T.....\n" +
                        "...WWWWWWW.........B............\n" +
                        "...WWWWWWW.........B..RR........\n" +
                        "...WW..........BBBBB..RR.WWWWWWW\n" +
                        "...WW..........B.........W......\n" +
                        "WWWW...........B...WWWWWWW......\n" +
                        "...WWWWWWW.....B............BBBB\n" +
                        "...WWWWWWW.....BBB..........B...\n" +
                        "...WWWWWWW.......BWWWWWWBBBBB...\n" +
                        "...WWWWWWW.......BWWWWWWB.......\n" +
                        "...........BBB..........BB......\n" +
                        ".....RR....B.............B......\n" +
                        ".....RR....B.............B.T....\n" +
                        "...........B.....RR......B......\n" +
                        "...........B.....RR.............\n" +
                        "...........B..........RR........\n" +
                        "...........B..........RR........\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n" +
                        "................................\n";
        Board knightBoard = new Board(board);
        Path p = knightBoard.computeShortestPath(new Position(0, 0), new Position(31, 31));
        System.out.println(p);
    }
}
