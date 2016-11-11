package control;

import control.Dharma;
import java.util.HashMap;
import control.MarkovController;

public class MCCDcontroller {

    MarkovController markovController = new MarkovController();
    Dharma dharma = new Dharma();

    public MCCDcontroller() {
    }

    public void simulateHMM(HashMap<String, String> eventReceived) {
        String event = eventReceived.get("event");
        String classification = eventReceived.get("classification");
        String att2 = "HMM: IDAtaque=\"1\";";
        String att3 = "HMM: IDAtaque=\"1\";";
        
        //";TipoAtaque=\"DDOS\";Nodos=\"D2,D3,A2,N2\";Estado=\"D2\";PEstado=\"0.3\";PFFinal=\"0.4\"");
        
        //markovController.parse(markovLog);
    }

}
