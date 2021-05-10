import java.io.*;
import java.util.Scanner;
/**
 * allows for the reading and writing of files
 *
 * @author Rory Campbell
 * @date 6 May 2019
 */
public class FileIO
{
    private String fileName;

    /**
     * default constructor
     */
    public FileIO()
    {
        fileName = "";
    }

    /**
     * non-default constructor
     */
    public FileIO(String newFileName)
    {
        fileName = newFileName;
    }

    /**
     * gets the current file name
     */
    public String getFileName()
    {
        return fileName;
    }

    /**
     * sets the file name
     */
    public void setFileName(String newFileName)
    {
        fileName = newFileName;
    }

    /**
     * reads a txt file and returns a string
     */
    public String readFile(String fileName)
    {
        String outPut = "";
        try
        {
            FileReader inputFile = new FileReader(fileName);
            Scanner parser = new Scanner(inputFile);
            StringBuffer buff = new StringBuffer();
            while(parser.hasNext())
            {
                buff.append(parser.nextLine());
                buff.append("\n");
            }
            outPut = buff.toString();
            inputFile.close();
        }
        catch (IOException e)
        {
            System.out.println("File not found");
            System.exit(1);
        }
        return outPut;
    }

    /**
     * writes the contents of the list of drivers array to a txt file
     */
    public void writeToFile(String fileName, String message)
    {
        try
        {
            PrintWriter outputFile = new PrintWriter(fileName);
            outputFile.print(message);
            outputFile.close();
        }
        catch(IOException e)
        {
            System.out.println("File not found");
            System.exit(1);
        }

    }
}
