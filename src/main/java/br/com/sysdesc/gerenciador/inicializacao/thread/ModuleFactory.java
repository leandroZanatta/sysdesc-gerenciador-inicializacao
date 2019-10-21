package br.com.sysdesc.gerenciador.inicializacao.thread;

import static br.com.sysdesc.gerenciador.inicializacao.util.GerenciadorInicializacaoLogConstants.ERRO_EXCLUIR_ARQUIVO_LOG;
import static br.com.sysdesc.gerenciador.inicializacao.util.GerenciadorInicializacaoLogConstants.ERRO_EXECUTAR_PROCESSO;
import static br.com.sysdesc.gerenciador.inicializacao.util.GerenciadorInicializacaoLogConstants.PROCESSO_PARADO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import javax.swing.event.EventListenerList;

import br.com.sysdesc.gerenciador.inicializacao.listener.InicializacaoModulosListener;
import br.com.sysdesc.gerenciador.inicializacao.util.GerenciadorInicializacaoResources;
import br.com.sysdesc.util.classes.LongUtil;
import br.com.sysdesc.util.classes.ProcessUtil;
import br.com.sysdesc.util.classes.StringUtil;
import br.com.sysdesc.util.vo.ModuleVO;
import br.com.sysdesc.util.vo.ServerVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ModuleFactory implements Callable<ModuleVO> {

	private final ServerVO pdvVendaServerModel;

	private final EventListenerList listenerList;

	public ModuleFactory(ServerVO pdvVendaServerModel, EventListenerList listenerList) {

		this.listenerList = listenerList;
		this.pdvVendaServerModel = pdvVendaServerModel;
	}

	@Override
	public ModuleVO call() {

		fireModuloStarting();

		File logFile = null;

		try {

			logFile = File.createTempFile("log", "tmp");

			return this.executarProcesso(logFile);

		} catch (IOException e) {

			log.error(ERRO_EXECUTAR_PROCESSO, e);

			Thread.currentThread().interrupt();

		} finally {

			if (logFile != null && !logFile.delete()) {

				log.info(ERRO_EXCLUIR_ARQUIVO_LOG);
			}
		}

		return null;

	}

	private void validarFechamentoProcesso(Process process) {

		try {

			process.waitFor();

			process.destroyForcibly();

			fireModuloStopped();

		} catch (InterruptedException e) {

			process.destroyForcibly();

			Thread.currentThread().interrupt();

			log.info(PROCESSO_PARADO, e);
		}
	}

	private ModuleVO executarProcesso(File logFile) {

		Process process;

		Thread threadFechamentoExterno = null;

		try (FileInputStream fileInputStream = new FileInputStream(logFile);
				BufferedReader input = new BufferedReader(new InputStreamReader(fileInputStream))) {

			process = ProcessUtil.createProcess(this.pdvVendaServerModel.getComand(), logFile,
					this.pdvVendaServerModel.getDirectory());

			threadFechamentoExterno = new Thread(() -> this.validarFechamentoProcesso(process));

			threadFechamentoExterno.start();

			if (LongUtil.isNullOrZero(this.pdvVendaServerModel.getMaximumExecutionTime())
					|| StringUtil.isNullOrEmpty(this.pdvVendaServerModel.getMsgValidacaoStart())) {

				fireModuloStared();

				return new ModuleVO(process, threadFechamentoExterno);
			}

			Long timeExecution = 0L;

			String line;

			while (true) {

				while ((line = input.readLine()) != null) {

					if (Thread.currentThread().isInterrupted()) {

						pararProcesso(process, threadFechamentoExterno);

						return null;
					}

					if (line.contains(this.pdvVendaServerModel.getMsgValidacaoStart())) {

						fireModuloStared();

						return new ModuleVO(process, threadFechamentoExterno);

					}
				}

				Thread.sleep(1000);

				timeExecution++;

				if (timeExecution.equals(this.pdvVendaServerModel.getMaximumExecutionTime())) {

					pararProcesso(process, threadFechamentoExterno);

					return null;
				}
			}

		} catch (InterruptedException e) {

			if (threadFechamentoExterno != null) {

				threadFechamentoExterno.interrupt();
			}

			Thread.currentThread().interrupt();

			log.info(PROCESSO_PARADO, e);

		} catch (IOException e) {

			if (threadFechamentoExterno != null) {

				threadFechamentoExterno.interrupt();
			}

			fireModuloStopped();

			log.error(GerenciadorInicializacaoResources.ERRO_INICIANDO_MODULOS, e);
		}

		return null;
	}

	private void pararProcesso(Process process, Thread thread) {

		thread.interrupt();

		process.destroyForcibly();
	}

	private void fireModuloStopped() {

		Object[] listeners = listenerList.getListenerList();

		for (int i = 0; i < listeners.length; i = i + 2) {

			if (listeners[i] == InicializacaoModulosListener.class) {

				((InicializacaoModulosListener) listeners[i + 1]).moduloStopped(pdvVendaServerModel.getId());
			}
		}
	}

	private void fireModuloStared() {

		Object[] listeners = listenerList.getListenerList();

		for (int i = 0; i < listeners.length; i = i + 2) {

			if (listeners[i] == InicializacaoModulosListener.class) {

				((InicializacaoModulosListener) listeners[i + 1]).moduloStarted(pdvVendaServerModel.getId());
			}
		}
	}

	private void fireModuloStarting() {

		Object[] listeners = listenerList.getListenerList();

		for (int i = 0; i < listeners.length; i = i + 2) {

			if (listeners[i] == InicializacaoModulosListener.class) {

				((InicializacaoModulosListener) listeners[i + 1]).moduloStarting(pdvVendaServerModel.getId());
			}
		}
	}
}
