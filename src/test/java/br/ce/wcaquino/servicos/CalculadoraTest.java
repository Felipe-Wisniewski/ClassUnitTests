package br.ce.wcaquino.servicos;

import br.ce.wcaquino.exceptions.DivisaoPorZeroException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CalculadoraTest {

    private Calculadora calc;

    @Before
    public void setupCalc() {
        calc = new Calculadora();
    }

    @Test
    public void deveSomarDoisValores() {
        //cenário
        int valor1 = 3;
        int valor2 = 5;

        //ação
        int resultado = calc.somar(valor1, valor2);

        //validação
        Assert.assertEquals(8, resultado);
    }

    @Test
    public void deveSubtrairDoisValores() {
        //cenário
        int valor1 = 6;
        int valor2 = 5;

        //ação
        int resultado = calc.subtrair(valor1, valor2);

        //validação
        Assert.assertEquals(1, resultado);
    }

    @Test
    public void deveMultiplicarDoisValores() {
        //cenário
        int valor1 = 3;
        int valor2 = 7;

        //ação
        int resultado = calc.multiplicar(valor1, valor2);

        //validação
        Assert.assertEquals(21, resultado);
    }

    @Test
    public void deveDividirDoisValores() throws DivisaoPorZeroException {
        //cenário
        int valor1 = 6;
        int valor2 = 3;

        //ação
        int resultado = calc.dividir(valor1, valor2);

        //validação
        Assert.assertEquals(2, resultado);
    }

    @Test(expected = DivisaoPorZeroException.class)
    public void deveGerarUmaExceptionAoDividirPorZero() throws DivisaoPorZeroException {
        //cenário
        int valor1 = 4;
        int valor2 = 0;

        //ação
        int resultado = calc.dividir(valor1, valor2);
    }
}
