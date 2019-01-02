package Model;

import Configuration.Config;
import Utility.ScraperUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class Season {
    private String mYear;
    private ArrayList<Series> mSeriesList;

    public Season(String  year) {
        mYear = year;
        mSeriesList = new ArrayList<>();
        extractSeriesList();
    }

    private void extractSeriesList() {
        String seasonUrl = Config.HOMEPAGE + "/cricket-scorecard-archives/" + mYear;
        Document seasonDoc = ScraperUtils.getDocument(seasonUrl);
        Elements seriesElements = seasonDoc.select("a.text-hvr-underline");
        for (Element seriesElement: seriesElements) {
            Series series = Series.extractSeries(seriesElement, mYear);
            if (series != null) {
                mSeriesList.add(series);
            }
        }
    }
    public String  getYear() {
        return mYear;
    }

    public ArrayList<Series> getSeriesList() {
        return mSeriesList;
    }
}
