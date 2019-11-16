package com.istio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.annotation.Timed;
import io.opentracing.Tracer;

@RestController
public class RecommendationController
{
	private static final String RESPONSE_STRING_FORMAT = "recommendation v1 from '%s': %d\n";

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private Tracer tracer;

	/**
	 * Counter to help us see the lifecycle
	 */
	private int count = 0;

	/**
	 * Flag for throwing a 503 when enabled
	 */
	private boolean misbehave = false;

	private static final String HOSTNAME = System.getenv().getOrDefault("HOSTNAME", "unknown");

	@Timed(value = "recommendation.get.request", histogram = true, extraTags =
	{ "version", "1.0" }, percentiles =
	{ 0.95, 0.99 })

	@RequestMapping("/")
	public ResponseEntity<String> getRecommendations()
	{
		logger.info(">> getRecommendations() called");
		logger.info(">> Baggage retrieved from Customer for key user-agent: {}", tracer.activeSpan().getBaggageItem("user-agent"));
		count++;
		logger.debug(String.format(">> recommendation from %s: %d", HOSTNAME, count));

		// timeout();

		logger.debug(">> recommendation service ready to return");
		if (misbehave)
		{
			return doMisbehavior();
		}
		return ResponseEntity.ok(String.format(RecommendationController.RESPONSE_STRING_FORMAT, HOSTNAME, count));
	}

	private void timeout()
	{
		try
		{
			Thread.sleep(3000);
		}
		catch (InterruptedException e)
		{
			logger.info("Thread interrupted");
		}
	}

	private ResponseEntity<String> doMisbehavior()
	{
		logger.debug(String.format("Misbehaving %d", count));
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
			.body(String.format("misbehavior from '%s'\n", HOSTNAME));
	}

	@RequestMapping("/misbehave/{value}")
	public ResponseEntity<String> flagMisbehave(@PathVariable boolean value)
	{
		this.misbehave = value ? value : false;
		logger.debug(">> misbehave' has been set to {}", value);
		if (misbehave)
			return ResponseEntity.ok("Next request to / will return a 503\n");
		else
			return ResponseEntity.ok("Everything's fine");
	}

}
