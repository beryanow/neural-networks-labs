import java.io.Serializable;

abstract class Layer implements Serializable {
    private static final long serialVersionUID = 9L;

    int planes_amount;
    int plane_size;
    int receptive_field_size;

    Layer(int planes_amount, int plane_size, int receptive_field_size) {
        this.planes_amount = planes_amount;
        this.plane_size = plane_size;
        this.receptive_field_size = receptive_field_size;
    }

    double[][] initialize_mexican_hat_weights() {
        double[][] weights = new double[receptive_field_size][receptive_field_size];
        int half_size = receptive_field_size / 2;
        for (int i = -half_size; i <= half_size; i++) {
            for (int j = -half_size; j <= half_size; j++) {
                double squared_t = i * i + j * j;
                weights[i + half_size][j + half_size] = (squared_t - 1) * Math.exp(-squared_t / 2);
            }
        }
        return weights;
    }

    abstract void propagate(double[][][] image, double[][] v_output, double[][][] s_output, boolean train);
    abstract double[][] get_v_output();
    abstract double[][][] get_s_output();
    abstract double[][][] get_c_output();
}

class Simple_Layer extends Layer implements Serializable {
    private static final long serialVersionUID = 2L;

    Simple_Cell[][][] simple_cells;
    double[][] c_weights;
    double[] b_weights;
    double q;
    double θ;

    Simple_Layer(int planes_amount, int simple_plane_size, int receptive_field_size, int previous_layer_planes_amount, double θ, double q) {
        super(planes_amount, simple_plane_size, receptive_field_size);
        c_weights = initialize_mexican_hat_weights();
        simple_cells = new Simple_Cell[planes_amount][simple_plane_size][simple_plane_size];
        for (int i = 0; i < planes_amount; i++) {
            for (int j = 0; j < simple_plane_size; j++) {
                for (int l = 0; l < simple_plane_size; l++) {
                    simple_cells[i][j][l] = new Simple_Cell(previous_layer_planes_amount, receptive_field_size);
                }
            }
        }
        b_weights = new double[planes_amount];
        this.θ = θ;
        this.q = q;
    }

    void hebb_rule_delta_a_evaluation(double[][][] image, int shift_x, int shift_y, int plane_number) {
        for (int k = 0; k < image.length; k++) {
            double[][] delta_a = new double[receptive_field_size][receptive_field_size];
            for (int x = 0; x < receptive_field_size; x++) {
                for (int y = 0; y < receptive_field_size; y++) {
                    double image_value = 0;
                    try {
                        image_value = image[k][shift_x + x - receptive_field_size / 2][shift_y + y - receptive_field_size / 2];
                    } catch (ArrayIndexOutOfBoundsException ignored) {}

                    // evaluating delta "a weights"
                    delta_a[x][y] = q * c_weights[x][y] * image_value;
                }
            }

            // changing "a weights" according to the plane number which image is involved in connection area extraction process
            simple_cells[plane_number][shift_x][shift_y].change_a_weights(delta_a, k);

            // giving neighbour cells the same "a weights" that the main cell owns now
            double[][][] changed_a_weights = simple_cells[plane_number][shift_x][shift_y].get_a_weights();
            for (int x = 0; x < receptive_field_size; x++) {
                for (int y = 0; y < receptive_field_size; y++) {
                    simple_cells[plane_number][shift_x + x - receptive_field_size / 2][shift_y + y - receptive_field_size / 2].set_a_weights(changed_a_weights);
                }
            }
        }
    }

    void evaluate_behaviour(double[][][] image, int shift_x, int shift_y, int plane_number, double[][] v_output) {
        double sum = 0;
        for (int k = 0; k < image.length; k++) {
            // observing floating window field
            double[][] image_window = new double[receptive_field_size][receptive_field_size];
            for (int x = 0; x < receptive_field_size; x++) {
                for (int y = 0; y < receptive_field_size; y++) {
                    image_window[x][y] = 0;
                    try {
                        image_window[x][y] = image[k][shift_x + x - receptive_field_size / 2][shift_y + y - receptive_field_size / 2];
                    } catch (ArrayIndexOutOfBoundsException ignored) {}
                }
            }

            // evaluating simple cell output primary sum
            sum += simple_cells[plane_number][shift_x][shift_y].multiply(image_window, k);
        }

        // preparing for activation
        sum = (1 + sum) / (1 + θ * b_weights[plane_number] * v_output[shift_x][shift_y]) - 1;

        // activating using ReLU method
        if (sum < 0) {
            sum = 0;
        }

        // proceeding last step of simple cell output evaluation
        sum *= θ / (1 - θ);

        simple_cells[plane_number][shift_x][shift_y].set_output(sum);
    }

    double hebb_rule_b_evaluation(int plane_number, int x, int y) {
        double value = simple_cells[plane_number][x][y].evaluate_b_weights(c_weights);
        return Double.isNaN(value) ? 0 : value;
    }

