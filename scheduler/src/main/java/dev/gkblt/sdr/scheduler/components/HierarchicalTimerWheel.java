package dev.gkblt.sdr.scheduler.components;

import dev.gkblt.sdr.scheduler.model.Job;

import java.util.LinkedList;

public class HierarchicalTimerWheel {

    private final int granularity;
    private final int levelSize;

    private int startTimestamp;
    LinkedList<Job>[][] entries;
    private int[] currentIndexes;
    /**
     *
     * @param granularity the size of the bucket of the lowest level, in milliseconds
     * @param levelSize how many buckets each level contains
     * @param levels how many levels there exists
     */
    public HierarchicalTimerWheel(int granularity, int levelSize, int levels, int startTimestamp) {
        this.granularity = granularity;
        this.levelSize = levelSize;
        this.startTimestamp = startTimestamp;

        this.entries = new LinkedList[levels][levelSize];
        for (int l=0;l<levels;l++) {
            for (int i=0;i<levelSize;i++) {
                this.entries[l][i] = new LinkedList<Job>();
            }
        }
        this.currentIndexes = new int[levels];
    }

    public HierarchicalTimerWheel(int granularity, int levelSize, int levels) {
        this(granularity, levelSize, levels, 0);
    }
    public HierarchicalTimerWheel(int granularity, int levelSize) {
        this(granularity, levelSize, 4);
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
    public HierarchicalTimerWheel() {
        this(1000,75);
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
        if (job.nextSchedule() < startTimestamp) {
            return false;
        }
        long nextDue = (job.nextSchedule() - startTimestamp) / granularity;
        LinkedList<Job> jobSlot = locate(nextDue);

        return jobSlot != null && jobSlot.remove(job);
    }

    public boolean add(Job job) {
        if (job.nextSchedule() < startTimestamp) {
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
}
