package br.com.sysdesc.gerenciador.inicializacao.ftp.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import br.com.sysdesc.util.resources.ApplicationProperies;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FTPClient {

	private final String ip;

	public FTPClient(String ip) {
		this.ip = ip;
	}

	public Boolean getFile(String caminho, String arquivo) {

		Integer porta = Integer.valueOf(ApplicationProperies.getInstance().getProperty("ftp.server.port", "3210"));

		try (Socket client = new Socket(ip, porta);
				DataInputStream input = new DataInputStream(client.getInputStream());
				DataOutputStream output = new DataOutputStream(client.getOutputStream())) {

			System.out.println(input.readUTF());

			output.writeUTF(arquivo);

			try (ObjectInputStream in = new ObjectInputStream(client.getInputStream())) {

				String fileName = in.readUTF();

				if (fileName != null) {

					long size = in.readLong();

					log.info("Processando arquivo: " + fileName + " - " + size + " bytes.");

					File file = new File(caminho);

					if (file.exists() == false) {
						file.mkdir();
					}

					File ouput = new File(file, fileName);

					try (FileOutputStream fos = new FileOutputStream(ouput)) {

						byte[] buf = new byte[4096];

						while (true) {
							int len = in.read(buf);
							if (len == -1)
								break;

							fos.write(buf, 0, len);
						}

						fos.flush();

						return Boolean.TRUE;
					}
				}
			}

			log.info(input.readUTF());

		} catch (IOException e) {

			log.error("Não foi possivel inicializar o servidor", e);
		}
		return Boolean.FALSE;
	}
}
