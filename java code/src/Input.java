import java.util.Scanner;
import java.lang.Runtime;
/**
 *
 * Class for accepting inputs oy type string and type int
 *
 * Created by Rory Campbell on 5 April 2019
 *
 */

public class Input
{
    //creating new scanner for user input
    Scanner console = new Scanner(System.in);
    /**
     * Default Constructor
     */
    Input()
    {
    }
    /**
     * Allows for a message to be displayed, which prompts the user for a string input,
     * which is read after the enter button is pressed
     */
    public String acceptInput(String displayMessage)
    {
        System.out.print(displayMessage);
        return console.nextLine();
    }

    /**
     * Allows for a message to be displayed, which prompts the user for an integer input,
     * which is read after the enter button is pressed
     */
    public int acceptInt(String displayMessage)
    {
        System.out.print(displayMessage);
        return console.nextInt();
    }

    /**
     * Allows for a message to be displayed, which prompts the user to press enter to continue
     */
    public void nextLine(String displayMessage)
    {
        System.out.print(displayMessage);
        console.nextLine();
    }
}