package bookstore.model;

import java.io.Serializable;

public class SearchRequestDB implements Serializable{
    private String title;
    private String database;

    public SearchRequestDB(String title, String database) {
        this.title = title;
        this.database = database;
    }

    public String getTitle(){
        return this.title;
    }

    public String getDatabase() {
        return database;
    }
}
