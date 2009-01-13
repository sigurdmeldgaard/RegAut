package RegAut;

import java.util.*;

/**
 * Representation of an alphabet. 
 * <p>
 * An alphabet is represented by a non-empty set of {@link Character} objects.
 * For simplificy, alphabets should not contain the characters 
 * '<tt>#</tt>', '<tt>%</tt>', '<tt>+</tt>', '<tt>*</tt>', '<tt>(</tt>', or '<tt>)</tt>',
 * which have special meaning in {@link RegExp} regular expresions. 
 * (The character &#92;uFFFF, which we reserve for later use, is also forbidden.)
 */
public class Alphabet {
    
    /** 
     * Set of {@link Character} objects. 
     * The set should not be changed when the alphabet is being used in instances
     * of {@link RegExp}, {@link FA}, {@link NFA}, or {@link NFALambda}.
     */
    public Set<Character> symbols;
    
    /**
     * Constructs an alphabet consisting of the given symbols. 
     * @param symbols non-empty string of symbols
     * @exception IllegalArgumentException if <tt>symbols</tt> is empty
     */
    public Alphabet(String symbols) throws IllegalArgumentException {
        if (symbols.length()==0)
            throw new IllegalArgumentException("an alphabet cannot be empty");
        this.symbols = new HashSet<Character>();
        for (char c : symbols.toCharArray())
            this.symbols.add(c);
        checkSpecialChars();
    }
    
    /**
     * Constructs an alphabet consisting of the symbols in the given Unicode character interval. 
     * @param min begin character
     * @param max end character (included)
     * @exception IllegalArgumentException if <tt>min</tt>&gt;<tt>max</tt>
     */
    public Alphabet(char min, char max) throws IllegalArgumentException {
        if (min>max)
            throw new IllegalArgumentException("an alphabet cannot be empty");
        this.symbols = new HashSet<Character>();
        for (char c = min; c<=max; c++)
            this.symbols.add(c);
        checkSpecialChars();
    }
    
    /** 
     * Checks that special characters do not occur in the alphabet. 
     * This method is used by the constructors.
     */
    private void checkSpecialChars() throws IllegalArgumentException {
        if (symbols.contains('#') ||
            symbols.contains('%') ||
            symbols.contains('+') ||
            symbols.contains('*') ||
            symbols.contains('(') ||
            symbols.contains(')') ||
            symbols.contains(NFALambda.LAMBDA))
            throw new IllegalArgumentException("#, %, +, *, (, ), and \\uFFFF are not allowed in alphabets");
    }
    
    /** Returns number of symbols in this alphabet. */
    public int getNumberOfSymbols() {
        return symbols.size();
    }
    
    /** 
     * Computes hash code for this object. 
     * (When {@link #equals(Object)} is implemented, <tt>hashCode</tt> must also be there.)
     */
    @Override
    public int hashCode() {
        return symbols.hashCode(); 
    }
    
    /** Checks whether the given alphabet is the same as this one. */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Alphabet))
            return false;
        Alphabet a = (Alphabet) obj;
        return symbols.equals(a.symbols); 
    }
}
