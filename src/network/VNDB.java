package network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

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

    public List<VNCharacter> getCharacters(int vnID)throws IOException
    {
        String query = "get character basic,details (vn= " + vnID + ") {\"page\":";
        int pageNum = 1;
        ArrayList<VNCharacter> characters = new ArrayList<>();
        while(true)
        {
            sendMessage(query + pageNum + "}");
            VNCharacterResult result = gson.fromJson(readResponse(), VNCharacterResult.class);
            characters.addAll(result.items);

            if(!result.more)break;
            pageNum++;
        }

        return characters;
    }

    private class VNCharacterResult
    {
        int num;
        boolean more;
        List<VNCharacter> items;
    }

    public static void main(String[] args)throws Exception
    {
        VNDB vndb = new VNDB();
        /*VNEntry[] results = vndb.getVNs("miagete goran");
        System.out.println("result count: " + results.length);
        VNEntry chosenVN = results[0];
        System.out.println("First VN: " + chosenVN);
        List<VNCharacter> characters = vndb.getCharacters(chosenVN.id);
        System.out.println("Character count: " + characters.size());
        System.out.println("Character list:");
        for(VNCharacter character:characters)
        {
            System.out.println(character.toSimpleDefinitionLines());
        }*/
        vndb.generateCharacterDictionary(new File("dictionaries/vnCharacters/MiageteGoran.tsv"), 16560, "Miagete Goran");
        vndb.generateCharacterDictionary(new File("dictionaries/vnCharacters/dracuRiot.tsv"), 8213, "Dracu Riot");
    }

    public void generateCharacterDictionary(File output, int vnID, String vnName)throws IOException
    {
        //noinspection ResultOfMethodCallIgnored
        output.getParentFile().mkdirs();

        Writer fr = new OutputStreamWriter(new FileOutputStream(output, false), Charset.forName("UTF-8"));
        fr.write("Imported\n");//DefSource name on first line
        List<VNCharacter> characters = getCharacters(vnID);
        for(VNCharacter character:characters)
        {
            fr.append(character.toSimpleDefinitionLines(vnName));
        }
        fr.close();
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
        //details
        String aliases;
        String description;
        String image;

        public String getFirstDescLine()
        {
            if(description == null)return "";
            int lastPos = Math.min( (description + ".").indexOf(".") + 1,
                                    (description + "[spoiler]").indexOf("[spoiler]"));//first sentence or before first spoiler tag
            return description.substring(0, lastPos)//relevant text
                    .replaceAll("\\[(?:.|\\n)*?]", "")//remove formatting
                    .replace("\n", "\t");//format to def
        }

        private String vnName = null;
        /**
         * Generates simple definitions for every name associated with this character.
         * With the new sense system, this could be improved upon to tag things like first name and nickname to specific senses of a single definition entry.
         * @return a String with lines of text for looking up this character
         */
        public String toSimpleDefinitionLines(String vnName)
        {
            this.vnName = vnName;
            int firstNameIndex = 1;
            int surnameIndex = 0;

            StringBuilder out = new StringBuilder();
            //find all names for this character
            String[] names = null;
            String[] readings = null;
            String[] others = null;
            readings = name.split("[　 ・]");//fullwidth space, normal space, dot for katakana names
            if(original != null)names = original.split("[　 ・]");
            if(aliases != null)others = aliases.split("\n");//split by newline


            if(others != null)//aliases available
                for(String other:others)
                    addDef(out, toKana(other), null, "nickname");

            if(names == null)//only kana available
            {
                if(readings.length == 2)
                {
                    addDef(out, toKana(readings[firstNameIndex]), null, "first name");
                    addDef(out, toKana(readings[surnameIndex]), null, "surname");
                } else for(String reading:readings)
                    addDef(out, toKana(reading), null, "name");
            }
            else//Kanji and reading available
            {
                if(names.length != readings.length)throw new IllegalStateException("Names and readings do not match - please fix VNDB entry");
                if(names.length == 2)
                {
                    addDef(out, names[firstNameIndex], toKana(readings[firstNameIndex]), "first name");
                    addDef(out, names[surnameIndex], toKana(readings[surnameIndex]), "surname");
                }
                else for(int i = 0; i < names.length; i++)
                {
                    addDef(out, names[i], toKana(readings[i]), "name");
                }
            }


            return out.toString();
        }
        private String toKana(String text)
        {
            String convert = Japanese.romajiToKana(text.toLowerCase());
            if(!Japanese.hasOnlyKana(convert))return text;//cannot be written as Kana
            else return convert;
        }
        private void addDef(StringBuilder line, String name, String reading, String comment)
        {
            line.append(name);//name first
            if(reading != null && !reading.equals(name))line.append(':').append(reading);//optional reading
            line.append("\tn\t");//tag as noun
            line.append((int)(7000000 + Math.random()*700000));//ID
            line.append('\t').append(getFirstDescLine());//description text
            if(comment != null)line.append('\t').append(comment);//comment on name type
            if(vnName != null) line.append('\t').append("from ").append(vnName);//comment on origin
            //TODO append some attributes (red hair, etc)
            line.append('\n');
        }

        @Override
        public String toString()
        {
            return original != null? (original + " [" + name + "]"):name;
        }
    }
}
