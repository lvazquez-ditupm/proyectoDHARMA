package communications;

import control.Dharma;
import control.Main;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import control.MarkovController;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.IDSManager;
import utils.NetAnomManager;

/**
 * This class represents an receiver of logs coming from different IDSs or HMM
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class LogReceiver {

    private DatagramSocket socketUDP;
    boolean received_alert = false;
    String log_received;
    Dharma dharma = new Dharma();

    public LogReceiver(int UDPportHMM, String ip) {
        try {
            socketUDP = new DatagramSocket(UDPportHMM, InetAddress.getByName(ip));
        } catch (UnknownHostException | SocketException e) {
            System.err.println("Imposible obtener acceso al socket UDP. Terminando sistema...");
            System.exit(0);
        }
    }

    public void start() {
        System.out.println("****  Arrancando receptor de logs de HMM  ****");
        ReceiveSocketUDP h = new ReceiveSocketUDP();
        new Thread(h).start();
    }

    /**
     * Recibe datos del socket UDP y los gestiona
     */
    class ReceiveSocketUDP implements Runnable {

        MarkovController markovController = new MarkovController();

        @Override
        public void run() {
            try {
                byte[] buf = new byte[4096];

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socketUDP.receive(packet);
                    String receivedLog = new String(packet.getData(), packet.getOffset(), packet.getLength());
                    if (!receivedLog.equals("")) {
                        //System.out.println(receivedLog);
                        if (receivedLog.contains("Finished attack")) {
                            markovController.delete(receivedLog);
                        } else if (receivedLog.contains("IDS///")) {
                            new IDSManager(receivedLog.substring(6));
                        } else if (receivedLog.contains("NetAnom///")) {
                            new NetAnomManager(receivedLog.substring(10));
                        } else {
                            try{
                                markovController.parse(receivedLog);
                            }catch(Exception ex){
                                Logger.getLogger(LogReceiver.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(LogReceiver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
