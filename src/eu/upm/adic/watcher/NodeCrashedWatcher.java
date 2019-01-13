package eu.upm.adic.watcher;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class NodeCrashedWatcher implements Watcher {

    /**
     * Responsible for taking the necessary steps when the watch is triggered.
     * @param event The event object that triggered the watch.
     */
    @Override
    public void process(WatchedEvent event) {

        if (event.getType() == Event.EventType.NodeDeleted) {

            System.out.println("Node is down ---> " + event.getPath());

//            try {
//                new Main();
//            } catch (KeeperException e) {
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

//            Runtime rt = Runtime.getRuntime();
//            String[] commands = {"java", "Main"};
//            try {
//                rt.exec(commands);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

        }
    }
}
