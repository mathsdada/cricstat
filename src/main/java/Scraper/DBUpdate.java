package Scraper;

import Model.Match;
import Model.Season;
import Model.Series;

public class DBUpdate {
    private void updateStats(String year) {
        Season season = new Season(year);
        for (Series series: season.getSeriesList()) {
            for (Match match: series.getMatches()) {
                continue;
            }
        }
    }
}
