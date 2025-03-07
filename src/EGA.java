import java.security.SecureRandom;
import java.util.*;
import java.util.List;

/*
 * Класс реализующий решение при помощи эволюционно генетического алгоритма (ЭГА).
 *
 * Порядок выполнения ЭГА: 1) выбор родительской пары 2) кроссовер 3) мутация 4)селекция
 * 5) условия останова (в случае успешного прохождения пункта 5 возврат на пункт 1).
 *
 * Метод egaSolver принимает список из начальных поколений и для каждого поколения реализует ЭГА.
 *
 * Ограничения в egaSolver: реализует ЭГА до тех пор, пока не будет выполнено условие
 * останова (для данной задачи 200 поколений или
 * 10 поколений без смены лучшей особи(то есть особи с лучшей приспособленностью(значением критерия)).
 *
 * egaSolver вызывает методы выбора родительской пары (parentSelector()), кроссовера(crossOver()),
 * мутации(geneticMutation()) и селекции(betaTournament).
 *
 * Выбор родительской пары parentSelector():
 * метод принимает стартовое поколение(начальную популяцию) и отбирает
 * особей с максимальным различием приспособленности(значения критерия) и формирует список родительских пар.
 *
 * метод crossOver():
 * получив список родителей формирует родительские пары и отправляет их во вспомогательный метод createChild(),
 * метод createChild() вызывается до тех пор, пока не будет создано два потомка отличных друг от друга и
 * уникальных для репродукционного множества.
 *
 * Каждый потомок полученный в методе createChild() проходит обновление критерия во
 * вспомогательном методе criterionUpdater().
 *
 * createChild реализует OX порядковый кроссовер, генерируется 2 случайные точки разрыва (точка начала и точка конца),
 * далее аллеи между этими точками копируются из первого родителя и переносятся в потомка с сохранением позиций,
 * после этого оставшиеся аллеи копируются из второго родителя начиная со второй точки разрыва, исключая уже имеющиеся.
 *
 * Метод criterionUpdater получает особь для обновления критерия.
 * Метод проходится по всем элементам перестановки(особи) и вычисляет значение критерия и возвращает особь
 * с обновленным критерием.
 * Подробнее о критерии в файле Heuristic.
 *
 * Метод geneticMutation() принимает репродукционное множество и с вероятностью 10% реализует генетическую мутацию.
 * Генетическая мутация: выбирается два случайных соседних элемента (первый и последний элементы
 * перестановки считаются соседними), далее эти элементы меняются местами.
 * Далее каждый полученный мутант проходит обновление критерия в методе criterionUpdater() и возвращается.
 *
 * Метод betaTournament() реализует селекцию бета-турниром:
 * метод получает список из стартового поколения и репродукционного множества, далее из старого поколения случайным
 * образом удаляется некоторое количество особей, а для репродукционного множества проводится отбор того же количества
 * особей путем бета-турнира.
 * Бета-турнир:
 * из репродукционного множества отбирается некоторое количество особей(в данном случае выбор происходит случайно)
 * далее эти особи проходят сравнение приспособленности(значений критерия) и особь с лучшей приспособленностью
 * получает право попасть в стартовое поколение.
 *
 * Метод egaPrintResul() выполняет печать лучшей особи(перестановки) и значения ее критерия среди всех задач,
 * а так же время, потраченное на решение этих задач.
 * */
public class EGA {

    // количество родительских пар и пар потомков
    int parentsAndChild = 5;
    //количество особей для удаления и для получения из бета-турнира
    int oldAndNewIndividual = 3;
    //количество участников для бета-турнира
    int candidatesForSelection = 2;
    //переменная для нахождения лучшего критерия среди всех задач
    int globalMaxF = 0;
    // переменная для хранения лучшей перестановки среди всех зада
    Map.Entry<List<NIRData>, Integer> tmp_result;

    long programStartTime = System.currentTimeMillis();

    // вспомогательный метод для получения результата, используемый в дочернем классе.
    public Map.Entry<List<NIRData>, Integer> getResult() {
        return tmp_result;
    }

