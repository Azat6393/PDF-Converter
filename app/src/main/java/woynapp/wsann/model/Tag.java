package woynapp.wsann.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Tag {
    public String name;
    public String number;

    public Tag(){

    }
    public Tag(String name, String number){
        this.name = name;
        this.number = number;
    }
}
