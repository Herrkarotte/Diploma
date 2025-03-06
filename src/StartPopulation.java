import java.util.*;

/*
* Класс реализующий генерацию стартового(начального) поколения
* Метод StartPopulationForming():
* Метод получает список задач, после чего формирует начальное поколение для каждой задачи
* путем вызова метода getStartPopulation(). Далее, получив список перестановок, для каждой перестановки вычисляется
* значение критерия (подробнее о критерии в файле Heuristic) после чего формируется начальное поколение вида:
* (особь(перестановка), приспособленность(значение критерия)
*
* getStartPopulation():
* метод получает список деталей(то есть одну задачу) и генерирует некоторое количество(*) случайных перестановок,
* после чего формирует из них список и возвращает.
*
* (*)- количество перестановок определяет размер начальной популяции.
*
* */

public class StartPopulation {
    int directiveDeadline;
    int leadTime;
    int totalTime;
    int startTime;
    int F = 0;

    public List<Map<List<NIRData>, Integer>> StartPopulationForming(ArrayList<ArrayList<NIRData>> tasks) {

        List<Map<List<NIRData>, Integer>> individualWithFitness = new ArrayList<>();

        for (ArrayList<NIRData> inner : tasks) {
            List<List<NIRData>> startPopulation = getStartPopulation(inner);

            Map<List<NIRData>, Integer> Individual = new HashMap<>();

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

    public List<List<NIRData>> getStartPopulation(List<NIRData> list) {
        Set<List<NIRData>> uniquePermutations = new HashSet<>();
        Random random = new Random();

        float maxPermutations = factorial(list.size());

        while (uniquePermutations.size() < 30 && uniquePermutations.size() < maxPermutations) {
            List<NIRData> shuffledList = new ArrayList<>(list);
            Collections.shuffle(shuffledList, random);
            uniquePermutations.add(shuffledList);
        }

        return new ArrayList<>(uniquePermutations);
    }

    private float factorial(float n) {
        if (n <= 1) return 1;
        return n * factorial(n - 1);
    }
}
