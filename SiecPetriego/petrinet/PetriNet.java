package petrinet;

import java.util.*;
import java.util.concurrent.Semaphore;

public class PetriNet<T> {

    public static void main(String[] args) throws InterruptedException {
        Map<String, Integer> map = new HashMap<>();
        map.put("A", 1);
        map.put("B", 1);
        PetriNet<String> PetriNet = new PetriNet<>(map, true);
        Collection<String> empty = new HashSet<>();
        Collection<String> inhibitor = new HashSet<>();
        inhibitor.add("A");
        Map<String, Integer> input = new HashMap<>();
        input.put("A", 1);
        Map<String, Integer> output = new HashMap<>();
        output.put("B", 1);
        Map<String, Integer> input2 = new HashMap<>();
        input2.put("B", 1);
        Map<String, Integer> output2 = new HashMap<>();
        output2.put("A", 1);
        Map<String, Integer> input3 = new HashMap<>();
        input3.put("B", 1);
        Map<String, Integer> output3 = new HashMap<>();
        output3.put("C", 1);
        Map<String, Integer> input4 = new HashMap<>();
        input4.put("C", 1);
        Map<String, Integer> output4 = new HashMap<>();
        output4.put("D", 1);
        Map<String, Integer> input5 = new HashMap<>();
        input5.put("D", 1);
        Map<String, Integer> output5 = new HashMap<>();
        output5.put("A", 1);
        Transition<String> t1 = new Transition<>(input, empty, empty, output);
        Transition<String> t2 = new Transition<>(input2, empty, empty, output2);
        Transition<String> t3 = new Transition<>(input3, empty, empty, output3);
        Transition<String> t4 = new Transition<>(input4, empty, empty, output4);
        Transition<String> t5 = new Transition<>(input5, empty, empty, output5);
        Collection<Transition<String>> coll1 = new HashSet<>();
        coll1.add(t1);
        Collection<Transition<String>> coll2 = new HashSet<>();
        coll2.add(t2);
        Collection<Transition<String>> coll3 = new HashSet<>();
        coll3.add(t1);
        coll3.add(t2);
        Collection<Transition<String>> all = new HashSet<>();
        all.add(t1);
        all.add(t2);
        all.add(t3);
        all.add(t4);
        all.add(t5);
       /* Runnable r1 = new Pomocniczy<String>(coll1, PetriNet);
        Thread thread1 = new Thread(r1, "1");
        Thread thread1A = new Thread(r1, "1A");
        Thread thread1B = new Thread(r1, "1B");
        Runnable r2 = new Pomocniczy<String>(coll2, PetriNet);
        Thread thread2 = new Thread(r2, "2");
        Runnable r3 = new Pomocniczy<String>(coll3, PetriNet);
        Thread thread3 = new Thread(r3, "3");
        Thread thread3A = new Thread(r3, "3A");
        Thread.sleep(1000);
        thread1.start();
        Thread.sleep(1000);
        thread1A.start();
        Thread.sleep(1000);
        thread1B.start();
        Thread.sleep(1000);
        thread2.start();
        Thread.sleep(1000);
        thread3.start();
        Thread.sleep(1000);
        thread3A.start(); */
        Set<Map<String, Integer>> set = PetriNet.reachable(all);
        System.out.println(set.size());
    }


    private Semaphore security;
    private Map<T, Integer> net;
    private LinkedList<Pair<T>> queue = new LinkedList<>();
    private boolean fair;

    private static class Pair<T> {
        Collection<Transition<T>> first;
        Semaphore second;

        Pair(Collection<Transition<T>> f, Semaphore s){
            this.first = f;
            this.second = s;
        }
    }

    private static class Pomocniczy<T> implements Runnable {

        Collection<Transition<T>> coll;
        PetriNet<T> net;

        public Pomocniczy(Collection<Transition<T>> coll, PetriNet<T> net){
            this.coll = coll;
            this.net = net;
        }

