package br.com.sysdesc.gerenciador.inicializacao.service.impl;

import static br.com.sysdesc.gerenciador.inicializacao.util.GerenciadorInicializacaoConstants.FILE_NETWORK_JSON;
import static br.com.sysdesc.gerenciador.inicializacao.util.GerenciadorInicializacaoConstants.FILE_SERVER_JSON;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import br.com.sysdesc.gerenciador.inicializacao.ftp.client.FTPClient;
import br.com.sysdesc.gerenciador.inicializacao.intercomm.ConfiguracaoModulosClient;
import br.com.sysdesc.gerenciador.inicializacao.intercomm.builder.RequestBuilder;
import br.com.sysdesc.gerenciador.inicializacao.service.ConfiguracaoModulosService;
import br.com.sysdesc.gerenciador.inicializacao.util.ExtratorZip;
import br.com.sysdesc.gerenciador.inicializacao.util.GerenciadorInicializacaoLogConstants;
import br.com.sysdesc.util.classes.IPUtil;
import br.com.sysdesc.util.classes.ListUtil;
import br.com.sysdesc.util.resources.Configuracoes;
import br.com.sysdesc.util.vo.ConfigurationVO;
import br.com.sysdesc.util.vo.IPVO;
import br.com.sysdesc.util.vo.ServerVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfiguracaoModulosServiceImpl implements ConfiguracaoModulosService {

	private static final String HTTP_REQUEST = "http://%s:%d";
	private Charset charset = StandardCharsets.UTF_8;

	private ExtratorZip extratorZip = new ExtratorZip();

	@Override
	public ConfigurationVO buscarConfiguracaoModulos() {
		log.info("==>Executando o método buscarConfiguracaoModulos");

		ConfigurationVO configuracao = null;

		try (InputStream inputStream = new FileInputStream(new File(FILE_SERVER_JSON));

				InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset)) {

			configuracao = new Gson().fromJson(inputStreamReader, ConfigurationVO.class);

		} catch (Exception e) {

			log.info(GerenciadorInicializacaoLogConstants.CONFIGURACAO_MODULOS_NAO_ENCONTRADA + " - " + e.getMessage());

			configuracao = new ConfigurationVO();
		}

		return configuracao;
	}

	@Override
	public void salvarConfiguracaoModulos(ConfigurationVO configurationVO) {
		log.info("==>Executando o método salvarConfiguracaoModulos");

		try {

			String data = new Gson().toJson(configurationVO);

			FileUtils.writeStringToFile(new File(FILE_SERVER_JSON), data, charset);

		} catch (Exception e) {

			log.error(GerenciadorInicializacaoLogConstants.ERRO_SALVAR_CONFIGURACAO_MODULOS + " - " + e.getMessage());
		}

	}

	@Override
	public ConfigurationVO buscarConfiguracoesGerenciadorERP(List<IPVO> ipvos) {
		log.info("==>Executando o método buscarConfiguracoesGerenciadorERP");

		List<IPVO> ipsGerenciador = IPUtil.getIps();

		ConfigurationVO configurationVO = null;

		while (configurationVO == null) {

			for (IPVO ipRede : ipvos) {

				List<IPVO> ipsValidos = ipsGerenciador.stream()
						.filter(ipGerenciador -> IPUtil.isNetworkMatch(ipRede, ipGerenciador.getIp()))
						.collect(Collectors.toList());

				for (IPVO ipValido : ipsValidos) {

					String requisicao = String.format(HTTP_REQUEST, ipRede.getIp(), ipRede.getPorta());

					log.info("Tentando conectar ao endpoint: " + requisicao);

					configurationVO = RequestBuilder.build().target(ConfiguracaoModulosClient.class, requisicao)
							.buscarConfiguracaoModulos(ipValido.getIp());

					if (configurationVO != null) {

						return configurationVO;
					}
				}
			}

			try {
				Thread.sleep(5000);

			} catch (InterruptedException e) {

				log.error(
						GerenciadorInicializacaoLogConstants.ERRO_BUSCAR_CONFIGURACAO_MODULOS + " - " + e.getMessage());
			}
		}

		return null;
	}

	@Override
	public Boolean salvarConfiguracaoIPERP(List<IPVO> ipvos) {

		log.info("Salvando Configuração de ips de conexão do ERP");

		try {

			FileUtils.writeStringToFile(new File(FILE_NETWORK_JSON), new Gson().toJson(ipvos), charset);

		} catch (Exception e) {

			log.error(GerenciadorInicializacaoLogConstants.ERRO_SALVAR_CONFIGURACAO_MODULOS + " - " + e.getMessage());

			return Boolean.FALSE;
		}

		return Boolean.TRUE;
	}

	@Override
	public Boolean atualizarConfiguracao(List<IPVO> ipvos) {

		log.info("Atualizando Configuração de ips de conexão do ERP");

		if (!ListUtil.isNullOrEmpty(ipvos)) {

			List<IPVO> ipsCadastrados = buscarConfiguracaoIP();

			if (ListUtil.isNullOrEmpty(ipsCadastrados)) {

				atualizarConfiguracaoGerenciador(ipvos);
			}

			String strIp = new Gson().toJson(ipvos);

			if (!strIp.equals(new Gson().toJson(ipsCadastrados))) {

				return salvarConfiguracaoIPERP(ipvos);
			}

			return Boolean.FALSE;
		}

		return Boolean.FALSE;
	}

	private List<IPVO> buscarConfiguracaoIP() {
		log.info("==>Executando o método buscarConfiguracaoIP");

		List<IPVO> configuracao = null;

		try (InputStream inputStream = new FileInputStream(new File(FILE_NETWORK_JSON));

				InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.defaultCharset())) {

			configuracao = new Gson().fromJson(inputStreamReader,
					TypeToken.getParameterized(List.class, IPVO.class).getType());

		} catch (Exception e) {

			log.info(GerenciadorInicializacaoLogConstants.CONFIGURACAO_NETWORK_NAO_ENCONTRADA + " - " + e.getMessage());

			configuracao = new ArrayList<>();
		}

		return configuracao;
	}

	private void atualizarConfiguracaoGerenciador(List<IPVO> ipvos) {
		log.info("==>Executando o método atualizarConfiguracaoGerenciador");

		ConfigurationVO configurationVO = buscarConfiguracoesGerenciadorERP(ipvos);

		if (configurationVO != null) {

			salvarConfiguracaoModulos(configurationVO);

			efetuarDownloadModulos(ipvos, configurationVO);
			try {

				InicializacaoModulosServiceIpml.getInstance().iniciarModulos();

			} catch (InterruptedException | ExecutionException e) {

				log.error("erro ao inicializar módulos", e);
			}
		}

	}

	private void efetuarDownloadModulos(List<IPVO> ipvos, ConfigurationVO configurationVO) {
		log.info("==>Executando o método efetuarDownloadModulos");

		File pastaVersoes = new File(Configuracoes.FOLDER_VERSOES);

		Map<String, Boolean> zipFiles = buscarArquivosConfiguracao(configurationVO);

		for (IPVO ipErp : ipvos) {

			if (efetuarDownloadArquivo(zipFiles, pastaVersoes, ipErp)) {
				break;
			}
		}

		descompactarArquivos(configurationVO);

	}

	private void descompactarArquivos(ConfigurationVO configurationVO) {
		log.info("==>Executando o método descompactarArquivos");

		descompactar(configurationVO.getFrontEnds());

		descompactar(configurationVO.getServers());

	}

	private void descompactar(List<ServerVO> servers) {
		log.info("==>Executando o método descompactar");

		File path = new File(Configuracoes.FOLDER_VERSOES);

		for (ServerVO server : servers) {

			try {
				File arquivo = new File(path, server.getZipFile());

				if (arquivo.exists()) {

					File diretorio = new File(server.getDirectory());

					FileUtils.deleteDirectory(diretorio);

					diretorio.mkdirs();

					extratorZip.extrairVersao(diretorio, arquivo);
				}
			} catch (Exception e) {
				log.error("Não foi possivel descompactar a versão", e);
			}
		}
	}

	private Map<String, Boolean> buscarArquivosConfiguracao(ConfigurationVO configurationVO) {
		log.info("==>Executando o método buscarArquivosConfiguracao");

		Map<String, Boolean> mapaDownloads = new HashMap<>();

		configurationVO.getFrontEnds().stream().map(ServerVO::getZipFile)
				.forEach(zip -> mapaDownloads.put(zip, Boolean.FALSE));

		configurationVO.getServers().stream().map(ServerVO::getZipFile)
				.forEach(zip -> mapaDownloads.put(zip, Boolean.FALSE));

		return mapaDownloads;
	}

	private Boolean efetuarDownloadArquivo(Map<String, Boolean> zipFiles, File pastaVersoes, IPVO ipErp) {
		log.info("==>Executando o método efetuarDownloadArquivo");
		try {

			for (Entry<String, Boolean> file : zipFiles.entrySet()) {

				if (!file.getValue()) {

					new FTPClient(ipErp.getIp()).getFile(pastaVersoes.getAbsolutePath(), file.getKey());
				}
			}

		} catch (Exception e) {

			return Boolean.FALSE;
		}

		return Boolean.TRUE;
	}

}
