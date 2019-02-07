package Model;

public class PlayerBattingScore {
    private String mPlayer;
    private String mStatus;
    private String mRuns;
    private String mBalls;
    private String mFours;
    private String mSixes;
    private String mStrikeRate;

    public PlayerBattingScore(String player, String status, String runs, String balls, String fours, String sixes, String strikeRate) {
        mPlayer = player;
        mStatus = status;
        mRuns = runs;
        mBalls = balls;
        mFours = fours;
        mSixes = sixes;
        mStrikeRate = strikeRate;
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
