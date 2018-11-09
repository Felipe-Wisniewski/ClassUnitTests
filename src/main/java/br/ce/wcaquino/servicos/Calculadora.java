package br.ce.wcaquino.servicos;

import br.ce.wcaquino.exceptions.DivisaoPorZeroException;

public class Calculadora {

    public int somar(int valor1, int valor2) {
        return valor1 + valor2;
    }

    public int subtrair(int valor1, int valor2) {
        return valor1 - valor2;
    }

    public int multiplicar(int valor1, int valor2) {
        return valor1 * valor2;
    }

    public int dividir(int valor1, int valor2) throws DivisaoPorZeroException {

        if(valor2 == 0) {
            throw new DivisaoPorZeroException();
        }
        return valor1 / valor2;
    }
}
