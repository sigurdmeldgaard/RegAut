package dRegAut;

/** An automaton state. */
public class State implements Cloneable {
    
    /** State name. (The state name is only for printing and has no formal meaning.) */
    public String name;
    
    /** Constructs a new state with no name. */
    public State() {
        name = "";
    }
    
    /** 
     * Constructs a new state with the given name. 
     * @param name state name
     */
    public State(String name) {
        this.name = name;
    }
    
    /** Clones this state. */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
