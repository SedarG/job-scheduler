package dev.gkblt.sdr.scheduler.components;

import dev.gkblt.sdr.scheduler.model.Job;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class HierarchicalTimerWheelSlice {
    private final int granularity;
    private final int levelSize;
    private final long maxRecurrence;

    private final long startTimestamp;
    LinkedList<Job>[][] entries;
    protected final int[] currentIndexes;
    /**
     *
     * @param granularity the size of the bucket of the lowest level, in milliseconds
     * @param levelSize how many buckets each level contains
     * @param levels how many levels there exists
     */
    public HierarchicalTimerWheelSlice(long startTimestamp, int granularity, int levelSize, int levels) {
        this.startTimestamp = startTimestamp;
        this.granularity = granularity;
        this.levelSize = levelSize;

        this.entries = new LinkedList[levels][levelSize];
        long maxRecurrence = 0;
        for (int l=0;l<levels;l++) {
            for (int i=0;i<levelSize;i++) {
                this.entries[l][i] = new LinkedList<Job>();
            }
            maxRecurrence += granularity * (long) Math.pow(levelSize, l+1);
        }

        this.maxRecurrence = maxRecurrence;
        this.currentIndexes = new int[levels];
    }


    public HierarchicalTimerWheelSlice(long startTimestamp, int granularity, int levelSize) {
        this(startTimestamp, granularity, levelSize, 4);
    }

    /**
     * The default parameterless constructor will generate a wheel
     * of 4 levels; the first one contains the immediate minute in 75 seconds
     * the second one contains the 75^2 seconds, in 75 buckets
     * and so on.
     * 75^4/60/60/24 is slightly more than 366 days
     * Any future scheduling beyond 366 days won't be possible.
     * That is, recurrence > 366 days won't be supported.
     *
     */
    public HierarchicalTimerWheelSlice(long startTimestamp) {
        this(startTimestamp, 1000,75);
    }

    public long getMaxRecurrence() {
        return this.maxRecurrence;
    }

    public long getStartTimestamp() {
        return this.startTimestamp;
    }

    private LinkedList<Job> locate(long time) {

        int l = 0;
        long levelStart = 0;
        long levelRange = levelSize;
        long levelEnd = levelStart + levelRange;
        while(time >= levelEnd) {
            l++;
            levelStart = levelEnd;
            levelRange = levelSize * levelRange;
            levelEnd = levelStart + levelRange;
        }
        if (l >= this.currentIndexes.length) {
            return null;
        }
        int idxIntoLevel = (int)((time - levelStart) / (levelRange/levelSize));
        return entries[l][idxIntoLevel];
    }

    public boolean remove(Job job) {
        if (job.nextSchedule() < startTimestamp || job.nextSchedule() >= startTimestamp + maxRecurrence) {
            return false;
        }
        long nextDue = (job.nextSchedule() - startTimestamp) / granularity;
        LinkedList<Job> jobSlot = locate(nextDue);

        return jobSlot != null && jobSlot.remove(job);
    }

    public boolean add(Job job) {
        if (job.nextSchedule() < startTimestamp || job.nextSchedule() >= startTimestamp + maxRecurrence) {
            return false;
        }
        long nextDue = (job.nextSchedule() - startTimestamp) / granularity;
        LinkedList<Job> jobSlot = locate(nextDue);
        if (jobSlot == null) {
            return false;
        }
        jobSlot.add(job);
        return true;
    }

    protected List<Job> jobsDue() {
        if (consolidateJobs(0)) {
            List<Job> due = (List<Job>) entries[0][currentIndexes[0]].clone();
            entries[0][currentIndexes[0]].clear();
            currentIndexes[0]++;
            return due;
        } else {
            return null;
        }
    }

    protected long levelBase(int level) {
        int l = 0;
        long levelStart = startTimestamp;
        long bucketWidth = granularity;
        while(l < level) {
            levelStart += bucketWidth*levelSize;
            bucketWidth *= granularity;
            l++;
        }
        return levelStart;
    }
    /**
     * moves jobs from one upper level to the current level, recursively
     * @param level
     */
    private boolean consolidateJobs(int level) {
        if (level == currentIndexes.length - 1) {
            return currentIndexes[level] < levelSize;
        }
        if (currentIndexes[level] < levelSize) {
            return true;
        }
        if (currentIndexes[level+1] < levelSize) {
            LinkedList<Job> nextLevelJobs = entries[level + 1][currentIndexes[level + 1]];
            long rangeStart = levelBase(level + 1);

            for (Job job : nextLevelJobs) {
                int indexIntoCurrentLevel = (int) (((job.nextSchedule() - rangeStart) % Math.pow(granularity, level + 2)) / Math.pow(granularity, level + 1));
                entries[level][indexIntoCurrentLevel].add(job);
            }

            entries[level + 1][currentIndexes[level + 1]].clear();
            if (level != currentIndexes.length - 1)
                currentIndexes[level + 1]++;
            currentIndexes[level] = 0;
            return true;
        } else {
            return consolidateJobs(level + 1) && consolidateJobs(level);
        }
    }
}
