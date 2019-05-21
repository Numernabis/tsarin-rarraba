package bookstore.model;

import java.io.Serializable;

public class OrderResponse implements Serializable {
    private String title;
    private boolean completed;

    public OrderResponse(String title, boolean completed) {
        this.title = title;
        this.completed = completed;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted() {
        return completed;
    }
}
