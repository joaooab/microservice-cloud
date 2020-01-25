package br.com.alura.microservice.fornecedor;

import br.com.alura.microservice.fornecedor.model.Produto;
import br.com.alura.microservice.fornecedor.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PopulaBanco implements ApplicationRunner {

	@Autowired
	private ProdutoRepository produtoRepository;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		produtoRepository.save(new Produto(1L, "rosas", "df", "rosas", new BigDecimal(2)));
		produtoRepository.save(new Produto(2L, "orquideas", "df", "orquideas", new BigDecimal(25)));
		produtoRepository.save(new Produto(3L, "girassol", "df", "girassol", new BigDecimal(5)));
	}
}
