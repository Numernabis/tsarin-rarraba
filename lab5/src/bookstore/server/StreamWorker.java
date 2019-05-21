package bookstore.server;

import akka.NotUsed;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import akka.stream.OverflowStrategy;
import akka.stream.ThrottleMode;
import akka.stream.javadsl.*;
import bookstore.model.*;
import akka.actor.*;
import scala.concurrent.duration.FiniteDuration;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class StreamWorker extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private ActorRef client = null;

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(StreamRequest.class, order -> {
                    client = getSender();
                    SearchRequest request = new SearchRequest(order.getTitle());
                    getContext().actorSelection("akka.tcp://server_system@127.0.0.1:3552/user/server/searchWorker")
                            .tell(request, getSelf());
                })
                .match(SearchResponse.class, response -> {
                    if (response.getPrice() == 0) {
                        String msg = "Cannot stream, no such book in databases.";
                        log.info(msg);
                        StreamResponse result = new StreamResponse(msg, true);
                        client.tell(result, getSelf());
                        return;
                    }
                    String title_ = response.getTitle().replace(" ", "_");
                    String fileName = "src/data/books/".concat(title_.toLowerCase().concat(".txt"));
                    log.info("File to stream : " + fileName);

                    File f = new File(fileName);
                    ActorMaterializer mat = ActorMaterializer.create(getContext());
                    ActorRef run = Source.actorRef(1000, OverflowStrategy.dropNew())
                            .throttle(
                                    1,
                                    FiniteDuration.create(1, TimeUnit.SECONDS),
                                    1,
                                    ThrottleMode.shaping())
                            .to(Sink.actorRef(client, NotUsed.getInstance()))
                            .run(mat);

                    Stream<String> lines = Files.lines(f.toPath());
                    log.info("Streaming file to client...");
                    lines.forEachOrdered(
                            line -> run.tell(new StreamResponse(line, false), getSelf()));
                    run.tell(new StreamResponse("Done", true), getSelf());
                    log.info("Streaming completed.");
                })
                .matchAny(o -> log.info(o.toString()))
                .build();
    }
}
