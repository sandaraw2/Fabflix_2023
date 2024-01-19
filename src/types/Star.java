package types;

public class Star {
    private String id;
    private String name;
    private Integer birthYear;

    public Star(String id, String name, Integer birthYear){
        //birthYear can be null
        this.id = id;
        this.name = name;
        this.birthYear = birthYear;
    }

    public Star(){
        id = null;
        name = null;
        birthYear = null;
    }

    public void setId(String tempId){id = tempId;}
    public void setName(String tempName){name = tempName;}
    public void setBirthYear(Integer tempBY){birthYear = tempBY;}

    public String getId(){
        return this.id;
    }
    public String getName(){
        return this.name;
    }
    public Integer getBirthYear(){
        return this.birthYear;
    }

    public String returnString(){ //ignore id cause its NULL
        return name + " | " + Integer.toString(birthYear);
    }

}
