package br.com.sysdesc.gerenciador.inicializacao.intercomm;

import br.com.sysdesc.util.vo.ConfigurationVO;
import feign.Param;
import feign.RequestLine;

public interface ConfiguracaoModulosClient {

	@RequestLine("GET /configuracaoModulos/buscarConfiguracaoModulos?ipGerenciador={ipGerenciador}")
	public abstract ConfigurationVO buscarConfiguracaoModulos(@Param("ipGerenciador") String ipGerenciador);
}
