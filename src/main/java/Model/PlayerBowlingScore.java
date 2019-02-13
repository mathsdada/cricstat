package Model;

public class PlayerBowlingScore {
    private Player mPlayer;
    private String mOvers;
    private String mMaidens;
    private String mRuns;
    private String mWickets;
    private String mNoBalls;
    private String mWides;
    private String mEconomy;

    public PlayerBowlingScore(Player player,
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

    public Player getPlayer() {
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
