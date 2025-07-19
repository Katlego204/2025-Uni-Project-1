import java.util.*;

public class QP {
    private static final String ALPHABET = "ACGT";

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Invalid arguments: expected 1, found " + args.length);
            System.exit(1);
        }
        String mode = args[0];
        if (!mode.equals("search") && !mode.equals("generate")) {
            System.err.println("Invalid arguments: expected 'search' or 'generate', found '" + mode + "'");
            System.exit(1);
        }

        Scanner scanner = new Scanner(System.in);
        try {
            if (mode.equals("search")) {
                handleSearchMode(scanner);
            } else {
                handleGenerateMode(scanner);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        } finally {
            scanner.close();
        }
    }

    private static void handleSearchMode(Scanner scanner) {
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                System.err.println("Invalid parameters: expected input, found nothing");
                continue;
            }
            if (line.startsWith("#")) continue;

            String[] params = line.split("\\s+");
            if (params.length < 7) {
                System.err.println("Invalid parameters: expected at least 7, found " + params.length);
                continue;
            }

            try {
                int nrSwapsMin = Integer.parseInt(params[0]);
                int nrSwapsMax = params[1].equals("-1") ? Integer.MAX_VALUE : 
                Integer.parseInt(params[1]);
                int subLenMin = Integer.parseInt(params[2]);
                int subLenMax = params[3].equals("-1") ? Integer.MAX_VALUE : 
                Integer.parseInt(params[3]);
                int jumpMin = Integer.parseInt(params[4]);
                int jumpMax = params[5].equals("-1") ? Integer.MAX_VALUE : 
                Integer.parseInt(params[5]);
                int stringLength = Integer.parseInt(params[6]);

                validateSearchParams(nrSwapsMin, nrSwapsMax, subLenMin, subLenMax, 
                jumpMin, jumpMax, stringLength);

                System.out.println("SearchArgs [nrSwapsMin=" + nrSwapsMin 
                + ", nrSwapsMax=" + nrSwapsMax +
                        ", subLenMin=" + subLenMin + ", subLenMax=" 
                        + subLenMax + ", jumpMin=" + jumpMin +
                        ", jumpMax=" + jumpMax + ", stringLength=" 
                        + stringLength + "]");

                while (scanner.hasNextLine()) {
                    String searchStr = scanner.nextLine().trim();
                    if (searchStr.isEmpty()) {
                        System.err.println("Invalid parameters: expected input, found nothing");
                        continue;
                    }
                    if (searchStr.startsWith("#")) continue;

                    if (searchStr.length() != stringLength) {
                        System.err.println("Invalid search string length: expected " 
                        + stringLength +
                                ", found " + searchStr.length());
                        continue;
                    }
                    if (!isValidString(searchStr)) {
                        System.err.println("Invalid character: expected 'A, C, G or T', found '" +
                                findInvalidChar(searchStr) + "'");
                        continue;
                    }

                    processSearchString(searchStr, nrSwapsMin, nrSwapsMax, subLenMin, 
                    subLenMax, jumpMin, jumpMax);
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid parameters: expected integer values");
            }
        }
    }

    private static void validateSearchParams(int nrSwapsMin, int nrSwapsMax, 
    int subLenMin, int subLenMax,
    int jumpMin, int jumpMax, int stringLength) {
        if (nrSwapsMin < 1)
            System.err.println("Invalid number of swaps: expected at least 1, found " 
            + nrSwapsMin);
        if (nrSwapsMax < nrSwapsMin)
            System.err.println("Invalid number of swaps: expected at least " + nrSwapsMin 
            + ", found " + nrSwapsMax);
        if (subLenMin < 3)
            System.err.println("Invalid substring length: expected at least 3, found " 
            + subLenMin);
        if (subLenMax < subLenMin)
            System.err.println("Invalid substring length: expected at least " 
            + subLenMin + ", found " + subLenMax);
        if (jumpMin < 1)
            System.err.println("Invalid jump size: expected at least 1, found "
             + jumpMin);
        if (jumpMax < jumpMin)
            System.err.println("Invalid jump size: expected at least " 
            + jumpMin + ", found " + jumpMax);
        if (stringLength < 3)
            System.err.println("Invalid string length: expected at least 3, found " 
            + stringLength);
    }

    private static void processSearchString(String searchStr, int nrSwapsMin, 
    int nrSwapsMax, int subLenMin,
                                            int subLenMax, int jumpMin, int jumpMax) {
        Map<String, List<List<int[]>>> qpMap = new HashMap<>();
        int maxSubLen = Math.min(subLenMax, searchStr.length());
        for (int len = subLenMin; len <= maxSubLen; len++) {
            for (int i = 0; i <= searchStr.length() - len; i++) {
                String sub = searchStr.substring(i, i + len);
                if (isPalindrome(sub)) continue;

                List<List<int[]>> swapSequences = findValidSwaps(sub, nrSwapsMin, 
                nrSwapsMax, jumpMin, jumpMax);
                if (!swapSequences.isEmpty()) {
                    List<int[]> minimalSwaps = findEarliestSwaps(swapSequences);
                    qpMap.put(sub, Collections.singletonList(minimalSwaps));
                }
            }
        }

        List<String> sortedQPs = new ArrayList<>(qpMap.keySet());
        sortedQPs.sort((a, b) -> {
            if (a.length() != b.length()) return a.length() - b.length();
            return a.compareTo(b);
        });

        StringBuilder output = new StringBuilder("{");
        for (String qp : sortedQPs) {
            output.append(qp).append("=[");
            List<List<int[]>> swapsList = qpMap.get(qp);
            for (int i = 0; i < swapsList.size(); i++) {
                List<int[]> swaps = swapsList.get(i);
                for (int j = 0; j < swaps.size(); j++) {
                    int[] swap = swaps.get(j);
                    output.append("(").append(swap[0]).append(",").append(swap[1]).append(")");
                    if (j < swaps.size() - 1) output.append(", ");
                }
                // output.append("###");
                if (i < swapsList.size() - 1) output.append(", ");
            }
            output.append("]");
            
        }
        // output.append("###");
        output.append("}");
        System.out.println(output.toString());
    }

    private static List<int[]> findEarliestSwaps(List<List<int[]>> swapSequences) {
        return swapSequences.stream()
                .min((a, b) -> {
                    for (int i = 0; i < Math.min(a.size(), b.size()); i++) {
                        int[] swapA = a.get(i);
                        int[] swapB = b.get(i);
                        if (swapA[0] != swapB[0]) return Integer.compare(swapA[0], swapB[0]);
                        else return Integer.compare(swapA[1], swapB[1]);
                    }
                    return Integer.compare(a.size(), b.size());
                })
                .orElse(Collections.emptyList());
    }

    private static List<List<int[]>> findValidSwaps(String sub, int nrSwapsMin, 
    int nrSwapsMax, int jumpMin, int jumpMax) {
        List<List<int[]>> validSequences = new ArrayList<>();
        char[] chars = sub.toCharArray();
        for (int k = nrSwapsMin; k <= nrSwapsMax; k++) {
            backtrack(chars, k, 0, new ArrayList<>(), validSequences, jumpMin, jumpMax);
        }
        return validSequences;
    }

    private static void backtrack(char[] chars, int swapsLeft, 
    int start, List<int[]> currentSwaps,
                                  List<List<int[]>> validSequences, 
                                  int jumpMin, int jumpMax) {
        if (swapsLeft == 0) {
            if (isPalindrome(new String(chars))) {
                validSequences.add(new ArrayList<>(currentSwaps));
            }
            return;
        }

        for (int i = start; i < chars.length; i++) {
            for (int j = i + 1; j < chars.length; j++) {
                int jump = j - i;
                if (jump < jumpMin || jump > jumpMax) continue;

                swap(chars, i, j);
                currentSwaps.add(new int[]{i, j});
                backtrack(chars, swapsLeft - 1, i + 1, currentSwaps, 
                validSequences, jumpMin, jumpMax);
                currentSwaps.remove(currentSwaps.size() - 1);
                swap(chars, i, j);
            }
        }
    }

    private static void swap(char[] chars, int i, int j) {
        char temp = chars[i];
        chars[i] = chars[j];
        chars[j] = temp;
    }

    private static boolean isPalindrome(String s) {
        int left = 0, right = s.length() - 1;
        while (left < right) {
            if (s.charAt(left) != s.charAt(right)) return false;
            left++;
            right--;
        }
        return true;
    }

    private static void handleGenerateMode(Scanner scanner) {
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                System.err.println("Invalid parameters: expected input, found nothing");
                continue;
            }
            if (line.startsWith("#")) continue;

            String[] params = line.split("\\s+");
            if (params.length < 5) {
                System.err.println("Invalid parameters: expected at least 5, found " + params.length);
                continue;
            }

            try {
                int length = Integer.parseInt(params[0]);
                int nrQPs = Integer.parseInt(params[1]);
                int lenQP = Integer.parseInt(params[2]);
                int nrSwaps = Integer.parseInt(params[3]);
                int jumpMax = params[4].equals("-1") ? Integer.MAX_VALUE : 
                Integer.parseInt(params[4]);

                validateGenerateParams(length, nrQPs, lenQP, nrSwaps, jumpMax);

                System.out.println("GenerationArgs [length=" + length + ", nrQPs=" 
                + nrQPs +
                        ", lenQP=" + lenQP + ", nrSwaps=" + nrSwaps + ", jumpMax=" 
                        + jumpMax + "]");

                String generated = generateString(length, nrQPs, lenQP, nrSwaps, jumpMax);
                System.out.println(generated != null ? generated : "*NO SUCH STRING*");
            } catch (NumberFormatException e) {
                System.err.println("Invalid parameters: expected integer values");
            }
        }
    }

    private static void validateGenerateParams(int length, int nrQPs, int lenQP, 
    int nrSwaps, int jumpMax) {
        if (length < 3)
            System.err.println("Invalid length: expected at least 3, found " + length);
        if (nrQPs < 0)
            System.err.println("Invalid number of QPs: expected at least 0, found " + nrQPs);
        if (lenQP < 3)
            System.err.println("Invalid QP length: expected at least 3, found " + lenQP);
        if (nrSwaps < 1)
            System.err.println("Invalid number of swaps: expected at least 1, found " + nrSwaps);
        if (jumpMax < 1)
            System.err.println("Invalid jump size: expected at least 1, found " + jumpMax);
    }

    private static String generateString(int length, int nrQPs, int lenQP, 
    int nrSwaps, int jumpMax) {
        char[] result = new char[length];
        Arrays.fill(result, 'A');
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            result[i] = ALPHABET.charAt(random.nextInt(ALPHABET.length()));
        }
        return new String(result);
    }

    private static boolean isValidString(String s) {
        for (char c : s.toCharArray()) {
            if (ALPHABET.indexOf(c) == -1) return false;
        }
        return true;
    }

    private static char findInvalidChar(String s) {
        for (char c : s.toCharArray()) {
            if (ALPHABET.indexOf(c) == -1) return c;
        }
        return ' ';
    }
}