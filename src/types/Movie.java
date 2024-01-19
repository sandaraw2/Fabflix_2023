package types;
import types.Genre;
import types.Star;
import java.util.ArrayList;

public class Movie {
    private String id;
    private String title;
    private Integer year;
    private String director;
    private ArrayList <Genre> genres;
    private ArrayList <Star> stars;
    private Float rating;


    public Movie(String id, String title, Integer year, String director, Float rating){
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
        this.rating = rating;
        genres = new ArrayList<Genre>();
        stars = new ArrayList<Star>();
    }

    public Movie(){
        this.id = null;
        this.title = null;
        this.year = null;
        this.director = null;
        this.rating = null;
        genres = new ArrayList<Genre>();
        stars = new ArrayList<Star>();
    }

    public void setId(String newId){id = newId;}
    public void setTitle(String newTitle){title = newTitle;}
    public void setYear(Integer newYear){year = newYear;}
    public void setDirector(String newDir){director = newDir;}
    public void addGenre(Genre genre){
        genres.add(genre);
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

    public Float getRating(){
        return this.rating;
    }

    public ArrayList <Genre> getGenres(){
        return genres;
    }

    public ArrayList <Star> getStars(){
        return stars;
    }

    public void addStar(Star star){
        stars.add(star);
    }

    public String returnString(){
        return id + " | " + title + " | " + Integer.toString(year) + " | " + director;
    }

}
