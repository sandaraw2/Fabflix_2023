package types;

public class Genre {
    private Integer id;
    private String name;

    public Genre(Integer id, String name){
        this.id = id;
        this.name = name;
    }


    public Integer getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

}
