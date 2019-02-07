package Common;

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

    public static String correctPlayerName(String name) {
        name = name
                .replace("(c)", "")
                .replace("(wk)", "")
                .replace("(c & wk)", "")
                .strip();
        if (name.endsWith(" sub")) {
            name = name.replace(" sub", "").strip();
        } else if (name.endsWith(" Sub")) {
            name = name.replace(" Sub", "").strip();
        }
        return name;
    }

    public static String correctTeamName(String teamName) {
        switch (teamName) {
            case "west indies": return "windies";
            case "uae": return "united arab emirates";
            case "hk": return "hong kong";
            case "marylebone cricket club world xi": return "mcc world xi";
            case "pakistan u-19": return "pakistan u19";
            case "west indies women": return "windies women";
            case "rising pune supergiants": return "rising pune supergiant";
            case "st lucia zouks": return "st lucia stars";
            case "cobras": return "cape cobras";
            case "west indies u19": return "windies u19";
            case "west indies a": return "windies a";
            case "trinidad & tobago": return "trinidad and tobago";
            case "wayamba": return "wayamba elevens";
            case "rsa": return "south africa";
            case "sl": return "sri lanka";
            default:
                return teamName;
        }
    }
}
