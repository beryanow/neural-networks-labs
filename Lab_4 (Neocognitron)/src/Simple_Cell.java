import java.io.Serializable;

class Simple_Cell implements Serializable {
    private static final long serialVersionUID = 5L;

    double[][][] a_weights;
    double output;

    Simple_Cell(int previous_layer_planes_amount, int receptive_field_size) {
        a_weights = new double[previous_layer_planes_amount][receptive_field_size][receptive_field_size];
        for (int k = 0; k < previous_layer_planes_amount; k++) {
            for (int i = 0; i < receptive_field_size; i++) {
                for (int j = 0; j < receptive_field_size; j++) {
                    a_weights[k][i][j] = 0.0;
                }
            }
        }
    }

    void change_a_weights(double[][] delta_a, int previous_layer_plane) {
        for (int i = 0; i < delta_a.length; i++) {
            for (int j = 0; j < delta_a[0].length; j++) {
                a_weights[previous_layer_plane][i][j] += delta_a[i][j];
            }
        }
    }

    double[][][] get_a_weights() {
        return a_weights;
    }

    void set_a_weights(double[][][] a_weights) {
        this.a_weights = a_weights;
    }

    void set_output(double value) {
        output = value;
    }

    double get_output() {
        return output;
    }

    double evaluate_b_weights(double[][] c_weights) {
        double value = 0;
        for (int k = 0; k < a_weights.length; k++) {
            for (int i = 0; i < a_weights[0].length; i++) {
                for (int j = 0; j < a_weights[0][0].length; j++) {
                    if (c_weights[i][j] == 0) {
                        continue;
                    }
                    value += a_weights[k][i][j] * a_weights[k][i][j] / c_weights[i][j];
                }
            }
        }
        return Math.sqrt(value);
    }

    double multiply(double[][] image_window, int plane_number) {
        double value = 0;
        for (int i = 0; i < a_weights[plane_number].length; i++) {
            for (int j = 0; j < a_weights[plane_number][0].length; j++) {
                value += a_weights[plane_number][i][j] * image_window[i][j];
            }
        }
        return value;
    }
}
