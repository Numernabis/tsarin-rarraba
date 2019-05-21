package bookstore.model;

import java.io.Serializable;

public class OrderRequest extends Request implements Serializable {
    private String title;

    public OrderRequest(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }
}
