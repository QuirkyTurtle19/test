import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.*;

import java.security.cert.Certificate;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class FIT5003App {
    private static KeyStore my_ks;
    private static final String key = "aesEncryptionKey";
    private static final String initVector = "encryptionIntVec";
    private static char[] keyStorePassword;
    private static Scanner console = new Scanner(System.in);
    private static KeyPair pair;

    public static String acceptInput(String displayMessage)
    {
        System.out.print(displayMessage);
        return console.nextLine();
    }




    public static byte[] getSHA(String input) throws NoSuchAlgorithmException
    {
        // Static getInstance method is called with hashing SHA-1
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
        return md.digest(input.getBytes(UTF_8));
    }

    public static String toHexString(byte[] hash)
    {
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, hash);

        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 32)
        {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }


    public static void main(String[] args) throws Exception {
        
        String username;
        String passwd;
        MessageDigest md= null;


//------------------Get Administrator user username and password from the Standard input (System in)----------------------------------


        //accepts the username and password of the admin
        username = acceptInput("Please enter username: ");
        passwd = acceptInput("Please enter password: ");

        //combines the username and password
        String usernamePassword = username + passwd;
        String digest  = "";
        String key = "";



//------------------Generate the keystore password ------------------------------------------------------------------
//-----------------You should use  SHA256. You can use the methods provided in this file to extract the 5 Bytes of the digest as a String ----------------


        //encrypt the username+password using SHA256
        try {
            byte[] SHA = getSHA(usernamePassword);
            byte[] bytes = extractbytes(SHA, 5);
            key = encodeHexString(bytes);
            //System.out.println(key);
        }
        catch (NoSuchAlgorithmException e){
            System.out.println("Exception thrown for incorrect algorithm: " + e);
        }






//-----Hint: use a try catch statement to capture the exception if the keystore cannot be opened (wrong username and password) ------------
// Read the Keystore. The provided keystore is of type: JCEKS

        //unlock the keystore with the SHA256 output
        char[] keyStorePassword = key.toCharArray();
        my_ks = KeyStore.getInstance("JCEKS");
        try ( InputStream keyStoreData = new FileInputStream("fit5003A_keystore.jks")){
            my_ks.load( keyStoreData , keyStorePassword );
            //System.out.println("success");
        }

        catch(Exception e){
            System.out.println("KeyStore cannot be unlocked" + e);
        }
           

//------------------Retrieve all the aliases from the keystore and print the required information ---------

        //loop to print out aliases and certificates
        Enumeration enumeration = my_ks.aliases();
        List<String> alias_list = new ArrayList<String>();
        while(enumeration.hasMoreElements()) {
            String alias = (String)enumeration.nextElement();
            Certificate certificate = my_ks.getCertificate(alias);
            System.out.println("alias name: " + alias);
            System.out.println("The Certificate is: " + certificate);
            alias_list.add(alias);
        }

//------------------Ask the Administrator user to provide the alias of another user's certificate entry for whom the administrator will secure the file  ---------    


        //asks for admin for the user that they wish to sign the document for
        String aliasToSign = acceptInput("Please enter the alias of the user you wish to sign: ");
        boolean b1 = false;
        while (b1 == false) {
            if (!alias_list.contains(aliasToSign)) {
                acceptInput("Cannot find alias, please try again: ");
            }
            b1 = true;
        }
//------------------Generate an AES 256 key and a IV  and encrypt the file using AES CBC with PKCS5PADDIN---------


        //asks for the file name they wish to sign on behalf of the user
        String fileName = acceptInput("Please enter the file you wish to encrypt");


        FileIO file = new FileIO();
        String encryptedString = "";




            //initialise the AES encryption with the IV
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKey secretKey = KeyGenerator.getInstance("AES").generateKey();


            //get the cipher instance
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);


            //read the file contents
            String contents = file.readFile(fileName);

            //encrypt the file contenst with the AES cipher
            byte[] encrypted = cipher.doFinal(contents.getBytes());
            encryptedString = encodeHexString(encrypted);



//------------Extract from the chosen certificate (from the provided alias by the Administrator) the user Public key---
//------------Use the Public Key to encrypt the AES key and concatenate the result to the AES encrypted file ------------

        //get the certificate of the previously stated alias to sign
        Certificate cert = my_ks.getCertificate(aliasToSign);
        Key publicKey = cert.getPublicKey();

       //converting the IV and secret key into strings
       String ivString = encodeHexString(iv.getIV());
       String secKeyString = secretKey.toString();

       //encrypting the IV and secret keys with the public key of the alias
       Cipher encryptCipher = Cipher.getInstance (" RSA ");
        encryptCipher.init ( Cipher.ENCRYPT_MODE , publicKey );
        byte [] ivCipherText = encryptCipher.doFinal (  ivString . getBytes ( UTF_8 ));
        byte [] secKeyCipherText = encryptCipher.doFinal (  secKeyString . getBytes ( UTF_8 ));


        //concatonating the newly created encrypted IV and secret key together with the encrypted contents
        String ivAndSecKeyString = encodeHexString(ivCipherText).concat(encodeHexString(secKeyCipherText) );
        String fullString = encryptedString.concat(ivAndSecKeyString);







//---------------Extract from the opened keystore the Administrator user entry. Extract from this entry the Administrator user privatekey ---

        //taking the admin secret key from the keystore to sign the encrypted string
        KeyStore.ProtectionParameter entryPassword = new KeyStore.PasswordProtection ( keyStorePassword );
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) my_ks.getEntry ("user1", entryPassword );
        PrivateKey adminPrivateKey = privateKeyEntry.getPrivateKey();


//---------------Use the extracted Private key to generate the Digital signature of the encrypted file (that includes the concatenated encrypted public key)----

       //signing the encrypted string
        Signature privateSignature = Signature.getInstance ("SHA256withRSA");
        privateSignature.initSign(adminPrivateKey);
        privateSignature.update( fullString.getBytes ( UTF_8 ));
        byte [] signature = privateSignature.sign ();
        String sigString = Base64.getEncoder().encodeToString(signature);


//---------------------------Store the Digital Signature as well as the encrypted file (that includes the concatenated encrypted public key) in the hard drive------------------------------
        //writing the entire contents back to the file

        file.writeToFile(fileName, sigString);

    }



    
        public static String byteToHex(byte num) {
            char[] hexDigits = new char[2];
            hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
            hexDigits[1] = Character.forDigit((num & 0xF), 16);
            return new String(hexDigits);
        }
    
        public static String encodeHexString(byte[] byteArray) {
            StringBuffer hexStringBuffer = new StringBuffer();
            for (int i = 0; i < byteArray.length; i++) {
                hexStringBuffer.append(byteToHex(byteArray[i]));
            }
            return hexStringBuffer.toString();
        }
    
        private static byte[]  extractbytes(byte[] input, int len) {
    
            ByteBuffer bb = ByteBuffer.wrap(input);
    
            byte[] output = new byte[len];
            bb.get(output, 0, output.length);
            return output;
        }
    
}
