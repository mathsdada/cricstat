package Model;

import Configuration.Config;
import Utility.ScraperUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.regex.Pattern;

public class Player {
    private String mId;
    private String mName;
    private String mRole;
    private String mBattingStyle;
    private String mBowlingStyle;


    private Player(String id, String name, String role, String battingStyle, String bowlingStyle) {
        mId = id;
        mName = name;
        mRole = role;
        mBattingStyle = battingStyle;
        mBowlingStyle = bowlingStyle;
    }

    public static Player extract(Element playerElement, HashMap<String, Player> playerCacheMap) {
        // Ex :- href="/profiles/7776/marcus-harris"
        String playerUrl = playerElement.attr("href");
        String playerName = correctName(playerElement.text());

        String playerId = playerUrl.split(Pattern.quote("/"))[2];
        if(!playerCacheMap.containsKey(playerId)) {
            HashMap<String, String> profileMap = getProfileMap(Config.HOMEPAGE + playerUrl);
            playerCacheMap.put(playerId,
                    new Player(playerId, playerName,
                            profileMap.get("Role"), profileMap.get("Batting Style"), profileMap.get("Bowling Style")));
        }
        return playerCacheMap.get(playerId);
    }

    private static HashMap<String, String> getProfileMap(String playerUrl) {
        HashMap<String, String> profileMap = new HashMap<>();
        profileMap.put("Role",  null);
        profileMap.put("Batting Style", null);
        profileMap.put("Bowling Style", null);

        Document playerDoc = ScraperUtils.getDocument(playerUrl);
        Elements keyElements = playerDoc.select("div.cb-col.cb-col-40.text-bold.cb-lst-itm-sm");
        Elements valElements = playerDoc.select("div.cb-col.cb-col-60.cb-lst-itm-sm");
        for (int i=0; (i<keyElements.size()) && (i<valElements.size()); i++) {
            String key = keyElements.get(i).text().strip();
            String val = valElements.get(i).text().strip();
            if(profileMap.containsKey(key)) {
                profileMap.put(key, val);
            }
        }
        return profileMap;
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

    public static String correctName(String name) {
        name = name
                .replace("(c)", "")
                .replace("(wk)", "")
                .replace("(c & wk)", "")
                .strip();
        if (name.endsWith(" sub")) {
            name = name.replace(" sub", "").strip();
        } else if (name.endsWith(" Sub")) {
            name = name.replace(" Sub", "").strip();
        }
        return name;
    }
}
