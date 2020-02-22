import java.util.LinkedHashMap;

class DecisionTreeResolvent {
    String resolveCase(DecisionTreeNode decisionTreeNode, LinkedHashMap<String, String> example) {
        while (decisionTreeNode.getAttributeName() != null) {
            String attribute = decisionTreeNode.getAttributeName();
            String choice = example.get(attribute);

            if (choice.equals("?")) {
                decisionTreeNode = decisionTreeNode.getPossibleVariants().get(0);
            }

            for (DecisionTreeNode decisionTreeNodeVariant : decisionTreeNode.getPossibleVariants()) {
                if (decisionTreeNodeVariant.getVariantFollowed().equals(choice)) {
                    decisionTreeNode = decisionTreeNodeVariant;
                    break;
                }
            }
        }
        return decisionTreeNode.getVariantFollowed();
    }
}
