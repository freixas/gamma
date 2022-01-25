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

import org.freixas.gamma.value.ConcreteLine;
import org.freixas.gamma.value.Coordinate;
import org.freixas.gamma.value.Line;
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
public class OffsetAccelerationTest
{
    public static final double EPSILON = 5.0E-10;

    public OffsetAccelerationTest()
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
        for (double a = -1.0; a <= 1.0; a += .1) {
            if (Util.fuzzyZero(a)) {
                OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
                try {
                    double x = oa.vToX(0);
                    fail("Should have thrown an ArithmeticException");
                }
                catch (ArithmeticException e) {
                }
            }
            else {
               for (double v = -.95; v < 1; v += .05) {
                    OffsetAcceleration oa = new OffsetAcceleration(a, v, new Coordinate(3, -4), 2, 1);
                    double x = oa.vToX(v);
                    double v2 = oa.xToV(x, a* v > 0);
                    assertEquals(v, v2, EPSILON);
                }
            }
        }
    }

    @Test
    public void test_vToD()
    {
        System.out.println("test vToD()");
        for (double a = -1.0; a <= 1.0; a += .1) {
            if (Util.fuzzyZero(a)) {
		OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
                try {
                    double d = oa.vToD(0);
                    fail("Should have thrown an ArithmeticException");
                }
                catch (ArithmeticException e) {
                }
            }
            else {
                for (double v = -.95; v < 1; v += .05) {
                    OffsetAcceleration oa = new OffsetAcceleration(a, v, new Coordinate(3, -4), 2, 1);
                    double d = oa.vToD(v);
                    double v2 = oa.dToV(d);
                    assertEquals(v, v2, EPSILON);
                }
            }
        }
    }

    @Test
    public void test_vToT()
    {
        System.out.println("test vToT()");
        for (double a = -1.0; a <= 1.0; a += .1) {
            if (Util.fuzzyZero(a)) {
		OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
                try {
                    double t = oa.vToT(0);
                    fail("Should have thrown an ArithmeticException");
                }
                catch (ArithmeticException e) {
                }
            }
            else {
                for (double v = -.95; v < 1; v += .05) {
                    OffsetAcceleration oa = new OffsetAcceleration(a, v, new Coordinate(3, -4), 2, 1);
                    double t = oa.vToT(v);
                    double v2 = oa.tToV(t);
                    assertEquals(v, v2, EPSILON);
                }
            }
        }
    }

    @Test
    public void test_vToTau()
    {
        System.out.println("test vToTau()");
        for (double a = -1.0; a <= 1.0; a += .1) {
            if (Util.fuzzyZero(a)) {
		OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
                try {
                    double tau = oa.vToTau(0);
                    fail("Should have thrown an ArithmeticException");
                }
                catch (ArithmeticException e) {
                }
            }
            else {
                for (double v = -.95; v < 1; v += .05) {
                    OffsetAcceleration oa = new OffsetAcceleration(a, v, new Coordinate(3, -4), 2, 1);
                    double tau = oa.vToTau(v);
                    double v2 = oa.tauToV(tau);
                    assertEquals(v, v2, EPSILON);
                }
            }
        }
    }

    @Test
    public void test_dToV()
    {
        System.out.println("test dToV()");
        for (double a = -1.0; a <= 1.0; a += .1) {
            if (Util.fuzzyZero(a)) {
		OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
                double v = oa.dToV(0);
                assertEquals(v, .1, EPSILON);
            }
            else {
                for (double d = -5; d <= 5; d += .1) {
		    OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
                    double v = oa.dToV(d);
                    double d2 = oa.vToD(v);
                    assertEquals(d, d2, EPSILON);
                }
            }
        }

    }

    @Test
    public void test_dToX()
    {
        System.out.println("test dToX()");
        for (double a = -1.0; a <= 1.0; a += .1) {
            if (Util.fuzzyZero(a)) {
		OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
                    double x = oa.dToX(0);
                    assertEquals(x, 2.0, EPSILON);
           }
            else {
                for (double d = 1; d <= 5; d += .1) {
		    OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
                    double x = oa.dToX(d);
                    double d2 = oa.xToD(x, Util.fuzzyGE(d, 1.0));
                    assertEquals(d, d2, EPSILON);
                }
            }
        }
    }

    @Test
    public void test_dToT()
    {
        System.out.println("test dToT()");
        for (double a = -1.0; a <= 1.0; a += .1) {
            if (Util.fuzzyZero(a)) {
		OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
                double t = oa.dToT(0);
                assertEquals(-14.0, t, EPSILON);
            }
            else {
                for (double d = -5; d <= 5; d += .1) {
		    OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
                    double t = oa.dToT(d);
                    double d2 = oa.tToD(t);
                    assertEquals(d, d2, EPSILON);
                }
            }
        }
    }

    @Test
    public void test_dToTau()
    {
        System.out.println("test dToTau()");
        for (double a = -1.0; a <= 1.0; a += .1) {
            if (Util.fuzzyZero(a)) {
		OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
                try {
                    double tau = oa.dToTau(0);
                    assertEquals(oa.tToTau(-14), tau, EPSILON);
                }
                catch (ArithmeticException e) {
                }
            }
            else {
                for (double d = -5; d <= 5; d += .1) {
		    OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
                    double tau = oa.dToTau(d);
                    double d2 = oa.tauToD(tau);
                    assertEquals(d, d2, EPSILON);
                }
            }
        }
    }

    @Test
    public void test_tToV()
    {
        System.out.println("test tToV()");
        for (double a = -1.0; a <= 1.0; a += .1) {
            if (Util.fuzzyZero(a)) {
		OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
                double v = oa.tToV(5);
                assertEquals(.1, v, EPSILON);
            }
            else {
                for (double t = -5; t <= 5; t += .1) {
		    OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
                    double v = oa.tToV(t);
                    double t2 = oa.vToT(v);
                    assertEquals(t, t2, EPSILON);
                }
            }
        }
    }

    @Test
    public void test_tToX()
    {
        System.out.println("test tToX()");
        for (double a = -1.0; a <= 1.0; a += .1) {
            if (Util.fuzzyZero(a)) {
		OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
                double x = oa.tToX(0);
                assertEquals(x, 3, EPSILON);
                x = oa.tToX(-5.0);
                assertEquals(x, 3, EPSILON);
                x = oa.tToX(5.0);
                assertEquals(x, 3, EPSILON);
            }
            else {
                for (double t = -5; t <= 5; t += .1) {
		    OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
                    double x = oa.tToX(t);
                    double t2 = oa.xToT(x, t >= -4.0);
                    assertEquals(t, t2, EPSILON);
                }
            }
        }
    }

    @Test
    public void test_tToD()
    {
        System.out.println("test tToD()");
        for (double a = -1.0; a <= 1.0; a += .1) {
            if (Util.fuzzyZero(a)) {
		OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
                double d = oa.tToD(0);
                assertEquals(1.4, d, EPSILON);
                d = oa.tToD(-5.0);
                assertEquals(.9, d, EPSILON);
                d = oa.tToD(5.0);
                assertEquals(1.9, d, EPSILON);
            }
            else {
                for (double t = -5; t <= 5; t += .1) {
		    OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
                    double d = oa.tToD(t);
                    double t2 = oa.dToT(d);
                    assertEquals(t, t2, EPSILON);
                }
            }
        }
    }

    @Test
    public void test_tToTau()
    {
        System.out.println("test tToTau()");
        for (double a = -1.0; a <= 1.0; a += .1) {
            for (double t = -5; t <= 5; t += .1) {
		OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
                double tau = oa.tToTau(t);
                double t2 = oa.tauToT(tau);
                assertEquals(t, t2, EPSILON);
            }
        }
    }

    @Test
    public void test_tauToV()
    {
        System.out.println("test tauToV()");
        for (double a = -1.0; a <= 1.0; a += .1) {
            if (Util.fuzzyZero(a)) {
		OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
                double v = oa.tauToV(5);
                assertEquals(0.1, v, EPSILON);
            }
            else {
                for (double tau = -5; tau <= 5; tau += .1) {
		    OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
                    double v = oa.tauToV(tau);
                    double tau2 = oa.vToTau(v);
                    assertEquals(tau, tau2, EPSILON);
                }
            }
        }
    }

    @Test
    public void test_tauToX()
    {
        System.out.println("test tauToX()");
        for (double a = -1.0; a <= 1.0; a += .1) {
            if (Util.fuzzyZero(a)) {
		OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
                double x = oa.tauToX(0);
                assertEquals(x, 3, EPSILON);
                x = oa.tauToX(-5.0);
                assertEquals(x, 3, EPSILON);
                x = oa.tauToX(5.0);
                assertEquals(x, 3, EPSILON);
            }
            else {
                for (double tau = -5; tau <= 5; tau += .1) {
		    OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
                    double x = oa.tauToX(tau);
                    double tau2 = oa.xToTau(x, tau >= 2.0);
                    assertEquals(tau, tau2, EPSILON);
                }
            }
        }
    }

    @Test
    public void test_tauToD()
    {
        System.out.println("test tauToD()");
        for (double a = -1.0; a <= 1.0; a += .1) {
            if (Util.fuzzyZero(a)) {
		OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
                double d = oa.tauToD(-7.949874371066199);
                assertEquals(0, d, EPSILON);
            }
            else {
                for (double tau = -5; tau <= 5; tau += .1) {
		    OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
                    double d = oa.tauToD(tau);
                    double tau2 = oa.dToTau(d);
                    assertEquals(tau, tau2, EPSILON);
                }
            }
        }
    }

    @Test
    public void test_tauToT()
    {
        System.out.println("test tauToT()");
        for (double a = -1.0; a <= 1.0; a += .1) {
            for (double tau = -5; tau <= 5; tau += .1) {
		OffsetAcceleration oa = new OffsetAcceleration(a, .1, new Coordinate(3, -4), 2, 1);
		double t = oa.tauToT(tau);
                double tau2 = oa.tToTau(t);
                assertEquals(tau, tau2, EPSILON);
            }
        }
    }

    @Test
    public void test_intersect()
    {
        System.out.println("test intersect()");
        for (double a = 0.1; a <= 1.0; a += .1) {
            OffsetAcceleration oa = new OffsetAcceleration(a, 0, new Coordinate(0, 0), 0, 0);
            for (double angle = -90.0; angle <= 90.0; angle++) {
                System.out.println("Acc " + a + " angle " + angle);
                if (Math.abs(angle) <= 45.0) continue;

                Line line = new ConcreteLine(angle, new Coordinate(5, 0));

                // There should always be an intersection

                Coordinate[] intersection = oa.intersect(line);
                assertNotNull(intersection[0]);

                // The (x, t) coordinate we get should be on the curve

                double t = oa.xToT(intersection[0].x, false);
                double t1 = oa.xToT(intersection[0].x, true);
                double x = oa.tToX(intersection[0].t);

                System.out.println("x " + intersection[0].x + " to " + x);
                System.out.println("t " + intersection[0].t + " to " + t);
                System.out.println("t1 " + intersection[0].t + " to " + t1);

                //if (Util.fuzzyEQ(t1, intersection[0] .t)) t = t1;
                assertEquals(intersection[0] .x, x, EPSILON);
                assertEquals(intersection[0] .t, t, EPSILON);

                // There should always be an intersection[0]

                assertNotNull(intersection[1] );

                // The (x, t) coordinate we get should be on the curve

                t = oa.xToT(intersection[1].x, true);
                t1 = oa.xToT(intersection[1].x, false);
                x = oa.tToX(intersection[1].t);

                //if (Util.fuzzyEQ(t1, intersection[1] .t)) t = t1;
                assertEquals(intersection[1].x, x, EPSILON);
                assertEquals(intersection[1].t, t, EPSILON);

            }
        }
    }

}
