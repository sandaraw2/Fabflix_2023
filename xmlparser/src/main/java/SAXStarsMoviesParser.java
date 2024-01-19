package main.java;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import types.Star;
import types.StarsInMovies;

// casts -> dirfilms -> filmc -> m
// movies table: title, id, year, director

public class SAXStarsMoviesParser extends DefaultHandler {
    private Stack<String> elementStack = new Stack<>(); // track next element to traverse hopefully

    private HashMap<String,String> starIds; //mapped: name and Id
    private Set<StarsInMovies> mySIM;
    private String tempVal;
    private StarsInMovies tempSIM;
    private boolean validStar = true;

    private Integer existingStars = 0;

    public SAXStarsMoviesParser(HashMap<String,String> sIds) {
        starIds = sIds;
        mySIM = new HashSet<StarsInMovies>();
    }

    public void runParser() {
        parseDocument();
        writeToCSV();
    }

    private void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();
            sp.parse("src/main/java/xml/casts124.xml", this);
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    private void writeToCSV() {
        System.out.println("Insert " + mySIM.size() + " Stars In Movies");

        File csvFileStarsInMovies = new File("src/main/java/csvFiles/starsInMovies.csv");

        try{
            PrintWriter outStarsInMovies = new PrintWriter(csvFileStarsInMovies);
            for (StarsInMovies nextSIM: mySIM) {
                outStarsInMovies.println(nextSIM.getStarId() + " | " + nextSIM.getMovieId());
            }
            outStarsInMovies.close();
        } catch (FileNotFoundException e){
            System.out.println(e.toString());
        }

        System.out.println("Ignored " + existingStars.toString() + " Stars not in Actors");
    }

    // starts at tag
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        elementStack.push(qName);
        String currentElement = String.join("/", elementStack);
        if (currentElement.equals("casts/dirfilms/filmc/m")){
            tempSIM = new StarsInMovies();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    // runs when the /tag appears
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String currentElement = String.join("/", elementStack);

        //to do: add error catching
        if (currentElement.equals("casts/dirfilms/filmc/m")){
            if (validStar){mySIM.add(tempSIM);}
            else{validStar = true;}
        } else if (currentElement.equals("casts/dirfilms/filmc/m/f")) {
            tempSIM.setMovieId(tempVal.strip());
        } else if (currentElement.equals("casts/dirfilms/filmc/m/a")) {
            if (starIds.containsKey(tempVal.strip())){
                String starId = starIds.get(tempVal.strip());
                tempSIM.setStarId(starId);}
            else{
                existingStars++;
                validStar = false;
            }
        }
        elementStack.pop();
    }

    public static void main(String[] args) {
        //SAXStarsMoviesParser sgp = new SAXStarsMoviesParser();
        //sgp.runParser();
        System.out.println("Hi");
    }

}
