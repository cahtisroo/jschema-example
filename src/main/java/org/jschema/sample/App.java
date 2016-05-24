package org.jschema.sample;

import org.jschema.sample.model.YahooAPI;
import org.jschema.sample.model.YahooFinance;
import org.jschema.sample.view.View;
import spark.Request;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;
import static spark.Spark.staticFileLocation;

public class App
{


  public static void main( String[] args )
  {

    //============================================================
    // Init
    //============================================================
    staticFileLocation( "public" );
    port( getHerokuAssignedPort() );

    //============================================================
    // Routing
    //============================================================
    get( "/", ( req, res ) -> {
      List<YahooFinance.Query.Results.Quote> tickers = getTickersList( req );
      Map states = null;
      if( YahooAPI.areMarketsOpen() )
      {
        states = refreshTickers( tickers );
      }
      if( "true".equals( req.queryParams( "ic-request" ) ) )
      {
        return View.renderRaw( "ticker_table.html.vm",
                               "tickers", tickers,
                               "states", states );
      }
      else
      {
        return View.renderPage( "index.html.vm",
                                "tickers", tickers,
                                "states", states);
      }
    } );

    post("/", ( req, res ) -> {
      List tickers = getTickersList( req );
      String ticker = req.queryParams( "ticker" );
      if( !ticker.isEmpty() ){
        YahooFinance.Query.Results.Quote quote = null;
        try
        {
          quote = YahooAPI.getQuote( ticker );
          // bad quote
          if( quote.getAsk() == null )
          {
            quote = null;
          }
        }
        catch( Exception e )
        {
          //bad ticker
        }
        if( quote != null )
        {
          tickers.add( 0, quote );
        }
        else
        {
          res.header( "X-IC-Trigger", "{\"applicationError\":[\"Ticker Not Valid!\"]}" );
        }
      }
      return View.renderRaw( "index.html.vm", "tickers", tickers, "newticker", ticker );
    } );

    delete( "/", ( req, res ) -> {
      List tickers = getTickersList( req );
      tickers.clear();
      return View.renderRaw( "index.html.vm", "tickers", tickers );
    } );

    delete( "/:ticker", ( req, res ) -> {
      String ticker = req.params( "ticker" );

      List<YahooFinance.Query.Results.Quote> tickers = getTickersList( req );
      Iterator<YahooFinance.Query.Results.Quote> it = tickers.iterator();
      while( it.hasNext() )
      {
        YahooFinance.Query.Results.Quote quote = it.next();
        if( quote.getsymbol().equals( ticker ) )
        {
          it.remove();
        }
      }

      return View.renderRaw( "index.html.vm", "tickers", tickers );
    } );
  }

  //============================================================
  // Ticker List Management
  //============================================================
  private static Map refreshTickers( List<YahooFinance.Query.Results.Quote> tickers ) throws IOException
  {
    HashMap<Object, Object> map = new HashMap<>();
    for( int i = 0; i < tickers.size(); i++ )
    {
      YahooFinance.Query.Results.Quote quote = tickers.get( i );
      YahooFinance.Query.Results.Quote newQuote = YahooAPI.getQuote( quote.getsymbol() );
      double oldAsk = Double.parseDouble( quote.getAsk() );
      double newAsk = Double.parseDouble( newQuote.getAsk() );
      if( oldAsk < newAsk ) {
        map.put( quote.getsymbol(), "up" );
      }
      else if( oldAsk > newAsk )
      {
        map.put( quote.getsymbol(), "down" );
      }
      tickers.set( i, newQuote );
    }
    return map;
  }

  private static List<YahooFinance.Query.Results.Quote> getTickersList( Request req )
  {
    List tickers = req.session().attribute( "tickers" );
    if( tickers == null )
    {
      tickers = new LinkedList();
      req.session().attribute( "tickers", tickers );
    }
    return tickers;
  }

  //============================================================
  // Utilities
  //============================================================
  static int getHerokuAssignedPort()
  {
    ProcessBuilder processBuilder = new ProcessBuilder();
    if( processBuilder.environment().get( "PORT" ) != null )
    {
      return Integer.parseInt( processBuilder.environment().get( "PORT" ) );
    }
    return 4567;
  }

}