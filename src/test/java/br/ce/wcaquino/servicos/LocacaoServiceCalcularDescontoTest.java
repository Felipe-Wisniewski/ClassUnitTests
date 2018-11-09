package br.ce.wcaquino.servicos;

import br.ce.wcaquino.dao.LocacaoDao;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class LocacaoServiceCalcularDescontoTest {

    @InjectMocks
    private LocacaoService service;

    @Mock private LocacaoDao dao;
    @Mock private SPCService spc;

    @Parameter
    public List<Filme> filmes;

    @Parameter(value = 1)
    public Double valorLocacao;

    @Parameter(value = 2)
    public String nomeTest;

    @Before
    public void setupTest() {
        MockitoAnnotations.initMocks(this);
    }

    private static Filme filme1 = new Filme("filme1", 2, 5.0);
    private static Filme filme2 = new Filme("filme2", 2, 5.0);
    private static Filme filme3 = new Filme("filme3", 2, 5.0);
    private static Filme filme4 = new Filme("filme4", 2, 5.0);
    private static Filme filme5 = new Filme("filme5", 2, 5.0);
    private static Filme filme6 = new Filme("filme6", 2, 5.0);
    private static Filme filme7 = new Filme("filme7", 2, 5.0);


    @Parameters(name = "{2}")
    public static Collection<Object[]> getParametros() {
        return Arrays.asList(new Object[][] {
                {Arrays.asList(filme1, filme2), 10.0, "DoisFilmesSemDesconto"},
                {Arrays.asList(filme1, filme2, filme3), 13.75, "DescontoNoTerceiroFilme 25%"},
                {Arrays.asList(filme1, filme2, filme3, filme4), 16.25, "DescontoNoQuartoFilme 50%"},
                {Arrays.asList(filme1, filme2, filme3, filme4, filme5), 17.50, "DescontoNoQuintoFilme 75%"},
                {Arrays.asList(filme1, filme2, filme3, filme4, filme5, filme6), 17.50, "DescontoNoSextoFilme 100%"},
                {Arrays.asList(filme1, filme2, filme3, filme4, filme5, filme6, filme7), 22.50, "SemDescontoNoSetimoFilme"}
        });
    }

    @Test
    public void deveCalcularValorDaLocacao() throws FilmeSemEstoqueException, LocadoraException {
        //cenário
        Usuario usuario = new Usuario("Felipe");

        //ação
        Locacao locacao = service.alugarFilme(usuario, filmes);

        //validação
        assertEquals(valorLocacao, locacao.getValor(), 0.01);
    }


}
