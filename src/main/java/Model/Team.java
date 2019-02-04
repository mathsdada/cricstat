package Model;

import java.util.ArrayList;

public class Team {
    private String mName;
    private String mShortName;
    private ArrayList<Player> mSquad;

    public Team(String mName, String mShortName) {
        this.mName = mName;
        this.mShortName = mShortName;
        mSquad = new ArrayList<>();
    }

    public String getName() {
        return mName;
    }

    public String getShortName() {
        return mShortName;
    }

    public void addPlayer(Player player) {
        mSquad.add(player);
    }

    public ArrayList<Player> getSquad() {
        return mSquad;
    }

    public static String correctTeamName(String teamName) {
        switch (teamName) {
            case "west indies": return "windies";
            case "uae": return "united arab emirates";
            case "hk": return "hong kong";
            case "marylebone cricket club world xi": return "mcc world xi";
            case "pakistan u-19": return "pakistan u19";
            case "west indies women": return "windies women";
            case "rising pune supergiants": return "rising pune supergiant";
            case "st lucia zouks": return "st lucia stars";
            case "cobras": return "cape cobras";
            case "west indies u19": return "windies u19";
            case "west indies a": return "windies a";
            case "trinidad & tobago": return "trinidad and tobago";
            case "wayamba": return "wayamba elevens";
            case "rsa": return "south africa";
            case "sl": return "sri lanka";
            default:
                return teamName;
        }
    }
}
