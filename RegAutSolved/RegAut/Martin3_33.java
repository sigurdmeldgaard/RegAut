package RegAut;

public class Martin3_33 {
    public static void main(String [] arg){
	Alphabet sigma = new Alphabet("01");
	FA l2 = new FA();
	l2.alphabet = sigma;
	State x = new State("X");
	State y = new State("Y");
	State z = new State("Z");
	l2.states.add(x);
	l2.states.add(y);
	l2.states.add(z);
	l2.accept.add(z);
	l2.initial = x;
	l2.transitions.put(new StateSymbolPair(x, '0'), x);
	l2.transitions.put(new StateSymbolPair(x, '1'), y);
	l2.transitions.put(new StateSymbolPair(y, '0'), x);
	l2.transitions.put(new StateSymbolPair(y, '1'), z);
	l2.transitions.put(new StateSymbolPair(z, '0'), z);
	l2.transitions.put(new StateSymbolPair(z, '1'), z);
	FA l3 = new FA();
	l3.alphabet = sigma;
	State r = new State("R");
	State s = new State("S");
	l3.initial = r;
	l3.states.add(r);
	l3.states.add(s);
	l3.accept.add(s);
	l3.transitions.put(new StateSymbolPair(r, '0'), s);
	l3.transitions.put(new StateSymbolPair(r, '1'), s);
	l3.transitions.put(new StateSymbolPair(s, '0'), r);
	l3.transitions.put(new StateSymbolPair(s, '1'), r);
	FA difference = l3.minus(l2);
	System.out.println(difference.toDot());
	
    }
}
    