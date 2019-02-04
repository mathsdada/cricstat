package Model;

import Utility.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class PlayerBowlingScore {
    private String mPlayer;
    private String mOvers;
    private String mMaidens;
    private String mRuns;
    private String mWickets;
    private String mNoBalls;
    private String mWides;
    private String mEconomy;

    private PlayerBowlingScore(String player,
                              String overs, String maidens, String wickets, String noBalls, String wides,
                              String runs, String economy) {
        mPlayer = player;
        mOvers = overs;
        mMaidens = maidens;
        mRuns = runs;
        mWickets = wickets;
        mNoBalls = noBalls;
        mWides = wides;
        mEconomy = economy;
    }

    public static PlayerBowlingScore extractPlayerBowlingScore(Element bowlingScoreElement) {
        Element bowlerElement = bowlingScoreElement.select("div.cb-col.cb-col-40").first();
        if (bowlerElement == null) {
            return null;
        }
        Element bowlerInfoElement = bowlerElement.selectFirst("a");
        if (bowlerInfoElement == null) {
            return null;
        }
        String playerName = StringUtils.correctPlayerName(bowlerElement.text());
        // [Overs, Maidens, Wickets, NB, Wides, Runs, Eco]
        ArrayList<String> scoreCols = new ArrayList<>(7);
        // [Overs, Maidens, Wickets, NB, Wides]
        Elements scorecardCols = bowlingScoreElement.select("div.cb-col.cb-col-8.text-right");
        for (Element scorecardCol : scorecardCols) {
            scoreCols.add(scorecardCol.text());
        }
        // [Runs, Eco]
        scorecardCols = bowlingScoreElement.select("div.cb-col.cb-col-10.text-right");
        for (Element scorecardCol : scorecardCols) {
            scoreCols.add(scorecardCol.text());
        }
        return new PlayerBowlingScore(playerName, scoreCols.get(0), scoreCols.get(1), scoreCols.get(2), scoreCols.get(3),
                scoreCols.get(4), scoreCols.get(5), scoreCols.get(6));
    }

    public String getPlayer() {
        return mPlayer;
    }

    public String getOvers() {
        return mOvers;
    }

    public String getMaidens() {
        return mMaidens;
    }

    public String getRuns() {
        return mRuns;
    }

    public String getWickets() {
        return mWickets;
    }

    public String getNoBalls() {
        return mNoBalls;
    }

    public String getWides() {
        return mWides;
    }

    public String getEconomy() {
        return mEconomy;
    }

    @Override
    public String toString() {
        return "Bowling Score: \nName : " + mPlayer +
               "\nO : " + mOvers +
               "\nM : " + mMaidens +
               "\nR : " + mRuns +
               "\nW : " + mWickets +
               "\nNB : " + mNoBalls +
               "\nWD : " + mWides +
               "\nECO : " + mEconomy;
    }
}
