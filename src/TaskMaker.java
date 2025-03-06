import java.util.ArrayList;
import java.util.Random;
/*
 * Класс для генерации задач.
 * Метод MakeTask():
 * генерирует для каждой задачи startTime(время начало выполнения детали) в диапазоне от 1 до 10,
 * directiveDeadline(директивный срок) в диапазоне от 15 до 20,
 * leadTime(время выполнения детали) в диапазоне от 3 до 10,
 * а так же порядковый номер для детали.
 * После этого формирует список из задачи и возвращает его.
 * */
public class TaskMaker {

    int startTime;
    int directiveDeadline;
    int leadTime;

    int sTMin = 1;
    int sTMax = 10;
    int ddlMin = 15;
    int ddlMax = 20;
    int lTMin = 3;
    int lTMax = 10;

    public ArrayList<ArrayList<NIRData>> MakeTask() {
        Random random = new Random();
        ArrayList<ArrayList<NIRData>> listNIRData = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            ArrayList<NIRData> innerList = new ArrayList<>();
            for (int j = 1; j <= 10; j++) {
                //генерация начального времени до тех пор, пока оно не попадет в нужный диапазон
                do {
                    startTime = (int) random.nextGaussian((float) ((sTMin + sTMax) / 2), 5.0);
                } while (startTime < sTMin || startTime > sTMax);

                // генерация директивного срока до тех пор, пока он не попадет в нужный диапазон
                do {
                    directiveDeadline = (int) random.nextGaussian((float) (ddlMin + ddlMax / 2), 5.0);
                } while (directiveDeadline < ddlMin || directiveDeadline > ddlMax);

                //генерация времени выполнения работы до тех пор, пока оно не попадет в нужный диапазон
                do {
                    leadTime = (int) random.nextGaussian(((float) (lTMin + lTMax) / 2), 5.0);
                } while (leadTime < lTMin || leadTime > lTMax);

                NIRData Task = new NIRData(j, startTime, directiveDeadline, leadTime);
                innerList.add(Task);
            }
            listNIRData.add(innerList);
        }
        return listNIRData;
    }
}
