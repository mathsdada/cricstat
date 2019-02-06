package Model;

public class Player {
    private String mId;
    private String mName;
    private String mRole;
    private String mBattingStyle;
    private String mBowlingStyle;


    public Player(String id, String name, String role, String battingStyle, String bowlingStyle) {
        mId = id;
        mName = name;
        mRole = role;
        mBattingStyle = battingStyle;
        mBowlingStyle = bowlingStyle;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getRole() {
        return mRole;
    }

    public String getBattingStyle() {
        return mBattingStyle;
    }

    public String getBowlingStyle() {
        return mBowlingStyle;
    }
}
