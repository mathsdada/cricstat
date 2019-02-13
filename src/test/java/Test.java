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
        Season season = Scraper.Season.build("2018");
        for (Model.Series series: season.getSeriesList()) {
            Series.insert(connection, Integer.parseInt(series.getId()), series.getTitle(), StringUtils.getGender(series.getTitle()));
            for (Model.Match match : series.getMatches()) {
                /* do not fill other tables if Match table insertion fails */
                if (!Match.insert(connection, Integer.parseInt(match.getId()), Integer.parseInt(series.getId()), match.getTitle(),
                        match.getFormat(), new String[]{match.getTeams().get(0).getName(), match.getTeams().get(1).getName()},
                        match.getOutcome(), match.getWinningTeam(), match.getVenue(), match.getDate(), match.getStatus())) {
                    continue;
                }
                for (Model.InningsScore inningsScore : match.getInningsScores()) {
                    InningsScore.insert(connection, Integer.parseInt(match.getId()), inningsScore.getInningsNum(),
                            inningsScore.getBattingTeam().getName(), inningsScore.getBowlingTeam().getName(),
                            inningsScore.getScoreHeader().getRuns(), inningsScore.getScoreHeader().getWickets(),
                            inningsScore.getScoreHeader().getOvers());
                    for (Model.PlayerBattingScore playerBattingScore: inningsScore.getPlayerBattingScores()) {
                        Model.Player batsman = playerBattingScore.getBatsman();
                        Model.Player bowler = playerBattingScore.getBowler();
                        String bowlerId = null;
                        Player.insert(connection, Integer.parseInt(batsman.getId()), batsman.getName(), batsman.getRole(),
                                batsman.getBattingStyle(), batsman.getBowlingStyle());
                        if (bowler != null) {
                            Player.insert(connection, Integer.parseInt(bowler.getId()), bowler.getName(), bowler.getRole(),
                                    bowler.getBattingStyle(), bowler.getBowlingStyle());
                            bowlerId = bowler.getId();
                        }
                        PlayerBattingScore.insert(connection, Integer.parseInt(batsman.getId()), Integer.parseInt(match.getId()),
                                inningsScore.getInningsNum(), inningsScore.getBattingTeam().getName(), inningsScore.getBowlingTeam().getName(),
                                Integer.parseInt(playerBattingScore.getRuns()), Integer.parseInt(playerBattingScore.getBalls()),
                                Integer.parseInt(playerBattingScore.getFours()), Integer.parseInt(playerBattingScore.getSixes()),
                                playerBattingScore.getStrikeRate(), playerBattingScore.getStatus(),
                                bowlerId);
                    }
                    for (Model.PlayerBowlingScore playerBowlingScore: inningsScore.getPlayerBowlingScores()) {
                        Model.Player bowler = playerBowlingScore.getPlayer();
                        Player.insert(connection, Integer.parseInt(bowler.getId()), bowler.getName(), bowler.getRole(),
                                bowler.getBattingStyle(), bowler.getBowlingStyle());
                        PlayerBowlingScore.insert(connection, Integer.parseInt(bowler.getId()), Integer.parseInt(match.getId()),
                                inningsScore.getInningsNum(), inningsScore.getBowlingTeam().getName(), inningsScore.getBattingTeam().getName(),
                                playerBowlingScore.getOvers(), Integer.parseInt(playerBowlingScore.getMaidens()),
                                Integer.parseInt(playerBowlingScore.getRuns()), Integer.parseInt(playerBowlingScore.getWickets()),
                                Integer.parseInt(playerBowlingScore.getNoBalls()), Integer.parseInt(playerBowlingScore.getWides()),
                                playerBowlingScore.getEconomy());
                    }

                }
                for (Model.HeadToHead headToHead : match.getHeadToHeadList()) {
                    Pair<Model.Player, Team> batsman = headToHead.getBatsman();
                    Pair<Model.Player, Team> bowler = headToHead.getBowler();
                    Player.insert(connection, Integer.parseInt(batsman.getFirst().getId()), batsman.getFirst().getName(),
                            batsman.getFirst().getRole(), batsman.getFirst().getBattingStyle(),
                            batsman.getFirst().getBowlingStyle());
                    Player.insert(connection, Integer.parseInt(bowler.getFirst().getId()), bowler.getFirst().getName(),
                            bowler.getFirst().getRole(), bowler.getFirst().getBattingStyle(),
                            bowler.getFirst().getBowlingStyle());
                    HeadToHead.insert(connection, Integer.parseInt(batsman.getFirst().getId()), batsman.getSecond().getName(),
                            Integer.parseInt(bowler.getFirst().getId()), bowler.getSecond().getName(),
                            Integer.parseInt(match.getId()), headToHead.getInningsNum(),
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