import java.util.*;

public class StartPopulationMod extends StartPopulation {
    int heuristicValue;
    int populationSize = 28;

    @Override
    public List<Map<List<NIRData>, Integer>> StartPopulationForming(ArrayList<ArrayList<NIRData>> tasks) {
        List<Map<List<NIRData>, Integer>> individualWithFitness = new ArrayList<>();

        for (ArrayList<NIRData> inner : tasks) {
            List<List<NIRData>> startPopulation = getStartPopulation(inner, populationSize);
            Map<List<NIRData>, Integer> Individual = new HashMap<>();
            ArrayList<Map<NIRData, Integer>> mapList = new ArrayList<>();
            Map<NIRData, Integer> combination = new HashMap<>();
            for (NIRData detail : inner) {

                startTime = detail.getStartTime();
                directiveDeadline = detail.getDirectiveDeadline();
                leadTime = detail.getLeadTime();

                heuristicValue = (startTime + directiveDeadline) / leadTime;
                combination.put(detail, heuristicValue);
            }
            Map<NIRData, Integer> sorted = sortByValue(combination, true);
            mapList.add(sorted);
            Map<NIRData, Integer> sorted2 = sortByValue(combination, false);
            mapList.add(sorted2);


            for (Map<NIRData, Integer> map : mapList) {
                List<NIRData> nirDataList = new ArrayList<>(map.keySet());
                startPopulation.add(nirDataList);
            }
            for (List<NIRData> individuals : startPopulation) {
                totalTime = 0;
                F = 0;
                List<NIRData> immutableIndividuals = Collections.unmodifiableList(individuals);

                for (NIRData chromosome : individuals) {
                    directiveDeadline = chromosome.getDirectiveDeadline();
                    leadTime = chromosome.getLeadTime();
                    if (totalTime == 0) {
                        startTime = chromosome.getStartTime();
                    } else {
                        startTime = Math.max(chromosome.getStartTime(), totalTime);
                    }
                    int endTime = startTime + leadTime;
                    if (endTime <= chromosome.getDirectiveDeadline()) {
                        F++;
                    }
                    totalTime = endTime;
                }
                Individual.put(immutableIndividuals, F);
            }
            individualWithFitness.add(Individual);
        }
        return individualWithFitness;
    }

    public static Map<NIRData, Integer> sortByValue(Map<NIRData, Integer> map, boolean operator) {
        List<Map.Entry<NIRData, Integer>> list = new ArrayList<>(map.entrySet());
        if (operator) {
            list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        } else {
            list.sort(Map.Entry.comparingByValue(Comparator.naturalOrder()));
        }
        Map<NIRData, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<NIRData, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
}
