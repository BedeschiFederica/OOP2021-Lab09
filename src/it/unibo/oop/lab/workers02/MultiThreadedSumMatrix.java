package it.unibo.oop.lab.workers02;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a tool that sums a matrix using multiple threads.
 */
public final class MultiThreadedSumMatrix implements SumMatrix {

    private final int nthread;

    /**
     * Build a new {@link MultiThreadedSumMatrix}.
     * @param nthread
     *            no. of thread performing the sum
     */
    public MultiThreadedSumMatrix(final int nthread) {
        this.nthread = nthread;
    }

    private static class Worker extends Thread {
        private final double[][] matrix;
        private final int startRow;
        private final int nRows;
        private double sum;

        /**
         * Build a new {@link Worker}.
         * 
         * @param matrix
         *            the matrix to sum
         * @param startRow
         *            the initial position for this worker
         * @param nRows
         *            the no. of rows to sum up for this worker
         */
        Worker(final double[][] matrix, final int startRow, final int nRows) {
            super();
            this.matrix = matrix.clone();
            this.startRow = startRow;
            this.nRows = nRows;
        }

        @Override
        public void run() {
            System.out.println("Working from row " + this.startRow + " to row "
                    + (this.startRow + this.nRows - 1 < this.matrix.length ? this.startRow + this.nRows - 1
                            : this.matrix.length - 1));
            for (int row = this.startRow; row < this.startRow + this.nRows && row < this.matrix.length; row++) {
                for (final double elem: this.matrix[row]) {
                    this.sum += elem;
                }
            }
        }

        /**
         * Returns the result of summing up the selected rows of the matrix.
         * 
         * @return the sum of each element of the selected rows
         */
        public double getResult() {
            return this.sum;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double sum(final double[][] matrix) {
        final int workerRows = matrix.length % nthread + matrix.length / nthread;
        final List<Worker> workers = new ArrayList<>(nthread);
        for (int startRow = 0; startRow < matrix.length; startRow += workerRows) {
            workers.add(new Worker(matrix, startRow, workerRows));
        }
        for (final Worker w: workers) {
            w.start();
        }
        double sum = 0;
        for (final Worker w: workers) {
            try {
                w.join();
                sum += w.getResult();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        return sum;
    }
}
