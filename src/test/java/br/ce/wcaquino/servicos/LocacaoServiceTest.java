package br.ce.wcaquino.servicos;

import br.ce.wcaquino.dao.LocacaoDao;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.*;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.mockito.*;

import java.util.*;

import static org.junit.Assert.*;

public class LocacaoServiceTest {

    @InjectMocks
    private LocacaoService service;

    @Mock private LocacaoDao dao;
    @Mock private SPCService spc;
    @Mock private EmailService email;

    @Rule
    public ErrorCollector error = new ErrorCollector();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setupTest() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void deveAlugarFilme() throws Exception {
        Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
        //cenario
        Usuario usuario = new Usuario("Felipe");
        List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 5.0));

        //acao
        Locacao locacao = service.alugarFilme(usuario, filmes);

        //verificacao
        error.checkThat(locacao.getValor(), CoreMatchers.is(5.0));

        assertEquals(5.0, locacao.getValor(), 0.0);

        assertTrue(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()));
        assertTrue(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)));
    }

    @Test(expected = FilmeSemEstoqueException.class)
    public void deveLancarExcecaoAoAlugarFilmeSemEstoque() throws Exception {
        //cenario
        Usuario usuario = new Usuario("Felipe");
        List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 0, 5.0));

        //acao
        service.alugarFilme(usuario, filmes);
    }

    @Test
    public void naoDeveAlugarFilmesSemUsuario() throws FilmeSemEstoqueException {
        //cenario
        List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 0, 5.0));

        //acao
        try {
            service.alugarFilme(null, filmes);
            fail("Deveria ter lancado uma excecao!");
        } catch (LocadoraException e) {
            assertThat(e.getMessage(), CoreMatchers.is("Usuario vazio"));
        }
    }

    @Test
    public void naoDeveAlugarFilmeSemFilme() throws FilmeSemEstoqueException, LocadoraException {
        //cenario
        Usuario usuario = new Usuario("Felipe");

        exception.expect(LocadoraException.class);
        exception.expectMessage("Filme vazio");

        //acao
        service.alugarFilme(usuario, null);
    }

    @Test
    public void deveDevolverNaSegundaAoAlugarAosSabados() throws FilmeSemEstoqueException, LocadoraException {
        Assume.assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));

        //cenáro
        Usuario usuario = new Usuario("Felipe");
        List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 4.0));

        //ação
        Locacao locacao = service.alugarFilme(usuario, filmes);

        //validação
        boolean ehSegunda = DataUtils.verificarDiaSemana(locacao.getDataRetorno(), Calendar.MONDAY);
        assertTrue(ehSegunda);
    }

    @Test
    public void naoDeveAlugarFilmeParaNegativadoSPC() throws Exception {
        //cenário
        Usuario usuario = new Usuario("Usuario");
        List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 5.0));

        Mockito.when(spc.possuiNegativacao(usuario)).thenReturn(true);

        //ação
        try {
            service.alugarFilme(usuario, filmes);

        //validacao
            Assert.fail();
        } catch (LocadoraException e) {
            Assert.assertThat(e.getMessage(), CoreMatchers.is("Usuário negativado"));
        }

        Mockito.verify(spc).possuiNegativacao(usuario);
    }

    @Test
    public void deveEnviarEmailParaLocacoesAtrasadas() throws FilmeSemEstoqueException, LocadoraException {
        //cenário
        Usuario usuario1 = new Usuario("Usuario atrasado");
        Usuario usuario2 = new Usuario("Usuario em dia");

        List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 5.0));

        Locacao locacao1 = service.alugarFilme(usuario1, filmes);
        locacao1.setDataLocacao(DataUtils.obterDataComDiferencaDias(-4));
        locacao1.setDataRetorno(DataUtils.obterDataComDiferencaDias(-2));

        Locacao locacao2 = service.alugarFilme(usuario2, filmes);

        List<Locacao> locacoes = Arrays.asList(locacao1, locacao2);

        Mockito.when(dao.obterLocacoesPendentes()).thenReturn(locacoes);

        //ação
        service.notificarAtrasos();

        //validação
        Mockito.verify(email).notificarAtraso(usuario1);
        Mockito.verify(email, Mockito.never()).notificarAtraso(usuario2);
        Mockito.verifyNoMoreInteractions(email);
    }

    @Test
    public void deveTratarErroNoSPC() throws Exception {
        //cenário
        Usuario usuario = new Usuario("Usuario atrasado");
        List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 5.0));

        Mockito.when(spc.possuiNegativacao(usuario)).thenThrow(new Exception("Falha catastrófica"));

        //validação
        exception.expect(LocadoraException.class);
        exception.expectMessage("Problemas com SPC");

        //ação
        service.alugarFilme(usuario, filmes);
    }

    @Test
    public void deveProrrogarUmaLocacao() throws FilmeSemEstoqueException, LocadoraException {
        //cenário
        Usuario usuario = new Usuario("Usuario1");
        List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 5.0));
        Locacao locacao = new Locacao();
        locacao.setUsuario(usuario);
        locacao.setFilmes(filmes);
        locacao.setDataLocacao(new Date());
        locacao.setDataRetorno(DataUtils.obterDataComDiferencaDias(1));
        locacao.setValor(5.0);

        //ação
        service.prorrogarLocacao(locacao, 3);

        //validação
        ArgumentCaptor<Locacao> argCapt = ArgumentCaptor.forClass(Locacao.class);
        Mockito.verify(dao).salvar(argCapt.capture());
        Locacao locacaoCapt = argCapt.getValue();

        error.checkThat(locacaoCapt.getValor(), CoreMatchers.is(15.0));
        //error.checkThat(locacaoCapt.getDataLocacao(),  );
        Assert.assertTrue(DataUtils.isMesmaData(locacaoCapt.getDataLocacao(), new Date()));
        //error.checkThat(locacaoCapt.getDataRetorno(), CoreMatchers.is(DataUtils.obterDataComDiferencaDias(3)));
        Assert.assertTrue(DataUtils.isMesmaData(locacaoCapt.getDataRetorno(), DataUtils.obterDataComDiferencaDias(3)));
    }
}
