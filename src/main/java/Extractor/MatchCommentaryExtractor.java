package Extractor;

import Model.HeadToHead;
import Model.HeadToHeadData;
import Model.Player;
import Model.Team;
import Utility.Pair;
import Utility.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

public class MatchCommentaryExtractor {
    private HashMap<Integer, HashMap<Player, HashMap<Player, HeadToHead>>> mHeadToHeadPerInningsCache = new HashMap<>();

    public MatchCommentaryExtractor(Document commentaryDoc, ArrayList<Team> teams) {
        HashMap<Player, Team> playerTeamHashMap = getPlayerTeamHashMap(teams);
        Team currentBattingTeam = null;
        int inningsNumber = 0;
        Elements perBallCommentaryElements = commentaryDoc.select("p.cb-col.cb-col-90.cb-com-ln");
        for (Element perBallCommentaryElement : perBallCommentaryElements) {
            String[] perBallCommentary = perBallCommentaryElement.text().split(Pattern.quote(","));
            String[] players = perBallCommentary[0].split(Pattern.quote(" to "));
            if (players.length >= 2) {
                Pair<Player, Team> batsman = findPlayer(players[1].strip(), playerTeamHashMap);
                Pair<Player, Team> bowler = findPlayer(players[0].strip(), playerTeamHashMap);
                // Update Innings number in case of change in batting team
                if (batsman.getSecond() != currentBattingTeam) {
                    inningsNumber += 1;
                }
                currentBattingTeam = batsman.getSecond();
                getHeadToHeadFromCache(inningsNumber, batsman, bowler).setHeadToHeadData(extractHeadToHeadData(perBallCommentary));
            }
        }
    }

    public ArrayList<HeadToHead> getHeadToHead() {
        ArrayList<HeadToHead> headToHeadArrayList = new ArrayList<>();
        for (int innNum: mHeadToHeadPerInningsCache.keySet()) {
            for (Player batsman: mHeadToHeadPerInningsCache.get(innNum).keySet()) {
                for (Player bowler: mHeadToHeadPerInningsCache.get(innNum).get(batsman).keySet()) {
                    headToHeadArrayList.add(mHeadToHeadPerInningsCache.get(innNum).get(batsman).get(bowler));
                }
            }
        }
        return headToHeadArrayList;
    }
    private HeadToHeadData extractHeadToHeadData(String[] perBallCommentary) {
        if (perBallCommentary.length >= 3) {
            return HeadToHeadData.extractHeadToHeadData(perBallCommentary[1], perBallCommentary[2]);
        } else if (perBallCommentary.length >= 2) {
            return HeadToHeadData.extractHeadToHeadData(perBallCommentary[1], "");
        }
        return null;
    }

    private HeadToHead getHeadToHeadFromCache(int inningsNumber, Pair<Player, Team> batsman, Pair<Player, Team> bowler) {
        if (!mHeadToHeadPerInningsCache.containsKey(inningsNumber)) {
            mHeadToHeadPerInningsCache.put(inningsNumber, new HashMap<>());
        }
        if (!mHeadToHeadPerInningsCache.get(inningsNumber).containsKey(batsman.getFirst())) {
            mHeadToHeadPerInningsCache.get(inningsNumber).put(batsman.getFirst(), new HashMap<>());
        }
        if (!mHeadToHeadPerInningsCache.get(inningsNumber).get(batsman.getFirst()).containsKey(bowler.getFirst())) {
            mHeadToHeadPerInningsCache.get(inningsNumber).get(batsman.getFirst()).put(bowler.getFirst(), new HeadToHead(inningsNumber, batsman, bowler));
        }
        return mHeadToHeadPerInningsCache.get(inningsNumber).get(batsman.getFirst()).get(bowler.getFirst());
    }

    private HashMap<Player, Team> getPlayerTeamHashMap(ArrayList<Team> teams) {
        HashMap<Player, Team> playerTeamHashMap = new HashMap<>();
        for (Team team: teams) {
            for (Player player: team.getSquad()) {
                playerTeamHashMap.put(player, team);
            }
        }
        return playerTeamHashMap;
    }
    private Pair<Player, Team> findPlayer(String name, HashMap<Player, Team> playerTeamHashMap) {
        Player resPlayer = null;
        int curMaxLen = -1;
        for (Player player : playerTeamHashMap.keySet()) {
            int curMatchLen = StringUtils.longestCommonSubstringSize(name, player.getName());
            if (curMatchLen > curMaxLen) {
                curMaxLen = curMatchLen;
                resPlayer = player;
            }
        }
        return new Pair<>(resPlayer, playerTeamHashMap.get(resPlayer));
    }
}
