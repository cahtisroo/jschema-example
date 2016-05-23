package org.jschema.sample.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class YahooAPI
{
  public static YahooFinance.Query.Results.Quote getQuote( String ticker ) throws IOException
  {

    String urlString = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(%22" + ticker + "%22)%0A%09%09&env=http%3A%2F%2Fdatatables.org%2Falltables.env&format=json";
    String json = getContent( urlString );

    YahooFinance finance = YahooFinance.parse( json );

    YahooFinance.Query query = finance.getquery();
    YahooFinance.Query.Results results = query.getresults();
    YahooFinance.Query.Results.Quote quote = results.getquote();

    return quote;
  }

  private static String getContent( String urlStr ) throws IOException
  {
    String result;
    URL url = new URL( urlStr );
    InputStream in = (InputStream)url.getContent();
    StringBuilder sb = new StringBuilder();
    BufferedReader br = new BufferedReader( new InputStreamReader( in ) );
    String read;
    String ex;

    while( (read = br.readLine()) != null )
    {
      sb.append( read );
    }

    br.close();
    result = sb.toString();
    return result;
  }

}
