package Scraper;

import Common.Configuration;
import Common.Pair;
import Common.StringUtils;
import Model.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

class Match {
    static Model.Match build(Element matchElement, String seriesFormats, HashMap<String, Model.Player> playerCacheMap) {
        MatchInfoExtractor matchInfoExtractor = new MatchInfoExtractor();

        Elements matchTitleElement = matchElement.select("a.text-hvr-underline");
        Elements matchOutcomeElement = matchElement.select("a.cb-text-complete");
        Elements matchVenueElement = matchElement.select("div.text-gray");
        // Match Title & Link & ID
        String title = matchTitleElement.text().toLowerCase();
        String url = matchTitleElement.attr("href");
        if (url.contains("live-cricket-scores") ||
                !url.contains("cricket-scores")) {
            return null;
        }
        String id = url.split(Pattern.quote("/"))[2];
        /* If matchID is already available in DB then do not scrape that match again */
        if (Model.Match.dbOpCheckId(id)) {
            return null;
        }
        // Match Venue
        String venue = null;
        if (matchVenueElement != null) {
            venue = matchVenueElement.text().toLowerCase();
        }
        // Match Status and Outcome
        String outcome = null, status = null;
        if (matchOutcomeElement != null) {
            outcome = matchOutcomeElement.text().toLowerCase();
            status = matchInfoExtractor.extractStatus(matchOutcomeElement);
        }
        // Match Format
        String format = matchInfoExtractor.extractFormat(title, seriesFormats);
        // Match Winning Team
        String winningTeam = matchInfoExtractor.extractWinningTeam(status, outcome);

        if (venue != null && status != null && format != null) {
            Model.Match match = new Model.Match(Configuration.HOMEPAGE + url, id, title, format, venue, status, outcome, winningTeam);
            scrape(match, playerCacheMap);
            return match;
        }
        return null;
    }

    private static void scrape(Model.Match match, HashMap<String, Model.Player> playerCacheMap) {
        String scoreCardUrl = Configuration.HOMEPAGE + "/api/html/cricket-scorecard/" + match.getId();
        Document iScorecardDoc = Common.getDocument(scoreCardUrl);
        Document commentaryDoc = Common.getDocument(match.getUrl());

        MatchInfoExtractor matchInfoExtractor = new MatchInfoExtractor(iScorecardDoc);
        match.setDate(matchInfoExtractor.extractMatchDate());
        match.setTeams(matchInfoExtractor.extractPlayingTeams(iScorecardDoc, match.getTitle(), playerCacheMap));
        match.setInningsScores(new MatchScoreExtractor().extractMatchScores(iScorecardDoc, match.getTeams()));
        match.setHeadToHeadList(new MatchCommentaryExtractor(commentaryDoc, match.getTeams()).getHeadToHead());
    }

    private static class MatchInfoExtractor {
        private HashMap<String, String> mMatchInfo;

        MatchInfoExtractor(Document iScorecardDoc) {
            mMatchInfo = extractMatchInfo(iScorecardDoc);
        }

        MatchInfoExtractor() {
        }

        private HashMap<String, String> extractMatchInfo(Document scorecardDoc) {
            HashMap<String, String> matchInfo = new HashMap<>();
            Elements matchInfoElements = scorecardDoc.select("div.cb-col.cb-col-100.cb-mtch-info-itm");
            for (Element matchInfoElement : matchInfoElements) {
                String key = matchInfoElement.select("div.cb-col.cb-col-27").text().strip();
                String value = matchInfoElement.select("div.cb-col.cb-col-73").text().strip();
                matchInfo.put(key, value);
            }
            return matchInfo;
        }

        String extractStatus(Elements matchOutcomeElement) {
            String[] pattern = {"match tied", " won by ", "match drawn", " abandoned", "no result"};

            String outcome = matchOutcomeElement.text().toLowerCase();
            for (int index = 0; index < pattern.length; index++) {
                if (outcome.contains(pattern[index])) {
                    return Configuration.MATCH_STATUS[index];
                }
            }
            return null;
        }

