package sudokuproject;

import java.io.*;
import java.util.*;

public class CSVReader {
    public static int[][] readCSV(String path) throws IOException {
        int[][] board = new int[9][9];
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        int r = 0;
        while ((line = br.readLine()) != null && r < 9) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts;
            if (line.contains(",")) parts = line.split("\\s*,\\s*");
            else parts = line.split("\\s+");
            if (parts.length < 9) {
                br.close();
                throw new IOException("Each row must contain 9 values");
            }
            for (int c = 0; c < 9; c++) board[r][c] = Integer.parseInt(parts[c]);
            r++;
        }
        br.close();
        if (r != 9) throw new IOException("CSV must contain 9 rows");
        return board;
    }
}
