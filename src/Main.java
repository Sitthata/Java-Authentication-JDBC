import java.sql.*;
import java.util.Scanner;
import java.util.logging.Logger;

public class Main {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/authentication_db";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "deeprock25";

    public static void main(String[] args) {
        authentication();
        System.out.println("Welcome to my program!");
    }

    public static void authentication() {
        boolean loggedIn = false;
        while(!loggedIn) {
            Scanner sc = new Scanner(System.in);
            System.out.println("Welcome to my Program.");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.print("Enter choice: ");
            String choice = sc.next();
            switch (choice) {
                case "1" -> {
                    System.out.println("LOGIN");
                    System.out.print("Enter username: ");
                    String username = sc.next();
                    System.out.print("Enter password: ");
                    String password = sc.next();
                    if (login(username, password)) {
                        System.out.println("Login successful");
                        loggedIn = true;
                    } else {
                        System.out.println("Login failed");
                    }
                }
                case "2" -> {
                    System.out.println("REGISTER");
                    System.out.print("Enter username: ");
                    String username = sc.next();
                    System.out.print("Enter password: ");
                    String password = sc.next();
                    try {
                        register(username, password);
                        System.out.println("Registration successful");
                    } catch (SQLException e) {
                        System.out.println("Registration failed");
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void register(String username, String password) throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            // Check if username exists
            String sql = "SELECT * FROM authentication_db.users WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                throw new SQLException("Username already exists");
            }

            String insertCommand = "INSERT INTO authentication_db.users (username, password) VALUES (?, ?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertCommand);

            // Check user complexity

            if (validCredential(username) || validCredential(password)) {
                throw new SQLException("Credential does not meet complexity requirements");
            }

            if (password.contains(username)) {
                throw new SQLException("Password is too similar to username");
            }

            insertStatement.setString(1, username);
            insertStatement.setString(2, password);
            insertStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static boolean login(String username, String password) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            if (isValidInput(username) || isValidInput(password)) {
                throw new SQLException("Username cannot be empty");
            }
            String sql = "SELECT * FROM authentication_db.users WHERE username = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            // Username doesn't exist
            if(!resultSet.next()) return false;

            // Username exists, check password
            String dbPassword = resultSet.getString("password");
            if(dbPassword.equals(password)) return true;
            else throw new SQLException("Incorrect password");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isValidInput(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean validCredential(String input) {
        int minLength = 8;
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\\\d)(?=.*[@$!%*?&])[A-Za-z\\\\d@$!%*?&]{8,}$";
        return input.length() < minLength;
    }

}