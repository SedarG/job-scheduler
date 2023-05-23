# job-scheduler
## Overview
This is a classic systems-design interview question. It could help you understand what it means to design a job-scheduler.

The goal of the job scheduler is to provide a repository for users to schedule jobs and do CRUD on their jobs efficiently while providing an efficient algorithm to find out what's due.

### Algorithm
The data structure used to store jobs is a combination of a HashMap<userId, HashMap<jobId, Job>> and HierarchicalTimerWheel of Jobs. When a job is created, it will be recorded in both of these data structures and HTW will efficiently find the jobs that are due and the jobs will be executed. For demonstration purposes, the execution of the job is calling a Logging service to record the execution. 

#### HierarchicalTimerWheel (HTW)
HTW is a stack of circular buffers, representing buckets of layered granularities. It's based on [Hashed and hierarchical timing wheels: data structures for the efficient implementation of a timer facility](https://dl.acm.org/doi/10.1145/41457.37504). I then used a pair of these data structures to store the scheduled jobs in the respective slots. Each HTW is capable of storing jobs that are due in a fixed offset of time. The first HTW keeps the imminent jobs, the second one keeps the jobs that are due in the next window. When the first HTW is drained, the algorithm makes the second one the imminent and creates a new HTW for next window. 

## Status
The basic functionality is implemented and functional. Beyond that is T.B.D. for now.
Fun things to add:
* Abstractions on APIs and Storage at the service/process level
* Partitioning and Replication
* Ser/Deserialization to a file format to persist on a durable data store + WAL
* Allow jobs to define a set of dependencies, build a graph beside the HashMap and HTW to efficiently calculate the dependency closure. 
* Real executables as jobs
* Efficient and durable storage of executable packages/container images

## Running the example
* On a terminal, start the logging service using gradle:
```
~/job-scheduler/logging % ./gradlew bootRun
```
The logging service is hosted on http://localhost:8081
* On another terminal, start the scheduler service, the same way
```
~/job-scheduler/scheduler % ./gradlew bootRun
```
* Create a new job
```
% curl --header "Content-Type: application/json" \
--request POST \
--data "{\"userId\": 1, \"jobId\": 1, \"nextSchedule\":`date -d '+1 minutes' +%s`000 }" \ 
 http://localhost:8080/v1/users/1/jobs
```
* 1 minute later, you can query the logging service to see the log of the job
```
% curl http://localhost:8081/v1/logs
```
The log line would look like 
```
[{"timestamp":1684431087846,"entry":"Running {userID: 1, jobId: 1, nextSchedule: 1684431087000, recurrence: None"}]
```
The timestamp is the system time of the scheduler showing the time the log entry was created. nextSchedule shows the scheduling time of the task. In the above example the task is created 846 milliseconds after its scheduled time.
