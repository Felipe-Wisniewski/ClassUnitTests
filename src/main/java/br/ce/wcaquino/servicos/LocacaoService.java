package br.ce.wcaquino.servicos;

import br.ce.wcaquino.dao.LocacaoDao;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static br.ce.wcaquino.utils.DataUtils.adicionarDias;

public class LocacaoService {

	private LocacaoDao locacaoDao;
	private SPCService spc;
	private EmailService emailService;

	public Locacao alugarFilme(Usuario usuario, List<Filme> filmes) throws FilmeSemEstoqueException, LocadoraException {

		if(usuario == null) {
			throw new LocadoraException("Usuario vazio");
		}

		if(filmes == null || filmes.isEmpty()) {
			throw new LocadoraException("Filme vazio");
		}

		for (Filme filme: filmes) {
			if(filme.getEstoque() == 0){
				throw new FilmeSemEstoqueException("Filme sem estoque");
			}
		}

		boolean negativado;
		try {
			negativado = spc.possuiNegativacao(usuario);
		} catch (Exception e) {
			throw new LocadoraException("Problemas com SPC");
		}
		if (negativado) {
			throw new LocadoraException("Usuário negativado");
		}

		Locacao locacao = new Locacao();
		locacao.setFilmes(filmes);
		locacao.setUsuario(usuario);
		locacao.setDataLocacao(new Date());
		locacao.setValor(calcularValorLocacao(filmes));

		//Entrega no dia seguinte
		Date dataEntrega = new Date();
		dataEntrega = adicionarDias(dataEntrega, 1);
		if (DataUtils.verificarDiaSemana(dataEntrega, Calendar.SUNDAY)) {
			dataEntrega = adicionarDias(dataEntrega, 1);
		}
		locacao.setDataRetorno(dataEntrega);
		
		//Salvando a locacao...	
		locacaoDao.salvar(locacao);

		return locacao;
	}

	private Double calcularValorLocacao(List<Filme> filmes) {
		Double valorTotal = 0d;
		for (int i = 0; i < filmes.size(); i++) {
			Double valorFilme = filmes.get(i).getPrecoLocacao();
			switch (i){
				case 2:
					valorFilme = valorFilme * 0.75;
					break;
				case 3:
					valorFilme = valorFilme * 0.5;
					break;
				case 4:
					valorFilme = valorFilme * 0.25;
					break;
				case 5:
					valorFilme = 0d;
					break;
			}
			valorTotal += valorFilme;
		}
		return valorTotal;
	}

	public void notificarAtrasos() {
		List<Locacao> locacoes = locacaoDao.obterLocacoesPendentes();

		for (Locacao locacao: locacoes) {
			if (locacao.getDataRetorno().before(new Date()))
				emailService.notificarAtraso(locacao.getUsuario());
		}
	}

	public void prorrogarLocacao(Locacao locacao, int dias) {
		Locacao novaLocacao = new Locacao();
		novaLocacao.setUsuario(locacao.getUsuario());
		novaLocacao.setFilmes(locacao.getFilmes());
		novaLocacao.setDataLocacao(new Date());
		novaLocacao.setDataRetorno(DataUtils.obterDataComDiferencaDias(dias));
		novaLocacao.setValor(locacao.getValor() * dias);
		locacaoDao.salvar(novaLocacao);
	}


}