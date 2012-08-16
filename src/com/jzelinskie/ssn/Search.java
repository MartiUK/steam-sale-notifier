package com.jzelinskie.ssn;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Search {

	private URI uri;
	public Document doc;
	private ArrayList<Game> gameResults = new ArrayList<Game>();

	public Search(String searchTerm) throws IOException, URISyntaxException {
		uri = new URI("http", "store.steampowered.com", "/search/", "term="
				+ searchTerm, null);
		doc = Jsoup.connect(uri.toString()).get();
		Elements results = doc.select("a.search_result_row");
		// Add only games to the games list -- no trailers
		for (Element r : results) {
			if (r.attr("href").lastIndexOf("video") == -1) {
				gameResults.add(new Game(r));
			}
		}
	}

	public ArrayList<Game> results() {
		return gameResults;
	}
}