        String extractFormat(String title, String seriesFormats) {
            String format = title.split(Pattern.quote(","))[1].toLowerCase();
            seriesFormats = seriesFormats.toLowerCase();

            String[] pattern = {"practice", "warm-up", "unofficial", " t20", " odi", " test"};
            String[] result = {null, null, null, Configuration.FORMATS[0], Configuration.FORMATS[1], Configuration.FORMATS[2]};
            String[] inputs = {format, seriesFormats};
            for (String inputStr : inputs) {
                for (int index = 0; index < pattern.length; index++) {
                    if (inputStr.contains(pattern[index])) {
                        return result[index];
                    }
                }
            }
            return null;
        }

        String extractWinningTeam(String status, String outcome) {
            String winningTeam = null;
            if (status != null && outcome != null) {
                if (status.equals("W")) {
                    if (outcome.contains(" won by ")) {
                        winningTeam = outcome.split(Pattern.quote(" won by "))[0];
                    } else {
                        winningTeam = outcome.split(Pattern.quote(" Won by "))[0];
                    }
                    winningTeam = StringUtils.correctTeamName(winningTeam.strip());
                }
            }
            if (winningTeam != null) winningTeam = winningTeam.toLowerCase();
            return winningTeam;
        }

        String extractMatchDate() {
            String dateStr = mMatchInfo.get("Date");
            //        Examples: 1) Friday, January 05, 2018 - Tuesday, January 09, 2018
            //                  2) Tuesday, February 13, 2018
            dateStr = dateStr.split(Pattern.quote(" - "))[0].strip();
            SimpleDateFormat inputSdf = new SimpleDateFormat("EEEE, MMM dd, yyyy");
            SimpleDateFormat outputSdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                return outputSdf.format(inputSdf.parse(dateStr));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        ArrayList<Team> extractPlayingTeams(Document scorecardDoc, String title, HashMap<String, Model.Player> playerCacheMap) {
            String shortTitle = mMatchInfo.get("Match");

            ArrayList<Team> teams = new ArrayList<>();
            Team currentTeam = null;

            // Extract Full Name and Short Name of Playing Teams
            String[] fullNames = title.split(Pattern.quote(","))[0].split(Pattern.quote(" vs "));
            String[] shortNames = shortTitle.split(Pattern.quote(","))[0].split(Pattern.quote(" vs "));
            for (int index = 0; index < fullNames.length; index++) {
                teams.add(new Team(fullNames[index].toLowerCase(), shortNames[index].toLowerCase()));
            }

            // Extract Squad of Playing Teams
            Elements squadElements = scorecardDoc.select("div.cb-col.cb-col-100.cb-minfo-tm-nm");
            for (Element squadElement : squadElements) {
                Elements playerElements = squadElement.select("a.margin0.text-black.text-hvr-underline");
                if (playerElements.size() == 0) {
                    String teamName = squadElement.text().toLowerCase();
                    if (teamName.contains("squad")) {
                        teamName = teamName.split(Pattern.quote("squad"))[0].strip();
                        for (Team team : teams) {
                            if (team.getName().equals(teamName)) {
                                currentTeam = team;
                                break;
                            }
                        }
                    }
                } else {
                    for (Element playerElement : playerElements) {
                        assert currentTeam != null; // we should not be hitting this assert
                        currentTeam.addPlayer(Player.build(playerElement, playerCacheMap));
                    }
                }
            }
            return teams;
        }
    }

    private static class MatchScoreExtractor {
        ArrayList<InningsScore> extractMatchScores(Document iScorecardDoc, ArrayList<Team> playingTeams) {
            ArrayList<InningsScore> inningsScores = new ArrayList<>();
            Elements inningsElements = iScorecardDoc.select("div[id]");
            for (int inningNum = 0; inningNum < inningsElements.size(); inningNum++) {
                InningsScore inningsScore = extractInningsScoreHeader(inningsElements.get(inningNum), playingTeams, inningNum);
                inningsScore.setPlayerBattingScores(extractInningsBattingScores(inningsElements.get(inningNum)));
                inningsScore.setPlayerBowlingScores(extractInningsBowlingScores(inningsElements.get(inningNum)));
                inningsScores.add(inningsScore);
            }
            return inningsScores;
        }

