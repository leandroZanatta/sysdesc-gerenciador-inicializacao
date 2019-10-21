package br.com.sysdesc.gerenciador.inicializacao.intercomm.builder;

import feign.Feign;
import feign.Feign.Builder;
import feign.Retryer;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.slf4j.Slf4jLogger;

public class RequestBuilder {

	public static Builder build() {

		return Feign.builder().retryer(Retryer.NEVER_RETRY).encoder(new GsonEncoder()).decoder(new GsonDecoder())
				.logger(new Slf4jLogger());
	}
}
