package ShardSecurity;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.*;
import javax.swing.*;

public class SecretShare {
    public static void main(String[] args) throws FileNotFoundException {
        Scanner console = new Scanner(System.in);
        boolean done = false;
        while (!done) {
            System.out.println("Would you like to read or write shards?");
            String response = console.nextLine();
            if (response.toLowerCase().startsWith("r")) {
                readShards();
                done = true;
            } else if (response.toLowerCase().startsWith("w")) {
                writeShards();
                done = true;
            } else {
                System.out.println("Please respond with 'Read' or 'Write'");
                continue;
            }
        }
        console.close();
    }

    // Builds a random polynomial of power threshold - 1
    public static int poly(List<Integer> coef, int x) {
        int result = 0;
        for (int i = 0; i < coef.size(); i++) {
            result += coef.get(i) * Math.pow(x, i + 1);
        }
        return result;
    }

    // Reads a shard file and creates a Shard object
    public static Shard readShard(File sh) throws FileNotFoundException{
        if (!sh.exists()) {
            System.out.println("File not found");
            return null;
        } else {
            Scanner shardScan = new Scanner(sh);
            int ind = 0;
            int val = 0;
            String name = "";
            if (shardScan.hasNext()) {
                name = shardScan.next();
                ind = shardScan.nextInt();
                val = shardScan.nextInt();
            }
            shardScan.close();
            return new Shard(name, val, ind);
        }
    }

    // Writes a shard to a file called "name.txt"
    public static void writeShard(Shard sh) throws FileNotFoundException {
        PrintStream out = new PrintStream(sh.getName() + ".txt");
        String nam = sh.getName();
        char num = nam.charAt(nam.length() - 1);
        nam = nam.substring(0, 5);
        nam = nam + num;
        out.println(nam);
        out.println(sh.getIndex());
        out.println(sh.getValue());
        out.close();
    }

    // Read shards from txt files and calculates the hidden value
    public static void readShards() throws FileNotFoundException {
        Scanner console = new Scanner(System.in);
        List<Shard> shards = new ArrayList<Shard>();
        System.out.println("How many Shards are you using?");
        int quorem = console.nextInt();
        console.nextLine();
        for (int i = 0; i < quorem; i++) {
            System.out.println("What is the name of shard " + (i + 1) + "?");
            String f = console.nextLine();
            File sh = new File(f);
            if (!sh.exists()) {
                System.out.println("File not Found");
                i--;
            } else {
                Scanner fileScan = new Scanner(sh);
                Shard shard = new Shard(fileScan.nextLine(), fileScan.nextInt(), fileScan.nextInt());
                shards.add(shard);
                fileScan.close();
            }
        }
        System.out.println();
        System.out.println("You entered the following shards:");
        System.out.println("---------------------------------");
        for (int i = 0; i < shards.size(); i++) {
            System.out.println(shards.get(i));
        }
        System.out.println();
        System.out.println("Your hidden value is:");
        // Do regression
        if (quorem == 2) {
            // Linear
            System.out.println((int)linReg(shards));
        } else if (quorem == 3) {
            // Quadratic
            System.out.println((int)quadReg(shards));
        }
        console.close();
    }

    // Writes shards to txt files
    public static void writeShards() throws FileNotFoundException {
        Scanner console = new Scanner(System.in);
        System.out.println("What do you want to encrypt? ");
        int key = console.nextInt();
        System.out.println("Threshold? ");
        int threshold = console.nextInt();
        System.out.println("Total shards? ");
        int numShards = console.nextInt();
        // Generate coeffiecients
        List<Integer> coef = new ArrayList<Integer>();
        for (int i = 0; i < threshold - 1; i++) {
            coef.add(ThreadLocalRandom.current().nextInt(-20000, 20000 + 1));
        }
        // Build shards and add to list
        List<Shard> shards = new ArrayList<Shard>();
        for (int i = 1; i <= numShards; i++) {
            Shard sh = new Shard("Shard" + i, key + poly(coef, i), i);
            writeShard(sh);
            shards.add(sh);
        }
        System.out.println();
        System.out.println("Shards Generated.");
        System.out.println("-----------------");
        for (Shard sh : shards) {
            System.out.println(sh);
        }
        System.out.println();
        console.close();
    }


    // Calculates c from y = mx + c
    public static double linReg(List<Shard> list) { 
        int size = list.size();
        int x[] = new int[size];
        int y[] = new int[size];
        for (int i = 0; i < list.size(); i++) {
            y[i] = list.get(i).getIndex();
            x[i] = list.get(i).getValue();
        }
        double m = (y[0] - y[1]) / (x[0] - x[1]);
        int min = Math.min(x[0], x[1]);
        int ind = 0;
        if (x[1] < x[0]) {
            ind = 1;
        }
        double c = y[ind] - m * min;
        return c;
    } 

    // Calculates c for quadratic  aA + bB+ c = zZ
    public static double quadReg(List<Shard> list) {
        int size = list.size();
        int x[] = new int[size];
        int y[] = new int[size];
        for (int i = 0; i < list.size(); i++) {
            y[i] = list.get(i).getIndex();
            x[i] = list.get(i).getValue();
        }
        // Equation 1
        double a = Math.pow(x[0], 2) - Math.pow(x[1], 2);
        double b = x[0] - x[1];
        double z = y[0] - y[1];
        // Equation 2
        double a1 = Math.pow(x[2], 2) - Math.pow(x[1], 2);
        double b1= x[2] - x[1];
        double z1 = y[2] - y[1];

        b = -b / a;
        z = z / a;
        b1 += a1 * (b);
        z1 -= a1 * z;
        double bb = z1 / b1;
        // Equation 1 reset
        a = Math.pow(x[0], 2) - Math.pow(x[1], 2);
        b = x[0] - x[1];
        z = y[0] - y[1];
        // Equation 2 reset
        a1 = Math.pow(x[2], 2) - Math.pow(x[1], 2);
        b1= x[2] - x[1];
        z1 = y[2] - y[1];
        double aa = (z - b * bb) / a;
        return -1 * (aa * Math.pow(x[0], 2) + bb * x[0] - y[0]);
    }

    public static JFrame buildGui() {
        JFrame frame = new JFrame("Secret Share");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel test = new JLabel();
        test.setPreferredSize(new Dimension(600, 600));
        test.setText("This is a test");
        frame.getContentPane().add(test, BorderLayout.CENTER);

        frame.pack();
        frame.setVisible(true);

        return frame;
    }
}