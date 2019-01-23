# Bank-Management-with-Zookeeper
 Managements information of the clients in a bank, provide fault tolerance and consistency

### Consistency
We needed to make sure that every server uses the same state of the database at all times and every operation is executed in the correct order. In order to do that, while read operations can be executed by any server (since they do not change the state of the DB), write operations can only be handled by the leader node of the Zookeeper ensemble. Therefore, if a follower node receives a write request, they have to forward it to the leader node first which executes the operations and informs every follower about completed operations in the form of a broadcast message.


