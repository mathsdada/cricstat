import Model.Season;
import Model.Series;
import Utility.StringUtils;

public class Test {
    public static void main(String args[]) {
        System.out.println("Starting the test");
//        Series series = new Series("2699", "The Ashes", "2017",
//                "https://www.cricbuzz.com/cricket-series/2699/india-tour-of-australia-2018-19/matches");
        Season season = new Season("2018");
        System.out.println(StringUtils.longestCommonSubstringSize("Virat Kohli", "Kohli"));
    }
}