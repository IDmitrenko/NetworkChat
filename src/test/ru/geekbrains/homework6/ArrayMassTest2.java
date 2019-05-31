package ru.geekbrains.homework6;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)

public class ArrayMassTest2 {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {new int[]{1, 2, 4, 4, 2, 3, 4, 1, 7},
                        true},
                {new int[]{32, 34, 4, 2, 90, 87, 125},
                        false},
                {new int[]{45, 666, 916, 22, 39, 124, 21, 10},
                        false},
                {new int[]{4, 4, 7, 0, 30, 5, 2019, 5, 66, 88, 94, 64},
                        false}
        });
    }

    private int[] arr;
    private boolean rez;

    public ArrayMassTest2(int[] arr, boolean rez) {
        this.arr = arr;
        this.rez = rez;
    }

    private ArrayAction arrayAction;

    @Before
    public void init() {
        arrayAction = new ArrayAction();
    }

    @Test
    public void test() {
        Assert.assertEquals(rez, arrayAction.compositionCheck(arr));
    }

}
