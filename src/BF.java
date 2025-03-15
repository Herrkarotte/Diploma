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
 * Метод generatePermutation():
 * получает задачу и очередь. Генерирует все возможные перестановки для задачи,
 * после чего загружает список перестановок в очередь для дальнейшей обработки.
 *
 * Метод solveTask():
 * получает список всех перестановок и производит вычисления критерия и поиск локального(для задачи)
 * и глобального(среди всех задач) максимума критерия.
 *
 * метод printResul():
 * производит печать полученного лучшего критерия и перестановки среди всех задач, а так же времени,
 * затраченного на выполнения решения всех задач.
 *
 * Количество доступных ядер процессора получается путем деления количества потоков путем деления на 2, подразумевая,
 * что на каждое ядро процессора приходится 2 потока.
 *
 * Соотношение 84 к 16 было оптимальным на моей системе и было принято за основу для вычисления соотношения для
 * других процессоров с другим количеством ядер/потоков. Например, для 12 ядер будет получено соотношение 10
 * потоков-генераторов к 2 потокам-обработчикам, что может быть не самым оптимальным соотношением для такого процессора.
 * */

public class BF {
    // определяем доступное количество ядер на процессоре
    int availableProcessors = (Runtime.getRuntime().availableProcessors()) / 2;
    // количество потоков-генераторов и потоков-обработчиков в соотношении 84%к16%
    int numOfGenerators = (int) (availableProcessors * 0.84f);
    int numOfConsumers = availableProcessors - numOfGenerators;

    // переменные для поиска лучшего решения среди всех выполненных задач
    private static int globalMaxF = 0;
    private static List<NIRData> tmpGlobalResult;
    //вспомогательные переменные
    int listOfTaskSize;
    long programStartTime = System.currentTimeMillis();
    List<NIRData> tmpResult;


    private static final AtomicInteger completedTasks = new AtomicInteger(0);
    List<List<NIRData>> POISON_PILL = new ArrayList<>();

    public void bfSolver(ArrayList<ArrayList<NIRData>> tasks) {

        listOfTaskSize = tasks.size();
        //очередь для синхронизации потоков-генераторов и обработчиков
        BlockingDeque<List<List<NIRData>>> queue = new LinkedBlockingDeque<>(100);
        //пулы потоков генераторов и обработчиков
        ExecutorService generators = Executors.newFixedThreadPool(numOfGenerators);
        ExecutorService consumers = Executors.newFixedThreadPool(numOfConsumers);
        // счетчик завершенных потоков обработчиков для корректного завершения выполнения метода
        CountDownLatch latch = new CountDownLatch(numOfConsumers);
        // запуск потоков-генераторов
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
        // запуск потоков-обработчиков
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

    // генерация перестановок для задач.
    private void generatePermutation(ArrayList<NIRData> task, BlockingDeque<List<List<NIRData>>> queue) {
        try {
            PermutationIterator<NIRData> permutationIterator = new PermutationIterator<>(task);
            List<List<NIRData>> permutation = IteratorUtils.toList(permutationIterator);
            queue.put(permutation);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    //обработка перестановок.
    private void solveTask(List<List<NIRData>> permutations) {
        int startTime;
        int leadTime;
        int totalTime;
        int F;
        int maxF;
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