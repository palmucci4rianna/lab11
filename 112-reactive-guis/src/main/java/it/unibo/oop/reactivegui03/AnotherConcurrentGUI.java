package it.unibo.oop.reactivegui03;

import java.io.Serial;

import javax.swing.JFrame;
import it.unibo.oop.JFrameUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.w3c.dom.css.Counter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
//import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Third experiment with reactive gui.
 */
public final class AnotherConcurrentGUI extends JFrame {

    private static final long TIME_10_SECONDS = 10_000;

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(AnotherConcurrentGUI.class);

    private final JLabel display = new JLabel();
    private final JButton stop = new JButton("stop");
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");

    private final CounterAgent counteragent = new CounterAgent();

    /**
     * Builds a new CGUI.
     */
    public AnotherConcurrentGUI() {
        super();
        JFrameUtil.dimensionJFrame(this);
        final JPanel panel = new JPanel();
        panel.add(display);
        panel.add(stop);
        panel.add(up);
        panel.add(down);
        this.getContentPane().add(panel);
        this.setVisible(true);
        up.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                counteragent.countUp();
            }
        });
        down.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                counteragent.countDown();
            }
        });
        stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                AnotherConcurrentGUI.this.stopCounting();
            }
        });
        new Thread(counteragent).start();
        new Thread(() -> {
            try {
                Thread.sleep(TIME_10_SECONDS);
            } catch (final InterruptedException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
            stopCounting();
        }).start();
    }

    private void stopCounting() {
        counteragent.stopCounting();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                stop.setEnabled(false);
                up.setEnabled(false);
                down.setEnabled(false);
            }
        });
    }

    private final class CounterAgent implements Runnable, Serializable {

        @Serial
        private static final long serialVersionUID = 1L;
        private volatile boolean stop;
        private volatile boolean up = true;
        private volatile int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    SwingUtilities.invokeAndWait(() -> display.setText(Integer.toString(counter)));
                    counter += up ? 1 : -1;
                    Thread.sleep(100);
                } catch (final InvocationTargetException | InterruptedException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                    Thread.currentThread().interrupt();
                }
            }
        }

        public void stopCounting() {
            this.stop = true;
        }

        public void countUp() {
            this.up = true;
        }

        public void countDown() {
            this.up = false;
        }
    }
}
