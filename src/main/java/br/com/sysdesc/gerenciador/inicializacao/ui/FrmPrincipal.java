package br.com.sysdesc.gerenciador.inicializacao.ui;

import static br.com.sysdesc.gerenciador.inicializacao.util.GerenciadorInicializacaoResources.ERRO_ABRINDO_FRONTEND;
import static br.com.sysdesc.gerenciador.inicializacao.util.GerenciadorInicializacaoResources.ERRO_INICIANDO_MODULOS;
import static br.com.sysdesc.gerenciador.inicializacao.util.GerenciadorInicializacaoResources.ERRO_PARANDO_MODULOS;
import static br.com.sysdesc.gerenciador.inicializacao.util.GerenciadorInicializacaoResources.GERENCIAMENTO_MODULOS;
import static br.com.sysdesc.util.resources.Resources.translate;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;

import br.com.sysdesc.gerenciador.inicializacao.adapter.InicializacaoModulosPrincipalAdapter;
import br.com.sysdesc.gerenciador.inicializacao.service.InicializacaoModulosService;
import br.com.sysdesc.gerenciador.inicializacao.service.impl.InicializacaoModulosServiceIpml;
import br.com.sysdesc.util.classes.ListUtil;
import br.com.sysdesc.util.resources.ApplicationProperies;
import br.com.sysdesc.util.vo.InicializacaoModulosVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FrmPrincipal extends JFrame {

	private static final String GERENCIADOR_PDV = "Gerenciador do PDV";

	private static final long serialVersionUID = 1L;

	private static final String ABRIR_PDV_AUTOMATICAMENTE = "Abrir PDV automaticamente";
	private static final String PARAR_SERVICOS = "3 - Parar Serviços";
	private static final String ABRIR_PDV = "1 - Abrir PDV";
	private static final String REINICIAR_SERVICOS = "2 - Reiniciar Serviços";

	private JPanel contentPane;
	private JButton btAbrirPDV;
	private JButton btReiniciar;
	private JButton btCancelar;
	private JCheckBox chIniciarPDV;
	private JTextPane txLogServicos;
	private JScrollPane scrollPane;

	private transient InicializacaoModulosService inicializacaoModulosService = InicializacaoModulosServiceIpml
			.getInstance();

	public FrmPrincipal() {

		initComponents();
	}

	private void initComponents() {

		contentPane = new JPanel();

		btAbrirPDV = new JButton(ABRIR_PDV);
		btReiniciar = new JButton(REINICIAR_SERVICOS);
		btCancelar = new JButton(PARAR_SERVICOS);
		chIniciarPDV = new JCheckBox(ABRIR_PDV_AUTOMATICAMENTE);
		txLogServicos = new JTextPane();
		scrollPane = new JScrollPane(VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_NEVER);

		txLogServicos.setEditable(Boolean.FALSE);
		scrollPane.setViewportView(txLogServicos);
		contentPane.setLayout(null);

		btAbrirPDV.setBounds(52, 36, 134, 23);
		btReiniciar.setBounds(190, 36, 131, 23);
		btCancelar.setBounds(325, 36, 117, 23);
		chIniciarPDV.setBounds(7, 63, 157, 23);
		scrollPane.setBounds(7, 90, 480, 124);

		contentPane.add(btAbrirPDV);
		contentPane.add(btReiniciar);
		contentPane.add(btCancelar);
		contentPane.add(chIniciarPDV);
		contentPane.add(scrollPane);
		setContentPane(contentPane);

		this.adicionarAcoesBotoes();

		setTitle(GERENCIADOR_PDV);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(500, 250);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {

				pararModulos();

				System.exit(0);
			}
		});

		inicializacaoModulosService
				.addInicializacaoModulosListener(new InicializacaoModulosPrincipalAdapter(txLogServicos) {

					@Override
					protected void atualizarBotoes() {

						btAbrirPDV.setEnabled(inicializacaoModulosVOs.stream()
								.anyMatch(modulo -> modulo.getIsFrontEnd() && !modulo.getIsStarted()));

						chIniciarPDV.setEnabled(
								inicializacaoModulosVOs.stream().anyMatch(InicializacaoModulosVO::getIsFrontEnd));

						Boolean modulosAtivos = inicializacaoModulosVOs.stream()
								.anyMatch(InicializacaoModulosVO::getIsStarted);

						btReiniciar.setText(modulosAtivos ? REINICIAR_SERVICOS : "2 - Iniciar Serviços");
						btReiniciar.setEnabled(!ListUtil.isNullOrEmpty(inicializacaoModulosVOs));
						btCancelar.setEnabled(modulosAtivos);
					}
				});

		this.iniciarModulos();
	}

	private void adicionarAcoesBotoes() {

		JRootPane rootPane = contentPane.getRootPane();

		InputMap imap = rootPane.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);

		String mapAbrirPDV = "mapAbrirPDV";
		String mapReiniciar = "mapReiniciar";
		String mapParar = "mapParar";

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1, 0), mapAbrirPDV);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, 0), mapReiniciar);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD3, 0), mapParar);

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0), mapAbrirPDV);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0), mapReiniciar);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_3, 0), mapParar);

		Action actionAbrirPDV = new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				abrirPDV();
			}
		};

		Action actionReiniciar = new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				reiniciarModulos();
			}
		};

		Action actionParar = new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				pararModulos();
			}
		};

		Boolean inicializacaoAutomatica = Boolean.valueOf(ApplicationProperies.getInstance()
				.getProperty(ApplicationProperies.INICIALIZACAO_AUTOMATICA_FRONTEND, Boolean.TRUE.toString()));

		btAbrirPDV.addActionListener(actionAbrirPDV);
		btReiniciar.addActionListener(actionReiniciar);
		btCancelar.addActionListener(actionParar);
		chIniciarPDV.addActionListener(e -> alterarPropriedadeInicialiacao());

		chIniciarPDV.setSelected(inicializacaoAutomatica);

		ActionMap actionMap = new ActionMap();

		actionMap.put(mapAbrirPDV, actionAbrirPDV);
		actionMap.put(mapReiniciar, actionReiniciar);
		actionMap.put(mapParar, actionParar);

		rootPane.setActionMap(actionMap);
	}

	private void alterarPropriedadeInicialiacao() {

		String inicializacaoAutomatica = String.valueOf(chIniciarPDV.isSelected());

		ApplicationProperies.getInstance().setPropertie(ApplicationProperies.INICIALIZACAO_AUTOMATICA_FRONTEND,
				inicializacaoAutomatica);
	}

	private void abrirPDV() {

		if (btAbrirPDV.isEnabled()) {

			try {

				inicializacaoModulosService.abrirFrontEnds();

			} catch (Exception e) {

				log.error(translate(ERRO_ABRINDO_FRONTEND), e);

				JOptionPane.showMessageDialog(this, translate(ERRO_ABRINDO_FRONTEND), translate(GERENCIAMENTO_MODULOS),
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void reiniciarModulos() {

		if (btReiniciar.isEnabled()) {

			pararModulos();

			iniciarModulos();

		}
	}

	private void pararModulos() {

		txLogServicos.setText("");

		if (btCancelar.isEnabled()) {

			try {

				inicializacaoModulosService.pararModulos();

			} catch (Exception e) {

				log.error(translate(ERRO_PARANDO_MODULOS), e);

				JOptionPane.showMessageDialog(this, translate(ERRO_PARANDO_MODULOS), translate(GERENCIAMENTO_MODULOS),
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void iniciarModulos() {

		new Thread(() -> {

			try {

				inicializacaoModulosService.iniciarModulos();

			} catch (Exception e) {

				log.error(translate(ERRO_INICIANDO_MODULOS), e);

				JOptionPane.showMessageDialog(this, translate(ERRO_INICIANDO_MODULOS), translate(GERENCIAMENTO_MODULOS),
						JOptionPane.ERROR_MESSAGE);
			}
		}).start();
	}

}
