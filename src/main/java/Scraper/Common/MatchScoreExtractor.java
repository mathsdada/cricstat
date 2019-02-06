package Scraper.Common;

import Model.*;
import Scraper.Model.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class MatchScoreExtractor {
    public ArrayList<InningsScore> extractMatchScores(Document iScorecardDoc, ArrayList<Team> playingTeams) {
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
            PlayerBattingScore playerBattingScore = PlayerBattingScore.extractPlayerBattingScore(battingScoreElement);
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
            PlayerBowlingScore playerBowlingScore = PlayerBowlingScore.extractPlayerBowlingScore(bowlingScoreElement);
            if (playerBowlingScore != null) {
                playerBowlingScores.add(playerBowlingScore);
            }
        }
        return playerBowlingScores;
    }
}
