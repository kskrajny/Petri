package petrinet;

import java.util.Collection;
import java.util.Map;


public class Transition<T> {

    public Map<T, Integer> input;
    public Collection<T> reset;
    public Collection<T> inhibitor;
    public  Map<T, Integer> output;

    public Transition(Map<T, Integer> input, Collection<T> reset,
                      Collection<T> inhibitor, Map<T, Integer> output) {

        this.input = input;
        this.reset = reset;
        this.inhibitor = inhibitor;
        this.output = output;

    }

}
