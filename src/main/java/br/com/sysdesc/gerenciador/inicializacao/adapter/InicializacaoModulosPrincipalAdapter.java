package br.com.sysdesc.gerenciador.inicializacao.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.swing.JTextPane;

import br.com.sysdesc.gerenciador.inicializacao.listener.InicializacaoModulosListener;
import br.com.sysdesc.util.classes.DateUtil;
import br.com.sysdesc.util.vo.ConfigurationVO;
import br.com.sysdesc.util.vo.InicializacaoModulosVO;

public abstract class InicializacaoModulosPrincipalAdapter implements InicializacaoModulosListener {

	protected List<InicializacaoModulosVO> inicializacaoModulosVOs = new ArrayList<>();

	private JTextPane textPane;

	public InicializacaoModulosPrincipalAdapter(JTextPane textPane) {
		this.textPane = textPane;
	}

	@Override
	public void configuracaoModulosChanged(ConfigurationVO configuracao) {

		inicializacaoModulosVOs = new ArrayList<>();

		configuracao.getFrontEnds().stream().forEach(modulo -> inicializacaoModulosVOs
				.add(new InicializacaoModulosVO(modulo.getId(), modulo.getServerName(), Boolean.TRUE, Boolean.FALSE)));

		configuracao.getServers().stream().forEach(modulo -> inicializacaoModulosVOs
				.add(new InicializacaoModulosVO(modulo.getId(), modulo.getServerName(), Boolean.FALSE, Boolean.FALSE)));

		atualizarBotoes();
	}

	@Override
	public void moduloStarted(Long codigoModulo) {

		changeStatusModule(codigoModulo, Boolean.TRUE, "Módulo %s Inicializado");
	}

	@Override
	public void moduloStopped(Long codigoModulo) {

		changeStatusModule(codigoModulo, Boolean.FALSE, "Módulo %s parado");
	}

	@Override
	public void moduloStarting(Long codigoModulo) {

		changeStatusModule(codigoModulo, Boolean.TRUE, "Iniciando Módulo - %s");
	}

	private synchronized void changeStatusModule(Long codigoModulo, Boolean status, String template) {

		Optional<InicializacaoModulosVO> optional = inicializacaoModulosVOs.stream()
				.filter(modulo -> modulo.getCodigoModulo().equals(codigoModulo)).findFirst();

		if (optional.isPresent()) {

			atualizarComponente(String.format(template, optional.get().getServerName()));

			optional.get().setIsStarted(status);

			atualizarBotoes();
		}
	}

	private synchronized void atualizarComponente(String string) {

		StringBuilder stringBuilder = new StringBuilder(textPane.getText());

		stringBuilder.append(DateUtil.format(DateUtil.FORMATO_DD_MM_YYYY_HH_MM_SS, new Date())).append(" - ")
				.append(string).append("\n");

		textPane.setText(stringBuilder.toString());

	}

	protected abstract void atualizarBotoes();

}
