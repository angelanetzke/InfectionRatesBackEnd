package io.github.angelanetzke.infectionrates;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Scanner;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Main {
	private static long sixHoursMS = 1000l * 60l * 60l * 6l;
	private static String urlString = "https://api.covidactnow.org/v2/states.json?apiKey=";

	public static void main(String[] args) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(2345), 0);
		server.createContext("/infectionrates", new HttpHandler() {

			@Override
			public void handle(HttpExchange exchange) throws IOException {
				Date now = new Date();
				long nowMS = now.getTime();
				File cacheFile = new File("cache.json");
				long cacheLastModifiedMS = cacheFile.lastModified();
				if (cacheFile.exists() && nowMS - cacheLastModifiedMS < sixHoursMS) {
					byte[] response = Files.readAllBytes(new File("cache.json").toPath());
					sendResponse(exchange, response);
				} else {
					Scanner keyFile = new Scanner(new File("key.txt"));
					String APIKey = keyFile.nextLine();
					keyFile.close();
					HttpClient client = HttpClient.newHttpClient();
					HttpRequest request = HttpRequest.newBuilder(URI.create(urlString + APIKey))
							.header("accept", "application/json").build();
					try {
						HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
						Files.writeString(Paths.get("cache.json"), response.body());
						sendResponse(exchange, response.body().getBytes());
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			private void sendResponse(HttpExchange exchange, byte[] response) throws IOException {
				exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
				exchange.sendResponseHeaders(200, response.length);
				OutputStream os = exchange.getResponseBody();
				os.write(response);
				os.close();
			}
		});
		server.start();
	}

}
