import java.util.ArrayList;
import java.util.*;

/*
 *Класс реализующий решение задачи при помощи эвристического метода
 *
 * Метод heuristicSole():
 * метод получает список задач, для каждой задачи метод вычисляет значение эвристики по формуле:
 * d+D/t ((начало изготовления+ Директивный срок)/время выполнения), после чего присваивает это значение каждой детали
 * в задаче, формируя тем самым список пар(*) вида: деталь+ значение эвристики, далее вызывается вспомогательный метод
 * sortByValue(). Получив отсортированный список метод выполняет подсчет критерия. После подсчета критерия
 * метод выполняет поиск максимального критерия среди всех задач.
 *
 * Критерий:
 * Для данной задачи критерием является максимизация количества ненарушенных директивных сроков.
 * Производится подсчет количества деталей которые не нарушили свой директивный срок, то есть
 * если время работы(время начала выполнения(**)+ время выполнения) для детали не превышает ее директивный срок
 * счетчик увеличивается на единицу, иначе не увеличивается.
 *
 * sortByValue():
 * получает список пар (деталь+ значение) и сортирует его в порядке убывания значения, после чего возвращает
 * отсортированный список.
 *
 * printResult():
 * производит печать лучшей среди всех задач, перестановки и ее значение критерия, а так же время затраченное
 * на решение всех задач данным методом.
 *
 * (*)- в данной программе в роли списка пар выступает структура данных Map, в роли ключа принимается
 * деталь(Объект NIRData), а в роли значения принимается значение эвристики.
 * В дальнейшем (например в реализации эволюционно генетического алгоритма) Map будет принимать в роли ключа
 * перестановку из деталей (объектов NIRData), а в роли значения будет выступать значение критерия.
 *
 * (**)- время начала выполнения детали может не совпадать с предполагаемым временем начала
 *  прописанной в самой детали, такое может произойти если время выполнения прошлых деталей оказалось больше
 *  чем предполагаемое время начала выполнения детали, детали придется начать свое выполнение
 *  позднее чем это предполагалось.
 * */
public class Heuristic {
    int startTime;
    int directiveDeadline;
    int leadTime;

    // значение эвристики для каждой детали
    int heuristicValue;
    int totalTime;
    int F;
    //переменная хранящая лучшую перестановку среди всех задач.
    Map<NIRData, Integer> tmp_result;
    //переменная хранящая лучшее значение критерия среди всех задач.
    int maxF = 0;

    long programStartTime = System.currentTimeMillis();


    public void heuristicSolve(ArrayList<ArrayList<NIRData>> task) {
        ArrayList<Map<NIRData, Integer>> mapList = new ArrayList<>();
        for (ArrayList<NIRData> inner : task) {
            Map<NIRData, Integer> combination = new HashMap<>();
            for (NIRData detail : inner) {

                startTime = detail.getStartTime();
                directiveDeadline = detail.getDirectiveDeadline();
                leadTime = detail.getLeadTime();

                heuristicValue = (startTime + directiveDeadline) / leadTime;
                combination.put(detail, heuristicValue);
            }
            Map<NIRData, Integer> sorted = sortByValue(combination);
            mapList.add(sorted);
        }

        for (Map<NIRData, Integer> inner : mapList) {
            F = 0;
            totalTime = 0;
            for (Map.Entry<NIRData, Integer> entry : inner.entrySet()) {
                int startTime;
                int leadTime = entry.getKey().getLeadTime();

                if (totalTime == 0) {
                    startTime = entry.getKey().getStartTime();
                } else {
                    startTime = Math.max(entry.getKey().getStartTime(), totalTime);
                }

                int endTime = startTime + leadTime;

                if (endTime <= entry.getKey().getDirectiveDeadline()) {
                    F++;
                }
                totalTime = endTime;
            }
            //System.out.println(F);
            if (F >= maxF) {
                maxF = F;
                tmp_result = inner;
            }
        }
    }

    public static Map<NIRData, Integer> sortByValue(Map<NIRData, Integer> map) {
        List<Map.Entry<NIRData, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        Map<NIRData, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<NIRData, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    public void printResult() {
        System.out.print("Лучшая перестановка, полученная эвристикой за 100 задач:[");
        for (Map.Entry<NIRData, Integer> entry : tmp_result.entrySet()) {
            int part = (entry.getKey().getNumberOfDetails());
            System.out.print(part + " ");
        }
        System.out.println("] Итоговое значение критерия: " + maxF);

        //вычисляем время работы программы:
        long endTime = System.currentTimeMillis();
        long duration = endTime - programStartTime;
        System.out.println("Время выполнения: " + duration + " мс");


    }
}


