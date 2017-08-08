package network;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.Charset;

import com.google.gson.Gson;
import language.dictionary.Japanese;
import main.Main;

/**
 * Wrapper for VNDB related interactions over TCP.
 * API docs: https://vndb.org/d11
 *
 * @author Laurens
 */
public class VNDB
{
    private Socket socket;
    private Gson gson = new Gson();
    /**
     * Establishes a connection to the VNDB API.
     */
    public VNDB()throws IOException
    {
        socket = new Socket("api.vndb.org", 19534);
        //required for other commands
        sendMessage("login {\"protocol\":1,\"client\":\"Spark Reader\",\"clientver\":" + Main.VERSION_NUM + "}");
    }
    private void sendMessage(String message)throws IOException
    {
        socket.getOutputStream().write(message.getBytes(Charset.forName("UTF-8")));
        socket.getOutputStream().write(0x04);//end of transmission character
    }
    private String readResponse()throws IOException
    {
        InputStreamReader is = new InputStreamReader(socket.getInputStream(), "UTF-8");
        //noinspection StatementWithEmptyBody
        while(is.read() != ' ');//read until start of JSON data
        if(is.read() != '{')throw new IllegalStateException("Expected start of JSON");
        StringBuilder sb = new StringBuilder("{");
        int depth = 1;//look for closing brace
        while(depth != 0)
        {
            int read = is.read();
            if(read == '}')depth--;
            if(read == '{')depth++;
            sb.append((char)read);
        }
        return sb.toString();
    }

    public VNEntry[] getVNs(String search)throws IOException
    {
        sendMessage("get vn basic (title~\"" + search + "\" or original~\"" + search + "\")");
        return gson.fromJson(readResponse(), VNItemResult.class).items;
    }
    private class VNItemResult
    {
        int num;
        boolean more;
        VNEntry items[];
    }

    public VNCharacter[] getCharacters(VNEntry vn)throws IOException
    {
        sendMessage("get character basic (vn= " + vn.id + ")");
        return gson.fromJson(readResponse(), VNCharacterResult.class).items;
        //TODO if there's more than 10, loop to read all pages
    }

    private class VNCharacterResult
    {
        int num;
        boolean more;
        VNCharacter items[];
    }

    public static void main(String[] args)throws Exception
    {
        VNDB vndb = new VNDB();
        VNEntry[] results = vndb.getVNs("miagete goran");
        System.out.println("result count: " + results.length);
        VNEntry chosenVN = results[0];
        System.out.println("First VN: " + chosenVN);
        VNCharacter[] characters = vndb.getCharacters(chosenVN);
        System.out.println("Character count: " + characters.length);
        System.out.println("Character list:");
        for(VNCharacter character:characters)
        {
            System.out.println(character);
        }
    }

    public void close()throws IOException
    {
        socket.close();
    }


    private class VNEntry
    {
        int id;
        //basic
        String title;
        String original;
        String released;
        String orig_lang[];
        String languages[];
        String platforms[];

        @Override
        public String toString()
        {
            return original != null? original:title;
        }
    }

    private class VNCharacter
    {
        int id;
        //basic
        String name;
        String original;
        String gender;
        String bloodt;
        String birthday[];

        @Override
        public String toString()
        {
            return original != null? (original + " [" + Japanese.romajiToKana(name.toLowerCase()) + "]"):name;
        }
    }
}
