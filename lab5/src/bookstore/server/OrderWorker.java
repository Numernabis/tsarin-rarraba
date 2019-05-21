package bookstore.server;

import java.io.*;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import bookstore.model.*;

public class OrderWorker extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private ActorRef client = null;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(OrderRequest.class, order -> {
                    client = getSender();
                    SearchRequest request = new SearchRequest(order.getTitle());
                    getContext().actorSelection("akka.tcp://server_system@127.0.0.1:3552/user/server/searchWorker")
                            .tell(request, getSelf());
                })
                .match(SearchResponse.class, response -> {
                    if (response.getPrice() != 0) {
                        boolean completed = saveOrder(response.getTitle());
                        log.info("Requested book ordered successfully.");
                        OrderResponse result = new OrderResponse(response.getTitle(), completed);
                        client.tell(result, getSelf());
                    } else {
                        log.info("Cannot order, no such book in databases.");
                        OrderResponse result = new OrderResponse(response.getTitle(), false);
                        client.tell(result, getSelf());
                    }
                })
                .build();
    }

    private synchronized boolean saveOrder(String title) {
        try {
            PrintWriter printWriter = new PrintWriter(new FileWriter("src/data/orders.txt", true));
            printWriter.println(title);
            printWriter.close();
        } catch (IOException ex) {
            return false;
        }
        return true;
    }
}
