package Utilities;

public class StringUtils {
    static public int longestCommonSubstringSize(String string_1, String string_b){
        // https://github.com/mission-peace/interview/blob/master/src/com/interview/dynamic/LongestCommonSubstring.java
        int[][] table = new int[string_1.length() + 1][string_b.length() + 1];

        int max = 0;
        for(int i=1; i <= string_1.length(); i++){
            for(int j=1; j <= string_b.length(); j++){
                if(string_1.charAt(i-1) == string_b.charAt(j-1)){
                    table[i][j] = table[i-1][j-1] +1;
                    if(max < table[i][j]){
                        max = table[i][j];
                    }
                }
            }
        }
        return max;
    }
}
