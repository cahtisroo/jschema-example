package org.jschema.sample;
import java.util.ArrayList;
import java.util.List;
import org.jschema.sample.Token.TokenType;

public class Tokenizer {
  private String _string;
  private int _offset;
  private int _line;
  private int _column;
  private char ch;
  private int _errorCount;

  public Tokenizer(String string) {
    _string = string;
    _offset = 0;
    _line = 1;
    _column = 0;
    nextChar();
  }

  //========================================================================================
  //  Main tokenization loop
  //========================================================================================

  public Token next() {
    Token T;
    eatWhiteSpace(); // eat leading whitespace
    switch(ch) {
      case '"':
        T = consumeString();
        break;
      case '-':
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
        T = consumeNumber();
        break;
      case '{':
        T = newToken(TokenType.LCURLY, "{");
        nextChar();
        break;
      case '}':
        T = newToken(TokenType.RCURLY, "}");
        nextChar();
        break;
      case '[':
        T = newToken(TokenType.LSQUARE, "[");
        nextChar();
        break;
      case ']':
        T = newToken(TokenType.RSQUARE, "]");
        nextChar();
        break;
      case ',':
        T = newToken(TokenType.COMMA, ",");
        nextChar();
        break;
      case ':':
        T = newToken(TokenType.COLON, ":");
        nextChar();
        break;
      case 't':
      case 'f':
      case 'n':
        T = consumeConstant();
        break;
      case '\0':
        T = new Token(TokenType.EOF, "EOF", _line, _column, _offset, 0.0);
        _string = "";
        break;
      default:
        // unrecognized token
        T = errorToken( String.valueOf(ch) );
        nextChar();
    }
    return T;
  }

  //========================================================================================
  //  Tokenization type methods
  //========================================================================================

  private Token consumeString() {
    StringBuilder sb = new StringBuilder();
    Token T;
    nextChar();
    while(moreChars() && ch != '"') {
      if(ch == '\\') {
        nextChar();
        switch(ch) {
          case '"':
          case '\\':
          case '/':
            sb.append(ch);
            nextChar();
            break;
          case 'b':
            sb.append('\b');
            nextChar();
            break;
          case 'f':
            sb.append('\f');
            nextChar();
            break;
          case 'n':
            sb.append('\n');
            nextChar();
            break;
          case 'r':
            sb.append('\r');
            nextChar();
            break;
          case 't':
            sb.append('\t');
            nextChar();
            break;
          case 'u':
            nextChar();
            int u = 0;
            for(int i = 0; i < 4; i++) {
              if(isHexDigit(ch)) {
                u = u * 16 + ch - '0';
                if(ch >= 'A') { // handle hex numbers: 'A' = 65, '0' = 48. 'A'-'0' = 17, 17 - 7 = 10
                  u = u - 7;
                }
              } else {
                nextChar();
                return errorToken( sb.toString() );
              }
              nextChar();
            }
            sb.append((char) u);
            break;
          default:
            return errorToken( sb.toString() );
        }
      } else {
        sb.append(ch);
        nextChar();
      }
    }
    if(ch == '"') {
      T = newToken(TokenType.STRING, sb.toString());
    } else {
      T = errorToken( sb.toString() );
    }
    nextChar();
    return T;
  }

  //needs to work for integers and decimals
  private Token consumeNumber() {
    StringBuilder sb = new StringBuilder();
    Token T;
    int num = 0;
    int frac = 0;
    int numFracDigit = 0;
    int exp = 0;
    boolean neg = false;
    if(ch == '-') {
      sb.append(ch);
      nextChar();
      neg = true;
    }
    if(ch != '0') {
      num = consumeDigits(sb);
      if(num == -1) {
        return errorToken( sb.toString() );
      }
    } else {
      sb.append(ch);
      nextChar();
    }
    if(ch == '.') {
      sb.append(ch);
      nextChar();
      numFracDigit = sb.length();
      frac = consumeDigits(sb);
      if(frac == -1) {
        return errorToken( sb.toString() );
      }
      numFracDigit = sb.length() - numFracDigit;
    }
    if(ch == 'E' || ch == 'e') {
      sb.append(ch);
      nextChar();
      boolean negExp = false;
      if(ch == '-') {
        sb.append(ch);
        nextChar();
        negExp = true;
      } else if(ch == '+') {
        sb.append(ch);
        nextChar();
      }
      exp = consumeDigits(sb);
      if(negExp) {
        exp = -exp;
      }
    }
    double doubleValue = num;
    if(frac != 0) {
      doubleValue += (frac * Math.pow(10, -numFracDigit));
    }
    if(exp != 0) {
      doubleValue = doubleValue * Math.pow(10, exp);
    }
    if(neg) {
      doubleValue = -doubleValue;
    }
    T = newNumberToken(TokenType.NUMBER, sb.toString(), doubleValue);
    return T;
  }

  private int consumeDigits(StringBuilder sb) {
    int num = 0;
    if(isDigit(ch)) {
      while(moreChars() && isDigit(ch)) {
        sb.append(ch);
        num = num * 10 + ch - '0';
        nextChar();
      }
    } else {
      num = -1;
    }
    return num;
  }

  private boolean isDigit(char ch) {
    return ch >= '0' && ch <= '9';
  }

  private boolean isHexDigit(char ch) {
    return ch >= '0' && ch <= '9' || ch >= 'A' && ch <= 'F' || ch >= 'a' && ch <= 'f';
  }

  private Token consumeConstant() {
    StringBuilder sb = new StringBuilder();
    Token T;
    do {
      sb.append(ch);
      nextChar();
    } while(moreChars() && (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z'));
    String str = sb.toString();
    TokenType type = Token.constants.get(str);
    if(type == null) {
      T = errorToken( str );
    } else {
      T = newToken(type, str);
    }
    return T;
  }

  private Token errorToken( String str )
  {
    _errorCount++;
    return newToken( TokenType.ERROR, str);
  }

  public int getErrorCount() {
    return _errorCount;
  }

  //========================================================================================
  //  Utility methods
  //========================================================================================

  private Token newToken(TokenType type, String tokenValue) {
    return new Token(type, tokenValue, _line, _column, _offset + 1, 0);
  }

  private Token newNumberToken(TokenType type, String tokenValue, double num) {
    return new Token(type, tokenValue, _line, _column, _offset + 1, num);
  }

  private void eatWhiteSpace() {
    //while there exists more characters and the current character is white space
    while(moreChars() && (ch == '\t' || ch == '\n' || ch == '\r' || ch == ' ')) {
      nextChar();
    }
  }

  private void nextChar() {
    if(_offset < _string.length()) {
      ch = _string.charAt(_offset);
      if(ch == '\n') // if we are at a newline character, bump the line number and reset the column
      {
        _line++;
        _column = 0;
      }
      _offset = _offset + 1; // bump offset
      _column = _column + 1; // bump column
    } else {
      ch = '\0';
    }
  }

  private boolean moreChars() {
    return ch != '\0';
  }

  public List<Token> tokenize() {
    ArrayList<Token> list = new ArrayList<Token>();
    Token token = next();
    while(token.getTokenType() != TokenType.EOF) {
      list.add(token);
      token = next();
    }
    return list;
  }
}
