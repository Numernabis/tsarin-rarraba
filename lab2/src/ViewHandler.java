import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.MergeView;
import org.jgroups.View;

public class ViewHandler extends Thread {
    JChannel channel;
    MergeView view;

    public ViewHandler(JChannel channel, MergeView view) {
        this.channel = channel;
        this.view = view;
    }

    public void run() {
        View tmp_view = view.getSubgroups().get(0);
        Address local_addr = channel.getAddress();
        System.out.println("ViewHandler.run");

        if (!tmp_view.getMembers().contains(local_addr)) {
            System.out.println("Not member of the new primary partition ("
                    + tmp_view + "), will re-acquire the state");
            try {
                channel.getState(null, 30000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Not member of the new primary partition ("
                    + tmp_view + "), will do nothing");
        }
    }
}