    public List<Map<List<NIRData>, Integer>> parentSelector(Map<List<NIRData>, Integer> startPopulation) {
        List<Map.Entry<List<NIRData>, Integer>> entries = new ArrayList<>(startPopulation.entrySet());

        List<Map<List<NIRData>, Integer>> parents = new ArrayList<>();
        entries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        for (int i = 0; i < parentsAndChild; i++) {
            Map<List<NIRData>, Integer> parentOne = new HashMap<>();
            Map<List<NIRData>, Integer> parentTwo = new HashMap<>();

            parentOne.put(entries.get(i).getKey(), entries.get(i).getValue());
            parentTwo.put(entries.get(entries.size() - 1 - i).getKey(), entries.get(entries.size() - 1 - i).getValue());
            parents.add(parentOne);
            parents.add(parentTwo);
        }
        return parents;
    }


    public Map<List<NIRData>, Integer> crossOver(List<Map<List<NIRData>, Integer>> parents) {
        Map<List<NIRData>, Integer> reproductiveSet = new HashMap<>();

        Map<List<NIRData>, Integer> parentOne;
        Map<List<NIRData>, Integer> parentTwo;

        for (int i = 0; i < parentsAndChild; i++) {
            parentOne = parents.get(i);
            parentTwo = parents.get(i + (parents.size() / 2));

            Map<List<NIRData>, Integer> childOne;
            // выполняем получение потомка до тех пор, пока не получим уникальную для репродукционного множества особь
            do {
                childOne = createChild(parentOne, parentTwo);
            } while (reproductiveSet.containsKey(childOne.keySet().iterator().next()));

            Map<List<NIRData>, Integer> childTwo;
            // выполняем получения потомка до тех пор, пока не получим уникальную для репродукционного множества особь,
            // отличную от первого потомка(childOne)
            do {
                childTwo = createChild(parentTwo, parentOne);
            } while (reproductiveSet.containsKey(childTwo.keySet().iterator().next()) || childTwo.equals(childOne));

            reproductiveSet.putAll(childOne);
            reproductiveSet.putAll(childTwo);
        }
        return reproductiveSet;
    }

    public Map<List<NIRData>, Integer> createChild(Map<List<NIRData>, Integer> parentOne,
                                                   Map<List<NIRData>, Integer> parentTwo) {

        Map<List<NIRData>, Integer> child = new HashMap<>();

        List<NIRData> parentOneList = new ArrayList<>(parentOne.keySet().iterator().next());
        List<NIRData> parentTwoList = new ArrayList<>(parentTwo.keySet().iterator().next());

        SecureRandom secureRandom = new SecureRandom();
        int size = parentOneList.size();

        int start = secureRandom.nextInt(size - 1) + 1;
        int end = start + secureRandom.nextInt(size - start);

        List<NIRData> childList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            childList.add(null);
        }

        for (int i = start; i <= end; i++) {
            childList.set(i, parentOneList.get(i));
        }
        int currentIndex = (end + 1) % childList.size();
        int currentIndexInParentTwo = (end + 1) % parentTwoList.size();
        for (int i = 0; i < parentTwoList.size(); i++) {
            NIRData item = parentTwoList.get(currentIndexInParentTwo);
            if (!childList.contains(item)) {
                while (childList.get(currentIndex) != null) {
                    currentIndex = (currentIndex + 1) % size;
                }
                childList.set(currentIndex, item);
            }
            currentIndexInParentTwo = (currentIndexInParentTwo + 1) % parentTwoList.size();
        }
        child.put(childList, 0);
        child = criterionUpdater(child);

