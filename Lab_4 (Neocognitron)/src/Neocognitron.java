import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Neocognitron implements Serializable {
    private static final long serialVersionUID = 1L;
    private int[][] prediction; // output -> real amounts
    private HashMap<Integer, Integer> final_prediction; // output -> real prediction

    private double[][] image;
    private Layer[][] stage;

    void set_image(BufferedImage image) {
        this.image = parse_image(image);
    }

    Neocognitron() {
        stage = new Layer[4][3];
        prediction = new int[9][9];
        final_prediction = new HashMap<>();

        int[] planes_amount = {16, 16, 16, 9};
        int[] simple_plane_sizes = {28, 20, 12, 10};
        int[] complex_plane_sizes = {20, 12, 10, 1};
        int[] simple_receptive_field_sizes = {7, 5, 3, 3};
        int[] complex_receptive_field_sizes = {7, 5, 3, 9};
        double[] θ = {0.55, 0.51, 0.58, 0.3};
        double[] q = {100, 100, 100, 100};

        for (int i = 0; i < 4; i++) {
            stage[i][0] = new Simple_Layer(planes_amount[i], simple_plane_sizes[i], simple_receptive_field_sizes[i], (i == 0) ? 1 : planes_amount[i - 1], θ[i], q[i]);
            stage[i][1] = new Complex_Layer(planes_amount[i], complex_plane_sizes[i], complex_receptive_field_sizes[i]);
            stage[i][2] = new Inhibitory_Layer(simple_plane_sizes[i], simple_receptive_field_sizes[i]);
        }

    }

    void proceed(boolean train, int real) {
        propagate_first_stage(train);
        propagate_second_stage(train);
        propagate_third_stage(train);
        propagate_fourth_stage(train);

        double max = -1;
        int index = -1;
        double[][][] output = stage[3][1].get_c_output();
        System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.print("\nThe last C-layer outputs: ");
        for (int k = 0; k < output.length; k++) {
            for (int i = 0; i < output[0].length; i++) {
                for (int j = 0; j < output[0][0].length; j++) {
                        if (output[k][i][j] > max) {
                            max = output[k][i][j];
                            index = k;
                        }
                    System.out.print(output[k][i][j] + " ");
                }
            }
        }

        if (train) {
            prediction[index][real]++;
        }

        if (train) {
            System.out.println("\nThe digit train output is: " + (index + 1) + "\n");
        } else {
            System.out.println("\nThe digit output predicted is: " + (final_prediction.get(index) + 1) + "\n");
            if (final_prediction.get(index) + 1 == real) {
                System.out.println("True prediction");
            } else {
                System.out.println("False prediction");
            }
        }
    }

    void propagate_first_stage(boolean train) {
        double[][][] image = new double[1][][];
        image[0] = this.image;

        stage[0][2].propagate(image, null, null, train);
        stage[0][0].propagate(image, stage[0][2].get_v_output(), null, train);
        stage[0][1].propagate(null, null, stage[0][0].get_s_output(), train);
    }

    void propagate_second_stage(boolean train) {
        double[][][] previous_complex_images = stage[0][1].get_c_output();
        stage[1][2].propagate(previous_complex_images, null, null, train);
        stage[1][0].propagate(previous_complex_images, stage[1][2].get_v_output(), null, train);
        stage[1][1].propagate(null, null, stage[1][0].get_s_output(), train);
    }

    void propagate_third_stage(boolean train) {
        double[][][] previous_complex_images = stage[1][1].get_c_output();
        stage[2][2].propagate(previous_complex_images, null, null, train);
        stage[2][0].propagate(previous_complex_images, stage[2][2].get_v_output(), null, train);
        stage[2][1].propagate(null, null, stage[2][0].get_s_output(), train);
    }

    void propagate_fourth_stage(boolean train) {
        double[][][] previous_complex_images = stage[2][1].get_c_output();
        stage[3][2].propagate(previous_complex_images, null, null, train);
        stage[3][0].propagate(previous_complex_images, stage[3][2].get_v_output(), null, train);
        stage[3][1].propagate(null, null, stage[3][0].get_s_output(), train);
    }

    double[][] parse_image(BufferedImage image) {
        double[][] image_array;

        int image_width = image.getWidth();
        int image_height = image.getHeight();

        image_array = new double[image_width][image_height];

        System.out.println("Next image:");
        for (int i = 0; i < image_width; i++) {
            for (int j = 0; j < image_height; j++) {
                int color = image.getRGB(j, i);

                if ((color & 0xff) + ((color & 0xff00) >> 8) + ((color & 0xff0000) >> 16) <= 230) {
                    image_array[j][i] = 0;
                } else {
                    image_array[j][i] = 1;
                }
                System.out.print((int) image_array[j][i] + " ");
            }
            System.out.println();
        }

        System.out.println("\nProcessing...");

        return image_array;
    }

    void evaluate_prediction() {
        for (int i = 0; i < 9; i++) {
            int index = -1;
            int max = -1;
            for (int j = 0; j < 9; j++) {
                if (max < prediction[i][j]) {
                    max = prediction[i][j];
                    index = j;
                }
            }
            final_prediction.put(i, index);
        }
    }
}
