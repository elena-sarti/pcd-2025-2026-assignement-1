package assignment2Extension;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

import javax.swing.*;
import java.awt.*;

public class GUI {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        FSStatExtension lib = new FSStatExtension(vertx);

        JFrame frame = new JFrame("GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Margine interno

        Dimension fieldSize = new Dimension(400, 25);

        JLabel label1 = new JLabel("Insert directory to inspect:");
        label1.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField dir = new JTextField();
        dir.setMaximumSize(fieldSize);
        dir.setAlignmentX(Component.CENTER_ALIGNMENT);
        JScrollPane jScrollPane = new JScrollPane(dir);
        jScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        JLabel label2 = new JLabel("Insert the maximum possible file dimension:");
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
        JTextArea results = new JTextArea(8, 30);
        results.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(results);
        scrollPane.setMaximumSize(new Dimension(500, 150));
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);

        startButton.addActionListener(startPressed -> {
            lib.setStopped(false);
            String d = dir.getText();
            results.append("Generating report for directory "+d+" :\n");
            int maxSize = Integer.parseInt(maxFS.getText());
            int numBands = Integer.parseInt(nB.getText());
            long timer = vertx
                        .setPeriodic(100, t -> {
                            String lastFile = lib.getLastFileFound();
                            if (lastFile != null && !lib.getStopped()) {
                                SwingUtilities.invokeLater(() -> {
                                    results.append(lastFile + "\n");
                                });
                            }
                            if (lib.getStopped()) {
                                vertx.cancelTimer(t);
                            }
                        });
            lib
            .getFSReport(d, maxSize, numBands)
            .onSuccess(report -> {
                vertx.cancelTimer(timer);
                if(!lib.getStopped()) {
                    results.append(report.toString()+"\n");
                }
            });
        });

        stopButton.addActionListener(stopPressed -> {
            lib.setStopped(true);
            results.append("Stopped report generation for directory "+ dir.getText() +".\n");
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
        mainPanel.add(scrollPane);

        frame.add(mainPanel);
        frame.setVisible(true);
    }
}