        private InningsScore extractInningsScoreHeader(Element inningsElement, ArrayList<Team> playingTeams, int inningsNum) {
            String inningsScoreHeader;
            Team battingTeamObj, bowlingTeamObj;

            inningsScoreHeader = inningsElement.selectFirst("div.cb-col.cb-col-100.cb-scrd-hdr-rw").text();
            // inningsScoreHeader Example : "England 1st Innings 302-10 (116.4)"
            String[] inningsScoreData = inningsScoreHeader.split(Pattern.quote(" Innings "));
            String battingTeam = inningsScoreData[0]
                    .replace(" 1st", "")
                    .replace(" 2nd", "")
                    .strip().toLowerCase();
            String runs = inningsScoreData[1].split(Pattern.quote(" "))[0].split(Pattern.quote("-"))[0];
            String wickets = inningsScoreData[1].split(Pattern.quote(" "))[0].split(Pattern.quote("-"))[1];
            String overs = inningsScoreData[1].split(Pattern.quote(" "))[1].replace("(", "").replace(")", "").strip();
            if (battingTeam.equals(playingTeams.get(0).getName())) {
                battingTeamObj = playingTeams.get(0);
                bowlingTeamObj = playingTeams.get(1);
            } else {
                battingTeamObj = playingTeams.get(1);
                bowlingTeamObj = playingTeams.get(0);
            }
            InningsScoreHeader scoreHeader = new InningsScoreHeader(runs, wickets, overs);
            InningsScore inningsScore = new InningsScore(inningsNum, battingTeamObj, bowlingTeamObj);
            inningsScore.setScoreHeader(scoreHeader);
            return inningsScore;
        }

        private ArrayList<PlayerBattingScore> extractInningsBattingScores(Element inningsElement) {
            ArrayList<PlayerBattingScore> playerBattingScores = new ArrayList<>();
            Element inningsBattingScoresElement = inningsElement.select("div.cb-col.cb-col-100.cb-ltst-wgt-hdr").first();

            Elements battingScoreElements = inningsBattingScoresElement.select("div.cb-col.cb-col-100.cb-scrd-itms");
            for (Element battingScoreElement : battingScoreElements) {
                PlayerBattingScore playerBattingScore = extractPlayerBattingScore(battingScoreElement);
                if (playerBattingScore != null) {
                    playerBattingScores.add(playerBattingScore);
                }
            }
            return playerBattingScores;
        }

        private ArrayList<PlayerBowlingScore> extractInningsBowlingScores(Element inningsElement) {
            ArrayList<PlayerBowlingScore> playerBowlingScores = new ArrayList<>();
            Element inningsBowlingScoresElement = inningsElement.select("div.cb-col.cb-col-100.cb-ltst-wgt-hdr").last();

            Elements bowlingScoreElements = inningsBowlingScoresElement.select("div.cb-col.cb-col-100.cb-scrd-itms");
            for (Element bowlingScoreElement : bowlingScoreElements) {
                PlayerBowlingScore playerBowlingScore = extractPlayerBowlingScore(bowlingScoreElement);
                if (playerBowlingScore != null) {
                    playerBowlingScores.add(playerBowlingScore);
                }
            }
            return playerBowlingScores;
        }

        static PlayerBattingScore extractPlayerBattingScore(Element battingScoreElement) {
            Element batsmanElement = battingScoreElement.select("div.cb-col.cb-col-27").first();
            if (batsmanElement == null) {
                return null;
            }
            Element batsmanInfoElement = batsmanElement.selectFirst("a");
            if (batsmanInfoElement == null) {
                return null;
            }
            String playerName = StringUtils.correctPlayerName(batsmanElement.text());
            // Player Status
            String status = battingScoreElement.selectFirst("div.cb-col.cb-col-33").text();
            // [Runs, Balls, 4s, 6s, SR]
            ArrayList<String> scoreCols = new ArrayList<>(5);
            Elements scorecardCols = battingScoreElement.select("div.cb-col.cb-col-8.text-right");
            for (Element scorecardCol : scorecardCols) {
                scoreCols.add(scorecardCol.text());
            }
            return new PlayerBattingScore(playerName, status, scoreCols.get(0), scoreCols.get(1), scoreCols.get(2),
                    scoreCols.get(3), scoreCols.get(4));
        }

