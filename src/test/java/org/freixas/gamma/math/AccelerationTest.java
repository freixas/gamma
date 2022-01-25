/*
 * Copyright (C) 2021 Antonio Freixas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freixas.gamma.math;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Antonio Freixas
 */
public class AccelerationTest
{

    public AccelerationTest()
    {
    }

    @BeforeClass
    public static void setUpClass()
    {
    }

    @AfterClass
    public static void tearDownClass()
    {
    }

    @Before
    public void setUp()
    {
    }

    @After
    public void tearDown()
    {
    }

    @Test
    public void test_vToX()
    {
        System.out.println("test vToX()");
        for (double a = -5.0; a <= 5.0; a += .1) {
            if (Util.fuzzyZero(a)) {
                double x = Acceleration.vToX(0, 0);
                assertEquals(x, 0, Util.EPSILON);
            }
            else {
                for (double v = -.95; v < 1; v += .05) {
                    double x = Acceleration.vToX(a, v);
                    double v2 = Acceleration.xToV(a, x, a* v > 0);
                    assertEquals(v, v2, Util.EPSILON);
                }
            }
        }
    }

    @Test
    public void test_vToD()
    {
        System.out.println("test vToD()");
        for (double a = -5.0; a <= 5.0; a += .1) {
            if (Util.fuzzyZero(a)) {
                double d = Acceleration.vToD(0, 0);
                assertEquals(d, 0, Util.EPSILON);
            }
            else {
                for (double v = -.95; v < 1; v += .05) {
                    double d = Acceleration.vToD(a, v);
                    double v2 = Acceleration.dToV(a, d);
                    assertEquals(v, v2, Util.EPSILON);
                }
            }
        }
    }

    @Test
    public void test_vToT()
    {
        System.out.println("test vToT()");
        for (double a = -5.0; a <= 5.0; a += .1) {
            if (Util.fuzzyZero(a)) {
                try {
                    double t = Acceleration.vToT(0, 0);
                    fail("Should have thrown an ArithmeticException");
                }
                catch (ArithmeticException e) {
                }
            }
            else {
                for (double v = -.95; v < 1; v += .05) {
                    double t = Acceleration.vToT(a, v);
                    double v2 = Acceleration.tToV(a, t);
                    assertEquals(v, v2, Util.EPSILON);
                }
            }
        }
    }

    @Test
    public void test_vToTau()
    {
        System.out.println("test vToTau()");
        for (double a = -5.0; a <= 5.0; a += .1) {
            if (Util.fuzzyZero(a)) {
                try {
                    double t = Acceleration.vToT(0, 0);
                    fail("Should have thrown an ArithmeticException");
                }
                catch (ArithmeticException e) {
                }
            }
            else {
                for (double v = -.95; v < 1; v += .05) {
                    double tau = Acceleration.vToTau(a, v);
                    double v2 = Acceleration.tauToV(a, tau);
                    assertEquals(v, v2, Util.EPSILON);
                }
            }
        }
    }

    @Test
    public void test_dToV()
    {
        System.out.println("test dToV()");
        for (double a = -5.0; a <= 5.0; a += .1) {
            if (Util.fuzzyZero(a)) {
                double d = Acceleration.dToV(0, 0);
                assertEquals(d, 0, Util.EPSILON);
            }
            else {
                for (double d = -5; d <= 5; d += .1) {
                    double v = Acceleration.dToV(a, d);
                    double d2 = Acceleration.vToD(a, v);
                    assertEquals(d, d2, Util.EPSILON);
                }
            }
        }

    }

    @Test
    public void test_dToX()
    {
        System.out.println("test dToX()");
        for (double a = -5.0; a <= 5.0; a += .1) {
            if (Util.fuzzyZero(a)) {
                    double x = Acceleration.dToX(0, 0);
                    assertEquals(x, 0.0, Util.EPSILON);
           }
            else {
                for (double d = -5; d <= 5; d += .1) {
                    double x = Acceleration.dToX(a, d);
                    double d2 = Acceleration.xToD(a, x, d >= 0.0);
                    assertEquals(d, d2, Util.EPSILON);
                }
            }
        }
    }

    @Test
    public void test_dToT()
    {
        System.out.println("test dToT()");
        for (double a = -5.0; a <= 5.0; a += .1) {
            if (Util.fuzzyZero(a)) {
                try {
                    double t = Acceleration.dToT(0, 0);
                    fail("Should have thrown an ArithmeticException");
                }
                catch (ArithmeticException e) {
                }
            }
            else {
                for (double d = -5; d <= 5; d += .1) {
                    double t = Acceleration.dToT(a, d);
                    double d2 = Acceleration.tToD(a, t);
                    assertEquals(d, d2, Util.EPSILON);
                }
            }
        }
    }

    @Test
    public void test_dToTau()
    {
        System.out.println("test dToTau()");
        for (double a = -5.0; a <= 5.0; a += .1) {
            if (Util.fuzzyZero(a)) {
                try {
                    double t = Acceleration.dToTau(0, 0);
                    fail("Should have thrown an ArithmeticException");
                }
                catch (ArithmeticException e) {
                }
            }
            else {
                for (double d = -5; d <= 5; d += .1) {
                    double tau = Acceleration.dToTau(a, d);
                    double d2 = Acceleration.tauToD(a, tau);
                    assertEquals(d, d2, Util.EPSILON);
                }
            }
        }
    }

