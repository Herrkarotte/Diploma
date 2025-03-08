import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.iterators.PermutationIterator;


import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


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

    int availableProcessors = (Runtime.getRuntime().availableProcessors())/2;

    int listOfTaskSize;
    private static int globalMaxF = 0;
    long programStartTime = System.currentTimeMillis();

    List<NIRData> tmpResult;
    private static List<NIRData> tmpGlobalResult;

    int numOfExecutors = (int)(availableProcessors*0.84f);
    int numOfConsumers = availableProcessors-numOfExecutors;

    private static final AtomicInteger completedTasks = new AtomicInteger(0);
    List<List<NIRData>> POISON_PILL = new ArrayList<>();

    public void bfSolver(ArrayList<ArrayList<NIRData>> tasks) {
        listOfTaskSize = tasks.size();
        BlockingDeque<List<List<NIRData>>> queue = new LinkedBlockingDeque<>(100);
        ExecutorService generators = Executors.newFixedThreadPool(numOfExecutors);
        ExecutorService consumers = Executors.newFixedThreadPool(numOfConsumers);

        CountDownLatch latch = new CountDownLatch(numOfConsumers);

        for (ArrayList<NIRData> task : tasks) {
            generators.submit(() -> {
                generatePermutation(task, queue);
                if (completedTasks.incrementAndGet() == tasks.size()) {
                    try {
                        for (int j = 0; j < numOfConsumers; j++) {
                            queue.put(POISON_PILL);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
        for (int i = 0; i < numOfConsumers; i++) {
            consumers.submit(() -> {
                while (true) {
                    try {
                        List<List<NIRData>> permutations = queue.take();
                        if (permutations == POISON_PILL) {
                            latch.countDown();
                            break;
                        }
                        solveTask(permutations);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }
        generators.shutdown();
        consumers.shutdown();
        try {
            latch.await();
        } catch (InterruptedException e) {
            System.out.println("Завершение было прервано");
        }
    }

    private void generatePermutation(ArrayList<NIRData> task, BlockingDeque<List<List<NIRData>>> queue) {
        try {
            PermutationIterator<NIRData> permutationIterator = new PermutationIterator<>(task);
            List<List<NIRData>> permutation = IteratorUtils.toList(permutationIterator);
            queue.put(permutation);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void solveTask(List<List<NIRData>> permutations) {
        int startTime;
        int leadTime;

        int totalTime;
        int F;
        int maxF;
        maxF = 0;
        F = 0;
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
        synchronized (BF.class) {
            if (maxF >= globalMaxF) {
                globalMaxF = maxF;
                tmpGlobalResult = tmpResult;
            }
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
