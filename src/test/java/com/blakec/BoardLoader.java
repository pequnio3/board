package com.blakec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by blakec on 6/4/15.
 */
public class BoardLoader {
    final static String SIMPLE_BOARD_32x32 = "/simple_board_32x32.txt";
    final static String SIMPLE_BOARD_16x8 = "/simple_board_16x8.txt";
    final static String SIMPLE_BOARD_8x8 = "/simple_board_8x8.txt";
    final static String TELEPORT_BOARD_8x8 = "/teleport_board_8x8.txt";
    final static String TELEPORTER_LAVA_BOARD_8x8 = "/teleporter_lava_board_8x8.txt";
    final static String SPLIT_BOARD_8x8 = "/split_board_8x8.txt";
    final static String SPECIAL_BOARD_4x4 = "/special_board_4x4.txt";
    final static String SPECIAL_BOARD_32x32 = "/special_board_32x32.txt";

    protected Board loadBoardFromFile(final String resource) throws IOException {
        final String board = new String(Files.readAllBytes(Paths.get(this.getClass().getResource(resource).getPath())));
        return new Board(board);
    }
}
