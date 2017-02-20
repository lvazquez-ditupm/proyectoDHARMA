package communications;

import control.Dharma;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import control.MarkovController;

/**
 * This class represents an receiver of logs coming from different IDSs
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class LogReceiver {

    private DatagramSocket socketUDP;
    boolean received_alert = false;
    String log_received;
    Dharma dharma = new Dharma();

    public LogReceiver(int UDPport, String ip) {
        try {
            socketUDP = new DatagramSocket(UDPport, InetAddress.getByName(ip));
        } catch (UnknownHostException | SocketException e) {
            System.err.println("Imposible obtener acceso al socket UDP. Terminando sistema...");
            System.exit(0);
        }
    }

    public void start() {
        System.out.println("****  Arrancando receptor de logs de HMM  ****");
        ReceiveSocketUDPAlert u = new ReceiveSocketUDPAlert();
        new Thread(u).start();
    }

    /**
     * Recibe datos del socket UDP y los gestiona
     */
    class ReceiveSocketUDPAlert implements Runnable {

        MarkovController markovController = new MarkovController();

        @Override
        public void run() {
            try {
                byte[] buf = new byte[4096];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socketUDP.receive(packet);
                    String receivedLog = new String(packet.getData(),
                            packet.getOffset(), packet.getLength());
                    if (!receivedLog.equals("")) {
                        System.out.println(receivedLog);
                        if (receivedLog.contains("Finished attack")) {
                            markovController.delete(receivedLog);
                        } else {
                            markovController.parse(receivedLog);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

}
