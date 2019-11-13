package multiplicator;

import petrinet.PetriNet;
import petrinet.Transition;

import java.util.Scanner;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Main {

    private static Thread thread1;
    private static Thread thread2;
    private static Thread thread3;
    private static Thread thread4;
    private static Thread threadMain;

    public static class Pomocniczy implements Runnable {

        private Collection<Transition<String>> coll;
        private PetriNet<String> net;
        private Integer ile = 0;

        public Pomocniczy(Collection<Transition<String>> coll, PetriNet<String> net){
            this.coll = coll;
            this.net = net;
        }

        public Integer getIle(){
            return this.ile;
        }

        @Override
        public void run() {
            try {
                while(true) {
                    net.fire(coll);
                    this.ile++;
                }
            } catch (InterruptedException e) {
            } finally {
                System.out.println(Thread.currentThread().getName()+" "+this.ile);
            }
        }
    }

    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        Map<String, Integer> map = new HashMap<>();
        map.put("M", sc.nextInt());
        map.put("N1", sc.nextInt());
        PetriNet<String > net = new PetriNet<>(map, true);
        Map<String, Integer> emptyMap = new HashMap<>();
        Collection<String> emptyColl = new HashSet<>();
        Collection<String> mainInhibitor = new HashSet<>();
        mainInhibitor.add("N");
        mainInhibitor.add("M");
        mainInhibitor.add("m1");
        mainInhibitor.add("m2");
        Transition<String> last = new Transition<>(emptyMap, emptyColl, mainInhibitor, emptyMap);

        Map<String, Integer> input1 = new HashMap<>();
        input1.put("N1", 1);
        input1.put("m1", 1);
        Map<String, Integer> output1 = new HashMap<>();
        output1.put("N2", 1);
        output1.put("m1", 1);
        output1.put("Final", 1);
        Collection<String> inhibitor1 = new HashSet<>();
        inhibitor1.add("m2");
        Transition<String> N1 = new Transition<>(input1, emptyColl, inhibitor1, output1);

        Map<String, Integer> input2 = new HashMap<>();
        input2.put("N2", 1);
        input2.put("m2", 1);
        Map<String, Integer> output2 = new HashMap<>();
        output2.put("m2", 1);
        output2.put("N1", 1);
        Collection<String> inhibitor2 = new HashSet<>();
        inhibitor2.add("m1");
        Transition<String> N2 = new Transition<>(input2, emptyColl, inhibitor2, output2);

        Map<String, Integer> input3 = new HashMap<>();
        input3.put("m1", 1);
        Map<String, Integer> output3 = new HashMap<>();
        output3.put("m2", 1);
        Collection<String> inhibitor3 = new HashSet<>();
        inhibitor3.add("N1");
        Transition<String> m1 = new Transition<>(input3, emptyColl, inhibitor3, output3);

        Map<String, Integer> input4 = new HashMap<>();
        input4.put("m2", 1);
        Collection<String> inhibitor4 = new HashSet<>();
        inhibitor4.add("N2");
        Transition<String> m2 = new Transition<>(input4, emptyColl, inhibitor4, emptyMap);

        Map<String, Integer> input5 = new HashMap<>();
        input5.put("M", 1);
        Map<String, Integer> output5 = new HashMap<>();
        output5.put("m1", 1);
        Collection<String> inhibitor5 = new HashSet<>();
        inhibitor5.add("N2");
        inhibitor5.add("m1");
        inhibitor5.add("m2");
        Transition<String> M = new Transition<>(input5, emptyColl, inhibitor5, output5);

        Collection<Transition<String>> all = new HashSet<>();
        all.add(N1);
        all.add(N2);
        all.add(m1);
        all.add(m2);
        all.add(M);

        Collection<Transition<String>> finish = new HashSet<>();
        finish.add(last);

        Runnable r1 = new Pomocniczy(all, net);
        Runnable r2 = new Pomocniczy(all, net);
        Runnable r3 = new Pomocniczy(all, net);
        Runnable r4 = new Pomocniczy(all, net);
        thread1 = new Thread(r1, "t1");
        thread2 = new Thread(r2, "t2");
        thread3 = new Thread(r3, "t3");
        thread4 = new Thread(r4, "t4");

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        try {
            net.fire(finish);
            System.out.println(net.get("Final"));
        } catch (InterruptedException e) {
            System.out.println("ERROR in fire()");
        }

        thread1.interrupt();
        thread2.interrupt();
        thread3.interrupt();
        thread4.interrupt();
    }
}
