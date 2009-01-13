package RegAut;

import java.util.*;

/**
 * Deterministic finite state automaton. [Martin, Def. 3.2]
 */
public class FA implements Cloneable {
    
    /** Set of {@link State} objects (Q). */
    public Set<State> states;
    
    /** The automaton alphabet (<img src="http://www.brics.dk/~amoeller/dRegAut/Sigma.gif" alt="sigma">). */
    public Alphabet alphabet;
    
    /** Initial state (q<sub>0</sub>). Member of {@link #states}. */
    public State initial;
    
    /** Accept states (A). Subset of {@link #states}. */
    public Set<State> accept;
    
    /**
     * Transition function (<img src="http://www.brics.dk/~amoeller/dRegAut/delta.gif" alt="delta">). 
     * This is a map from pairs of states and alphabet symbols to states
     * (<img src="http://www.brics.dk/~amoeller/dRegAut/delta.gif" alt="delta">: 
     * Q<img src="http://www.brics.dk/~amoeller/dRegAut/times.gif" 
     *      alt="x"><img src="http://www.brics.dk/~amoeller/dRegAut/Sigma.gif" alt="sigma">
     * <img src="http://www.brics.dk/~amoeller/dRegAut/rightarrow.gif" alt="-&gt;"> Q).
     */
    public Map<StateSymbolPair,State> transitions;
    
    /**
     * Checks that this automaton is well-defined.
     * In particular, this method checks that the transition function is total.
     * This method should be invoked after each <tt>FA</tt> operation during testing.
     * @return this automaton
     * @exception AutomatonNotWellDefinedException if this automaton is not well-defined
     */
    public FA checkWellDefined() throws AutomatonNotWellDefinedException {
        if (states==null || alphabet==null || alphabet.symbols==null ||
            initial==null || accept==null || transitions==null)
            throw new AutomatonNotWellDefinedException("invalid null pointer");
        if (!states.contains(initial))
            throw new AutomatonNotWellDefinedException("the initial state is not in the state set");
        if (!states.containsAll(accept))
            throw new AutomatonNotWellDefinedException("not all accept states are in the state set");
        for (State s : states) 
            for (char c : alphabet.symbols) {
                if (c==NFALambda.LAMBDA)
                    throw new AutomatonNotWellDefinedException("lambda transition appears in transitions");
                State s2 = transitions.get(new StateSymbolPair(s, c));
                if (s2==null)
                    throw new AutomatonNotWellDefinedException("transition function is not total");
                if (!states.contains(s2))
                    throw new AutomatonNotWellDefinedException("there is a transition to a state that is not in state set");
            }
        for (StateSymbolPair sp : transitions.keySet()) {
            if (!states.contains(sp.state))
                throw new AutomatonNotWellDefinedException("transitions refer to a state not in the state set");
            if (!alphabet.symbols.contains(sp.symbol))
                throw new AutomatonNotWellDefinedException("non-alphabet symbol appears in transitions");
        }
        return this;
    }
    
    /**
     * Constructs an uninitialized FA. 
     * <tt>states</tt> and <tt>accept</tt> are set to empty sets,
     * <tt>transitions</tt> is set to an empty map.
     */
    public FA() {
        states = new HashSet<State>();
        accept = new HashSet<State>();
        transitions = new HashMap<StateSymbolPair,State>();
    }
    
    /** 
     * Constructs a new FA consisting of one reject state. 
     * @param a automaton alphabet
     */
    public FA(Alphabet a) {
        states = new HashSet<State>();
        accept = new HashSet<State>();
        alphabet = a;
        
        // make a state
        State s = new State();
        states.add(s);
        initial = s;
        
        // add a loop transition for each alphabet symbol
        transitions = new HashMap<StateSymbolPair,State>();
        for (char c : alphabet.symbols) 
            transitions.put(new StateSymbolPair(s, c), s);
    }
    
