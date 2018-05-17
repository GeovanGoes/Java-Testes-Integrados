/**
 * 
 */
package br.com.caelum.pm73;

import static org.junit.Assert.assertEquals;
import br.com.caelum.pm73.builder.LeilaoBuilder;
import br.com.caelum.pm73.dao.CriadorDeSessao;
import br.com.caelum.pm73.dao.LeilaoDao;
import br.com.caelum.pm73.dao.UsuarioDao;
import br.com.caelum.pm73.dominio.Leilao;
import br.com.caelum.pm73.dominio.Usuario;

import java.util.Calendar;
import java.util.List;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author geovan.goes
 *
 */
public class LeilaoDaoTest
{

	private Session session;
	private UsuarioDao usuarioDao;
	private LeilaoDao leilaoDao;
	
	@Before
	public void antes()
	{
		session = new CriadorDeSessao().getSession();
		usuarioDao = new UsuarioDao(session);
		leilaoDao = new LeilaoDao(session);
		
		session.beginTransaction();
	}
	
	@After
	public void depois()
	{
		session.getTransaction().rollback();
		session.close();
	}
	
	@Test
	public void deveContarLeiloesEncerrados()
	{
		Usuario dono = new Usuario("Mauricio", "mauricio@mauricio.com");
		Leilao ativo = new Leilao("Geladeira", 1500.00, dono, false);
		
		Leilao encerrado = new Leilao("Xbox", 700.00, dono, false);
		encerrado.encerra();
		
		usuarioDao.salvar(dono);
		leilaoDao.salvar(ativo);
		leilaoDao.salvar(encerrado);

		long resultado = leilaoDao.total();

		assertEquals(1L, resultado);
	}
	
	@Test
    public void deveContarLeiloesNaoEncerrados() 
	{
        // criamos um usuario
        Usuario mauricio = new Usuario("Mauricio Aniche",
                "mauricio@aniche.com.br");

        // criamos os dois leiloes

        Leilao ativo = new LeilaoBuilder()
            .comDono(mauricio)
            .constroi();
        Leilao encerrado = new LeilaoBuilder()
            .comDono(mauricio)
            .encerrado()
            .constroi();

        // persistimos todos no banco
        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(ativo);
        leilaoDao.salvar(encerrado);

        // pedimos o total para o DAO
        long total = leilaoDao.total();

        assertEquals(1L, total);
    }
	
	@Test
    public void deveRetornarLeiloesDeProdutosNovos() 
	{
        Usuario mauricio = new Usuario("Mauricio Aniche", "mauricio@aniche.com.br");

        Leilao produtoNovo = new Leilao("XBox", 700.0, mauricio, false);
        Leilao produtoUsado = new Leilao("Geladeira", 1500.0, mauricio,true);

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(produtoNovo);
        leilaoDao.salvar(produtoUsado);

        List<Leilao> novos = leilaoDao.novos();

        assertEquals(1, novos.size());
        assertEquals("XBox", novos.get(0).getNome());
    }
	
	
	@Test
    public void deveTrazerSomenteLeiloesAntigos() 
	{
        Usuario mauricio = new Usuario("Mauricio Aniche","mauricio@aniche.com.br");

        Leilao recente = new Leilao("XBox", 700.0, mauricio, false);
        Leilao antigo = new Leilao("Geladeira", 1500.0, mauricio,true);

        Calendar dataRecente = Calendar.getInstance();
        Calendar dataAntiga = Calendar.getInstance();
        dataAntiga.add(Calendar.DAY_OF_MONTH, -10);

        recente.setDataAbertura(dataRecente);
        antigo.setDataAbertura(dataAntiga);

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(recente);
        leilaoDao.salvar(antigo);

        List<Leilao> antigos = leilaoDao.antigos();

        assertEquals(1, antigos.size());
        assertEquals("Geladeira", antigos.get(0).getNome());
    }
	
	
	@Test
	public void deveTrazerLeiloesNaoencerradosNoPeriodo()
	{
		Calendar inicio = Calendar.getInstance();
		inicio.add(Calendar.DAY_OF_MONTH, -10);
		
		Calendar fim = Calendar.getInstance();
		
		Usuario mauricio = new Usuario("Mauricio Aniche","mauricio@aniche.com.br");
		
		Leilao leilao1 = new Leilao("Geladeira", 700.0, mauricio, false);
		Calendar data1 = Calendar.getInstance();
		data1.add(Calendar.DAY_OF_MONTH, -2);
		leilao1.setDataAbertura(data1);
		
		Leilao leilao2 = new Leilao("XBox", 1500.0, mauricio,true);
		Calendar data2 = Calendar.getInstance();
		data2.add(Calendar.DAY_OF_MONTH, -20);
		leilao1.setDataAbertura(data2);
		
		usuarioDao.salvar(mauricio);
        leilaoDao.salvar(leilao1);
        leilaoDao.salvar(leilao2);
		
		List<Leilao> list = leilaoDao.porPeriodo(inicio, fim);
		
		assertEquals(1, list.size());
        assertEquals("XBox", list.get(0).getNome());
	}
	
