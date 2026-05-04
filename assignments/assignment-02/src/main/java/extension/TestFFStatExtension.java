package extension;

import io.vertx.core.Vertx;

import javax.swing.*;
import java.awt.*;

public class TestFFStatExtension {
    static Vertx vertx = Vertx.vertx();
    static FSStatExtension lib = new FSStatExtension(vertx);
    static long timer;

    public static void gui(){
        JFrame frame = new JFrame("GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 700);
        frame.setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        Dimension fieldSize = new Dimension(400, 40);

        JLabel label1 = new JLabel("Insert directory to inspect:");
        label1.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField dir = new JTextField();
        dir.setMaximumSize(fieldSize);
        dir.setAlignmentX(Component.CENTER_ALIGNMENT);
        JScrollPane jScrollPane = new JScrollPane(dir);
        jScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        JLabel label2 = new JLabel("Insert the maximum file dimension (in kB) possible:");
        label2.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField maxFS = new JTextField();
        maxFS.setMaximumSize(fieldSize);
        maxFS.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label3 = new JLabel("Insert the number of bands for the distribution of file sizes:");
        label3.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField nB = new JTextField();
        nB.setMaximumSize(fieldSize);
        nB.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton startButton = new JButton("Start");
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton stopButton = new JButton("Stop");
        stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label4 = new JLabel("Current results:");
        label4.setAlignmentX(Component.CENTER_ALIGNMENT);
        IstogramPanel istogram = new IstogramPanel(new int[0]);

        JLabel label5 = new JLabel("Result:");
        label5.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextArea results = new JTextArea(2, 10);
        JScrollPane jScrollPane2 = new JScrollPane(results);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane2.setAlignmentX(Component.CENTER_ALIGNMENT);

        startButton.addActionListener(startPressed -> {
            lib.setStopped(false);
            String d = dir.getText();
            int maxSize = Integer.parseInt(maxFS.getText());
            int numBands = Integer.parseInt(nB.getText());
            results.append("Generating report for directory " + d + ":\n");
            istogram.updateIstogram(new int[numBands + 1]);
            timer = vertx
                    .setPeriodic(30, t -> {
                        Report lastUpdate = lib.getLastUpdate();
                        if (!lib.getStopped()) {
                            SwingUtilities.invokeLater(() -> {
                                istogram.updateIstogram(lastUpdate.fileSizesDistribution());
                            });
                        } else {
                            vertx.cancelTimer(t);
                        }
                    });
            lib
            .getFSReport(d, maxSize, numBands)
            .onSuccess(report -> {
                vertx.cancelTimer(timer);
                if (!lib.getStopped()) {
                    SwingUtilities.invokeLater(() -> {
                        istogram.updateIstogram(lib.getLastUpdate().fileSizesDistribution());
                        results.append(report.toString() + "\n");
                        lib.setStopped(true);
                    });
                }
            })
            .onFailure(err -> {
                vertx.cancelTimer(timer);
            });
        });

        stopButton.addActionListener(stopPressed -> {
            if (!lib.getStopped()) {
                lib.setStopped(true);
                SwingUtilities.invokeLater(() -> {
                    results.append("Stopped report generation for directory "+ dir.getText() +".\n");
                });
            }
        });

        mainPanel.add(label1);
        mainPanel.add(jScrollPane);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        mainPanel.add(label2);
        mainPanel.add(maxFS);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        mainPanel.add(label3);
        mainPanel.add(nB);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        mainPanel.add(startButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(stopButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        mainPanel.add(label4);
        mainPanel.add(istogram);
        mainPanel.add(Box.createVerticalGlue());

        mainPanel.add(label5);
        mainPanel.add(jScrollPane2);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    static class IstogramPanel extends JPanel {
        private int[] reportData;

        public IstogramPanel(int[] reportData) {
            this.reportData = reportData;
            setBackground(Color.WHITE);
        }

        public void updateIstogram(int[] updatedData) {
            this.reportData = updatedData;
            this.repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(500, 250);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            int margin = 40;
            int graphWidth = getWidth() - 2 * margin;
            int graphHeight = getHeight() - 2 * margin;

            if (reportData == null || reportData.length == 0) return;

            int max = 0;
            for (int v : reportData) if (v > max) max = v;

            int barWidth = graphWidth / reportData.length;

            for (int i = 0; i < reportData.length; i++) {
                int barHeight = (int) ((double) reportData[i] / max * graphHeight);

                int x = margin + i * barWidth;
                int y = getHeight() - margin - barHeight;

                g2.setColor(new Color(70, 130, 180));
                g2.fillRect(x + 5, y, barWidth - 10, barHeight);

                g2.setColor(Color.DARK_GRAY);
                g2.drawRect(x + 5, y, barWidth - 10, barHeight);

                g2.setColor(Color.BLACK);
                g2.drawString(String.valueOf(reportData[i]), x + (barWidth / 2) - 10, y - 5);

                g2.drawString("B" + i, x + (barWidth / 2) - 10, getHeight() - margin + 15);
            }

            g2.drawLine(margin, getHeight() - margin, getWidth() - margin, getHeight() - margin);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TestFFStatExtension::gui);
    }
}

