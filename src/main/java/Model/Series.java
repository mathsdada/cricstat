package Model;

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
    private String mUrl;

    private String mSeriesFormat;
    private ArrayList<Match> mMatches;

    public Series(String id, String title, String year, String link) {
        mId = id;
        mTitle = title;
        mYear = year;
        mUrl = link;
        mMatches = new ArrayList<>();
        extractSeriesData();
    }

    private void extractSeriesData() {
        Document seriesDocument = ScraperUtils.getDocument(mUrl);
        // extract Model.Series Formats
        mSeriesFormat = getSeriesFormat(seriesDocument);
        // Extract Model.Series Matches
        ArrayList<Match> seriesMatchList = getSeriesMatchList(seriesDocument);
        for (Match match: seriesMatchList) {
            match.scrape();
            mMatches.add(match);
//            break;
        }
    }

    private String getSeriesFormat(Document seriesDocument) {
        Elements elements = seriesDocument.select("div.cb-col-100.cb-col.cb-nav-main.cb-bg-white").first()
                .select("div");
        String string = elements.get(1).text();
        System.out.println(string);
        for (String str : string.split(Pattern.quote(" . "))) {
            System.out.println(str);
        }
        return string.split(Pattern.quote(" . "))[0];
    }

    private ArrayList<Match> getSeriesMatchList(Document seriesDocument) {
        ArrayList<Match> matches = new ArrayList<>();
        Elements matchElements = seriesDocument.select("div.cb-col-60.cb-col.cb-srs-mtchs-tm");
        for (Element matchElement: matchElements) {
            Match match = Match.extractMatchData(matchElement, mSeriesFormat);
            if (match != null) {
                matches.add(match);
            }
        }
        return matches;
    }
}
