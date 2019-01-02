package Model;

import Configuration.Config;
import Utility.ScraperUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
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

    public static Series extractSeries(Element seriesElement, String year) {
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
        Document seriesDocument = ScraperUtils.getDocument(seriesUrl);
        String seriesFormat = getSeriesFormat(seriesDocument);
        ArrayList<Match> matches = new ArrayList<>();

        Elements matchElements = seriesDocument.select("div.cb-col-60.cb-col.cb-srs-mtchs-tm");
        for (Element matchElement: matchElements) {
            Match match = Match.extractMatchData(matchElement, seriesFormat);
            if (match != null) {
                match.scrape();
                matches.add(match);
            }
        }
        return matches;
    }
}
