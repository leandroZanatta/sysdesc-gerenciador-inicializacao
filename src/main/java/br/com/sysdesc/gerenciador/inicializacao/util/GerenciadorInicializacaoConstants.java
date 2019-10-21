package br.com.sysdesc.gerenciador.inicializacao.util;

import java.io.File;

public class GerenciadorInicializacaoConstants {

	private static final String FOLDER_USER = System.getProperty("user.dir");
	private static final String SEPARATOR = File.separator;

	public static final String FOLDER_CONFIG = FOLDER_USER + SEPARATOR + "config";
	public static final String FILE_SERVER_JSON = FOLDER_CONFIG + SEPARATOR + "server.json";
	public static final String FILE_NETWORK_JSON = FOLDER_CONFIG + SEPARATOR + "network.json";
	public static final String FILE_VALIDACAO_CONFIGURACOES_JSON = FOLDER_CONFIG + SEPARATOR
			+ "validacaoConfiguracoes.json";

	private GerenciadorInicializacaoConstants() {
	}

}
