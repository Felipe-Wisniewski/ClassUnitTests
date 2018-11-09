package br.ce.wcaquino.suites;

import br.ce.wcaquino.servicos.CalculadoraTest;
import br.ce.wcaquino.servicos.LocacaoServiceCalcularDescontoTest;
import br.ce.wcaquino.servicos.LocacaoServiceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

//@RunWith(Suite.class)
@Suite.SuiteClasses({
        CalculadoraTest.class,
        LocacaoServiceTest.class,
        LocacaoServiceCalcularDescontoTest.class
})
public class SuiteExecucao {

}
