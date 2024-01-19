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

    public Movie(){
        this.id = null;
        this.title = null;
        this.year = null;
        this.director = null;
        genres = new ArrayList<Genre>();
    }

    public void setId(String newId){id = newId;}
    public void setTitle(String newTitle){title = newTitle;}
    public void setYear(Integer newYear){year = newYear;}
    public void setDirector(String newDir){director = newDir;}

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

    public ArrayList <Genre> getGenres(){
        return genres;
    }
    public void addGenre(Genre genre){
        genres.add(genre);
    }

    public String returnString(){
        return id + " | " + title + " | " + Integer.toString(year) + " | " + director;
    }

}
