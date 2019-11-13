package alternator;

import petrinet.PetriNet;
import petrinet.Transition;

import java.util.*;

public class Main {

    private static Collection<Transition<String>> out = new HashSet<>();
    private static Collection<Transition<String>> A = new HashSet<>();
    private static Collection<Transition<String>> B = new HashSet<>();
    private static Collection<Transition<String>> C = new HashSet<>();

    public static class Pomocniczy implements Runnable {

        private Collection<Transition<String>> coll;
        private PetriNet<String> net;

        public Pomocniczy(Collection<Transition<String>> coll, PetriNet<String> net){
            this.coll = coll;
            this.net = net;
        }

        @Override
        public void run() {
            try {
                while(true) {
                    net.fire(coll);
                    System.out.print(Thread.currentThread().getName());
                    System.out.print(".");
                    net.fire(out);
                }
            } catch (InterruptedException e) {
            }
        }
    }


    public static void main(String args[]){
        Map<String, Integer> map = new HashMap<>();
        map.put("S", 1);
        PetriNet<String > net = new PetriNet<>(map, true);
        Map<String, Integer> emptyMap = new HashMap<>();
        Collection<String> emptyColl = new HashSet<>();

        Map<String, Integer> inputAIN = new HashMap<>();
        inputAIN.put("S", 1);
        Map<String, Integer> outputAIN = new HashMap<>();
        outputAIN.put("A", 1);
        Collection<String> inhiAIN = new HashSet<>();
        inhiAIN.add("A");
        Collection<String> resetAIN = new HashSet<>();
        resetAIN.add("B");
        resetAIN.add("C");
        Transition<String> AIN = new Transition<>(inputAIN, resetAIN, inhiAIN, outputAIN);

        Map<String, Integer> inputBIN = new HashMap<>();
        inputBIN.put("S", 1);
        Map<String, Integer> outputBIN = new HashMap<>();
        outputBIN.put("B", 1);
        Collection<String> inhiBIN = new HashSet<>();
        inhiBIN.add("B");
        Collection<String> resetBIN = new HashSet<>();
        resetBIN.add("A");
        resetBIN.add("C");
        Transition<String> BIN = new Transition<>(inputBIN, resetBIN, inhiBIN, outputBIN);

        Map<String, Integer> inputCIN = new HashMap<>();
        inputCIN.put("S", 1);
        Map<String, Integer> outputCIN = new HashMap<>();
        outputCIN.put("C", 1);
        Collection<String> inhiCIN = new HashSet<>();
        inhiCIN.add("C");
        Collection<String> resetCIN = new HashSet<>();
        resetCIN.add("B");
        resetCIN.add("A");
        Transition<String> CIN = new Transition<>(inputCIN, resetCIN, inhiCIN, outputCIN);

        Map<String, Integer> outputOUT = new HashMap<>();
        outputOUT.put("S", 1);
        Collection<String> inhiOUT = new HashSet<>();
        inhiOUT.add("S");
        Transition<String> OUT = new Transition<>(emptyMap, emptyColl, inhiOUT, outputOUT);

        out.add(OUT);
        A.add(AIN);
        B.add(BIN);
        C.add(CIN);

        Collection<Transition<String>> all = new HashSet<>();
        all.add(AIN);
        all.add(BIN);
        all.add(CIN);
        all.add(OUT);


        Set<Map<String, Integer>> available = net.reachable(all);
        System.out.println("Liczba możliwych znakowań : "+available.size());

        /* Poniżej sprawdzamy warunek bezpieczenstwa
            1. Może pisać tylko jeden wątek, więc po wykonaniu protokołu wejścia,
                jedynym legalnym przejściem musi być protokół wyjścia.
                Po wykonaniu protokołu wejścia, miejsce S ma 0 żetonóœ.
                Jeśli w danym znakowaniu zachodzi taka sytuacja, należy sprawdzić,
                czy OUT jest jedynym dozwolonym przejściem.
            2. Wątek nie może pisać dwa razy z rzędu.
                Miejsce o nazwie takiej jak wątek, który jako ostatni był w sekcji krytycznej,
                w tej sieci Petriego posiada jeden żeton, dopóki inny wątek nie wykona swojego
                protokołu wejścia. Zatem jeśli w pewnym znakowaniu miejsce A ma żeton,
                to zabronione ma być przejście, będące protokołem wejścia wątku A.
        */

        for(Map<String, Integer> x : available){
            Collection<Transition<String>> checked = net.checkWhich(all, x);
            if(!x.containsKey("S")){
                if(!checked.contains(OUT) || checked.size() != 1){
                    System.out.println("ERROR: Wiele wątków w sekcji krytycznej");
                }
            }
            if(x.containsKey("A")){
                if(checked.contains(AIN)){
                    System.out.println("ERROR: A pisze dwa razy rzędu");
                }
            }
            if(x.containsKey("B")){
                if(checked.contains(BIN)){
                    System.out.println("ERROR: B pisze dwa razy rzędu");
                }
            }
            if(x.containsKey("C")){
                if(checked.contains(CIN)){
                    System.out.println("ERROR: C pisze dwa razy rzędu");
                    System.out.println(net.get("C"));
                }
            }
        }
        // zakończyliśmy sprawdzanie

        Runnable a = new Pomocniczy(A, net);
        Thread threadA = new Thread(a,"A");
        Runnable b = new Pomocniczy(B, net);
        Thread threadB = new Thread(b, "B");
        Runnable c = new Pomocniczy(C, net);
        Thread threadC = new Thread(c, "C");

        threadA.start();
        threadB.start();
        threadC.start();

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadA.interrupt();
        threadB.interrupt();
        threadC.interrupt();

    }
}
