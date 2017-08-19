import eu.phisikus.plotka.conf.model.BasicPeerConfiguration;
import eu.phisikus.plotka.model.NetworkPeer;
import eu.phisikus.plotka.network.consumer.StandardNetworkMessageConsumer;
import eu.phisikus.plotka.network.listener.NetworkListener;
import eu.phisikus.plotka.network.listener.NetworkListenerBuilder;
import eu.phisikus.plotka.network.talker.NetworkTalker;
import eu.phisikus.plotka.network.talker.Talker;
import scala.Serializable;
import scala.runtime.BoxedUnit;

public class SimpleExample {

    public static void main(String[] args) {
        NetworkPeer localPeer = new NetworkPeer("LocalPeer1", "127.0.0.1", 3030);
        Talker testTalker = new NetworkTalker(localPeer);
        NetworkListener testListener = NetworkListenerBuilder.apply()
                .withId(localPeer.id())
                .withAddress(localPeer.address())
                .withPort(localPeer.port())
                .withPeer(new BasicPeerConfiguration(localPeer.address(), localPeer.port()))
                .withMessageHandler(new StandardNetworkMessageConsumer(localPeer, (message, talker) -> {
                    TextMessage textMessage = (TextMessage) message.message();
                    System.out.println(textMessage);
                    return BoxedUnit.UNIT;
                }))
                .build();

        testListener.start();
        testTalker.send(localPeer, new TextMessage("Hello!"));
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        testListener.stop();
    }

}
