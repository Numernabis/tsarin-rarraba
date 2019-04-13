public class SampleApp {
    public static void main(String[] args) {
        try {
            new MyChannel().start();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
