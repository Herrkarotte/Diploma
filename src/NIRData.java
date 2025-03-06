import java.util.Objects;
/*
* Класс NIRData описывает объект "деталь".
* Реализует методы возвращения значения, для получения доступа к полям класса.
* Переопределяет методу equals() и hashCode(), для того чтобы получить возможно принимать в качестве ключа
* перестановку из элементов данного класса (List<NIRData>).
*
* */

public class NIRData {
    // номер детали
    private final int numberOfDetails;
    // предполагаемое время начала исполнения детали на станке
    private final int startTime;
    // директивный срок выполнения детали
    private final int directiveDeadline;
    // время выполнения детали
    private final int leadTime;


    public NIRData(int num, int sT, int dDl, int lT) {
        numberOfDetails = num;
        startTime = sT;
        directiveDeadline = dDl;
        leadTime = lT;
    }

    public int getNumberOfDetails() {
        return numberOfDetails;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getDirectiveDeadline() {
        return directiveDeadline;
    }

    public int getLeadTime() {
        return leadTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NIRData data = (NIRData) o;
        return numberOfDetails == data.numberOfDetails &&
                startTime == data.startTime &&
                directiveDeadline == data.directiveDeadline &&
                leadTime == data.leadTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numberOfDetails, startTime, directiveDeadline, leadTime);
    }
}