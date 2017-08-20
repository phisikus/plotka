import eu.phisikus.plotka.conf.NodeConfiguration;
import eu.phisikus.plotka.conf.model.BasicNodeConfiguration;
import eu.phisikus.plotka.conf.model.BasicPeerConfiguration;
import eu.phisikus.plotka.model.NetworkPeer;
import eu.phisikus.plotka.model.Peer;
import eu.phisikus.plotka.network.listener.NetworkListener;
import eu.phisikus.plotka.network.listener.NetworkListenerBuilder;
import eu.phisikus.plotka.network.talker.NetworkTalker;
import eu.phisikus.plotka.network.talker.Talker;
import scala.collection.concurrent.BasicNode;

public class SimpleExample {

    public static void main(String[] args) {
        NetworkPeer localPeer = new NetworkPeer("LocalPeer1", "127.0.0.1", 3030);
        Talker testTalker = new NetworkTalker(localPeer);
        NetworkListener testListener = NetworkListenerBuilder.apply()
                .withId(localPeer.getId())
                .withAddress(localPeer.getAddress())
                .withPort(localPeer.getPort())
                .withPeer(new BasicPeerConfiguration(localPeer.getAddress(), localPeer.getPort()))
                .withMessageHandler(message -> {
                })
                .build();


        NodeConfiguration nodeConfiguration = new BasicNodeConfiguration("123", 123, "123", null);
        Peer peer = new Peer("wef");

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
