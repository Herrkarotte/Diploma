import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.iterators.PermutationIterator;

import java.util.ArrayList;
import java.util.List;
/*
 * Класс реализующий метод решения путем полного перебора(Brut Force):
 * Метод bfSolver:
 * метод получает список задач, для каждой задачи генерируются все возможные перестановки без повторений. Далее
 * для каждой перестановки производится поиск значения критерия(подробнее о критерии в файле Heuristic),
 * после чего происходит поиск максимального значения критерия среди перестановок внутри задачи,
 * а так же среди всех задач.
 *
 * метод printResul():
 * производит печать полученного лучшего критерия и перестановки среди всех задач, а так же времени,
 * затраченного на выполнения решения всех задач.
 * */

public class BF {
    int listOfTaskSize;

    int startTime;
    int leadTime;

    int totalTime;
    int F;
    int maxF = 0;
    int globalMaxF = 0;
    long programStartTime = System.currentTimeMillis();

    List<NIRData> tmpResult;
    List<NIRData> tmpGlobalResult;

    public void bfSolver(ArrayList<ArrayList<NIRData>> tasks) {
            listOfTaskSize=tasks.size();
        for (ArrayList<NIRData> inner : tasks) {
            PermutationIterator<NIRData> permutationIterator = new PermutationIterator<>(inner);
            List<List<NIRData>> permutations = IteratorUtils.toList(permutationIterator);
            maxF = 0;
            for (List<NIRData> permutation : permutations) {
                F = 0;
                totalTime = 0;
                for (NIRData detail : permutation) {
                    leadTime = detail.getLeadTime();
                    if (totalTime == 0) {
                        startTime = detail.getStartTime();
                    } else {
                        startTime = Math.max(detail.getStartTime(), totalTime);
                    }
                    int endTime = startTime + leadTime;
                    if (endTime <= detail.getDirectiveDeadline()) {
                        F++;
                    }
                    totalTime = endTime;
                }

                if (F >= maxF) {
                    maxF = F;
                    tmpResult = permutation;
                }
            }
            //System.out.println(maxF);
            if (maxF >= globalMaxF) {
                globalMaxF = maxF;
                tmpGlobalResult = tmpResult;
            }

        }
    }

    public void printResult() {
        System.out.print("Лучшая перестановка, полученная путем полного перебора за "+listOfTaskSize+" задач:[ ");
        for (NIRData detail : tmpGlobalResult) {
            System.out.print(detail.getNumberOfDetails() + " ");
        }
        System.out.println("] Значение критерия: " + globalMaxF);

        // вычисляем время работы программы:
        long endTime = System.currentTimeMillis();
        long duration = endTime - programStartTime;
        System.out.println("Время выполнения: " + duration + " мс " + duration / 1000 + " сек");
    }

}

