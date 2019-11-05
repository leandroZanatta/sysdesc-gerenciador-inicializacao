package br.com.sysdesc.gerenciador.inicializacao.statup;

import br.com.sysdesc.gerenciador.inicializacao.ui.FrmPrincipal;
import br.com.sysdesc.gerenciador.inicializacao.util.LookAndFeelUtil;
import br.com.sysdesc.http.server.JavaHTTPServer;
import br.com.sysdesc.util.resources.ApplicationProperies;

public class StartUp {

	public static void main(String[] args) throws Exception {

		LookAndFeelUtil.configureLayout();

		Integer porta = Integer
				.valueOf(ApplicationProperies.getInstance().getProperty("gerenciador.server.port", "3100"));

		new JavaHTTPServer(porta, "br.com.sysdesc.gerenciador.inicializacao.controller").start();

		new FrmPrincipal().setVisible(Boolean.TRUE);
	}

}
