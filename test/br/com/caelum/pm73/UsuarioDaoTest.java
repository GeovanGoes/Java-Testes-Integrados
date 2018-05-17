/**
 * 
 */
package br.com.caelum.pm73;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import br.com.caelum.pm73.dao.CriadorDeSessao;
import br.com.caelum.pm73.dao.UsuarioDao;
import br.com.caelum.pm73.dominio.Usuario;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author geovan.goes
 *
 */
public class UsuarioDaoTest
{

	private Session session;
	private UsuarioDao dao;

	@Before
	public void antes()
	{
		session = new CriadorDeSessao().getSession();
		dao = new UsuarioDao(session);
		session.beginTransaction();
	}

	@After
	public void depois()
	{
		session.getTransaction().rollback();
		session.close();
	}

	@Test
	public void deveEncontrarPeloNomeEmail()
	{
		Usuario novoUsuario = new Usuario("Joao da Silva", "joao@dasilva.com.br");

		dao.salvar(novoUsuario);

		Usuario porNomeEEmail = dao.porNomeEEmail("Joao da Silva", "joao@dasilva.com.br");

		assertEquals("Joao da Silva", porNomeEEmail.getNome());
		assertEquals("joao@dasilva.com.br", porNomeEEmail.getEmail());
	}

	@Test
	public void deveRetornarNuloSeNaoEncontrarUsuario()
	{
		Session session = new CriadorDeSessao().getSession();
		UsuarioDao usuarioDao = new UsuarioDao(session);

		Usuario usuarioDoBanco = usuarioDao.porNomeEEmail("João Joaquim", "joao@joaquim.com.br");

		assertNull(usuarioDoBanco);
	}

	@Test
	public void deveDeletarUsuario()
	{
		Usuario usuario = new Usuario("Joao da Silva", "joao@dasilva.com.br");

		dao.salvar(usuario);
		dao.deletar(usuario);

		session.flush();
		session.clear();

		Usuario deletado = dao.porNomeEEmail("Joao da Silva", "joao@dasilva.com.br");

		assertNull(deletado);
	}

	@Test
	public void deveAlterarUmUsuario()
	{
		Usuario usuario = new Usuario("Mauricio Aniche", "mauricio@aniche.com.br");

		dao.salvar(usuario);

		usuario.setNome("João da Silva");
		usuario.setEmail("joao@silva.com.br");

		dao.atualizar(usuario);

		session.flush();

		Usuario novoUsuario = dao.porNomeEEmail("João da Silva", "joao@silva.com.br");
		assertNotNull(novoUsuario);
		System.out.println(novoUsuario);

		Usuario usuarioInexistente = dao.porNomeEEmail("Mauricio Aniche", "mauricio@aniche.com.br");
		assertNull(usuarioInexistente);

	}
}
