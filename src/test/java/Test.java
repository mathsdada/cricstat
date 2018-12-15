import Utilities.StringUtils;

public class Test {
    public static void main(String args[]) {
        System.out.println("Starting the test");
//        String string = "Virat Kohli (wk)";
//        System.out.println(string.replace("(c)", ""));
        Series series = new Series("2699", "The Ashes", "2017",
                "https://www.cricbuzz.com/cricket-series/2699/india-tour-of-australia-2018-19/matches");
        System.out.println(StringUtils.longestCommonSubstringSize("Virat Kohli", "Kohli"));
    }
}