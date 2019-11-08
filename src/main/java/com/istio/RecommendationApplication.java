package com.istio;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;

@SpringBootApplication
public class RecommendationApplication
{
	private static final Logger logger = LoggerFactory.getLogger(RecommendationApplication.class);

	public static void main(String[] args)
	{
		SpringApplication app = new SpringApplication(RecommendationApplication.class);
		app.addListeners((e) ->
		{
			if (e instanceof ApplicationReadyEvent)
			{
				File file = new File(File.separatorChar + "var" + File.separatorChar + "ready");
				logger.info("Creating file at: " + file);
				if (!(file.exists()))
				{
					try
					{
						file.createNewFile();
						logger.info("created file: " + file);
					}
					catch (IOException e1)
					{
						logger.info("Creating file failed");
					}

				}
			}
		});
		app.run(args);
	}

}
