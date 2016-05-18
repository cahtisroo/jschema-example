package org.jschema.sample;
import java.util.ArrayList;
import java.util.HashMap;
import org.jschema.sample.Token.TokenType;
import static org.jschema.sample.Token.TokenType.*;

public class Parser
{

  private final Tokenizer _tokenizer;
  private Token _currentToken;

  public Parser( String src )
  {
    _tokenizer = new Tokenizer( src );
    nextToken();
  }

  //=================================================================================
  //  JSON Grammar
  //=================================================================================

  public Object parse() {
    Object value = parseValue();
    if( match( EOF ) )
    {
      return value;
    }
    else
    {
      return error();
    }
  }

  public Object parseValue()
  {

    if( match( LCURLY ) )
    {
      nextToken();
      return parseObject();
    }

    // parse arrays
    if( match( LSQUARE ) )
    {
      nextToken();
      return parseArray();
    }

    if( match( TRUE ) )
    {
      nextToken();
      return true;
    }

    if( match( FALSE ) )
    {
      nextToken();
      return false;
    }
    if( match( NULL ) )
    {
      nextToken();
      return null;
    }

    if( match( STRING ) )
    {
      String tokenValue = _currentToken.getTokenValue();
      nextToken();
      return tokenValue;
    }
    if( match( NUMBER ) )
    {
      String tokenValue = _currentToken.getTokenValue();
      double tokenNum = _currentToken.getTokenNumberValue();
      if(tokenValue.indexOf('e') >= 0 || tokenValue.indexOf('E') >= 0){
        nextToken();
        return Double.parseDouble(tokenValue);
      }
      if(!tokenValue.contains(".")){
        nextToken();
        return Integer.parseInt(tokenValue);
      }
      else{
        nextToken();
        return tokenNum;
      }
    }
    return error();
  }

  public Object parseObject() {
    HashMap<String, Object> map = new HashMap();

    if(match(STRING)) {
      parseMember(map);
    }
    while (match(COMMA)) {
      nextToken();
      if (match(STRING)) {
        parseMember(map);
      }
      else{
        nextToken();
        return error();
      }
    }
    if (match(RCURLY)){
      nextToken();
      return map;
    }
    else {
      nextToken();
      return error();
    }
  }

  private void parseMember( HashMap map )
  {
    String key;
    Object obj;

    key = _currentToken.getTokenValue();
    nextToken();
    if(match(COLON)){
      nextToken();
      obj = parseValue();
      if(obj instanceof Error){
        parseError();
      }
      map.put(key, obj);
    }
    else{
      parseError();
    }
  }

  private Error parseError(){
    nextToken();
    return error();
  }

  public Object parseArray()
  {
    ArrayList list = new ArrayList();
    if(match(RSQUARE)){
      nextToken();
      return list;
    }
    list.add(parseValue());
    while (match(COMMA)) {
      nextToken();
      if(match(RSQUARE)){
        parseError();
      }
      list.add(parseValue());
    }
    if(match(RSQUARE)) {
      nextToken();
      return list;
    }
    else{
      nextToken();
      return error();
    }
  }

  //=================================================================================
  //  Tokenizer helpers
  //=================================================================================
  private void nextToken()
  {
    _currentToken = _tokenizer.next();
  }

  private boolean match( TokenType type )
  {
    return _currentToken.getTokenType() == type;
  }

  private Error error()
  {
    return new Error("Unexpected Token: " + _currentToken.toString());
  }

}