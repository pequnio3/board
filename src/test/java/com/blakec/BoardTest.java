package com.blakec;

import com.blakec.graph.Path;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Created by blakec on 6/2/15.
 */
public class BoardTest {

    @Test
    public void testIsValidPosition_valid() throws IOException {
        Board knightBoard = loadBoardFromFile(BoardLoader.SPECIAL_BOARD_32x32);
        Position p = new Position(13, 13);
        assertTrue(knightBoard.isValidPosition(p));
    }

    @Test
    public void testIsValidPosition_invalid_offBoard() throws IOException {
        Board knightBoard = loadBoardFromFile(BoardLoader.SPECIAL_BOARD_32x32);
        Position p = new Position(35, 23);
        assertFalse(knightBoard.isValidPosition(p));
    }

    @Test
    public void testIsValidPosition_invalid_rock() throws IOException {
        Board knightBoard = loadBoardFromFile(BoardLoader.SPECIAL_BOARD_32x32);
        Position p = new Position(24, 18);
        assertFalse(knightBoard.isValidPosition(p));
    }

    @Test
    public void testIsValidMove_valid() throws IOException {
        Board knightBoard = loadBoardFromFile(BoardLoader.SPECIAL_BOARD_32x32);
        Position start = new Position(13, 10);
        Position end = new Position(11, 9);
        assertTrue(knightBoard.isValidMove(start, end));
    }

    @Test
    public void testIsValidMove_valid_teleport() throws IOException {
        Board knightBoard = loadBoardFromFile(BoardLoader.SPECIAL_BOARD_32x32);
        Position start = new Position(11, 26);
        Position end = new Position(23, 27);
        assertTrue(knightBoard.isValidMove(start, end));
    }

    @Test
    public void testIsValidMove_invalid_barrier() throws IOException {
        Board knightBoard = loadBoardFromFile(BoardLoader.SPECIAL_BOARD_32x32);
        Position start = new Position(10, 15);
        Position end = Board.moveDirection(start, Board.Movement.UP_LEFT);
        assertFalse(knightBoard.isValidMove(start, end));
    }

    @Test
    public void testComputeCostOfMove_toWater() throws IOException {
        Board knightBoard = loadBoardFromFile(BoardLoader.SPECIAL_BOARD_32x32);
        Position end = new Position(12, 9);
        Position start = Board.moveDirection(end, Board.Movement.DOWN_LEFT);
        assertEquals(Board.WATER_COST, knightBoard.computeCostOfMove(start, end),0.0001);
    }

    @Test
    public void testComputeCostOfMove_toLava() throws IOException {
        Board knightBoard = loadBoardFromFile(BoardLoader.SPECIAL_BOARD_32x32);
        Position end = new Position(5, 15);
        Position start = Board.moveDirection(end, Board.Movement.DOWN_LEFT);
        assertEquals(Board.LAVA_COST, knightBoard.computeCostOfMove(start, end),0.0001);
    }
    @Test
    public void testComputeCostOfMove_toTeleporter_fromTeleporter() throws IOException {
        Board knightBoard = loadBoardFromFile(BoardLoader.SPECIAL_BOARD_32x32);
        Position start = new Position(11,26);
        Position end = new Position(23, 27);
        assertEquals(Board.TELEPORTER_COST, knightBoard.computeCostOfMove(start, end),0.0001);
    }

    @Test
    public void testShortestPath_basicBoard() throws Exception {
        Board knightBoard = loadBoardFromFile(BoardLoader.SIMPLE_BOARD_8x8);
        Path p = knightBoard.computeShortestPath(new Position(0, 0), new Position(7, 7));
        assertEquals(7, p.getPath().size());
        assertTrue(knightBoard.isValidSetOfMoves(p));
    }

