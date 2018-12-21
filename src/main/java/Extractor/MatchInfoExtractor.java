package Extractor;

import Configuration.Config;
import Model.Player;
import Model.Team;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class MatchInfoExtractor {
    private HashMap<String, String> mMatchInfo;

    public MatchInfoExtractor(Document iScorecardDoc) {
        mMatchInfo = extractMatchInfo(iScorecardDoc);
    }

    public MatchInfoExtractor() {
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

    public String extractStatus(Elements matchOutcomeElement) {
        String[] pattern = {"match tied", " won by ", "match drawn", " abandoned", "no result"};

        String outcome = matchOutcomeElement.text().toLowerCase();
        for (int index = 0; index < pattern.length; index++) {
            if (outcome.contains(pattern[index])) {
                return Config.MATCH_STATUS[index];
            }
        }
        return null;
    }

    public String extractFormat(String title, String seriesFormats) {
        String format = title.split(Pattern.quote(","))[1].toLowerCase();
        seriesFormats = seriesFormats.toLowerCase();

        String[] pattern = {"practice", "warm-up", "unofficial", " t20", " odi", " test"};
        String[] result = {null, null, null, Config.FORMATS[0], Config.FORMATS[1], Config.FORMATS[2]};
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

    public String extractWinningTeam(String status, String outcome) {
        String winningTeam = null;
        if (status != null && outcome != null) {
            if (status.equals("W")) {
                if (outcome.contains(" won by ")) {
                    winningTeam = outcome.split(Pattern.quote(" won by "))[0];
                } else {
                    winningTeam = outcome.split(Pattern.quote(" Won by "))[0];
                }
                winningTeam = Team.correctTeamName(winningTeam.strip());
            }
        }
        return winningTeam;
    }

    public String extractMatchDate() {
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

    public ArrayList<Team> extractPlayingTeams(Document scorecardDoc, String title) {
        String shortTitle = mMatchInfo.get("Match");

        ArrayList<Team> teams = new ArrayList<>();
        Team currentTeam = null;

        // Extract Full Name and Short Name of Playing Teams
        String[] fullNames = title.split(Pattern.quote(","))[0].split(Pattern.quote(" vs "));
        String[] shortNames = shortTitle.split(Pattern.quote(","))[0].split(Pattern.quote(" vs "));
        for (int index = 0; index < fullNames.length; index++) {
            teams.add(new Team(fullNames[index], shortNames[index]));
        }

        // Extract Squad of Playing Teams
        Elements squadElements = scorecardDoc.select("div.cb-col.cb-col-100.cb-minfo-tm-nm");
        for (Element squadElement : squadElements) {
            Elements playerElements = squadElement.select("a.margin0.text-black.text-hvr-underline");
            if (playerElements.size() == 0) {
                String teamName = squadElement.text();
                if (teamName.contains("Squad")) {
                    teamName = teamName.split(Pattern.quote("Squad"))[0].strip();
                    for (Team team : teams) {
                        if (team.getName().equals(teamName)) {
                            currentTeam = team;
                            break;
                        }
                    }
                }
            } else {
                for (Element playerElement : playerElements) {
                    assert currentTeam != null; // we should not hit this assert
                    currentTeam.addPlayer(new Player(playerElement));
                }
            }
        }
        return teams;
    }
}