        @Override
        public void run() {
            try {
                net.fire(coll);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public PetriNet(Map<T, Integer> initial, boolean fair) {
        this.net = initial;
        this.security = new Semaphore(1);
        this.fair = fair;
    }

    public Integer get(T key){
        return coalesce(net.get(key), 0);
    }

    public Set<Map<T, Integer>> reachable(Collection<Transition<T>> transitions) {
        try {
            security.acquire();
            Set<Map<T, Integer>> set =  new HashSet<>();
            Queue<Map<T, Integer>> queue = new LinkedList<>();
            queue.add(net);
            BFS(set, transitions, queue);
            return set;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            security.release();
        }
        return null;
    }

    // return true to stop
    public boolean BFS(Set<Map<T, Integer>> set, Collection<Transition<T>> transitions,
                        Queue<Map<T, Integer>> queue){
        Map<T, Integer> map;
        while(!queue.isEmpty()){
            map = queue.poll();
            for(T k : map.keySet()){
                if(map.get(k) == 0){
                    map.remove(k);
                }
            }
            if (set.add(map)) {
                int i = 1;
                for (Transition<T> x : transitions) {
                    Map<T, Integer> petri = new HashMap<>(map);
                    if(checkOne(x, petri)){
                        perform(x, petri);
                        queue.add(petri);
                    }
                    i++;
                }
            }
        }
        return true;
    }

    public Transition<T> fire(Collection<Transition<T>> transitions) throws InterruptedException {
        security.acquire();
        if(!checkMany(transitions)){
            // then we have to insert into queue pairs <Transition<T>, Semaphore>
            Semaphore s = new Semaphore(0, true); // styknie true?
            Pair<T> pair = new Pair<>(transitions, s);
            queue.add(pair);
            security.release();
            s.acquire();
        }
        Transition<T> ret = perform(transitions);

        for(int i=0; i<queue.size(); i++){
            // this will run only if something is in the queue
            if(checkMany(queue.get(i).first)){
                Semaphore s = queue.get(i).second;
                queue.remove(i);
                s.release();
                return ret;
            }
        }
        security.release();
        return ret;
    }

    // returns true if given transition is allowed
    // else false
    private boolean checkOne(Transition<T> t){

        for(T k : t.input.keySet()){
            if(net.get(k) == null || net.get(k) < t.input.get(k)){
                return false;
            }
        }

        for(T k : t.inhibitor){
            if(net.get(k) != null && net.get(k) != 0){
                return false;
            }
        }
        return true;
    }

    private boolean checkOne(Transition<T> t, Map<T, Integer> petri){

        for(T k : t.input.keySet()){
            if(petri.get(k) == null || petri.get(k) < t.input.get(k)){
                return false;
            }
        }

        for(T k : t.inhibitor){
            if(petri.get(k) != null && petri.get(k) != 0){
                return false;
            }
        }
        return true;
    }

    // returns true if one of given transitions is allowed
    // else false
    private boolean checkMany(Collection<Transition<T>> transitions){
        for(Transition<T> t : transitions){
            if(checkOne(t)){
                return true;
            }
        }
        return false;
    }

    // activate one of given transitions in global map "this.net"
    private Transition<T> perform(Collection<Transition<T>> transitions){
        for(Transition<T> t : transitions){
            if(checkOne(t)) {
                t.input.forEach((k, v) -> net.put(k,net.get(k) - v));
                t.output.forEach((k, v) -> net.put(k, coalesce(net.get(k), 0) + v));
                for(T k : t.input.keySet()) {
                    if(net.get(k) == 0){
                        net.remove(k);
                    }
                }

                for (T k : t.reset) {
                    net.remove(k);
                }
                return t;
            }
        }
        return null;
    }

    public static Integer coalesce(Integer a, Integer b) {
        return a == null ? b : a;
    }

    // activate given transition in given map
    private boolean perform(Transition<T> t, Map<T, Integer> petri){
            if(checkOne(t, petri)) {
                t.input.forEach((k, v) -> petri.put(k, petri.get(k) - v));
                t.output.forEach((k, v) -> petri.put(k, coalesce(petri.get(k), 0) + v));
                for(T k : t.input.keySet()) {
                    if(petri.get(k) == 0){
                        petri.remove(k);
                    }
                }
                for (T k : t.reset) {
                    petri.remove(k);
                }
                return true;
            }
        return false;
    }

}