    @Test
    public void testShortestPath_teleportBoard() throws Exception {
        Board knightBoard = loadBoardFromFile(BoardLoader.TELEPORT_BOARD_8x8);
        Path p = knightBoard.computeShortestPath(new Position(0, 0), new Position(7, 7));
        assertEquals(2, p.getPath().size());
        assertEquals(0.0, p.getWeight(), 0.0001);
        assertTrue(knightBoard.isValidSetOfMoves(p));
    }

    @Test
    public void testShortestPath_teleportLavaBoardd() throws Exception {
        Board knightBoard = loadBoardFromFile(BoardLoader.TELEPORTER_LAVA_BOARD_8x8);
        Path p = knightBoard.computeShortestPath(new Position(0, 0), new Position(7, 7));
        assertEquals(4, p.getPath().size());
        assertEquals(6.0, p.getWeight(), 0.0001);
        assertTrue(knightBoard.isValidSetOfMoves(p));
    }

    @Test
    public void testShortestPath_split_board_8x8_no_path() throws Exception {
        Board knightBoard = loadBoardFromFile(BoardLoader.SPLIT_BOARD_8x8);
        Path p = knightBoard.computeShortestPath(new Position(0, 0), new Position(7, 7));
        assertTrue(p.getPath().isEmpty());
    }

    @Test
    public void testShortestPath_special_32x32() throws Exception {
        Board knightBoard = loadBoardFromFile(BoardLoader.SPECIAL_BOARD_32x32);
        Path p = knightBoard.computeShortestPath(new Position(0, 0), new Position(31, 31));
        assertEquals(23, p.getPath().size());
        assertEquals(23.0, p.getWeight(), 0.0001);
        assertTrue(knightBoard.isValidSetOfMoves(p));
    }

    @Test
    public void testShortestPath_special_32x32_special_deep() throws Exception {
        Board knightBoard = loadBoardFromFile(BoardLoader.SPECIAL_BOARD_32x32);
        Path p = knightBoard.computeShortestPath(new Position(0, 0), new Position(0, 31));
        assertTrue(knightBoard.isValidSetOfMoves(p));
        assertEquals(28, p.getPath().size());
        assertEquals(27.0, p.getWeight(), 0.0001);
    }

    @Test
    public void testLongestPath_simple8x8() throws Exception {
        Board knightBoard = loadBoardFromFile(BoardLoader.SIMPLE_BOARD_8x8);
        final long start = System.currentTimeMillis();
        Path p = knightBoard.computeLongestPathBruteForce(new Position(0, 0), new Position(0, 1));
        assertEquals(64, p.getPath().size());
        assertTrue(knightBoard.isValidSetOfMoves(p));
    }

    @Test
    public void testLongestPath_simple16x8() throws Exception {
        final Board knightBoard = loadBoardFromFile(BoardLoader.SIMPLE_BOARD_16x8);
        Path p = knightBoard.computeLongestPath(new Position(0, 0), new Position(13, 4));
        assertEquals(128, p.getPath().size());
        assertTrue(knightBoard.isValidSetOfMoves(p));
    }

    @Test
    public void testLongestPath_simple32x32_sameColor() throws Exception {
        final Board knightBoard = loadBoardFromFile(BoardLoader.SIMPLE_BOARD_32x32);
        Path p = knightBoard.computeLongestPath(new Position(0, 0), new Position(31, 31));
        assertEquals(1023, p.getPath().size());
        assertTrue(knightBoard.isValidSetOfMoves(p));
    }

    @Test
    public void testLongestPath_simple32x32_differentColor() throws Exception {
        final Board knightBoard = loadBoardFromFile(BoardLoader.SIMPLE_BOARD_32x32);
        Path p = knightBoard.computeLongestPath(new Position(0, 0), new Position(31, 30));
        assertEquals(1024, p.getPath().size());
        assertTrue(knightBoard.isValidSetOfMoves(p));
    }

    protected Board loadBoardFromFile(final String resource) throws IOException {
        final String board = new String(Files.readAllBytes(Paths.get(this.getClass().getResource(resource).getPath())));
        return new Board(board);
    }
}
