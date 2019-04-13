import org.jgroups.*;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.*;
import org.jgroups.stack.ProtocolStack;
import org.jgroups.util.Util;

import java.io.*;
import java.net.InetAddress;
import java.util.*;

public class MyChannel extends ReceiverAdapter {
    private JChannel channel;
    private final Map<String, Integer> state = new HashMap<>();
    private DistributedMap localMap = new DistributedMap(state);
    private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    private static JChannel createChannel() {
        System.setProperty("java.net.preferIPv4Stack", "true");
        JChannel channel = null;

        try {
            channel = new JChannel(false);
            InetAddress address = InetAddress.getByName("232.232.232.32");
            ProtocolStack stack = new ProtocolStack();

            channel.setProtocolStack(stack);
            stack.addProtocol(new UDP()
                            .setValue("mcast_group_addr", address))
                    .addProtocol(new PING())
                    .addProtocol(new MERGE3())
                    .addProtocol(new FD_SOCK())
                    .addProtocol(new FD_ALL()
                            .setValue("timeout", 12000)
                            .setValue("interval", 3000))
                    .addProtocol(new VERIFY_SUSPECT())
                    .addProtocol(new BARRIER())
                    .addProtocol(new NAKACK2())
                    .addProtocol(new UNICAST3())
                    .addProtocol(new STABLE())
                    .addProtocol(new GMS())
                    .addProtocol(new UFC())
                    .addProtocol(new MFC())
                    .addProtocol(new FRAG2())
                    .addProtocol(new STATE()); //<-----
                    //.addProtocol(new DISCARD().setUpDiscardRate(1)); //<-----
                    //.addProtocol(new SEQUENCER())
                    //.addProtocol(new FLUSH());

            stack.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return channel;
    }

    public void start() throws Exception {
        channel = createChannel();
        channel.setReceiver(this);
        channel.connect("fantastic");
        channel.getState(null, 10000);
        //channel.getState(null, 10000);
        cmdLoop();
        //Thread.sleep(60 * 1000);
        channel.close();
    }

    private void cmdLoop() {
        while (true) {
            try {
                System.out.flush();
                System.out.print("> ");
                String line = in.readLine().toLowerCase();
                if (line.startsWith("q")) {
                    break;
                }
                String[] input = line.split(" ");
                switch (input[0]) {
                    case "c":
                        System.out.println(localMap.containsKey(input[1]));
                        break;
                    case "g":
                        System.out.println(localMap.get(input[1]));
                        break;
                    case "r":
                        if (localMap.containsKey(input[1])) {
                            System.out.println("remove element locally (key=" + input[1] + ")");
                            localMap.remove(input[1]);
                        } else {
                            System.out.println("unable to remove, no such key");
                        }
                        break;
                    case "p":
                        if (input[2].matches("\\d+")) {
                            channel.getState(null, 10000);

                            System.out.println("add new element (key=" + input[1]
                                    + ", value=" + input[2] + ")");
                            localMap.put(input[1], Integer.valueOf(input[2]));

                            Message msg = new Message(null, null, line);
                            send(msg);
                        } else {
                            System.out.println("value needs to be integer");
                        }
                        break;
                    case "m":
                        System.out.println("localMap = " + localMap.getMap());
                        break;
                    case "x":
                        simulatePartition();
                        break;
                    default:
                        printHelp();
                        break;
                }

//                Message msg = new Message(null, null, line);
//                send(msg);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void simulatePartition() throws Exception {
        channel.close();
        channel = createChannel();
        channel.setReceiver(this);

        channel.connect("temp-part");
        channel.getState(null, 10000);

        localMap.put("m", 54);
        localMap.put("k", 65);
        localMap.put("j", 76);
        System.out.println("localMap = " + localMap.getMap());
        Thread.sleep(2000);

        channel.close();
        channel = createChannel();
        channel.setReceiver(this);

        channel.connect("fantastic");
        channel.getState(null, 10000);
    }

    private void printHelp() {
        System.out.println("----- HELP -----\n"
                + "c key - containsKey(key)\n"
                + "g key - get(key)\n"
                + "r key - remove(key)\n"
                + "p key value - put(key,value)\n"
                + "m - print localMap\n"
                + "x - simulatePartition\n"
                + "----- ---- -----");
    }

    /* ----------------------------------------------------------------------- */

    private void send(Message msg) {
        try {
            channel.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receive(Message msg) {
        String msgReceived = msg.getSrc() + ": " + msg.getObject();
        System.out.println(msgReceived);

        String[] msgPart = msg.getObject().toString().split(" ");
        String action = msgPart[0];
        if (action.equals("p")) {
            localMap.put(msgPart[1], Integer.valueOf(msgPart[2]));
        }
//        } else if (action.equals("r")) {
//            localMap.remove(msgPart[1]);
//        }

        System.out.println("localMap = " + localMap.getMap());
    }

    /* ----------------------------------------------------------------------- */
    // http://www.jgroups.org/manual/index.html#StateTransfer

    public void getState(OutputStream output) throws Exception {
        synchronized (state) {
            Util.objectToStream(state, new DataOutputStream(output));
        }
    }

    public void setState(InputStream input) throws Exception {
        Map<String, Integer> map;
        map = (Map<String, Integer>) Util.objectFromStream(new DataInputStream(input));
        synchronized (state) {
            state.clear();
            state.putAll(map);
        }
        System.out.println("received state (" + map.size() + " elements in hashTable):");
        for (Map.Entry<String, Integer> str : map.entrySet()) {
            System.out.println(str);
        }
    }

    /* ----------------------------------------------------------------------- */
    // http://www.jgroups.org/manual/index.html#HandlingNetworkPartitions

    private static void handleView(JChannel channel, View view) {
        if (view instanceof MergeView) {
            ViewHandler handler = new ViewHandler(channel, (MergeView) view);
            handler.start();
        }
    }

    @Override
    public void viewAccepted(View view) {
        System.out.println("** view: " + view);
        handleView(channel, view);
    }
}
