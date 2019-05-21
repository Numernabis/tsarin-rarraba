package bookstore.model;

import java.io.Serializable;

public class SearchRequest extends Request implements Serializable {
    private String title;

    public SearchRequest(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }
}