    @Override
    void propagate(double[][][] image, double[][] v_output, double[][][] s_output, boolean train) {
        int starting_point = receptive_field_size / 2;
        int amount = plane_size / receptive_field_size;

        if (train) {
            int plane_number = 0;
            for (int i = starting_point; i <= starting_point + receptive_field_size * (amount - 1); i += receptive_field_size) {
                for (int j = starting_point; j <= starting_point + receptive_field_size * (amount - 1); j += receptive_field_size) {
                    hebb_rule_delta_a_evaluation(image, i, j, plane_number);
                    b_weights[plane_number] = hebb_rule_b_evaluation(plane_number, i, j);
                    plane_number++;
                }
            }
        }

        for (int k = 0; k < simple_cells.length; k++) {
            for (int i = 0; i < plane_size; i++) {
                for (int j = 0; j < plane_size; j++) {
                    evaluate_behaviour(image, i, j, k, v_output);
                }
            }
        }
    }

    @Override
    double[][] get_v_output() {
        return null;
    }

    @Override
    double[][][] get_s_output() {
        double[][][] s_output = new double[simple_cells.length][plane_size][plane_size];
        for (int k = 0; k < simple_cells.length; k++) {
            for (int i = 0; i < plane_size; i++) {
                for (int j = 0; j < plane_size; j++) {
                   s_output[k][i][j] = simple_cells[k][i][j].get_output();
                }
            }
        }
        return s_output;
    }

    @Override
    double[][][] get_c_output() {
        return null;
    }
}

class Complex_Layer extends Layer implements Serializable {
    private static final long serialVersionUID = 3L;

    double[][] d_weights;
    double[][][] c_output;

    Complex_Layer(int planes_amount, int complex_plane_size, int receptive_field_size) {
        super(planes_amount, complex_plane_size, receptive_field_size);
        d_weights = initialize_mexican_hat_weights();
        c_output = new double[planes_amount][plane_size][plane_size];
    }

    void make_pooling(double[][] image, int plane_number, int shift_x, int shift_y, int shift) {
        double value = 0;
        for (int x = 0; x < receptive_field_size; x++) {
            for (int y = 0; y < receptive_field_size; y++) {
                // observing floating window cell value
                double image_value = 0;
                try {
                    image_value = image[shift_x + x - receptive_field_size / 2][shift_y + y - receptive_field_size / 2];
                } catch (ArrayIndexOutOfBoundsException ignored) {}

                value += d_weights[x][y] * image_value;
            }
        }

        value = (value < 0 ? 0 : value) / (1 + (value < 0 ? 0 : value));
        c_output[plane_number][shift_x - shift][shift_y - shift] = value;
    }

    @Override
    void propagate(double[][][] image, double[][] v_output, double[][][] s_output, boolean train) {
        int shift = (s_output[0].length - plane_size) / 2;

        for (int k = 0; k < s_output.length; k++) {
            for (int i = 0; i < plane_size; i++) {
                for (int j = 0; j < plane_size; j++) {
                    make_pooling(s_output[k], k, i + shift, j + shift, shift);
                }
            }
        }
    }

    @Override
    double[][] get_v_output() {
        return null;
    }

    @Override
    double[][][] get_s_output() {
        return null;
    }

    @Override
    double[][][] get_c_output() {
        return c_output;
    }

}

class Inhibitory_Layer extends Layer implements Serializable{
    private static final long serialVersionUID = 4L;

    double[][] v_output;
    double[][] c_weights;

    Inhibitory_Layer(int inhibitory_plane_size, int receptive_field_size) {
        super(1, inhibitory_plane_size, receptive_field_size);
        v_output = new double[plane_size][plane_size];
        c_weights = initialize_mexican_hat_weights();
    }

    // evaluating inhibitory cells output
    double evaluate_behaviour(double[][][] image, int shift_x, int shift_y) {
        double value = 0;
        for (int k = 0; k < image.length; k++) {
            for (int x = 0; x < receptive_field_size; x++) {
                for (int y = 0; y < receptive_field_size; y++) {
                    // observing floating window cell value
                    double image_value = 0;
                    try {
                        image_value = image[k][shift_x + x - receptive_field_size / 2][shift_y + y - receptive_field_size / 2];
                    } catch (ArrayIndexOutOfBoundsException ignored) {}

                    value += c_weights[x][y] * image_value * image_value;
                }
            }
        }

        return Math.sqrt(value);
    }

    @Override
    void propagate(double[][][] image, double[][] v_output, double[][][] s_output, boolean train) {
        for (int i = 0; i < plane_size; i++) {
            for (int j = 0; j < plane_size; j ++) {
                double value = evaluate_behaviour(image, i, j);
                this.v_output[i][j] = Double.isNaN(value) ? 0 : value;
            }
        }
    }

    @Override
    double[][] get_v_output() {
        return v_output;
    }

    @Override
    double[][][] get_s_output() {
        return null;
    }

    @Override
    double[][][] get_c_output() {
        return null;
    }
}
