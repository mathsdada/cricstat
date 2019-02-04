package Model;

import Configuration.Config;
import Extractor.MatchCommentaryExtractor;
import Extractor.MatchInfoExtractor;
import Extractor.MatchScoreExtractor;
import Utility.ScraperUtils;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.HashMap;

public class Match {
    private String mUrl;
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
    private ArrayList<HeadToHead> mHeadToHeadList;

    public Match(String url, String id, String title, String format, String venue, String status, String outcome, String winningTeam, HashMap<String, Player> playerCacheMap) {
        mUrl = url;
        mId = id;
        mTitle = title;
        mFormat = format;
        mVenue = venue;
        mStatus = status;
        mOutcome = outcome;
        mWinningTeam = winningTeam;
        scrape(playerCacheMap);
    }

    private void scrape(HashMap<String, Player> playerCacheMap) {
        String scoreCardUrl = Config.HOMEPAGE + "/api/html/cricket-scorecard/" + this.getId();
        Document iScorecardDoc = ScraperUtils.getDocument(scoreCardUrl);
        Document commentaryDoc = ScraperUtils.getDocument(this.getUrl());

        MatchInfoExtractor matchInfoExtractor = new MatchInfoExtractor(iScorecardDoc);
        mDate = matchInfoExtractor.extractMatchDate();
        mTeams = matchInfoExtractor.extractPlayingTeams(iScorecardDoc, this.getTitle(), playerCacheMap);
        mInningsScores = new MatchScoreExtractor().extractMatchScores(iScorecardDoc, this.getTeams());
        mHeadToHeadList = new MatchCommentaryExtractor(commentaryDoc, this.getTeams()).getHeadToHead();
    }

    private String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    private String getId() {
        return mId;
    }

    private String getTitle() {
        return mTitle;
    }

    public String getFormat() {
        return mFormat;
    }

    public String getVenue() {
        return mVenue;
    }

    public String getDate() {
        return mDate;
    }

    public String getStatus() {
        return mStatus;
    }

    public String getOutcome() {
        return mOutcome;
    }

    public String getWinningTeam() {
        return mWinningTeam;
    }

    private ArrayList<Team> getTeams() {
        return mTeams;
    }

    public ArrayList<InningsScore> getInningsScores() {
        return mInningsScores;
    }

    private void setDate(String date) {
        mDate = date;
    }

    private void setTeams(ArrayList<Team> teams) {
        mTeams = teams;
    }

    private void setInningsScores(ArrayList<InningsScore> inningsScores) {
        mInningsScores = inningsScores;
    }

    public ArrayList<HeadToHead> getHeadToHeadList() {
        return mHeadToHeadList;
    }

    public void setHeadToHeadList(ArrayList<HeadToHead> headToHeadList) {
        mHeadToHeadList = headToHeadList;
    }
}

