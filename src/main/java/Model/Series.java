package Model;

import Configuration.Config;
import Utility.ObjectBuilder;
import Utility.ScraperUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class Series {
    private String mId;
    private String mTitle;
    private String mYear;
    private String mUrl;
    private ArrayList<Match> mMatches;


    public Series(String id, String title, String year, String url) {
        mId = id;
        mTitle = title;
        mYear = year;
        mUrl = url;
        mMatches = new ArrayList<>();
    }

    void scrape() {
        // Maintain player Cache to avoid repeatedly scraping player profile for each match in a particular series.
        // This is valid only if all matches of a series gets scraped by single thread. This logic need to changed if threading behavior changes
        // Key : Player ID, Value : Player
        HashMap<String, Player> playerCacheMap = new HashMap<>();
        Document seriesDocument = ScraperUtils.getDocument(mUrl);
        String seriesFormat = getSeriesFormat(seriesDocument).toLowerCase();

        Elements matchElements = seriesDocument.select("div.cb-col-60.cb-col.cb-srs-mtchs-tm");
        for (Element matchElement: matchElements) {
            Match match = ObjectBuilder.Match.build(matchElement, seriesFormat, playerCacheMap);
            if (match != null) {
                mMatches.add(match);
            }
        }
    }

    private static String getSeriesFormat(Document seriesDocument) {
        Elements elements = seriesDocument.select("div.cb-col-100.cb-col.cb-nav-main.cb-bg-white").first()
                .select("div");
        return elements.get(1).text().split(Pattern.quote(" . "))[0];
    }

    public String getTitle() {
        return mTitle;
    }

    public ArrayList<Match> getMatches() {
        return mMatches;
    }

    public String getId() {
        return mId;
    }
    public String getYear() {
        return mYear;
    }

    /* DB Operations */
    public boolean insertSeries(Connection connection) throws SQLException {
        if (!findSeriesById(connection, Integer.parseInt(mId))) {
            String SQL = "INSERT INTO series VALUES(?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(SQL);
            preparedStatement.setInt(1, Integer.parseInt(mId));
            preparedStatement.setString(2, mTitle);
            preparedStatement.setString(3, "Men");
            preparedStatement.setString(4, mYear);

            return (0 != preparedStatement.executeUpdate());
        } else {
            System.out.println("Series Available in DB : " + mId);
            return false;
        }
    }

    private boolean findSeriesById(Connection connection, int id) throws SQLException {
        String SQL = "SELECT count(*) FROM series WHERE series.id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(SQL);
        preparedStatement.setInt(1, id);
        ResultSet resultSet =preparedStatement.executeQuery();
        resultSet.next();
        return (0!=resultSet.getInt(1));
    }
}
