import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class realQP {
    
    // static variables allow me to use variables across different functions
    static int nrSwapsMin; 
    static int nrSwapsMax; 
    static int subLenMin; 
    static int subLenMax; 
    static int jumpMin; 
    static int jumpMax; 
    static int stringLength; 

    @SuppressWarnings("ConvertToStringSwitch")
    public static void main(String[] args) { 
        // I will do phase one and thus "generate" will not be tested
        // return an error if 'generate' is passed
        // the program should continue reading stdin until it recieves an EOF Error
        // lines starting with # should be ignored
        // Empty lines should produce an error with accordance with the error section
        /*  phase 1: Searching
        Input Format The following parameters will be provided as input(use Scanner) via stdin
        1. nrSwapsMin: The minimum number of swaps (must be at least one)
        2. nrSwapsMax: The maximum number of swaps
        3. subLenMin: The minimum length of the substrings (must be at least three)
        4. subLenMax: The maximum length of the substring
        5. jumpMin: The minimum distance of the jump (must be at least one)
        6. jumpMax: The maximum distance of the jump
        7. stringLength: The length of strings to search (must be at least three)
        [nrSwapsMin, nrSwapsMax, subLenMin, subLenMax, jumpMin, jumpMax, stringLength] */ 
        
        //Step1: Checking if we have enough arguments
        if (args.length == 0) { 
            System.err.println("Too few arguments. Expected 1, got " + args.length);
            System.exit(0); // End the program
            //ending statement
        } else if (args.length > 1) { 
            System.err.println("Too many arguments. Expected 1, got " + args.length);
            System.exit(0);
        } else { 
            // think about it
            if (args[0].equals("search")) { 
                //I will create a the search function
                // if (validateParameters() == false) {
                //     System.exit(-1);
                // }
                if (validateParameters()) { // if the function is true we move to next function
                    validateString();
                }
                //call the validateString function if validateParameters is done
            } else if (args[0].equals("generate")) { 
                System.err.println("Invalid Execution Mode: 'generate'"
                + " is not supported in Phase 1");
                System.exit(-1);
            } else { 
                System.err.println("Invalid mode: expected 'search' or 'generate', "
                + "found " + "'" + args[0] + "'");
                System.exit(-1);
            } 
        }
    }
    
    public static boolean checkIfPalindrome(String word) {
        String reversedWord = "";

        for (int i = word.length() - 1; i > 0 - 1; i--) {
            reversedWord += word.charAt(i);
        }

        return word.equals(reversedWord); // returns either true or false
    } 
     
    public static String swappString(String input) { 
        String swappedString = "";
        char[] inputArray = input.toCharArray();
        char temp;
        //2nd function
        for (int j = 0; j < inputArray.length; j++) { 
            swappedString = "";
            try {
                temp = inputArray[j];
                inputArray[j] = inputArray[j + 1];
                inputArray[j + 1] = temp; 
                for (int k = 0; k < inputArray.length; k++) {
                    swappedString += inputArray[k];
                } 

                inputArray[j + 1] = inputArray[j];
                inputArray[j] = temp;
                
            } catch (ArrayIndexOutOfBoundsException e) {
                // do nothing
            } 
        } return swappedString;
    } 

    public static List<String> modifyString(String input) { 
        /*  this code will be responsible for modifying my
          string so it matches the parameter requirements */
        List<String> inputOutcome = new ArrayList<>();
        
        for (int length = subLenMin; length <= subLenMax; length++) {
            for (int j = 0; j <= input.length() - length; j++) {
                inputOutcome.add(input.substring(j, j + length));
            }
        }
        return inputOutcome;
    }

    public static boolean validateParameters() { 
        //this function will validate the parameters
        try { 
            Scanner scanner = new Scanner(System.in);
                // while (scanner.hasNextLine()) {
                while (true) {
                    String input = scanner.nextLine();

                    boolean startOver = false; // this will help in future
                    
                    // scanner.nextLine();

                    

                    if (input.startsWith("#")) { 
                        break;
                    } 
                    if (input.isEmpty()) { // if there is an empty line
                        // System.exit(0);
                        break;
                    } 
                    //this line of code gets the input and puts it in an array
                    String[] parameters = input.split(" ");
                    System.out.println(("before: " + input));
                    System.out.println("after: " + Arrays.toString(parameters));

                    List<Integer> validInputs = new ArrayList<>();

                    for (String parameter : parameters) {
                        try {
                            validInputs.add(Integer.valueOf(parameter));
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid parameters: expected search "
                                    + "parameters, found " + parameter);
                            startOver = true;
                        }
                    }
                    if (startOver) { 
                        break; // this is for resetting the loop if the error occured
                    }

                    int[] finalInputs = validInputs.stream().mapToInt(i -> i).toArray();
                    
                    if (finalInputs.length != 7) { 
                        System.err.println("Invalid parameters: expected at "
                        + "least 7, found " + finalInputs.length);
                    } else { 
                        nrSwapsMin = finalInputs[0];
                        nrSwapsMax = finalInputs[1];
                        subLenMin = finalInputs[2];
                        subLenMax = finalInputs[3];
                        jumpMin = finalInputs[4];
                        jumpMax = finalInputs[5];
                        stringLength = finalInputs[6];
                        
                        //vaidation
                        if (subLenMax == -1) {
                            subLenMax = stringLength;
                        }
                        if (nrSwapsMax == -1) {
                            nrSwapsMax = (byte) ((subLenMax * (subLenMax - 1)) / 2);
                        }
                        if (jumpMax == -1) {
                            jumpMax = (byte) (subLenMax - 1);
                        }
                        if ((nrSwapsMin < 1) && ((nrSwapsMin != -1))) { 
                            System.err.println("Invalid number of swaps: expected at "
                            + "least 1, found " + nrSwapsMin);
                            // finalInputs = new int[0];
                            // return false;
                            // continue; // skips the rest of the loop and goes to next iteration
                            break;
                        } 
                        if ((nrSwapsMax < nrSwapsMin) && ((nrSwapsMax != -1))) { 
                            System.err.println("Invalid number of swaps: " + nrSwapsMin  
                            + " must be less than " + nrSwapsMax);
                            // return false;
                            // continue; // skips the rest of the loop and goes to next iteration
                            break;
                        } 
                        if ((subLenMin < 3) && (subLenMin != -1)) { 
                            System.err.println("Invalid substring length: expected at "
                            + "least 3, found " + subLenMin);
                            // return false;
                            // continue; // skips the rest of the loop and goes to next iteration
                            break;
                        } 
                        if (subLenMax < subLenMin) { 
                            System.err.println("Invalid substring length: " + subLenMin
                            + " must be less than " + subLenMax);
                            // return false;
                            // continue; // skips the rest of the loop and goes to next iteration
                            break;
                        } 
                        if (subLenMax > stringLength) { 
                            System.err.println("Invalid substring length: expected "
                            +  "at most " + stringLength + ", found " + subLenMax);
                            // return false;
                            // continue;
                            break;
                        } 
                        if (jumpMin < 1) { 
                            System.err.println("Invalid jump size: expected at "
                            + "least 1, found " + jumpMin);
                            // return false;
                            // continue; // skips the rest of the loop and goes to next iteration
                            break;
                        } 
                        if (jumpMax > subLenMax) {
                            System.err.println("Invalid jump size: expected at most "
                            + (subLenMax - 1) + ", found " + jumpMax);
                            // return false;
                            // continue; /////////////
                            break;
                        }
                        if (jumpMax >= (stringLength)) {
                            System.err.println("Invalid jump size: expected "
                            + "at most " + jumpMax + ", found " + (stringLength - 1));
                            // return false;
                            // continue;
                            break;
                        } 
                        if ((stringLength - 1) < jumpMin) {
                            System.err.println("Invalid jump size: expected at least "
                            + jumpMin + ", found " + (stringLength - 1));
                            // return false;
                            // continue;
                            break;
                        } 
                        if (jumpMax < jumpMin) { 
                            System.err.println("Invalid jump size: " + jumpMin 
                            + " must be less than " + jumpMax);
                            // return false;
                            // continue; // skips the rest of the loop and goes to next iteration
                            break;
                        } 
                        System.err.print("SearchArgs ");
                        System.err.println("[nrSwapsMin=" + nrSwapsMin + ", nrSwapsMax=" 
                        + nrSwapsMax
                        + ", subLenMin=" + subLenMin + ", subLenMax=" + subLenMax 
                        + ", jumpMin=" + jumpMin
                        + ", jumpMax=" + jumpMax + ", stringLength=" + stringLength + "]");

                        // scanner.close();
                        return true;
                    } 
                } //System.exit(-1); //EOF error
            
        } catch (NumberFormatException e) { 
            //do nothing
        } 
        return true;
    } 

    public static boolean validateString() { 
        Scanner scan = new Scanner(System.in);
        boolean proceed = true;
        
        while (proceed) {
            while (scan.hasNextLine()) {
                String inputString = scan.nextLine();
                if (inputString.isEmpty()) {
                    proceed = false; //stop everything
                    // System.exit(0);
                }
                if (inputString.matches("[1,2,3,4,5,6,7,8,9,0]+")) { // change this one ######
                    // System.out.println("###");
                    validateParameters();
                }

                if (inputString.startsWith("#")) {
                    continue;
                }
                //checks if length of given code matches expected length
                if (!(inputString.length() == (stringLength))) {
                    System.err.println("Invalid search string length: expected "
                    + stringLength + ", found " + inputString.length());
                    continue;
                }
                // Check if the string contains only A, C, G, or T
                if (!inputString.matches("[A,C,G,T]+")) {  //change this one ######
                    for (int i = 0; i < inputString.length(); i++) {  // Traditional for loop
                        char basePair = inputString.charAt(i);  // Get character at index i
                        if ("ACGT".indexOf(basePair) == -1) {  // Java raises -1 if not found
                            System.err.println("Invalid character: expected 'A, C, G or T', "
                            + "found '" + basePair + "'");
                        } 
                    } continue;
                }
                // Process substrings and quasi-palindromes
        Map<String, List<PairUp>> results = findQuasiPalindromes(inputString);

        //PRINT OUT
        // System.out.print("SearchArgs ");
        //     System.out.println("[nrSwapsMin=" + nrSwapsMin + ", nrSwapsMax=" 
        //     + nrSwapsMax
        //     + ", subLenMin=" + subLenMin + ", subLenMax=" + subLenMax 
        //     + ", jumpMin=" + jumpMin
        //     + ", jumpMax=" + jumpMax + ", stringLength=" + stringLength + "]");
        System.out.println(results);
            }
        } return true;
    }

    private static Map<String, List<PairUp>> findQuasiPalindromes(String input) {
        Map<String, List<PairUp>> palindromeMap = new TreeMap<>();
        int n = input.length();

        // Loop over substrings within given length range
        for (int len = subLenMin; len <= subLenMax; len++) {
            for (int i = 0; i <= n - len; i++) {
                String substring = input.substring(i, i + len);

                // Attempt swaps and check for quasi-palindromes
                for (int j = 0; j < len; j++) {
                    for (int k = j + jumpMin; k < len && k <= j + jumpMax; k++) {
                        char[] charArray = substring.toCharArray();
                        swap(charArray, j, k);
                        String modifiedString = new String(charArray);

                        // Check if swapped substring forms a palindrome
                        if (checkIfPalindrome(modifiedString)) {
                            PairUp swapPair = new PairUp(i + j, i + k);

                            // Add to map or update map with swap details
                            palindromeMap.computeIfAbsent(substring, key -> new 
                            ArrayList<>()).add(swapPair);
                        }
                    }
                }
            }
        }
        return palindromeMap;
    }

    // Function to swap two characters in a char array
    private static void swap(char[] inputArr, int i, int ii) {
        char temp = inputArr[i];
        inputArr[i] = inputArr[ii];
        inputArr[ii] = temp;
    }
    // Custom class to store pairs of swapped indices
    static class PairUp {
        int start, end;

        PairUp(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return "(" + start + "," + end + ")";
        }
    }
}
