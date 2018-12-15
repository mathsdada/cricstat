import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class PlayerBattingScore {
    private String mPlayer;
    private String mStatus;
    private String mRuns;
    private String mBalls;
    private String mFours;
    private String mSixes;
    private String mStrikeRate;

    PlayerBattingScore(String player, String status, String runs, String balls, String fours, String sixes, String strikeRate) {
        mPlayer = player;
        mStatus = status;
        mRuns = runs;
        mBalls = balls;
        mFours = fours;
        mSixes = sixes;
        mStrikeRate = strikeRate;
    }

    static PlayerBattingScore extractPlayerBattingScore(Element battingScoreElement) {
        Element batsmanElement = battingScoreElement.select("div.cb-col.cb-col-27").first();
        if (batsmanElement == null) {
            return null;
        }
        Element batsmanInfoElement = batsmanElement.selectFirst("a");
        if (batsmanInfoElement == null) {
            return null;
        }
        String playerName = Player.correctName(batsmanElement.text());
        // Player Status
        String status = battingScoreElement.selectFirst("div.cb-col.cb-col-33").text();
        // [Runs, Balls, 4s, 6s, SR]
        ArrayList<String> scoreCols = new ArrayList<>(5);
        Elements scorecardCols = battingScoreElement.select("div.cb-col.cb-col-8.text-right");
        for (Element scorecardCol : scorecardCols) {
            scoreCols.add(scorecardCol.text());
        }
        return new PlayerBattingScore(playerName, status, scoreCols.get(0), scoreCols.get(1), scoreCols.get(2),
                scoreCols.get(3), scoreCols.get(4));
    }

    public String getPlayer() {
        return mPlayer;
    }

    public String getStatus() {
        return mStatus;
    }

    public String getRuns() {
        return mRuns;
    }

    public String getBalls() {
        return mBalls;
    }

    public String getFours() {
        return mFours;
    }

    public String getSixes() {
        return mSixes;
    }

    public String getStrikeRate() {
        return mStrikeRate;
    }

    @Override
    public String toString() {
        return "Batting Score: \nName : " + mPlayer +
                "\nS : " + mStatus +
                "\nR : " + mRuns +
                "\nB : " + mBalls +
                "\n4s : " + mFours +
                "\n6s : " + mSixes +
                "\nSR : " + mStrikeRate;
    }
}
