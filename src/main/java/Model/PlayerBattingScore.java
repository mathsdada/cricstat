package Model;

public class PlayerBattingScore {
    private Player mBatsman;
    private String mStatus;
    private String mRuns;
    private String mBalls;
    private String mFours;
    private String mSixes;
    private String mStrikeRate;
    private Player mBowler;

    public PlayerBattingScore(Player player, String status, String runs, String balls, String fours, String sixes, String strikeRate, Player bowler) {
        mBatsman = player;
        mStatus = status;
        mRuns = runs;
        mBalls = balls;
        mFours = fours;
        mSixes = sixes;
        mStrikeRate = strikeRate;
        mBowler = bowler;
    }

    public Player getBatsman() {
        return mBatsman;
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

    public Player getBowler() {
        return mBowler;
    }

    @Override
    public String toString() {
        return "Batting Score: \nName : " + mBatsman +
                "\nS : " + mStatus +
                "\nR : " + mRuns +
                "\nB : " + mBalls +
                "\n4s : " + mFours +
                "\n6s : " + mSixes +
                "\nSR : " + mStrikeRate;
    }
}
