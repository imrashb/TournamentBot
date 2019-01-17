package DiscordBot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

import java.sql.*;
import java.util.List;

public class Database {

    private Connection conn;
    private Statement statement;
    private final String DB_NAME = "discord_bot.db";

    public Database() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + DB_NAME);
            statement = conn.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS users(_id TEXT, summonerId TEXT, region TEXT)");

            statement.close();
            conn.close();
        } catch (SQLException e) {
            System.err.println("There was a problem : "+e.getMessage());
        }
    }

    public void connect() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + DB_NAME);
            statement = conn.createStatement();
        } catch (SQLException e) {
            System.err.println("There was a problem : "+e.getMessage());
        }
    }

    public void close() {
        try {
            if(statement != null)
                statement.close();
            else
                System.err.println("Can't close statement in database");

            if (conn != null)
                conn.close();
            else
                System.err.println("Can't close connection to database");

        } catch(SQLException e) {
            System.err.println("Something wrong happened : "+e.getMessage());
        }
    }

    public Statement getStatement() {
        return statement;
    }

    public void update(JDA jda) {
        connect();
        try {
            List<User> userList = jda.getUsers();
                for(int i = 0; i<userList.size(); i++) {
                    ResultSet result = statement.executeQuery("SELECT * FROM users WHERE _id='"+userList.get(i).getId()+"'");
                    if(!result.next())
                        statement.execute("INSERT INTO users(_id) VALUES('"+userList.get(i).getId()+"')");
                }

        }catch(Exception e) {
            System.out.println("Something went wrong : "+e.getMessage());
        }

        close();
    }

}
