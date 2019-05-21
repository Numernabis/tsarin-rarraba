package bookstore.server;

import akka.actor.AbstractActor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import akka.event.Logging;
import akka.event.LoggingAdapter;
import bookstore.model.*;

public class SearchHelper extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchRequestDB.class, request -> {
                    int price = searchPrice(request.getTitle(), request.getDatabase());
                    SearchResponse response = new SearchResponse(request.getTitle(), price);
                    getSender().tell(response, getSelf());
                })
                .build();
    }

    private Integer searchPrice(String title, String database) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(database));
        int price = 0;
        while (true) {
            String line = br.readLine();
            if (line == null) break;
            if (line.contains(title)) {
                log.info("Book found in :" + database);
                String[] elems = line.split("#");
                price = Integer.parseInt(elems[1]);
                break;
            }
        }
        br.close();
        return price;
    }


}
