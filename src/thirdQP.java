import java.util.*;
import java.util.stream.Collectors;

public class thirdQP {
    private static class PairUp implements Comparable<PairUp> {
        final int first;
        final int second;
        
        PairUp(int a, int b) {
            first = Math.min(a, b);
            second = Math.max(a, b);
        }

        @Override
        public int compareTo(PairUp other) {
            if (this.first != other.first) return Integer.compare(this.first, other.first);
            return Integer.compare(this.second, other.second);
        }

        @Override
        public String toString() {
            return "(" + first + "," + second + ")";
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Invalid arguments: expected 1, found " + args.length);
            System.exit(1);
        }

        if (!args[0].equals("search")) {
            System.err.println("Invalid mode: expected 'search', found '" + args[0] + "'");
            System.exit(1);
        }

        processSearch();
    }

    private static void processSearch() {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String paramLine = scanner.nextLine().trim();
            if (paramLine.startsWith("#") || paramLine.isEmpty()) continue;

            try {
                String[] params = paramLine.split("\\s+");
                if (params.length != 7) {
                    System.err.println("Invalid parameters: expected 7, found " + params.length);
                    continue;
                }

                int[] parameters = Arrays.stream(params)
                                       .mapToInt(Integer::parseInt)
                                       .toArray();

                if (!validateParameters(parameters)) continue;

                String searchStr = readSearchString(scanner, parameters[6]);
                if (searchStr == null) continue;

                Map<String, List<PairUp>> results = findQuasiPalindromes(searchStr, parameters);
                formatAndPrintResults(results);
                
            } catch (NumberFormatException e) {
                System.err.println("Invalid parameters: non-integer values detected");
            }
        }
    }

    private static boolean validateParameters(int[] params) {
        int nrSwapsMin = params[0], nrSwapsMax = params[1], 
            subLenMin = params[2], subLenMax = params[3], 
            jumpMin = params[4], jumpMax = params[5], 
            stringLength = params[6];

        if (nrSwapsMin < 1) {
            System.err.println("Invalid number of swaps: expected at least 1, found " + nrSwapsMin);
            return false;
        }
        if (nrSwapsMax != -1 && nrSwapsMax < nrSwapsMin) {
            System.err.println("Invalid number of swaps: " + nrSwapsMin + " must be less than " + nrSwapsMax);
            return false;
        }
        if (subLenMin < 3) {
            System.err.println("Invalid substring length: expected at least 3, found " + subLenMin);
            return false;
        }
        if (subLenMax != -1 && subLenMax < subLenMin) {
            System.err.println("Invalid substring length: " + subLenMin + " must be less than " + subLenMax);
            return false;
        }
        if (subLenMax > stringLength) {
            System.err.println("Invalid substring length: expected at most " + stringLength + ", found " + subLenMax);
            return false;
        }
        if (jumpMin < 1) {
            System.err.println("Invalid jump size: expected at least 1, found " + jumpMin);
            return false;
        }
        if (jumpMax != -1 && jumpMax < jumpMin) {
            System.err.println("Invalid jump size: " + jumpMin + " must be less than " + jumpMax);
            return false;
        }
        if (stringLength < 3) {
            System.err.println("Invalid string length: expected at least 3, found " + stringLength);
            return false;
        }
        return true;
    }

    private static String readSearchString(Scanner scanner, int expectedLength) {
        if (!scanner.hasNextLine()) {
            System.err.println("Missing search string");
            return null;
        }

        String searchStr = scanner.nextLine().trim();
        
        if (searchStr.length() != expectedLength) {
            System.err.println("Invalid string length: expected " + expectedLength + ", found " + searchStr.length());
            return null;
        }

        for (int i = 0; i < searchStr.length(); i++) {
            char c = searchStr.charAt(i);
            if (c != 'A' && c != 'C' && c != 'G' && c != 'T') {
                System.err.println("Invalid character: expected 'A, C, G or T', found '" + c + "'");
                return null;
            }
        }

        return searchStr;
    }

    private static Map<String, List<PairUp>> findQuasiPalindromes(String input, int[] params) {
        Map<String, List<PairUp>> results = new TreeMap<>((a, b) -> {
            if (a.length() != b.length()) return a.length() - b.length();
            return a.compareTo(b);
        });

        int subLenMax = params[3] == -1 ? input.length() : params[3];
        subLenMax = Math.min(subLenMax, input.length());

        for (int len = params[2]; len <= subLenMax; len++) {
            for (int i = 0; i <= input.length() - len; i++) {
                final int offset = i;
                String substring = input.substring(offset, offset + len);
                if (isPalindrome(substring)) continue;

                int actualJumpMax = params[5] == -1 ? len - 1 : params[5];
                List<PairUp> validSwaps = findValidSwaps(substring, params[4], actualJumpMax, params[0], params[1]);

                if (!validSwaps.isEmpty()) {
                    List<PairUp> adjustedSwaps = validSwaps.stream()
                        .map(pair -> new PairUp(pair.first + offset, pair.second + offset))
                        .sorted()
                        .collect(Collectors.toList());
                    results.put(substring, adjustedSwaps);
                }
            }
        }
        return results;
    }

    private static boolean isPalindrome(String s) {
        int left = 0, right = s.length() - 1;
        while (left < right) {
            if (s.charAt(left++) != s.charAt(right--)) return false;
        }
        return true;
    }

    private static List<PairUp> findValidSwaps(String s, int jumpMin, int jumpMax, int minSwaps, int maxSwaps) {
        List<PairUp> possibleSwaps = new ArrayList<>();
        int n = s.length();

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int distance = j - i;
                if (distance >= jumpMin && distance <= jumpMax) {
                    possibleSwaps.add(new PairUp(i, j));
                }
            }
        }

        int maxPossibleSwaps = (n * (n - 1)) / 2;
        int actualMaxSwaps = maxSwaps == -1 ? maxPossibleSwaps : Math.min(maxSwaps, maxPossibleSwaps);
        actualMaxSwaps = Math.min(actualMaxSwaps, minSwaps + 10); // Limit for performance

        return findMinimalSwapCombinations(s, possibleSwaps, minSwaps, actualMaxSwaps);
    }

    private static List<PairUp> findMinimalSwapCombinations(String s, List<PairUp> swaps, int minSwaps, int maxSwaps) {
        char[] original = s.toCharArray();
        List<PairUp> minimalSwaps = new ArrayList<>();

        for (int k = minSwaps; k <= maxSwaps; k++) {
            List<List<PairUp>> combinations = new ArrayList<>();
            generateCombinations(swaps, k, 0, new ArrayList<>(), combinations);

            for (List<PairUp> combo : combinations) {
                char[] modified = original.clone();
                for (PairUp swap : combo) {
                    int i = swap.first;
                    int j = swap.second;
                    if (i < modified.length && j < modified.length) {
                        char temp = modified[i];
                        modified[i] = modified[j];
                        modified[j] = temp;
                    }
                }
                if (isPalindrome(new String(modified))) {
                    combo.sort(PairUp::compareTo);
                    return combo;
                }
            }
            if (!minimalSwaps.isEmpty()) break;
        }
        return Collections.emptyList();
    }

    private static void generateCombinations(List<PairUp> swaps, int k, int start, List<PairUp> current, List<List<PairUp>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = start; i < swaps.size(); i++) {
            current.add(swaps.get(i));
            generateCombinations(swaps, k, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    private static void formatAndPrintResults(Map<String, List<PairUp>> results) {
        if (results.isEmpty()) {
            System.out.println("{}");
            return;
        }

        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<String, List<PairUp>> entry : results.entrySet()) {
            sb.append(entry.getKey()).append("=[");
            List<PairUp> swaps = entry.getValue().stream()
                                    .sorted()
                                    .collect(Collectors.toList());
            for (int i = 0; i < swaps.size(); i++) {
                sb.append(swaps.get(i));
                if (i < swaps.size() - 1) sb.append(", ");
            }
            sb.append("], ");
        }
        if (sb.length() > 1) sb.setLength(sb.length() - 2);
        sb.append("}");
        System.out.println(sb);
    }
}