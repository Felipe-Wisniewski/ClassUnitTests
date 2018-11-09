package br.ce.wcaquino.servicos;

import org.junit.Assert;
import org.junit.Test;

public class AssertTest {

    @Test
    public void testAsserts() {
        Assert.assertTrue(true);
        Assert.assertFalse(false);

        Assert.assertEquals(10, 10);
        Assert.assertEquals(1.1234, 1.1255, 0.01);

        int i = 5;
        Integer ia = 5;
        Assert.assertEquals(Integer.valueOf(i), ia);
        Assert.assertEquals(i, ia.intValue());

        Assert.assertEquals("bola", "bola");
        Assert.assertNotEquals("bola", "casa");
        Assert.assertTrue("bola".equalsIgnoreCase("BoLa"));

    }
}