        return child;
    }

    public Map<List<NIRData>, Integer> criterionUpdater(Map<List<NIRData>, Integer> individuals) {
        int leadTime;
        int totalTime;
        int startTime;
        int F;

        for (Map.Entry<List<NIRData>, Integer> entry : individuals.entrySet()) {
            List<NIRData> list = entry.getKey();
            F = 0;
            totalTime = 0;
            for (NIRData chromosome : list) {
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
            entry.setValue(F);
        }

        return individuals;
    }

    public Map<List<NIRData>, Integer> geneticMutation(Map<List<NIRData>, Integer> population) {
        SecureRandom secureRandom = new SecureRandom();
        float probability;
        int startPoint;
        int endPoint;

        Map<List<NIRData>, Integer> mutants = new HashMap<>();
        for (Map.Entry<List<NIRData>, Integer> entry : population.entrySet()) {
            probability = secureRandom.nextFloat();
            if (probability <= 0.1f) {
                List<NIRData> list = entry.getKey();
                List<NIRData> newList = new ArrayList<>(list);

                startPoint = secureRandom.nextInt(list.size());
                endPoint = (startPoint + 1) % list.size();
                Collections.swap(newList, startPoint, endPoint);

                mutants.put(newList, 0);
            }
        }
        mutants = criterionUpdater(mutants);
        population.putAll(mutants);
        return population;
    }

    public Map<List<NIRData>, Integer> betaTournament(Map<List<NIRData>, Integer> oldPopulations,
                                                      Map<List<NIRData>, Integer> newPopulations) {

        SecureRandom secureRandom = new SecureRandom();

        Map<List<NIRData>, Integer> oldPopulation = oldPopulations;
        Map<List<NIRData>, Integer> newPopulation = newPopulations;

        List<List<NIRData>> keys = new ArrayList<>(oldPopulation.keySet());

        for (int i = 0; i < oldAndNewIndividual; i++) {
            int randomIndex = secureRandom.nextInt(keys.size());
            List<NIRData> randomKey = keys.get(randomIndex);
            oldPopulation.remove(randomKey);
            keys.remove(randomIndex);
        }
        Map<List<NIRData>, Integer> updatedPopulation = new HashMap<>(oldPopulation);
        Map<List<NIRData>, Integer> champions = new HashMap<>();
        for (int i = 0; i <oldAndNewIndividual; i++) {

            List<List<NIRData>> candidateKeys = new ArrayList<>(newPopulation.keySet());
            Map<List<NIRData>, Integer> candidatMap = new HashMap<>();
            for (int j = 0; j < candidatesForSelection; j++) {
                int size = candidateKeys.size();
                int randomIndex;
                List<NIRData> randomCandidateKey = new ArrayList<>();
                do {
                    randomIndex = secureRandom.nextInt(size);
                    randomCandidateKey = candidateKeys.get(randomIndex);
                } while (oldPopulation.containsKey(randomCandidateKey));
                Integer value = newPopulation.get(randomCandidateKey);
                candidatMap.put(randomCandidateKey, value);
                candidateKeys.remove(randomIndex);
            }
            Map.Entry<List<NIRData>, Integer> champion = null;
            for (Map.Entry<List<NIRData>, Integer> entry : candidatMap.entrySet()) {
                if (champion == null || entry.getValue() > champion.getValue()) {
                    champion = entry;
                }
            }
            newPopulation.remove(champion.getKey());
            champions.put(champion.getKey(), champion.getValue());
        }
        updatedPopulation.putAll(champions);
        return updatedPopulation;
    }

    public void egaSolver(List<Map<List<NIRData>, Integer>> startPopulations) {

        for (Map<List<NIRData>, Integer> startPopulation : startPopulations) {
            int generationCounter = 0;
            int withOutChangeCounter = 0;
            int maxFInPopulation = 0;
            Map.Entry<List<NIRData>, Integer> bestIndividual = null;
            Map<List<NIRData>, Integer> oldPopulation = new HashMap<>(startPopulation);
            Map<List<NIRData>, Integer> reproductiveSet;
            while (generationCounter < 200 && withOutChangeCounter < 10) {
                //System.out.println("Generation:" + generationCounter);
                List<Map<List<NIRData>, Integer>> parents = parentSelector(oldPopulation);
                reproductiveSet = crossOver(parents);
                reproductiveSet = geneticMutation(reproductiveSet);
                oldPopulation = betaTournament(oldPopulation, reproductiveSet);

                for (Map.Entry<List<NIRData>, Integer> entry : oldPopulation.entrySet()) {
                    if (bestIndividual == null || entry.getValue() > bestIndividual.getValue()) {
                        bestIndividual = entry;
                    }
                }

                if (bestIndividual.getValue() > maxFInPopulation) {
                    withOutChangeCounter = 0;
                    maxFInPopulation = bestIndividual.getValue();
                }
                generationCounter++;
                withOutChangeCounter++;
            }
            if (bestIndividual.getValue() > globalMaxF) {
                globalMaxF = bestIndividual.getValue();
                tmp_result = bestIndividual;
            }
            System.out.println(bestIndividual.getValue());
        }

    }

    public void egaPrintResult() {
        System.out.print("Лучшая перестановка, полученная при помощи ЭГА за 100 задач:[ ");
        List<NIRData> details = tmp_result.getKey();
        for (NIRData detail : details) {
            System.out.print(detail.getNumberOfDetails() + " ");
        }
        System.out.println("] Значение критерия: " + globalMaxF);

        // вычисляем время работы программы:
        long endTime = System.currentTimeMillis();
        long duration = endTime - programStartTime;
        System.out.println("Время выполнения: " + duration + " мс " + duration / 1000 + " сек");
    }
}