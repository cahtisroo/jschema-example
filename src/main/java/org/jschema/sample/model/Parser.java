package org.jschema.sample.model;
import java.util.ArrayList;
import java.util.HashMap;

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
    if( match( Token.TokenType.EOF ) )
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

    if( match( Token.TokenType.LCURLY ) )
    {
      nextToken();
      return parseObject();
    }

    // parse arrays
    if( match( Token.TokenType.LSQUARE ) )
    {
      nextToken();
      return parseArray();
    }

    if( match( Token.TokenType.TRUE ) )
    {
      nextToken();
      return true;
    }

    if( match( Token.TokenType.FALSE ) )
    {
      nextToken();
      return false;
    }
    if( match( Token.TokenType.NULL ) )
    {
      nextToken();
      return null;
    }

    if( match( Token.TokenType.STRING ) )
    {
      String tokenValue = _currentToken.getTokenValue();
      nextToken();
      return tokenValue;
    }
    if( match( Token.TokenType.NUMBER ) )
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

    if(match( Token.TokenType.STRING)) {
      parseMember(map);
    }
    while (match( Token.TokenType.COMMA)) {
      nextToken();
      if (match( Token.TokenType.STRING)) {
        parseMember(map);
      }
      else{
        nextToken();
        return error();
      }
    }
    if (match( Token.TokenType.RCURLY)){
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
    if(match( Token.TokenType.COLON)){
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
    if(match( Token.TokenType.RSQUARE)){
      nextToken();
      return list;
    }
    list.add(parseValue());
    while (match( Token.TokenType.COMMA)) {
      nextToken();
      if(match( Token.TokenType.RSQUARE)){
        parseError();
      }
      list.add(parseValue());
    }
    if(match( Token.TokenType.RSQUARE)) {
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

  private boolean match( Token.TokenType type )
  {
    return _currentToken.getTokenType() == type;
  }

  private Error error()
  {
    return new Error("Unexpected Token: " + _currentToken.toString());
  }

}