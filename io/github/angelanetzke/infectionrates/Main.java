package io.github.angelanetzke.infectionrates;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Main {

	public static void main(String[] args) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(2345), 0);
		server.createContext("/infectionrates", new HttpHandler() {

			@Override
			public void handle(HttpExchange exchange) throws IOException {
				File cacheFile = new File("cache.json");
				if (cacheFile.exists()) {
					byte[] response = Files.readAllBytes(new File("cache.json").toPath());
					sendResponse(exchange, response);
				}
				else {
					byte[] response = "File not found.".getBytes();
					sendResponse(exchange, response);
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
