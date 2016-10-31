/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gmmain;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author vagisha
 */
class TextFileInput {
    
    /**  Name of text file  */
   private String filename;

   /**  Buffered character stream from file  */
   private BufferedReader br;  

   /**  Count of lines read so far.  */
   private int lineCount = 0;

   /**
    * Creates a buffered character input
    * strea, for the specified text file.
    *
    * @param filename the input text file.
    * @exception RuntimeException if an
    *          IOException is thrown when
    *          attempting to open the file.
    *
    */
   
   public TextFileInput(String filename)
   {
      this.filename = filename;
      try  {
         br = new BufferedReader(
                  new InputStreamReader(
                      new FileInputStream(filename)));
      } catch ( IOException ioe )  {
         throw new RuntimeException(ioe);
      }  // catch
   }  // constructor
    public void close()
   {
      try  {
         br.close();
         br = null;
      } catch ( NullPointerException npe )  {
         throw new NullPointerException(
                        filename + "already closed.");
      } catch ( IOException ioe )  {
         throw new RuntimeException(ioe);
      }  // catch
   }  // method close
    
    public String readLine()
   {
      return readLineOriginal();
   }  // method readLine()

    public int getLineCount()  { return lineCount; }
    public static boolean isOneOf(char toBeChecked,
                                 char[] options)
   {
      boolean oneOf = false;
      for ( int i = 0; i < options.length && !oneOf; i++ )
         if ( Character.toUpperCase(toBeChecked)
                   == Character.toUpperCase(options[i]) )
            oneOf = true;
      return oneOf;
   }  // method isOneOf(char, char[])
    
    public static boolean isOneOf(String toBeChecked,
                                 String[] options)
   {
      boolean oneOf = false;
      for ( int i = 0; i < options.length && !oneOf; i++ )
         if ( toBeChecked.equalsIgnoreCase(options[i]) )
            oneOf = true;
      return oneOf;
   }  // method isOneOf(String, String[])
 
    public String readSelection(String[] options)
   {
      if ( options == null || options.length == 0 )
         throw new NullPointerException(
                            "No options provided for "
                            + " selection to be read in file "
                            + filename + ", line " 
                            + (lineCount + 1) + ".");

      String answer = readLine();

      if ( answer == null )
         throw new NullPointerException(
                            "End of file "
                            + filename + "has been reached.");

      if ( !TextFileInput.isOneOf(answer, options) )  {
         String optionString = options[0];
         for ( int i = 1; i < options.length; i++ )
            optionString += ", " + options[i];
         throw new RuntimeException("File " + filename
                            + ", line " + lineCount
                            + ": \"" + answer
                            + "\" not one of "
                            + optionString + ".");
      }  // if
      return answer;
  }  // method readSelection
    
    public boolean readBooleanSelection()
   {
      String[] options = {"Y", "N", "yes", "no", "1", "0",
                          "T", "F", "true", "false"};
      String answer = readSelection(options);
      return isOneOf(answer,
                     new String[] {"Y", "yes", "1", "T", "true"} );
   }  // method askUserYesNo
    
    protected final String readLineOriginal()
   {
       try  {
          if ( br == null )
             throw new RuntimeException(
                                "Cannot read from closed file "
                                + filename + ".");
          String line = br.readLine();
          if ( line != null )
             lineCount++;
          return line;
       } catch (IOException ioe)  {
          throw new RuntimeException(ioe);
       }  // catch
   }  // method readLineOriginal
    
}
