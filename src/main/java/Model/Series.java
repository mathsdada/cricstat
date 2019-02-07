package Model;

import java.sql.*;
import java.util.ArrayList;

public class Series {
    private String mId;
    private String mTitle;
    private ArrayList<Match> mMatches;


    public Series(String id, String title, ArrayList<Match> matches) {
        mId = id;
        mTitle = title;
        mMatches = matches;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public ArrayList<Match> getMatches() {
        return mMatches;
    }

    public void setMatches(ArrayList<Match> matches) {
        mMatches = matches;
    }

    /* DB Operations */
    public boolean insertSeries(Connection connection) throws SQLException {
        if (!findSeriesById(connection, Integer.parseInt(mId))) {
            String SQL = "INSERT INTO series VALUES(?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(SQL);
            preparedStatement.setInt(1, Integer.parseInt(mId));
            preparedStatement.setString(2, mTitle);
            preparedStatement.setString(3, "Men");
//            preparedStatement.setString(4, mYear);

            return (0 != preparedStatement.executeUpdate());
        } else {
            System.out.println("Series Available in DB : " + mId);
            return false;
        }
    }

    private boolean findSeriesById(Connection connection, int id) throws SQLException {
        String SQL = "SELECT count(*) FROM series WHERE series.id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(SQL);
        preparedStatement.setInt(1, id);
        ResultSet resultSet =preparedStatement.executeQuery();
        resultSet.next();
        return (0!=resultSet.getInt(1));
    }
}
