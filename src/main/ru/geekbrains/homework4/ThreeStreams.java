package ru.geekbrains.homework4;

public class ThreeStreams {

        private static volatile char currentLetter = 'A';

        public static void main(String[] args) {
            new Thread(ThreeStreams::printA).start();
            new Thread(ThreeStreams::printB).start();
            new Thread(ThreeStreams::printC).start();
        }

        private synchronized static void printA() {
            for (int i = 0; i < 5; i++) {
                try {
                    while (currentLetter != 'A') {
                        ThreeStreams.class.wait();
                    }
                    System.out.printf("A");
                    currentLetter = 'B';
                    ThreeStreams.class.notifyAll();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        private synchronized static void printB() {
            for (int i = 0; i < 5; i++) {
                try {
                    while (currentLetter != 'B') {
                        ThreeStreams.class.wait();
                    }
                    System.out.printf("B");
                    currentLetter = 'C';
                    ThreeStreams.class.notifyAll();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

    private synchronized static void printC() {
        for (int i = 0; i < 5; i++) {
            try {
                while (currentLetter != 'C') {
                    ThreeStreams.class.wait();
                }
                System.out.println("C");
                currentLetter = 'A';
                ThreeStreams.class.notifyAll();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

}
