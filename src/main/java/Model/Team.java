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
            case "West Indies": return "Windies";
            case "UAE": return "United Arab Emirates";
            case "HK": return "Hong Kong";
            case "Marylebone Cricket Club World XI": return "MCC World XI";
            case "Pakistan U-19": return "Pakistan U19";
            case "West Indies Women": return "Windies Women";
            case "Rising Pune Supergiants": return "Rising Pune Supergiant";
            case "St Lucia Zouks": return "St Lucia Stars";
            case "Cobras": return "Cape Cobras";
            case "West Indies U19": return "Windies U19";
            case "West Indies A": return "Windies A";
            case "Trinidad & Tobago": return "Trinidad and Tobago";
            case "Wayamba": return "Wayamba Elevens";
            case "RSA": return "South Africa";
            case "SL": return "Sri Lanka";
            default:
                return teamName;
        }
    }
}
