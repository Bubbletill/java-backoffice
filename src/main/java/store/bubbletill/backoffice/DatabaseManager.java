package store.bubbletill.backoffice;

import store.bubbletill.commons.LocalData;
import store.bubbletill.commons.OperatorData;
import store.bubbletill.commons.OperatorGroup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseManager {

    private final String url = "jdbc:mysql://localhost:3306/bubbletill";
    private final String username;
    private final String password;
    private final LocalData localData;

    public DatabaseManager(String username, String password, LocalData localData) {
        this.username = username;
        this.password = password;
        this.localData = localData;
    }

    public ArrayList<OperatorData> getOperators() {
        ArrayList<OperatorData> toReturn = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM operators");
            while (rs.next()) {
                toReturn.add(new OperatorData(rs.getString("id"), rs.getString("name"), rs.getString("password"), rs.getString("groups")));
            }

            return toReturn;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public HashMap<Integer, OperatorGroup> getOperatorGroups() {
        HashMap<Integer, OperatorGroup> toReturn = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM operator_groups");
            while (rs.next()) {
                OperatorGroup og = new OperatorGroup(rs.getInt("id"), rs.getString("name"), rs.getString("allowed_pos_actions"), rs.getString("allowed_bo_actions"));
                toReturn.put(og.getId(), og);
            }

            return toReturn;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
