package com.rokid.alliancedemo;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
        A a = new A();
        B b = new B();

        if (b instanceof A) {
            System.out.println("b instance b return true");
        }

        if (b instanceof C) {

        }
    }


    private class A {

    }

    private class B extends A implements C{

    }

    private interface C {}

}