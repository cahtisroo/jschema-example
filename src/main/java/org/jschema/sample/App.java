package org.jschema.sample;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import static spark.Spark.*;
import static spark.Spark.staticFileLocation;

public class App {

    public static void main(String[] args) {
        staticFileLocation("public");
        port(getHerokuAssignedPort());
        get("/", (req, res) -> {
            String ticker = req.queryParams("ticker");
            String json = "";
            String resultsString = "";
            if (ticker != null)  {
                String urlString = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(%22"+ticker+"%22)%0A%09%09&env=http%3A%2F%2Fdatatables.org%2Falltables.env&format=json";
                json = getContent(urlString);

                YahooFinance finance = YahooFinance.parse(json);

                YahooFinance.Query query = finance.getquery();
                YahooFinance.Query.Results results = query.getresults();
                YahooFinance.Query.Results.Quote quote = results.getquote();

                String symbol = quote.getsymbol();
                String askingPrice = quote.getAsk();
                String change = quote.getChange();
                resultsString = "<h2>" + symbol + "</h2><br>" + "<h2>" + askingPrice + " USD</h2><br>" + "<h2>Change: " + change + "</h2><br>";
            }  else {
                ticker = "";
            }

            String header = "<html><title>JSchema Sample</title><script type=\"text/javascript\" src=\"main.js\" ></script><link href=\"bootstrap.css\" rel=\"stylesheet\" type=\"text/css\"><body onload=\"pageLoaded()\"><div class=\"container\"><h1 class=\"text-center\">JSchema Sample</h1><br><form id=\"form\"><input type='text' id='ticker-input' class='form-control input-lg' name='ticker' placeholder=\"Enter a stock ticker...\" value='";
            return header + ticker + "'/></form><hr/><pre>" + resultsString + "</pre></div></body></html>";
        } );
    }

    private static String getContent(String urlStr) throws IOException {
        String result;
        URL url = new URL(urlStr);
        InputStream in = (InputStream)url.getContent();
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String read;
        String ex;

        while ((read = br.readLine()) != null) {
            sb.append(read);
        }

        br.close();
        result = sb.toString();
        return result;
    }

    static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567;
    }

}