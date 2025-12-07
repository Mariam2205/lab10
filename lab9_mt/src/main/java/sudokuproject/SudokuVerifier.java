package sudokuproject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

public class SudokuVerifier {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar <jar> <csv-path> <mode>");
            System.out.println("mode: 0 (sequential), 3 (3 threads), 27 (27 threads)");
            return;
        }

        String path = args[0];
        int mode;
        try {
            mode = Integer.parseInt(args[1]);
            if (mode != 0 && mode != 3 && mode != 27) throw new NumberFormatException();
        } catch (Exception e) {
            System.out.println("Mode must be one of: 0, 3, 27");
            return;
        }

        int[][] board;
        try {
            board = CSVReader.readCSV(path);
        } catch (IOException e) {
            System.out.println("Failed to read CSV: " + e.getMessage());
            return;
        }

        ValidationResult res = validateBoard(board, mode);

        if (res.isValid()) {
            System.out.println("VALID");
        } else {
            System.out.println("INVALID");
            if (!res.getRowMessages().isEmpty()) {
                for (String s : res.getRowMessages()) System.out.println(s);
                System.out.println("------------------------------------------");
            }
            if (!res.getColMessages().isEmpty()) {
                for (String s : res.getColMessages()) System.out.println(s);
                System.out.println("------------------------------------------");
            }
            if (!res.getBoxMessages().isEmpty()) {
                for (String s : res.getBoxMessages()) System.out.println(s);
            }
        }
    }

    /**
     * Programmatic validator used by tests.
     */
    public static ValidationResult validateBoard(int[][] board, int mode) {
        SudokuBoard sBoard = new SudokuBoard(board);
        ResultCollector collector = new ResultCollector();
        ValidatorFactory factory = new ValidatorFactory();

        if (mode == 0) {
            // sequential: rows, cols, boxes
            for (int r = 0; r < 9; r++) {
                Validator v = factory.createRowValidator(sBoard, collector, r);
                v.validate();
            }
            for (int c = 0; c < 9; c++) {
                Validator v = factory.createColValidator(sBoard, collector, c);
                v.validate();
            }
            for (int b = 0; b < 9; b++) {
                Validator v = factory.createBoxValidator(sBoard, collector, b);
                v.validate();
            }
        } else if (mode == 3) {
            ExecutorService exec = Executors.newFixedThreadPool(3);
            // rows worker
            exec.submit(() -> {
                for (int r = 0; r < 9; r++) factory.createRowValidator(sBoard, collector, r).validate();
            });
            // cols worker
            exec.submit(() -> {
                for (int c = 0; c < 9; c++) factory.createColValidator(sBoard, collector, c).validate();
            });
            // boxes worker
            exec.submit(() -> {
                for (int b = 0; b < 9; b++) factory.createBoxValidator(sBoard, collector, b).validate();
            });
            exec.shutdown();
            try { exec.awaitTermination(5, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
        } else {
            // mode == 27
            ExecutorService exec = Executors.newFixedThreadPool(27);
            for (int r = 0; r < 9; r++) {
                final int rr = r;
                exec.submit(() -> factory.createRowValidator(sBoard, collector, rr).validate());
            }
            for (int c = 0; c < 9; c++) {
                final int cc = c;
                exec.submit(() -> factory.createColValidator(sBoard, collector, cc).validate());
            }
            for (int b = 0; b < 9; b++) {
                final int bb = b;
                exec.submit(() -> factory.createBoxValidator(sBoard, collector, bb).validate());
            }
            exec.shutdown();
            try { exec.awaitTermination(5, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
        }

        ValidationResult res = new ValidationResult();
        res.setRowMessages(collector.getRows());
        res.setColMessages(collector.getCols());
        res.setBoxMessages(collector.getBoxes());
        return res;
    }
}
