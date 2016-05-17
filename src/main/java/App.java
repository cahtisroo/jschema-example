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
            String ticker = req.queryParams( "ticker" );
            String result = "";
            if(ticker != null)  {
                String urlStr = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(%22"+ticker+"%22)%0A%09%09&env=http%3A%2F%2Fdatatables.org%2Falltables.env&format=json";
                result = getContent( urlStr );
            }  else {
                ticker = "";
            }
            return "<html><link href=\"bootstrap.css\" rel=\"stylesheet\" type=\"text/css\"><div class=\"container\"><h1>Enter a stock ticker:</h1><form><input type='text' class='tickerBox' name='ticker' value='" + ticker + "'/><button class='submitButton'>Submit</button></form><hr/><pre>" + result + "</pre></div></html>";
        } );
    }


    private static String getContent( String urlStr ) throws IOException
    {
        String result;
        URL url = new URL( urlStr );
        InputStream in = (InputStream)url.getContent();
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String read;
        String ex;

        while((read=br.readLine()) != null) {
            //System.out.println(read);
            sb.append(read);
        }

        br.close();
        result =  sb.toString();
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