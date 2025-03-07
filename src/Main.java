import java.util.*;

public class Main {
    public static void main(String[] args) {

        ArrayList<ArrayList<NIRData>> tasks = new TaskMaker().MakeTask();
        Heuristic heuristic = new Heuristic();
        heuristic.heuristicSolve(tasks);
        heuristic.printResult();
        BF bf = new BF();
        bf.bfSolver(tasks);
        bf.printResult();
        //List<Map<List<NIRData>, Integer>> startPopulation = new StartPopulation().StartPopulationForming(tasks);
        List<Map<List<NIRData>, Integer>> startPopulation = new StartPopulationMod().StartPopulationForming(tasks);
        EGA ega = new EGA();
        ega.egaSolver(startPopulation);
        ega.egaPrintResult();
        EGAmod egaMod = new EGAmod();
        egaMod.egaSolverCall(startPopulation);
        egaMod.printResult();
    }
    
}
