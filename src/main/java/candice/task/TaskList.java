package candice.task;

import candice.Storage;

import java.io.BufferedReader;
import java.io.IOException;

import java.nio.file.Files;

import java.time.LocalDate;
import java.time.LocalTime;

import java.util.ArrayList;

public class TaskList {
    private final Storage storage;
    private final ArrayList<Task> taskArrayList = new ArrayList<>();

    public TaskList(Storage storage) {
        this.storage = storage;
    }

    public void parseTaskListText() {
        try {
            BufferedReader reader = Files.newBufferedReader(this.storage.getPath());
            String nextLine = reader.readLine();
            Task nextTask;

            while (nextLine != null) {
                String[] taskDescription = nextLine.split(", ");
                boolean isFinished = taskDescription[1].equals("finished");
                String taskName = taskDescription[2];

                if (taskDescription[0].equals("[T]")) { // Todo task
                    nextTask = new Task.ToDo(taskName);
                } else { // Timed task
                    String dateAsString = taskDescription[3];
                    LocalDate date = LocalDate.parse(dateAsString);

                    if (taskDescription[0].equals("[D]")) { // Deadline task
                        String deadlineTimeAsString = taskDescription[4];
                        LocalTime deadlineTime = deadlineTimeAsString.equals("no time given")
                                ? null
                                : LocalTime.parse(deadlineTimeAsString);
                        nextTask = new TimedTask.Deadline(taskName, date, deadlineTime);
                    } else { // Event task
                        LocalTime eventStartTime;
                        LocalTime eventEndTime;

                        if (taskDescription[4].equals("no time given")) {
                            eventStartTime = null;
                            eventEndTime = null;
                        } else {
                            eventStartTime = LocalTime.parse(taskDescription[4]);
                            eventEndTime = LocalTime.parse(taskDescription[5]);
                        }

                        nextTask = new TimedTask.Event(taskName, date, eventStartTime, eventEndTime);
                    }
                }

                if (isFinished) {
                    nextTask.setFinished();
                }

                this.taskArrayList.add(nextTask);

                nextLine = reader.readLine();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }


    public int getLength() {
        return this.taskArrayList.size();
    }

    public void addTask(Task newTask) {
        taskArrayList.add(newTask);
        String taskDescription = newTask.getStorageDescription();
        this.storage.addTask(taskDescription);
    }

    public void checkValidTaskNumber(int taskNumber) throws IllegalArgumentException {
        if (taskNumber <= 0 || taskNumber > this.getLength()) {
            throw new IllegalArgumentException("The task number you put is wrong bro. The task must exist for " +
                    "you to delete it.");
        }
    }

    public Task deleteTask(int taskNumber) throws IllegalArgumentException {
        checkValidTaskNumber(taskNumber);
        Task removedTask = taskArrayList.remove(taskNumber - 1);
        updateStorage();
        return removedTask;
    }

    public Task markTask(int taskNumber) throws IllegalArgumentException {
        checkValidTaskNumber(taskNumber);
        Task selectedTask = taskArrayList.get(taskNumber - 1);
        selectedTask.setFinished();
        updateStorage();
        return selectedTask;
    }

    public Task unmarkTask(int taskNumber) throws IllegalArgumentException {
        checkValidTaskNumber(taskNumber);
        Task selectedTask = this.taskArrayList.get(taskNumber - 1);
        selectedTask.setUnfinished();
        updateStorage();
        return selectedTask;
    }

    /**
     * Returns a String representing all the tasks that contain the keyword in the task name.
     *
     * @param taskKeyword The keyword that is inside the name of the tasks.
     * @return A String containing all the tasks that contain the keyword.
     */
    public String findTasks(String taskKeyword) {
        StringBuilder taskList = new StringBuilder();
        int taskNumber = 1;

        for (int i = 0; i < this.getLength(); i++) {
            Task task = this.taskArrayList.get(i);
            String taskStatus = task.getStatus();

            if (taskStatus.contains(taskKeyword)) {
                taskList.append(taskNumber).append(".").append(taskStatus).append("\n");
                taskNumber++;
            }
        }

        return taskList.length() != 0
               ? taskList.substring(0, taskList.length() - 1) // to remove the last "\n"
               : taskList.toString();
    }

    private void updateStorage() {
        int taskListLength = this.getLength();
        String[] taskDescriptionArray = new String[taskListLength];
        for (int i = 0; i < taskListLength; i++) {
            taskDescriptionArray[i] = this.taskArrayList.get(i).getStorageDescription();
        }
        this.storage.update(taskDescriptionArray);
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < this.getLength(); i++) {
            Task task = this.taskArrayList.get(i);
            String taskNumber = String.valueOf(i + 1);
            output.append(taskNumber).append(".").append(task.getStatus()).append("\n");
        }

        return output.length() != 0
               ? output.substring(0, output.length() - 1) // to remove the last "\n"
               : output.toString();
    }
}
