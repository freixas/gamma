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
package org.freixas.gamma.parser;

import org.freixas.gamma.css.parser.CSSTokenizer;
import java.io.File;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Antonio Freixas
 */
public class CSSTokenizerTest
{

    public CSSTokenizerTest()
    {
    }

    @BeforeClass
    static public void setUpClass()
    {
    }

    @AfterClass
    static public void tearDownClass()
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

    /**
     * Test of tokenize method, of class CSSTokenizer.
     */
    @Test
    public void testTokenize()
        throws Exception
    {
        System.out.println("tokenize");
        String css = """
/* Bob's Settings */

.bob {
  color: blue;
}
axes.bob {
  color: #88F;
}
grid.bob {
  color: #CCF;
}
.simul.bob {
  color: cyan;
}
.calculatedTime {
  textAnchor: BR;
}
.seenTime {
  textAnchor: TR;
}

/* Alice's Settings */

.alice {
  color: red;
}
axes.alice {
  color: #F88;
}
grid.alice {
  color: #FCC;
}
.simul.alice {
  color: magenta;
}
.calculatedTime {
  textAnchor: BL;
}
.seenTime {
  textAnchor: TL;
}

.light {
  color: yellow;
}""";
        CSSTokenizer instance = new CSSTokenizer(new File("test"), css);
        ArrayList<Token<?>> result = instance.tokenize();

    }

}
