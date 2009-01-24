/*
 Examples:
 (1+2)(0+1+2+3)*
 (0+1+2+3)(0+1+2+3)(1+2)(0+1+2+3)*
 (1+2)*0(0+1+2+3)*
 (1+2+3)*0(1+2)*0(0+1+2+3)*
 (2(1+2)*2+1(1+2)*1+3(0+3)*3+0(0+3)*0)(0+1+2+3)*
 (1+2)*(0+3)*
 (1+2)*(0+3+1)*
 */

import dRegAut.*;
import java.awt.*;
import javax.swing.*;
import java.util.*;

public class RegularImage extends JComponent {
    private FA m;
    private int resolution;
    
    public static void main(String[] args) {
        if (args.length!=2) {
            System.out.println("Usage: java -classpath dRegAut.jar:. RegularImage <regexp> <resolution>\n");
            System.out.println("where <regexp> is a regular expression over the alphabet {0,1,2,3}");
            System.out.println("and <resolution> is an integer from 1 to 9 representing the image resolution");
            System.exit(-1);
        }
        String regexp = args[0];
        int resolution = Integer.parseInt(args[1]);
        
        Alphabet a = new Alphabet("0123");
        RegExp r = new RegExp(regexp, a);
        FA m = r.toNFALambda().removeLambdas().determinize().minimize();
        System.out.println("The FA has "+m.getNumberOfStates()+" states.");

	JFrame f = new JFrame("RegularImage");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLocation(100, 100);
        f.setSize(512, 512);
        f.setTitle("dRegAut");
        f.setBackground(Color.white);
	f.getContentPane().add(new RegularImage(m, resolution));
	f.pack();
	f.setVisible(true);
    }
    
    public RegularImage(FA m, int resolution) {
        this.m = m;
        this.resolution = resolution;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        System.out.print("Drawing...");
        g.setColor(Color.black);
        LinkedList<State> pending = new LinkedList<State>();
        LinkedList<String> paths = new LinkedList<String>();
        pending.add(m.initial);
        paths.add("");
        int n = 0;
        while (!pending.isEmpty()) {
            State q = pending.removeFirst();
            String path = paths.removeFirst();
            boolean b = true;
            if (!m.accept.contains(q)) // only consider accept states
                b = false;
            else {
                if (path.length()<resolution) { // draw big pixel if state is sigma-star state
                    for (char c : m.alphabet.symbols) {
                        State p = m.delta(q, c);
                        if (p!=q) {
                            b = false;
                            break;
                        }
                    }
                }
            }
            if (b) {		
                Point p = str2point(path);
                int z = 512 >> path.length();
                g.fillRect(p.x*z, p.y*z, z, z);
                n++;
            } else if (path.length()<resolution) {
                for (char c : m.alphabet.symbols) {
                    State p = m.delta(q, c);
                    pending.add(p);
                    paths.add(path+c);
                }
            }
        }
        System.out.println(" done, "+n+" black pixels drawn.");
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(512, 512);
    }
  
    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    Point str2point(String s) {
        int x = 0;
        int y = 0;
        int increment = 1;
        for (int i=s.length()-1; i>=0; i--) {
            switch (s.charAt(i)) {
            case '2': 
                x = x+increment; 
                y = y+increment; 
                break;
            case '3': 
                x = x+increment; 
                break;
            case '0': 
                y = y+increment; 
                break;
            case '1': 
                break;
            }
            increment = increment*2;
        }
        return new Point(x, y);
    }
}
