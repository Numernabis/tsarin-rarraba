package bookstore.server;

import akka.event.Logging;
import akka.event.LoggingAdapter;
import bookstore.model.*;
import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import scala.concurrent.duration.Duration;

import static akka.actor.SupervisorStrategy.restart;

public class SearchWorker extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private ActorRef client = null;
    private int counter = 2;
    private boolean found = false;

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(SearchRequest.class, request -> {
                    found = false;
                    counter = 2;
                    client = getSender();
                    SearchRequestDB requestDB1 = new SearchRequestDB(request.getTitle(), "src/data/db1.txt");
                    SearchRequestDB requestDB2 = new SearchRequestDB(request.getTitle(), "src/data/db2.txt");
                    context().child("searchDB1").get().tell(requestDB1, getSelf());
                    context().child("searchDB2").get().tell(requestDB2, getSelf());
                })
                .match(SearchResponse.class, response -> {
                    counter--;
                    if (found) {
                        return;
                    } else if ((response.getPrice() != 0) || counter == 0) {
                        client.tell(response, getSelf());
                        log.info("Search result send directly to client.");
                        found = true;
                    }
                })
                .build();
    }

    private SupervisorStrategy strategy
            = new OneForOneStrategy(10, Duration.create("1 minute"), DeciderBuilder
            .matchAny(o -> restart())
            .build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    @Override
    public void preStart() throws Exception {
        context().actorOf(Props.create(SearchHelper.class), "searchDB1");
        context().actorOf(Props.create(SearchHelper.class), "searchDB2");
    }
}
