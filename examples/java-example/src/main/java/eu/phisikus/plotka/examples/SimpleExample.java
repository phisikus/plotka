package eu.phisikus.plotka.examples;

import eu.phisikus.plotka.model.NetworkPeer;
import eu.phisikus.plotka.network.consumer.StandardNetworkMessageConsumer;
import eu.phisikus.plotka.network.listener.NetworkListener;
import eu.phisikus.plotka.network.listener.NetworkListenerBuilder;
import eu.phisikus.plotka.network.talker.NetworkTalker;
import eu.phisikus.plotka.network.talker.Talker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.runtime.BoxedUnit;

public class SimpleExample {

    private static final Logger logger = LoggerFactory.getLogger(SimpleExample.class);

    public static void main(String[] args) throws InterruptedException {

        NetworkPeer localPeer = new NetworkPeer("LocalPeer1", "127.0.0.1", 3030);
        Talker testTalker = new NetworkTalker(localPeer);

        StandardNetworkMessageConsumer messageConsumer = new StandardNetworkMessageConsumer(localPeer,
                (message, talker) -> {
                    TextMessage textMessage = (TextMessage) message.getMessage();
                    String receivedText = textMessage.getText();
                    logger.info("All good! I've got the message: {}", receivedText);
                    return BoxedUnit.UNIT;
                });

        NetworkListener testListener = NetworkListenerBuilder.apply()
                .withId(localPeer.getId())
                .withAddress(localPeer.getAddress())
                .withPort(localPeer.getPort())
                .withMessageHandler(messageConsumer)
                .build();


        testListener.start();
        testTalker.send(localPeer, new TextMessage("Hello!"));
        Thread.sleep(1000L);
        testListener.stop();
    }

}
