package Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database {
    private static Database ourInstance = new Database();
    public static Database getInstance() {
        return ourInstance;
    }

    private Connection mConnection;
    private int mUsageCount = 0;
    private final Object mMutex = new Object();
    private Database() {
        try {
            mConnection = DriverManager.getConnection("jdbc:postgresql://localhost/cricstat", "mathsdada", "1@gangadhar");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PreparedStatement getPreparedStatement(String sqlStmt) throws SQLException {
        return mConnection.prepareStatement(sqlStmt);
    }
    public Connection getConnection() throws SQLException {
        synchronized (mMutex) {
            if (mUsageCount == 0) {
                mConnection = DriverManager.getConnection("jdbc:postgresql://localhost/cricstat", "mathsdada", "1@gangadhar");
            }
            mUsageCount++;
        }
        return mConnection;
    }

    public void releaseConnection() throws SQLException {
        synchronized (mMutex) {
            mUsageCount--;
            if (mUsageCount == 0) {
                mConnection.close();
                mConnection = null;
            }
        }
    }

}
