import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

class DataParser {
    private Scanner scanner;

    DataParser() throws IOException {
        scanner = new Scanner(Paths.get("test.txt"));
    }

    String[] getAttributes() {
        return scanner.nextLine().split(" ");
    }

    String[][] getTable(String[] attributes) {
        int attributesAmount = attributes.length;

        ArrayList<ArrayList<String>> values = new ArrayList<>();
        values.add(new ArrayList<>());

        for (String attribute : attributes) {
            values.get(0).add(attribute);
        }

        int examplesAmount = 1;
        while (scanner.hasNextLine()) {
            values.add(new ArrayList<>());
            String[] example = scanner.nextLine().split("\t");
            for (int i = 0; i < attributesAmount; i++) {
                values.get(examplesAmount).add(example[i]);
            }
            examplesAmount++;
        }

        String[][] table = new String[examplesAmount][attributesAmount];

        for (int i = 0; i < examplesAmount; i++) {
            for (int j = 0; j < attributesAmount; j++) {
                table[i][j] = values.get(i).get(j);
            }
        }

        return table;
    }
}
