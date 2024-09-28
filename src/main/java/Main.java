import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.BiFunction;

public class Main {
    JFrame frame;
    ConsolePanel consolePanel;
    JComboBox<String> equationComboBox;
    Algorithm algorithm;
    ChartPanel chartPanel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }

    public Main() {
        algorithm = new Algorithm();

        frame = new JFrame("LAB 6");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel mainPanel = createMainPanel();
        frame.add(mainPanel);
        frame.setVisible(true);
    }

    public JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel chartPanel = createChartPanel();
        JPanel inputPanel = createInputPanel();

        mainPanel.add(chartPanel, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.EAST);

        return mainPanel;
    }

    public JPanel createChartPanel() {
        XYSeries emptySeries = new XYSeries("Точное решение (заглушка)");
        emptySeries.add(0, 0);

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(emptySeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "График решений (ожидание данных)",
                "x", "y",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        return chartPanel;
    }

    public JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1;
        gbc.gridx = 0;

        JLabel equationLabel = new JLabel("Выберите уравнение:");
        gbc.gridy = 0;
        inputPanel.add(equationLabel, gbc);

        equationComboBox = new JComboBox<>(new String[]{
                "y' = x + y", "y' = x - y", "y' = x * y"
        });
        gbc.gridy = 1;
        inputPanel.add(equationComboBox, gbc);

        gbc.gridy = 2;
        JLabel x0Label = new JLabel("x0:");
        inputPanel.add(x0Label, gbc);
        gbc.gridy = 3;
        JTextField x0Field = new JTextField("0.0");
        inputPanel.add(x0Field, gbc);

        gbc.gridy = 4;
        JLabel y0Label = new JLabel("y0:");
        inputPanel.add(y0Label, gbc);
        gbc.gridy = 5;
        JTextField y0Field = new JTextField("1.0");
        inputPanel.add(y0Field, gbc);

        gbc.gridy = 6;
        JLabel xnLabel = new JLabel("xn:");
        inputPanel.add(xnLabel, gbc);
        gbc.gridy = 7;
        JTextField xnField = new JTextField("2.0");
        inputPanel.add(xnField, gbc);

        gbc.gridy = 8;
        JLabel hLabel = new JLabel("Шаг h:");
        inputPanel.add(hLabel, gbc);
        gbc.gridy = 9;
        JTextField hField = new JTextField("0.1");
        inputPanel.add(hField, gbc);

        gbc.gridy = 10;
        JLabel epsilonLabel = new JLabel("Точность ε:");
        inputPanel.add(epsilonLabel, gbc);
        gbc.gridy = 11;
        JTextField epsilonField = new JTextField("0.00001");
        inputPanel.add(epsilonField, gbc);

        gbc.gridy = 12;
        JLabel methodLabel = new JLabel("Выберите метод:");
        inputPanel.add(methodLabel, gbc);

        JComboBox<String> methodComboBox = new JComboBox<>(new String[]{
                "Метод Эйлера", "Метод Рунге-Кутта", "Метод Милна"
        });
        gbc.gridy = 13;
        inputPanel.add(methodComboBox, gbc);

        consolePanel = new ConsolePanel();
        gbc.gridy = 14;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        inputPanel.add(consolePanel, gbc);

        JButton calculateButton = new JButton("Вычислить");
        gbc.gridy = 15;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        inputPanel.add(calculateButton, gbc);

        calculateButton.addActionListener(e -> {
            try {
                double x0 = Double.parseDouble(x0Field.getText());
                double y0 = Double.parseDouble(y0Field.getText());
                double xn = Double.parseDouble(xnField.getText());
                double h = Double.parseDouble(hField.getText());
                double epsilon = Double.parseDouble(epsilonField.getText());
                int equationChoice = equationComboBox.getSelectedIndex();
                String method = (String) methodComboBox.getSelectedItem();

                BiFunction<Double, Double, Double> exactFunction = null;
                switch (equationChoice) {
                    case 0:  // y' = x + y
                        exactFunction = (x, C) -> C * Math.exp(x) - x - 1;
                        break;
                    case 1:  // y' = x - y
                        exactFunction = (x, C) -> C * Math.exp(-x) + x - 1;
                        break;
                    case 2:  // y' = x * y
                        exactFunction = (x, C) -> C * Math.exp(x * x / 2);
                        break;
                    default:
                        throw new IllegalStateException("Неизвестное уравнение");
                }

                if (exactFunction == null) {
                    throw new NullPointerException("Функция точного решения не определена.");
                }

                algorithm.selectEquation(equationChoice + 1);
                List<Double> exactSolution = algorithm.exactSolution(exactFunction, x0, y0, xn, h);

                List<Double> numericSolution = null;
                if (method.equals("Метод Эйлера")) {
                    numericSolution = algorithm.eulerMethod(x0, y0, xn, h, epsilon);
                } else if (method.equals("Метод Рунге-Кутта")) {
                    numericSolution = algorithm.rungeKuttaMethod(x0, y0, xn, h, epsilon);
                } else if (method.equals("Метод Милна")) {
                    List<Double> initialValues = algorithm.rungeKuttaMethod(x0, y0, x0 + 3 * h, h, epsilon);
                    numericSolution = algorithm.milneMethod(x0, y0, xn, h, initialValues);
                }

                consolePanel.print("x\tТочное решение\tЧисленное решение");
                double x = x0;
                for (int i = 0; i < exactSolution.size(); i++) {
                    consolePanel.print(String.format("%.2f\t%.5f\t\t%.5f", x, exactSolution.get(i), numericSolution.get(i)));
                    x += h;
                }

                updateChart(x0, xn, h, exactSolution, numericSolution);

            } catch (Exception ex) {
                consolePanel.print("Ошибка ввода данных: " + ex.getMessage());
            }
        });

        return inputPanel;
    }

    public void updateChart(double x0, double xn, double h, List<Double> exactSolution, List<Double> numericSolution) {
        XYSeries exactSeries = new XYSeries("Точное решение");
        XYSeries numericSeries = new XYSeries("Численное решение");

        double x = x0;
        int steps = exactSolution.size();
        for (int i = 0; i < steps; i++) {
            exactSeries.add(x, exactSolution.get(i));
            numericSeries.add(x, numericSolution.get(i));
            x += h;
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(exactSeries);
        dataset.addSeries(numericSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "График решений",
                "x", "y",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        chartPanel.setChart(chart);
    }
}
