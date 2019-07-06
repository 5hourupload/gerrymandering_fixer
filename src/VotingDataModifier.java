import java.io.*;

public class VotingDataModifier {
    public static void main(String[] args) throws IOException {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("voting_percentages.txt", "UTF-8");
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }



        try (BufferedReader br = new BufferedReader(new FileReader("voting_data.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.contains("Representative District"))
                    writer.println(line.substring(line.indexOf("ct ") + 3, line.indexOf(" -")));
                if (line.contains("REP")){
                    writer.println(line);
                    br.readLine();
                    line = br.readLine();
                    writer.println(line.substring(0,line.indexOf("%")));
                }
                if (line.contains("DEM")) {
                    writer.println(line);
                    br.readLine();
                    line = br.readLine();
                    writer.println(line.substring(0,line.indexOf("%")));
                }
            }
        }

        writer.close();

    }
}
