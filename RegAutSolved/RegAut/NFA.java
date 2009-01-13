package RegAut;

import java.util.*;

/**
 * Nondeterministic finite state automaton. [Martin, Def. 4.1]
 */
public class NFA implements Cloneable {
    
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
     * This is a map from pairs of states and alphabet symbols to sets of states
     * (<img src="http://www.brics.dk/~amoeller/dRegAut/delta.gif" alt="delta">: 
     * Q<img src="http://www.brics.dk/~amoeller/dRegAut/times.gif" 
     *      alt="x"><img src="http://www.brics.dk/~amoeller/dRegAut/Sigma.gif" alt="sigma">
     * <img src="http://www.brics.dk/~amoeller/dRegAut/rightarrow.gif" alt="-&gt;"> 2<sup>Q</sup>).
     */
    public Map<StateSymbolPair,Set<State>> transitions;
    
    /**
     * Checks that this automaton is well-defined.
     * This method should be invoked after each <tt>NFA</tt> operation during testing.
     * @return this automaton
     * @exception AutomatonNotWellDefinedException if this automaton is not well-defined
     */
    public NFA checkWellDefined() throws AutomatonNotWellDefinedException {
        if (states==null || alphabet==null || alphabet.symbols==null ||
            initial==null || accept==null || transitions==null)
            throw new AutomatonNotWellDefinedException("invalid null pointer");
        if (!states.contains(initial))
            throw new AutomatonNotWellDefinedException("the initial state is not in the state set");
        if (!states.containsAll(accept))
            throw new AutomatonNotWellDefinedException("not all accept states are in the state set");
        for (State s : states) {
            if (transitions.get(new StateSymbolPair(s, NFALambda.LAMBDA))!=null)
                throw new AutomatonNotWellDefinedException("lambda transition appears in transitions");
            for (char c : alphabet.symbols) {
                Set<State> ps = transitions.get(new StateSymbolPair(s, c));
                if (ps!=null)
                    for (State s2 : ps) 
                        if (!states.contains(s2))
                            throw new AutomatonNotWellDefinedException("there is a transition to a state that is not in state set");
            }
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
     * Constructs an uninitialized NFA. 
     * <tt>states</tt> and <tt>accept</tt> are set to empty sets,
     * <tt>transitions</tt> is set to an empty map.
     */
    public NFA() {
        states = new HashSet<State>();
        accept = new HashSet<State>();
        transitions = new HashMap<StateSymbolPair,Set<State>>();
    }
    
    /** 
     * Constructs a new NFA consisting of one reject state. 
     * @param a automaton alphabet
     */
    public NFA(Alphabet a) {
        states = new HashSet<State>();
        accept = new HashSet<State>();
        alphabet = a;
        State s = new State();
        states.add(s);
        initial = s;
        transitions = new HashMap<StateSymbolPair,Set<State>>();
    }
    
    /** Clones this automaton. */
    @Override
    public Object clone() {
        NFA f;
        try {
            f = (NFA) super.clone(); // always clone using super.clone()
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        f.states = new HashSet<State>();
        f.accept = new HashSet<State>();
        f.transitions = new HashMap<StateSymbolPair,Set<State>>();
        Map<State,State> m = new HashMap<State,State>(); // map from old states to new states
        for (State p : states) {
            State s = (State) p.clone();
            f.states.add(s);
            m.put(p, s);
            if (accept.contains(p))
                f.accept.add(s);
        }
        f.initial = m.get(initial);
        for (Map.Entry<StateSymbolPair,Set<State>> e : transitions.entrySet()) {
            StateSymbolPair ssp = e.getKey();
            Set<State> set = e.getValue();
            Set<State> ns = new HashSet<State>();
            for (State q : set)
                ns.add(m.get(q));
            f.transitions.put(new StateSymbolPair(m.get(ssp.state), ssp.symbol), ns);
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
        for (Map.Entry<StateSymbolPair,Set<State>> e : transitions.entrySet()) {
            StateSymbolPair ssp = e.getKey();
            Set<State> set = e.getValue();
            for (State q : set) {
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
        }
        return b.append("}\n").toString();
    }
    
    /** Returns number of states in this automaton. */
    public int getNumberOfStates() {
        return states.size();
    }
    
    /**
     * Looks up transition in transition function.
     * @return <img src="http://www.brics.dk/~amoeller/dRegAut/delta.gif" alt="delta">(q,c), 
     *         the result is never null and it should never be modified by the caller
     * @exception IllegalArgumentException if <tt>c</tt> is not in the alphabet
     */
    public Set<State> delta(State q, char c) throws IllegalArgumentException {
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /**
     * Performs transitions in extended transition function. [Martin, Def. 4.2]
     * @return <img src="http://www.brics.dk/~amoeller/dRegAut/delta.gif" alt="delta">*(q,s) 
     *         (the result is never null)
     * @exception IllegalArgumentException if a symbol in <tt>s</tt> is not in the alphabet
     */
    public Set<State> deltaStar(State q, String s) throws IllegalArgumentException {
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /**
     * Runs the given string on this automaton. [Martin, Def. 4.3]
     * @param s a string
     * @return true iff the string is accepted
     * @exception IllegalArgumentException if a symbol in <tt>s</tt> is not in the alphabet
     */
    public boolean accepts(String s) throws IllegalArgumentException {
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /** 
     * Converts this NFA into an equivalent {@link NFALambda}. 
     * Note: this is trivial since an NFA is just a special kind of NFALambda (see [Martin, Th. 4.3]).
     */
    public NFALambda toNFALambda() {
        NFALambda f = new NFALambda();
        f.alphabet = alphabet;
        Map<State,State> m = new HashMap<State,State>(); // map from old states to new states
        for (State p : states) {
            State s = (State) p.clone();
            f.states.add(s);
            m.put(p, s);
            if (accept.contains(p))
                f.accept.add(s);
            if (p==initial)
                f.initial = s;
        }
        for (Map.Entry<StateSymbolPair,Set<State>> e : transitions.entrySet()) {
            StateSymbolPair ssp = e.getKey();
            Set<State> set = e.getValue();
            Set<State> ns = new HashSet<State>();
            for (State q : set)
                ns.add(m.get(q));
            f.transitions.put(new StateSymbolPair(m.get(ssp.state), ssp.symbol), ns);
        }
        return f;
    }
    
    /** 
     * Determinizes this automaton by constructing an equivalent {@link FA}. [Martin, Th. 4.1] 
     * The input automaton is unmodified.
     * This implementation only constructs the reachable part of the subset state space.
     */
    public FA determinize() {
        throw new UnsupportedOperationException("method not implemented yet!");
    }
}
