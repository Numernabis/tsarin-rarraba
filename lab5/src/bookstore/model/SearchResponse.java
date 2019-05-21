package bookstore.model;

import java.io.Serializable;

public class SearchResponse implements Serializable {
    private String title;
    private Integer price;

    public SearchResponse(String title, Integer price) {
        this.price = price;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public Integer getPrice() {
        return price;
    }
}
