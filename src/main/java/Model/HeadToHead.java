package Model;

import Utility.Pair;

import java.security.KeyPair;

public class HeadToHead {
    private int mInningsNum;
    private Pair<Player, Team> mBatsman;
    private Pair<Player, Team> mBowler;
    private HeadToHeadData mHeadToHeadData;

    public HeadToHead(int inningsNumber, Pair<Player, Team> batsman, Pair<Player, Team> bowler) {
        mInningsNum = inningsNumber;
        mBatsman = batsman;
        mBowler = bowler;
        mHeadToHeadData = null;
    }

    public Pair<Player, Team> getBatsman() {
        return mBatsman;
    }

    public Pair<Player, Team> getBowler() {
        return mBowler;
    }

    public HeadToHeadData getHeadToHeadData() {
        return mHeadToHeadData;
    }

    public void setHeadToHeadData(HeadToHeadData headToHeadData) {
        if (headToHeadData == null) return;
        if (mHeadToHeadData == null) {
            mHeadToHeadData = headToHeadData;
        } else {
            HeadToHeadData.add(mHeadToHeadData, headToHeadData);
        }
    }
}
