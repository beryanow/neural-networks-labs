import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        DataParser dataParser = new DataParser();
        String[] attributes = dataParser.getAttributes();
        String[][] table = dataParser.getTable(attributes);

        GainRatioEvaluator gainRatioEvaluator = new GainRatioEvaluator();
        gainRatioEvaluator.evaluateGainRatio(table);
    }
}
