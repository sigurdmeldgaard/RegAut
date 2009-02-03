package dRegAut;

/**
 * Regular expression. [Martin, Def. 3.1] 
 * <p>
 * We use the following special notation for regular expressions:
 * <ul>
 * <li><tt>#</tt>: the empty language (usually written as &Oslash;)
 * <li><tt>%</tt>: the empty string (usually written as <img 
 *    src="http://www.brics.dk/~amoeller/dRegAut/Lambda.gif" alt="Lambda">)
 * <li><tt>a</tt> (where <tt>a</tt><img src="http://www.brics.dk/~amoeller/dRegAut/in.gif" 
 *    alt="in"><img src="http://www.brics.dk/~amoeller/dRegAut/Sigma.gif" alt="sigma">):
 *    the singleton string <tt>a</tt>
 * <li><i>r</i><sub>1</sub><tt>+</tt><i>r</i><sub>2</sub>: union
 * <li><i>r</i><sub>1</sub><i>r</i><sub>2</sub>: concatenation
 * <li><i>r</i><tt>*</tt>: Kleene star
 * </ul>
 * Parentheses are used as described in [Martin, p. 86-87].
 */
public class RegExp {
    
    /** Root of abstract syntax tree of parsed regular expression. */
    Node ast;
    
    /** Alphabet. */
    Alphabet alphabet;
    
    /** 
     * Constructs a regular expression from a string. [Martin, Def. 3.1] 
     * @param string string representation of regular expression
     * @param alphabet the alphabet
     * @exception IllegalArgumentException if an alphabet symbol in the string is not in the alphabet
     *            or the string is not a syntactically correct regular expression
     */
    public RegExp(String string, Alphabet alphabet) throws IllegalArgumentException {
        this.alphabet = alphabet;
        Parser p = new Parser(string);
        ast = p.parseUnionExp(alphabet);
        if (p.more())
            throw new IllegalArgumentException("syntax error at position " + p.pos);
    }
    
    /** Constructs a regular expression from an abstract syntax tree. */
    RegExp(Node ast, Alphabet alphabet) {
        this.ast = ast;
        this.alphabet = alphabet;
    }
    
    /** Simplifies this regular expression (using some very simple rewrite rules). */
    public void simplify() {
        ast = ast.simplify();
    }
    
    /** 
     * Returns a string representation of this regular expression. 
     * The resulting string may contain superfluous parentheses. 
     */
    @Override
    public String toString() {
        StringBuffer b = new StringBuffer();
        ast.append(b);
        return b.toString();
    }
    
    /** Converts this regular expression to an {@link NFALambda}. [Martin, Th. 4.4] */
    public NFALambda toNFALambda() {
        return ast.toNFALambda(alphabet);
    }
    
    /**
     * Regular expression parser. 
     * This is a fairly standard recursive descent parser.
     */
    static class Parser {
        /** The string being parsed. */
        StringBuffer b;
        
        /** Current position in the string. */
        int pos;
        
        /** Constructs a new parser. */
        Parser(String string) {
            b = new StringBuffer(string);
            pos = 0;
        }
        
        /** Checks that we haven't reached the end of the string. */
        boolean more() {
            return pos<b.length();
        }
        
        /** Checks that the next char is among those in <tt>s</tt>. */
        boolean peek(String s) {
            return more() && s.indexOf(b.charAt(pos))!=-1;
        }
        
        /** Checks that the next char is the same as <tt>c</tt>, and then advance the position. */
        boolean match(char c) {
            if (pos>=b.length())
                return false;
            if (b.charAt(pos)==c) {
                pos++;
                return true;
            }
            return false;
        }
        
        /** Advances the position if not at the end of the string. */
        char next() throws IllegalArgumentException {
            if (!more())
                throw new IllegalArgumentException("unexpected end-of-string");
            return b.charAt(pos++);
        }
        
        /** Attempts to parse a union expression. */	
        Node parseUnionExp(Alphabet a) throws IllegalArgumentException {
            Node n = parseConcatExp(a);
            if (match('+'))
                n = new UnionNode(n, parseUnionExp(a));
            return n;
        }
        
        /** Attempts to parse a concat expression. */
        Node parseConcatExp(Alphabet a) throws IllegalArgumentException {
            Node n = parseStarExp(a);
            if (more() && !peek(")+")) 
                n = new ConcatNode(n, parseConcatExp(a));
            return n;
        }
        
        /** Attempts to parse a star expression. */
        Node parseStarExp(Alphabet a) throws IllegalArgumentException {
            Node n = parseSimpleExp(a);
            while (match('*'))
                n = new StarNode(n);
            return n;
        }
        
        /** Attempts to parse a simple expression, i.e., either an atomic expression or a parenthesis expression. */
        Node parseSimpleExp(Alphabet a) throws IllegalArgumentException {
            if (match('#'))
                return new EmptyLanguageNode();
            else if (match('%'))
                return new EmptyStringNode();
            else if (match('(')) {
                Node n = parseUnionExp(a);
                if (!match(')'))
                    throw new IllegalArgumentException("expected ')' at position " + pos);
                return n;
            } else {
                char c = next();
                if (c=='#' || c=='%' || c=='+' || c=='*' || c=='(' || c==')')
                    throw new IllegalArgumentException("syntax error at position " + pos);
                if (!a.symbols.contains(Character.valueOf(c)))
                    throw new IllegalArgumentException("symbol '"+c+"' at position "+pos+" not in alphabet");
                return new SymbolNode(c);
            }
        }
    }
    
    /** Abstract super class for parse tree nodes. */
    abstract static class Node {
        
