package com.example.emf_monitor;

import android.database.sqlite.SQLiteCursor;
import android.util.Log;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;

import static org.junit.Assert.*;

import com.example.emf_monitor.SQL_database.EMFMonitorDbHelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class UnitTests {

    private static Connection con = null;
    private static Statement stmnt = null;
    private static ResultSet rs = null;

    private static String testUser = "test";
    private static String testData = "100.0, 220.1, 419.9";
    private static Double[] testDataArr = {100.0, 220.1, 419.9};
    private static String testStart = "2020/10/28 15:01:01";
    private static String testStop = "2020/10/28 15:02:01";

    @BeforeClass
    public static void createDB() {


        try {
            Class.forName("org.sqlite.JDBC");

            con = DriverManager.getConnection("jdbc:sqlite::memory:");
            stmnt = con.createStatement();

            stmnt.execute(EMFMonitorDbHelper.SQL_CREATE_TABLE_USERS);
            stmnt.execute(EMFMonitorDbHelper.SQL_CREATE_TABLE_DATA);
            stmnt.execute(EMFMonitorDbHelper.SQL_POPULATE_TABLE_USERS);
        }
        catch (SQLException e) {e.printStackTrace();}
        catch (Exception e) {e.printStackTrace();}
    }

    @Test
    public void insertData() {

        String sql = "INSERT INTO " + EMFMonitorDbHelper.DataContract.DataEntry.TABLE_NAME +
                     "(" + EMFMonitorDbHelper.DataContract.DataEntry.COLUMN_NAME_UID + "," +
                     EMFMonitorDbHelper.DataContract.DataEntry.COLUMN_NAME_DATA + "," +
                     EMFMonitorDbHelper.DataContract.DataEntry.COLUMN_NAME_START + "," +
                     EMFMonitorDbHelper.DataContract.DataEntry.COLUMN_NAME_STOP  + ") " +
                     "VALUES ((SELECT UID FROM users WHERE username = '" + testUser + "'), " +
                     "'" + testData + "', " + "'" + testStart + "', " + "'" + testStop + "');";

        try {
            stmnt = con.createStatement();

            stmnt.execute(sql);
        }
        catch (SQLException e) {
            // Fail the test if there is an SQL Exception
            e.printStackTrace();
            System.out.println(sql);
            assertTrue(false);

        }
        catch (Exception e) {
            // Fail the test if there is any other exception
            e.printStackTrace();
            assertTrue(false);
        }

        sql = "SELECT data FROM data";

        try {
            stmnt = con.createStatement();
            rs = stmnt.executeQuery(sql);
            String data = null;

            while(rs.next()) {
                data = rs.getString("data");
            }

            String[] stringArr = data.split(",");
            Double[] dataArr = new Double[stringArr.length];

            for (int i = 0; i < stringArr.length; i++) {
                dataArr[i] = Double.parseDouble(stringArr[i]);
            }

            assertArrayEquals(testDataArr, dataArr);

        }
        catch (SQLException e) {
            // Fail the test if there is an SQL Exception
            e.printStackTrace();
            System.out.println(sql);
            assertTrue(false);

        }
        catch (Exception e) {
            // Fail the test if there is any other exception
            e.printStackTrace();
            assertTrue(false);
        }

    }

    @AfterClass
    public static void endTest() {
        try {
            con.close();
        } catch (Exception e) {e.printStackTrace();}
    }

}