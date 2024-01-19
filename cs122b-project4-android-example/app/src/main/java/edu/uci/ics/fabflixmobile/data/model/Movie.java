package edu.uci.ics.fabflixmobile.data.model;

import java.util.ArrayList;

/**
 * Movie class that captures movie information for movies retrieved from MovieListActivity
 */
public class Movie {
    private String id;
    private String title;
    private Integer year;
    private String director;
    private ArrayList<String> genres;
    private ArrayList<String> stars; //just names no one gives afa bout nothin else
    private Float rating;

    public Movie(String id, String title, Integer year, String director, Float rating){
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
        genres = new ArrayList<String>();
        stars = new ArrayList<String>();
        this.rating = rating;
    }

    public String getId(){
        return this.id;
    }

    public String getTitle(){
        return this.title;
    }

    public Integer getYear(){
        return this.year;
    }

    public String getDirector(){
        return this.director;
    }


    public ArrayList <String> getGenres(){
        return genres;//returns as list of 3
    }

    public ArrayList <String> getStars(){
        return stars; // same as genres
    }

    public Float getRating(){
        return this.rating;
    }

    public void addGenre(String genre){
        genres.add(genre);
    }

    public void addStar(String star){
        stars.add(star);
    }
}