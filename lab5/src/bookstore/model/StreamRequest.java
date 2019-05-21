package bookstore.model;

import java.io.Serializable;

public class StreamRequest extends Request implements Serializable {
    private String title;

    public StreamRequest(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }
}

