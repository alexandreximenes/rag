package com.ia.poc_rag;

import com.ia.poc_rag.config.AIProperties;
import com.ia.poc_rag.config.DataLoaderProperties;
import com.ia.poc_rag.config.RhProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({DataLoaderProperties.class, RhProperties.class, AIProperties.class})
@SpringBootApplication
public class PocRagApplication {

	public static void main(String[] args) {
		SpringApplication.run(PocRagApplication.class, args);
	}

}
