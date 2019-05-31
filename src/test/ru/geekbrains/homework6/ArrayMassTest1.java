package ru.geekbrains.homework6;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ArrayMassTest1 {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {new int[]{1, 2, 4, 4, 2, 3, 4, 1, 7},
                        new int[]{1, 7}, null},
                {new int[]{32, 34, 4, 2, 90, 87, 125},
                        new int[]{2, 90, 87, 125}, null},
                {new int[]{45, 666, 916, 22, 39, 124, 21, 10},
                        new int[]{}, RuntimeException.class},
                {new int[]{4, 4, 7, 0, 30, 5, 2019, 5, 66, 88, 94, 64},
                        new int[]{7, 0, 30, 5, 2019, 5, 66, 88, 94, 64}, null}
        });
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private int[] arrIn;
    private int[] arrOut;
    public Class<? extends Exception> expectedException;

    public ArrayMassTest1(int[] arrIn, int[] arrOut, Class<? extends Exception> expectedException) {
        this.arrIn = arrIn;
        this.arrOut = arrOut;
        this.expectedException = expectedException;
    }

    private ArrayAction arrayAction;

    @Before
    public void init() {
        arrayAction = new ArrayAction();
    }

    @Test
    public void test() {
        if(expectedException != null) {
            thrown.expect(expectedException);
        }
        Assert.assertArrayEquals(arrOut, arrayAction.arrayProcessing(arrIn));
    }

}
