import java.util.ArrayList;
import java.util.HashSet;

class DecisionTreeBuilder {
    void buildTree(String[][] table, String[] attributes, DecisionTreeNode decisionTreeNode) {
        int tableWidth = table[0].length;
        int tableHeight = table.length;

        int classifyIndex = attributes.length - 1;

        double bestIG = -1;
        String bestAttribute = "";

        if (classifyIndex == 0) {
            decisionTreeNode.setAttributeName(attributes[0]);
            DecisionTreeNode newVariant = new DecisionTreeNode();
            newVariant.setVariantFollowed(table[1][0]);
            decisionTreeNode.getPossibleVariants().add(newVariant);
            return;
        }

        for (int i = 0; i < classifyIndex; i++) {
            String selectedAttribute = attributes[i];

            HashSet<String> variants = new HashSet<>();
            for (int j = 1; j < tableHeight; j++) {
                String exampleVariant = table[j][i];
                variants.add(exampleVariant);
            }

            int variantsAmount = variants.size();
            String[] selectedAttributeVariants = variants.toArray(new String[0]);

            for (int j = 0; j < variantsAmount; j++) {
                String variantAccessed = selectedAttributeVariants[j];
                int yesAmount = 0;
                int noAmount = 0;
                for (int k = 1; k < tableHeight; k++) {
                    String exampleVariant = table[k][i];
                    if (exampleVariant.equals(variantAccessed)) {
                        if (table[k][classifyIndex].equals("Yes")) {
                            yesAmount++;
                        } else {
                            noAmount++;
                        }
                    }
                }

                String variantCharacteristics = new StringBuilder().append(variantAccessed).append(" ").append(yesAmount).append(" ").append(noAmount).toString();
                selectedAttributeVariants[j] = variantCharacteristics;
            }

            double IEafter = 0;
            double IEbefore = 0;
            int yesAmount = 0;
            int noAmount = 0;

            for (int j = 0; j < variantsAmount; j++) {
                String[] variantInfo = selectedAttributeVariants[j].split(" ");
                int timesAmount = Integer.parseInt(variantInfo[1]) + Integer.parseInt(variantInfo[2]);
                yesAmount += Integer.parseInt(variantInfo[1]);
                noAmount += Integer.parseInt(variantInfo[2]);
                double yesDivision = Double.parseDouble(variantInfo[1]) / (double) timesAmount;
                double noDivision = Double.parseDouble(variantInfo[2]) / (double) timesAmount;
                double variantIE = -yesDivision * (Math.log10(yesDivision) / Math.log10(2)) - noDivision * (Math.log10(noDivision) / Math.log10(2));
                if (Double.isNaN(variantIE)) {
                    variantIE = 0;
                }
                double examplesAmount = tableHeight - 1;
                double partedGeneralVariantIE = ((double) timesAmount / examplesAmount) * variantIE;
                IEafter += partedGeneralVariantIE;
                if (j == variantsAmount - 1) {
                    yesDivision = (double) yesAmount / examplesAmount;
                    noDivision = (double) noAmount / examplesAmount;
                    IEbefore = -yesDivision * (Math.log10(yesDivision) / Math.log10(2)) - noDivision * (Math.log10(noDivision) / Math.log10(2));
                    if (Double.isNaN(IEbefore)) {
                        IEbefore = 0;
                    }
                }
            }

            double variantIG = IEbefore - IEafter;

            if (bestIG < variantIG) {
                bestIG = variantIG;
                bestAttribute = selectedAttribute;
            }
        }

        decisionTreeNode.setAttributeName(bestAttribute);

        int bestAttributeIndex = 0;

        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i].equals(bestAttribute)) {
                bestAttributeIndex = i;
                break;
            }
        }

        HashSet<String> variants = new HashSet<>();
        for (int j = 1; j < tableHeight; j++) {
            String exampleVariant = table[j][bestAttributeIndex];
            variants.add(exampleVariant);
        }

        int variantsAmount = variants.size();
        String[] selectedAttributeVariants = variants.toArray(new String[0]);

        for (int j = 0; j < variantsAmount; j++) {
            String[] variant = selectedAttributeVariants[j].split(" ");
            DecisionTreeNode newVariant = new DecisionTreeNode();
            newVariant.setVariantFollowed(variant[0]);
            decisionTreeNode.getPossibleVariants().add(newVariant);

            bestAttributeIndex = 0;
            int variantExamplesAmount = 0;

            ArrayList<ArrayList<String>> values = new ArrayList<>();

            for (int k = 0; k < tableWidth; k++) {
                if (table[0][k].equals(bestAttribute)) {
                    bestAttributeIndex = k;
                    break;
                }
            }

            for (int m = 0; m < tableHeight; m++) {
                if (table[m][bestAttributeIndex].equals(variant[0]) || m == 0) {
                    values.add(new ArrayList<>());
                    for (int k = 0; k < tableWidth; k++) {
                        if (k != bestAttributeIndex) {
                            values.get(variantExamplesAmount).add(table[m][k]);
                        }
                    }
                    variantExamplesAmount++;
                }
            }

            String[][] tableSplit = new String[variantExamplesAmount][tableWidth - 1];
            for (int k = 0; k < variantExamplesAmount; k++) {
                for (int m = 0; m < tableWidth - 1; m++) {
                    tableSplit[k][m] = values.get(k).get(m);
                }
            }

            String[] attributesSplit = new String[classifyIndex];
            int m = 0;
            for (String attribute : attributes) {
                if (!attribute.equals(bestAttribute)) {
                    attributesSplit[m] = attribute;
                    m++;
                }
            }

            buildTree(tableSplit, attributesSplit, newVariant);
        }
    }
}
