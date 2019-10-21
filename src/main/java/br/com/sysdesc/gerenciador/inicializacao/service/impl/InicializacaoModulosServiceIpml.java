package br.com.sysdesc.gerenciador.inicializacao.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.swing.event.EventListenerList;

import br.com.sysdesc.gerenciador.inicializacao.listener.InicializacaoModulosListener;
import br.com.sysdesc.gerenciador.inicializacao.service.ConfiguracaoModulosService;
import br.com.sysdesc.gerenciador.inicializacao.service.InicializacaoModulosService;
import br.com.sysdesc.gerenciador.inicializacao.thread.ModuleFactory;
import br.com.sysdesc.util.classes.ListUtil;
import br.com.sysdesc.util.resources.ApplicationProperies;
import br.com.sysdesc.util.vo.ConfigurationVO;
import br.com.sysdesc.util.vo.ModuleVO;
import br.com.sysdesc.util.vo.ServerVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InicializacaoModulosServiceIpml implements InicializacaoModulosService {

	private static final InicializacaoModulosService INSTANCE = new InicializacaoModulosServiceIpml();

	protected EventListenerList listenerList = new EventListenerList();

	private ExecutorService executor = Executors.newCachedThreadPool();

	private Map<Long, Future<ModuleVO>> modulos = new HashMap<>();

	private ConfiguracaoModulosService configuracaoModulosService = new ConfiguracaoModulosServiceImpl();

	private InicializacaoModulosServiceIpml() {
	}

	@Override
	public void addInicializacaoModulosListener(InicializacaoModulosListener listener) {

		listenerList.add(InicializacaoModulosListener.class, listener);
	}

	@Override
	public void removeInicializacaoModulosListener(InicializacaoModulosListener listener) {

		listenerList.remove(InicializacaoModulosListener.class, listener);
	}

	public static InicializacaoModulosService getInstance() {

		return INSTANCE;
	}

	@Override
	public void iniciarModulos() throws InterruptedException, ExecutionException {

		ConfigurationVO configuracoes = configuracaoModulosService.buscarConfiguracaoModulos();

		fireConfiguracaoModulosChanged(configuracoes);

		Boolean inicializacaoAutomatica = Boolean.valueOf(ApplicationProperies.getInstance()
				.getProperty(ApplicationProperies.INICIALIZACAO_AUTOMATICA_FRONTEND, Boolean.TRUE.toString()));

		if (!ListUtil.isNullOrEmpty(configuracoes.getFrontEnds()) && inicializacaoAutomatica) {

			this.inicializarListaModulos(configuracoes.getFrontEnds());
		}

		this.inicializarListaModulos(configuracoes.getServers());
	}

	@Override
	public void pararModulos() throws InterruptedException, ExecutionException {

		for (Entry<Long, Future<ModuleVO>> modulo : modulos.entrySet()) {

			this.encerrarModulo(modulo.getKey(), modulo.getValue());
		}

		modulos.clear();
	}

	@Override
	public void abrirFrontEnds() throws InterruptedException, ExecutionException {

		List<ServerVO> frontends = configuracaoModulosService.buscarConfiguracaoModulos().getFrontEnds().stream()
				.collect(Collectors.toList());

		if (!ListUtil.isNullOrEmpty(frontends)) {

			this.inicializarListaModulos(frontends);
		}
	}

	private void inicializarListaModulos(List<ServerVO> serverModel) throws InterruptedException, ExecutionException {

		if (!ListUtil.isNullOrEmpty(serverModel)) {

			List<ServerVO> execucoesIndependentes = serverModel.stream()
					.filter(x -> ListUtil.isNullOrEmpty(x.getDependsOn())).collect(Collectors.toList());

			this.pararEIniciarProcessos(execucoesIndependentes);

			List<ServerVO> execucoesDependentes = serverModel.stream()
					.filter(x -> !ListUtil.isNullOrEmpty(x.getDependsOn())).collect(Collectors.toList());

			this.pararEIniciarProcessos(execucoesDependentes);

		}
	}

	private void pararEIniciarProcessos(List<ServerVO> execucoes) throws InterruptedException, ExecutionException {

		if (!ListUtil.isNullOrEmpty(execucoes)) {

			for (ServerVO execucao : execucoes) {
				this.validarDependsOnOutrosProjetos(execucao);
			}
		}
	}

	private void validarDependsOnOutrosProjetos(ServerVO modulo) throws InterruptedException, ExecutionException {

		if (modulos.containsKey(modulo.getId())) {

			this.encerrarModulo(modulo.getId(), modulos.get(modulo.getId()));
		}

		if (ListUtil.isNullOrEmpty(modulo.getDependsOn())) {

			modulos.put(modulo.getId(), executeTread(modulo));

		} else {

			this.iniciarProcessoDependsOn(modulo);
		}

	}

	private void iniciarProcessoDependsOn(ServerVO modulo) {

		new Thread(() -> this.iniciarExecucaoSincrona(modulo)).start();
	}

	private void iniciarExecucaoSincrona(ServerVO modulo) {

		try {

			for (Long moduloStartado : modulo.getDependsOn()) {

				if (modulos.containsKey(moduloStartado)) {

					tornarThreadsDoProcessoSincrono(moduloStartado);
				}
			}

			modulos.put(modulo.getId(), executeTread(modulo));

		} catch (Exception e) {

			log.error("", e);
		}
	}

	private void tornarThreadsDoProcessoSincrono(Long moduloStartado) throws InterruptedException, ExecutionException {

		Future<ModuleVO> moduleExecution = modulos.get(moduloStartado);

		if (!moduleExecution.isDone()) {

			moduleExecution.get();

		}
	}

	private Future<ModuleVO> executeTread(ServerVO modulo) {

		return executor.submit(new ModuleFactory(modulo, listenerList));
	}

	private void encerrarModulo(Long codigoModulo, Future<ModuleVO> modulo)
			throws InterruptedException, ExecutionException {

		if (!modulo.isDone() && !modulo.isCancelled()) {

			modulo.cancel(Boolean.TRUE);
		}

		if (!modulo.isCancelled() && modulo.get() != null) {

			ModuleVO moduloVo = modulo.get();

			if (moduloVo.getFechamentoExterno() != null) {

				moduloVo.getFechamentoExterno().interrupt();
			}

			moduloVo.getProcess().destroyForcibly();
		}

		fireModuloStopped(codigoModulo);

	}

	private void fireConfiguracaoModulosChanged(ConfigurationVO configuracao) {

		Object[] listeners = listenerList.getListenerList();

		for (int i = 0; i < listeners.length; i = i + 2) {

			if (listeners[i] == InicializacaoModulosListener.class) {

				((InicializacaoModulosListener) listeners[i + 1]).configuracaoModulosChanged(configuracao);
			}
		}
	}

	private void fireModuloStopped(Long codigoModulo) {

		Object[] listeners = listenerList.getListenerList();

		for (int i = 0; i < listeners.length; i = i + 2) {

			if (listeners[i] == InicializacaoModulosListener.class) {

				((InicializacaoModulosListener) listeners[i + 1]).moduloStopped(codigoModulo);
			}
		}
	}

}
