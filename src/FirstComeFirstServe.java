import java.util.ArrayList;
import java.util.PriorityQueue;

public class FirstComeFirstServe {
    private ArrayList<Process> processArrayList;
    private ArrayList<Integer> pageFaultList = new ArrayList<>();

    private int numberOfProcess;
    private int blockedTime;
    private int turnAroundTime;
    private int waitingTime;
    private int totalTime;
    private int totalFaults;

    public FirstComeFirstServe(ArrayList<Process> processArrayList, int numberOfProcess) {
        this.processArrayList = processArrayList;
        this.numberOfProcess = numberOfProcess;
        this.blockedTime = 0;
        this.turnAroundTime = 0;
        this.waitingTime = 0;
        this.totalTime = 0;
        this.totalFaults = 0;
    }

    public FirstComeFirstServe() {}

    public ArrayList<Process> getProcessArrayList() {
        return processArrayList;
    }

    public void setProcessArrayList(ArrayList<Process> processArrayList) {
        this.processArrayList = processArrayList;
    }

    public ArrayList<Integer> getPageFaultList() {
        return pageFaultList;
    }

    public void setPageFaultList(ArrayList<Integer> pageFaultList) {
        this.pageFaultList = pageFaultList;
    }

    public int getNumberOfProcess() {
        return numberOfProcess;
    }

    public void setNumberOfProcess(int numberOfProcess) {
        this.numberOfProcess = numberOfProcess;
    }

    public int getExecutionTime() {
        return blockedTime;
    }

    public void setExecutionTime(int blockedTime) {
        this.blockedTime = blockedTime;
    }

    public double getTurnAroundTime() {
        return (double)turnAroundTime / (double)numberOfProcess;
    }

    public void setTurnAroundTime(int turnAroundTime) {
        this.turnAroundTime = turnAroundTime;
    }

    public double getWaitingTime() {
        return (double)waitingTime / (double)numberOfProcess;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    public int getTotalFaults() {
        return totalFaults;
    }

    public void setTotalFaults(int totalFaults) {
        this.totalFaults = totalFaults;
    }

    public int[] handlePageFault(Process aProcess, PriorityQueue<Process> processPriorityQueue, ProcessOperation operation, int totalFaults, int blockedTime, int totalTime) {
        int[] frameArray = aProcess.getLRUFrameArray();
        int[] aPageRef = aProcess.getPageRefs();
        int freeSlot = operation.findFreeSlot();
        int[] returnVal = new int[2];
        totalFaults += 1;
        aProcess.pageFault += 1;
        processPriorityQueue.remove();
        blockedTime = Math.max(aProcess.getProcessArrivalTime(), blockedTime);
        blockedTime = Math.max(blockedTime, totalTime);
        blockedTime += 60;
        aProcess.processArrivalTime = blockedTime;
        frameArray[freeSlot] = aPageRef[aProcess.alreadyExecutedIdx];
        operation.fixFramePosition(freeSlot);
        processPriorityQueue.add(aProcess);
        returnVal[0] = totalFaults;
        returnVal[1] = blockedTime;
        return returnVal;
    }

    public int calculateExecTime(Process aProcess) {
        int execTime = 0;
        int[] pageRef = aProcess.getPageRefs();
        ProcessOperation operation = new ProcessOperation(aProcess);

        while(aProcess.alreadyExecutedIdx < aProcess.getPageRefs().length &&
                operation.getFrameID(pageRef[aProcess.alreadyExecutedIdx]) != -1) {
            int frameID = operation.getFrameID(pageRef[aProcess.alreadyExecutedIdx]);
            operation.fixFramePosition(frameID);
            execTime += 30;
            aProcess.alreadyExecutedIdx += 1;
        }
        return execTime;
    }

    public void FCFS() {
        PriorityQueue<Process> processPriorityQueue = new PriorityQueue<>(new ArrivalTimeCompFCFS());
        for(Process process: processArrayList) {
            processPriorityQueue.add(process);
            turnAroundTime -= process.getProcessArrivalTime();
            waitingTime -= new ProcessOperation(process).getExecutionTime();
        }

        int[] aPageRef;
        int[] returnPageFault;
        while(!processPriorityQueue.isEmpty()) {
            Process aProcess = processPriorityQueue.peek();
            ProcessOperation processOperation = new ProcessOperation(aProcess);
            aPageRef = aProcess.getPageRefs();

            if(aProcess.alreadyExecutedIdx >= aProcess.getPageRefs().length) {
                turnAroundTime += totalTime;
                processPriorityQueue.remove();
            } else {
                if(aProcess.getProcessArrivalTime() > totalTime) totalTime = aProcess.getProcessArrivalTime();

                if(processOperation.getFrameID(aPageRef[aProcess.alreadyExecutedIdx]) == -1) {
                    returnPageFault = handlePageFault(aProcess, processPriorityQueue, processOperation, totalFaults, blockedTime, totalTime);
                    totalFaults = returnPageFault[0];
                    blockedTime = returnPageFault[1];
                } else {
                    totalTime += calculateExecTime(aProcess);
                }
            }
        }
        waitingTime += turnAroundTime;
        for(Process process: processArrayList) {
            pageFaultList.add(process.pageFault);
        }
    }
}
