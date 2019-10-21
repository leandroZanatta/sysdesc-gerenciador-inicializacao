package br.com.sysdesc.gerenciador.inicializacao.service;

import java.util.concurrent.ExecutionException;

import br.com.sysdesc.gerenciador.inicializacao.listener.InicializacaoModulosListener;

public interface InicializacaoModulosService {

	public abstract void addInicializacaoModulosListener(InicializacaoModulosListener listener);

	public abstract void removeInicializacaoModulosListener(InicializacaoModulosListener listener);

	public abstract void iniciarModulos() throws InterruptedException, ExecutionException;

	public abstract void pararModulos() throws InterruptedException, ExecutionException;

	public abstract void abrirFrontEnds() throws InterruptedException, ExecutionException;

}
