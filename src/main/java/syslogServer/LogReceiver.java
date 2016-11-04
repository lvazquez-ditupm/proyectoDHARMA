package syslogServer;

import control.Dharma;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import utils.MarkovController;

/**
 * This class represents an receiver of logs coming from different IDSs
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class LogReceiver {

    private String netMask;
    private DatagramSocket socketUDP;
    public static String fname;
    public static Object lck = new Object();
    boolean received_alert = false;
    String log_received;
    Dharma dharma;

    public LogReceiver(Dharma dharma, int UDPport, String ip) {
        try {
            this.dharma = dharma;
            socketUDP = new DatagramSocket(UDPport, InetAddress.getByName(ip));
        } catch (UnknownHostException | SocketException e) {
            System.err.println("Imposible obtener acceso al socket UDP. Terminando sistema...");
            System.exit(0);
        }
    }

    public void start() {

        System.out.println("****  Arrancando receptor de logs de AIRS externos  *****");

        Receiver r = new Receiver(dharma);
        ReceiveSocketUDPAlert u = new ReceiveSocketUDPAlert();

        new Thread(r).start();
        new Thread(u).start();

    }

    /**
     * Recepción de hilos ordenada
     *
     * @return log recibido
     */
    synchronized String getAlert() {
        if (!received_alert) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("InterruptedException caught");
            }
        }

        received_alert = false;
        notify();
        return log_received;
    }

    /**
     * Libera el cerrojo al recibir un log nuevo
     *
     * @param log log obtenido
     */
    synchronized void putAlert(String log) {
        if (received_alert) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("InterruptedException caught");
            }
        }
        this.log_received = log;
        received_alert = true;
        notify();
    }

    /**
     * Recoge los datos obtenidos y los envía al gestor de Syslogs
     */
    class Receiver implements Runnable {

        ExecutorService exec;
        SyslogCreator syslogCreator = new SyslogCreator();
        Dharma dharma;
        MarkovController markovController;
        
        public Receiver(Dharma dharma) {
            exec = Executors.newFixedThreadPool(10);
            this.dharma = dharma;
            markovController=new MarkovController();
        }

        public void run() {
            try {
                while (true) {
                    String receivedLog = getAlert();
                    if (receivedLog.contains("HMM")) {
                        markovController.parse(dharma, receivedLog);
                    }else if(receivedLog.contains("Finished attack")){
                        markovController.delete(dharma, receivedLog);
                    }
                    else {
                        syslogCreator.put(receivedLog);
                    }
                }
            } catch (Exception ex) {
            }
        }
    }

    /**
     * Recibe datos del socket UDP y notifica a los hilos
     */
    class ReceiveSocketUDPAlert implements Runnable {

        public void run() {

            String logFormatted;

            try {
                byte[] buf = new byte[4096];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socketUDP.receive(packet);
                    String log = new String(packet.getData(), packet.getOffset(), packet.getLength());

                    if (!log.equals("")) {
                        putAlert(log);
                    }
                }
            } catch (Exception e) {
            }
        }
    }

}
