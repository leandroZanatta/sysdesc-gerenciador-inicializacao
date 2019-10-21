package br.com.sysdesc.gerenciador.inicializacao.listener;

import java.util.EventListener;

import br.com.sysdesc.util.vo.ConfigurationVO;

public interface InicializacaoModulosListener extends EventListener {

	public default void configuracaoModulosChanged(ConfigurationVO configuracao) {
	};

	public default void moduloStopped(Long codigoModulo) {
	};

	public default void moduloStarted(Long codigoModulo) {
	};

	public default void moduloStarting(Long codigoModulo) {
	};

}
