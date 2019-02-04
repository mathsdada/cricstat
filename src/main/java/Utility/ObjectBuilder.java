package Utility;

import Configuration.Config;
import Extractor.MatchInfoExtractor;
import Model.Match;
import Model.Player;
import Model.Series;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.regex.Pattern;

public class ObjectBuilder {
    public static class Series {
        public static Model.Series build(Element seriesElement, String year) {
            String seriesUrl = seriesElement.attr("href");
            // Validate Url Before proceeding
            if (!seriesUrl.contains("cricket-series") || seriesUrl.contains("qualifier") || seriesUrl.contains("warm-up") || seriesUrl.contains("practice")) {
                return null;
            }
            String seriesId = seriesUrl.split(Pattern.quote("/"))[2];
            String seriesTitle = seriesElement.text().toLowerCase();
            return new Model.Series(seriesId, seriesTitle, year, Config.HOMEPAGE + seriesUrl);
        }
    }
    public static class Match {
        public static Model.Match build(Element matchElement, String seriesFormats, HashMap<String, Model.Player> playerCacheMap) {
            MatchInfoExtractor matchInfoExtractor = new MatchInfoExtractor();

            Elements matchTitleElement = matchElement.select("a.text-hvr-underline");
            Elements matchOutcomeElement = matchElement.select("a.cb-text-complete");
            Elements matchVenueElement = matchElement.select("div.text-gray");
            // Model.Match Title & Link & ID
            String title = matchTitleElement.text().toLowerCase();
            String url = matchTitleElement.attr("href");
            if (url.contains("live-cricket-scores") ||
                    !url.contains("cricket-scores")) {
                return null;
            }
            String id = url.split(Pattern.quote("/"))[2];
            // Model.Match Venue
            String venue = null;
            if (matchVenueElement != null) {
                venue = matchVenueElement.text().toLowerCase();
            }
            // Model.Match Status and Outcome
            String outcome = null, status = null;
            if (matchOutcomeElement != null) {
                outcome = matchOutcomeElement.text().toLowerCase();
                status = matchInfoExtractor.extractStatus(matchOutcomeElement);
            }
            // Model.Match Format
            String format = matchInfoExtractor.extractFormat(title, seriesFormats);
            // Model.Match Winning Model.Team
            String winningTeam = matchInfoExtractor.extractWinningTeam(status, outcome);

            if (venue != null && status != null && format != null) {
                return new Model.Match(Config.HOMEPAGE + url, id, title, format, venue, status, outcome, winningTeam, playerCacheMap);
            }
            return null;
        }
    }
    public static class Player {
        public static Model.Player build(Element playerElement, HashMap<String, Model.Player> playerCacheMap) {
            // Ex :- href="/profiles/7776/marcus-harris"
            String playerUrl = playerElement.attr("href");
            String playerName = StringUtils.correctPlayerName(playerElement.text()).toLowerCase();

            String playerId = playerUrl.split(Pattern.quote("/"))[2];
            if(!playerCacheMap.containsKey(playerId)) {
                HashMap<String, String> profileMap = getProfileMap(Config.HOMEPAGE + playerUrl);
                playerCacheMap.put(playerId,
                        new Model.Player(playerId, playerName, profileMap.get("role"), profileMap.get("batting style"), profileMap.get("bowling style")));
            }
            return playerCacheMap.get(playerId);
        }
        private static HashMap<String, String> getProfileMap(String playerUrl) {
            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("role",  null);
            profileMap.put("batting style", null);
            profileMap.put("bowling style", null);

            Document playerDoc = ScraperUtils.getDocument(playerUrl);
            Elements keyElements = playerDoc.select("div.cb-col.cb-col-40.text-bold.cb-lst-itm-sm");
            Elements valElements = playerDoc.select("div.cb-col.cb-col-60.cb-lst-itm-sm");
            for (int i=0; (i<keyElements.size()) && (i<valElements.size()); i++) {
                String key = keyElements.get(i).text().strip().toLowerCase();
                String val = valElements.get(i).text().strip().toLowerCase();
                if(profileMap.containsKey(key)) {
                    profileMap.put(key, val);
                }
            }
            return profileMap;
        }

    }
}
