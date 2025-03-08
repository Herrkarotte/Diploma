import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.iterators.PermutationIterator;


import java.util.concurrent.*;
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

    BlockingDeque<List<List<NIRData>>> queue = new LinkedBlockingDeque<>(100);
    CountDownLatch latch = new CountDownLatch(4);


    public void bfSolver(ArrayList<ArrayList<NIRData>> tasks) {

//        int mid = (tasks.size() + 1) / 2; // Учитываем нечетное количество задач
//        ArrayList<ArrayList<NIRData>> part1 = new ArrayList<>(tasks.subList(0, mid));
//        ArrayList<ArrayList<NIRData>> part2 = new ArrayList<>(tasks.subList(mid, tasks.size()));
        ArrayList<ArrayList<NIRData>> part1 = new ArrayList<>();
        ArrayList<ArrayList<NIRData>> part2 = new ArrayList<>();
        ArrayList<ArrayList<NIRData>> part3 = new ArrayList<>();

        int size = tasks.size();
        int partSize = (size + 2) / 3;

        for (int i = 0; i < partSize && i < size; i++) {
            part1.add(tasks.get(i));
        }

        for (int i = partSize; i < 2 * partSize && i < size; i++) {
            part2.add(tasks.get(i));
        }
        for (int i = 2 * partSize; i < size; i++) {
            part3.add(tasks.get(i));
        }

        listOfTaskSize = tasks.size();
        Thread producer = new Thread(() -> {
            for (ArrayList<NIRData> inner : part1) {
                try {
                    PermutationIterator<NIRData> permutationIterator = new PermutationIterator<>(inner);
                    List<List<NIRData>> permutations = IteratorUtils.toList(permutationIterator);
                    queue.put(permutations);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            latch.countDown();
        });
        Thread producer2 = null;
        if (!part2.isEmpty()) {
            producer2 = new Thread(() -> {
                for (ArrayList<NIRData> inner : part2) {
                    try {
                        PermutationIterator<NIRData> permutationIterator = new PermutationIterator<>(inner);
                        List<List<NIRData>> permutations = IteratorUtils.toList(permutationIterator);
                        queue.put(permutations);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                latch.countDown();
            });
        }
        Thread producer3 = null;
        if (!part3.isEmpty()) {
            producer3 = new Thread(() -> {
                for (ArrayList<NIRData> inner : part3) {
                    try {
                        PermutationIterator<NIRData> permutationIterator = new PermutationIterator<>(inner);
                        List<List<NIRData>> permutations = IteratorUtils.toList(permutationIterator);
                        queue.put(permutations);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                latch.countDown();
            });
        }
        Thread consumer = new Thread(() -> {
            for (ArrayList<NIRData> inner : tasks)
                try {
                    List<List<NIRData>> permutations = queue.take();

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

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

            latch.countDown();
        });
        producer.start();
        if (producer2 != null) producer2.start();
        if (producer3 != null) producer3.start();

        consumer.start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void printResult() {
        System.out.print("Лучшая перестановка, полученная путем полного перебора за " + listOfTaskSize + " задач:[ ");
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
