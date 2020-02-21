import java.io.IOException;
import java.util.LinkedHashMap;

public class Main {
    public static void main(String[] args) throws IOException {
        DataParser dataParser = new DataParser();
        String[] attributes = dataParser.getAttributes();
        String[][] table = dataParser.getTable(attributes);

        DecisionTreeBuilder decisionTreeBuilder = new DecisionTreeBuilder();
        DecisionTreeNode decisionTree = new DecisionTreeNode();
        decisionTreeBuilder.buildTree(table, attributes, decisionTree);

        DecisionTreeResolvent decisionTreeResolvent = new DecisionTreeResolvent();
        LinkedHashMap<String, String> example = new LinkedHashMap<>();

        example.put("Outlook", "Overcast");
        example.put("Temperature", "Hot");
        example.put("Humidity", "High");
        example.put("Wind", "Low");
        example.put("Play", "?");

        System.out.println(new StringBuilder().append(example.values())
                .append(" -> ")
                .append(decisionTreeResolvent.resolveCase(decisionTree, example)));

        example.clear();
        example.put("Outlook", "Rain");
        example.put("Temperature", "Cold");
        example.put("Humidity", "Normal");
        example.put("Wind", "High");
        example.put("Play", "?");

        System.out.println(new StringBuilder().append(example.values())
                                              .append(" -> ")
                                              .append(decisionTreeResolvent.resolveCase(decisionTree, example)));
    }
}
