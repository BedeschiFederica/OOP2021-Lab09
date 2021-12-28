package it.unibo.oop.lab.reactivegui03;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * A reactive GUI that counts incrementally or decrementally.
 */
public final class AnotherConcurrentGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private final JLabel display = new JLabel();
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");
    private final JButton stop = new JButton("stop");
    private final CounterAgent counterAgent = new CounterAgent();

    /**
     * Builds a new CGUI.
     */
    public AnotherConcurrentGUI() {
        super();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.add(this.display);
        panel.add(this.up);
        panel.add(this.down);
        panel.add(this.stop);
        this.getContentPane().add(panel);
        this.setVisible(true);
        this.stop.addActionListener(e -> stopCounter());
        this.up.addActionListener(e -> this.counterAgent.countIncrementally());
        this.down.addActionListener(e -> this.counterAgent.countDecrementally());
        final StopCounterAgent stopCounterAgent = new StopCounterAgent();
        new Thread(this.counterAgent).start();
        new Thread(stopCounterAgent).start();
    }

    private void stopCounter() {
        this.counterAgent.stopCounting();
        this.stop.setEnabled(false);
        this.up.setEnabled(false);
        this.down.setEnabled(false);
    }

    private class CounterAgent implements Runnable {

        private int counter;
        private volatile boolean stop;
        private volatile boolean increment = true;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    final int counterCopy = this.counter;
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            AnotherConcurrentGUI.this.display.setText(Integer.toString(counterCopy));
                        }
                    });
                    this.counter += increment ? 1 : -1;
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }

        /**
         * External command to count incrementally.
         */
        public void countIncrementally() {
            this.increment = true;
        }

        /**
         * External command to count decrementally.
         */
        public void countDecrementally() {
            this.increment = false;
        }
    }

    private class StopCounterAgent implements Runnable {

        private static final int SECONDS_TO_WAIT = 10;
        private static final int S_TO_MS = 1000;

        @Override
        public void run() {
            try {
                Thread.sleep(SECONDS_TO_WAIT * S_TO_MS);
                SwingUtilities.invokeLater(() -> AnotherConcurrentGUI.this.stopCounter());
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}
