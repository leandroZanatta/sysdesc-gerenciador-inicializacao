package br.com.sysdesc.gerenciador.inicializacao.service;

import java.util.List;

import br.com.sysdesc.util.vo.ConfigurationVO;
import br.com.sysdesc.util.vo.IPVO;

public interface ConfiguracaoModulosService {

	public ConfigurationVO buscarConfiguracaoModulos();

	public void salvarConfiguracaoModulos(ConfigurationVO configurationVO);

	public ConfigurationVO buscarConfiguracoesGerenciadorERP(List<IPVO> ipvos);

	public Boolean salvarConfiguracaoIPERP(List<IPVO> ipvos);

	public Boolean atualizarConfiguracao(List<IPVO> ipvos);
}
