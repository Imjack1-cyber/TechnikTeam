package de.technikteam.config;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SanitizerConfig {

	/**
	 * A policy factory that allows common text formatting and block-level elements.
	 * Suitable for rich text content like event descriptions. Allows: <b>, <i>,
	 * <u>, <s>, <sub>, <sup>,
	 * <p>
	 * , <blockquote>,
	 * <ul>
	 * ,
	 * <ol>
	 * ,
	 * <li>, <br>
	 * ,
	 * <h1>-
	 * <h6>
	 */
	@Bean("richTextPolicy")
	public PolicyFactory richTextPolicy() {
		return new HtmlPolicyBuilder().allowCommonBlockElements() // p, div, h1-h6, etc.
				.allowCommonInlineFormattingElements() // b, i, u, etc.
				.allowElements("br", "ul", "ol", "li", "blockquote", "s", "sub", "sup").toFactory();
	}

	/**
	 * A stricter policy that only allows inline formatting, no block elements.
	 * Suitable for single-line content or chat messages where block elements are
	 * undesirable. Allows: <b>, <i>, <u>, <s>, <sub>, <sup>
	 */
	@Bean("inlineFormattingPolicy")
	public PolicyFactory inlineFormattingPolicy() {
		return new HtmlPolicyBuilder().allowCommonInlineFormattingElements().allowElements("s", "sub", "sup")
				.toFactory();
	}

	/**
	 * A policy that strips all HTML, leaving only plain text. Useful for fields
	 * that should not contain any formatting.
	 */
	@Bean("plainTextPolicy")
	public PolicyFactory plainTextPolicy() {
		return new HtmlPolicyBuilder().toFactory(); 
	}
}