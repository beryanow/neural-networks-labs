import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

public class Network {
    private int n1 = 17; // input neurons amount
    private int[] n1_array; // distinct values amount for each neuron from input layer
    private int n2 = 997; // hidden layer 1 neurons amount
    private int n3 = 93; // hidden layer 2 neurons amount
    private int n4 = 2; // output neurons amount
    private int epochs = 128; // epochs processing amount
    private double learning_rate = 0.001; // learning rate value
    private double momentum = 0.1; // back propagation optimization momentum value

    // w* -> layer * weights
    // t* -> layer * gradient
    // d* -> layer * delta weights
    // out* -> layer * neurons' outputs
    // in* -> layer * neurons' inputs

    // input layer -> hidden layer 1
    private double[][] w1;
    private double[][] d1;
    private double[] out1;

    // hidden layer 1 -> hidden layer 2
    private double[][] w2;
    private double[][] d2;
    private double[] t2;
    private double[] in2;
    private double[] out2;

    // hidden layer 2 -> output layer
    private double[][] w3;
    private double[][] d3;
    private double[] t3;
    private double[] in3;
    private double[] out3;

    // output layer
    private double[] t4;
    private double[] in4;
    private double[] out4;

    // expected example values
    private double[] expected;

    void test() {
        Scanner scanner = null;
        try {
            scanner = new Scanner(Paths.get("data_test.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringBuilder kgf_str = new StringBuilder();
        StringBuilder g_total_str = new StringBuilder();

        while (scanner.hasNextLine()) {
            String input_info = scanner.nextLine();
            add_input_info(input_info);
            perceptron();
            double G_total = -Math.log((1 - out4[1]) / out4[1]) * 8.15 + 2.78;
            double KGF = -Math.log((1 - out4[2]) / out4[2]) * 254.42 + 131;

            double G_total_real = -Math.log((1 - expected[1]) / expected[1]) * 8.15 + 2.78;
            double KGF_real = -Math.log((1 - expected[2]) / expected[2]) * 254.42 + 131;


            g_total_str.append(G_total).append(" ").append(G_total_real).append('\n');
            kgf_str.append(KGF).append(" ").append(KGF_real).append('\n');
        }
        System.out.println(g_total_str);
        System.out.println("lol");
        System.out.println(kgf_str);
    }

    double square_error() {
        double error = 0.0;

        for (int i = 1; i <= n4; i++) {
            if (expected[i] == -1) {
                continue;
            }
            error += (out4[i] - expected[i]) * (out4[i] - expected[i]);
        }
        error *= 0.5;

        return error;
    }

    void back_propagation() {
        double sum;

        // hidden layer 2 -> output layer gradient
        for (int i = 1; i <= n4; i++) {
            if (expected[i] != -1) {
                t4[i] = out4[i] * (1 - out4[i]) * (expected[i] - out4[i]);
            } else {
                t4[i] = 0;
            }
        }

        // hidden layer 1 -> hidden layer 2 gradient
        for (int i = 1; i <= n3; i++) {
            sum = 0.0;
            for (int j = 1; j <= n4; j++) {
                sum += w3[i][j] * t4[j];
            }
            t3[i] = out3[i] * (1 - out3[i]) * sum;
        }

        // input layer -> hidden layer 1 gradient
        for (int i = 1; i <= n2; i++) {
            sum = 0.0;
            for (int j = 1; j <= n3; j++) {
                sum += w2[i][j] * t3[j];
            }
            t2[i] = out2[i] * (1 - out2[i]) * sum;
        }

        // hidden layer 2 -> output layer delta rule update weights
        for (int i = 1; i <= n3; i++) {
            for (int j = 1; j <= n4; j++) {
                d3[i][j] = (learning_rate * t4[j] * out3[i]) + (momentum * d3[i][j]);
                w3[i][j] += d3[i][j];
            }
        }

        // hidden layer 1 -> hidden layer 2 delta rule update weights
        for (int i = 1; i <= n2; i++) {
            for (int j = 1; j <= n3; j++) {
                d2[i][j] = (learning_rate * t3[j] * out2[i]) + +(momentum * d2[i][j]);
                w2[i][j] += d2[i][j];
            }
        }

        // input layer -> hidden layer 1 delta rule update weights
        int shift = 0;
        for (int i = 1; i <= n1; i++) {
            for (int j = shift + 1; j <= n1_array[i - 1] + shift; j++) {
                d1[i][j - shift] = (learning_rate * t2[j] * out1[i]) + (momentum * d1[i][j - shift]);
                w1[i][j - shift] += d1[i][j - shift];
            }
            shift += n1_array[i - 1];
        }
    }

    double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    void perceptron() {
        for (int i = 1; i <= n2; ++i) {
            in2[i] = 0.0;
        }

        for (int i = 1; i <= n3; ++i) {
            in3[i] = 0.0;
        }

        for (int i = 1; i <= n4; ++i) {
            in4[i] = 0.0;
        }

        // input layer -> hidden layer 1
        int shift = 0;
        for (int i = 1; i <= n1; i++) {
            for (int j = shift + 1; j <= n1_array[i - 1] + shift; j++) {
                in2[j] = out1[i] * w1[i][j - shift];
            }
            shift += n1_array[i - 1];
        }

        for (int i = 1; i <= n2; i++) {
            out2[i] = sigmoid(in2[i]);
        }

        // hidden layer 1 -> hidden layer 2
        for (int j = 1; j <= n3; j++) {
            for (int i = 1; i <= n2; i++) {
                in3[j] += out2[i] * w2[i][j];
            }
        }

        for (int i = 1; i <= n3; ++i) {
            out3[i] = sigmoid(in3[i]);
        }

        // hidden layer 2 -> output layer
        for (int j = 1; j <= n4; j++) {
            for (int i = 1; i <= n3; i++) {
                in4[j] += out3[i] * w3[i][j];
            }
        }

        for (int i = 1; i <= n4; ++i) {
            out4[i] = sigmoid(in4[i]);
        }
    }

    void learning_process() {
        for (int i = 1; i <= n1; i++) {
            for (int j = 1; j <= n1_array[i - 1]; j++) {
                d1[i][j] = 0.0;
            }
        }

        for (int i = 1; i <= n2; ++i) {
            for (int j = 1; j <= n3; ++j) {
                d2[i][j] = 0.0;
            }
        }

        for (int i = 1; i <= n3; ++i) {
            for (int j = 1; j <= n4; ++j) {
                d3[i][j] = 0.0;
            }
        }

        perceptron();
        back_propagation();
    }

    void process() {
        Scanner scanner = null;

        for (int i = 1; i <= epochs; i++) {

            try {
                scanner = new Scanner(Paths.get("data_learn.txt"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (scanner.hasNextLine()) {
                String input_info = scanner.nextLine();
                add_input_info(input_info);
                learning_process();
//                if (i == 1) {
//                    System.out.println(square_error());
//                }
//                if (i + 1 == epochs) {
//                    System.out.println();
//                }
//                if (i == epochs) {
//                    System.out.println(square_error());
//                }
            }
        }
//        for (int i = 1; i <= n1; ++i) {
//            for (int j = 1; j <= n1_array[i - 1]; j++) {
//                System.out.print(w1[i][j] + " ");
//            }
//            System.out.println();
//        }
    }

    void add_input_info(String input_info) {
        String[] input_values = input_info.split("[ \t]");

        for (int i = 1; i <= n1; i++) {
            if (input_values[i - 1].equals("-")) {
                out1[i] = 0;
            } else {
                out1[i] = Double.parseDouble(input_values[i - 1]);
            }
        }

        for (int i = 1; i <= n4; i++) {
            if (input_values[n1 - i + 1].equals("-")) {
                expected[i] = -1;
            } else {
                expected[i] = sigmoid(Double.parseDouble(input_values[n1 + i - 1]));
            }
        }
    }

    void initialize() {
        // input layer -> hidden layer 1
        w1 = new double[n1 + 1][];
        d1 = new double[n1 + 1][];
        n1_array = new int[]{35, 8, 109, 32, 34, 42, 38, 8, 36, 160, 115, 12, 171, 131, 23, 23, 20};

        for (int i = 1; i <= n1; i++) {
            w1[i] = new double[n1_array[i - 1] + 1];
            d1[i] = new double[n1_array[i - 1] + 1];
        }

        out1 = new double[n1 + 1];

        // hidden layer 1 -> hidden layer 2
        w2 = new double[n2 + 1][];
        d2 = new double[n2 + 1][];

        for (int i = 1; i <= n2; ++i) {
            w2[i] = new double[n3 + 1];
            d2[i] = new double[n3 + 1];
        }

        in2 = new double[n2 + 1];
        out2 = new double[n2 + 1];
        t2 = new double[n2 + 1];

        // hidden layer 2 -> output layer
        w3 = new double[n3 + 1][];
        d3 = new double[n3 + 1][];

        for (int i = 1; i <= n3; ++i) {
            w3[i] = new double[n4 + 1];
            d3[i] = new double[n4 + 1];
        }

        in3 = new double[n3 + 1];
        out3 = new double[n3 + 1];
        t3 = new double[n3 + 1];

        // output layer
        in4 = new double[n4 + 1];
        out4 = new double[n4 + 1];
        t4 = new double[n4 + 1];
        expected = new double[n4 + 1];

        // initialization for weights from input layer -> hidden layer 1
        for (int i = 1; i <= n1; ++i) {
            for (int j = 1; j <= n1_array[i - 1]; j++) {
                int sign = (((int) (Math.random() * 32767)) % 2);

                w1[i][j] = (double) (((int) (Math.random() * 32767)) % 3) / 10.0;
                if (sign == 1) {
                    w1[i][j] = -w1[i][j];
                }
            }
        }

        // initialization for weights from hidden layer 1 -> hidden layer 2
        for (int i = 1; i <= n2; ++i) {
            for (int j = 1; j <= n3; ++j) {
                int sign = (((int) (Math.random() * 32767)) % 2);

                w2[i][j] = (double) (((int) (Math.random() * 32767)) % 6) / 10.0;
                if (sign == 1) {
                    w2[i][j] = -w2[i][j];
                }
            }
        }

        // initialization for weights from hidden layer 2 -> output layer
        for (int i = 1; i <= n3; ++i) {
            for (int j = 1; j <= n4; ++j) {
                int sign = (((int) (Math.random() * 32767)) % 2);

                w3[i][j] = (double) (((int) (Math.random() * 32767)) % 9) / 10.0;
                if (sign == 1) {
                    w3[i][j] = -w3[i][j];
                }
            }
        }
    }
}
