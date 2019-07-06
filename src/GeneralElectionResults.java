import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class GeneralElectionResults {
    public static void main(String[] args) throws IOException {
        int repPop = 0;
        int demPop = 0;

        int repRep = 0;
        int demRep = 0;
        boolean newDistrict = true;
        int currentRep = 0;
        int currentDem = 0;
        List<List<String>> countyPopulationsSpreadsheet = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("2018.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.equals("REP")){
                    int number =Integer.parseInt(br.readLine().replace(",",""));
                    repPop+=number;
                    currentRep = number;
                }
                if (line.equals("DEM")){
                    currentDem = Integer.parseInt(br.readLine().replace(",",""));

                    demPop+=currentDem;

                }
                if (line.contains("Total"))
                {
                    if (currentDem > currentRep) demRep++;
                    else repRep++;
                }
//                String[] values = line.split(",");
//                LinkedList<String> temp = new LinkedList<>(Arrays.asList(values));
//                countyPopulationsSpreadsheet.add(temp);
            }
        }
        System.out.println("Republicans: " + repPop + " " + (double)repPop/(repPop+demPop) + " Democrats: " + demPop);
        System.out.println("Republicans: " + repRep + " " + (double)repRep/(repRep+demRep) + " Democrats: " + demRep);

    }
}
