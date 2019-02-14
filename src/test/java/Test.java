import Common.Pair;
import Common.StringUtils;
import Database.*;
import Model.Season;
import Model.Team;

import java.sql.Connection;
import java.sql.SQLException;

public class Test {
    public static void main(String args[]) throws SQLException {
        clearTables();
        Connection connection = DatabaseEngine.getInstance().getConnection();
        Season season = Scraper.Season.build("2019");
        for (Model.Series series: season.getSeriesList()) {
            Series.insert(connection, series.getId(), series.getTitle(), StringUtils.getGender(series.getTitle()));
            for (Model.Match match : series.getMatches()) {
                /* do not fill other tables if Match table insertion fails */
                if (!Match.insert(connection, match.getId(), series.getId(), match.getTitle(),
                        match.getFormat(), new String[]{match.getTeams().get(0).getName(), match.getTeams().get(1).getName()},
                        match.getOutcome(), match.getWinningTeam(), match.getVenue(), match.getDate(), match.getStatus())) {
                    continue;
                }
                for (Model.InningsScore inningsScore : match.getInningsScores()) {
                    InningsScore.insert(connection, match.getId(), inningsScore.getInningsNum(),
                            inningsScore.getBattingTeam().getName(), inningsScore.getBowlingTeam().getName(),
                            inningsScore.getScoreHeader().getRuns(), inningsScore.getScoreHeader().getWickets(),
                            inningsScore.getScoreHeader().getOvers());
                    for (Model.PlayerBattingScore playerBattingScore: inningsScore.getPlayerBattingScores()) {
                        Model.Player batsman = playerBattingScore.getBatsman();
                        Model.Player bowler = playerBattingScore.getBowler();
                        int bowlerId = 0;
                        Player.insert(connection, batsman.getId(), batsman.getName(), batsman.getRole(),
                                batsman.getBattingStyle(), batsman.getBowlingStyle());
                        if (bowler != null) {
                            Player.insert(connection, bowler.getId(), bowler.getName(), bowler.getRole(),
                                    bowler.getBattingStyle(), bowler.getBowlingStyle());
                            bowlerId = bowler.getId();
                        }
                        PlayerBattingScore.insert(connection, batsman.getId(), match.getId(),
                                inningsScore.getInningsNum(), inningsScore.getBattingTeam().getName(), inningsScore.getBowlingTeam().getName(),
                                playerBattingScore.getRuns(), playerBattingScore.getBalls(),
                                playerBattingScore.getFours(), playerBattingScore.getSixes(),
                                playerBattingScore.getStrikeRate(), playerBattingScore.getStatus(),
                                bowlerId);
                    }
                    for (Model.PlayerBowlingScore playerBowlingScore: inningsScore.getPlayerBowlingScores()) {
                        Model.Player bowler = playerBowlingScore.getPlayer();
                        Player.insert(connection, bowler.getId(), bowler.getName(), bowler.getRole(),
                                bowler.getBattingStyle(), bowler.getBowlingStyle());
                        PlayerBowlingScore.insert(connection, bowler.getId(), match.getId(),
                                inningsScore.getInningsNum(), inningsScore.getBowlingTeam().getName(), inningsScore.getBattingTeam().getName(),
                                playerBowlingScore.getOvers(), playerBowlingScore.getMaidens(),
                                playerBowlingScore.getRuns(), playerBowlingScore.getWickets(),
                                playerBowlingScore.getNoBalls(), playerBowlingScore.getWides(),
                                playerBowlingScore.getEconomy());
                    }

                }
                for (Model.HeadToHead headToHead : match.getHeadToHeadList()) {
                    Pair<Model.Player, Team> batsman = headToHead.getBatsman();
                    Pair<Model.Player, Team> bowler = headToHead.getBowler();
                    Player.insert(connection, batsman.getFirst().getId(), batsman.getFirst().getName(),
                            batsman.getFirst().getRole(), batsman.getFirst().getBattingStyle(),
                            batsman.getFirst().getBowlingStyle());
                    Player.insert(connection, bowler.getFirst().getId(), bowler.getFirst().getName(),
                            bowler.getFirst().getRole(), bowler.getFirst().getBattingStyle(),
                            bowler.getFirst().getBowlingStyle());
                    HeadToHead.insert(connection, batsman.getFirst().getId(), batsman.getSecond().getName(),
                            bowler.getFirst().getId(), bowler.getSecond().getName(),
                            match.getId(), headToHead.getInningsNum(),
                            headToHead.getHeadToHeadData().getBalls(), headToHead.getHeadToHeadData().getRuns(),
                            headToHead.getHeadToHeadData().getWicket(), headToHead.getHeadToHeadData().getDotBalls(),
                            headToHead.getHeadToHeadData().getFours(), headToHead.getHeadToHeadData().getSixes(),
                            headToHead.getHeadToHeadData().getNoBall());
                }
            }
        }
        DatabaseEngine.getInstance().releaseConnection();
    }

    private static void clearTables() {
        try {
            Connection connection = DatabaseEngine.getInstance().getConnection();
            HeadToHead.clear(connection);
            PlayerBattingScore.clear(connection);
            PlayerBowlingScore.clear(connection);
            InningsScore.clear(connection);
            Player.clear(connection);
            Match.clear(connection);
            Series.clear(connection);
            DatabaseEngine.getInstance().releaseConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}