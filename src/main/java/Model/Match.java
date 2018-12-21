package Model;

import Configuration.Config;
import Extractor.MatchInfoExtractor;
import Extractor.MatchScoreExtractor;
import Utility.ScraperUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
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

    static Match extractMatchData(Element matchElement, String seriesFormats) {
        MatchInfoExtractor matchInfoExtractor = new MatchInfoExtractor();

        Elements matchTitleElement = matchElement.select("a.text-hvr-underline");
        Elements matchOutcomeElement = matchElement.select("a.cb-text-complete");
        Elements matchVenueElement = matchElement.select("div.text-gray");
        // Model.Match Title & Link & ID
        String title = matchTitleElement.text();
        String url = matchTitleElement.attr("href");
        String id = url.split(Pattern.quote("/"))[2];
        url = Config.HOMEPAGE + url;
        // Model.Match Venue
        String venue = null;
        if (matchVenueElement != null) {
            venue = matchVenueElement.text();
        }
        // Model.Match Status and Outcome
        String outcome = null, status = null;
        if (matchOutcomeElement != null) {
            outcome = matchOutcomeElement.text();
            status = matchInfoExtractor.extractStatus(matchOutcomeElement);
        }
        // Model.Match Format
        String format = matchInfoExtractor.extractFormat(title, seriesFormats);
        // Model.Match Winning Model.Team
        String winningTeam = matchInfoExtractor.extractWinningTeam(status, outcome);

        if (url.contains("cricket-scores") && venue != null && status != null && format != null) {
            return new Match(id, title, format, venue, status, outcome, winningTeam);
        }
        System.out.println("Returning NULL");
        return null;
    }

    private Match(String id, String title, String format, String venue, String status, String outcome, String winningTeam) {
        mId = id;
        mTitle = title;
        mFormat = format;
        mVenue = venue;
        mStatus = status;
        mOutcome = outcome;
        mWinningTeam = winningTeam;
    }

    void scrape() {
        String scoreCardUrl = Config.HOMEPAGE + "/api/html/cricket-scorecard/" + this.getId();
        Document iScorecardDoc = ScraperUtils.getDocument(scoreCardUrl);

        MatchInfoExtractor matchInfoExtractor = new MatchInfoExtractor(iScorecardDoc);
        MatchScoreExtractor matchScoreExtractor = new MatchScoreExtractor();

        this.setDate(matchInfoExtractor.extractMatchDate());
        this.setTeams(matchInfoExtractor.extractPlayingTeams(iScorecardDoc, this.getTitle()));
        this.setInningsScores(matchScoreExtractor.extractMatchScores(iScorecardDoc, this.getTeams()));
    }

    public String getId() {
        return mId;
    }

    public String getTitle() {
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

    public ArrayList<Team> getTeams() {
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
}

