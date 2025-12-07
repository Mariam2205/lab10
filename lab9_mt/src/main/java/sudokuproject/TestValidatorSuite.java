package sudokuproject;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TestValidatorSuite {
    public static void main(String[] args) throws Exception {
        System.out.println("Running Validator tests...");

        testAllOnesInvalid();
        testValidSolution();
        testModeEquivalence();
        testCsvReader();
        testOutputFormats();

        System.out.println("All validator tests passed.");
    }

    private static void testAllOnesInvalid() throws Exception {
        int[][] allOnes = new int[9][9];
        for (int r=0;r<9;r++) for (int c=0;c<9;c++) allOnes[r][c] = 1;
        ValidationResult r0 = SudokuVerifier.validateBoard(allOnes, 0);
        assert !r0.isValid() : "All-ones should be invalid";
        // Expect many ROW, COL, BOX messages
        assert !r0.getRowMessages().isEmpty() && !r0.getColMessages().isEmpty() && !r0.getBoxMessages().isEmpty();
        System.out.println("testAllOnesInvalid passed.");
    }

    private static void testValidSolution() throws Exception {
        int[][] valid = {
                {5,3,4,6,7,8,9,1,2},
                {6,7,2,1,9,5,3,4,8},
                {1,9,8,3,4,2,5,6,7},
                {8,5,9,7,6,1,4,2,3},
                {4,2,6,8,5,3,7,9,1},
                {7,1,3,9,2,4,8,5,6},
                {9,6,1,5,3,7,2,8,4},
                {2,8,7,4,1,9,6,3,5},
                {3,4,5,2,8,6,1,7,9}
        };
        ValidationResult v = SudokuVerifier.validateBoard(valid, 27);
        assert v.isValid() : "Valid solution should be VALID";
        System.out.println("testValidSolution passed.");
    }

    private static void testModeEquivalence() throws Exception {
        // Use a board with a few deliberate duplicates
        int[][] b = {
            {5,3,4,6,7,8,9,1,2},
            {6,7,2,1,9,5,3,4,8},
            {1,9,8,3,4,2,5,6,7},
            {8,5,9,7,6,1,4,2,3},
            {4,2,6,8,5,3,7,9,1},
            {7,1,3,9,2,4,8,5,6},
            {9,6,1,5,3,7,2,8,4},
            {2,8,7,4,1,9,6,3,5},
            {3,4,5,2,8,6,1,7,7} // duplicate 7 in last row & last column and last box
        };
        ValidationResult a0 = SudokuVerifier.validateBoard(b, 0);
        ValidationResult a3 = SudokuVerifier.validateBoard(b, 3);
        ValidationResult a27 = SudokuVerifier.validateBoard(b, 27);
        assert !a0.isValid() && !a3.isValid() && !a27.isValid();
        // compare sets of messages (order may differ in multithreaded modes)
        Set<String> s0 = new HashSet<>(); s0.addAll(a0.getRowMessages()); s0.addAll(a0.getColMessages()); s0.addAll(a0.getBoxMessages());
        Set<String> s3 = new HashSet<>(); s3.addAll(a3.getRowMessages()); s3.addAll(a3.getColMessages()); s3.addAll(a3.getBoxMessages());
        Set<String> s27 = new HashSet<>(); s27.addAll(a27.getRowMessages()); s27.addAll(a27.getColMessages()); s27.addAll(a27.getBoxMessages());
        assert s0.equals(s3) && s0.equals(s27) : "Modes produced different findings";
        System.out.println("testModeEquivalence passed.");
    }

    private static void testCsvReader() throws Exception {
        Path p = Files.createTempFile("sudoku_test", ".csv");
        List<String> lines = Arrays.asList(
                "5,3,4,6,7,8,9,1,2",
                "6,7,2,1,9,5,3,4,8",
                "1,9,8,3,4,2,5,6,7",
                "8,5,9,7,6,1,4,2,3",
                "4,2,6,8,5,3,7,9,1",
                "7,1,3,9,2,4,8,5,6",
                "9,6,1,5,3,7,2,8,4",
                "2,8,7,4,1,9,6,3,5",
                "3,4,5,2,8,6,1,7,9"
        );
        Files.write(p, lines);
        int[][] loaded = CSVReader.readCSV(p.toString());
        assert loaded[0][0] == 5 && loaded[8][8] == 9;
        Files.delete(p);
        System.out.println("testCsvReader passed.");
    }

    private static void testOutputFormats() throws Exception {
        // ensure message formatting contains expected tokens and no spaces in list
        int[][] allOnes = new int[9][9]; for (int r=0;r<9;r++) for (int c=0;c<9;c++) allOnes[r][c]=1;
        ValidationResult r = SudokuVerifier.validateBoard(allOnes, 0);
        for (String s : r.getRowMessages()) {
            assert s.startsWith("ROW ");
            assert s.contains(", #");
            assert s.contains("[") && s.contains("]");
            assert !s.contains("[ "); // ensure no space after '[' to match lab style
        }
        System.out.println("testOutputFormats passed.");
    }
}
