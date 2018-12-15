public class InningsScoreHeader {
    private String mRuns;
    private String mWickets;
    private String mOvers;

    public InningsScoreHeader(String mRuns, String mWickets, String mOvers) {
        this.mRuns = mRuns;
        this.mWickets = mWickets;
        this.mOvers = mOvers;
    }

    public String getRuns() {
        return mRuns;
    }

    public String getWickets() {
        return mWickets;
    }

    public String getOvers() {
        return mOvers;
    }
}
