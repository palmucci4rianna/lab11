package it.unibo.oop.workers02;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class MultiThreadedSumMatrixWithFutures implements SumMatrix {

    private final int nthread;

    public MultiThreadedSumMatrixWithFutures(final int nthread) {
        super();
        if (nthread < 1) {
            throw new IllegalArgumentException();
        }
        this.nthread = nthread;
    }

    @Override
    public double sum(final double[][] matrix) {
        final int rows = matrix.length;
        final int chunk = rows / nthread + (rows % nthread == 0 ? 0 : 1);

        final ForkJoinPool pool = new ForkJoinPool(nthread);
        final List<Future<Double>> futures = new ArrayList<>();

        try {
            /*
            * Create tasks
            */
            for (int start = 0; start < rows; start += chunk) {
                final int from = start;
                final int to = Math.min(start + chunk, rows);

                futures.add(pool.submit(new Worker(matrix, from, to)));
            }

            /*
            * Collect results
            */
            double sum = 0;
            for (final Future<Double> f : futures) {
                sum += f.get();
            }
            return sum; 

        } catch (final InterruptedException | ExecutionException ex )  {
            throw new IllegalStateException(ex);
        } finally {
            pool.shutdown();
            try {
                pool.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static class Worker implements Callable<Double> {

        private final double[][] matrix;
        private final int fromRow;
        private final int toRow;

        Worker(final double[][] matrix, final int fromRow, final int toRow) {
            super();
            this.matrix = matrix;
            this.fromRow = fromRow;
            this.toRow = toRow;
        }

        @Override
        public Double call() {
            double res = 0;
            for (int r = fromRow; r < toRow && r < matrix.length ; r++) {
                for (int c = 0; c < matrix[r].length; c++) {
                    res += matrix[r][c];
                }
            }
            return res;
        }
    }
}