	@Test
	public void naoDeveTrazerLeiloesEncerradosNoPeriodo()
	{
		Calendar inicio = Calendar.getInstance();
		inicio.add(Calendar.DAY_OF_MONTH, -10);
		
		Calendar fim = Calendar.getInstance();
		
		Usuario mauricio = new Usuario("Mauricio Aniche","mauricio@aniche.com.br");
		
		Leilao leilao1 = new Leilao("Geladeira", 700.0, mauricio, true);
		Calendar data1 = Calendar.getInstance();
		data1.add(Calendar.DAY_OF_MONTH, -2);
		leilao1.setDataAbertura(data1);
		leilao1.encerra();
		
		
		usuarioDao.salvar(mauricio);
		leilaoDao.salvar(leilao1);
		
		assertEquals(0, leilaoDao.porPeriodo(inicio, fim).size());
	}
	
	@Test
    public void deveRetornarLeiloesDisputados() {
        Usuario mauricio = new Usuario("Mauricio", "mauricio@aniche.com.br");
        Usuario marcelo = new Usuario("Marcelo", "marcelo@aniche.com.br");

        Leilao leilao1 = new LeilaoBuilder()
                .comDono(marcelo)
                .comValor(3000.0)
                .comLance(Calendar.getInstance(), mauricio, 3000.0)
                .comLance(Calendar.getInstance(), marcelo, 3100.0)
                .constroi();

        Leilao leilao2 = new LeilaoBuilder()
                .comDono(mauricio)
                .comValor(3200.0)
                .comLance(Calendar.getInstance(), mauricio, 3000.0)
                .comLance(Calendar.getInstance(), marcelo, 3100.0)
                .comLance(Calendar.getInstance(), mauricio, 3200.0)
                .comLance(Calendar.getInstance(), marcelo, 3300.0)
                .comLance(Calendar.getInstance(), mauricio, 3400.0)
                .comLance(Calendar.getInstance(), marcelo, 3500.0)
                .constroi();

        usuarioDao.salvar(marcelo);
        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(leilao1);
        leilaoDao.salvar(leilao2);

        List<Leilao> leiloes = leilaoDao.disputadosEntre(2500, 3500);

        assertEquals(1, leiloes.size());
        assertEquals(3200.0, leiloes.get(0).getValorInicial(), 0.00001);
    }
	
	@Test
    public void listaSomenteOsLeiloesDoUsuario() throws Exception 
	{
        Usuario dono = new Usuario("Mauricio", "m@a.com");
        Usuario comprador = new Usuario("Victor", "v@v.com");
        Usuario comprador2 = new Usuario("Guilherme", "g@g.com");
        Leilao leilao = new LeilaoBuilder()
            .comDono(dono)
            .comValor(50.0)
            .comLance(Calendar.getInstance(), comprador, 100.0)
            .comLance(Calendar.getInstance(), comprador2, 200.0)
            .constroi();
        Leilao leilao2 = new LeilaoBuilder()
            .comDono(dono)
            .comValor(250.0)
            .comLance(Calendar.getInstance(), comprador2, 100.0)
            .constroi();
        usuarioDao.salvar(dono);
        usuarioDao.salvar(comprador);
        usuarioDao.salvar(comprador2);
        leilaoDao.salvar(leilao);
        leilaoDao.salvar(leilao2);

        List<Leilao> leiloes = leilaoDao.listaLeiloesDoUsuario(comprador);
        assertEquals(1, leiloes.size());
        assertEquals(leilao, leiloes.get(0));
    }
	
	
	@Test
    public void listaDeLeiloesDeUmUsuarioNaoTemRepeticao() throws Exception {
        Usuario dono = new Usuario("Mauricio", "m@a.com");
        Usuario comprador = new Usuario("Victor", "v@v.com");
        Leilao leilao = new LeilaoBuilder()
            .comDono(dono)
            .comLance(Calendar.getInstance(), comprador, 100.0)
            .comLance(Calendar.getInstance(), comprador, 200.0)
            .constroi();
        usuarioDao.salvar(dono);
        usuarioDao.salvar(comprador);
        leilaoDao.salvar(leilao);

        double leiloes = leilaoDao.getValorInicialMedioDoUsuario(comprador);
        assertEquals(1.0D, leiloes);
    }
	
}
