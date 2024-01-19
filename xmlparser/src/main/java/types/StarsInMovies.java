package types;

public class StarsInMovies {
    String starId;
    String movieId;
    public StarsInMovies(String sName, String mId){
        starId = sName;
        movieId = mId;
    }

    public StarsInMovies(){
        starId = null;
        movieId = null;
    }

    public void setStarId(String sId){ starId = sId;}
    public void setMovieId(String mId){movieId = mId;}

    public String getStarId(){return starId;}

    public String getMovieId() {return movieId;}
}
