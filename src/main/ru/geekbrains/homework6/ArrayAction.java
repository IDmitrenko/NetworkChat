package ru.geekbrains.homework6;

public class ArrayAction {

    private static int number1 = 4;
    private static int number2 = 1;

    public int[] arrayProcessing(int[] arr) throws RuntimeException {
        int origin = -1;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == number1)
                origin = i;
        }

        if (origin >= 0) {
            int[] rez = new int[arr.length - origin - 1];
            System.arraycopy(arr,origin + 1, rez, 0, arr.length - origin - 1);
            return rez;
        } else {
            throw new RuntimeException();
        }
    }

    public boolean compositionCheck(int[] arr) {
        boolean rez1 = false;
        boolean rez2 = false;
        for (int i = 0; i < arr.length; i++) {
            if (rez1 && rez2) {
                return true;
            } else if (arr[i] == number1) {
                rez1 = true;
            } else if (arr[i] == number2) {
                rez2 = true;
            }
        }
        return false;
    }

}
