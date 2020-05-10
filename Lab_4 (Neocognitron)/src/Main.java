import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
//        BufferedImage image = null;
//
//        Neocognitron neocognitron = new Neocognitron();
//
//        boolean train = true;
//        for (int k = 1; k <= 9; k++) {
//            for (int i = 0; i < 500; i++) {
//                image = ImageIO.read(new File("train_images/" + k + "_" + i + ".jpg"));
//
//                neocognitron.set_image(image);
//                neocognitron.proceed(train, k - 1);
//            }
//        }
//
//        neocognitron.evaluate_prediction();
//
//        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("structure/neocognitron"));
//        objectOutputStream.writeObject(neocognitron);
//        objectOutputStream.close();

        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("structure/neocognitron"));

        Neocognitron deserialized_neocognitron = (Neocognitron) objectInputStream.readObject();

        BufferedImage image = null;
        boolean train = false;
        for (int k = 1; k <= 9; k++) {
            for (int i = 0; i < 50; i++) {
                image = ImageIO.read(new File("test_images/" + k + "_" + i + ".jpg"));
                deserialized_neocognitron.set_image(image);
                deserialized_neocognitron.proceed(train, k);
            }
        }
    }
}
