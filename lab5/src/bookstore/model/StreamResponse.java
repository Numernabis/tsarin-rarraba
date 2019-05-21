package bookstore.model;

import java.io.Serializable;

public class StreamResponse implements Serializable {
    private String line;
    private boolean completed;

    public StreamResponse(String line, boolean completed) {
        this.line = line;
        this.completed = completed;
    }

    public String getLine() {
        return line;
    }

    public boolean isCompleted() {
        return completed;
    }
}
