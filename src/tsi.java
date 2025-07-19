import java.util.*;
import java.io.*;

public class tsi {
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
        if (mode.equals("search")) {
            processSearchPhase();
        } else {
            System.err.println("Generate phase not implemented yet");
            System.exit(1);
        }
    }

    private static void processSearchPhase() {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.startsWith("#")) continue;
            if (line.isEmpty()) {
                System.err.println("Invalid parameters: expected input, found nothing");
                continue;
            }
            String[] params = line.split(" ");
            if (params.length < 7) {
                System.err.println("Invalid parameters: expected at least 7, found " + params.length);
                continue;
            }

            try {
                int nrSwapsMin = parseParam(params[0]);
                int nrSwapsMax = parseParam(params[1]);
                int subLenMin = parseParam(params[2]);
                int subLenMax = parseParam(params[3]);
                int jumpMin = parseParam(params[4]);
                int jumpMax = parseParam(params[5]);
                int stringLength = parseParam(params[6]);

                nrSwapsMax = (nrSwapsMax == -1) ? Integer.MAX_VALUE : nrSwapsMax;
                subLenMax = (subLenMax == -1) ? stringLength : subLenMax;
                jumpMax = (jumpMax == -1) ? stringLength - 1 : jumpMax;

                if (!validateSearchParams(nrSwapsMin, nrSwapsMax, subLenMin, subLenMax, jumpMin, jumpMax, stringLength)) {
                    continue;
                }

                if (!scanner.hasNextLine()) { // if scanner does not have next line
                    System.err.println("Invalid parameters: search string missing");
                    continue;
                }
                String searchStr = scanner.nextLine().trim();
                if (searchStr.isEmpty()) {
                    System.err.println("Invalid parameters: expected input, found nothing");
                    continue;
                }
                if (searchStr.length() != stringLength) {
                    System.err.println("Invalid search string length: expected " + stringLength + ", found " + searchStr.length());
                    continue;
                }
                if (!isValidDNA(searchStr)) {
                    System.err.println("Invalid character: expected 'A, C, G or T', found '" + findInvalidChar(searchStr) + "'");
                    continue;
                }

                List<QuasiPalindrome> results = processString(searchStr, nrSwapsMin, nrSwapsMax, subLenMin, subLenMax, jumpMin, jumpMax);
                System.out.println(formatResults(results));
            } catch (NumberFormatException e) {
                System.err.println("Invalid parameters: expected integers");
            }
        }
        scanner.close();
    }

    private static int parseParam(String param) {
        return Integer.parseInt(param);
    }

    private static boolean validateSearchParams(int nrSwapsMin, int nrSwapsMax, int subLenMin, int subLenMax, int jumpMin, int jumpMax, int stringLength) {
        if (nrSwapsMin < 1) {
            System.err.println("Invalid number of swaps: expected at least 1, found " + nrSwapsMin);
            return false;
        }
        if (nrSwapsMax < nrSwapsMin) {
            System.err.println("Invalid number of swaps: " + nrSwapsMin + " must be less than " + nrSwapsMax);
            return false;
        }
        if (subLenMin < 3) {
            System.err.println("Invalid substring length: expected at least 3, found " + subLenMin);
            return false;
        }
        if (subLenMax < subLenMin) {
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
        if (jumpMax < jumpMin) {
            System.err.println("Invalid jump size: " + jumpMin + " must be less than " + jumpMax);
            return false;
        }
        if (stringLength < 3) {
            System.err.println("Invalid string length: expected at least 3, found " + stringLength);
            return false;
        }
        return true;
    }

    private static boolean isValidDNA(String s) {
        for (char c : s.toCharArray()) {
            if ("ACGT".indexOf(c) == -1) return false;
        }
        return true;
    }

    private static char findInvalidChar(String s) {
        for (char c : s.toCharArray()) {
            if ("ACGT".indexOf(c) == -1) return c;
        }
        return ' ';
    }

    private static List<QuasiPalindrome> processString(String searchStr, int nrSwapsMin, int nrSwapsMax, int subLenMin, int subLenMax, int jumpMin, int jumpMax) {
        List<QuasiPalindrome> results = new ArrayList<>();
        for (int len = subLenMin; len <= subLenMax; len++) {
            for (int start = 0; start <= searchStr.length() - len; start++) {
                String substr = searchStr.substring(start, start + len);
                if (isPalindrome(substr)) continue;

                List<List<Swap>> swapsList = findValidSwaps(substr, nrSwapsMin, nrSwapsMax, jumpMin, jumpMax);
                if (!swapsList.isEmpty()) {
                    List<Swap> bestSwaps = swapsList.get(0);
                    Collections.sort(bestSwaps);
                    results.add(new QuasiPalindrome(substr, bestSwaps));
                }
            }
        }
        Collections.sort(results);
        return results;
    }

    private static boolean isPalindrome(String s) {
        int left = 0, right = s.length() - 1;
        while (left < right) {
            if (s.charAt(left++) != s.charAt(right--)) return false;
        }
        return true;
    }

    private static List<List<Swap>> findValidSwaps(String s, int minSwaps, int maxSwaps, int jumpMin, int jumpMax) {
        List<Swap> possibleSwaps = generatePossibleSwaps(s, jumpMin, jumpMax);
        List<List<Swap>> validCombinations = new ArrayList<>();

        for (int k = minSwaps; k <= maxSwaps && k <= possibleSwaps.size(); k++) {
            List<List<Swap>> combinations = new ArrayList<>();
            generateCombinations(possibleSwaps, k, 0, new ArrayList<>(), combinations);

            for (List<Swap> combo : combinations) {
                char[] temp = s.toCharArray();
                for (Swap swap : combo) {
                    char tmp = temp[swap.i];
                    temp[swap.i] = temp[swap.j];
                    temp[swap.j] = tmp;
                }
                if (isPalindrome(new String(temp))) {
                    validCombinations.add(combo);
                    break;
                }
            }
            if (!validCombinations.isEmpty()) break;
        }
        return validCombinations;
    }

    private static List<Swap> generatePossibleSwaps(String s, int jumpMin, int jumpMax) {
        List<Swap> swaps = new ArrayList<>();
        int n = s.length();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int distance = j - i;
                if (distance >= jumpMin && distance <= jumpMax && s.charAt(i) != s.charAt(j)) {
                    swaps.add(new Swap(i, j));
                }
            }
        }
        return swaps;
    }

    private static void generateCombinations(List<Swap> swaps, int k, int start, List<Swap> current, List<List<Swap>> result) {
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

    private static String formatResults(List<QuasiPalindrome> results) {
        if (results.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < results.size(); i++) {
            QuasiPalindrome qp = results.get(i);
            sb.append(qp.substring).append("=[");
            for (int j = 0; j < qp.swaps.size(); j++) {
                Swap swap = qp.swaps.get(j);
                sb.append("(").append(swap.i).append(",").append(swap.j).append(")");
                if (j < qp.swaps.size() - 1) sb.append(", ");
            }
            sb.append("]");
            if (i < results.size() - 1) sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }

    static class Swap implements Comparable<Swap> {
        int i, j;

        Swap(int i, int j) {
            this.i = Math.min(i, j);
            this.j = Math.max(i, j);
        }

        @Override
        public int compareTo(Swap other) {
            if (this.i != other.i) return Integer.compare(this.i, other.i);
            return Integer.compare(this.j, other.j);
        }
    }

    static class QuasiPalindrome implements Comparable<QuasiPalindrome> {
        String substring;
        List<Swap> swaps;

        QuasiPalindrome(String substring, List<Swap> swaps) {
            this.substring = substring;
            this.swaps = swaps;
        }

        @Override
        public int compareTo(QuasiPalindrome other) {
            if (this.substring.length() != other.substring.length()) {
                return Integer.compare(this.substring.length(), other.substring.length());
            }
            return this.substring.compareTo(other.substring);
        }
    }
}