import Utilities.ScraperUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

class Match {
    private String mId;
    private String mTitle;
    private String mFormat;
    private String mVenue;
    private String mDate;
    private String mStatus;
    private String mOutcome;
    private String mWinningTeam;

    private ArrayList<Team> mTeams;
    private ArrayList<InningsScore> mInningsScores;

    public Match(String id, String title, String format, String venue, String status, String outcome, String winningTeam) {
        mId = id;
        mTitle = title;
        mFormat = format;
        mVenue = venue;
        mStatus = status;
        mOutcome = outcome;
        mWinningTeam = winningTeam;
    }

    void scrape() {
        String scoreCardUrl = Config.HOMEPAGE + "/api/html/cricket-scorecard/" + mId;
        Document iScorecardDoc = ScraperUtils.getDocument(scoreCardUrl);

        HashMap<String, String> iMatchInfoMap = extractMatchInfo(iScorecardDoc);
        mDate = extractMatchDate(iMatchInfoMap.get("Date"));
        mTeams = extractPlayingTeams(iScorecardDoc, mTitle, iMatchInfoMap.get("Match"));
        extractMatchScores(iScorecardDoc, mTeams);
    }

    /* Extractor Private Methods */
    public static Match extractMatchData(Element matchElement, String seriesFormats) {
        Elements matchTitleElement = matchElement.select("a.text-hvr-underline");
        Elements matchOutcomeElement = matchElement.select("a.cb-text-link");
        Elements matchVenueElement = matchElement.select("div.text-gray");
        // Match Title & Link & ID
        String title = matchTitleElement.text();
        String url = matchTitleElement.attr("href");
        String id = url.split(Pattern.quote("/"))[2];
        url = Config.HOMEPAGE + url;
        // Match Venue
        String venue = null;
        if (matchVenueElement != null) {
            venue = matchVenueElement.text();
        }
        // Match Status and Outcome
        String outcome = null, status = null;
        if (matchOutcomeElement != null) {
            outcome = matchOutcomeElement.text();
            status = extractStatus(matchOutcomeElement);
        }
        // Match Format
        String format = extractFormat(title, seriesFormats);
        // Match Winning Team
        String winningTeam = extractWinningTeam(status, outcome);

        if (url.contains("cricket-scores") && venue != null && status != null && format != null) {
            return new Match(id, title, format, venue, status, outcome, winningTeam);
        }
        return null;
    }

    private static String extractStatus(Elements matchOutcomeElement) {
        String[] pattern = {"match tied", " won by ", "match drawn", " abandoned", "no result"};

        String outcome = matchOutcomeElement.text().toLowerCase();
        for (int index = 0; index < pattern.length; index++) {
            if(outcome.contains(pattern[index])) {
                return Config.MATCH_STATUS[index];
            }
        }
        return null;
    }

    private static String extractFormat(String title, String seriesFormats) {
        String format = title.split(Pattern.quote(","))[1].toLowerCase();
        seriesFormats = seriesFormats.toLowerCase();

        String[] pattern = {"practice", "warm-up", "unofficial", " t20", " odi", " test"};
        String[] result =  {null, null, null, Config.FORMATS[0], Config.FORMATS[1], Config.FORMATS[2]};
        String[] inputs = {format, seriesFormats};
        for (String inputStr : inputs) {
            for (int index = 0; index < pattern.length; index++) {
                if(inputStr.contains(pattern[index])) {
                    return result[index];
                }
            }
        }
        return null;
    }

    private static String extractWinningTeam(String status, String outcome) {
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

    private String extractMatchDate(String dateStr) {
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

    private ArrayList<Team> extractPlayingTeams(Document scorecardDoc, String title, String shortTitle) {
        ArrayList<Team> teams = new ArrayList<>();
        Team currentTeam = null;

        // Extract Full Name and Short Name of Playing Teams
        String[] fullNames = title.split(Pattern.quote(","))[0].split(Pattern.quote(" vs "));
        String[] shortNames = shortTitle.split(Pattern.quote(","))[0].split(Pattern.quote(" vs "));
        for (int index=0; index < fullNames.length; index++) {
            teams.add(new Team(fullNames[index], shortNames[index]));
        }

        // Extract Squad of Playing Teams
        Elements squadElements = scorecardDoc.select("div.cb-col.cb-col-100.cb-minfo-tm-nm");
        for (Element squadElement : squadElements) {
            Elements playerElements = squadElement.select("a.margin0.text-black.text-hvr-underline");
            if(playerElements.size() == 0) {
                String teamName = squadElement.text();
                if(teamName.contains("Squad")) {
                    teamName = teamName.split(Pattern.quote("Squad"))[0].strip();
                    for (Team team: teams) {
                        if (team.getName().equals(teamName)) {
                            currentTeam = team;
                            break;
                        }
                    }
                }
            } else {
                for (Element playerElement: playerElements) {
                    assert currentTeam != null; // we should not hit this assert
                    currentTeam.addPlayer(new Player(playerElement));
                }
            }
        }
        return teams;
    }

    private void extractMatchScores(Document iScorecardDoc, ArrayList<Team> playingTeams) {
        mInningsScores = new ArrayList<>();
        Elements inningsElements = iScorecardDoc.select("div[id]");
        for (int inningNum=0; inningNum < inningsElements.size(); inningNum++) {
            InningsScore inningsScore = extractInningsScoreHeader(inningsElements.get(inningNum), playingTeams, inningNum);
            inningsScore.setPlayerBattingScores(extractInningsBattingScores(inningsElements.get(inningNum), inningsScore.getBattingTeam()));
            inningsScore.setPlayerBowlingScores(extractInningsBowlingScores(inningsElements.get(inningNum)));
            mInningsScores.add(inningsScore);
        }
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
                .strip();
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

    private ArrayList<PlayerBattingScore> extractInningsBattingScores(Element inningsElement, Team battingTeam) {
        ArrayList<PlayerBattingScore> playerBattingScores = new ArrayList<>();
        Element inningsBattingScoresElement = inningsElement.select("div.cb-col.cb-col-100.cb-ltst-wgt-hdr").first();

        Elements battingScoreElements = inningsBattingScoresElement.select("div.cb-col.cb-col-100.cb-scrd-itms");
        for (Element battingScoreElement : battingScoreElements) {
            PlayerBattingScore playerBattingScore = PlayerBattingScore.extractPlayerBattingScore(battingScoreElement);
            if (playerBattingScore != null) {
                System.out.println(playerBattingScore.toString());
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
            PlayerBowlingScore playerBowlingScore = PlayerBowlingScore.extractPlayerBowlingScore(bowlingScoreElement);
            if (playerBowlingScore != null) {
                System.out.println(playerBowlingScore.toString());
                playerBowlingScores.add(playerBowlingScore);
            }
        }
        return playerBowlingScores;
    }
}
