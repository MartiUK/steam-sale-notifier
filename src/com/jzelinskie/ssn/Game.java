package com.jzelinskie.ssn;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Game {
	private String title, url, price, salePrice;
	private Date updated;

	public Game(String title, String url, String price, String salePrice,
			Date updated) {
		this.title = title;
		this.url = url;
		this.price = price;
		this.salePrice = salePrice;
		this.updated = updated;
	}

	public Game(Element elm) {
		updateFromElement(elm);
	}

	public void update() throws IOException, URISyntaxException {
		String url = new URI("http", "store.steampowered.com", "/search/",
				"term=" + this.title(), null).toString();
		Document doc = Jsoup.connect(url).get();
		Elements results = doc.select("a.search_result_row");
		for (Element r : results) {
			if (r.select("h4").text().equals(this.title())) {
				Element elm = r;
				updateFromElement(elm);
				break;
			}
		}
	}

	private void updateFromElement(Element elm) {
		boolean sale;
		// Determine if the item is on sale and get prices
		if (elm.select("strike").text().contains("$")) {
			sale = true;
		} else {
			sale = false;
		}
		if (sale) {
			price = elm.select("strike").text();
		} else {
			price = elm.select("div.search_price").text();
		}
		if (sale) {
			salePrice = elm.select("div.search_price").text().split(" ")[1];
		} else {
			salePrice = "";
		}

		// Determine game metadata
		title = elm.select("h4").text();
		url = elm.attr("href");
		updated = new Date();
	}

	public Date updated() {
		return updated;
	}

	public String title() {
		return title;
	}

	public String url() {
		return url;
	}

	public String price() {
		return price;
	}

	public String salePrice() {
		return salePrice;
	}

	public boolean sale() {
		return salePrice != "";
	}
}