package de.dreierschach.vz200ui;

public class Sqrt {
    public static void main(String[] args) {
        for (int i = 0; i < 256; i++) {
            System.out.print("            defw ");
            double w = ((double) i) / 128 * 3.1415927;
            int v = (int) (Math.sin(w) * 255);
            if (v < 0) {
                v = 65536 + v;
            }
            System.out.print(String.format("0x%04X", v));
            System.out.println();
        }

//        for (int i = 0; i < 256; i++) {
//            if (i % 8 == 0) {
//                System.out.print("            defw ");
//            }
//            var v = Math.sqrt((double) i);
//            var v1 = (int) v;
//            var v2 = (int) ((v - v1) * 256);
//            System.out.print(String.format("0x%02X%02X", v1, v2));
//            if (i % 8 == 7) {
//                System.out.println();
//            } else {
//                System.out.print(", ");
//            }
//        }
    }
}
