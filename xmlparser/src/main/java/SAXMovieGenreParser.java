package main.java;

import java.io.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.util.Properties;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Stack;
import java.util.Set;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import types.Movie;

import org.xml.sax.helpers.DefaultHandler;
import types.Genre;

// movies -> directorfilms -> director -> films -> film
// movies table: title, id,

public class SAXMovieGenreParser extends DefaultHandler {
    private Stack<String> elementStack = new Stack<>(); //next path

    private HashMap<String, Integer> genreIds = mapGenreIds();
    private HashMap<String, Integer> newGenreIds = new HashMap<>();
    private Set<Movie> myMovies;
    private String tempVal;
    private Movie tempMovie;
    HashMap<String, String> allGenres = new HashMap<String, String>(){{
        put("actn", "Action");
        put("advt", "Adventure");
        put("biop", "Biography");
        put("comd", "Comedy");
        put("dram", "Drama");
        put("docu", "Documentary");
        put("fant", "Fantasy");
        put("hist", "History");
        put("horr", "Horror");
        put("musc", "Musical");
        put("myst", "Mystery");
        put("romt", "Romance");
        put("scfi", "Sci-Fi");
        put("susp", "Thriller");
        put("west", "Western");
    }};

    Integer allFields = 0;
    Integer numFormatErrors = 0;
    String currDirector;

    public SAXMovieGenreParser() {
        myMovies = new HashSet<Movie>();
    }

    public void runParser() {
        parseDocument();
        writeToCSV();
    }

    private void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();
            sp.parse("src/main/java/xml/mains243.xml", this);
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    private void writeToCSV() { // turn this into csv write
        System.out.println("Insert " + myMovies.size() + " Movies");
        System.out.println("Insert " + newGenreIds.size() + " Genres");
        Integer numGenresInMovies= 0;

        File csvFileMovies = new File("src/main/java/csvFiles/movies.csv");
        File csvFileGenresOfMovies = new File("src/main/java/csvFiles/genresInMovies.csv");
        File csvFileGenres = new File("src/main/java/csvFiles/genres.csv");
        try{
            PrintWriter outMovies = new PrintWriter(csvFileMovies);
            PrintWriter outGenresOfMovies = new PrintWriter(csvFileGenresOfMovies);
            PrintWriter outGenres = new PrintWriter(csvFileGenres);

            for (Movie nextMovie: myMovies) {
                outMovies.println(nextMovie.returnString());

                for (Genre nextGenre: nextMovie.getGenres()){
                    numGenresInMovies++;
                    outGenresOfMovies.println(nextGenre.getId() + " | " + nextMovie.getId() );
                }
            }

            for (Map.Entry<String, Integer> entry : newGenreIds.entrySet()){
                String genre = entry.getKey();
                Integer id = entry.getValue();
                outGenres.println(id.toString() + " | " + genre);
            }

            outMovies.close();
            outGenresOfMovies.close();
            outGenres.close();
        } catch (FileNotFoundException e){
            System.out.println(e.toString());
        }

        System.out.println("Insert " + numGenresInMovies.toString() + " Genres In Movies");
        System.out.println( numFormatErrors.toString() + " NumberFormatException (year) Errors");
    }

    private HashMap<String, Integer> mapGenreIds(){
        HashMap<String, Integer> tempGenreIds = new HashMap<>();

        DataSource dataSource;
        try (InputStream input = SAXMovieGenreParser.class.getClassLoader().getResourceAsStream("config.properties")){
            if (input == null){return tempGenreIds;}

            Properties prop = new Properties();
            prop.load(input);

            String url = prop.getProperty("db.url");
            String username = prop.getProperty("db.username");
            String password = prop.getProperty("db.password");

            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                Statement statement = conn.createStatement();
                String query = "SELECT * from genres";
                ResultSet resultSet = statement.executeQuery(query);

                while (resultSet.next()){
                    String currGenre = resultSet.getString("name");
                    Integer currId = resultSet.getInt("id");
                    tempGenreIds.put(currGenre,currId);
                }

                statement.close();
                resultSet.close();
            } catch (Exception ee){
                ee.printStackTrace();
            }
        } catch (IOException eee){
            eee.printStackTrace();
        }

        return tempGenreIds;
    }
    private String closestGenre(String genre){
        if (allGenres.containsKey(genre.toLowerCase())){
            genre = allGenres.get(genre.toLowerCase());}
        return genre;
    }

    // starts at <tag>
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        elementStack.push(qName);
        String currentElement = String.join("/", elementStack);
        if (currentElement.equals("movies/directorfilms/films/film")){
            tempMovie = new Movie();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    // runs when the </tag> appears
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String currentElement = String.join("/", elementStack);

        if (currentElement.equals("movies/directorfilms/films/film")){
            if (allFields == 3){
                tempMovie.setDirector(currDirector);
                myMovies.add(tempMovie);
            }
            allFields = 0;
        }else if (currentElement.equals("movies/directorfilms/director/dirname")) {
            currDirector = tempVal;
        } else if (currentElement.equals("movies/directorfilms/films/film/fid")) {
            tempMovie.setId(tempVal);
            allFields +=1;
        } else if (currentElement.equals("movies/directorfilms/films/film/t")) {
            tempMovie.setTitle(tempVal);
            allFields +=1;
        } else if (currentElement.equals("movies/directorfilms/films/film/year")) {
            try{
                tempMovie.setYear(Integer.parseInt(tempVal.strip()));
                allFields +=1;
            } catch (NumberFormatException e){
                numFormatErrors++;
            }
        } else if (currentElement.equals("movies/directorfilms/films/film/cats/cat")){
            String genre  = closestGenre(tempVal.strip());
            Integer genreId = 0;
            if(genreIds.containsKey(genre)){
                genreId = genreIds.get(genre);}
            else{
                genreId = genreIds.size() + 1;
                genreIds.put(genre,genreId);
                newGenreIds.put(genre,genreId);
            }

            Genre newGenre = new Genre(genreId, genre);
            tempMovie.addGenre(newGenre);
        }

        elementStack.pop();
    }

    public static void main(String[] args) {
        SAXMovieGenreParser sgp = new SAXMovieGenreParser();
        sgp.runParser();
    }

}