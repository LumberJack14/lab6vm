import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class Algorithm {

    private BiFunction<Double, Double, Double> equation;

    public void selectEquation(int choice) {
        switch (choice) {
            case 1:
                // y' = x + y
                equation = (x, y) -> x + y;
                break;
            case 2:
                // y' = x - y
                equation = (x, y) -> x - y;
                break;
            case 3:
                // y' = x * y
                equation = (x, y) -> x * y;
                break;
            default:
                throw new IllegalArgumentException("Invalid equation choice.");
        }
    }

    public List<Double> eulerMethod(double x0, double y0, double xn, double h, double epsilon) {
        List<Double> results = new ArrayList<>();
        double x = x0;
        double y = y0;
        results.add(y);

        while (x < xn) {
            double yEuler = y + h * equation.apply(x, y);
            double yHalfStep = y + (h / 2) * equation.apply(x, y);
            yHalfStep += (h / 2) * equation.apply(x + h / 2, yHalfStep);

            double error = Math.abs(yEuler - yHalfStep);
            if (error > epsilon) {
                System.out.println("Warning: ошибка превышает точность на шаге x = " + x);
            }

            y = yEuler;
            x += h;
            results.add(y);
        }

        return results;
    }

    public List<Double> rungeKuttaMethod(double x0, double y0, double xn, double h, double epsilon) {
        List<Double> results = new ArrayList<>();
        double x = x0;
        double y = y0;
        results.add(y);

        while (x < xn) {
            double k1 = h * equation.apply(x, y);
            double k2 = h * equation.apply(x + h / 2, y + k1 / 2);
            double k3 = h * equation.apply(x + h / 2, y + k2 / 2);
            double k4 = h * equation.apply(x + h, y + k3);

            double yRK4 = y + (k1 + 2 * k2 + 2 * k3 + k4) / 6;

            double kHalf1 = (h / 2) * equation.apply(x, y);
            double kHalf2 = (h / 2) * equation.apply(x + h / 4, y + kHalf1 / 2);
            double kHalf3 = (h / 2) * equation.apply(x + h / 4, y + kHalf2 / 2);
            double kHalf4 = (h / 2) * equation.apply(x + h / 2, y + kHalf3);
            double yHalfStep = y + (kHalf1 + 2 * kHalf2 + 2 * kHalf3 + kHalf4) / 6;

            double error = Math.abs(yRK4 - yHalfStep);
            if (error > epsilon) {
                System.out.println("Warning: ошибка превышает точность на шаге x = " + x);
            }

            y = yRK4;
            x += h;
            results.add(y);
        }

        return results;
    }

    public List<Double> milneMethod(double x0, double y0, double xn, double h, List<Double> initialValues) {
        List<Double> results = new ArrayList<>(initialValues);
        double x = x0 + 3 * h;
        double y;

        while (x < xn) {
            int n = results.size();
            double yPredictor = results.get(n - 4) + 4 * h / 3 *
                    (2 * equation.apply(x - h, results.get(n - 1))
                            - equation.apply(x - 2 * h, results.get(n - 2))
                            + 2 * equation.apply(x - 3 * h, results.get(n - 3)));

            double yCorrector = results.get(n - 2) + h / 3 *
                    (equation.apply(x + h, yPredictor)
                            + 4 * equation.apply(x, results.get(n - 1))
                            + equation.apply(x - h, results.get(n - 2)));

            y = yCorrector;
            x += h;
            results.add(y);
        }

        return results;
    }

    public List<Double> exactSolution(BiFunction<Double, Double, Double> exactFunction, double x0, double y0, double xn, double h) {
        List<Double> exactValues = new ArrayList<>();
        double C = y0 - exactFunction.apply(x0, 0.0);
        double x = x0;
        while (x <= xn) {
            exactValues.add(exactFunction.apply(x, C));
            x += h;
        }
        return exactValues;
    }
}

