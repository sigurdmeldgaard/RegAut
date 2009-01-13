package RegAut;

/** 
 * A pair of a state and an alphabet symbol. 
 * This is used in the representation of transition functions in the automata.
 */
public class StateSymbolPair {
    
    /** The first component of the pair. */
    public State state;
    
    /** The second component of the pair. */
    public Character symbol;
    
    /** Constructs a new pair. */
    public StateSymbolPair(State state, Character symbol) {
        this.state = state;
        this.symbol = symbol;
    }
    
    /** Checks whether two pairs are equals. */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StateSymbolPair))
            return false;
        StateSymbolPair p = (StateSymbolPair) obj;
        return p.state==state && p.symbol.charValue()==symbol.charValue();
    }
    
    /** Computes hash code for this object. */
    @Override
    public int hashCode() {
        return state.hashCode()*3 + symbol.hashCode()*2;
    }
}
