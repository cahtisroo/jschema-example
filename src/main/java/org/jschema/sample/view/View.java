package org.jschema.sample.view;


import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class View
{
  private static final VelocityEngine VELOCITY = new org.apache.velocity.app.VelocityEngine( velocityProperties() );
  private static final ViewHelper HELPER = new ViewHelper();

  public static String renderPage( String templateName, Object... args )
  {
    return renderRaw( "layout.html.vm", "content", renderRaw( templateName, args ) );
  }

  public static String renderRaw( String templateName, Object... args )
  {
    Template template = VELOCITY.getTemplate( tempatePath( templateName ) );
    Map modelMap = new HashMap();
    Iterator<Object> iterator = Arrays.asList( args ).iterator();
    while( iterator.hasNext() )
    {
      Object name = iterator.next();
      Object val = iterator.next();
      modelMap.put( name, val );
    }
    modelMap.put( "helper", HELPER );
    VelocityContext context = new VelocityContext( modelMap );
    StringWriter writer = new StringWriter();
    template.merge( context, writer );
    return writer.toString();
  }

  private static String tempatePath( String templateName )
  {
    return "views/" + templateName;
  }

  private static Properties velocityProperties() {
    Properties properties = new Properties();
    properties.setProperty("resource.loader", "class");
    properties.setProperty( "class.resource.loader.class",
                            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    return properties;
  }

  public static class ViewHelper {

    public String include( String templateName, Object... args ) {
      return View.renderRaw( templateName, args );
    }

    public String formatDoubleString(String doubleString) {
      if( doubleString == null )
      {
        return "";
      }
      else
      {
        DecimalFormat df = new DecimalFormat( "#.00");
        return df.format(Double.parseDouble(doubleString));
      }
    }

    public String chomp(String string) {
      return string.substring( 1 );
    }
  }
}