        /** Appends string representation to the given <tt>StringBuffer</tt>. */
        abstract void append(StringBuffer b);
        
        /**
         * Constructs a new {@link NFALambda} automaton whose language is same
         * as the language of this regular expression. [Martin, Th. 4.4]
         */
        abstract NFALambda toNFALambda(Alphabet a);
        
        /** Simplifies this regular expression. */
        Node simplify() {
            return this;
        }
    }
    
    /** Parse tree node representing '<tt>#</tt>'. */
    static class EmptyLanguageNode extends Node {
        
        @Override 
        void append(StringBuffer b) {
            b.append('#');
        }
        
        @Override 
        NFALambda toNFALambda(Alphabet a) {
            return NFALambda.makeEmptyLanguage(a).checkWellDefined();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof EmptyLanguageNode))
                return false;
            return true;
        }
        
        @Override
        public int hashCode() {
            return 1;
        }
    }
    
    /** Parse tree node representing '<tt>%</tt>'. */
    static class EmptyStringNode extends Node {
        
        @Override 
        void append(StringBuffer b) {
            b.append('%');
        }
        
        @Override 
        NFALambda toNFALambda(Alphabet a) {
            return NFALambda.makeEmptyString(a).checkWellDefined();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof EmptyStringNode))
                return false;
            return true;
        }
        
        @Override
        public int hashCode() {
            return 2;
        }
    }
    
    /**
     * Parse tree node representing <tt>a</tt><img src="http://www.brics.dk/~amoeller/dRegAut/in.gif" 
     *   alt="in"><img src="http://www.brics.dk/~amoeller/dRegAut/Sigma.gif" alt="sigma">. 
     */
    static class SymbolNode extends Node {
        
        char c;
        
        SymbolNode(char c) {
            this.c = c;
        }
        
        @Override 
        void append(StringBuffer b) {
            b.append(c);
        }
        
        @Override 
        NFALambda toNFALambda(Alphabet a) {
            return NFALambda.makeSingleton(a, c).checkWellDefined();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SymbolNode))
                return false;
            SymbolNode t = (SymbolNode) obj;
            return c==t.c;
        }
        
        @Override
        public int hashCode() {
            return c*3;
        }
    }
    
    /** Parse tree node representing <i>r</i><sub>1</sub><tt>+</tt><i>r</i><sub>2</sub>. */
    static class UnionNode extends Node {
        Node n1, n2;
        
        UnionNode(Node n1, Node n2) {
            this.n1 = n1;
            this.n2 = n2;
        }
        
        @Override 
        void append(StringBuffer b) {
            b.append('(');
            n1.append(b);
            b.append('+');
            n2.append(b);
            b.append(')');
        }
        
        @Override 
        NFALambda toNFALambda(Alphabet a) {
            return n1.toNFALambda(a).union(n2.toNFALambda(a)).checkWellDefined();
        }
        
        @Override 
        Node simplify() {
            n1 = n1.simplify();
            n2 = n2.simplify();
            if (n1 instanceof EmptyLanguageNode)
                return n2;
            if (n2 instanceof EmptyLanguageNode)
                return n1;
            if (n1.equals(n2))
                return n1;
            return this;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof UnionNode))
                return false;
            UnionNode t = (UnionNode) obj;
            return n1.equals(t.n1) && n2.equals(t.n2);
        }
        
        @Override
        public int hashCode() {
            return n1.hashCode()*5+n2.hashCode()*7+3;
        }
    }
    
    /** Parse tree node representing <i>r</i><sub>1</sub><i>r</i><sub>2</sub>. */
    static class ConcatNode extends Node {
        Node n1, n2;
        
        ConcatNode(Node n1, Node n2) {
            this.n1 = n1;
            this.n2 = n2;
        }
        
        @Override 
        void append(StringBuffer b) {
            b.append('(');
            n1.append(b);
            n2.append(b);
            b.append(')');
        }
        
        @Override 
        NFALambda toNFALambda(Alphabet a) {
            return n1.toNFALambda(a).concat(n2.toNFALambda(a)).checkWellDefined();
        }
        
        @Override 
        Node simplify() {
            n1 = n1.simplify();
            n2 = n2.simplify();
            if (n1 instanceof EmptyStringNode)
                return n2;
            if (n2 instanceof EmptyStringNode)
                return n1;
            if (n1 instanceof EmptyLanguageNode)
                return n1;
            if (n2 instanceof EmptyLanguageNode)
                return n2;
            return this;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ConcatNode))
                return false;
            ConcatNode t = (ConcatNode) obj;
            return n1.equals(t.n1) && n2.equals(t.n2);
        }
        
        @Override
        public int hashCode() {
            return n1.hashCode()*5+n2.hashCode()*7+4;
        }
    }
    
    /** Parse tree node representing <i>r</i><tt>*</tt>. */
    static class StarNode extends Node {
        
        Node n;
        
        StarNode(Node n) {
            this.n = n;
        }
        
        @Override 
        void append(StringBuffer b) {
            b.append('(');
            n.append(b);
            b.append('*');
            b.append(')');
        }
        
        @Override 
        NFALambda toNFALambda(Alphabet a) {
            return n.toNFALambda(a).kleene().checkWellDefined();
        }
        
        @Override 
        Node simplify() {
            n = n.simplify();
            if (n instanceof EmptyStringNode || n instanceof EmptyLanguageNode)
                return n;
            return this;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof StarNode))
                return false;
            StarNode t = (StarNode) obj;
            return n.equals(t.n);
        }
        
        @Override
        public int hashCode() {
            return n.hashCode()*5+4;
        }
    }
}