        static PlayerBowlingScore extractPlayerBowlingScore(Element bowlingScoreElement) {
            Element bowlerElement = bowlingScoreElement.select("div.cb-col.cb-col-40").first();
            if (bowlerElement == null) {
                return null;
            }
            Element bowlerInfoElement = bowlerElement.selectFirst("a");
            if (bowlerInfoElement == null) {
                return null;
            }
            String playerName = StringUtils.correctPlayerName(bowlerElement.text());
            // [Overs, Maidens, Wickets, NB, Wides, Runs, Eco]
            ArrayList<String> scoreCols = new ArrayList<>(7);
            // [Overs, Maidens, Wickets, NB, Wides]
            Elements scorecardCols = bowlingScoreElement.select("div.cb-col.cb-col-8.text-right");
            for (Element scorecardCol : scorecardCols) {
                scoreCols.add(scorecardCol.text());
            }
            // [Runs, Eco]
            scorecardCols = bowlingScoreElement.select("div.cb-col.cb-col-10.text-right");
            for (Element scorecardCol : scorecardCols) {
                scoreCols.add(scorecardCol.text());
            }
            return new PlayerBowlingScore(playerName, scoreCols.get(0), scoreCols.get(1), scoreCols.get(2), scoreCols.get(3),
                    scoreCols.get(4), scoreCols.get(5), scoreCols.get(6));
        }

    }

    private static class MatchCommentaryExtractor {
        private HashMap<Integer, HashMap<Model.Player, HashMap<Model.Player, HeadToHead>>>
                mHeadToHeadPerInningsCache = new HashMap<>();

        MatchCommentaryExtractor(Document commentaryDoc, ArrayList<Team> teams) {
            HashMap<Model.Player, Team> playerTeamHashMap = getPlayerTeamHashMap(teams);
            Team currentBattingTeam = null;
            int inningsNumber = 0;
            Elements perBallCommentaryElements = commentaryDoc.select("p.cb-col.cb-col-90.cb-com-ln");
            for (Element perBallCommentaryElement : perBallCommentaryElements) {
                String[] perBallCommentary = perBallCommentaryElement.text().split(Pattern.quote(","));
                String[] players = perBallCommentary[0].split(Pattern.quote(" to "));
                if (players.length >= 2) {
                    Pair<Model.Player, Team> batsman = findPlayer(players[1].strip().toLowerCase(), playerTeamHashMap);
                    Pair<Model.Player, Team> bowler = findPlayer(players[0].strip().toLowerCase(), playerTeamHashMap);
                    // Update Innings number in case of change in batting team
                    if (batsman.getSecond() != currentBattingTeam) {
                        inningsNumber += 1;
                    }
                    currentBattingTeam = batsman.getSecond();
                    getHeadToHeadFromCache(inningsNumber, batsman, bowler).setHeadToHeadData(
                            extractHeadToHeadData(perBallCommentary));
                }
            }
        }

        ArrayList<HeadToHead> getHeadToHead() {
            ArrayList<HeadToHead> headToHeadArrayList = new ArrayList<>();
            for (int innNum: mHeadToHeadPerInningsCache.keySet()) {
                for (Model.Player batsman: mHeadToHeadPerInningsCache.get(innNum).keySet()) {
                    for (Model.Player bowler: mHeadToHeadPerInningsCache.get(innNum).get(batsman).keySet()) {
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

        private HeadToHead getHeadToHeadFromCache(int inningsNumber, Pair<Model.Player, Team> batsman, Pair<Model.Player, Team> bowler) {
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

        private HashMap<Model.Player, Team> getPlayerTeamHashMap(ArrayList<Team> teams) {
            HashMap<Model.Player, Team> playerTeamHashMap = new HashMap<>();
            for (Team team: teams) {
                for (Model.Player player: team.getSquad()) {
                    playerTeamHashMap.put(player, team);
                }
            }
            return playerTeamHashMap;
        }
        private Pair<Model.Player, Team> findPlayer(String name, HashMap<Model.Player, Team> playerTeamHashMap) {
            Model.Player resPlayer = null;
            int curMaxLen = -1;
            for (Model.Player player : playerTeamHashMap.keySet()) {
                int curMatchLen = StringUtils.longestCommonSubstringSize(name, player.getName());
                if (curMatchLen > curMaxLen) {
                    curMaxLen = curMatchLen;
                    resPlayer = player;
                }
            }
            return new Pair<>(resPlayer, playerTeamHashMap.get(resPlayer));
        }
    }
}
