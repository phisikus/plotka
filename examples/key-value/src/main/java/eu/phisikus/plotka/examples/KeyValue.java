package eu.phisikus.plotka.examples;

import com.google.gson.Gson;
import eu.phisikus.plotka.conf.NodeConfiguration;
import eu.phisikus.plotka.conf.PeerConfiguration;
import eu.phisikus.plotka.conf.providers.FileConfigurationProvider;
import eu.phisikus.plotka.examples.messages.KVEntry;
import eu.phisikus.plotka.model.NetworkPeer;
import eu.phisikus.plotka.network.consumer.StandardNetworkMessageConsumer;
import eu.phisikus.plotka.network.listener.NetworkListener;
import eu.phisikus.plotka.network.talker.NetworkTalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.immutable.List;
import scala.runtime.BoxedUnit;
import spark.Route;
import spark.Spark;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KeyValue {
    private static Map<String, KVEntry> data = new ConcurrentHashMap<>();
    private static Logger logger = LoggerFactory.getLogger(KeyValue.class);
    private static ScheduledExecutorService replicationService = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) {
        NodeConfiguration nodeConfiguration = getNodeConfiguration();
        NetworkPeer myself = new NetworkPeer(nodeConfiguration.id(), nodeConfiguration.address(), nodeConfiguration.port());
        NetworkTalker networkTalker = new NetworkTalker(myself);
        NetworkListener networkListener = new NetworkListener(nodeConfiguration, getMessageHandlerThatAddsIncommingKVs(myself));
        networkListener.start();

        Gson serializer = new Gson();
        Spark.port(nodeConfiguration.settings().get().getInt("http-port"));
        Spark.get("/store", (request, response) -> serializer.toJson(data.values()));
        Spark.get("/store/:key", getValueHandler());
        Spark.post("/store/:key", setValueHandler(networkTalker, nodeConfiguration.peers()));
        Spark.put("/store/:key", setValueHandler(networkTalker, nodeConfiguration.peers()));

        Runnable sendAllKeysToEveryone = () -> data.values().forEach(kvEntry -> nodeConfiguration
                .peers()
                .iterator()
                .foreach(peer -> {
                    NetworkPeer recipient = new NetworkPeer(peer.address(), peer.port());
                    networkTalker.send(recipient, kvEntry);
                    return BoxedUnit.UNIT;
                }));
        replicationService.scheduleWithFixedDelay(sendAllKeysToEveryone, 0L, 15L, TimeUnit.SECONDS);

    }

    private static StandardNetworkMessageConsumer getMessageHandlerThatAddsIncommingKVs(NetworkPeer myself) {
        return new StandardNetworkMessageConsumer(myself, (message, talker) -> {
            KVEntry newEntry = (KVEntry) message.getMessage();
            logger.info("I've received new entry from my peer: {}", newEntry);
            KVEntry entry = data.getOrDefault(newEntry.getKey(), newEntry);
            if (entry.getTimestamp() >= newEntry.getTimestamp()) {
                data.put(newEntry.getKey(), newEntry);
            }
            return BoxedUnit.UNIT;
        });
    }

    private static NodeConfiguration getNodeConfiguration() {
        FileConfigurationProvider configurationProvider = new FileConfigurationProvider();
        return configurationProvider.loadConfiguration();
    }

    private static Route setValueHandler(NetworkTalker networkTalker, List<PeerConfiguration> peers) {
        return (request, response) -> {
            String value = request.body();
            String key = request.params("key");
            KVEntry newEntry = new KVEntry(key, value, Calendar.getInstance().getTimeInMillis());
            peers.foreach(peer -> {
                NetworkPeer recipient = new NetworkPeer(peer.address(), peer.port());
                networkTalker.send(recipient, newEntry);
                return BoxedUnit.UNIT;
            });
            return value;
        };
    }

    private static Route getValueHandler() {
        return (request, response) -> {
            String key = request.params("key");
            if (data.containsKey(key)) {
                response.status(200);
                return data.get(key).getValue();
            }
            response.status(404);
            return "key not found";
        };
    }
}