    /** Clones this automaton. */
    @Override
    public Object clone() {
        FA f;
        try {
            f = (FA) super.clone(); // always clone using super.clone()
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        f.states = new HashSet<State>();
        f.accept = new HashSet<State>();
        f.transitions = new HashMap<StateSymbolPair,State>();
        Map<State,State> m = new HashMap<State,State>(); // map from old states to new states
        for (State p : states) {
            State s = (State)p.clone();
            f.states.add(s);
            m.put(p, s);
            if (accept.contains(p))
                f.accept.add(s);
        }
        f.initial = m.get(initial);
        for (Map.Entry<StateSymbolPair,State> e : transitions.entrySet()) {
            StateSymbolPair ssp = e.getKey();
            State q = e.getValue();
            f.transitions.put(new StateSymbolPair(m.get(ssp.state), ssp.symbol), m.get(q));
        }
        return f;
    }
    
    /** 
     * Returns <a href="http://www.research.att.com/sw/tools/graphviz/" target="_top">Graphviz Dot</a> 
     * representation of this automaton. 
     * (To convert a dot file to postscript, run '<tt>dot -Tps -o file.ps file.dot</tt>'.)
     */
    public String toDot() {
        StringBuffer b = new StringBuffer("digraph Automaton {\n");
        b.append("  rankdir = LR;\n");
        Map<State,Integer> id = new HashMap<State,Integer>();
        for (State s : states) 
            id.put(s, Integer.valueOf(id.size()));
        for (State s : states) {
            b.append("  ").append(id.get(s));
            if (accept.contains(s))
                b.append(" [shape=doublecircle,label=\""+s.name+"\"];\n");
            else
                b.append(" [shape=circle,label=\""+s.name+"\"];\n");
            if (s==initial) {
                b.append("  in [shape=plaintext,label=\"\"];\n");
                b.append("  in -> ").append(id.get(s)).append(";\n");
            }
        }
        for (Map.Entry<StateSymbolPair,State> e : transitions.entrySet()) {
            StateSymbolPair ssp = e.getKey();
            State q = e.getValue();
            b.append("  ").append(id.get(ssp.state)).append(" -> ").append(id.get(q));
            b.append(" [label=\"");
            char c = ssp.symbol.charValue();
            if (c>=0x21 && c<=0x7e && c!='\\' && c!='%')
                b.append(c);
            else {
                b.append("\\u");
                String s = Integer.toHexString(c);
                if (c<0x10)
                    b.append("000").append(s);
                else if (c<0x100)
                    b.append("00").append(s);
                else if (c<0x1000)
                    b.append("0").append(s);
                else
                    b.append(s);
            }
            b.append("\"];\n");
        }
        return b.append("}\n").toString();
    }
    
    /** Returns number of states in this automaton. */
    public int getNumberOfStates() {
        return states.size();
    }
    
    /** 
     * Sets a transition in the transition function. 
     * <img src="http://www.brics.dk/~amoeller/dRegAut/delta.gif" alt="delta">(q,c)=p
     */
    public void setTransition(State q, char c, State p) {
        transitions.put(new StateSymbolPair(q, c), p);
    }
    
    /**
     * Looks up transition in transition function.
     * @return <img src="http://www.brics.dk/~amoeller/dRegAut/delta.gif" alt="delta">(q,c) 
     * @exception IllegalArgumentException if <tt>c</tt> is not in the alphabet
     */
    public State delta(State q, char c) throws IllegalArgumentException {
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /**
     * Performs transitions in extended transition function. [Martin, Def. 3.3]
     * @return <img src="http://www.brics.dk/~amoeller/dRegAut/delta.gif" alt="delta">*(q,s) 
     * @exception IllegalArgumentException if a symbol in <tt>s</tt> is not in the alphabet
     */
    public State deltaStar(State q, String s) throws IllegalArgumentException {
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /**
     * Runs the given string on this automaton. [Martin, Def. 3.4]
     * @param s a string
     * @return true iff the string is accepted
     * @exception IllegalArgumentException if a symbol in <tt>s</tt> is not in the alphabet
     */
    public boolean accepts(String s) throws IllegalArgumentException {
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /** Pair of states. Used in product construction and in construction of regular expression. */
    static class StatePair {
        
        State s1, s2;
        
        /** Constructs a new pair. */
        StatePair(State s1, State s2) {
            this.s1 = s1;
            this.s2 = s2;
        }
        
        /** Checks whether two pairs are equal. */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof StatePair))
                return false;
            StatePair ss = (StatePair) obj;
            return s1==ss.s1 && s2==ss.s2;
        }
        
        /** Computes hash code for this object. */
        @Override
        public int hashCode() {
            return s1.hashCode()*3 + s2.hashCode()*2;
        }
    }
    
    /** Converts this automaton into an equivalent {@link RegExp} regular expression. [Martin, Th. 4.5] */
    public RegExp toRegExp() {
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /** 
     * Constructs a new automaton that accepts the complement of the language of this automaton. [Martin, p. 110] 
     * The input automaton is unmodified.
     */
    public FA complement() {
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /** Finds the set of states that are reachable from the initial state. */
    public Set<State> findReachableStates() { 
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /** 
     * Constructs a new automaton with the same language as this automaton but without unreachable states. [Martin, Exercise 3.29] 
     * The input automaton is unmodified.
     */
    public FA removeUnreachableStates() {
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /** 
     * Constructs a new minimal automaton with the same language as this automaton. [Martin, Sec. 5.2] 
     * The input automaton is unmodified.
     * Note: this textbook algorithm is simple to understand but not very efficient 
     * compared to other existing algorithms.
     */
    public FA minimize() { 
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /** Checks whether the language of this automaton is finite. [Martin, Sec. 5.4] */
    public boolean isFinite() { 
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /** Checks whether the language of this automaton is empty. [Martin, Sec. 5.4] */
    public boolean isEmpty() {
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /** Checks whether the language of this automaton is a subset of the language of the given automaton. [Martin, Sec. 5.4] */
    public boolean subsetOf(FA f) {
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /** 
     * Computes hash code for this object. 
     * (When {@link #equals(Object)} is implemented, <tt>hashCode</tt> must also be there.)
     */
    @Override
    public int hashCode() {
        return getNumberOfStates(); // a very simple but valid hash code
    }
    
    /** Checks whether the language of this automaton is equal to the language of the given automaton. [Martin, Sec. 5.4] */
    @Override
    public boolean equals(Object obj) {
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /** 
     * Returns a shortest string that is accepted by this automaton. 
     * @return a (not necessarily unique) shortest example string, null if the language of this automaton is empty
     */
    public String getAShortestExample() { 
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /**
     * Constructs a new automaton whose language is the intersection of the language of this automaton
     * and the language of the given automaton. [Martin, Th. 3.4]
     * The input automata are unmodified.
     * @exception IllegalArgumentException if the alphabets of <tt>f</tt> and this automaton are not the same
     */
    public FA intersection(FA f) throws IllegalArgumentException {
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /**
     * Constructs a new automaton whose language is the union of the language of this automaton
     * and the language of the given automaton. [Martin, Th. 3.4]
     * The input automata are unmodified.
     * @exception IllegalArgumentException if the alphabets of <tt>f</tt> and this automaton are not the same
     * @see NFALambda#union
     */
    public FA union(FA f) throws IllegalArgumentException {
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /**
     * Constructs a new automaton whose language is equal to the language of this automaton
     * minus the language of the given automaton. [Martin, Th. 3.4]
     * The input automata are unmodified.
     * @exception IllegalArgumentException if the alphabets of <tt>f</tt> and this automaton are not the same
     */
    public FA minus(FA f) throws IllegalArgumentException {
        throw new UnsupportedOperationException("method not implemented yet!");
    }
}
