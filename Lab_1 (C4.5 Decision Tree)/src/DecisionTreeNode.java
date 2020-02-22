import java.util.ArrayList;

class DecisionTreeNode {
    private ArrayList<DecisionTreeNode> possibleVariants;
    private String attributeName;
    private String variantFollowed;

    DecisionTreeNode() {
        possibleVariants = new ArrayList<>();
    }

    String getAttributeName() {
        return attributeName;
    }

    void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    String getVariantFollowed() {
        return variantFollowed;
    }

    void setVariantFollowed(String variantFollowed) {
        this.variantFollowed = variantFollowed;
    }

    ArrayList<DecisionTreeNode> getPossibleVariants() {
        return possibleVariants;
    }
}
