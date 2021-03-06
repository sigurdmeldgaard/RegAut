package dRegAut;

import java.util.*;

/**
 * Nondeterministic finite state automaton with 
 * <img src="http://www.brics.dk/~amoeller/dRegAut/Lambda.gif" alt="Lambda"> transitions. 
 */
public class NFALambda implements Cloneable {
    
    /** Our representation of <img src="http://www.brics.dk/~amoeller/dRegAut/Lambda.gif" alt="Lambda">. */
    public static final char LAMBDA = Character.MAX_VALUE;
    
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
     *      alt="x">(<img src="http://www.brics.dk/~amoeller/dRegAut/Sigma.gif" 
     *      alt="sigma"><img src="http://www.brics.dk/~amoeller/dRegAut/union.gif" 
     *      alt="union">{<img src="http://www.brics.dk/~amoeller/dRegAut/Lambda.gif" alt="Lambda">})
     * <img src="http://www.brics.dk/~amoeller/dRegAut/rightarrow.gif" alt="-&gt;"> 2<sup>Q</sup>).
     * We represent <img src="http://www.brics.dk/~amoeller/dRegAut/Lambda.gif" alt="Lambda"> by {@link #LAMBDA}.
     */
    public Map<StateSymbolPair,Set<State>> transitions;
    
    /**
     * Checks that this automaton is well-defined.
     * This method should be invoked after each <tt>NFALambda</tt> operation during testing.
     * @return this automaton
     * @exception AutomatonNotWellDefinedException if this automaton is not well-defined
     */
    public NFALambda checkWellDefined() throws AutomatonNotWellDefinedException {
        if (states==null || alphabet==null || alphabet.symbols==null ||
            initial==null || accept==null || transitions==null)
            throw new AutomatonNotWellDefinedException("invalid null pointer");
        if (!states.contains(initial))
            throw new AutomatonNotWellDefinedException("the initial state is not in the state set");
        if (!states.containsAll(accept))
            throw new AutomatonNotWellDefinedException("not all accept states are in the state set");
        for (State s : states) {
            ArrayList<Character> extended_symbols = new ArrayList<Character>(alphabet.symbols);
            extended_symbols.add(LAMBDA);
            for (char c : extended_symbols) {
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
            if (sp.symbol!=LAMBDA && !alphabet.symbols.contains(sp.symbol))
                throw new AutomatonNotWellDefinedException("non-alphabet symbol appears in transitions");
        }
        return this;
    }
    
    /** 
     * Constructs an uninitialized NFALambda. 
     * <tt>states</tt> and <tt>accept</tt> are set to empty sets,
     * <tt>transitions</tt> is set to an empty map.
     */
    public NFALambda() {
        states = new HashSet<State>();
        accept = new HashSet<State>();
        transitions = new HashMap<StateSymbolPair,Set<State>>();
    }
    
    /** Clones this automaton. */
    @Override
    public Object clone() {
        NFALambda f;
        try {
            f = (NFALambda) super.clone(); // always clone using super.clone()
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
     * Constructs a new NFALambda that accepts the empty language. [Martin, Th. 4.4]
     * @param a automaton alphabet
     */
    public static NFALambda makeEmptyLanguage(Alphabet a) {
        NFALambda f = new NFALambda();
        f.alphabet = a;
        State s = new State();
        f.states.add(s);
        f.initial = s;
        return f;
    }
    
    /** 
     * Constructs a new NFALambda that accepts the language containing only the empty string. [Martin, Th. 4.4]
     * @param a automaton alphabet
     */
    public static NFALambda makeEmptyString(Alphabet a) {
        NFALambda f = new NFALambda();
        f.alphabet = a;
        State s = new State();
        f.states.add(s);
        f.initial = s;
        f.accept.add(s);
        return f;
    }

    
    /** 
     * Constructs a new NFALambda that accepts the language containing only the given singleton string. [Martin, Th. 4.4]
     * @param a automaton alphabet
     * @param c the symbol in the singleton string
     */
    public static NFALambda makeSingleton(Alphabet a, char c) {
        NFALambda f = new NFALambda();
        f.alphabet = a;
        State s = new State();
        State s1 = new State();
        f.states.add(s);
        f.states.add(s1);
        f.initial = s;
        Set<State> toSet = new HashSet<State>();
        toSet.add(s1);
	f.transitions.put(new StateSymbolPair(s, c), toSet);
        f.accept.add(s1);
        return f;
    }
    
    /** 
     * Returns <a href="http://www.research.att.com/sw/tools/graphviz/" target="_top">Graphviz Dot</a> 
     * representation of this automaton. 
     * (To convert a dot file to postscript, run '<tt>dot -Tps -o file.ps file.dot</tt>'.)
     * Lambdas are written as '<tt>%</tt>'.
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
                if (ssp.symbol==LAMBDA)
                    b.append('%');
                else {
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
     * Looks up transitions in transition function.
     * @return <img src="http://www.brics.dk/~amoeller/dRegAut/delta.gif" alt="delta">(q,c), 
     *         the result is never null and it should never be modified by the caller
     * @exception IllegalArgumentException if <tt>c</tt> is not in the alphabet
     */
    public Set<State> delta(State q, char c) throws IllegalArgumentException {
        if (!alphabet.symbols.contains(c) && c!=LAMBDA)
            throw new IllegalArgumentException("symbol '"+c+"' not in alphabet");
        Set<State> set = transitions.get(new StateSymbolPair(q, c));
        if (set==null)
            set = new HashSet<State>();
        return set;
    }

    
    /**
     * Computes the <img src="http://www.brics.dk/~amoeller/dRegAut/Lambda.gif" alt="Lambda">-closure
     * of the given state set. [Martin, Def. 4.6]
     * @param set set of {@link State} objects
     * @return set of {@link State} objects (the input set is unmodified)
     */
    public Set<State> lambdaClosure(Set<State> set) {
        Set<State> res = new HashSet<State>(); // contains the states that are in the closure and have been visited
        Set<State> pending = new HashSet<State>(); // contains the states that are in the closure but have not yet been visited
        pending.addAll(set);
        while (!pending.isEmpty()) {
            State q = pending.iterator().next();
            pending.remove(q);
            res.add(q);
            Set<State> s = new HashSet<State>(delta(q, LAMBDA));
            s.removeAll(res);
            pending.addAll(s);
        }
        return res;
    }

    /**
     * Performs transitions in extended transition function. [Martin, Def. 4.5]
     * @return <img src="http://www.brics.dk/~amoeller/dRegAut/delta.gif" alt="delta">*(q,s) 
     *         (the result is never null)
     * @exception IllegalArgumentException if a symbol in <tt>s</tt> is not in the alphabet
     */
    public Set<State> deltaStar(State q, String s) throws IllegalArgumentException {
        Set<State> set = new HashSet<State>();
        set.add(q);
        set = lambdaClosure(set);
        for (char c : s.toCharArray()) {
            Set<State> nset = new HashSet<State>();
            for (State p : set)
                nset.addAll(delta(p, c));
            set = lambdaClosure(nset);
        }
        return set;
    }

    
    /**
     * Runs the given string on this automaton. [Martin, Def. 4.5b]
     * @param s a string
     * @return true iff the string is accepted
     * @exception IllegalArgumentException if a symbol in <tt>s</tt> is not in the alphabet
     */
    public boolean accepts(String s) throws IllegalArgumentException {
        Set<State> set = deltaStar(initial, s);
        set.retainAll(accept);
        return !set.isEmpty();
    }

    
    /**
     * Adds a <img src="http://www.brics.dk/~amoeller/dRegAut/Lambda.gif" alt="Lambda"> transition.
     * @param p from state
     * @param q to state
     */
    public void addLambda(State p, State q) {
        StateSymbolPair ssp = new StateSymbolPair(p, LAMBDA);
        Set<State> s = transitions.get(ssp);
        if (s==null) {
            s = new HashSet<State>();
            transitions.put(ssp, s);
        }
        s.add(q);
    }
    
    /**
     * Constructs a new automaton whose language is the union of the language of this automaton
     * and the language of the given automaton.  [Martin, Th. 4.4]
     * The input automata are unmodified.
     * @exception IllegalArgumentException if the alphabets of <tt>f</tt> and this automaton are not the same
     */
    public NFALambda union(NFALambda f) throws IllegalArgumentException {
	if (!alphabet.equals(f.alphabet))
            throw new IllegalArgumentException("alphabets are different");
        NFALambda f1 = (NFALambda) this.clone();
        NFALambda f2 = (NFALambda) f.clone();
        NFALambda n = new NFALambda();
	State in = new State();
        n.alphabet = alphabet;
        n.states.addAll(f1.states);
        n.states.addAll(f2.states);
        n.accept.addAll(f1.accept);
        n.accept.addAll(f2.accept);
        n.initial = in;
        n.transitions.putAll(f1.transitions);
        n.transitions.putAll(f2.transitions);
	n.addLambda(in, f1.initial);
	n.addLambda(in, f2.initial);
        return n;
    }
    
    /**
     * Constructs a new automaton whose language is the concatenation of the language of this automaton
     * and the language of the given automaton.  [Martin, Th. 4.4]
     * The input automata are unmodified.
     * @exception IllegalArgumentException if the alphabets of <tt>f</tt> and this automaton are not the same
     */
    public NFALambda concat(NFALambda f) throws IllegalArgumentException {
        if (!alphabet.equals(f.alphabet))
            throw new IllegalArgumentException("alphabets are different");
        NFALambda f1 = (NFALambda) this.clone();
        NFALambda f2 = (NFALambda) f.clone();
        NFALambda n = new NFALambda();
        n.alphabet = alphabet;
        n.states.addAll(f1.states);
        n.states.addAll(f2.states);
        n.accept.addAll(f2.accept);
        n.initial = f1.initial;
        n.transitions.putAll(f1.transitions);
        n.transitions.putAll(f2.transitions);
        for (State s : f1.accept)
            n.addLambda(s, f2.initial);
        return n;
    }

    
    /**
     * Constructs a new automaton whose language is the Kleene star of the language of this automaton. [Martin, Th. 4.4]
     * The input automaton is unmodified.
     */
    public NFALambda kleene() {
        NFALambda n = (NFALambda) this.clone();
        for (State s : n.accept)
            n.addLambda(s, n.initial);
        return n;
    }
    
    /**
     * Constructs an equivalent {@link NFA} by reducing all
     * <img src="http://www.brics.dk/~amoeller/dRegAut/Lambda.gif" alt="Lambda"> transitions
     * to other transitions. [Martin, Th. 4.2]
     * The input automaton is unmodified.
     */
    public NFA removeLambdas() {
        NFA f = new NFA();
        f.alphabet = alphabet;
        Map<State,State> m = new HashMap<State,State>(); // map from old states to new states
        for (State p : states) {
            State s = (State) p.clone();
            f.states.add(s);
            m.put(p, s);
            if (accept.contains(p))
                f.accept.add(s);
        }
        f.initial = m.get(initial);
        Set<State> cl = new HashSet<State>();
        cl.add(initial);
        cl = lambdaClosure(cl);
        cl.retainAll(accept);
        if (!cl.isEmpty())
            f.accept.add(f.initial);
        for (State p : states) // this is quite naive but closely follows the mathematical definition in [Martin, Th. 4.2]
            for (char c : alphabet.symbols) {
                Set<State> set = deltaStar(p, ""+c);
                Set<State> nset = new HashSet<State>();
                for (State q : set) 
                    nset.add(m.get(q));
                f.transitions.put(new StateSymbolPair(m.get(p), c), nset);
            }
        return f;
    }

}
