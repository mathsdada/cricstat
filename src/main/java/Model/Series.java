package Model;

import Configuration.Config;
import Utility.ScraperUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class Series {
    private String mId;
    private String mTitle;
    private String mYear;
    private ArrayList<Match> mMatches;


    private Series(String id, String title, String year, ArrayList<Match> matches) {
        mId = id;
        mTitle = title;
        mYear = year;
        mMatches = matches;
    }

    public static Series extract(Element seriesElement, String year) {
        String seriesUrl = seriesElement.attr("href");
        // Validate Url Before proceeding
        if (!seriesUrl.contains("cricket-series") || seriesUrl.contains("qualifier") || seriesUrl.contains("warm-up") || seriesUrl.contains("practice")) {
            return null;
        }
        String seriesId = seriesUrl.split(Pattern.quote("/"))[2];
        String seriesTitle = seriesElement.text();
        return new Series(seriesId, seriesTitle, year, getSeriesMatchList(Config.HOMEPAGE + seriesUrl));
    }

    private static String getSeriesFormat(Document seriesDocument) {
        Elements elements = seriesDocument.select("div.cb-col-100.cb-col.cb-nav-main.cb-bg-white").first()
                .select("div");
        return elements.get(1).text().split(Pattern.quote(" . "))[0];
    }

    private static ArrayList<Match> getSeriesMatchList(String seriesUrl) {
        // Maintain player Cache to avoid repeatedly scraping player profile for each match in a particular series.
        // This is valid only if all matches of a series gets scraped by single thread. This logic need to changed if threading behavior changes
        // Key : Player ID, Value : Player
        HashMap<String, Player> playerCacheMap = new HashMap<>();
        ArrayList<Match> matches = new ArrayList<>();
        Document seriesDocument = ScraperUtils.getDocument(seriesUrl);

        Elements matchElements = seriesDocument.select("div.cb-col-60.cb-col.cb-srs-mtchs-tm");
        for (Element matchElement: matchElements) {
            Match match = Match.extract(matchElement, getSeriesFormat(seriesDocument));
            if (match != null) {
                System.out.println("Scraping " + match.getTitle());
                match.scrape(playerCacheMap);
                matches.add(match);
            }
        }
        return matches;
    }
}
