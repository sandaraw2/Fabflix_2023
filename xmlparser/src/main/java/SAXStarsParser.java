package main.java;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.Properties;

import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import types.Star;

// actor -> stagename(a in casts) -> filmc -> m
// want: actor id, actor name, actor dob (optional)
// movies table: title, id, year, director

public class SAXStarsParser extends DefaultHandler {
    private Stack<String> elementStack = new Stack<>();

    private HashMap<String, String> starIds = mapStarIds(); //Star name, id

    private Set<Star> myStars;
    private String tempVal;
    private Star tempStar;
    private Boolean duplicate = false;
    private Integer index = 9423080;

    private Integer numFormatErrors = 0;

    public SAXStarsParser() {
        myStars = new HashSet<Star>();
    }

    public void runParser() {
        parseDocument();
        writeToCSV();
    }

    private void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();
            sp.parse("src/main/java/xml/actors63.xml", this);
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    private void writeToCSV() { // use to see if values ok
        System.out.println("Insert " + myStars.size() + " Stars");

        File csvFileStars = new File("src/main/java/csvFiles/stars.csv");

        try{
            PrintWriter outStars = new PrintWriter(csvFileStars);
            for (Star nextStar: myStars) {
                outStars.println(nextStar.getId() + " | " + nextStar.getName() + " | " + nextStar.getBirthYear());
            }
            outStars.close();
        } catch (FileNotFoundException e){
            System.out.println(e.toString());
        }

        System.out.println( numFormatErrors.toString() + " NumberFormatException (birthYear) Errors");
    }

    private HashMap<String, String> mapStarIds(){ //takes info from database to make inital starIds list
        HashMap<String, String> tempStarIds = new HashMap<>();

        try (InputStream input = SAXMovieGenreParser.class.getClassLoader().getResourceAsStream("config.properties")){
            if (input == null){return tempStarIds;}

            Properties prop = new Properties();
            prop.load(input);

            String url = prop.getProperty("db.url");
            String username = prop.getProperty("db.username");
            String password = prop.getProperty("db.password");

            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                Statement statement = conn.createStatement();
                String query = "SELECT id,name from stars";
                ResultSet resultSet = statement.executeQuery(query);

                while (resultSet.next()){
                    String currName = resultSet.getString("name");
                    String currId = resultSet.getString("id");
                    tempStarIds.put(currName,currId);
                }

                statement.close();
                resultSet.close();
            } catch (Exception ee){
                ee.printStackTrace();
            }
        } catch (IOException eee){
            eee.printStackTrace();
        }

        return tempStarIds;

    }

    public HashMap<String, String> returnStarIds(){
        return starIds;
    }

    // starts at tag
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        elementStack.push(qName);
        String currentElement = String.join("/", elementStack);
        if (currentElement.equals("actors/actor")){
            tempStar = new Star();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    // runs when the /tag appears
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String currentElement = String.join("/", elementStack);

        if (currentElement.equals("actors/actor")){
            if (duplicate != true){ myStars.add(tempStar);}
            else{ duplicate = false;}
        } else if (currentElement.equals("actors/actor/stagename")) {
            if (starIds.containsKey(tempVal.strip())){
                duplicate = true;}
            else{
                String newId = "nm" + index.toString();
                index += 2;

                starIds.put(tempVal.strip(), newId);
                tempStar.setId(newId);
                tempStar.setName(tempVal.strip());}
        } else if (currentElement.equals("actors/actor/dob")) {
            try{
                if (!tempVal.isBlank()){
                    if (!tempVal.strip().equals("n.a.")){
                        tempStar.setBirthYear(Integer.parseInt(tempVal.strip()));}}
            } catch (NumberFormatException e){
                numFormatErrors++;
            }
        }
        elementStack.pop();
    }

    public static void main(String[] args) {
        SAXStarsParser sgp = new SAXStarsParser();
        sgp.runParser();
    }

}
