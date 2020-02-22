import java.util.HashSet;

class GainRatioEvaluator {
    void evaluateGainRatio(String[][] table, String[] attributes) {
        int tableHeight = table.length;

        int classifyIndex = attributes.length;

        for (int i = 0; i < classifyIndex; i++) {
            double examplesAmount = 0;

            HashSet<String> variants = new HashSet<>();
            for (int j = 1; j < tableHeight; j++) {
                String exampleVariant = table[j][i];
                if (exampleVariant.equals("-")) {
                    continue;
                }
                examplesAmount++;
                variants.add(exampleVariant);
            }

            int variantsAmount = variants.size();
            String[] selectedAttributeVariants = variants.toArray(new String[0]);

            for (int j = 0; j < variantsAmount; j++) {
                String variantAccessed = selectedAttributeVariants[j];
                int yesAmount = 0;

                for (int k = 1; k < tableHeight; k++) {
                    String exampleVariant = table[k][i];
                    if (exampleVariant.equals(variantAccessed)) {
                        yesAmount++;
                    }
                }

                String variantCharacteristics = new StringBuilder().append(variantAccessed).append(" ").append(yesAmount).toString();
                selectedAttributeVariants[j] = variantCharacteristics;
            }


            double IEafter = 0;
            double IEbefore = 0;
            double variantIV = 0;

            for (int j = 0; j < variantsAmount; j++) {
                String[] variantInfo = selectedAttributeVariants[j].split(" ");

                double meetingsAmount = Double.parseDouble(variantInfo[1]);
                double yesDivision = meetingsAmount / examplesAmount;
                double noDivision = (examplesAmount - meetingsAmount) / examplesAmount;

                variantIV += -(meetingsAmount / examplesAmount) * (Math.log10(meetingsAmount / examplesAmount) / Math.log10(2));

                double variantIE = -yesDivision * (Math.log10(yesDivision) / Math.log10(2)) - noDivision * (Math.log10(noDivision) / Math.log10(2));
                IEbefore += -yesDivision * (Math.log10(yesDivision) / Math.log10(2));

                if (Double.isNaN(variantIE)) {
                    variantIE = 0;
                }

                double partedGeneralVariantIE = (meetingsAmount / examplesAmount) * variantIE;

                IEafter += partedGeneralVariantIE;
            }

            double variantIG = IEbefore - IEafter;
            double gain_ratio = variantIG / variantIV;

            if (Double.isNaN(gain_ratio)) {
                gain_ratio = 0;
            }

            // System.out.println(attributes[i] + " " + variantIG + " " + variantIV + " " + variantGainRatio);
             System.out.println(attributes[i] + " " + gain_ratio);
            // System.out.println(attributes[i]);
            // System.out.println(gain_ratio);
        }
    }
}
