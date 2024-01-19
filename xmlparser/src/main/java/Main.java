package main.java;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;

public class Main {
    public void loadData(){
        DataSource dataSource;

        try (InputStream input = SAXMovieGenreParser.class.getClassLoader().getResourceAsStream("config.properties")){
            if (input == null){
                System.out.println("No config.properties");
                return;
            }

            Properties prop = new Properties();
            prop.load(input);

            String url = prop.getProperty("db.url");
            String username = prop.getProperty("db.username");
            String password = prop.getProperty("db.password");

            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                Statement statement = conn.createStatement();

                // import moviess
                String query = "LOAD DATA LOCAL INFILE 'src/main/java/csvFiles/movies.csv' INTO TABLE movies " +
                        "FIELDS TERMINATED BY ' | ' ENCLOSED BY '\"' " +
                        "LINES TERMINATED BY '\n';";

                statement.execute(query);

                // insert genres
                query = "LOAD DATA LOCAL INFILE 'src/main/java/csvFiles/genres.csv' INTO TABLE genres " +
                        "FIELDS TERMINATED BY ' | ' ENCLOSED BY '\"' " +
                        "LINES TERMINATED BY '\n';";
                statement.execute(query);
                query = "LOAD DATA LOCAL INFILE 'src/main/java/csvFiles/genresInMovies.csv' INTO TABLE genres_in_movies " +
                        "FIELDS TERMINATED BY ' | ' ENCLOSED BY '\"' " +
                        "LINES TERMINATED BY '\n';";
                statement.execute(query);

                //import stars
                query = "LOAD DATA LOCAL INFILE 'src/main/java/csvFiles/stars.csv' INTO TABLE stars " +
                        "FIELDS TERMINATED BY ' | ' ENCLOSED BY '\"' " +
                        "LINES TERMINATED BY '\n' " +
                        "(id, name, @birthYear)  " +
                        "SET birthYear = NULLIF(@birthYear, 'null');" ;
                statement.execute(query);
                query = "LOAD DATA LOCAL INFILE 'src/main/java/csvFiles/starsInMovies.csv' INTO TABLE stars_in_movies " +
                        "FIELDS TERMINATED BY ' | ' ENCLOSED BY '\"' " +
                        "LINES TERMINATED BY '\n';";
                statement.execute(query);


                statement.close();
                System.out.println("Load Data Complete!");
            } catch (Exception ee){
                ee.printStackTrace();}
        } catch (IOException e){
            e.printStackTrace();}
    }

    public static void main(String[] args){
        SAXMovieGenreParser.main(args);

        SAXStarsParser ssp = new SAXStarsParser();
        ssp.runParser();
        HashMap<String, String> starIds = ssp.returnStarIds();

        SAXStarsMoviesParser ssmp = new SAXStarsMoviesParser(starIds);
        ssmp.runParser();

        Main m = new Main();
        m.loadData();
    }
}