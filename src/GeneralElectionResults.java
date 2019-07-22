import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class GeneralElectionResults {
    public static void main(String[] args) throws IOException {
        Map<String, Integer> partyPopulations = new HashMap<>();
        partyPopulations.put("REP", 0);
        partyPopulations.put("DEM", 0);
        partyPopulations.put("misc", 0);
        int repRep = 0;
        int demRep = 0;

        int currentRep = 0;
        int currentDem = 0;
        List<Integer> populations = new LinkedList<>();
        int distPop = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("2018.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.equals("REP")){
                    int number =Integer.parseInt(br.readLine().replace(",",""));
                    partyPopulations.put("REP",partyPopulations.get("REP") + number);
                    currentRep = number;
                    distPop += number;
                }
                if (line.equals("DEM")){
                    currentDem = Integer.parseInt(br.readLine().replace(",",""));
                    partyPopulations.put("DEM",partyPopulations.get("DEM") + currentDem);
                    distPop += currentDem;
                }
                if (line.equals("LIB") || line.equals("IND") || line.equals("W-I"))
                {
                    int number = Integer.parseInt(br.readLine().replace(",",""));
                    partyPopulations.put("misc", partyPopulations.get("misc") + number);
                    distPop += number;
                }
                if (line.contains("Total"))
                {
                    if (currentDem > currentRep) demRep++;
                    else repRep++;

                    populations.add(distPop);
                    distPop=0;
                }
//                String[] values = line.split(",");
//                LinkedList<String> temp = new LinkedList<>(Arrays.asList(values));
//                countyPopulationsSpreadsheet.add(temp);
            }
        }
        System.out.println(partyPopulations);
        double total = partyPopulations.get("REP") + partyPopulations.get("DEM") + partyPopulations.get("misc");
        System.out.println(partyPopulations.get("REP")/total);
        System.out.println("Republicans: " + repRep + " " + (double)repRep/(repRep+demRep) + " Democrats: " + demRep);
        System.out.println(populations.size());
        System.out.println(populations);
        System.out.println(calculateSD(populations));
        double totall = 0;
        for (Integer i : populations) totall+=i;
        System.out.println(totall/36);



    }
    public static double calculateSD(List<Integer> numbers)
    {
        double sum = 0.0, standardDeviation = 0.0;
        int length = numbers.size();
        for(double num : numbers) {
            sum += num;
        }
        double mean = sum/length;
        for(double num: numbers) {
            standardDeviation += Math.pow(num - mean, 2);
        }
        return Math.sqrt(standardDeviation/length);
    }
}
