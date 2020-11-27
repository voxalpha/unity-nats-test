import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Subscription;
import org.HdrHistogram.Histogram;

import java.util.concurrent.atomic.AtomicLong;

public class NatsStatus {
    public String name = "";
    public AtomicLong activeNanoTime = new AtomicLong();
    public long batchStartedNanoTime = 0;
    public long batchFinishedNanoTime = 0;
    public boolean inProgress = true;
    public AtomicLong messages = new AtomicLong();
    public Connection connection = null;
    public Dispatcher dispatcher = null;
    public boolean updated = false;

    public long lCount = 0;
    public long lSum = 0;
    public long maxLatency = 0;
    public long minLatency = Long.MAX_VALUE;
    public Histogram histogram = new Histogram(3600000000000L, 3);
    //public long avgLatency = 0;
}
