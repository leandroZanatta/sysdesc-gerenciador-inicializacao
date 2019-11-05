package br.com.sysdesc.gerenciador.inicializacao.util;

import java.util.Properties;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.jtattoo.plaf.mint.MintLookAndFeel;

public class LookAndFeelUtil {

	public static void configureLayout() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException {
		configureLayout("Times New Roman plain 14");
	}

	public static void configureLayout(String fonte) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException {

		Properties p = new Properties();
		p.put("windowTitleFont", fonte);
		p.put("logoString", "");
		p.put("windowDecoration", "off");
		p.setProperty("controlTextFont", fonte);
		p.setProperty("systemTextFont", fonte);
		p.setProperty("userTextFont", fonte);
		p.setProperty("menuTextFont", fonte);
		p.setProperty("windowTitleFont", fonte);
		p.setProperty("subTextFont", fonte);

		MintLookAndFeel.setCurrentTheme(p);

		UIManager.setLookAndFeel("com.jtattoo.plaf.mint.MintLookAndFeel");
	}
}
