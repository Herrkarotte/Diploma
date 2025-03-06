import java.security.SecureRandom;
import java.util.*;
/*
* Класс реализующий ЭГА с замененным оператором.
* В данном случае заменен способ выбора родителей, теперь он происходит случайным образом
* остальные операторы остались без изменений и вызываются из родительского класса EGA
* */

public class EGAmod extends EGA {
    Map.Entry<List<NIRData>, Integer> result;
    @Override
    public List<Map<List<NIRData>, Integer>> parentSelector(Map<List<NIRData>, Integer> startPopulation) {
        // Преобразуем Map в список entry (ключ-значение)
        List<Map.Entry<List<NIRData>, Integer>> entries = new ArrayList<>(startPopulation.entrySet());

        // Перемешиваем список случайным образом
        Collections.shuffle(entries, new SecureRandom());

        // Создаем список для хранения родителей
        List<Map<List<NIRData>, Integer>> parents = new ArrayList<>();

        // Выбираем 150 уникальных родителей (по 2 за итерацию)
        for (int i = 0; i < parentsAndChild; i++) {
            Map<List<NIRData>, Integer> parentOne = new HashMap<>();
            Map<List<NIRData>, Integer> parentTwo = new HashMap<>();

            // Берем двух случайных родителей
            parentOne.put(entries.get(i * 2).getKey(), entries.get(i * 2).getValue());
            parentTwo.put(entries.get(i * 2 + 1).getKey(), entries.get(i * 2 + 1).getValue());

            // Добавляем родителей в список
            parents.add(parentOne);
            parents.add(parentTwo);
        }

        return parents;

    }
    public void egaSolverCall(List<Map<List<NIRData>, Integer>> startPopulations){
        super.egaSolver(startPopulations);
         result=getResult();
    }
    public void printResult(){
        System.out.print("Лучшая перестановка, полученная при помощи модифицированного ЭГА за 100 задач:[ ");
        List<NIRData> details = result.getKey();
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