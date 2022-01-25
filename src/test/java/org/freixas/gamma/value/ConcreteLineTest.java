/*
 *  Gamma - A Minkowski Spacetime Diagram Generator
 *  Copyright (C) 2021  by Antonio Freixas
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package gamma.value;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Antonio Freixas
 */
public class ConcreteLineTest
{

    public ConcreteLineTest()
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

//    /**
//     * Test of createCopy method, of class ConcreteLine.
//     */
//    @Test
//    public void testCreateCopy()
//    {
//        System.out.println("createCopy");
//        ConcreteLine instance = null;
//        Object expResult = null;
//        Object result = instance.createCopy();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getCoordinate method, of class ConcreteLine.
//     */
//    @Test
//    public void testGetCoordinate()
//    {
//        System.out.println("getCoordinate");
//        ConcreteLine instance = null;
//        Coordinate expResult = null;
//        Coordinate result = instance.getCoordinate();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getAngle method, of class ConcreteLine.
//     */
//    @Test
//    public void testGetAngle()
//    {
//        System.out.println("getAngle");
//        ConcreteLine instance = null;
//        double expResult = 0.0;
//        double result = instance.getAngle();
//        assertEquals(expResult, result, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getSlope method, of class ConcreteLine.
//     */
//    @Test
//    public void testGetSlope()
//    {
//        System.out.println("getSlope");
//        ConcreteLine instance = null;
//        double expResult = 0.0;
//        double result = instance.getSlope();
//        assertEquals(expResult, result, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getConstantOffset method, of class ConcreteLine.
//     */
//    @Test
//    public void testGetConstantOffset()
//    {
//        System.out.println("getConstantOffset");
//        ConcreteLine instance = null;
//        double expResult = 0.0;
//        double result = instance.getConstantOffset();
//        assertEquals(expResult, result, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of relativeTo method, of class ConcreteLine.
//     */
//    @Test
//    public void testRelativeTo()
//    {
//        System.out.println("relativeTo");
//        Frame prime = null;
//        ConcreteLine instance = null;
//        ConcreteLine expResult = null;
//        ConcreteLine result = instance.relativeTo(prime);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of intersect method, of class ConcreteLine.
//     */
//    @Test
//    public void testIntersect_Line()
//    {
//        System.out.println("intersect");
//        ConcreteLine other = null;
//        ConcreteLine instance = null;
//        Coordinate expResult = null;
//        Coordinate result = instance.intersect(other);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of intersect method, of class ConcreteLine.
//     */
//    @Test
//    public void testIntersect_Line_Line()
//    {
//        System.out.println("intersect");
//        ConcreteLine line1 = null;
//        ConcreteLine line2 = null;
//        Coordinate expResult = null;
//        Coordinate result = ConcreteLine.intersect(line1, line2);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of intersect method, of class ConcreteLine.
//     */
//    @Test
//    public void testIntersect_Bounds()
//    {
//        System.out.println("intersect");
//        Bounds bounds = null;
//        ConcreteLine instance = null;
//        LineSegment expResult = null;
//        LineSegment result = instance.intersect(bounds);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
    /**
     * Test of infiniteIntersect method, of class ConcreteLine.
     */
    @Test
    public void testInfiniteIntersect()
    {
        System.out.println("infiniteIntersect");

        Double pInf = Double.POSITIVE_INFINITY;
        Double nInf = Double.NEGATIVE_INFINITY;

        Bounds[] bounds = {
            new Bounds(-10, -10, 10, 10),
            new Bounds(nInf, -10, 10, 10),
            new Bounds(-10, nInf, 10, 10),
            new Bounds(-10, -10, pInf, 10),
            new Bounds(-10, -10, 10, pInf),
            new Bounds(nInf, nInf, 10, 10),
            new Bounds(-10, -10, pInf, pInf),
            new Bounds(nInf, -10, pInf, 10),
            new Bounds(-10, -10, pInf, pInf),
            new Bounds(nInf, -10, 10, pInf),
            new Bounds(-10, nInf, pInf, 10),
            new Bounds(nInf, nInf, 10, pInf),
            new Bounds(nInf, nInf, pInf, 10),
            new Bounds(-10, nInf, pInf, pInf),
            new Bounds(nInf, -10, pInf, pInf),
            new Bounds(nInf, nInf, pInf, pInf),
            new Bounds(nInf, -10, nInf, 10),
            new Bounds(-10, pInf, 10, pInf),
            new Bounds(nInf, pInf, nInf, pInf)
        };
        ConcreteLine[] lines = {
            new ConcreteLine(90, new Coordinate(0, 0)),
            new ConcreteLine(0, new Coordinate(0, 0)),
            new ConcreteLine(45, new Coordinate(0, 0)),
            new ConcreteLine(-45, new Coordinate(0, 0)),

            new ConcreteLine(90, new Coordinate(20, 20)),
            new ConcreteLine(0, new Coordinate(20, 20)),
            new ConcreteLine(45, new Coordinate(20, 20)),
            new ConcreteLine(-45, new Coordinate(20, 20)),

            new ConcreteLine(90, new Coordinate(0, 0)),
            new ConcreteLine(0, new Coordinate(0, 0)),
            new ConcreteLine(45, new Coordinate(0, 0)),
            new ConcreteLine(-45, new Coordinate(0, 0)),

            new ConcreteLine(90, new Coordinate(20, 20)),
            new ConcreteLine(0, new Coordinate(20, 20)),
            new ConcreteLine(45, new Coordinate(20, 20)),
            new ConcreteLine(-45, new Coordinate(20, 20)),

            new ConcreteLine(90, new Coordinate(0, 0)),
            new ConcreteLine(0, new Coordinate(0, 0)),
            new ConcreteLine(45, new Coordinate(0, 0)),
            new ConcreteLine(-45, new Coordinate(0, 0)),

            new ConcreteLine(90, new Coordinate(20, 20)),
            new ConcreteLine(0, new Coordinate(20, 20)),
            new ConcreteLine(45, new Coordinate(20, 20)),
            new ConcreteLine(-45, new Coordinate(20, 20)),
        };

        for (int i = 0; i < 8; i++) {
            lines[i + 8] = new ConcreteLine(lines[i + 8], false, true);
        }
        for (int i = 0; i < 8; i++) {
            lines[i + 16] = new ConcreteLine(lines[i + 16], true, false);
        }

        for (int b = 0; b < bounds.length; b++) {
            for (int l = 0; l < lines.length; l++) {
                System.out.println("\nb=" + b + " l=" + l);
                System.out.println(lines[l]);
                System.out.println(bounds[b]);
                CurveSegment segment = lines[l].infiniteIntersect(bounds[b]);
                System.out.println("Result is " + segment);
            }
        }
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
//
//    /**
//     * Test of isInfiniteMinus method, of class ConcreteLine.
//     */
//    @Test
//    public void testIsInfiniteMinus()
//    {
//        System.out.println("isInfiniteMinus");
//        ConcreteLine instance = null;
//        boolean expResult = false;
//        boolean result = instance.isInfiniteMinus();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setIsInfiniteMinus method, of class ConcreteLine.
//     */
//    @Test
//    public void testSetIsInfiniteMinus()
//    {
//        System.out.println("setIsInfiniteMinus");
//        boolean isInfiniteMinus = false;
//        ConcreteLine instance = null;
//        instance.setIsInfiniteMinus(isInfiniteMinus);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isInfinitePlus method, of class ConcreteLine.
//     */
//    @Test
//    public void testIsInfinitePlus()
//    {
//        System.out.println("isInfinitePlus");
//        ConcreteLine instance = null;
//        boolean expResult = false;
//        boolean result = instance.isInfinitePlus();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setIsInfinitePlus method, of class ConcreteLine.
//     */
//    @Test
//    public void testSetIsInfinitePlus()
//    {
//        System.out.println("setIsInfinitePlus");
//        boolean isInfinitePlus = false;
//        ConcreteLine instance = null;
//        instance.setIsInfinitePlus(isInfinitePlus);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getBounds method, of class ConcreteLine.
//     */
//    @Test
//    public void testGetBounds()
//    {
//        System.out.println("getBounds");
//        ConcreteLine instance = null;
//        Bounds expResult = null;
//        Bounds result = instance.getBounds();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of toDisplayableString method, of class ConcreteLine.
//     */
//    @Test
//    public void testToDisplayableString()
//    {
//        System.out.println("toDisplayableString");
//        HCodeEngine engine = null;
//        ConcreteLine instance = null;
//        String expResult = "";
//        String result = instance.toDisplayableString(engine);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of toString method, of class ConcreteLine.
//     */
//    @Test
//    public void testToString()
//    {
//        System.out.println("toString");
//        ConcreteLine instance = null;
//        String expResult = "";
//        String result = instance.toString();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}
