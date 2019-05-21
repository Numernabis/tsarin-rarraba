package bookstore.server;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import bookstore.model.*;
import scala.concurrent.duration.Duration;

import static akka.actor.SupervisorStrategy.*;

public class ServerActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, s -> {
                    if (s.startsWith("search")) {
                        SearchRequest request = new SearchRequest(s.substring(7));
                        context().child("searchWorker").get().tell(request, getSender());
                    } else if (s.startsWith("order")) {
                        OrderRequest request = new OrderRequest(s.substring(6));
                        context().child("orderWorker").get().tell(request, getSender());
                    } else if (s.startsWith("stream")) {
                        StreamRequest request = new StreamRequest(s.substring(7));
                        context().child("streamWorker").get().tell(request, getSender());
                    }
                })
                .build();
    }


    private static SupervisorStrategy strategy
            = new OneForOneStrategy(10, Duration.create("1 minute"), DeciderBuilder
            .matchAny(o -> restart())
            .build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    @Override
    public void preStart() throws Exception {
        context().actorOf(Props.create(SearchWorker.class), "searchWorker");
        context().actorOf(Props.create(OrderWorker.class), "orderWorker");
        context().actorOf(Props.create(StreamWorker.class), "streamWorker");
    }
}
