package util;

import org.junit.*;
import org.junit.jupiter.api.Assertions;

public class UtilTests {
    @Before
    public void init() throws ClassNotFoundException {
    }

    @Test
    public void testNDArrayInsert() {
        NDArray arr = new NDArray(2,2);
        arr.add(3.0, 0, 0);
        Assert.assertTrue(true);
    }

    @Test
    public void testNDArrayFetch() {
        NDArray arr = new NDArray(2,2,2);
        arr.add(3.0, 1, 0, 1);
        Assert.assertTrue(arr.get(1, 0, 1).equals(3.0));
    }

    @Test
    public void testLoopingUseCase() {
        NDArray arr = new NDArray(3, 3, 3);
        int num = 1;

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                for(int k = 0; k < 3; k++) {
                    arr.add(num, i, j, k);
                    num++;
                }
            }
        }

        num = 1;

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                for(int k = 0; k < 3; k++) {
                    Assert.assertTrue(arr.get(i, j, k).equals(num));
                    num++;
                }
            }
        }
    }

    @Test
    public void testLength() {
        NDArray arr = new NDArray(3, 4, 5);
        NDArray arrInner = (NDArray) arr.get(0);
        NDArray arrInnerInner = (NDArray) arrInner.get(0);

        Assertions.assertAll(
            () -> Assert.assertEquals(arr.length(), 3),
            () -> Assert.assertEquals(arrInner.length(), 4),
            () -> Assert.assertEquals(arrInnerInner.length(), 5)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void createZeroDimensionalArray() {
        NDArray arr = new NDArray();
    }

    @Test
    public void testNDArrayEquals() {
        NDArray arr1 = new NDArray(2, 6);
        double num = 1;
        for(int i = 0; i < 2; i++) {
            for(int j = 0; j < 6; j++) {
                arr1.add(num, i, j);
                num = num + 1;
            }
        }

        NDArray arr2 = NDArray.readCSV("test.csv");

        Assert.assertTrue(arr1.equals(arr2));
    }

    @Test
    public void testReadCSV() {
        NDArray arr = NDArray.readCSV("test.csv");
        double num = 1;
        for(int i = 0; i < 2; i++) {
            for(int j = 0; j < 6; j++) {
                Assert.assertTrue(arr.get(i,j).equals(num));
                num = num + 1;
            }
        }
    }
}