    @Test
    public void test_tToV()
    {
        System.out.println("test tToV()");
        for (double a = -5.0; a <= 5.0; a += .1) {
            if (Util.fuzzyZero(a)) {
                double t = Acceleration.tToV(0, 5);
                assertEquals(t, 0, Util.EPSILON);
            }
            else {
                for (double t = -5; t <= 5; t += .1) {
                    double v = Acceleration.tToV(a, t);
                    double t2 = Acceleration.vToT(a, v);
                    assertEquals(t, t2, Util.EPSILON);
                }
            }
        }
    }

    @Test
    public void test_tToX()
    {
        System.out.println("test tToX()");
        for (double a = -5.0; a <= 5.0; a += .1) {
            if (Util.fuzzyZero(a)) {
                double x = Acceleration.tToX(0, 0);
                assertEquals(x, 0, Util.EPSILON);
                x = Acceleration.tToX(0, -5.0);
                assertEquals(x, 0, Util.EPSILON);
                x = Acceleration.tToX(0, 5.0);
                assertEquals(x, 0, Util.EPSILON);
            }
            else {
                for (double t = -5; t <= 5; t += .1) {
                    double x = Acceleration.tToX(a, t);
                    double t2 = Acceleration.xToT(a, x, t >= 0.0);
                    assertEquals(t, t2, Util.EPSILON);
                }
            }
        }
    }

    @Test
    public void test_tToD()
    {
        System.out.println("test tToD()");
        for (double a = -5.0; a <= 5.0; a += .1) {
            if (Util.fuzzyZero(a)) {
                double d = Acceleration.tToD(0, 0);
                assertEquals(d, 0, Util.EPSILON);
                d = Acceleration.tToD(0, -5.0);
                assertEquals(d, 0, Util.EPSILON);
                d = Acceleration.tToD(0, 5.0);
                assertEquals(d, 0, Util.EPSILON);
            }
            else {
                for (double t = -5; t <= 5; t += .1) {
                    double d = Acceleration.tToD(a, t);
                    double t2 = Acceleration.dToT(a, d);
                    assertEquals(t, t2, Util.EPSILON);
                }
            }
        }
    }

    @Test
    public void test_tToTau()
    {
        System.out.println("test tToTau()");
        for (double a = -5.0; a <= 5.0; a += .1) {
            for (double t = -5; t <= 5; t += .1) {
                double tau = Acceleration.tToTau(a, t);
                double t2 = Acceleration.tauToT(a, tau);
                assertEquals(t, t2, Util.EPSILON);
            }
        }
    }

    @Test
    public void test_tauToV()
    {
        System.out.println("test tauToV()");
        for (double a = -1.0; a <= 1.0; a += .1) {
            if (Util.fuzzyZero(a)) {
                double tau = Acceleration.tauToV(0, 5);
                assertEquals(tau, 0.0, Util.EPSILON);
            }
            else {
                for (double tau = -5; tau <= 5; tau += .1) {
                    double v = Acceleration.tauToV(a, tau);
                    double tau2 = Acceleration.vToTau(a, v);
                    assertEquals(tau, tau2, Util.EPSILON);
                }
            }
        }
    }

    @Test
    public void test_tauToX()
    {
        System.out.println("test tauToX()");
        for (double a = -5.0; a <= 5.0; a += .1) {
            if (Util.fuzzyZero(a)) {
                double x = Acceleration.tauToX(0, 0);
                assertEquals(x, 0, Util.EPSILON);
                x = Acceleration.tauToX(0, -5.0);
                assertEquals(x, 0, Util.EPSILON);
                x = Acceleration.tauToX(0, 5.0);
                assertEquals(x, 0, Util.EPSILON);
            }
            else {
                for (double tau = -5; tau <= 5; tau += .1) {
                    double x = Acceleration.tauToX(a, tau);
                    double tau2 = Acceleration.xToTau(a, x, tau >= 0.0);
                    assertEquals(tau, tau2, Util.EPSILON);
                }
            }
        }
    }

    @Test
    public void test_tauToD()
    {
        System.out.println("test tauToD()");
        for (double a = -5.0; a <= 5.0; a += .1) {
            if (Util.fuzzyZero(a)) {
                double d = Acceleration.tauToD(0, 0);
                assertEquals(d, 0, Util.EPSILON);
                d = Acceleration.tauToD(0, -5.0);
                assertEquals(d, 0, Util.EPSILON);
                d = Acceleration.tauToD(0, 5.0);
                assertEquals(d, 0, Util.EPSILON);
            }
            else {
                for (double tau = -5; tau <= 5; tau += .1) {
                    double d = Acceleration.tauToD(a, tau);
                    double tau2 = Acceleration.dToTau(a, d);
                    assertEquals(tau, tau2, Util.EPSILON);
                }
            }
        }
    }

    @Test
    public void test_tauToT()
    {
        System.out.println("test tauToT()");
        for (double a = -5.0; a <= 5.0; a += .1) {
            for (double tau = -5; tau <= 5; tau += .1) {
                double t = Acceleration.tauToT(a, tau);
                double tau2 = Acceleration.tToTau(a, t);
                assertEquals(tau, tau2, Util.EPSILON);
            }
        }
    }

//    /**
//     * Test of intersect method, of class Acceleration.
//     */
//    @Test
//    public void testIntersect_3args()
//    {
//        System.out.println("intersect");
//        double a = 0.0;
//        Line line = null;
//        boolean later = false;
//        Coordinate expResult = null;
//        Coordinate result = Acceleration.intersect(a, line, later);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of intersect method, of class Acceleration.
//     */
//    @Test
//    public void testIntersect_WorldlineSegment()
//    {
//        System.out.println("intersect");
//        WorldlineSegment other = null;
//        Coordinate expResult = null;
//        Coordinate result = Acceleration.intersect(other);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}
