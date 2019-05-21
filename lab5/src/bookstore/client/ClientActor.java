package bookstore.client;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import bookstore.model.*;

public class ClientActor extends AbstractActor {
    // for logging
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    // server address
    private final ActorSelection server = getContext().actorSelection("akka.tcp://server_system@127.0.0.1:3552/user/server");

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, s -> {
                    if (s.startsWith("search") || s.startsWith("order") || s.startsWith("stream")) {
                        server.tell(s, getSelf());
                    } else {
                        System.out.println("Unsupported operation.");
                    }
                })
                .match(SearchResponse.class, search -> {
                    if (search.getPrice() != 0) {
                        System.out.println("Search for : " + search.getTitle() + " - price: " + search.getPrice());
                    } else {
                        System.out.println("Search for : " + search.getTitle() + " - not found.");
                    }
                })
                .match(OrderResponse.class, order -> {
                    if (order.isCompleted()) {
                        System.out.println("Order for : " + order.getTitle() + " - completed.");
                    } else {
                        System.out.println("Order for : " + order.getTitle() + " - failed / book not found.");
                    }
                })
                .match(StreamResponse.class, stream -> {
                    if (stream.isCompleted()) {
                        if (stream.getLine().startsWith("Done")) {
                            System.out.println("#stream : -- end of the book --");
                        } else {
                            System.out.println("#stream : " + stream.getLine());
                        }
                    } else {
                        System.out.println("#stream : " + stream.getLine());
                    }
                })
                .matchAny(o -> log.info("Received unknown message."))
                .build();
    }
}
