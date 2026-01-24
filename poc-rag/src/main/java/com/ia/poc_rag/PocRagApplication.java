package com.ia.poc_rag;

import com.ia.poc_rag.config.properties.AIProperties;
import com.ia.poc_rag.config.properties.DataLoaderProperties;
import com.ia.poc_rag.config.properties.RhProperties;
import com.ia.poc_rag.config.properties.TavilyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({DataLoaderProperties.class, RhProperties.class, AIProperties.class, TavilyProperties.class})
@SpringBootApplication
public class PocRagApplication {

	public static void main(String[] args) {
		SpringApplication.run(PocRagApplication.class, args);
	}

}
