import Model.Season;
import Common.StringUtils;

public class Test {
    public static void main(String args[]) {
        System.out.println("Starting the test");
//        DbOps dbOps = null;
//        try {
//            dbOps = new DbOps();
//            dbOps.clearSeriesAll();
//            Series series = new Series("2688", "The Ashes", "2017",
//                    "https://www.cricbuzz.com/cricket-series/2699/india-tour-of-australia-2018-19/matches");
//            boolean status = dbOps.insertSeries(series);
//            if (status) {
//                System.out.println("Series Inserted Successfully...");
//            } else {
//                System.out.println("Series Insertion Failed...");
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
        Season season = Scraper.Season.build("2019");
        System.out.println(StringUtils.longestCommonSubstringSize("Virat Kohli", "Kohli"));
    }
}