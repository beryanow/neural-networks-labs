import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

class GainRatioEvaluator {
    void evaluateGainRatio(String[][] table) {
        int tableHeight = table.length;
        int tableWidth = table[0].length;

        int classifyIndex = tableWidth - 3;
        for (int m = classifyIndex; m < tableWidth; m++) {
            ArrayList<Double> numbers = new ArrayList<>();

            for (int j = 1; j < tableHeight; j++) {
                if (!table[j][m].equals("-")) {
                    numbers.add(Double.parseDouble(table[j][m]));
                }
            }

            Collections.sort(numbers);

            int size = numbers.size() - 1;
            Double[] thresholds = new Double[size];

            for (int j = 0; j < size; j++) {
                thresholds[j] = (numbers.get(j) + numbers.get(j + 1)) / 2;
            }

            for (int i = 0; i < classifyIndex; i++) {
                double gainRatio = -1;
                for (Double threshold : thresholds) {
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
                        int noAmount = 0;

                        for (int k = 1; k < tableHeight; k++) {
                            if (table[k][i].equals(variantAccessed)) {
                                if (!table[k][m].equals("-")) {
                                    if (Double.parseDouble(table[k][m]) < threshold) {
                                        yesAmount++;
                                    } else {
                                        noAmount++;
                                    }
                                }
                            }
                        }

                        String variantCharacteristics = new StringBuilder().append(variantAccessed).append(" ").append(yesAmount).append(" ").append(noAmount).toString();
                        selectedAttributeVariants[j] = variantCharacteristics;
                    }

                    double IEafter = 0;
                    double IEbefore = 0;
                    double variantIV = 0;

                    for (int j = 0; j < variantsAmount; j++) {
                        String[] variantInfo = selectedAttributeVariants[j].split(" ");

                        double yesAmount = Double.parseDouble(variantInfo[1]);
                        double noAmount = Double.parseDouble(variantInfo[2]);

                        double yesDivision = yesAmount / (yesAmount + noAmount);
                        double noDivision = noAmount / (yesAmount + noAmount);

                        double variantIVParted = -((yesAmount + noAmount) / examplesAmount) * (Math.log10((yesAmount + noAmount) / examplesAmount) / Math.log10(2));
                        if (Double.isNaN(variantIVParted)) {
                            variantIVParted = 0;
                        }

                        variantIV += variantIVParted;

                        double variantIE = -yesDivision * (Math.log10(yesDivision) / Math.log10(2)) - noDivision * (Math.log10(noDivision) / Math.log10(2));
                        if (Double.isNaN(variantIE)) {
                            variantIE = 0;
                        }

                        double partedGeneralVariantIE = ((yesAmount + noAmount) / examplesAmount) * variantIE;
                        if (Double.isNaN(partedGeneralVariantIE)) {
                            partedGeneralVariantIE = 0;
                        }

                        IEafter += partedGeneralVariantIE;
                    }

                    double yesOverall = 0;
                    double noOverall = 0;

                    for (int k = 1; k < tableHeight; k++) {
                        if (!table[k][m].equals("-")) {
                            if (Double.parseDouble(table[k][m]) < threshold) {
                                yesOverall++;
                            } else {
                                noOverall++;
                            }
                        }
                    }

                    double yesOverallDivision = yesOverall / (yesOverall + noOverall);
                    double noOverallDivision = noOverall / (yesOverall + noOverall);

                    IEbefore = -yesOverallDivision * (Math.log10(yesOverallDivision) / Math.log10(2)) - noOverallDivision * (Math.log10(noOverallDivision) / Math.log10(2));

                    if (Double.isNaN(IEbefore)) {
                        IEbefore = 0;
                    }

                    double variantIG = IEbefore - IEafter;
                    double gainRatioTemp = variantIG / variantIV;

                    if (Double.isNaN(gainRatioTemp)) {
                        gainRatioTemp = 0;
                    }

                    if (gainRatioTemp > gainRatio) {
                        gainRatio = gainRatioTemp;
                    }

                    // System.out.println(attributes[i] + " " + variantIG + " " + variantIV + " " + variantGainRatio);
                    // System.out.println();

                    // System.out.println(attributes[i]);
                    // System.out.println(gainRatioTemp);
                }
                System.out.println(table[0][m] + ": ");
                System.out.println(gainRatio);
            }
        }
    }
}
